package com.elster.insight.usagepoint.config.rest.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class ResourceHelper {
    private final UsagePointConfigurationService usagePointConfigurationService;

    @Inject
    public ResourceHelper(UsagePointConfigurationService usagePointConfigurationService) {
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    public MetrologyConfiguration getMetrologyConfigOrThrowException(long metrologyConfigId){
        return usagePointConfigurationService.findMetrologyConfiguration(metrologyConfigId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }
}
