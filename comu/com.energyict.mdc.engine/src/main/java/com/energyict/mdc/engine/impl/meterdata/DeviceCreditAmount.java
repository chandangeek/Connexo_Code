/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.CollectedCreditAmountDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.upl.meterdata.CollectedCreditAmount;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Straightforward implementation of a CreditAmount collectedData object.
 * By default the credit amount is empty.
 */
public class DeviceCreditAmount extends CollectedDeviceData implements CollectedCreditAmount {

    private final DeviceIdentifier deviceDeviceIdentifier;
    private String creditType;
    private Optional<BigDecimal> creditAmount = Optional.empty();
    private ComTaskExecution comTaskExecution;

    public DeviceCreditAmount(DeviceIdentifier deviceIdentifier) {
        this.deviceDeviceIdentifier = deviceIdentifier;
    }

    public String getCreditType() {
        return creditType;
    }

    public void setCreditType(String creditType) {
        this.creditType = creditType;
    }

    @Override
    public Optional<BigDecimal> getCreditAmount() {
        return creditAmount;
    }

    @Override
    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = Optional.of(creditAmount);
    }

    @Override
    public void setDataCollectionConfiguration(DataCollectionConfiguration configuration) {
        this.comTaskExecution = (ComTaskExecution) configuration;
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return this.deviceDeviceIdentifier;
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new CollectedCreditAmountDeviceCommand(serviceProvider, this, comTaskExecution);
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return configuration.isConfiguredToReadStatusInformation();
    }
}