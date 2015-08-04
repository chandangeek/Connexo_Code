package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import com.elster.jupiter.events.EventService;

import java.time.Clock;

/**
 * Models the behavior of a command component
 * (as in <a href="http://en.wikipedia.org/wiki/Command_pattern">Command Design Pattern</a>)
 * that executes against a {@link com.energyict.mdc.protocol.api.device.BaseDevice device}.
 * The command are kept separate from the devices to be able to delay the execution.
 * The execution is done by the {@link DeviceCommandExecutor} process.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-17 (15:59)
 */
public interface DeviceCommand {

    /**
     * Provides Loging services for the execution of DeviceCommands.
     */
    public interface ExecutionLogger {

        /**
         * Logs that the specified DeviceCommand completed its execution.
         *
         * @param deviceCommand The DeviceCommand
         */
        public void executed (DeviceCommand deviceCommand);

        /**
         * Logs that an unexpected problem occurred.
         *
         * @param t The unexpected problem
         * @param comTaskExecution The context of the execution
         */
        public void logUnexpected (Throwable t, ComTaskExecution comTaskExecution);

        /**
         * Adds an additional issue to the log of a ComTaskExecution
         *
         * @param completionCode the additional completionCode
         * @param issue the issue that should be logged
         * @param comTaskExecution The ComTaskExecution
         */
        public void addIssue (CompletionCode completionCode, Issue issue, ComTaskExecution comTaskExecution);

    }

    /**
     * The list of all services that are required by
     * the different types {@link DeviceCommand}s.
     */
    public interface ServiceProvider {

        public EventService eventService();

        public IssueService issueService();

        public Clock clock();

        public MdcReadingTypeUtilService mdcReadingTypeUtilService();

        public EngineService engineService();

        public NlsService nlsService();

    }
    /**
     * Executes this DeviceCommand.<br>
     * Note that this may throw all of the runtime exceptions
     * mentioned in the {@link ComServerDAO} documentation.
     *
     * @param comServerDAO The ComServerDAO that must be used to access the database
     */
    public void execute (ComServerDAO comServerDAO);

    /**
     * Executes this DeviceCommand while the ComServer is shutting down.
     * Only really urgent DeviceCommands should actually be responding
     * to this execute request and will effectively call the execute method.<br>
     * Note that this may throw all of the runtime exceptions
     * mentioned in the {@link ComServerDAO} documentation.
     *
     * @param comServerDAO The ComServerDAO that must be used to access the database
     */
    public void executeDuringShutdown (ComServerDAO comServerDAO);

    /**
     * Instructs this DeviceCommand to log while executing
     * with the specified ExecutionLogger.
     *
     * @param logger The ExecutionLogger
     */
    public void logExecutionWith (ExecutionLogger logger);

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
    public ComServer.LogLevel getJournalingLogLevel ();

    /**
     * Converts this DeviceCommand to a String that will be used
     * as the human readable description for logging purposes.
     *
     * @param serverLogLevel The LogLevel set on the ComServer
     * @return The human readable description of this DeviceCommand
     */
    public String toJournalMessageDescription(ComServer.LogLevel serverLogLevel);

}