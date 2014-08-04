package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.Reading;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

import java.util.List;

/**
 * Provides functionality to store {@link com.energyict.mdc.protocol.api.device.BaseRegister} data into the system
 *
 * @author sva
 * @since 21/01/13 - 9:16
 */

public class CollectedRegisterListDeviceCommand extends DeviceCommandImpl {

    private final CollectedRegisterList collectedRegisterList;
    private final MeterDataStoreCommand meterDataStoreCommand;

    public CollectedRegisterListDeviceCommand(CollectedRegisterList collectedRegisterList, MeterDataStoreCommand meterDataStoreCommand) {
        super();
        this.collectedRegisterList = collectedRegisterList;
        this.meterDataStoreCommand = meterDataStoreCommand;
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        PreStoreRegisters preStoreRegisters = new PreStoreRegisters(getMdcReadingTypeUtilService(), comServerDAO);
        List<Reading> readings = preStoreRegisters.preStore(collectedRegisterList);
        if(readings.size() > 0){
            this.meterDataStoreCommand.addReadings(getDeviceIdentifier(), readings);
        }
    }

    private DeviceIdentifier<Device> getDeviceIdentifier() {
        return (DeviceIdentifier<Device>) collectedRegisterList.getDeviceIdentifier();
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
}
