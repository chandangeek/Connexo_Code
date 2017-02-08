/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.LogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLogBooksCommand;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;

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
        List<LogBookReader> logBookReaders = this.getLogBooksToCollect();
        setCollectedLogBooks(deviceProtocol.getLogBookData(logBookReaders));
        this.logBooksCommand.addListOfCollectedDataItems(collectedLogBooks);
    }

    protected List<LogBookReader> getLogBooksToCollect() {
        return this.logBooksCommand.getLogBookReaders();
    }

    private void setCollectedLogBooks(List<CollectedLogBook> collectedLogBooks) {
        this.collectedLogBooks = collectedLogBooks;
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