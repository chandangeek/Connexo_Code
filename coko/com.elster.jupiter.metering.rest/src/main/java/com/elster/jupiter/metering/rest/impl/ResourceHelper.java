package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Optional;

public class ResourceHelper {
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public ResourceHelper(MetrologyConfigurationService metrologyConfigurationService,
                          ConcurrentModificationExceptionFactory conflictFactory) {
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.conflictFactory = conflictFactory;
    }

    public Optional<UsagePointMetrologyConfiguration> getLockedMetrologyConfiguration(long id, long version) {
        return metrologyConfigurationService
                .findAndLockMetrologyConfiguration(id, version)
                .filter(metrologyConfiguration -> metrologyConfiguration instanceof UsagePointMetrologyConfiguration)
                .map(UsagePointMetrologyConfiguration.class::cast);
    }

    public UsagePointMetrologyConfiguration findAndLockMetrologyConfiguration(MetrologyConfigurationInfo info) {
        return getLockedMetrologyConfiguration(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentMetrologyConfigurationVersion(info.id))
                        .supplier());
    }

    public Long getCurrentMetrologyConfigurationVersion(long id) {
        return metrologyConfigurationService.findMetrologyConfiguration(id)
                .map(MetrologyConfiguration::getVersion)
                .orElse(null);
    }

    public UsagePointMetrologyConfiguration findMetrologyConfiguration(long id) {
        MetrologyConfiguration metrologyConfiguration = metrologyConfigurationService.findMetrologyConfiguration(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        if (metrologyConfiguration instanceof UsagePointMetrologyConfiguration) {
            return (UsagePointMetrologyConfiguration) metrologyConfiguration;
        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }
}
