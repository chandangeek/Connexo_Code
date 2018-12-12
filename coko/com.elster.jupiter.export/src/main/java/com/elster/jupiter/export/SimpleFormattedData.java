/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export;

import com.google.common.collect.ImmutableList;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class SimpleFormattedData implements FormattedData {

    private final Instant lastExported;
    private final List<FormattedExportData> data;

    private SimpleFormattedData(List<FormattedExportData> data, Instant lastExported) {
        this.data = ImmutableList.copyOf(data);
        this.lastExported = lastExported;
    }

    public static SimpleFormattedData of(List<FormattedExportData> data) {
        return new SimpleFormattedData(data, null);
    }

    public static SimpleFormattedData of(List<FormattedExportData> data, Instant instant) {
        return new SimpleFormattedData(data, instant);
    }

    public SimpleFormattedData merged(SimpleFormattedData other) {
        return new SimpleFormattedData(mergedData(other), mergedLastExported(other));
    }

    private Instant mergedLastExported(SimpleFormattedData other) {
        Comparator<Instant> instantComparator = Comparator.nullsFirst(Comparator.<Instant>naturalOrder());
        return Stream.of(lastExported, other.lastExported).max(instantComparator).orElse(null);
    }

    private ImmutableList<FormattedExportData> mergedData(SimpleFormattedData other) {
        List<FormattedExportData> datas = new ArrayList<>(data);
        datas.addAll(other.data);
        datas.sort(Comparator.comparing(this::toSortKey));
        return ImmutableList.copyOf(datas);
    }

    private String toSortKey(FormattedExportData data) {
        return data.getStructureMarker().getStructurePath().get(0);
    }

    @Override
    public Optional<Instant> lastExported() {
        return Optional.ofNullable(lastExported);
    }

    @Override
    public List<FormattedExportData> getData() {
        return Collections.unmodifiableList(data);
    }
}
