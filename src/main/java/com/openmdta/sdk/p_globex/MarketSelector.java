package com.openmdta.sdk.p_globex;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/** Immutable instrument expression. Construction validates syntax, not existence or permissions. */
public final class MarketSelector {
    public enum Subject { ENTITY, LIST, RECORD }
    private static final Pattern TYPE = Pattern.compile("[A-Z0-9_-]+"),
        LIST_CODE = Pattern.compile("[A-Z0-9][A-Z0-9._-]{0,63}"), MIC = Pattern.compile("(?:\\*|[A-Z0-9]{4})");
    private final String identifier, expression;
    private final Subject subject;

    private MarketSelector(String identifier, String expression, Subject subject) {
        this.identifier = identifier;
        this.expression = expression;
        this.subject = subject;
    }
    public static MarketSelector isin(String value) { return custom("ISIN", value); }
    public static MarketSelector us(String value) { return custom("US", value); }
    public static MarketSelector cusip(String value) { return custom("CUSIP", value); }
    public static MarketSelector sedol(String value) { return custom("SEDOL", value); }
    public static MarketSelector wkn(String value) { return custom("WKN", value); }
    public static MarketSelector figi(String value) { return custom("FIGI", value); }

    /** Scalar identifiers use locale-independent uppercase, as in the TypeScript builder. */
    public static MarketSelector custom(String type, String value) {
        String name = Objects.requireNonNull(type).trim().toUpperCase(Locale.ROOT);
        String scalar = Objects.requireNonNull(value).trim().toUpperCase(Locale.ROOT);
        if (!TYPE.matcher(name).matches() || name.equals("RAW") || scalar.isEmpty()
                || scalar.indexOf('(') >= 0 || scalar.indexOf(')') >= 0) {
            throw new IllegalArgumentException("Expected a scalar identifier; use raw for exact records");
        }
        if (name.equals("LIST") && !LIST_CODE.matcher(scalar).matches()) throw new IllegalArgumentException("Invalid list code");
        String identifier = name + "(" + scalar + ")";
        return new MarketSelector(identifier, identifier, name.equals("LIST") ? Subject.LIST : Subject.ENTITY);
    }
    public static MarketSelector list(String code) { return custom("LIST", code); }

    /** Preserves dataset/key case and nested record keys, for example PAIR(A,B). */
    public static MarketSelector raw(String dataset, String recordKey) {
        String source = Objects.requireNonNull(dataset).trim(), key = Objects.requireNonNull(recordKey).trim();
        if (source.isEmpty() || key.isEmpty() || source.indexOf(',') >= 0 || source.indexOf('(') >= 0 || source.indexOf(')') >= 0) {
            throw new IllegalArgumentException("Expected an exact dataset and record key");
        }
        int depth = 0;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (c == '(') depth++;
            if (c == ')' && --depth < 0) throw new IllegalArgumentException("Unbalanced record key");
        }
        if (depth != 0) throw new IllegalArgumentException("Unbalanced record key");
        String identifier = "RAW(" + source + "," + key + ")";
        return new MarketSelector(identifier, identifier, Subject.RECORD);
    }

    /** Union of venues. Replaces any previous venue selection. */
    public MarketSelector venue(String... venues) {
        Objects.requireNonNull(venues);
        return venueGroups(Arrays.stream(venues).map(List::of).toList());
    }
    /** First available venue, in preference order (per member for a list). */
    public MarketSelector venueFallback(String... venues) {
        Objects.requireNonNull(venues);
        return venueGroups(List.of(Arrays.asList(venues)));
    }
    /** Union of fallback groups. The supplied lists are not retained. */
    public MarketSelector venueGroups(List<? extends List<String>> groups) {
        if (subject == Subject.RECORD) throw new IllegalArgumentException("Exact records cannot have venue preferences");
        if (groups.isEmpty()) throw new IllegalArgumentException("Select at least one venue group");
        StringBuilder result = new StringBuilder(identifier).append('@');
        for (int i = 0; i < groups.size(); i++) {
            List<String> group = groups.get(i);
            if (group.isEmpty()) throw new IllegalArgumentException("Empty venue fallback group");
            if (i != 0) result.append(',');
            for (int j = 0; j < group.size(); j++) {
                String mic = Objects.requireNonNull(group.get(j)).trim().toUpperCase(Locale.ROOT);
                if (!MIC.matcher(mic).matches()) throw new IllegalArgumentException("Invalid MIC: " + mic);
                if (j != 0) result.append('>');
                result.append(mic);
            }
        }
        return new MarketSelector(identifier, result.toString(), subject);
    }
    public String expression() { return expression; }
    public Subject subject() { return subject; }
    @Override public String toString() { return expression; }
    @Override public boolean equals(Object other) { return other instanceof MarketSelector selector && expression.equals(selector.expression); }
    @Override public int hashCode() { return expression.hashCode(); }
}
