package com.openmdta.sdk.p_globex;

/** The session's application format. Body buffers do not include an SBE header. */
public record Format(int schemaId, int templateId, int version, int blockLength) {
    public Format {
        if (((schemaId | templateId | version | blockLength) & ~0xffff) != 0) {
            throw new IllegalArgumentException("SBE format outside uint16");
        }
    }
}
