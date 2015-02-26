package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

/**
 * Provides functionality to store {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile} data.
 *
 * Copyrights EnergyICT
 * Date: 29/08/12
 * Time: 14:52
 */
public class CollectedLoadProfileDeviceCommand extends DeviceCommandImpl {

    private final CollectedLoadProfile collectedLoadProfile;
    private final MeterDataStoreCommand meterDataStoreCommand;
    private ComServerDAO comServerDAO;

    public CollectedLoadProfileDeviceCommand(CollectedLoadProfile collectedLoadProfile, MeterDataStoreCommand meterDataStoreCommand, ServiceProvider serviceProvider) {
        super(serviceProvider);
        this.collectedLoadProfile = collectedLoadProfile;
        this.meterDataStoreCommand = meterDataStoreCommand;
    }

    @Override
    public void doExecute (ComServerDAO comServerDAO) {
        this.comServerDAO = comServerDAO;
        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(this.getClock(), this.getMdcReadingTypeUtilService(), comServerDAO);
        Pair<DeviceIdentifier<Device>, PreStoreLoadProfile.LocalLoadProfile> localLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);
        updateMeterDataStorer(localLoadProfile);
    }

    private void updateMeterDataStorer(final Pair<DeviceIdentifier<Device>, PreStoreLoadProfile.LocalLoadProfile> localLoadProfile) {
        if (!localLoadProfile.getLast().getIntervalBlocks().isEmpty()) {
            this.meterDataStoreCommand.addIntervalReadings(localLoadProfile.getFirst(), localLoadProfile.getLast().getIntervalBlocks());
            this.meterDataStoreCommand.addLastReadingUpdater(this.collectedLoadProfile.getLoadProfileIdentifier(), localLoadProfile.getLast().getLastReading());
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

    @Override
    public String getDescriptionTitle() {
        return "Collected load profile data";
    }

}