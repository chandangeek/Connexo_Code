package com.energyict.mdc.engine.impl.core.aspects.logging;

import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.core.CommandCreator;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines pointcuts and advice that will do logging for the {@link com.energyict.mdc.engine.impl.commands.collect.ComCommand}s.
 * <p/>
 * Copyrights EnergyICT
 * Date: 27/08/12
 * Time: 10:39
 */
public abstract aspect AbstractComCommandLogging {

    protected final ComCommandLogger getCommandLogger (JobExecution.ExecutionContext executionContext) {
        if (executionContext == null || executionContext.getComCommandLogger() == null) {
            return this.getAnonymousLogger();
        }
        else {
            return executionContext.getComCommandLogger();
        }
    }

    /**
     * Returns an anonymous logger that will mostly
     * be used during Test executions before
     * the actual logger is available.
     *
     * @return An anonymous logger that respects the ComCommandLogger interface
     */
    protected abstract ComCommandLogger getAnonymousLogger();

    /**
     * Returns a ComCommandLogger that is backed by the specified Logger.
     *
     * @param logger The actual logger
     * @param executionContext The ExecutionContext in which the execution of the ComCommand runs
     * @return The ComCommandLogger
     */
    protected abstract ComCommandLogger getActualLogger (Logger logger, JobExecution.ExecutionContext executionContext);

    private pointcut executeCommandRoot (CommandRootImpl commandRoot, JobExecution.ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, JobExecution.ExecutionContext))
         && target(commandRoot)
         && args(.., executionContext);

    before (CommandRootImpl commandRoot, JobExecution.ExecutionContext executionContext): executeCommandRoot(commandRoot, executionContext) {
        this.startActualLogger(executionContext);
    }

    private synchronized void startActualLogger (JobExecution.ExecutionContext executionContext) {
        if (executionContext != null) {
            CompositeComCommandLogger compositeLogger;
            compositeLogger = new CompositeComCommandLogger();
            executionContext.setComCommandLogger(compositeLogger);
            Logger logger = Logger.getAnonymousLogger();
            logger.setLevel(this.getCommunicationLogLevel(executionContext));
            ComCommandLogger comCommandLogger = this.getActualLogger(logger, executionContext);
            compositeLogger.start(comCommandLogger);
        }
    }

    private pointcut createdComCommands(CommandCreator commandCreator, CommandRoot commandRoot):
            execution(void CommandCreator+.createCommands(..))
                    && target(commandCreator)
                    && args(commandRoot, ..);

    after(CommandCreator commandCreator, CommandRoot commandRoot): createdComCommands(commandCreator, commandRoot){
        this.logCreatedCommands(commandCreator, commandRoot);
    }

    protected abstract void logCreatedCommands (CommandCreator commandCreator, CommandRoot commandRoot);

    private pointcut startBasicCheckCommand(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, JobExecution.ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.BasicCheckCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext): startBasicCheckCommand(deviceProtocol, executionContext) {
        this.logStartOfBasicCheckCommand(executionContext);
    }

    protected abstract void logStartOfBasicCheckCommand (JobExecution.ExecutionContext executionContext);

    private pointcut startVerifySerialNumberCommand(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, JobExecution.ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.VerifySerialNumberCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext): startVerifySerialNumberCommand(deviceProtocol, executionContext) {
        this.logStartOfVerifySerialNumberCommand(executionContext);
    }

    protected abstract void logStartOfVerifySerialNumberCommand (JobExecution.ExecutionContext executionContext);

    private pointcut serialNumberMisMatch(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext, String meterSerialNumber, String configuredSerialNumber):
            call(* com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException.serialNumberMisMatch(java.lang.String, java.lang.String))
         && args(meterSerialNumber, configuredSerialNumber)
         && cflowbelow(startVerifySerialNumberCommand(deviceProtocol, executionContext));

    before(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext, String meterSerialNumber, String configuredSerialNumber): serialNumberMisMatch(deviceProtocol, executionContext, meterSerialNumber, configuredSerialNumber){
        this.logSerialNumberMisMatch(executionContext, meterSerialNumber, configuredSerialNumber);
    }

    protected abstract void logSerialNumberMisMatch (JobExecution.ExecutionContext executionContext, String meterSerialNumber, String configuredSerialNumber);

    private pointcut startClockCommand(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, JobExecution.ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.ClockCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext): startClockCommand(deviceProtocol, executionContext) {
        this.logStartOfClockCommand(executionContext);
    }

    protected abstract void logStartOfClockCommand (JobExecution.ExecutionContext executionContext);

    /*
    Used a call instead of an execution because otherwise ALL protocols need to be woven for this logging...
     */
    private pointcut startedSetTime(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext, Date timeToSet):
            call(void com.energyict.mdc.protocol.api.tasks.support.DeviceClockSupport+.setTime(Date))
         //&& target(deviceProtocol)
         && args(timeToSet)
         && cflowbelow(startClockCommand(deviceProtocol, executionContext));

    before(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext, Date timeToSet): startedSetTime(deviceProtocol, executionContext, timeToSet) {
        this.logStartOfSetTime(executionContext, timeToSet);
    }

    protected abstract void logStartOfSetTime (JobExecution.ExecutionContext executionContext, Date timeToSet);

    private pointcut startedSetClockCommand(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.deviceactions.SetClockCommandImpl.execute(DeviceProtocol, JobExecution.ExecutionContext))
         && args(deviceProtocol, executionContext);

    private pointcut timeDiffAboveMaximum(long timeDifference):
            execution(private boolean com.energyict.mdc.engine.impl.commands.store.deviceactions.SetClockCommandImpl.aboveMaximum(long))
         && args(timeDifference);

    private pointcut timeDiffBelowMinimum(long timeDifference):
            execution(private boolean com.energyict.mdc.engine.impl.commands.store.deviceactions.SetClockCommandImpl.belowMinimum(long))
         && args(timeDifference);

    after(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext, long timeDifference) returning (boolean success):
        cflow(startedSetClockCommand(deviceProtocol, executionContext))
     && timeDiffAboveMaximum(timeDifference) {
        if (success) {
            this.logTimeDifferenceAboveMaximum(executionContext, timeDifference);
        }
    }

    protected abstract void logTimeDifferenceAboveMaximum (JobExecution.ExecutionContext executionContext, long timeDifference);

    after(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext, long timeDifference) returning (boolean success):
        cflow(startedSetClockCommand(deviceProtocol, executionContext))
     && timeDiffBelowMinimum(timeDifference) {
        if (success) {
            this.logTimeDifferenceBelowMinimum(executionContext, timeDifference);
        }
    }

    protected abstract void logTimeDifferenceBelowMinimum (JobExecution.ExecutionContext executionContext, long timeDifference);

    private pointcut startedSynchronizeClockCommand(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, JobExecution.ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.SynchronizeClockCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext): startedSynchronizeClockCommand(deviceProtocol, executionContext) {
        this.logStartOfSynchronizeClockCommand(executionContext);
    }

    protected abstract void logStartOfSynchronizeClockCommand (JobExecution.ExecutionContext executionContext);

    private pointcut getTimeShift(long timeDifference):
            execution(private long com.energyict.mdc.engine.impl.commands.store.deviceactions.SynchronizeClockCommandImpl.getTimeShift(long))
                    && args(timeDifference);

    after(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext, long timeDifference) returning (long timeShift):
        cflow(startedSynchronizeClockCommand(deviceProtocol, executionContext))
     && getTimeShift(timeDifference) {
        if (timeShift == 0) {
            this.logTimeDifferenceBelowMinimum(executionContext, timeDifference);
        } else {
            this.logSynchronizeClockWithTimeShift(executionContext, timeShift);
        }
    }

    protected abstract void logSynchronizeClockWithTimeShift (JobExecution.ExecutionContext executionContext, long timeShift);

    private pointcut startTimeDifferenceCommand(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, JobExecution.ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.TimeDifferenceCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext): startTimeDifferenceCommand(deviceProtocol, executionContext) {
        this.logStartOfTimeDifferenceCommand(executionContext);
    }

    protected abstract void logStartOfTimeDifferenceCommand (JobExecution.ExecutionContext executionContext);

    private pointcut startVerifyTimeDifferenceCommand(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, JobExecution.ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.VerifyTimeDifferenceCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext): startVerifyTimeDifferenceCommand(deviceProtocol, executionContext) {
        this.logStartOfVerifyTimeDifferenceCommand(executionContext);
    }

    protected abstract void logStartOfVerifyTimeDifferenceCommand (JobExecution.ExecutionContext executionContext);

    private pointcut timeDifferenceExceeded(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext, long actualTimeDifference, long maximumTimeDifference):
        call(* com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException.timeDifferenceExceeded(long, long))
            && args(actualTimeDifference, maximumTimeDifference)
            && cflowbelow(startVerifyTimeDifferenceCommand(deviceProtocol, executionContext));

    before(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext, long actualTimeDifference, long maximumTimeDifference): timeDifferenceExceeded(deviceProtocol, executionContext, actualTimeDifference, maximumTimeDifference){
        this.logTimeDifferenceExceeded(executionContext, actualTimeDifference, maximumTimeDifference);
    }

    protected abstract void logTimeDifferenceExceeded (JobExecution.ExecutionContext executionContext, long actualTimeDifference, long maximumTimeDifference);

    private pointcut startedForceClockCommand(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, JobExecution.ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.ForceClockCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext): startedForceClockCommand(deviceProtocol, executionContext) {
        this.logStartOfForceClockCommand(executionContext);
    }

    protected abstract void logStartOfForceClockCommand (JobExecution.ExecutionContext executionContext);

    private pointcut startLoadProfileCommand(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, JobExecution.ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.LoadProfileCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext): startLoadProfileCommand(deviceProtocol, executionContext) {
        this.logStartOfLoadProfileCommand(executionContext);
    }

    protected abstract void logStartOfLoadProfileCommand (JobExecution.ExecutionContext executionContext);

    private pointcut startMarkIntervalsAsBadTimeCommand(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, JobExecution.ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.MarkIntervalsAsBadTimeCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext): startMarkIntervalsAsBadTimeCommand(deviceProtocol, executionContext) {
        this.logStartOfMarkIntervalsAsBadTimeCommand(executionContext);
    }

    protected abstract void logStartOfMarkIntervalsAsBadTimeCommand (JobExecution.ExecutionContext executionContext);

    private pointcut startCreateMeterEventsFromStatusBits(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, JobExecution.ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.CreateMeterEventsFromStatusFlagsCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext): startCreateMeterEventsFromStatusBits(deviceProtocol, executionContext) {
        this.logStartOfCreateMeterEventsFromStatusBits(executionContext);
    }

    protected abstract void logStartOfCreateMeterEventsFromStatusBits (JobExecution.ExecutionContext executionContext);

    private pointcut startReadLoadProfileCommand(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, JobExecution.ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.ReadLoadProfileDataCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext): startReadLoadProfileCommand(deviceProtocol, executionContext) {
        this.logStartOfReadLoadProfileCommand(executionContext);
    }

    protected abstract void logStartOfReadLoadProfileCommand (JobExecution.ExecutionContext executionContext);

    private pointcut startVerifyLoadProfileCommand(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, JobExecution.ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.VerifyLoadProfilesCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext): startVerifyLoadProfileCommand(deviceProtocol, executionContext) {
        this.logStartOfVerifyLoadProfileCommand(executionContext);
    }

    protected abstract void logStartOfVerifyLoadProfileCommand (JobExecution.ExecutionContext executionContext);

    private pointcut readLoadProfileConfigurationsFromDevice(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext, List<CollectedLoadProfileConfiguration> loadProfileConfigurations):
            execution(private void com.energyict.mdc.engine.impl.commands.store.deviceactions.VerifyLoadProfilesCommandImpl.setLoadProfileConfigurations(List<CollectedLoadProfileConfiguration>))
         && args(loadProfileConfigurations)
         && cflowbelow(startVerifyLoadProfileCommand(deviceProtocol, executionContext));

    before (DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext, List<CollectedLoadProfileConfiguration> loadProfileConfigurations): readLoadProfileConfigurationsFromDevice(deviceProtocol, executionContext, loadProfileConfigurations){
        this.logReadLoadProfileConfigurationsFromDevice(executionContext, loadProfileConfigurations);
    }

    protected abstract void logReadLoadProfileConfigurationsFromDevice (JobExecution.ExecutionContext executionContext, List<CollectedLoadProfileConfiguration> loadProfileConfigurations);

    private Level getCommunicationLogLevel (JobExecution.ExecutionContext executionContext) {
        return this.getCommunicationLogLevel(executionContext.getComPort());
    }

    private Level getCommunicationLogLevel (ComPort comPort) {
        return this.getCommunicationLogLevel(comPort.getComServer());
    }

    private Level getCommunicationLogLevel (ComServer comServer) {
        switch (comServer.getCommunicationLogLevel()) {
            case ERROR:
                return Level.SEVERE;
            case WARN:
                return Level.WARNING;
            case INFO:
                return Level.INFO;
            case DEBUG:
                return Level.FINE;
            case TRACE:
                return Level.FINEST;
        }
        return Level.ALL;
    }

}