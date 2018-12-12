/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.LogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLogBooksCommand;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DeviceLogBook;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.Problem;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.protocol.LogBookReader;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for a ReadLogBooksCommand
 *
 * @author sva
 * @since 07/12/12 - 16:30
 */
public class ReadLogBooksCommandImpl extends SimpleComCommand implements ReadLogBooksCommand {

    /**
     * List of {@link CollectedLogBook} which have been collected from the device
     */
    private List<CollectedLogBook> collectedLogBooks = new ArrayList<>();

    /**
     * The {@link LogBooksCommand} which owns this command
     */
    private LogBooksCommand logBooksCommand;

    public ReadLogBooksCommandImpl(final GroupedDeviceCommand groupedDeviceCommand, final LogBooksCommand logBooksCommand) {
        super(groupedDeviceCommand);
        this.logBooksCommand = logBooksCommand;
    }

    @Override
    public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        verifyObisCodeRequiresSerialNumber();
        collectedLogBooks.addAll(deviceProtocol.getLogBookData(getLogBooksToCollect()));
        this.logBooksCommand.addListOfCollectedDataItems(collectedLogBooks);
    }

    /**
     * An obis code that contains an x (any channel) for the B-field requires the serial number to be filled in.
     * If it's not filled in, that specific logbook cannot (and will not) be read out. A proper issue will be logged.
     */
    private void verifyObisCodeRequiresSerialNumber() {
        List<LogBookReader> logBookReadersToRemove = new ArrayList<>();
        for (LogBookReader logBookReader : getLogBooksToCollect()) {
            if (logBookReader.getLogBookObisCode().anyChannel() && (isEmpty(logBookReader.getMeterSerialNumber()))) {
                Problem issue = getIssueService().newProblem(
                        logBookReader.getLogBookObisCode(),
                        MessageSeeds.ANY_CHANNEL_OBIS_CODE_REQUIRES_SERIAL_NUMBER,
                        logBookReader.getLogBookObisCode()
                );
                logBookReadersToRemove.add(logBookReader);
                createAndAddFailedCollectedLogBook(logBookReader, ResultType.ConfigurationError, issue);
            }
        }

        logBookReadersToRemove.forEach(logBookReader -> this.logBooksCommand.removeLogBookReader(logBookReader));
    }

    private void createAndAddFailedCollectedLogBook(LogBookReader logBookReader, ResultType resultType, Issue issue) {
        DeviceLogBook collectedLogBook = createFailedCollectedLogBook(
                logBookReader,
                issue,
                resultType);
        collectedLogBooks.add(collectedLogBook);
    }

    private DeviceLogBook createFailedCollectedLogBook(LogBookReader logBookReader, Issue issue, ResultType resultType) {
        DeviceLogBook collectedLogBook = new DeviceLogBook(logBookReader.getLogBookIdentifier());
        collectedLogBook.setFailureInformation(resultType, issue);
        return collectedLogBook;
    }

    protected List<LogBookReader> getLogBooksToCollect() {
        return this.logBooksCommand.getLogBookReaders();
    }

    @Override
    public String getDescriptionTitle() {
        return "Read out the device logbooks";
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (this.logBooksCommand.getLogBookReaders().isEmpty()) {
            builder.addLabel("No log books to read");
        } else {
            if (isJournalingLevelEnabled(serverLogLevel, LogLevel.DEBUG)) {
                PropertyDescriptionBuilder logbookObisCodesBuilder = builder.addListProperty("logbooks");
                for (LogBookReader logBookReader : this.logBooksCommand.getLogBookReaders()) {
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
                    logbookObisCodesBuilder.next();
                }
            } else {
                builder.addProperty("nrOfLogbooksToRead").append(this.logBooksCommand.getLogBookReaders().size());
            }
        }
    }

    private CollectedLogBook getCollectedLogBookForLogbookReader(LogBookReader reader) {
        for (CollectedLogBook collectedLogBook : collectedLogBooks) {
            if (collectedLogBook.getLogBookIdentifier().getLogBookObisCode().equals(reader.getLogBookIdentifier().getLogBookObisCode())) {
                return collectedLogBook;
            }
        }
        return null;
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.READ_LOGBOOKS_COMMAND;
    }
}