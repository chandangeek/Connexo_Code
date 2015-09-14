package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import com.elster.jupiter.util.Pair;

import java.util.Optional;

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

    public CollectedLoadProfileDeviceCommand(CollectedLoadProfile collectedLoadProfile, ComTaskExecution comTaskExecution, MeterDataStoreCommand meterDataStoreCommand, ServiceProvider serviceProvider) {
        super(comTaskExecution, serviceProvider);
        this.collectedLoadProfile = collectedLoadProfile;
        this.meterDataStoreCommand = meterDataStoreCommand;
    }

    @Override
    public void doExecute (ComServerDAO comServerDAO) {
        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(this.getClock(), this.getMdcReadingTypeUtilService(), comServerDAO);
        Optional<Pair<DeviceIdentifier<Device>, PreStoreLoadProfile.LocalLoadProfile>> localLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);
        if (localLoadProfile.isPresent()) {
            updateMeterDataStorer(localLoadProfile.get());
        }
        else {
            this.addIssue(
                    CompletionCode.ConfigurationWarning,
                    this.getIssueService().newWarning(
                            this,
                            MessageSeeds.UNKNOWN_DEVICE_LOAD_PROFILE.getKey(),
                            this.collectedLoadProfile.getLoadProfileIdentifier()));
        }
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