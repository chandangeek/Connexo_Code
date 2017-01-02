package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.events.EventService;
import com.energyict.mdc.common.comserver.logging.CanProvideDescriptionTitle;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilderImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.upl.issue.Issue;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Serves as the root for all components that intend to implement
 * the {@link DeviceCommand} interface and will provide
 * code reuse opportunities.
 *
 * @param <E> Event type that will be published once the DeviceCommand is executed
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-23 (09:10)
 */
public abstract class DeviceCommandImpl<E extends CollectedDataProcessingEvent> implements DeviceCommand, CanProvideDescriptionTitle {

    private ExecutionLogger logger;
    private final ComTaskExecution comTaskExecution;
    private final ServiceProvider serviceProvider;
    private List<Issue> issues = new ArrayList<>();

    public DeviceCommandImpl(ComTaskExecution comTaskExecution, ServiceProvider serviceProvider) {
        super();
        this.comTaskExecution = comTaskExecution;
        this.serviceProvider = serviceProvider;
    }

    protected ComTaskExecution getComTaskExecution() {
        return comTaskExecution;
    }

    public DeviceCommand.ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public ExecutionLogger getLogger() {
        return logger;
    }

    @Override
    public final void execute(ComServerDAO comServerDAO) {
        try {
            this.doExecute(comServerDAO);
            //Event Mechanism
            Optional<E> event = newEvent(getIssues());
            if (event.isPresent()) {
                publish(event.get());
            }
         } finally {
            if (logger != null) {
                logger.executed(this);
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

    protected void publish(E event){
        // event will be null if the execution of the device command did not result in data storage
        if (event != null) {
            EventPublisher publisher = this.serviceProvider.eventPublisher();
            if (publisher != null) {
                publisher.publish(event);
            }
        }
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
        this.issues.add(issue);
        if (logger != null) {
            logger.addIssue(completionCode, issue, this.getComTaskExecution());
        }
    }

    @Override
    public String toJournalMessageDescription(ComServer.LogLevel serverLogLevel) {
        DescriptionBuilder builder = new DescriptionBuilderImpl(this);
        toJournalMessageDescription(builder, serverLogLevel);
        return builder.toString();
    }
    // Needs to be overriden by subclasses for which 'data storage' events should be thrown;
    protected Optional<E> newEvent(List<Issue> issues) {
        return Optional.empty();
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

    protected class ComServerEventServiceProvider implements AbstractComServerEventImpl.ServiceProvider {
        @Override
        public Clock clock() {
            return serviceProvider.clock();
        }
    }
}