package com.energyict.mdc.engine.impl.core.aspects.logging;

import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootImpl;
import com.energyict.mdc.engine.impl.core.CommandCreator;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
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

    protected final ComCommandLogger getCommandLogger (ExecutionContext executionContext) {
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
    protected abstract ComCommandLogger getActualLogger (Logger logger, ExecutionContext executionContext);

    private pointcut executeCommandRoot (CommandRootImpl commandRoot, ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, ExecutionContext))
         && target(commandRoot)
         && args(.., executionContext);

    before (CommandRootImpl commandRoot, ExecutionContext executionContext): executeCommandRoot(commandRoot, executionContext) {
        this.startActualLogger(executionContext);
    }

    private synchronized void startActualLogger (ExecutionContext executionContext) {
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

    private pointcut startBasicCheckCommand(DeviceProtocol deviceProtocol, ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.BasicCheckCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, ExecutionContext executionContext): startBasicCheckCommand(deviceProtocol, executionContext) {
        this.logStartOfBasicCheckCommand(executionContext);
    }

    protected abstract void logStartOfBasicCheckCommand (ExecutionContext executionContext);

    private pointcut startVerifySerialNumberCommand(DeviceProtocol deviceProtocol, ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.VerifySerialNumberCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, ExecutionContext executionContext): startVerifySerialNumberCommand(deviceProtocol, executionContext) {
        this.logStartOfVerifySerialNumberCommand(executionContext);
    }

    protected abstract void logStartOfVerifySerialNumberCommand (ExecutionContext executionContext);

    private pointcut serialNumberMisMatch(DeviceProtocol deviceProtocol, ExecutionContext executionContext, String meterSerialNumber, String configuredSerialNumber):
            call(* com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException.serialNumberMisMatch(java.lang.String, java.lang.String, com.elster.jupiter.util.exception.MessageSeed))
         && args(meterSerialNumber, configuredSerialNumber)
         && cflowbelow(startVerifySerialNumberCommand(deviceProtocol, executionContext));

    before(DeviceProtocol deviceProtocol, ExecutionContext executionContext, String meterSerialNumber, String configuredSerialNumber): serialNumberMisMatch(deviceProtocol, executionContext, meterSerialNumber, configuredSerialNumber){
        this.logSerialNumberMisMatch(executionContext, meterSerialNumber, configuredSerialNumber);
    }

    protected abstract void logSerialNumberMisMatch (ExecutionContext executionContext, String meterSerialNumber, String configuredSerialNumber);

    private pointcut startClockCommand(DeviceProtocol deviceProtocol, ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.ClockCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, ExecutionContext executionContext): startClockCommand(deviceProtocol, executionContext) {
        this.logStartOfClockCommand(executionContext);
    }

    protected abstract void logStartOfClockCommand (ExecutionContext executionContext);

    /*
    Used a call instead of an execution because otherwise ALL protocols need to be woven for this logging...
     */
    private pointcut startedSetTime(DeviceProtocol deviceProtocol, ExecutionContext executionContext, Date timeToSet):
            call(void com.energyict.mdc.protocol.api.tasks.support.DeviceClockSupport+.setTime(Date))
         //&& target(deviceProtocol)
         && args(timeToSet)
         && cflowbelow(startClockCommand(deviceProtocol, executionContext));

    before(DeviceProtocol deviceProtocol, ExecutionContext executionContext, Date timeToSet): startedSetTime(deviceProtocol, executionContext, timeToSet) {
        this.logStartOfSetTime(executionContext, timeToSet);
    }

    protected abstract void logStartOfSetTime (ExecutionContext executionContext, Date timeToSet);

    private pointcut startedSetClockCommand(DeviceProtocol deviceProtocol, ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.deviceactions.SetClockCommandImpl.execute(DeviceProtocol, ExecutionContext))
         && args(deviceProtocol, executionContext);

    private pointcut timeDiffAboveMaximum(long timeDifference):
            execution(private boolean com.energyict.mdc.engine.impl.commands.store.deviceactions.SetClockCommandImpl.aboveMaximum(long))
         && args(timeDifference);

    private pointcut timeDiffBelowMinimum(long timeDifference):
            execution(private boolean com.energyict.mdc.engine.impl.commands.store.deviceactions.SetClockCommandImpl.belowMinimum(long))
         && args(timeDifference);

    after(DeviceProtocol deviceProtocol, ExecutionContext executionContext, long timeDifference) returning (boolean success):
        cflow(startedSetClockCommand(deviceProtocol, executionContext))
     && timeDiffAboveMaximum(timeDifference) {
        if (success) {
            this.logTimeDifferenceAboveMaximum(executionContext, timeDifference);
        }
    }

    protected abstract void logTimeDifferenceAboveMaximum (ExecutionContext executionContext, long timeDifference);

    after(DeviceProtocol deviceProtocol, ExecutionContext executionContext, long timeDifference) returning (boolean success):
        cflow(startedSetClockCommand(deviceProtocol, executionContext))
     && timeDiffBelowMinimum(timeDifference) {
        if (success) {
            this.logTimeDifferenceBelowMinimum(executionContext, timeDifference);
        }
    }

    protected abstract void logTimeDifferenceBelowMinimum (ExecutionContext executionContext, long timeDifference);

    private pointcut startedSynchronizeClockCommand(DeviceProtocol deviceProtocol, ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.SynchronizeClockCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, ExecutionContext executionContext): startedSynchronizeClockCommand(deviceProtocol, executionContext) {
        this.logStartOfSynchronizeClockCommand(executionContext);
    }

    protected abstract void logStartOfSynchronizeClockCommand (ExecutionContext executionContext);

    private pointcut getTimeShift(long timeDifference):
            execution(private long com.energyict.mdc.engine.impl.commands.store.deviceactions.SynchronizeClockCommandImpl.getTimeShift(long))
                    && args(timeDifference);

    after(DeviceProtocol deviceProtocol, ExecutionContext executionContext, long timeDifference) returning (long timeShift):
        cflow(startedSynchronizeClockCommand(deviceProtocol, executionContext))
     && getTimeShift(timeDifference) {
        if (timeShift == 0) {
            this.logTimeDifferenceBelowMinimum(executionContext, timeDifference);
        } else {
            this.logSynchronizeClockWithTimeShift(executionContext, timeShift);
        }
    }

    protected abstract void logSynchronizeClockWithTimeShift (ExecutionContext executionContext, long timeShift);

    private pointcut startTimeDifferenceCommand(DeviceProtocol deviceProtocol, ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.TimeDifferenceCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, ExecutionContext executionContext): startTimeDifferenceCommand(deviceProtocol, executionContext) {
        this.logStartOfTimeDifferenceCommand(executionContext);
    }

    protected abstract void logStartOfTimeDifferenceCommand (ExecutionContext executionContext);

    private pointcut startVerifyTimeDifferenceCommand(DeviceProtocol deviceProtocol, ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.VerifyTimeDifferenceCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, ExecutionContext executionContext): startVerifyTimeDifferenceCommand(deviceProtocol, executionContext) {
        this.logStartOfVerifyTimeDifferenceCommand(executionContext);
    }

    protected abstract void logStartOfVerifyTimeDifferenceCommand (ExecutionContext executionContext);

    private pointcut timeDifferenceExceeded(DeviceProtocol deviceProtocol, ExecutionContext executionContext, long actualTimeDifference, long maximumTimeDifference):
        call(* com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException.timeDifferenceExceeded(com.elster.jupiter.util.exception.MessageSeed, long, long))
            && args(actualTimeDifference, maximumTimeDifference)
            && cflowbelow(startVerifyTimeDifferenceCommand(deviceProtocol, executionContext));

    before(DeviceProtocol deviceProtocol, ExecutionContext executionContext, long actualTimeDifference, long maximumTimeDifference): timeDifferenceExceeded(deviceProtocol, executionContext, actualTimeDifference, maximumTimeDifference){
        this.logTimeDifferenceExceeded(executionContext, actualTimeDifference, maximumTimeDifference);
    }

    protected abstract void logTimeDifferenceExceeded (ExecutionContext executionContext, long actualTimeDifference, long maximumTimeDifference);

    private pointcut startedForceClockCommand(DeviceProtocol deviceProtocol, ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.ForceClockCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, ExecutionContext executionContext): startedForceClockCommand(deviceProtocol, executionContext) {
        this.logStartOfForceClockCommand(executionContext);
    }

    protected abstract void logStartOfForceClockCommand (ExecutionContext executionContext);

    private pointcut startLoadProfileCommand(DeviceProtocol deviceProtocol, ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.LoadProfileCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, ExecutionContext executionContext): startLoadProfileCommand(deviceProtocol, executionContext) {
        this.logStartOfLoadProfileCommand(executionContext);
    }

    protected abstract void logStartOfLoadProfileCommand (ExecutionContext executionContext);

    private pointcut startMarkIntervalsAsBadTimeCommand(DeviceProtocol deviceProtocol, ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.MarkIntervalsAsBadTimeCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, ExecutionContext executionContext): startMarkIntervalsAsBadTimeCommand(deviceProtocol, executionContext) {
        this.logStartOfMarkIntervalsAsBadTimeCommand(executionContext);
    }

    protected abstract void logStartOfMarkIntervalsAsBadTimeCommand (ExecutionContext executionContext);

    private pointcut startCreateMeterEventsFromStatusBits(DeviceProtocol deviceProtocol, ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.CreateMeterEventsFromStatusFlagsCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, ExecutionContext executionContext): startCreateMeterEventsFromStatusBits(deviceProtocol, executionContext) {
        this.logStartOfCreateMeterEventsFromStatusBits(executionContext);
    }

    protected abstract void logStartOfCreateMeterEventsFromStatusBits (ExecutionContext executionContext);

    private pointcut startReadLoadProfileCommand(DeviceProtocol deviceProtocol, ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.ReadLoadProfileDataCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, ExecutionContext executionContext): startReadLoadProfileCommand(deviceProtocol, executionContext) {
        this.logStartOfReadLoadProfileCommand(executionContext);
    }

    protected abstract void logStartOfReadLoadProfileCommand (ExecutionContext executionContext);

    private pointcut startVerifyLoadProfileCommand(DeviceProtocol deviceProtocol, ExecutionContext executionContext):
            execution(void com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand.execute(DeviceProtocol, ExecutionContext))
         && target(com.energyict.mdc.engine.impl.commands.store.deviceactions.VerifyLoadProfilesCommandImpl)
         && args(deviceProtocol, executionContext);

    before(DeviceProtocol deviceProtocol, ExecutionContext executionContext): startVerifyLoadProfileCommand(deviceProtocol, executionContext) {
        this.logStartOfVerifyLoadProfileCommand(executionContext);
    }

    protected abstract void logStartOfVerifyLoadProfileCommand (ExecutionContext executionContext);

    private pointcut readLoadProfileConfigurationsFromDevice(DeviceProtocol deviceProtocol, ExecutionContext executionContext, List<CollectedLoadProfileConfiguration> loadProfileConfigurations):
            execution(private void com.energyict.mdc.engine.impl.commands.store.deviceactions.VerifyLoadProfilesCommandImpl.setLoadProfileConfigurations(List<CollectedLoadProfileConfiguration>))
         && args(loadProfileConfigurations)
         && cflowbelow(startVerifyLoadProfileCommand(deviceProtocol, executionContext));

    before (DeviceProtocol deviceProtocol, ExecutionContext executionContext, List<CollectedLoadProfileConfiguration> loadProfileConfigurations): readLoadProfileConfigurationsFromDevice(deviceProtocol, executionContext, loadProfileConfigurations){
        this.logReadLoadProfileConfigurationsFromDevice(executionContext, loadProfileConfigurations);
    }

    protected abstract void logReadLoadProfileConfigurationsFromDevice (ExecutionContext executionContext, List<CollectedLoadProfileConfiguration> loadProfileConfigurations);

    private Level getCommunicationLogLevel (ExecutionContext executionContext) {
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