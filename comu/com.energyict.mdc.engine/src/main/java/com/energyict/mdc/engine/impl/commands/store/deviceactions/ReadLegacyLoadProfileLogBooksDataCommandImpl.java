package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.LegacyLoadProfileLogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLegacyLoadProfileLogBooksDataCommand;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

/**
 * Simple command that just reads the requested {@link com.energyict.mdc.upl.meterdata.LoadProfile}s from the device.
 */
public class ReadLegacyLoadProfileLogBooksDataCommandImpl extends SimpleComCommand implements ReadLegacyLoadProfileLogBooksDataCommand {

    private LegacyLoadProfileLogBooksCommand legacyLoadProfileLogBooksCommand;
    private List<CollectedData> loadProfileLogBooksData = new ArrayList<>();
    private Instant lastLogBookDate = null;

    public ReadLegacyLoadProfileLogBooksDataCommandImpl(final GroupedDeviceCommand groupedDeviceCommand, final LegacyLoadProfileLogBooksCommand legacyLoadProfileLogBooksCommand) {
        super(groupedDeviceCommand);
        this.legacyLoadProfileLogBooksCommand = legacyLoadProfileLogBooksCommand;
    }

    @Override
    public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        lastLogBookDate = getLastLogbookDate((MeterProtocolAdapter) deviceProtocol);
        loadProfileLogBooksData = ((MeterProtocolAdapter) deviceProtocol).getLoadProfileLogBooksData(legacyLoadProfileLogBooksCommand.getLoadProfileReaders(),
                legacyLoadProfileLogBooksCommand.getLogBookReaders());

        removeUnwantedChannels(legacyLoadProfileLogBooksCommand.getLoadProfileReaders(), loadProfileLogBooksData);
        addReadingTypesToChannelInfos(loadProfileLogBooksData, legacyLoadProfileLogBooksCommand.getLoadProfileReaders());

        this.legacyLoadProfileLogBooksCommand.addListOfCollectedDataItems(loadProfileLogBooksData);
    }

    private Instant getLastLogbookDate(MeterProtocolAdapter deviceProtocol) {
        try {
            Date lastLogBook = deviceProtocol.getValidLogBook(this.legacyLoadProfileLogBooksCommand.getLogBookReaders()).getLastLogBook();
            return lastLogBook == null ? null : lastLogBook.toInstant();
        } catch (Exception e) {
            return Instant.now();
        }
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.READ_LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND;
    }

    @Override
    public String getDescriptionTitle() {
        return "Read out load profile and logbook of legacy protocol";
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
            this.appendLoadProfileReaders(builder);
        }

        this.appendLoadProfileCollectedDataSummary(builder);
        this.appendLogBookColleectedDataSummary(builder);
    }

    private void appendLoadProfileReaders(DescriptionBuilder builder) {
        if (this.legacyLoadProfileLogBooksCommand.getLoadProfileReaders().isEmpty()) {
            builder.addLabel("There are no load profiles to read");
        } else {
            PropertyDescriptionBuilder loadProfilesToReadBuilder = builder.addListProperty("loadProfileToRead");
            for (LoadProfileReader loadProfileReader : this.legacyLoadProfileLogBooksCommand.getLoadProfileReaders()) {
                loadProfilesToReadBuilder.append(
                        MessageFormat.format(
                                "{0} [{1,date,yyyy-MM-dd HH:mm:ss} - {2,date,yyy-MM-dd HH:mm:ss}]",
                                loadProfileReader.getProfileObisCode(),
                                loadProfileReader.getStartReadingTime(),
                                loadProfileReader.getEndReadingTime()));
                loadProfilesToReadBuilder.next();
            }
        }
    }

    private void appendLoadProfileCollectedDataSummary(DescriptionBuilder builder) {
        PropertyDescriptionBuilder descriptionBuilder = builder.addListProperty("collectedProfiles");
        for (CollectedData collectedData : loadProfileLogBooksData) {
            if (collectedData instanceof CollectedLoadProfile) {
                CollectedLoadProfile collectedLoadProfile = (CollectedLoadProfile) collectedData;
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
                                    collectedLoadProfile.getCollectedIntervalDataRange().lowerEndpoint().isBefore(lastLogBookDate)
                                            ? collectedLoadProfile.getCollectedIntervalDataRange().lowerEndpoint() :
                                            lastLogBookDate,
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

    private void appendLogBookColleectedDataSummary(DescriptionBuilder builder) {
        PropertyDescriptionBuilder logbookObisCodesBuilder = builder.addListProperty("collectedLogBooks");
        for (LogBookReader logBookReader : this.legacyLoadProfileLogBooksCommand.getLogBookReaders()) {
            logbookObisCodesBuilder.append("(");
            logbookObisCodesBuilder.append(logBookReader.getLogBookObisCode());
            CollectedLogBook collectedLogBook = getCollectedLogBookForLogbookReader(logBookReader);
            if (collectedLogBook != null) {
                logbookObisCodesBuilder.append(" - ");
                logbookObisCodesBuilder.append(collectedLogBook.getResultType());
                logbookObisCodesBuilder.append(" - ");
                logbookObisCodesBuilder.append("nrOfEvents: ").append(collectedLogBook.getCollectedMeterEvents().size());
            }
            logbookObisCodesBuilder.append(")");
        }
    }

    private CollectedLogBook getCollectedLogBookForLogbookReader(LogBookReader reader) {
        for (CollectedData collectedData : loadProfileLogBooksData) {
            if (collectedData instanceof CollectedLogBook) {
                CollectedLogBook collectedLogBook = (CollectedLogBook) collectedData;
                if (collectedLogBook.getLogBookIdentifier().getLogBookObisCode().equals(reader.getLogBookIdentifier().getLogBookObisCode())) {
                    return collectedLogBook;
                }
            }
        }
        return null;
    }
}
