package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.config.UPMetrologyConfiguration;

import javax.inject.Inject;

public class UPMetrologyConfigurationImpl extends MetrologyConfigurationImpl implements UPMetrologyConfiguration {
    public static final String TYPE_IDENTIFIER = "U";

    @Inject
    UPMetrologyConfigurationImpl(ServerMetrologyConfigurationService metrologyConfigurationService, EventService eventService) {
        super(metrologyConfigurationService, eventService);
    }
}
