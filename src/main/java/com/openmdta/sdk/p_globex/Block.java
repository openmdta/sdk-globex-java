package com.openmdta.sdk.p_globex;

import java.util.Set;
import java.util.function.Supplier;
import org.agrona.sbe.MessageDecoderFlyweight;

/** A deployed Stream block. Allocate a decoder once per consumer, then reuse it. */
public record Block<D extends MessageDecoderFlyweight>(String semantic, Format format, Set<String> commands, Supplier<D> decoder) {
    public Block { commands = Set.copyOf(commands); }
}
