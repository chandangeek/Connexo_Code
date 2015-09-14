package com.energyict.mdc.engine.impl.commands.store.legacy;

import com.energyict.mdc.engine.exceptions.ComCommandException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolAdapter;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Command to initialize the logger on a {@link DeviceProtocolAdapter}.
 *
 * Copyrights EnergyICT
 * Date: 9/08/12
 * Time: 11:01
 */
public class InitializeLoggerCommand extends SimpleComCommand {

    public InitializeLoggerCommand(final CommandRoot commandRoot) {
        super(commandRoot);
    }

    @Override
    public void doExecute (DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        if (deviceProtocol instanceof DeviceProtocolAdapter) {
            Logger logger = this.newProtocolLogger(executionContext);
            ((DeviceProtocolAdapter) deviceProtocol).initializeLogger(logger);
        } else {
            throw ComCommandException.illegalCommand(this, deviceProtocol, MessageSeeds.ILLEGAL_COMMAND);
        }
    }

    /**
     * Creates a logger that will log all entries produced
     * by the protocol, except INFO and CONFIG in the
     * {@link com.energyict.mdc.engine.impl.core.ExecutionContext}'s ComSession.
     *
     * @return The Logger
     */
    private Logger newProtocolLogger (ExecutionContext executionContext) {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.FINEST);
        logger.addHandler(new ExecutionContextForwardHandler(executionContext));
        return logger;
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.INIT_LOGGER_COMMAND;
    }

    protected LogLevel defaultJournalingLogLevel () {
        return LogLevel.DEBUG;
    }

    @Override
    public String getDescriptionTitle() {
        return "Initialize the protocol logger";
    }

    /**
     * A Handler that forwards all records to the {@link com.energyict.mdc.engine.impl.core.ExecutionContext}'s Logger
     * leaving it to that Logger's responsibility if the record should be logged or not.
     * Note that the level of every record is actually set to FINEST before forwarding it
     * so that the log messages produced by old device protocols are only visible
     * when DEBUG log level is switched on.
     * Note that the ComCommand classes are already mimicking quite a lot of that logging today.
     */
    private final class ExecutionContextForwardHandler extends Handler {
        private ExecutionContext executionContext;

        private ExecutionContextForwardHandler (ExecutionContext executionContext) {
            this.executionContext = executionContext;
        }

        @Override
        public void publish (LogRecord record) {
            record.setLevel(Level.FINEST);
            this.executionContext.getLogger().log(record);
        }

        @Override
        public void flush () {
            // Nothing to flush
        }

        @Override
        public void close () throws SecurityException {
            // Nothing to close
        }
    }

}