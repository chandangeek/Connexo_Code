/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.LoadProfileCommandImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedDeviceCache;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLoadProfileChannel;

import com.energyict.protocol.ChannelInfo;

import java.text.MessageFormat;
import java.util.*;

public class InboundCollectedLoadProfileCommandImpl extends LoadProfileCommandImpl {

    private static final String FIRST_LOAD_PROFILE_ON_DEVICE_TYPE_NAME = "FirstLoadProfileOnDevice";
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
     * Also, the proper reading type MRIDs are added to the channel info.
     * The proper issues are created if the obiscodes or the number of channels mismatch.
     */
    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        for (ServerCollectedData dataItem : collectedData) {
            if (dataItem instanceof CollectedLoadProfile) {
                CollectedLoadProfile collectedLoadProfile = (CollectedLoadProfile) dataItem;

                List<OfflineLoadProfile> allOfflineLoadProfiles = getGroupedDeviceCommand().getOfflineDevice().getAllOfflineLoadProfiles();
                Optional<OfflineLoadProfile> optionalOfflineLoadProfile;
                if (((CollectedLoadProfile) dataItem).getLoadProfileIdentifier().forIntrospection().getTypeName().equals(FIRST_LOAD_PROFILE_ON_DEVICE_TYPE_NAME)) {
                    optionalOfflineLoadProfile = allOfflineLoadProfiles.stream().findFirst();
                } else {
                    optionalOfflineLoadProfile = allOfflineLoadProfiles
                            .stream()
                            .filter(lp -> lp.getObisCode().equals(collectedLoadProfile.getLoadProfileIdentifier().getProfileObisCode()))
                            .findAny();
                }

                if (optionalOfflineLoadProfile.isPresent()) {
                    OfflineLoadProfile offlineLoadProfile = optionalOfflineLoadProfile.get();
                    int configuredNumberOfChannels = offlineLoadProfile.getAllOfflineChannels().size();
                    int receivedNumberOfChannels = collectedLoadProfile.getChannelInfo().size();
                    if (configuredNumberOfChannels != receivedNumberOfChannels && !offlineLoadProfile.isDataLoggerSlaveLoadProfile()) {
                        //We received a wrong number of channels
                        addIssue(
                                getIssueService().newProblem(
                                        offlineLoadProfile.getObisCode(),
                                        MessageSeeds.LOAD_PROFILE_NUMBER_OF_CHANNELS_MISMATCH,
                                        offlineLoadProfile.getObisCode(),
                                        receivedNumberOfChannels,
                                        configuredNumberOfChannels),
                                CompletionCode.ConfigurationError
                        );
                    } else {
                        //Add the proper reading types to the channel infos, based on the given obiscode and unit.
                        for (ChannelInfo channelInfo : collectedLoadProfile.getChannelInfo()) {
                            Optional<OfflineLoadProfileChannel> offlineLoadProfileChannel = offlineLoadProfile
                                    .getAllOfflineChannels()
                                    .stream()
                                    .filter(channel -> channel.getObisCode().equals(channelInfo.getChannelObisCode()))
                                    .findAny();

                            if (offlineLoadProfileChannel.isPresent()) {
                                channelInfo.setReadingTypeMRID(offlineLoadProfileChannel.get().getReadingTypeMRID());
                            } else {
                                //The received LP contains an unknown channel obiscode
                                addIssue(
                                        getIssueService().newProblem(
                                                collectedLoadProfile.getLoadProfileIdentifier().getProfileObisCode(),
                                                MessageSeeds.LOAD_PROFILE_CHANNEL_MISSING,
                                                channelInfo.getChannelObisCode(),
                                                collectedLoadProfile.getLoadProfileIdentifier().getProfileObisCode()
                                        ),
                                        CompletionCode.ConfigurationError
                                );
                            }
                        }
                    }
                } else {
                    //We received a load profile with an obiscode that is not configured in Connexo
                    addIssue(
                            getIssueService().newProblem(
                                    collectedLoadProfile.getLoadProfileIdentifier().getProfileObisCode(),
                                    MessageSeeds.UNKNOWN_DEVICE_LOAD_PROFILE,
                                    collectedLoadProfile.getLoadProfileIdentifier().getProfileObisCode()
                            ),
                            CompletionCode.ConfigurationError
                    );
                }

                this.addCollectedDataItem(dataItem);
            } else if(dataItem instanceof CollectedDeviceCache){
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
                                    Date.from(collectedLoadProfile.getCollectedIntervalDataRange().lowerEndpoint()),
                                    Date.from(collectedLoadProfile.getCollectedIntervalDataRange().upperEndpoint())));
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