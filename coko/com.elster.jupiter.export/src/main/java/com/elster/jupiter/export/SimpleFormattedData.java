package com.elster.jupiter.export;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class SimpleFormattedData implements FormattedData {

    private final Instant lastExported;
    private final List<FormattedExportData> data;

    private SimpleFormattedData(List<FormattedExportData> data, Instant lastExported) {
        this.data = data;
        this.lastExported = lastExported;
    }

    public static FormattedData of(List<FormattedExportData> data) {
        return new SimpleFormattedData(data, null);
    }

    public static FormattedData of(List<FormattedExportData> data, Instant instant) {
        return new SimpleFormattedData(data, instant);
    }

    @Override
    public Optional<Instant> lastExported() {
        return Optional.ofNullable(lastExported);
    }

    @Override
    public List<FormattedExportData> getData() {
        return data;
    }
}
