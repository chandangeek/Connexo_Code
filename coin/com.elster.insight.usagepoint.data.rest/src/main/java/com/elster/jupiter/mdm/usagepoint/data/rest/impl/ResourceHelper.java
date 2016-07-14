package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.rest.util.ConcurrentModificationException;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;

import javax.inject.Inject;

public class ResourceHelper {

    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final MetrologyConfigurationService metrologyConfigurationService;

    @Inject
    public ResourceHelper(MeteringService meteringService,
                          ExceptionFactory exceptionFactory,
                          ConcurrentModificationExceptionFactory conflictFactory,
                          MetrologyConfigurationService metrologyConfigurationService) {
        super();
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
        this.conflictFactory = conflictFactory;
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    public MeterRole findMeterRoleOrThrowException(String key) {
        return metrologyConfigurationService.findMeterRole(key)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METER_ROLE_FOR_KEY, key));
    }

    public Meter findMeterOrThrowException(String mrid) {
        return meteringService.findMeter(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_DEVICE_FOR_MRID, mrid));
    }

    public UsagePoint findUsagePointByMrIdOrThrowException(String mrid) {
        return meteringService.findUsagePoint(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_USAGE_POINT_FOR_MRID, mrid));
    }

    public UsagePoint findUsagePointByIdOrThrowException(long id) {
        return meteringService.findUsagePoint(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_USAGE_POINT_FOR_ID, id));
    }

    public UsagePoint findAndLockUsagePointByMrIdOrThrowException(String mrid, long version) {
        UsagePoint up = findUsagePointByMrIdOrThrowException(mrid);
        return lockUsagePointOrThrowException(up.getId(), version, up.getMRID());
    }

    public EffectiveMetrologyConfigurationOnUsagePoint findEffectiveMetrologyConfigurationByUsagePointOrThrowException(UsagePoint usagePoint) {
        return usagePoint.getEffectiveMetrologyConfiguration()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METROLOGYCONFIG_FOR_USAGEPOINT, usagePoint.getMRID()));
    }

    public UsagePointMetrologyConfiguration findAndLockUsagePointMetrologyConfigurationOrThrowException(long id, long version) {
        return metrologyConfigurationService.findAndLockMetrologyConfiguration(id, version)
                .filter(mc -> mc instanceof UsagePointMetrologyConfiguration)
                .map(UsagePointMetrologyConfiguration.class::cast)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METROLOGYCONFIG_FOR_ID, id));
    }

    public UsagePointMetrologyConfiguration findAndLockActiveUsagePointMetrologyConfigurationOrThrowException(long id, long version) {
        return metrologyConfigurationService
                .findAndLockMetrologyConfiguration(id, version)
                .filter(metrologyConfiguration -> metrologyConfiguration instanceof UsagePointMetrologyConfiguration)
                .map(this::isActiveMetrologyConfigurationOrThrowException)
                .map(UsagePointMetrologyConfiguration.class::cast)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METROLOGYCONFIG_FOR_ID, id));
    }

    private MetrologyConfiguration isActiveMetrologyConfigurationOrThrowException(MetrologyConfiguration metrologyConfiguration) {
        if (metrologyConfiguration.isActive()) {
            return metrologyConfiguration;
        }
        throw exceptionFactory.newException(MessageSeeds.NOT_POSSIBLE_TO_LINK_INACTIVE_METROLOGY_CONFIGURATION_TO_USAGE_POINT, metrologyConfiguration.getName());

    }

    public ReadingType findReadingTypeByMrIdOrThrowException(String rt_mrid) {
        return meteringService.getReadingType(rt_mrid).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_READING_TYPE_FOR_MRID, rt_mrid));
    }


    public UsagePoint lockUsagePointOrThrowException(UsagePointInfo info) {
        return lockUsagePointOrThrowException(info.id, info.version, info.mRID);
    }

    public UsagePoint lockUsagePointOrThrowException(long id, long version, String name) {
        return meteringService.findAndLockUsagePointByIdAndVersion(id, version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(name)
                        .withActualVersion(() -> meteringService.findUsagePoint(id).map(UsagePoint::getVersion).orElse(null))
                        .supplier());
    }

    public ConcurrentModificationException throwUsagePointLinkedException(String mrid) {
        return conflictFactory.conflict().withMessageBody(MessageSeeds.USAGE_POINT_LINKED_EXCEPTION_MSG, mrid).withMessageTitle(MessageSeeds.USAGE_POINT_LINKED_EXCEPTION, mrid).build();
    }
}
