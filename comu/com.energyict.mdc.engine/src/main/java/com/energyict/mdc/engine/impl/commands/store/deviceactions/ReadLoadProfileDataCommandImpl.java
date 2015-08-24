package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLoadProfileDataCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;

import java.sql.Date;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Simple command that just reads the requested {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile loadProfiles} from the device
 */
public class ReadLoadProfileDataCommandImpl extends SimpleComCommand implements ReadLoadProfileDataCommand {

    private LoadProfileCommand loadProfileCommand;
    private List<LoadProfileReader> loadProfileReaders = new ArrayList<>(0);
    private List<CollectedLoadProfile> collectedLoadProfileList;

    public ReadLoadProfileDataCommandImpl(final LoadProfileCommand loadProfileCommand, final CommandRoot commandRoot) {
        super(commandRoot);
        this.loadProfileCommand = loadProfileCommand;
    }

    @Override
    public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        this.loadProfileReaders = loadProfileCommand.getLoadProfileReaders();
        this.collectedLoadProfileList = deviceProtocol.getLoadProfileData(this.loadProfileReaders);
        this.loadProfileCommand.addListOfCollectedDataItems(this.collectedLoadProfileList);
    }

    @Override
    public String getDescriptionTitle() {
        return "Read out the load profiles";
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
            this.appendLoadProfileReaders(builder);
        }
        this.appendLoadProfileCollectedDataSummary(builder);
    }

    private void appendLoadProfileReaders (DescriptionBuilder builder) {
        if (this.loadProfileReaders.isEmpty()) {
            builder.addLabel("There are no read profiles do read");
        }
        else {
            PropertyDescriptionBuilder loadProfilesToReadBuilder = builder.addListProperty("loadProfileToRead");
            for (LoadProfileReader loadProfileReader : this.loadProfileReaders) {
                loadProfilesToReadBuilder.append(
                        MessageFormat.format(
                                "{0} ({1,date,yyyy-MM-dd HH:mm:ss} - {2,date,yyy-MM-dd HH:mm:ss}",
                                loadProfileReader.getProfileObisCode(),
                                Date.from(loadProfileReader.getStartReadingTime()),
                                Date.from(loadProfileReader.getEndReadingTime())));
                loadProfilesToReadBuilder = loadProfilesToReadBuilder.next();
            }
        }
    }

    private void appendLoadProfileCollectedDataSummary(DescriptionBuilder builder) {
        if (this.collectedLoadProfileList != null) {
            PropertyDescriptionBuilder descriptionBuilder = builder.addListProperty("collectedProfiles");
            for (CollectedLoadProfile collectedLoadProfile : this.collectedLoadProfileList) {
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

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.READ_LOAD_PROFILE_COMMAND;
    }

    @Override
    protected LogLevel defaultJournalingLogLevel () {
        return LogLevel.INFO;
    }

}