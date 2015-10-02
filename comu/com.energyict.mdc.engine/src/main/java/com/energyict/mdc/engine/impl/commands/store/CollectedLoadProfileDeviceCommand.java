package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.metering.readings.IntervalBlock;
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

import java.time.Instant;
import java.util.List;

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
        PreStoreLoadProfile.PreStoredLoadProfile preStoredLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);
        if (preStoredLoadProfile.getPreStoreResult().equals(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.OK)) {
            updateMeterDataStorer(preStoredLoadProfile.getDeviceIdentifier(), preStoredLoadProfile.getIntervalBlocks(), preStoredLoadProfile.getLastReading());
        } else if(preStoredLoadProfile.getPreStoreResult().equals(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.NO_INTERVALS_COLLECTED)){
            this.addIssue(
                    CompletionCode.Ok,
                    this.getIssueService().newWarning(
                            this,
                            MessageSeeds.NO_LOAD_PROFILE_DATA_COLLECTED.getKey(),
                            comServerDAO.findOfflineLoadProfile(this.collectedLoadProfile.getLoadProfileIdentifier()).map(offlineLoadProfile -> offlineLoadProfile.getObisCode().toString()).orElse("")));
        }
        else {
            this.addIssue(
                    CompletionCode.ConfigurationWarning,
                    this.getIssueService().newWarning(
                            this,
                            MessageSeeds.UNKNOWN_DEVICE_LOAD_PROFILE.getKey(),
                            comServerDAO.findOfflineLoadProfile(this.collectedLoadProfile.getLoadProfileIdentifier()).map(offlineLoadProfile -> offlineLoadProfile.getObisCode().toString()).orElse("")));
        }
    }

    private void updateMeterDataStorer(DeviceIdentifier<Device> deviceIdentifier, List<IntervalBlock> intervalBlocks, Instant lastReading) {
        if (!intervalBlocks.isEmpty()) {
            this.meterDataStoreCommand.addIntervalReadings(deviceIdentifier, intervalBlocks);
            this.meterDataStoreCommand.addLastReadingUpdater(this.collectedLoadProfile.getLoadProfileIdentifier(), lastReading);
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