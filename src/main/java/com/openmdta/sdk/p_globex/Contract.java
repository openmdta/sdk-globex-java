package com.openmdta.sdk.p_globex;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.LinkedHashMap;

final class Contract {
    static final ObjectMapper JSON = new ObjectMapper();
    static final JsonNode ROOT;
    static final Map<String, Format> OPERATIONS = new LinkedHashMap<>();
    static final Map<String, Format[]> RESPONSES = new LinkedHashMap<>();
    static {
        try (var input = Contract.class.getResourceAsStream("contract.json")) {
            if (input == null) throw new IllegalStateException("Missing environment contract");
            ROOT = JSON.readTree(input);
            for (JsonNode operation : ROOT.path("runtime").path("operations")) {
                RESPONSES.put(operation.path("name").asText(), JSON.treeToValue(operation.path("responses"), Format[].class));
                OPERATIONS.put(operation.path("name").asText(), JSON.treeToValue(operation.path("request"), Format.class));
            }
        } catch (IOException error) { throw new ExceptionInInitializerError(error); }
    }
}
