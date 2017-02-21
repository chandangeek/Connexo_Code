/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.data.ReadingTypeObisCodeUsage;

import java.util.Optional;

public class DeviceObisCodeUsageUpdater {

    public void update(DeviceImpl device, ReadingType readingType, ObisCode overruledObisCode) {
        Optional<ReadingTypeObisCodeUsage> readingTypeObisCodeUsageOptional = device.getReadingTypeObisCodeUsage(readingType);
        boolean currentlyNoOverruledObisCodeAlthoughRequested = overruledObisCode != null && // obiscode overruling requested...
                !readingTypeObisCodeUsageOptional.isPresent(), // ...while currently there is none
                currentOverruledObisCodeIsNotTheCorrectOne = overruledObisCode != null && // obiscode overruling requested and...
                        readingTypeObisCodeUsageOptional.isPresent() && // ...the currently present one...
                        !readingTypeObisCodeUsageOptional.get()
                                .getObisCode()
                                .equals(overruledObisCode), // ...is different
                currentOverruledObisCodeIsNotNeeded = overruledObisCode == null && // no obiscode overruling requested...
                        readingTypeObisCodeUsageOptional.isPresent(); // ...however, currently present

        if (currentOverruledObisCodeIsNotTheCorrectOne || currentOverruledObisCodeIsNotNeeded) {
            device.removeReadingTypeObisCodeUsage(readingType);
        }
        if (currentOverruledObisCodeIsNotTheCorrectOne || currentlyNoOverruledObisCodeAlthoughRequested) {
            device.addReadingTypeObisCodeUsage(readingType, overruledObisCode);
        }
    }
}
