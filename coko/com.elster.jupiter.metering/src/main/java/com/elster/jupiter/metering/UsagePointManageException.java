package com.elster.jupiter.metering;

import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UsagePointManageException extends LocalizedException {

    private UsagePointManageException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static UsagePointManageException incorrectState(Thesaurus thesaurus, String usagePointMrid) {
        return new UsagePointManageException(thesaurus, MessageSeeds.USAGE_POINT_INCORRECT_STATE, usagePointMrid);
    }

    public static UsagePointManageException.MeterAlreadyActiveOnUsagePoint meterAlreadyActiveOnUsagePoint(Thesaurus thesaurus) {
        return new UsagePointManageException.MeterAlreadyActiveOnUsagePoint(thesaurus, MessageSeeds.THE_SAME_METER_ACTIVATED_TWICE_ON_USAGE_POINT);
    }

    public static class MeterAlreadyActiveOnUsagePoint extends UsagePointManageException {
        private Map<Meter, MeterRole> meterToRoleMapping = new HashMap<>();

        private MeterAlreadyActiveOnUsagePoint(Thesaurus thesaurus, MessageSeed messageSeed) {
            super(thesaurus, messageSeed);
        }

        public MeterAlreadyActiveOnUsagePoint addMeterWithRole(Meter meter, MeterRole meterRole) {
            this.meterToRoleMapping.put(meter, meterRole);
            return this;
        }

        public Map<Meter, MeterRole> getConflictMeterActivations() {
            return Collections.unmodifiableMap(this.meterToRoleMapping);
        }
    }
}
