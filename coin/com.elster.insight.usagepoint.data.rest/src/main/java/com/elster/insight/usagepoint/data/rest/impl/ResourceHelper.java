package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;

import javax.inject.Inject;

public class ResourceHelper {

    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public ResourceHelper(MeteringService meteringService,
                          ExceptionFactory exceptionFactory,
                          ConcurrentModificationExceptionFactory conflictFactory) {
        super();
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
        this.conflictFactory = conflictFactory;
    }

    public UsagePoint findUsagePointByMrIdOrThrowException(String mrid) {
        return meteringService.findUsagePoint(mrid)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_USAGE_POINT_FOR_MRID, mrid));
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
}
