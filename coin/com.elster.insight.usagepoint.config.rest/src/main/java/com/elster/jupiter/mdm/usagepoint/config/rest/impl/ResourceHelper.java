package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Optional;

public class ResourceHelper {
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final CustomPropertySetService customPropertySetService;

    @Inject
    public ResourceHelper(MetrologyConfigurationService metrologyConfigurationService, ConcurrentModificationExceptionFactory conflictFactory, CustomPropertySetService customPropertySetService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.conflictFactory = conflictFactory;
        this.customPropertySetService = customPropertySetService;
    }

    public MetrologyConfiguration getMetrologyConfigOrThrowException(long metrologyConfigId) {
        return metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    public Optional<MetrologyConfiguration> getLockedMetrologyConfiguration(long id, long version) {
        return metrologyConfigurationService.findAndLockMetrologyConfiguration(id, version);
    }

    public Long getCurrentMetrologyConfigurationVersion(long id) {
        return metrologyConfigurationService.findMetrologyConfiguration(id)
                .map(MetrologyConfiguration::getVersion)
                .orElse(null);
    }

    public MetrologyConfiguration findAndLockMetrologyConfiguration(MetrologyConfigurationInfo info) {
        return getLockedMetrologyConfiguration(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentMetrologyConfigurationVersion(info.id))
                        .supplier());
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySetOrThrowException(String id) {
        return customPropertySetService.findActiveCustomPropertySet(id)
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    // In fact the CPS has no version, so we rely on metrology version
    public MetrologyConfiguration findAndLockCPSOnMetrologyConfiguration(CustomPropertySetInfo<MetrologyConfigurationInfo> info) {
        return getLockedMetrologyConfiguration(info.parent.id, info.parent.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualParent(() -> getCurrentMetrologyConfigurationVersion(info.parent.id), info.parent.id)
                        .withActualVersion(() -> getCurrentMetrologyConfigurationVersion(info.parent.id))
                        .supplier());
    }
}
