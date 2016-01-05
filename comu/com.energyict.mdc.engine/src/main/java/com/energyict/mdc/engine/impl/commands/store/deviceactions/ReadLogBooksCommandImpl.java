package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLogBooksCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;

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
     * List of {@link LogBookReader} which need to be collected from the device.<br/>
     */
    private List<LogBookReader> logBooksToCollect = new ArrayList<>();

    /**
     * The {@link LogBooksCommand} which owns this command
     */
    private LogBooksCommand logBooksCommand;
    private final MeteringService meteringService;

    public ReadLogBooksCommandImpl(final LogBooksCommand logBooksCommand, final CommandRoot commandRoot) {
        super(commandRoot);
        this.logBooksCommand = logBooksCommand;
        this.meteringService = commandRoot.getServiceProvider().meteringService();
    }

    @Override
    public String getDescriptionTitle() {
        return "Read out the device logbooks";
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (this.logBooksToCollect.isEmpty()) {
            builder.addLabel("No log books to read");
        }
        else {
            PropertyDescriptionBuilder logbookObisCodesBuilder = builder.addListProperty("logbookObisCodes");
            for (LogBookReader logBookReader : this.logBooksToCollect) {
                logbookObisCodesBuilder = logbookObisCodesBuilder.append(logBookReader.getLogBookObisCode()).next();
            }
        }
    }

    @Override
    public void addLogBooks(final List<LogBookReader> logBooksToCollect) {
        if (logBooksToCollect != null) {
            // Do not attempt to use Streams here since canWeAddIt relies on side-effect
            for (LogBookReader logBookReader : logBooksToCollect) {
                if (canWeAddIt(logBookReader)) {
                    this.logBooksToCollect.add(logBookReader);
                }
            }
        }
    }

    /**
     * Check whether the {@link #logBooksToCollect} already contain this {@link LogBookReader}
     *
     * @param newReader the LogBookReader to check
     * @return true if it does not exist yet, false otherwise
     */
    protected boolean canWeAddIt(final LogBookReader newReader) {
        for (LogBookReader existingReader : this.logBooksToCollect) {
            if (this.sameLogBookObisCode(newReader, existingReader) || this.sameLogBook(newReader, existingReader)) {
                return false;
            }
        }
        return true;
    }

    private boolean sameLogBookObisCode(LogBookReader newReader, LogBookReader existingReader) {
        return existingReader.getLogBookObisCode().equals(newReader.getLogBookObisCode());
    }

    private boolean sameLogBook(LogBookReader newReader, LogBookReader existingReader) {
        LogBookIdentifier logBookIdentifier = existingReader.getLogBookIdentifier();
        LogBookIdentifier otherLogBookIdentifier = newReader.getLogBookIdentifier();
        return logBookIdentifier.getLogBook().equals(otherLogBookIdentifier.getLogBook());
    }

    @Override
    public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        List<LogBookReader> logBookReaders = this.getLogBooksToCollect();
        this.logBooksCommand.addListOfCollectedDataItems(deviceProtocol.getLogBookData(logBookReaders));
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.READ_LOGBOOKS_COMMAND;
    }

    protected List<LogBookReader> getLogBooksToCollect() {
        return this.logBooksToCollect;
    }

}