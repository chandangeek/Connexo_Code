package com.energyict.mdc.engine.impl.core.aspects.events;

import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.core.CommandCreator;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.core.aspects.logging.AbstractComCommandLogging;
import com.energyict.mdc.engine.impl.core.aspects.logging.ComCommandLogger;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extends {@link AbstractComCommandLogging} to inject a Logger
 * that will publish log messages as a {@link com.energyict.mdc.engine.events.ComServerEvent}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-07 (16:22)
 */
public aspect ComCommandLogEventPublisher extends AbstractComCommandLogging {

    @Override
    protected ComCommandLogger getAnonymousLogger () {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.FINEST);
        return LoggerFactory.getLoggerFor(ComCommandLogger.class, logger);
    }

    @Override
    protected ComCommandLogger getActualLogger (Logger sharedLogger, JobExecution.ExecutionContext executionContext) {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.FINEST);
        logger.addHandler(new ComCommandLogHandler(executionContext.getComPort(), executionContext.getConnectionTask(), executionContext.getComTaskExecution()));
        return LoggerFactory.getLoggerFor(ComCommandLogger.class, logger);
    }

    @Override
    protected void logCreatedCommands (CommandCreator commandCreator, CommandRoot commandRoot) {
        // The logging is actually done by my sibbling class
    }

    @Override
    protected void logStartOfBasicCheckCommand (JobExecution.ExecutionContext executionContext) {
        // The logging is actually done by my sibbling class
    }

    @Override
    protected void logStartOfVerifySerialNumberCommand (JobExecution.ExecutionContext executionContext) {
        // The logging is actually done by my sibbling class
    }

    @Override
    protected void logSerialNumberMisMatch (JobExecution.ExecutionContext executionContext, String meterSerialNumber, String configuredSerialNumber) {
        // The logging is actually done by my sibbling class
    }

    @Override
    protected void logStartOfClockCommand (JobExecution.ExecutionContext executionContext) {
        // The logging is actually done by my sibbling class
    }

    @Override
    protected void logStartOfSetTime (JobExecution.ExecutionContext executionContext, Date timeToSet) {
        // The logging is actually done by my sibbling class
    }

    @Override
    protected void logTimeDifferenceAboveMaximum (JobExecution.ExecutionContext executionContext, long timeDifference) {
        // The logging is actually done by my sibbling class
    }

    @Override
    protected void logTimeDifferenceBelowMinimum (JobExecution.ExecutionContext executionContext, long timeDifference) {
        // The logging is actually done by my sibbling class
    }

    @Override
    protected void logStartOfSynchronizeClockCommand (JobExecution.ExecutionContext executionContext) {
        // The logging is actually done by my sibbling class
    }

    @Override
    protected void logSynchronizeClockWithTimeShift (JobExecution.ExecutionContext executionContext, long timeShift) {
        // The logging is actually done by my sibbling class
    }

    @Override
    protected void logStartOfTimeDifferenceCommand (JobExecution.ExecutionContext executionContext) {
        // The logging is actually done by my sibbling class
    }

    @Override
    protected void logStartOfVerifyTimeDifferenceCommand (JobExecution.ExecutionContext executionContext) {
        // The logging is actually done by my sibbling class
    }

    @Override
    protected void logTimeDifferenceExceeded (JobExecution.ExecutionContext executionContext, long actualTimeDifference, long maximumTimeDifference) {
        // The logging is actually done by my sibbling class
    }

    @Override
    protected void logStartOfForceClockCommand (JobExecution.ExecutionContext executionContext) {
        // The logging is actually done by my sibbling class
    }

    @Override
    protected void logStartOfLoadProfileCommand (JobExecution.ExecutionContext executionContext) {
        // The logging is actually done by my sibbling class
    }

    @Override
    protected void logStartOfMarkIntervalsAsBadTimeCommand (JobExecution.ExecutionContext executionContext) {
        // The logging is actually done by my sibbling class
    }

    @Override
    protected void logStartOfCreateMeterEventsFromStatusBits (JobExecution.ExecutionContext executionContext) {
        // The logging is actually done by my sibbling class
    }

    @Override
    protected void logStartOfReadLoadProfileCommand (JobExecution.ExecutionContext executionContext) {
        // The logging is actually done by my sibbling class
    }

    @Override
    protected void logStartOfVerifyLoadProfileCommand (JobExecution.ExecutionContext executionContext) {
        // The logging is actually done by my sibbling class
    }

    @Override
    protected void logReadLoadProfileConfigurationsFromDevice (JobExecution.ExecutionContext executionContext, List<CollectedLoadProfileConfiguration> loadProfileConfigurations) {
        // The logging is actually done by my sibbling class
    }

}