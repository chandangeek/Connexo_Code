/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.LoadProfileCommandImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.tasks.LoadProfilesTask;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class InboundCollectedLoadProfileCommandImpl extends LoadProfileCommandImpl {

    private final List<ServerCollectedData> collectedData;

    public InboundCollectedLoadProfileCommandImpl(GroupedDeviceCommand groupedDeviceCommand, LoadProfilesTask loadProfilesTask, ComTaskExecution comTaskExecution, List<ServerCollectedData> collectedData) {
        super(groupedDeviceCommand, loadProfilesTask, comTaskExecution);
        this.collectedData = collectedData;
    }

    /**
     * This overrides the normal execution that would otherwise executes every
     * load profile step (verify, read LP, check bad time, create events)
     * <p>
     * Instead, only the collected data is stored, and events are created when relevant.
     */
    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        for (ServerCollectedData dataItem : collectedData) {
            if (dataItem instanceof CollectedLoadProfile) {
                this.addCollectedDataItem(dataItem);
            }
        }

        if (this.getLoadProfilesTaskOptions().isCreateMeterEventsFromStatusFlags()) {
            getCreateMeterEventsFromStatusFlagsCommand().execute(deviceProtocol, executionContext);
        }
    }

    @Override
    protected LogLevel defaultJournalingLogLevel() {
        return LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        this.appendLoadProfileCollectedDataSummary(builder);
    }

    private void appendLoadProfileCollectedDataSummary(DescriptionBuilder builder) {
        if (getListOfCollectedLoadProfiles().isEmpty()) {
            builder.addLabel("No load profile data collected");
        } else {
            PropertyDescriptionBuilder descriptionBuilder = builder.addListProperty("collectedProfiles");
            for (CollectedLoadProfile collectedLoadProfile : getListOfCollectedLoadProfiles()) {
                descriptionBuilder.append("(");
                descriptionBuilder.append(collectedLoadProfile.getLoadProfileIdentifier());
                descriptionBuilder.append(" - ");
                descriptionBuilder.append(collectedLoadProfile.getResultType());
                if (!collectedLoadProfile.getChannelInfo().isEmpty()) {
                    descriptionBuilder.append(" - ");
                    appendLoadProfileCollectedDataChannelInfo(descriptionBuilder, collectedLoadProfile);
                }
                if (!collectedLoadProfile.getCollectedIntervalData().isEmpty()) {
                    descriptionBuilder.append(" - ");
                    descriptionBuilder.append(
                            MessageFormat.format(
                                    "dataPeriod: [{0,date,yyyy-MM-dd HH:mm:ss} - {1,date,yyy-MM-dd HH:mm:ss}]",
                                    collectedLoadProfile.getCollectedIntervalDataRange().lowerEndpoint(),
                                    collectedLoadProfile.getCollectedIntervalDataRange().upperEndpoint()));
                }
                descriptionBuilder.append(")");
                descriptionBuilder.next();
            }
        }
    }

    private void appendLoadProfileCollectedDataChannelInfo(PropertyDescriptionBuilder descriptionBuilder, CollectedLoadProfile collectedLoadProfile) {
        descriptionBuilder.append("channels: ");
        ListIterator<ChannelInfo> channelInfoIterator = collectedLoadProfile.getChannelInfo().listIterator();

        while (channelInfoIterator.hasNext()) {
            ChannelInfo channelInfo = channelInfoIterator.next();
            descriptionBuilder.append(channelInfo.getName());
            if (channelInfoIterator.hasNext()) {
                descriptionBuilder.append(", ");
            }
        }
    }

    private List<CollectedLoadProfile> getListOfCollectedLoadProfiles() {
        List<CollectedLoadProfile> collectedLoadProfiles = new ArrayList<>();
        for (CollectedData data : getCollectedData()) {
            if (data instanceof CollectedLoadProfile) {
                collectedLoadProfiles.add((CollectedLoadProfile) data);
            }
        }
        return collectedLoadProfiles;
    }
}