package com.elster.jupiter.metering.config;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Optional;
import java.util.stream.Stream;

public enum DefaultMeterRole implements TranslationKey {

    DEFAULT("meter.role.default", "Default"),
    CONSUMPTION("meter.role.consumption", "Consumption"),
    PRODUCTION("meter.role.production", "Production"),
    MAIN("meter.role.main", "Main"),
    CHECK("meter.role.check", "Check");

    private String key;
    private String defaultFormat;

    DefaultMeterRole(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public static Optional<DefaultMeterRole> from(String role) {
        return Stream.of(values())
                .filter(d -> d.getKey().equals(role))
                .findFirst();
    }
}
