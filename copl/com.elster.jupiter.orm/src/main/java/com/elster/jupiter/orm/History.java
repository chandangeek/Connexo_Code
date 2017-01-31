/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public class History<T extends HasAuditInfo> {

    private final Map<Range<Instant>, T> historyMap;

    public History(List<? extends JournalEntry<? extends T>> entries, T current) {
        if (entries.isEmpty()) {
            if (current == null) {
                historyMap = ImmutableMap.of();
                return;
            }
            historyMap = ImmutableMap.of(Range.atLeast(current.getCreateTime()), current);
            return;
        }
        ImmutableMap.Builder<Range<Instant>, T> builder = ImmutableMap.builder();
        List<JournalEntry> sorted = new ArrayList<>(entries);
        sorted.sort(Comparator.comparing(JournalEntry::getJournalTime));
        Instant last = null;
        for (JournalEntry<T> entry : sorted) {
            T entity = entry.get();
            Instant start = last == null ? entity.getCreateTime() : last;
            last = entry.getJournalTime();
            Range<Instant> range = Range.closedOpen(start, last);
            builder.put(range, entity);
        }
        if (current != null) {
            builder.put(Range.atLeast(last), current);
        }
        historyMap = builder.build();
    }

    public Optional<T> getVersionAt(Instant at) {
        return historyMap.entrySet()
                .stream()
                .filter(e -> e.getKey().contains(at))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    public Optional<T> getVersion(long version) {
        return historyMap.values().stream()
                .filter(value -> value.getVersion() == version)
                .findFirst();
    }
}
