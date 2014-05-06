package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.energyict.comserver.core.ComServerDAO;
import com.energyict.mdc.MeterDataFactory;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;

/**
 * Provides functionality to store {@link com.energyict.mdc.protocol.api.device.BaseRegister} data into the system
 *
 * @author sva
 * @since 21/01/13 - 9:16
 */

public class CollectedRegisterListDeviceCommand extends DeviceCommandImpl {

    private final CollectedRegisterList collectedRegisterList;

    public CollectedRegisterListDeviceCommand(CollectedRegisterList collectedRegisterList, IssueService issueService) {
        super(issueService);
        this.collectedRegisterList = collectedRegisterList;
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        MeterReadingImpl meterReading = new MeterReadingImpl();
        for (CollectedRegister collectedRegister : collectedRegisterList.getCollectedRegisters()) {
            meterReading.addReading(MeterDataFactory.createReadingForDeviceRegisterAndObisCode(collectedRegister, collectedRegister.getRegisterIdentifier().getObisCode()));
        }

        comServerDAO.storeMeterReadings(collectedRegisterList.getDeviceIdentifier(), meterReading);
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel () {
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
