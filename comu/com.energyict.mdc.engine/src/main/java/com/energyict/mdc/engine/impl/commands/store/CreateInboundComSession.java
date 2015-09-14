package com.energyict.mdc.engine.impl.commands.store;

import java.time.Clock;
import java.time.Duration;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilderImpl;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;

import com.elster.jupiter.util.time.StopWatch;

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

    private final InboundComPort comPort;
    private final InboundConnectionTask connectionTask;
    private final ComSessionBuilder builder;
    private ComSession.SuccessIndicator successIndicator;
    private ComSession inboundComSession;
    private StopWatch stopWatch;

    public CreateInboundComSession(InboundComPort comPort, InboundConnectionTask connectionTask, ComSessionBuilder builder, ComSession.SuccessIndicator successIndicator, Clock clock) {
        super(comPort.getComServer().getCommunicationLogLevel(), clock);
        this.comPort = comPort;
        this.connectionTask = connectionTask;
        this.builder = builder;
        this.successIndicator = successIndicator;
    }

    @Override
    public ConnectionTask getConnectionTask() {
        return connectionTask;
    }

    @Override
    public void setStopWatch(StopWatch stopWatch) {
        this.stopWatch = stopWatch;
    }

    @Override
    public void updateSuccessIndicator(ComSession.SuccessIndicator successIndicator) {
        this.successIndicator = successIndicator;
    }

    @Override
    public ComSessionBuilder getComSessionBuilder() {
        return builder;
    }

    @Override
    public void execute (ComServerDAO comServerDAO) {
        try {
            this.stopWatch.stop();
            this.builder.storeDuration(Duration.ofNanos(this.stopWatch.getElapsed()));
            this.inboundComSession = comServerDAO.createComSession(this.builder, successIndicator);
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
    public ComSession getComSession() {
        return this.inboundComSession;
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
        return "Create inbound comSession command";
    }

    @Override
    public String toJournalMessageDescription(ComServer.LogLevel serverLogLevel) {
        if (this.inboundComSession == null) {
            return "";
        }
        else {
            DescriptionBuilder builder = new DescriptionBuilderImpl(this);
            if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.DEBUG)) {
                builder.addProperty("indicator").append(this.inboundComSession.getSuccessIndicator());
                builder.addProperty("connectionTaskId").append(this.inboundComSession.getConnectionTask().getId());
                builder.addProperty("comPortId").append(this.inboundComSession.getComPort().getId());
                builder.addProperty("number of tasks").append(this.inboundComSession.getComTaskExecutionSessions().size());
                builder.addProperty("number of journal entries").append(this.inboundComSession.getJournalEntries().size());
            }
            return builder.toString();
        }
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