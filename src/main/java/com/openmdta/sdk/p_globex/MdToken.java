package com.openmdta.sdk.p_globex;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import org.agrona.concurrent.UnsafeBuffer;
import com.openmdta.sdk.p_globex.sbe.mdtoken.*;
import java.util.zip.Deflater;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Server-side delegation. Never distribute the signing secret with the client artifact. */
public final class MdToken {
    private MdToken() {}
    public record Grant(String packageName, String quality) {}
    public record RateLimit(String bucket, BigInteger bucketSizeMicrotokens, BigInteger refillPerSecondMicrotokens) {}

    public static byte[] issue(String clientId, byte[] secret, String audience, Instant issuedAt, Instant expiresAt, List<Grant> grants, List<String> origins, RateLimit limit) {
        if (secret.length != 32 || clientId.isBlank() || audience.isBlank() || grants.isEmpty() || grants.size() > 256 || origins.size() > 65535 || !expiresAt.isAfter(issuedAt) || expiresAt.getEpochSecond() - issuedAt.getEpochSecond() > 300) throw new IllegalArgumentException("Invalid delegated token claims");
        int version = limit != null ? 2 : origins.isEmpty() ? 0 : 1;
        byte[] rate = new byte[0];
        if (limit != null) {
            byte[] bucket = limit.bucket().getBytes(StandardCharsets.UTF_8);
            for (BigInteger value : List.of(limit.bucketSizeMicrotokens(), limit.refillPerSecondMicrotokens())) if (value.signum() <= 0 || value.bitLength() > 64) throw new IllegalArgumentException("Invalid rate limit");
            rate = ByteBuffer.allocate(4 + bucket.length + 16).order(ByteOrder.LITTLE_ENDIAN).putInt(bucket.length).put(bucket).putLong(limit.bucketSizeMicrotokens().longValue()).putLong(limit.refillPerSecondMicrotokens().longValue()).array();
        }
        UnsafeBuffer claimsBuffer = new UnsafeBuffer(new byte[65_536]);
        MessageHeaderEncoder header = new MessageHeaderEncoder();
        MDTokenClaimsEncoder codec = new MDTokenClaimsEncoder().wrapAndApplyHeader(claimsBuffer, 0, header);
        header.version(version);
        codec.issuedAt(issuedAt.getEpochSecond()).expiresAt(expiresAt.getEpochSecond());
        var selected = codec.grantsCount(grants.size());
        for (Grant grant : grants) {
            byte[] name = grant.packageName().getBytes(StandardCharsets.UTF_8);
            selected.next().quality(switch (grant.quality()) { case "*" -> Quality.ANY; case "RT" -> Quality.RT; case "DL" -> Quality.DL; case "EOD" -> Quality.EOD; default -> throw new IllegalArgumentException("Invalid quality"); }).putPackage_(name, 0, name.length);
        }
        if (version >= 1) {
            var allowed = codec.allowedOriginsCount(origins.size());
            for (String origin : origins) { byte[] bytes = origin.getBytes(StandardCharsets.UTF_8); allowed.next().putOrigin(bytes, 0, bytes.length); }
        }
        byte[] audienceBytes = audience.getBytes(StandardCharsets.UTF_8);
        codec.putAudience(audienceBytes, 0, audienceBytes.length);
        if (version >= 2) codec.putRateLimit(rate, 0, rate.length);
        byte[] claims = Arrays.copyOf(claimsBuffer.byteArray(), 8 + codec.encodedLength());
        Deflater deflater = new Deflater(6);
        byte[] compressed;
        try {
            deflater.setInput(claims); deflater.finish();
            ByteArrayOutputStream output = new ByteArrayOutputStream(); byte[] buffer = new byte[4096];
            while (!deflater.finished()) output.write(buffer, 0, deflater.deflate(buffer));
            compressed = output.toByteArray();
        } finally { deflater.end(); }
        UnsafeBuffer tokenBuffer = new UnsafeBuffer(new byte[6_144]);
        MDTokenEncoder envelope = new MDTokenEncoder().wrapAndApplyHeader(tokenBuffer, 0, header);
        header.version(0);
        byte[] clientBytes = clientId.getBytes(StandardCharsets.UTF_8);
        if (clientBytes.length > 128 || audienceBytes.length > 256) throw new IllegalArgumentException("Token identity exceeds limits");
        envelope.putClientId(clientBytes, 0, clientBytes.length).putClaimsZlib(compressed, 0, compressed.length).putMac(new byte[32], 0, 32);
        byte[] token = Arrays.copyOf(tokenBuffer.byteArray(), 8 + envelope.encodedLength());
        try {
            Mac hmac = Mac.getInstance("HmacSHA256"); hmac.init(new SecretKeySpec(secret, "HmacSHA256"));
            hmac.update("OpenMDTA-MDToken-v1\0".getBytes(StandardCharsets.US_ASCII)); hmac.update(token, 0, token.length - 32);
            System.arraycopy(hmac.doFinal(), 0, token, token.length - 32, 32); return token;
        } catch (java.security.GeneralSecurityException error) { throw new IllegalStateException("HMAC-SHA256 unavailable", error); }
    }

    public static byte[] mainCredential(String clientId, byte[] secret) {
        if (secret.length != 32 || clientId.isBlank() || clientId.contains(":")) throw new IllegalArgumentException("Invalid main credential");
        return ("main:" + clientId + ":" + Base64.getUrlEncoder().withoutPadding().encodeToString(secret)).getBytes(StandardCharsets.UTF_8);
    }
}
