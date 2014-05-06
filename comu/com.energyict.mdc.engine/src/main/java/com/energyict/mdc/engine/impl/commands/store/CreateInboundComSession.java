package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.comserver.logging.LoggerFactory;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilderImpl;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;

/**
 * Provides an implementation for the {@link DeviceCommand} interface
 * that will create a ComSession
 * for inbound communication.
 * Note that this task should be executed as late as possible
 * as the ComSession statistics include the execution time
 * of all DeviceCommands.
 * <p/>
 * Copyrights EnergyICT
 * Date: 25/10/12
 * Time: 14:34
 */
public class CreateInboundComSession extends ExecutionLoggerImpl implements CreateComSessionDeviceCommand {

    private InboundComPort comPort;
    private InboundConnectionTask connectionTask;
    private ComSessionShadow shadow;

    public CreateInboundComSession (InboundComPort comPort, InboundConnectionTask connectionTask, ComSessionShadow shadow) {
        super(comPort.getComServer().getCommunicationLogLevel());
        this.comPort = comPort;
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
            comServerDAO.createInboundComSession(this.connectionTask, this.shadow);
        }
        catch (RuntimeException e) {
            if (this.connectionTask == null) {
                LoggerFactory.getLoggerFor(DeviceCommandLogger.class).inboundComSessionCreationFailed(e, this.comPort);
            }
            else {
                LoggerFactory.getLoggerFor(DeviceCommandLogger.class).inboundComSessionCreationFailed(e, this.comPort, this.connectionTask);
            }
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