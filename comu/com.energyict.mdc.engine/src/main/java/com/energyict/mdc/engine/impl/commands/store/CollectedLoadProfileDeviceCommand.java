package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedLoadProfileEvent;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;

import com.energyict.protocol.ChannelInfo;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

/**
 * Provides functionality to store {@link com.energyict.mdc.upl.meterdata.LoadProfile} data.
 * <p>
 * Copyrights EnergyICT
 * Date: 29/08/12
 * Time: 14:52
 */
public class CollectedLoadProfileDeviceCommand extends DeviceCommandImpl<CollectedLoadProfileEvent> {

    public static final String DESCRIPTION_TITLE = "Collected load profile data";

    private final CollectedLoadProfile collectedLoadProfile;
    private final MeterDataStoreCommand meterDataStoreCommand;

    public CollectedLoadProfileDeviceCommand(CollectedLoadProfile collectedLoadProfile, ComTaskExecution comTaskExecution, MeterDataStoreCommand meterDataStoreCommand, ServiceProvider serviceProvider) {
        super(comTaskExecution, serviceProvider);
        this.collectedLoadProfile = collectedLoadProfile;
        this.meterDataStoreCommand = meterDataStoreCommand;
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        PreStoreLoadProfile loadProfilePreStorer = new PreStoreLoadProfile(this.getClock(), this.getMdcReadingTypeUtilService(), comServerDAO);
        PreStoreLoadProfile.PreStoredLoadProfile preStoredLoadProfile = loadProfilePreStorer.preStore(collectedLoadProfile);
        if (preStoredLoadProfile.getPreStoreResult().equals(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.OK)) {
            preStoredLoadProfile.updateCommand(this.meterDataStoreCommand);
        } else if (preStoredLoadProfile.getPreStoreResult().equals(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.NO_INTERVALS_COLLECTED)) {
            final Optional<OfflineLoadProfile> optionalLoadProfile = comServerDAO.findOfflineLoadProfile(this.collectedLoadProfile.getLoadProfileIdentifier());
            java.util.Date lastReading = optionalLoadProfile.get().getLastReading() == null ? Date.from(getClock().instant()) : optionalLoadProfile.get().getLastReading();
            this.addIssue(
                    CompletionCode.Ok,
                    this.getIssueService().newWarning(
                            this,
                            MessageSeeds.NO_NEW_LOAD_PROFILE_DATA_COLLECTED,
                            optionalLoadProfile.get().getObisCode().toString(),
                            optionalLoadProfile.get().getLastReading()));
        } else if (preStoredLoadProfile.getPreStoreResult().equals(PreStoreLoadProfile.PreStoredLoadProfile.PreStoreResult.LOAD_PROFILE_CONFIGURATION_MISMATCH)) {
            final Optional<OfflineLoadProfile> optionalLoadProfile = comServerDAO.findOfflineLoadProfile(this.collectedLoadProfile.getLoadProfileIdentifier());
            this.addIssue(
                    CompletionCode.ConfigurationError,
                    this.getIssueService().newProblem(
                            this,
                            MessageSeeds.LOAD_PROFILE_CONFIGURATION_MISMATCH,
                            optionalLoadProfile.get().getObisCode().toString(),
                            optionalLoadProfile.get().interval().toString()));
        }
        else {
            this.addIssue(
                    CompletionCode.ConfigurationWarning,
                    this.getIssueService().newWarning(
                            this,
                            MessageSeeds.UNKNOWN_DEVICE_LOAD_PROFILE,
                            comServerDAO.findOfflineLoadProfile(this.collectedLoadProfile.getLoadProfileIdentifier())
                                    .map(offlineLoadProfile -> offlineLoadProfile.getObisCode().toString())
                                    .orElse("")));
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

    protected Optional<CollectedLoadProfileEvent> newEvent(List<Issue> issues) {
        CollectedLoadProfileEvent event = new CollectedLoadProfileEvent(new ComServerEventServiceProvider(), collectedLoadProfile);
        event.addIssues(issues);
        return Optional.of(event);
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }

}