/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.online;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.device.data.ActivatedBreakerStatus;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.upl.meterdata.BreakerStatus;

import com.energyict.obis.ObisCode;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Provides functionality to create/update {@link ActivatedBreakerStatus activatedBreakerStatuses}.
 */
class BreakerStatusStorage {
    private static final ObisCode BREAKER_STATUS = ObisCode.fromString("0.0.96.3.10.255");

    private final DeviceService deviceDataService;
    private final Clock clock;

    BreakerStatusStorage(DeviceService deviceDataService, Clock clock) {
        this.deviceDataService = deviceDataService;
        this.clock = clock;
    }

    private DeviceService getDeviceDataService() {
        return this.deviceDataService;
    }

    void updateBreakerStatus(Optional<BreakerStatus> collectedBreakerStatus, Device device) {
        collectedBreakerStatus.ifPresent(breakerStatus -> createOrUpdateActiveVersion(device, breakerStatus));
    }

    private ActivatedBreakerStatus createOrUpdateActiveVersion(Device device, BreakerStatus collectedBreakerStatus) {
        Optional<ActivatedBreakerStatus> activeBreakerStatus = getDeviceDataService().getActiveBreakerStatus(device);
        Optional<String> mRid = device.getRegisters().stream()
                .filter(register -> register.getRegisterTypeObisCode().equals(BREAKER_STATUS))
                .map(Register::getReadingType)
                .map(ReadingType::getMRID)
                .findFirst();

        ActivatedBreakerStatus activatedBreakerStatus;
        if (!checkIfBreakerStatusesAreEqual(collectedBreakerStatus, activeBreakerStatus)) {
            activatedBreakerStatus = createNewActiveBreakerStatus(device, collectedBreakerStatus);
        } else {
            activatedBreakerStatus = activeBreakerStatus.get();
            if (mRid.isPresent()) {
                MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
                meterReading.addReading(ReadingImpl.of(mRid.get(), BigDecimal.valueOf(collectedBreakerStatus.ordinal()), activatedBreakerStatus.getLastChecked()));
                device.store(meterReading);
            }
        }
        activatedBreakerStatus.setLastChecked(now());
        activatedBreakerStatus.save();
        return activatedBreakerStatus;
    }

    private Instant now() {
        return this.clock.instant();
    }

    private ActivatedBreakerStatus createNewActiveBreakerStatus(Device device, BreakerStatus collectedBreakerStatus) {
        return getDeviceDataService().newActivatedBreakerStatusFrom(device, collectedBreakerStatus, getIntervalFromNow());
    }

    private Boolean checkIfBreakerStatusesAreEqual(BreakerStatus breakerStatus, Optional<ActivatedBreakerStatus> activeBreakerStatus) {
        return activeBreakerStatus
                .map(activatedBreakerStatus -> activatedBreakerStatus.getBreakerStatus().equals(breakerStatus))
                .orElse(false);
    }

    private Interval getIntervalFromNow() {
        return Interval.of(Range.atLeast(now()));
    }
}