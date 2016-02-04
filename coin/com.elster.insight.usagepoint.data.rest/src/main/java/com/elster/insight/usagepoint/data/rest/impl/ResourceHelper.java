package com.elster.insight.usagepoint.data.rest.impl;

import javax.inject.Inject;


import com.elster.insight.common.rest.ExceptionFactory;
import com.elster.insight.usagepoint.config.UsagePointConfigurationService;
import com.elster.insight.usagepoint.data.UsagePointDataService;
import com.elster.insight.usagepoint.data.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;

public class ResourceHelper {

    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final UsagePointDataService usagePointDataService;

    @Inject
    public ResourceHelper(MeteringService meteringService,
                          ExceptionFactory exceptionFactory,
                          ConcurrentModificationExceptionFactory conflictFactory,
                          UsagePointConfigurationService usagePointConfigurationService,
                          UsagePointDataService usagePointDataService) {
        super();
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
        this.conflictFactory = conflictFactory;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.usagePointDataService = usagePointDataService;
    }

    public Meter findMeterByMrIdOrThrowException(String mRID) {
        return meteringService.findMeter(mRID).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_DEVICE_FOR_MRID, mRID));
    }

    public UsagePoint findUsagePointByMrIdOrThrowException(String mrid) {
        return meteringService.findUsagePoint(mrid)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_USAGE_POINT_FOR_MRID, mrid));
    }

    public ReadingType findReadingTypeByMrIdOrThrowException(String rt_mrid) {
        return meteringService.getReadingType(rt_mrid).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_READING_TYPE_FOR_MRID, rt_mrid));
    }

    public UsagePoint lockUsagePointOrThrowException(UsagePointInfo info) {
        return lockUsagePointOrThrowException(info.id, info.version, info.name);
    }

    public UsagePoint lockUsagePointOrThrowException(long id, long version, String name) {
        return meteringService.findAndLockUsagePointByIdAndVersion(id, version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(name)
                        .withActualVersion(() -> meteringService.findUsagePoint(id).map(UsagePoint::getVersion).orElse(null))
                        .supplier());
    }

    public UsagePointCustomPropertySetExtension findUsagePointExtensionByMrIdOrThrowException(String mrid) {
        return usagePointDataService.findUsagePointExtensionByMrid(mrid)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_USAGE_POINT_FOR_MRID, mrid));
    }
}
