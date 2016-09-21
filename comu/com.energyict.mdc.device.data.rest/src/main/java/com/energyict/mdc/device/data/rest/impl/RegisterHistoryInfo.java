package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;

import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 12.07.16
 * Time: 09:32
 */
public class RegisterHistoryInfo {
    public Long startDate;
    public Long endDate;
    public String deviceName;
    public Long registerId;

    public static RegisterHistoryInfo from(DataLoggerChannelUsage dataLoggerChannelUsage) {
        RegisterHistoryInfo registerHistoryInfo = new RegisterHistoryInfo();
        List<? extends ReadingType> slaveRegisterReadingTypes = dataLoggerChannelUsage.getSlaveChannel().getReadingTypes();
        Optional<Register> slaveRegister = dataLoggerChannelUsage.getDataLoggerReference()
                .getOrigin()
                .getRegisters()
                .stream()
                .filter((register) -> slaveRegisterReadingTypes.contains(register.getReadingType()))
                .findFirst();
        slaveRegister.ifPresent(register -> {
            registerHistoryInfo.registerId = register.getRegisterSpecId();
            registerHistoryInfo.deviceName = dataLoggerChannelUsage.getDataLoggerReference().getOrigin().getName();
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
