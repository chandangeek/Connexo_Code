/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;

import java.util.List;
import java.util.Optional;

public class RegisterHistoryInfo {
    public Long startDate;
    public Long endDate;
    public String deviceName;
    public Long registerId;

    public static RegisterHistoryInfo from(DataLoggerChannelUsage dataLoggerChannelUsage) {
        RegisterHistoryInfo registerHistoryInfo = new RegisterHistoryInfo();
        List<? extends ReadingType> slaveRegisterReadingTypes = dataLoggerChannelUsage.getSlaveChannel().getReadingTypes();
        Optional<Register> slaveRegister = dataLoggerChannelUsage.getPhysicalGatewayReference()
                .getOrigin()
                .getRegisters()
                .stream()
                .filter((register) -> slaveRegisterReadingTypes.contains(register.getReadingType()))
                .findFirst();
        slaveRegister.ifPresent(register -> {
            registerHistoryInfo.registerId = register.getRegisterSpecId();
            registerHistoryInfo.deviceName = dataLoggerChannelUsage.getPhysicalGatewayReference().getOrigin().getName();
            if (dataLoggerChannelUsage.getRange().hasLowerBound()) {
                registerHistoryInfo.startDate = dataLoggerChannelUsage.getRange().lowerEndpoint().toEpochMilli();
            }
            if (dataLoggerChannelUsage.getRange().hasUpperBound()) {
                registerHistoryInfo.endDate = dataLoggerChannelUsage.getRange().upperEndpoint().toEpochMilli();
            }
        });
        return registerHistoryInfo;
    }
}
