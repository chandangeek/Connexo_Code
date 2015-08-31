package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.CanProvideDescriptionTitle;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilderImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import com.elster.jupiter.events.EventService;

import java.time.Clock;

/**
 * Serves as the root for all components that intend to implement
 * the {@link DeviceCommand} interface and will provide
 * code reuse opportunities.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-23 (09:10)
 */
public abstract class DeviceCommandImpl implements DeviceCommand, CanProvideDescriptionTitle {

    private ExecutionLogger logger;
    private final ComTaskExecution comTaskExecution;
    private final ServiceProvider serviceProvider;

    public DeviceCommandImpl(ComTaskExecution comTaskExecution, ServiceProvider serviceProvider) {
        super();
        this.comTaskExecution = comTaskExecution;
        this.serviceProvider = serviceProvider;
    }

    protected ComTaskExecution getComTaskExecution() {
        return comTaskExecution;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    @Override
    public final void execute(ComServerDAO comServerDAO) {
        try {
            this.doExecute(comServerDAO);
        } finally {
            if (this.getExecutionLogger() != null) {
                this.getExecutionLogger().executed(this);
            }
        }
    }

    protected abstract void doExecute(ComServerDAO comServerDAO);

    protected IssueService getIssueService() {
        return this.serviceProvider.issueService();
    }

    protected Clock getClock() {
        return this.serviceProvider.clock();
    }

    protected MdcReadingTypeUtilService getMdcReadingTypeUtilService() {
        return this.serviceProvider.mdcReadingTypeUtilService();
    }

    protected EngineService getEngineService() {
        return this.serviceProvider.engineService();
    }

    protected EventService getEventService() {
        return this.serviceProvider.eventService();
    }

    @Override
    public void executeDuringShutdown(ComServerDAO comServerDAO) {
        /* Default is NOT to execute during shutdown
         * Really urgent subclasses will override this
         * and call the execute method instead. */
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel() {
        return ComServer.LogLevel.DEBUG;
    }

    @Override
    public void logExecutionWith(ExecutionLogger logger) {
        this.logger = logger;
    }

    protected ExecutionLogger getExecutionLogger() {
        return this.logger;
    }

    /**
     * Adds the specified {@link Issue} to the {@link ExecutionLogger}
     * against the related {@link ComTaskExecution}.
     *
     * @param completionCode the additional completionCode
     * @param issue the issue that should be logged
     */
    protected void addIssue (CompletionCode completionCode, Issue issue) {
        this.getExecutionLogger().addIssue(completionCode, issue, this.getComTaskExecution());
    }

    @Override
    public String toJournalMessageDescription(ComServer.LogLevel serverLogLevel) {
        DescriptionBuilder builder = new DescriptionBuilderImpl(this);
        toJournalMessageDescription(builder, serverLogLevel);
        return builder.toString();
    }

    protected abstract void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel);

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