/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.impl.MeteringDataModelService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsKey;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.TranslationKey;

import java.util.Optional;
import java.util.stream.Stream;

public enum DefaultMeterRole implements TranslationKey {

    DEFAULT("default", "Default"),
    CONSUMPTION("consumption", "Consumption"),
    PRODUCTION("production", "Production"),
    MAIN("main", "Main"),
    CHECK("check", "Check"),
    PEAK_CONSUMPTION("peak.consumption", "Peak consumption"),
    OFF_PEAK_CONSUMPTION("off.peak.consumption", "Off peak consumption");

    private String key;
    private String defaultFormat;

    DefaultMeterRole(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return "meter.role." + key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public NlsKey getNlsKey() {
        return SimpleNlsKey.key(MeteringDataModelService.COMPONENT_NAME, Layer.DOMAIN, getKey())
                .defaultMessage(this.defaultFormat);
    }

    public static Optional<DefaultMeterRole> from(String role) {
        return Stream.of(values())
                .filter(d -> ("MeterRole.custom." + d.key).equals(role))
                .findFirst();
    }
}
