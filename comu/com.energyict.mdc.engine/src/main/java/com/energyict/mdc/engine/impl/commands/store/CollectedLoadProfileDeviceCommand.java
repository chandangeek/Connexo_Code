/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedLoadProfileEvent;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;

import com.energyict.protocol.ChannelInfo;

import java.util.List;
import java.util.Optional;

/**
 * Provides functionality to store {@link com.energyict.mdc.upl.meterdata.LoadProfile} data.
 * <p>
 * <p>
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
        Optional<OfflineLoadProfile> offlineLoadProfile = comServerDAO.findOfflineLoadProfile(collectedLoadProfile.getLoadProfileIdentifier());
        if (offlineLoadProfile.isPresent()) {
            comServerDAO.storeLoadProfile(offlineLoadProfile, collectedLoadProfile, this.getClock().instant());
        } else {
            this.addIssue(
                    CompletionCode.ConfigurationWarning,
                    this.getIssueService().newWarning(
                            this,
                            MessageSeeds.UNKNOWN_DEVICE_LOAD_PROFILE));
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