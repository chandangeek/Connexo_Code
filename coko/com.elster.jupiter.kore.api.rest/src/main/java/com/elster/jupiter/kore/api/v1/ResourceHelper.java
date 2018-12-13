/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v1;

import com.elster.jupiter.kore.api.impl.MessageSeeds;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.rest.util.ExceptionFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class ResourceHelper {
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final ExceptionFactory exceptionFactory;


    @Inject
    public ResourceHelper(MetrologyConfigurationService metrologyConfigurationService,
                          ExceptionFactory exceptionFactory) {
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.exceptionFactory = exceptionFactory;
    }

    public UsagePointMetrologyConfiguration findUsagePointMetrologyConfigurationById (long id){
        return metrologyConfigurationService
                .findMetrologyConfiguration(id)
                .filter(config -> config instanceof UsagePointMetrologyConfiguration)
                .map(UsagePointMetrologyConfiguration.class::cast)
                .orElseThrow(() -> exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_METROLOGY_CONFIGURATION));

    }
}
