package com.openmdta.sdk.p_globex;

import java.nio.ByteOrder;
import java.util.function.Supplier;
import org.agrona.DirectBuffer;
import org.agrona.sbe.MessageDecoderFlyweight;

/** A generated, typed selection key for a Catalog field. */
public final class CatalogField<D extends MessageDecoderFlyweight> {
    private final String label;
    private final String semantic;
    private final Format format;
    private final Supplier<D> decoder;
    CatalogField(String label, Format format, Supplier<D> decoder) {
        this(label, label, format, decoder);
    }
    CatalogField(String label, String semantic, Format format, Supplier<D> decoder) {
        this.label = label; this.semantic = semantic; this.format = format; this.decoder = decoder;
    }
    public String label() { return label; }
    public String semantic() { return semantic; }
    Format format() { return format; }
    Supplier<D> decoder() { return decoder; }
    D wrap(D target, DirectBuffer nativePayload) {
        nativePayload.boundsCheck(0, 4);
        int length = nativePayload.getShort(0, ByteOrder.LITTLE_ENDIAN) & 0xffff;
        int version = nativePayload.getShort(2, ByteOrder.LITTLE_ENDIAN) & 0xffff;
        nativePayload.boundsCheck(4, length);
        target.wrap(nativePayload, 4, length, version);
        return target;
    }
}
