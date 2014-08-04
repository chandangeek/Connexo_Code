package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

/**
 * Provides functionality to store {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile} data into the system
 *
 * Copyrights EnergyICT
 * Date: 29/08/12
 * Time: 14:52
 */
public class CollectedLoadProfileDeviceCommand extends DeviceCommandImpl {

    private final CollectedLoadProfile collectedLoadProfile;
    private final MeterDataStoreCommand meterDataStoreCommand;
    private ComServerDAO comServerDAO;

    public CollectedLoadProfileDeviceCommand(CollectedLoadProfile collectedLoadProfile, MeterDataStoreCommand meterDataStoreCommand) {
        super();
        this.collectedLoadProfile = collectedLoadProfile;
        this.meterDataStoreCommand = meterDataStoreCommand;
    }

    @Override
    public void doExecute (ComServerDAO comServerDAO) {
        this.comServerDAO = comServerDAO;
        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(getClock(), getMdcReadingTypeUtilService(), comServerDAO);
        PreStoreLoadProfile.LocalLoadProfile localLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);
        updateMeterDataStorer(localLoadProfile);
    }

    private void updateMeterDataStorer(final PreStoreLoadProfile.LocalLoadProfile localLoadProfile) {
        if(localLoadProfile.getIntervalBlocks().size() > 0){
            DeviceIdentifier<Device> deviceIdentifier = this.comServerDAO.getDeviceIdentifierFor(this.collectedLoadProfile.getLoadProfileIdentifier());
            this.meterDataStoreCommand.addIntervalReadings(deviceIdentifier, localLoadProfile.getIntervalBlocks());
            this.meterDataStoreCommand.addLastReadingUpdater(this.collectedLoadProfile.getLoadProfileIdentifier(), localLoadProfile.getLastReading());
        }
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel() {
        return ComServer.LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addProperty("load profile").append(this.collectedLoadProfile.getLoadProfileIdentifier());
            builder.addProperty("interval data period").append(this.collectedLoadProfile.getCollectedIntervalDataRange());
            PropertyDescriptionBuilder listBuilder = builder.addListProperty("channels");
            for (ChannelInfo channel : this.collectedLoadProfile.getChannelInfo()) {
                listBuilder.append(channel.getChannelId()).next();
            }
        }
    }

}