package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.events.datastorage.NoopCollectedDataEvent;
import com.energyict.mdc.issues.Issue;

import java.util.*;

/**
 * Represents a no-operation {@link DeviceCommand},
 * i.e. a DeviceCommand that will do nothing.
 */
public class NoopDeviceCommand extends DeviceCommandImpl<NoopCollectedDataEvent> {

    private final static String DESCRIPTION_TITLE = "No operations device command";

    public NoopDeviceCommand() {
        /* Not passing the ComTaskExecution because current implementation
         * is not using it in any way. */
        super(null, new NoDeviceCommandServices());
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

    protected Optional<NoopCollectedDataEvent> newEvent(Issue issue) {
        return Optional.empty();
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }

}