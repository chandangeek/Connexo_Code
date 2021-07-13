/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.online;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.device.data.CreditAmount;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.upl.meterdata.CollectedCreditAmount;
import com.energyict.obis.ObisCode;
import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Provides functionality to create/update {@link CreditAmount creditAmountes}.
 */
class CreditAmountStorage {
    private static final ObisCode IMPORT_CREDIT = ObisCode.fromString("0.0.19.10.0.255");
    private static final ObisCode EMERGENCY_CREDIT = ObisCode.fromString("0.0.19.10.1.255");

    private final DeviceService deviceDataService;
    private final Clock clock;

    CreditAmountStorage(DeviceService deviceDataService, Clock clock) {
        this.deviceDataService = deviceDataService;
        this.clock = clock;
    }

    private DeviceService getDeviceDataService() {
        return this.deviceDataService;
    }

    void updateCreditAmount(CollectedCreditAmount collectedCreditAmount, Device device) {
        collectedCreditAmount.getCreditAmount().ifPresent(creditAmount -> createOrUpdate(device, collectedCreditAmount));
    }

    private CreditAmount createOrUpdate(Device device, CollectedCreditAmount collectedCreditAmount) {
        Optional<CreditAmount> creditAmount = getDeviceDataService().getCreditAmount(device);
        ObisCode creditTypeObisCode = collectedCreditAmount.getCreditType().equals("Import Credit") ? IMPORT_CREDIT : EMERGENCY_CREDIT;

        Optional<String> mRid = device.getRegisters().stream()
                .filter(reg -> reg.getRegisterTypeObisCode().equals(creditTypeObisCode))
                .map(Register::getReadingType)
                .map(ReadingType::getMRID)
                .findFirst();


        CreditAmount newCreditAmount;
        if (!checkIfCreditAmountesAreEqual(collectedCreditAmount, creditAmount)) {
            newCreditAmount = createNewCreditAmount(device, collectedCreditAmount);
        } else {
            newCreditAmount = creditAmount.get();
            if (mRid.isPresent()) {
                MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
                meterReading.addReading(ReadingImpl.of(mRid.get(), newCreditAmount.getCreditAmount(), newCreditAmount.getLastChecked()));
                device.store(meterReading);
            }
        }
        newCreditAmount.setLastChecked(now());
        newCreditAmount.save();

        return newCreditAmount;
    }

    private Instant now() {
        return this.clock.instant();
    }

    private CreditAmount createNewCreditAmount(Device device, CollectedCreditAmount collectedCreditAmount) {
        return getDeviceDataService().creditAmountFrom(device, collectedCreditAmount.getCreditType(), collectedCreditAmount.getCreditAmount().get());
    }

    private Boolean checkIfCreditAmountesAreEqual(CollectedCreditAmount newCreditAmount, Optional<CreditAmount> creditAmount) {
        if (creditAmount.isPresent())
            return newCreditAmount.getCreditType().equalsIgnoreCase(creditAmount.get().getCreditType()) &&
                newCreditAmount.getCreditAmount().get().equals(creditAmount.get().getCreditAmount());
        return false;
    }

    private Interval getIntervalFromNow() {
        return Interval.of(Range.atLeast(now()));
    }
}