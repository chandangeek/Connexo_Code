package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
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

    UsagePointMetrologyConfiguration getMetrologyConfigOrThrowException(long metrologyConfigId) {
        MetrologyConfiguration mc = metrologyConfigurationService.findMetrologyConfiguration(metrologyConfigId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        if (mc instanceof UsagePointMetrologyConfiguration) {
            return (UsagePointMetrologyConfiguration) mc;
        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    private Optional<UsagePointMetrologyConfiguration> getLockedMetrologyConfiguration(long id, long version) {
        return metrologyConfigurationService
                .findAndLockMetrologyConfiguration(id, version)
                .filter(metrologyConfiguration ->  metrologyConfiguration instanceof UsagePointMetrologyConfiguration)
                .map(UsagePointMetrologyConfiguration.class::cast);
    }

    private Long getCurrentMetrologyConfigurationVersion(long id) {
        return metrologyConfigurationService.findMetrologyConfiguration(id)
                .map(MetrologyConfiguration::getVersion)
                .orElse(null);
    }

    UsagePointMetrologyConfiguration findAndLockMetrologyConfiguration(MetrologyConfigurationInfo info) {
        return getLockedMetrologyConfiguration(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentMetrologyConfigurationVersion(info.id))
                        .supplier());
    }

    RegisteredCustomPropertySet getRegisteredCustomPropertySetOrThrowException(String id) {
        return customPropertySetService.findActiveCustomPropertySet(id)
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    // In fact the CPS values are considered part of the MetrologyConfiguration, so we need to rely on metrology version
    UsagePointMetrologyConfiguration findAndLockCPSOnMetrologyConfiguration(CustomPropertySetInfo<MetrologyConfigurationInfo> info) {
        return getLockedMetrologyConfiguration(info.parent.id, info.parent.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualParent(() -> getCurrentMetrologyConfigurationVersion(info.parent.id), info.parent.id)
                        .withActualVersion(() -> getCurrentMetrologyConfigurationVersion(info.parent.id))
                        .supplier());
    }

    MetrologyContract findAndLockContractOnMetrologyConfiguration(MetrologyContractInfo metrologyContractInfo) {
        return metrologyConfigurationService.findAndLockMetrologyContract(metrologyContractInfo.id, metrologyContractInfo.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(metrologyContractInfo.name)
                .withActualVersion(() -> getCurrentMetrologyContractVersion(metrologyContractInfo.id))
                .supplier());
    }

    MetrologyContract findContractOnMetrologyConfiguration(long contractId) {
        return metrologyConfigurationService.findMetrologyContract(contractId)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private Long getCurrentMetrologyContractVersion(long id) {
        return metrologyConfigurationService.findMetrologyContract(id)
                .map(MetrologyContract::getVersion)
                .orElse(null);
    }

}