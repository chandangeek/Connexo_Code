package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import com.elster.jupiter.metering.readings.Reading;

import java.util.List;
import java.util.Map;

/**
 * Provides functionality to store {@link com.energyict.mdc.protocol.api.device.BaseRegister} data into the system
 *
 * @author sva
 * @since 21/01/13 - 9:16
 */

public class CollectedRegisterListDeviceCommand extends DeviceCommandImpl {

    private final CollectedRegisterList collectedRegisterList;
    private final MeterDataStoreCommand meterDataStoreCommand;

    public CollectedRegisterListDeviceCommand(CollectedRegisterList collectedRegisterList, ComTaskExecution comTaskExecution, MeterDataStoreCommand meterDataStoreCommand, ServiceProvider serviceProvider) {
        super(comTaskExecution, serviceProvider);
        this.collectedRegisterList = collectedRegisterList;
        this.meterDataStoreCommand = meterDataStoreCommand;
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        PreStoreRegisters preStoreRegisters = new PreStoreRegisters(this.getMdcReadingTypeUtilService(), comServerDAO);
        Map<DeviceIdentifier, List<Reading>> readings = preStoreRegisters.preStore(collectedRegisterList);
        if (!readings.isEmpty()) {
            readings.forEach(this.meterDataStoreCommand::addReadings);
        }
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel() {
        return ComServer.LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addProperty("deviceIdentifier").append(this.collectedRegisterList.getDeviceIdentifier());
            builder.addProperty("nr of collected registers").append(this.collectedRegisterList.getCollectedRegisters().size());
        }
    }

    @Override
    public String getDescriptionTitle() {
        return "Collected register data";
    }

}
