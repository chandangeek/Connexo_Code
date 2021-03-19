/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.online;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.CreditAmount;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.upl.meterdata.CollectedCreditAmount;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Provides functionality to create/update {@link CreditAmount creditAmountes}.
 */
class CreditAmountStorage {

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

        CreditAmount newCreditAmount;
        if (!checkIfCreditAmountesAreEqual(collectedCreditAmount, creditAmount)) {
            newCreditAmount = createNewCreditAmount(device, collectedCreditAmount);
        } else {
            newCreditAmount = creditAmount.get();
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