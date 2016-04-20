package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
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

    public UsagePoint findUsagePointByMrIdOrThrowException(String mrid) {
        return meteringService.findUsagePoint(mrid)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_USAGE_POINT_FOR_MRID, mrid));
    }

    public UsagePoint findAndLockUsagePointByMrIdOrThrowException(String mrid, long version) {
        UsagePoint up = findUsagePointByMrIdOrThrowException(mrid);
        return lockUsagePointOrThrowException(up.getId(), version, up.getMRID());
    }

    public UsagePoint findUsagePointByIdOrThrowException(long id) {
        return meteringService.findUsagePoint(id)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_USAGE_POINT_FOR_ID, id));
    }

    public ReadingType findReadingTypeByMrIdOrThrowException(String rt_mrid) {
        return meteringService.getReadingType(rt_mrid).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_READING_TYPE_FOR_MRID, rt_mrid));
    }

    public UsagePointMetrologyConfiguration findAndLockUsagePointMetrologyConfigurationOrThrowException(long id, long version) {
        UsagePointMetrologyConfiguration upmc = metrologyConfigurationService.findUsagePointMetrologyConfiguration(id)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_METROLOGYCONFIG_FOR_ID, id));
        return metrologyConfigurationService.findAndLockUsagePointMetrologyConfiguration(id, version).get();
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
