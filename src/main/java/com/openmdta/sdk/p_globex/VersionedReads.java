package com.openmdta.sdk.p_globex;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;

/** Finite Catalog reads adapted by the gateway to this SDK's pinned owner contracts.
 * Numeric JSON values are exact strings. Stream feeds never use this adapter. */
public final class VersionedReads {
    private VersionedReads() {}
    private static final JsonNode CONTRACTS;
    static {
        try (var input = VersionedReads.class.getResourceAsStream("field-versions.json")) {
            if (input == null) throw new IllegalStateException("SDK has no retained version catalog");
            CONTRACTS = Contract.JSON.readTree(input);
        } catch (IOException error) { throw new ExceptionInInitializerError(error); }
    }
    public static JsonNode read(HttpClient client, URI gateway, String bearer, String datasetAlias,
            String selector, List<String> families, boolean allowLossy) throws IOException, InterruptedException {
        if (client.followRedirects() != HttpClient.Redirect.NEVER) throw new IllegalArgumentException("Versioned reads require redirects disabled");
        if (families.isEmpty() || families.size() > 64 || new HashSet<>(families).size() != families.size()) throw new IllegalArgumentException("Select 1–64 distinct families");
        var requests = Contract.JSON.createArrayNode();
        for (String family : families) {
            var request = requests.addObject();
            request.putObject("delivery").put("kind", "get");
            request.put("allowLossy", allowLossy);
            var supported = request.putArray("supported");
            var pins = request.putObject("pins");
            for (JsonNode contract : CONTRACTS) if (family.equals(contract.path("id").path("family").asText())) {
                supported.add(contract.path("id"));
                pins.put(contract.path("id").path("version").asText(), contract.path("sha256").asText());
            }
            if (supported.isEmpty()) throw new IllegalArgumentException("SDK does not know family " + family);
        }
        var url = gateway.resolve("/api/v1/datasets/" + URLEncoder.encode(datasetAlias, StandardCharsets.UTF_8).replace("+", "%20") + "/records?selector="
                + URLEncoder.encode(selector, StandardCharsets.UTF_8) + "&versions=" + URLEncoder.encode(requests.toString(), StandardCharsets.UTF_8));
        var response = client.send(HttpRequest.newBuilder(url).timeout(Duration.ofSeconds(30)).header("Authorization", "Bearer " + bearer).GET().build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new IOException("Versioned read failed: " + response.statusCode());
        var records = Contract.JSON.readTree(response.body());
        if (!records.isArray()) throw new IOException("Invalid versioned record response");
        for (JsonNode record : records) {
            if (!record.path("exists").isBoolean() || !record.path("versionedFields").isArray()) throw new IOException("Missing version negotiation result");
            var identities = new HashSet<String>();
            for (JsonNode field : record.path("versionedFields")) {
                boolean known = false;
                for (JsonNode contract : CONTRACTS) if (contract.path("id").equals(field.path("contract")) && families.contains(field.path("contract").path("family").asText())) {
                    known = contract.path("field").equals(field.path("field")) && contract.path("sha256").equals(field.path("conversion").path("targetSha256"));
                }
                String identity = field.path("contract").path("family").asText() + "\u0000" + field.path("subfield").toString();
                if (!known || !identities.add(identity) || !field.path("payloadBase64").isTextual() || !field.path("conversion").path("lossy").isBoolean() || (!allowLossy && field.path("conversion").path("lossy").asBoolean())) throw new IOException("Unrecognized negotiated contract");
            }
        }
        return records;
    }
}
