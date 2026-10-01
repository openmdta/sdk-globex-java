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
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

/** Server-side delegation. Never distribute the DataClient private key with the client artifact. */
public final class MdToken {
    private MdToken() {}
    public record RateLimit(String bucket, BigInteger bucketSizeMicrotokens, BigInteger refillPerSecondMicrotokens) {}

    /**
     * Sign with the DataClient's Ed25519 private key: its 32-byte seed as unpadded base64url.
     * Grants are `*`, `NAMESPACE:LICENSE` or `NAMESPACE:LICENSE:RT|DL|EOD`.
     */
    public static byte[] issue(String clientId, String privateKey, String audience, Instant issuedAt, Instant expiresAt, List<String> grants, List<String> origins, RateLimit limit) {
        byte[] seed = Base64.getUrlDecoder().decode(privateKey);
        if (seed.length != 32 || clientId.isBlank() || audience.isBlank() || grants.isEmpty() || grants.size() > 256 || origins.size() > 32 || !expiresAt.isAfter(issuedAt) || expiresAt.getEpochSecond() - issuedAt.getEpochSecond() > 300) throw new IllegalArgumentException("Invalid delegated token claims");
        byte[] rate = new byte[0];
        if (limit != null) {
            byte[] bucket = limit.bucket().getBytes(StandardCharsets.UTF_8);
            for (BigInteger value : List.of(limit.bucketSizeMicrotokens(), limit.refillPerSecondMicrotokens())) if (value.signum() < 0 || value.bitLength() > 64) throw new IllegalArgumentException("Invalid rate limit");
            rate = ByteBuffer.allocate(4 + bucket.length + 16).order(ByteOrder.LITTLE_ENDIAN).putInt(bucket.length).put(bucket).putLong(limit.bucketSizeMicrotokens().longValue()).putLong(limit.refillPerSecondMicrotokens().longValue()).array();
        }
        UnsafeBuffer claimsBuffer = new UnsafeBuffer(new byte[65_536]);
        MessageHeaderEncoder header = new MessageHeaderEncoder();
        MDTokenClaimsEncoder codec = new MDTokenClaimsEncoder().wrapAndApplyHeader(claimsBuffer, 0, header);
        codec.issuedAt(issuedAt.getEpochSecond()).expiresAt(expiresAt.getEpochSecond());
        var selected = codec.grantsCount(grants.size());
        for (String grant : grants) {
            String[] parts = grant.split(":", -1);
            if (!grant.equals("*") && (parts.length < 2 || parts.length > 3 || parts[0].isEmpty() || parts[1].isEmpty() || grant.contains("*"))) throw new IllegalArgumentException("Invalid grant " + grant);
            Quality quality = parts.length < 3 ? Quality.UNTIMED : switch (parts[2]) { case "RT" -> Quality.RT; case "DL" -> Quality.DL; case "EOD" -> Quality.EOD; default -> throw new IllegalArgumentException("Invalid quality in " + grant); };
            byte[] name = (parts.length < 3 ? grant : parts[0] + ":" + parts[1]).getBytes(StandardCharsets.UTF_8);
            selected.next().quality(quality).putPackage_(name, 0, name.length);
        }
        var allowed = codec.allowedOriginsCount(origins.size());
        for (String origin : origins) { byte[] bytes = origin.getBytes(StandardCharsets.UTF_8); allowed.next().putOrigin(bytes, 0, bytes.length); }
        byte[] audienceBytes = audience.getBytes(StandardCharsets.UTF_8);
        codec.putAudience(audienceBytes, 0, audienceBytes.length);
        codec.putRateLimit(rate, 0, rate.length);
        byte[] claims = Arrays.copyOf(claimsBuffer.byteArray(), 8 + codec.encodedLength());
        claims[6] = 3;
        claims[7] = 0;
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
        byte[] clientBytes = clientId.getBytes(StandardCharsets.UTF_8);
        if (clientBytes.length > 128 || audienceBytes.length > 256) throw new IllegalArgumentException("Token identity exceeds limits");
        envelope.putClientId(clientBytes, 0, clientBytes.length).putClaimsZlib(compressed, 0, compressed.length).putSignature(new byte[64], 0, 64);
        byte[] token = Arrays.copyOf(tokenBuffer.byteArray(), 8 + envelope.encodedLength());
        token[6] = 1;
        token[7] = 0;
        try {
            byte[] pkcs8 = new byte[48];
            System.arraycopy(new byte[] {0x30, 0x2e, 0x02, 0x01, 0x00, 0x30, 0x05, 0x06, 0x03, 0x2b, 0x65, 0x70, 0x04, 0x22, 0x04, 0x20}, 0, pkcs8, 0, 16);
            System.arraycopy(seed, 0, pkcs8, 16, 32);
            Signature signer = Signature.getInstance("Ed25519");
            signer.initSign(KeyFactory.getInstance("Ed25519").generatePrivate(new PKCS8EncodedKeySpec(pkcs8)));
            signer.update("OpenMDTA-MDToken-v2\0".getBytes(StandardCharsets.US_ASCII)); signer.update(token, 0, token.length - 64);
            System.arraycopy(signer.sign(), 0, token, token.length - 64, 64); return token;
        } catch (java.security.GeneralSecurityException error) { throw new IllegalStateException("Ed25519 unavailable", error); }
    }
}
