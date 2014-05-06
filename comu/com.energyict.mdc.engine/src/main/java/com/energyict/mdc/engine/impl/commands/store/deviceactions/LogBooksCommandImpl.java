package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLogBooksCommand;
import com.energyict.mdc.engine.impl.commands.store.core.CompositeComCommandImpl;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBook;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
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
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "logbookstask");
        }
        if (device == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "device");
        }
        if (commandRoot == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "commandRoot");
        }
        this.logBooksTask = logBooksTask;
        createLogBookReaders(device);

        ReadLogBooksCommand readLogBooksCommand = getCommandRoot().getReadLogBooksCommand(this, comTaskExecution);
        readLogBooksCommand.addLogBooks(this.logBookReaders);
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
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
     */
    private void createLogBookReaders(final OfflineDevice device) {
        List<OfflineLogBook> listOfAllLogBooks = device.getAllOfflineLogBooks();
        if (this.logBooksTask.getLogBookTypes().isEmpty()) {
            for (OfflineLogBook logBook : listOfAllLogBooks) {
                addLogBookToReaderList(logBook);
            }
        } else {
            for (LogBookType logBookType : this.logBooksTask.getLogBookTypes()) {
                for (OfflineLogBook logBook : listOfAllLogBooks) {
                    if (logBookType.getId() == logBook.getLogBookTypeId()) {
                        addLogBookToReaderList(logBook);
                    }
                }
            }
        }
    }

    /**
     * Add the given {@link com.energyict.mdc.protocol.api.device.BaseLogBook} to the {@link #logBookReaders list}
     *
     * @param logBook the logBook to add
     */
    protected void addLogBookToReaderList(final OfflineLogBook logBook) {
        LogBookIdentifierByIdImpl logBookIdentifier = new LogBookIdentifierByIdImpl((int) logBook.getLogBookId());
        LogBookReader logBookReader =
                new LogBookReader(
                        logBook.getObisCode(),
                        logBook.getLastLogBook(),
                        logBookIdentifier,
                        logBook.getMasterSerialNumber());
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