package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.obis.ObisCode;
import com.energyict.mdc.device.data.ReadingTypeObisCodeUsage;

import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 2/06/2016
 * Time: 16:51
 */
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
