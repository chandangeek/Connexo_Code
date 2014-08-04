package com.energyict.mdc.engine.impl.core.aspects.logging;

import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.CompositeComCommand;
import com.energyict.mdc.engine.impl.core.CommandCreator;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines pointcuts and advice that will do logging for the
 * {@link ComCommand ComCommands}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 27/08/12
 * Time: 10:39
 */
public aspect ComCommandLogging extends AbstractComCommandLogging {

    protected ComCommandLogger getAnonymousLogger () {
        Logger logger = Logger.getAnonymousLogger();
        logger.setLevel(Level.FINEST);
        return LoggerFactory.getLoggerFor(ComCommandLogger.class, logger);
    }

    protected ComCommandLogger getActualLogger (Logger logger, ExecutionContext executionContext) {
        logger.addHandler(new ComCommandMessageJournalist(ServiceProvider.instance.get().clock(), executionContext.getCurrentTaskExecutionBuilder()));
        return LoggerFactory.getLoggerFor(ComCommandLogger.class, logger);
    }

    @Override
    protected void logCreatedCommands (CommandCreator commandCreator, CommandRoot commandRoot) {
        logCommands(commandRoot.getExecutionContext(), commandRoot.getCommands());
    }

    /**
     * Log the creation of all the commands.
     *
     * @param executionContext The ExecutionContext that will provide access to the ComCommandLogger
     * @param commands the created commands
     */
    private void logCommands (ExecutionContext executionContext, Map<ComCommandTypes, ComCommand> commands) {
        for (ComCommand command : commands.values()) {
            if (command instanceof CompositeComCommand) {
                this.logCommands(executionContext, ((CompositeComCommand) command).getCommands());
            }
            else {
                this.getCommandLogger(executionContext).createdComCommand(command.getClass().getSimpleName());
            }
        }
    }

    @Override
    protected void logStartOfBasicCheckCommand (ExecutionContext executionContext) {
        this.getCommandLogger(executionContext).startedBasicCheckCommand();
    }

    @Override
    protected void logStartOfVerifySerialNumberCommand (ExecutionContext executionContext) {
        this.getCommandLogger(executionContext).startedToVerifySerialNumber();
    }

    @Override
    protected void logSerialNumberMisMatch (ExecutionContext executionContext, String meterSerialNumber, String configuredSerialNumber) {
        this.getCommandLogger(executionContext).serialNumberMisMatch(meterSerialNumber, configuredSerialNumber);
    }

    @Override
    protected void logStartOfClockCommand (ExecutionContext executionContext) {
        this.getCommandLogger(executionContext).startedClockCommand();
    }

    @Override
    protected void logStartOfSetTime (ExecutionContext executionContext, Date timeToSet) {
        if (timeToSet != null) {
            this.getCommandLogger(executionContext).startedSetClock(timeToSet.toString());
        }
    }

    @Override
    protected void logTimeDifferenceAboveMaximum (ExecutionContext executionContext, long timeDifference) {
        this.getCommandLogger(executionContext).timeDifferenceAboveMaximum(timeDifference);
    }

    @Override
    protected void logTimeDifferenceBelowMinimum (ExecutionContext executionContext, long timeDifference) {
        this.getCommandLogger(executionContext).timeDifferenceBelowMinimum(timeDifference);
    }

    @Override
    protected void logStartOfSynchronizeClockCommand (ExecutionContext executionContext) {
        this.getCommandLogger(executionContext).startedSynchronizeClockCommand();
    }

    @Override
    protected void logSynchronizeClockWithTimeShift (ExecutionContext executionContext, long timeShift) {
        this.getCommandLogger(executionContext).synchronizeClockWithTimeShift(timeShift);
    }

    @Override
    protected void logStartOfTimeDifferenceCommand (ExecutionContext executionContext) {
        this.getCommandLogger(executionContext).startedTimeDifferenceCommand();
    }

    @Override
    protected void logStartOfVerifyTimeDifferenceCommand (ExecutionContext executionContext) {
        this.getCommandLogger(executionContext).startedVerifyTimeDifferenceCommand();
    }

    @Override
    protected void logTimeDifferenceExceeded (ExecutionContext executionContext, long actualTimeDifference, long maximumTimeDifference) {
        this.getCommandLogger(executionContext).timeDifferenceExceeded(actualTimeDifference, maximumTimeDifference);
    }

    @Override
    protected void logStartOfForceClockCommand (ExecutionContext executionContext) {
        this.getCommandLogger(executionContext).startedForceClockCommand();
    }

    @Override
    protected void logStartOfLoadProfileCommand (ExecutionContext executionContext) {
        this.getCommandLogger(executionContext).startedLoadProfileCommand();
    }

    @Override
    protected void logStartOfMarkIntervalsAsBadTimeCommand (ExecutionContext executionContext) {
        this.getCommandLogger(executionContext).startedMarkIntervalsAsBadTimeCommand();
    }

    @Override
    protected void logStartOfCreateMeterEventsFromStatusBits (ExecutionContext executionContext) {
        this.getCommandLogger(executionContext).startedCreateMeterEventsFromStatusBits();
    }

    @Override
    protected void logStartOfReadLoadProfileCommand (ExecutionContext executionContext) {
        this.getCommandLogger(executionContext).startedReadLoadProfileCommand();
    }

    @Override
    protected void logStartOfVerifyLoadProfileCommand (ExecutionContext executionContext) {
        this.getCommandLogger(executionContext).startedToVerifyLoadProfile();
    }

    @Override
    protected void logReadLoadProfileConfigurationsFromDevice (ExecutionContext executionContext, List<CollectedLoadProfileConfiguration> loadProfileConfigurations) {
        StringBuilder loadProfileConfigs = createReadableLoadProfileConfigs(loadProfileConfigurations);
        this.getCommandLogger(executionContext).fetchedLoadProfileConfigurationsFromDevice(loadProfileConfigs.toString());
    }

    private StringBuilder createReadableLoadProfileConfigs(List<CollectedLoadProfileConfiguration> loadProfileConfigurations) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Supported:");
        strBuilder.append("\t");
        strBuilder.append("ObisCode:");
        strBuilder.append("\t");
        strBuilder.append("SerialNumber:");
        for (CollectedLoadProfileConfiguration loadProfileConfiguration : loadProfileConfigurations) {
            strBuilder.append("\r\n");
            strBuilder.append(loadProfileConfigurationToString(loadProfileConfiguration));
        }
        return strBuilder;
    }

    private String loadProfileConfigurationToString(CollectedLoadProfileConfiguration loadProfileConfiguration) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(this.toString(loadProfileConfiguration.isSupportedByMeter()));
        strBuilder.append("\t");
        strBuilder.append(loadProfileConfiguration.getObisCode());
        strBuilder.append("\t");
        strBuilder.append(loadProfileConfiguration.getDeviceIdentifier());
        if (loadProfileConfiguration.getChannelInfos() != null) {
            for (ChannelInfo channelInfo : loadProfileConfiguration.getChannelInfos()) {
                strBuilder.append("\r\n");
                strBuilder.append("\t\t");
                strBuilder.append(channelInfo.getName());
                strBuilder.append("\t");
                strBuilder.append(channelInfo.getUnit());
                strBuilder.append("\t");
                strBuilder.append(channelInfo.getMeterIdentifier());
            }
        }
        return strBuilder.toString();
    }

    private String toString (boolean flag) {
        if (flag) {
            return "Yes";
        }
        else {
            return "No";
        }
    }

}