package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.model.ComServer;

/**
 * Represents a no-operation {@link DeviceCommand},
 * i.e. a DeviceCommand that will do nothing.
 */
public class NoopDeviceCommand extends DeviceCommandImpl {

    public NoopDeviceCommand() {
        super(null);
    }

    @Override
    public void doExecute (ComServerDAO comServerDAO) {
        // noop is designed to do nothing
    }

    @Override
    public void executeDuringShutdown (ComServerDAO comServerDAO) {
        // noop is designed to do nothing
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel () {
        return ComServer.LogLevel.TRACE;
    }

    @Override
    public void logExecutionWith (ExecutionLogger logger) {
        // noop is not execution so nothing to log
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
    }
}
