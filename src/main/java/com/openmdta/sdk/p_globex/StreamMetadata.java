package com.openmdta.sdk.p_globex;

import com.openmdta.sdk.p_globex.sbe.gateway_protocol.StreamMetadataResponseDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.agrona.concurrent.UnsafeBuffer;

/** Owned, typed Stream activity metadata decoded from the public SBE response. */
public record StreamMetadata(String dataset, String quality, Activity activity) {
    public record Window(String open, String close) {}
    public record ExceptionDay(String date, String open, String close, boolean closed) {}
    public record Holiday(String date, String name) {}
    public record Calendar(String name, String displayName, List<Holiday> holidays) {}
    public record Activity(String timeZone, Map<String, Window> times, List<ExceptionDay> exceptions, Calendar calendar) {}

    static StreamMetadata decode(StreamMetadataResponseDecoder decoder, int bodyLength) {
        int presence = decoder.presence();
        if ((presence & ~7) != 0 || (presence & 1) == 0 && (presence & 6) != 0) {
            throw new IllegalArgumentException("Invalid Stream metadata presence");
        }
        var text = new UnsafeBuffer(0, 0);
        var times = new LinkedHashMap<String, Window>();
        var weekdays = List.of("mon", "tue", "wed", "thu", "fri", "sat", "sun");
        var weeklyWindows = decoder.weeklyWindows();
        if (weeklyWindows.count() > 7) throw new IllegalArgumentException("Too many activity weekdays");
        for (var window : weeklyWindows) {
            int weekday = window.weekday();
            if (weekday >= weekdays.size()) throw new IllegalArgumentException("Invalid activity weekday");
            window.wrapOpen(text); String open = utf8(text);
            window.wrapClose(text); String close = utf8(text);
            if (times.putIfAbsent(weekdays.get(weekday), new Window(open, close)) != null) {
                throw new IllegalArgumentException("Duplicate activity weekday");
            }
        }
        var exceptions = new ArrayList<ExceptionDay>();
        var exceptionGroup = decoder.exceptions();
        if (exceptionGroup.count() > 4096) throw new IllegalArgumentException("Too many activity exceptions");
        for (var exception : exceptionGroup) {
            int closed = exception.closed();
            exception.wrapDate(text); String date = utf8(text);
            exception.wrapOpen(text); String open = utf8(text);
            exception.wrapClose(text); String close = utf8(text);
            if (closed > 1 || closed == 1 && (!open.isEmpty() || !close.isEmpty())
                || closed == 0 && (open.isEmpty() || close.isEmpty())) {
                throw new IllegalArgumentException("Invalid activity exception");
            }
            exceptions.add(new ExceptionDay(date, closed == 1 ? null : open, closed == 1 ? null : close, closed == 1));
        }
        var holidays = new ArrayList<Holiday>();
        var holidayGroup = decoder.holidays();
        if (holidayGroup.count() > 4096) throw new IllegalArgumentException("Too many holidays");
        for (var holiday : holidayGroup) {
            int hasName = holiday.hasName();
            holiday.wrapDate(text); String date = utf8(text);
            holiday.wrapName(text); String name = utf8(text);
            if (hasName > 1 || hasName == 0 && !name.isEmpty()) throw new IllegalArgumentException("Invalid holiday name");
            holidays.add(new Holiday(date, hasName == 1 ? name : null));
        }
        decoder.wrapDataset(text); String dataset = utf8(text);
        decoder.wrapQuality(text); String quality = utf8(text);
        decoder.wrapTimeZone(text); String timeZone = utf8(text);
        decoder.wrapCalendarName(text); String calendarName = utf8(text);
        decoder.wrapCalendarDisplayName(text); String calendarDisplayName = utf8(text);
        if (decoder.limit() != bodyLength || (presence & 2) == 0 && (!times.isEmpty() || !exceptions.isEmpty())
            || (presence & 4) == 0 && (!holidays.isEmpty() || !calendarName.isEmpty() || !calendarDisplayName.isEmpty())
            || (presence & 1) == 0 && !timeZone.isEmpty() || (presence & 1) != 0 && timeZone.isEmpty()
            || (presence & 4) != 0 && (calendarName.isEmpty() || calendarDisplayName.isEmpty())) {
            throw new IllegalArgumentException("Invalid Stream metadata response length or absent field");
        }
        Calendar calendar = (presence & 4) == 0 ? null : new Calendar(calendarName, calendarDisplayName, List.copyOf(holidays));
        Activity activity = (presence & 1) == 0 ? null : new Activity(timeZone,
            (presence & 2) == 0 ? null : Map.copyOf(times),
            (presence & 2) == 0 ? null : List.copyOf(exceptions), calendar);
        return new StreamMetadata(dataset, quality, activity);
    }

    private static String utf8(UnsafeBuffer text) {
        return text.getStringWithoutLengthUtf8(0, text.capacity());
    }
}
