package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilderImpl;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.model.ComServer;

/**
 * Provides an implementation for the {@link DeviceCommand} interface
 * that will create an outbound ComSession.
 * Note that this task should be executed as late as possible
 * as the ComSession statistics include the execution time
 * of all DeviceCommands.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-23 (11:41)
 */
public class CreateOutboundComSession extends ExecutionLoggerImpl implements CreateComSessionDeviceCommand {

    private ScheduledConnectionTask connectionTask;
    private ComSessionShadow shadow;

    public CreateOutboundComSession (ComServer.LogLevel communicationLogLevel, ScheduledConnectionTask connectionTask, ComSessionShadow shadow) {
        super(communicationLogLevel);
        this.connectionTask = connectionTask;
        this.shadow = shadow;
    }

    @Override
    public ComSessionShadow getComSessionShadow () {
        return shadow;
    }

    @Override
    public void execute (ComServerDAO comServerDAO) {
        try {
            comServerDAO.createOutboundComSession(this.connectionTask, this.shadow);
        }
        catch (RuntimeException e) {
            LoggerFactory.getLoggerFor(DeviceCommandLogger.class).outboundComSessionCreationFailed(e, this.connectionTask);
            throw e;
        }
    }

    @Override
    public void executeDuringShutdown (ComServerDAO comServerDAO) {
        this.execute(comServerDAO);
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel () {
        return ComServer.LogLevel.TRACE;
    }

    @Override
    public void logExecutionWith (ExecutionLogger logger) {
        // I am the ExecutionLogger so ignore this
    }

    @Override
    public String getDescriptionTitle() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toJournalMessageDescription(ComServer.LogLevel serverLogLevel) {  //TODO: this method is currently nowhere used
        DescriptionBuilder builder = new DescriptionBuilderImpl(this);
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.DEBUG)) {
            builder.addProperty("indicator").append(shadow.getSuccessIndicator());
            builder.addProperty("connectionTaskId").append(shadow.getConnectionTaskId());
            builder.addProperty("comPortId").append(shadow.getComPortId());
            builder.addProperty("number of tasks").append(shadow.getComTaskExecutionSessionShadows().size());
            builder.addProperty("number of journal entries").append(shadow.getJournalEntryShadows().size());
        }
        return builder.toString();
    }

    /**
     * Tests if the specified server log level enables details of the
     * minimum level to be shown in journal messages.
     *
     * @param serverLogLevel The server LogLevel
     * @param minimumLevel   The minimum level that is required for a message to show up in journaling
     * @return A flag that indicates if message details of the minimum level should show up in journaling
     */
    protected boolean isJournalingLevelEnabled(ComServer.LogLevel serverLogLevel, ComServer.LogLevel minimumLevel) {
        return serverLogLevel.compareTo(minimumLevel) >= 0;
    }
}