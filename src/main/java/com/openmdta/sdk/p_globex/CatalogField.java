package com.openmdta.sdk.p_globex;

import java.nio.ByteOrder;
import java.util.function.Supplier;
import org.agrona.DirectBuffer;
import org.agrona.sbe.MessageDecoderFlyweight;

/** Catalog owner bodies carry a four-byte blockLength/version prefix. */
public record CatalogField<D extends MessageDecoderFlyweight>(String label, Format format, Supplier<D> decoder) {
    public D wrap(D target, DirectBuffer nativePayload) {
        nativePayload.boundsCheck(0, 4);
        int length = nativePayload.getShort(0, ByteOrder.LITTLE_ENDIAN) & 0xffff;
        int version = nativePayload.getShort(2, ByteOrder.LITTLE_ENDIAN) & 0xffff;
        nativePayload.boundsCheck(4, length);
        target.wrap(nativePayload, 4, length, version);
        return target;
    }
}
