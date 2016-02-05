package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.insight.common.rest.ExceptionFactory;
import com.elster.insight.usagepoint.data.UsagePointCustomPropertySetExtension;
import com.elster.insight.usagepoint.data.UsagePointDataService;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;

import javax.inject.Inject;
import java.util.Optional;

public class ResourceHelper {

    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final UsagePointDataService usagePointDataService;
    private final CustomPropertySetService customPropertySetService;

    @Inject
    public ResourceHelper(MeteringService meteringService,
                          ExceptionFactory exceptionFactory,
                          ConcurrentModificationExceptionFactory conflictFactory,
                          UsagePointDataService usagePointDataService,
                          CustomPropertySetService customPropertySetService) {
        super();
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
        this.conflictFactory = conflictFactory;
        this.usagePointDataService = usagePointDataService;
        this.customPropertySetService = customPropertySetService;
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


    public Long getCurrentUsagePointVersion(String mrid){
        return usagePointDataService.findUsagePointExtensionByMrid(mrid)
                .map(UsagePointCustomPropertySetExtension::getUsagePoint)
                .map(UsagePoint::getVersion)
                .orElse(null);
    }

    public Optional<UsagePointCustomPropertySetExtension> getLockedUsagePointCustomPropertySetExtensionById(long id, long version) {
        return usagePointDataService.findAndLockUsagePointExtensionByIdAndVersion(id, version);
    }

    public UsagePointCustomPropertySetExtension lockUsagePointCustomPropertySetExtensionOrThrowException(UsagePointInfo info) {
        return getLockedUsagePointCustomPropertySetExtensionById(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentUsagePointVersion(info.mRID))
                        .supplier());
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySetOrThrowException(String id){
        return customPropertySetService.findActiveCustomPropertySet(id)
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_CUSTOM_PROPERTY_SET, id));
    }
}
