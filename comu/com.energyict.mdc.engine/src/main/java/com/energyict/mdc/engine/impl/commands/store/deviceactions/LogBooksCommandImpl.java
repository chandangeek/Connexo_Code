package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLogBooksCommand;
import com.energyict.mdc.engine.impl.commands.store.core.CompositeComCommandImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBook;
import com.energyict.mdc.tasks.LogBooksTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple command to read out the requested {@link com.energyict.mdc.protocol.api.device.BaseLogBook logBooks} from the device
 *
 * @author sva
 * @since 07/12/12 - 11:36
 */
public class LogBooksCommandImpl extends CompositeComCommandImpl implements LogBooksCommand {

    /**
     * The task used for modeling this command
     */
    private final LogBooksTask logBooksTask;

    private List<LogBookReader> logBookReaders = new ArrayList<>();

    public LogBooksCommandImpl(final LogBooksTask logBooksTask, final OfflineDevice device, final CommandRoot commandRoot, ComTaskExecution comTaskExecution) {
        super(commandRoot);
        if (logBooksTask == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "logbookstask", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (device == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "device", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (commandRoot == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "commandRoot", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        this.logBooksTask = logBooksTask;
        createLogBookReaders(device, comTaskExecution.getDevice().getmRID());

        ReadLogBooksCommand readLogBooksCommand = getCommandRoot().getReadLogBooksCommand(this, comTaskExecution);
        readLogBooksCommand.addLogBooks(this.logBookReaders);
    }

    @Override
    public String getDescriptionTitle() {
        return "Executed logbook protocol task";
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        PropertyDescriptionBuilder logBookObisCodesBuilder = builder.addListProperty("logBookObisCodes");
        for (LogBookReader logBookReader : this.logBookReaders) {
            logBookObisCodesBuilder = logBookObisCodesBuilder.append(logBookReader.getLogBookObisCode()).next();
        }
    }

    /**
     * Create {@link LogBookReader}q for this {@link LogBooksCommand}, based on the {@link LogBookType}s specified in the {@link #logBooksTask}.
     * If no types are specified, then a {@link LogBookReader} for all
     * of the {@link com.energyict.mdc.protocol.api.device.BaseLogBook}s of the device will be created.
     *
     * @param device the <i>Master</i> Device for which LoadProfileReaders should be created
     * @param deviceMrid
     */
    private void createLogBookReaders(final OfflineDevice device, String deviceMrid) {
        List<OfflineLogBook> listOfAllLogBooks = device.getAllOfflineLogBooksForMRID(deviceMrid);
        if (this.logBooksTask.getLogBookTypes().isEmpty()) {
            listOfAllLogBooks.forEach(this::addLogBookToReaderList);
        } else {
            for (LogBookType logBookType : this.logBooksTask.getLogBookTypes()) {
                listOfAllLogBooks
                        .stream()
                        .filter(logBook -> logBookType.getId() == logBook.getLogBookTypeId())
                        .forEach(this::addLogBookToReaderList);
            }
        }
    }

    /**
     * Add the given {@link com.energyict.mdc.protocol.api.device.BaseLogBook} to the {@link #logBookReaders list}
     *
     * @param logBook the logBook to add
     */
    protected void addLogBookToReaderList(final OfflineLogBook logBook) {
        LogBookReader logBookReader =
                new LogBookReader(
                        this.getClock(),
                        logBook.getObisCode(),
                        logBook.getLastLogBook(),
                        logBook.getLogBookIdentifier(),
                        logBook.getDeviceIdentifier());
        this.logBookReaders.add(logBookReader);
    }

    protected List<LogBookReader> getLogBookReaders() {
        return logBookReaders;
    }

    @Override
    public LogBooksTask getLogBooksTask() {
        return logBooksTask;
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.LOGBOOKS_COMMAND;
    }

}