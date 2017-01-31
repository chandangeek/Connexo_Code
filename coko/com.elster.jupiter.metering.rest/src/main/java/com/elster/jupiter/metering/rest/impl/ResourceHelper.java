/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResourceHelper {
    private final MetrologyConfigurationService metrologyConfigurationService;
    private final MeteringService meteringService;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ResourceHelper(MetrologyConfigurationService metrologyConfigurationService,
                          MeteringService meteringService,
                          ConcurrentModificationExceptionFactory conflictFactory,
                          ExceptionFactory exceptionFactory) {
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.meteringService = meteringService;
        this.conflictFactory = conflictFactory;
        this.exceptionFactory = exceptionFactory;
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

    public ServiceCategory findServiceCategory(IdWithNameInfo serviceCategoryInfo) {
        return Arrays.stream(ServiceKind.values())
                .filter(kind -> kind.name().equals(serviceCategoryInfo.id))
                .findFirst()
                .flatMap(meteringService::getServiceCategory)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.SERVICE_CATEGORY_NOT_FOUND));
    }

    public List<ReadingType> findReadingTypes(List<ReadingTypeInfo> readingTypesInfo) {
        return meteringService.findReadingTypes(readingTypesInfo.stream().map(readingTypeInfo -> readingTypeInfo.mRID).collect(Collectors.toList()));
    }

    public Optional<UsagePoint> getLockedUsagePoint(long id, long version) {
        return meteringService
                .findAndLockUsagePointByIdAndVersion(id, version);
    }

    public UsagePoint findAndLockUsagePoint(UsagePointInfo info) {
        return getLockedUsagePoint(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentUsagePointVersion(info.id))
                        .supplier());
    }

    public UsagePoint findAndLockUsagePointForMetrologyConfigSave(UsagePointInfo info) {
        return getLockedUsagePoint(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withMessageTitle(MessageSeeds.METROLOGY_CONFIG_VERSION_CONCURRENCY_ERROR_ON_USAGE_POINT_TITLE, info.name)
                        .withMessageBody(MessageSeeds.METROLOGY_CONFIG_VERSION_CONCURRENCY_ERROR_ON_USAGE_POINT_BODY, info.name)
                        .withActualVersion(() -> getCurrentUsagePointVersion(info.id))
                        .supplier());
    }

    public EffectiveMetrologyConfigurationOnUsagePoint getMetrologyConfigVersionOrThrowException(UsagePoint usagePoint, Instant start) {
        return usagePoint.getEffectiveMetrologyConfigurationByStart(start)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METROLOGY_CONFIG_VERSION_WITH_START, start));
    }

    private Long getCurrentUsagePointVersion(long id) {
        return meteringService.findUsagePointById(id)
                .map(UsagePoint::getVersion)
                .orElse(null);
    }

    public UsagePoint findUsagePointByNameOrThrowException(String name) {
        return meteringService.findUsagePointByName(name)
                .orElseThrow(exceptionFactory.newExceptionSupplier(com.elster.jupiter.metering.MessageSeeds.NO_USAGE_POINT_WITH_NAME, name));
    }
}
