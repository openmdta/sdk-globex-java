package com.openmdta.sdk.p_globex;

import java.util.Set;
import java.util.function.Supplier;
import org.agrona.sbe.MessageDecoderFlyweight;

/** A generated, typed selection key for a deployed Stream block. */
public final class Block<D extends MessageDecoderFlyweight> {
    private final String semantic;
    private final Format format;
    private final Set<String> commands;
    private final Supplier<D> decoder;
    Block(String semantic, Format format, Set<String> commands, Supplier<D> decoder) {
        this.semantic = semantic; this.format = format; this.commands = Set.copyOf(commands); this.decoder = decoder;
    }
    public String semantic() { return semantic; }
    Format format() { return format; }
    Set<String> commands() { return commands; }
    Supplier<D> decoder() { return decoder; }
}
