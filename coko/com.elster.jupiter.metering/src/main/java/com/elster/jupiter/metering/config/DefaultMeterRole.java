package com.elster.jupiter.metering.config;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;

import java.util.Optional;
import java.util.stream.Stream;

public enum DefaultMeterRole {

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

    public String getDefaultFormat() {
        return defaultFormat;
    }

    public NlsKey getNlsKey() {
        return SimpleNlsKey.key(MetrologyConfigurationService.COMPONENT_NAME, Layer.DOMAIN, getKey())
                .defaultMessage(this.defaultFormat);
    }

    public static Optional<DefaultMeterRole> from(String role) {
        return Stream.of(values())
                .filter(d -> ("MeterRole.custom." + d.key).equals(role))
                .findFirst();
    }
}
