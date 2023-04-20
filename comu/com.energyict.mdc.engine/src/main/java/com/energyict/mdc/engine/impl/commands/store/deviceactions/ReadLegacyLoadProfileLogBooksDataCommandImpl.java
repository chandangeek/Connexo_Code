/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.LegacyLoadProfileLogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLegacyLoadProfileLogBooksDataCommand;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.CollectedLoadProfileHelper;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.Problem;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.exceptions.DataParseException;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

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

        CollectedLoadProfileHelper.removeUnwantedChannels(legacyLoadProfileLogBooksCommand.getLoadProfileReaders(), loadProfileLogBooksData);
        verifyReceivedChannelInfo();
        CollectedLoadProfileHelper.addReadingTypesToChannelInfos(loadProfileLogBooksData, legacyLoadProfileLogBooksCommand.getLoadProfileReaders());

        this.legacyLoadProfileLogBooksCommand.addListOfCollectedDataItems(loadProfileLogBooksData);
    }

    /**
     * Verify the received channel info<br/>
     * The number of channels has already been verified previously (as part of VerifyLoadProfilesCommand), so there is no need to check this again.
     * Here we should only check that the units of the received channels matches the configured ones, as for legacy protocols this could not be done
     * as part of VerifyLoadProfilesCommand (as the device channel infos were not present at that time and instead all units were set to 'undefined')
     */
    private void verifyReceivedChannelInfo() {
        Iterator<CollectedData> iterator = loadProfileLogBooksData.iterator();
        while (iterator.hasNext()) {
            CollectedData collectedData = iterator.next();
            if (collectedData instanceof CollectedLoadProfile) {
                CollectedLoadProfile collectedLoadProfile = (CollectedLoadProfile) collectedData;
                legacyLoadProfileLogBooksCommand.getLoadProfileReaders()
                        .stream()
                        .filter(lpr -> lpr.getProfileObisCode().equals(collectedLoadProfile.getLoadProfileIdentifier().getLoadProfileObisCode()))
                        .findAny()
                        .ifPresent(lpr -> {
                            List<Issue> issues = new ArrayList<>();
                            lpr.getChannelInfos().forEach(localChannelInfo -> issues.addAll(verifyLocalChannelConfiguration(collectedLoadProfile, localChannelInfo)));
                            if (!issues.isEmpty()) {
                                issues.forEach(issue -> addIssue(issue, CompletionCode.ConfigurationError));
                                iterator.remove();
                            }
                        });
            }
        }
    }

    private List<Issue> verifyLocalChannelConfiguration(CollectedLoadProfile collectedLoadProfile, ChannelInfo localChannelInfo) {
        List<Issue> issues = new ArrayList<>();
        ObisCode loadProfileConfigurationObisCode = collectedLoadProfile.getLoadProfileIdentifier().getLoadProfileObisCode();
        Optional<Problem> incorrectChannelUnitProblem = Optional.empty();
        for (ChannelInfo meterChannelInfo : collectedLoadProfile.getChannelInfo()) {
            if (match(localChannelInfo, meterChannelInfo)) {
                if (unitMismatch(localChannelInfo, meterChannelInfo)) {
                       /* Do not add problem right away, because we can may have multiple channels with the same obis code but with different units.
                        * Instead we should continue the loop and check if one of the other channels has a perfect match (for both obis and unit);
                        * If a perfect match is found later on, then validation should not fail. */
                    incorrectChannelUnitProblem = Optional.of(
                            getIssueService().newProblem(
                                    loadProfileConfigurationObisCode,
                                    MessageSeeds.CHANNEL_UNIT_MISMATCH,
                                    loadProfileConfigurationObisCode,
                                    meterChannelInfo.getChannelObisCode(),
                                    meterChannelInfo.getUnit(),
                                    localChannelInfo.getUnit()));
                } else {
                    return issues; // Configuration of the channel match, so return
                }
            }
        }
        if (incorrectChannelUnitProblem.isPresent()) { // When configuration of the channel doesn't match
            issues.add(incorrectChannelUnitProblem.get());
        } else { // When the channel is missing (load profile in the meter doesn't have the channel, whilst it is configured in eiMaster)
            issues.add(getIssueService().newProblem(
                    loadProfileConfigurationObisCode,
                    MessageSeeds.LOAD_PROFILE_CHANNEL_MISSING,
                    localChannelInfo.getChannelObisCode(),
                    loadProfileConfigurationObisCode
            ));
        }
        return issues;
    }

    /**
     * Compare 2 channel infos
     * Only ignore the B-field if it's a wildcard.
     */
    private boolean match(ChannelInfo localChannelInfo, ChannelInfo meterChannelInfo) {
        try {
            if (meterChannelInfo.getChannelObisCode().anyChannel() || localChannelInfo.getChannelObisCode().anyChannel()) {
                return meterChannelInfo.getChannelObisCode().equalsIgnoreBChannel(localChannelInfo.getChannelObisCode())
                        && meterChannelInfo.getMeterIdentifier().equalsIgnoreCase(localChannelInfo.getMeterIdentifier());
            } else {
                return meterChannelInfo.getChannelObisCode().equals(localChannelInfo.getChannelObisCode())
                        && meterChannelInfo.getMeterIdentifier().equalsIgnoreCase(localChannelInfo.getMeterIdentifier());
            }
        } catch (IllegalArgumentException e) {
            throw new DataParseException(e, MessageSeeds.COULD_NOT_PARSE_OBIS_CODE);
        }
    }

    private boolean unitMismatch(ChannelInfo localChannelInfo, ChannelInfo meterChannelInfo) {
        return !(meterChannelInfo.getUnit().isUndefined() || localChannelInfo.getUnit().isUndefined())
                && !meterChannelInfo.getUnit().getBaseUnit().equals(localChannelInfo.getUnit().getBaseUnit());
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
                    Instant from = collectedLoadProfile.getCollectedIntervalDataRange().lowerEndpoint().isBefore(lastLogBookDate)
                            ? collectedLoadProfile.getCollectedIntervalDataRange().lowerEndpoint() :
                            lastLogBookDate;
                    Instant to = collectedLoadProfile.getCollectedIntervalDataRange().upperEndpoint();
                    descriptionBuilder.append(
                            MessageFormat.format(
                                    "dataPeriod: [{0,date,yyyy-MM-dd HH:mm:ss} - {1,date,yyy-MM-dd HH:mm:ss}]",
                                    Date.from(from),
                                    Date.from(to)));
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
