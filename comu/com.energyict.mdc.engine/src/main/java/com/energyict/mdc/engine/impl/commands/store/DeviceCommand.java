package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.upl.issue.Issue;

import java.time.Clock;

/**
 * Models the behavior of a command component
 * (as in <a href="http://en.wikipedia.org/wiki/Command_pattern">Command Design Pattern</a>)
 * that executes against a {@link com.energyict.mdc.upl.meterdata.Device device}.
 * The command are kept separate from the devices to be able to delay the execution.
 * The execution is done by the {@link DeviceCommandExecutor} process.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-17 (15:59)
 */
public interface DeviceCommand {

    /**
     * Provides Logging services for the execution of DeviceCommands.
     */
    interface ExecutionLogger {

        /**
         * Logs that the specified DeviceCommand completed its execution.
         *
         * @param deviceCommand The DeviceCommand
         */
        void executed(DeviceCommand deviceCommand);

        /**
         * Logs that an unexpected problem occurred.
         *
         * @param t The unexpected problem
         * @param comTaskExecution The context of the execution
         */
        void logUnexpected(Throwable t, ComTaskExecution comTaskExecution);

        /**
         * Adds an additional issue to the log of a ComTaskExecution.
         *
         * @param completionCode the additional completionCode
         * @param issue the issue that should be logged
         * @param comTaskExecution The ComTaskExecution
         */
        void addIssue(CompletionCode completionCode, Issue issue, ComTaskExecution comTaskExecution);

        /**
         * Tests if {@link com.energyict.mdc.upl.issue.Problem}s have been added.
         *
         * @return true iff Problems have been added
         * @see #addIssue(CompletionCode, Issue, ComTaskExecution)
         */
        boolean hasProblems();

    }

    /**
     * The list of all services that are required by
     * the different types {@link DeviceCommand}s.
     */
    interface ServiceProvider {

        EventService eventService();

        IssueService issueService();

        Clock clock();

        MdcReadingTypeUtilService mdcReadingTypeUtilService();

        EngineService engineService();

        NlsService nlsService();

        EventPublisher eventPublisher();

    }

    /**
     * Executes this DeviceCommand.<br>
     * Note that this may throw all of the runtime exceptions
     * mentioned in the {@link ComServerDAO} documentation.
     *
     * @param comServerDAO The ComServerDAO that must be used to access the database
     */
    void execute(ComServerDAO comServerDAO);

    /**
     * Executes this DeviceCommand while the ComServer is shutting down.
     * Only really urgent DeviceCommands should actually be responding
     * to this execute request and will effectively call the execute method.<br>
     * Note that this may throw all of the runtime exceptions
     * mentioned in the {@link ComServerDAO} documentation.
     *
     * @param comServerDAO The ComServerDAO that must be used to access the database
     */
    void executeDuringShutdown(ComServerDAO comServerDAO);

    /**
     * Instructs this DeviceCommand to log while executing
     * with the specified ExecutionLogger.
     *
     * @param logger The ExecutionLogger
     */
    void logExecutionWith(ExecutionLogger logger);

    /**
     * Gets the minimum LogLevel that needs to be activated
     * before this DeviceCommand must be logged.
     * As an example when LogLevel {@link com.energyict.mdc.engine.config.ComServer.LogLevel#INFO} is returned
     * then the ComServer's log level must be at least INFO or higher
     * before this DeviceCommand will actually be logged as a
     * ComSessionJournalEntryShadow.
     *
     * @return The minimum ComServer.LogLevel
     */
    ComServer.LogLevel getJournalingLogLevel();

    /**
     * Converts this DeviceCommand to a String that will be used
     * as the human readable description for logging purposes.
     *
     * @param serverLogLevel The LogLevel set on the ComServer
     * @return The human readable description of this DeviceCommand
     */
    String toJournalMessageDescription(ComServer.LogLevel serverLogLevel);


}