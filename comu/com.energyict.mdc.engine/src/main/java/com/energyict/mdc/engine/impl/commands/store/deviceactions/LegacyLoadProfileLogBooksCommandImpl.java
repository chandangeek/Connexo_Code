package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.interval.Temporals;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CreateMeterEventsFromStatusFlagsCommand;
import com.energyict.mdc.engine.impl.commands.collect.LegacyLoadProfileLogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfilesTaskOptions;
import com.energyict.mdc.engine.impl.commands.collect.MarkIntervalsAsBadTimeCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLegacyLoadProfileLogBooksDataCommand;
import com.energyict.mdc.engine.impl.commands.collect.TimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.collect.VerifyLoadProfilesCommand;
import com.energyict.mdc.engine.impl.commands.store.core.CompositeComCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sva
 * @since 14/12/12 - 16:06
 */
public class LegacyLoadProfileLogBooksCommandImpl extends CompositeComCommandImpl implements LegacyLoadProfileLogBooksCommand {

    private final LoadProfilesTaskOptions loadProfilesTaskOptions;

    /**
     * The used {@link OfflineDevice} which contains relevant information for this ComCommand
     */
    private final OfflineDevice device;

    private List<LogBookReader> logBookReaders = new ArrayList<>();

    /**
     * Mapping between the {@link LoadProfileReader LoadProfileReaders} which are used to collect LoadProfileData from the devices and the
     * {@link com.energyict.mdc.upl.meterdata.LoadProfile} from EIServer
     */
    private Map<LoadProfileReader, OfflineLoadProfile> loadProfileReaderMap = new HashMap<>();

    /**
     * The command that will be used to verify the LoadProfile configuration (if necessary)
     */
    private VerifyLoadProfilesCommand verifyLoadProfilesCommand;

    /**
     * The used {@link TimeDifferenceCommand}
     */
    private TimeDifferenceCommand timeDifferenceCommand;

    /**
     * The used {@link ReadLoadProfileDataCommandImpl}
     */
    private ReadLegacyLoadProfileLogBooksDataCommand readLegacyLoadProfileLogBooksDataCommand;

    /**
     * The used {@link MarkIntervalsAsBadTimeCommandImpl}
     */
    private MarkIntervalsAsBadTimeCommand markIntervalsAsBadTimeCommand;
    /**
     * The used {@link CreateMeterEventsFromStatusFlagsCommand}
     */
    private CreateMeterEventsFromStatusFlagsCommand createMeterEventsFromStatusFlagsCommand;

    public LegacyLoadProfileLogBooksCommandImpl(GroupedDeviceCommand groupedDeviceCommand, LoadProfilesTask loadProfilesTask, LogBooksTask logBooksTask, ComTaskExecution comTaskExecution) {
        super(groupedDeviceCommand);
        if (groupedDeviceCommand == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "groupedDeviceCommand", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if ((loadProfilesTask == null) && (logBooksTask == null)) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "loadProfilesTask, logBooksTask", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (groupedDeviceCommand.getOfflineDevice() == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "device", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        this.loadProfilesTaskOptions = loadProfilesTask != null ? new LoadProfilesTaskOptions(loadProfilesTask) : new LoadProfilesTaskOptions();
        this.device = groupedDeviceCommand.getOfflineDevice();

        if (loadProfilesTask != null) {
            /**
             * It is important that the VerifyLoadProfilesCommand is first added to the command list.
             * The Execute method will chronologically execute all the commands so this one should be first
             */
            this.verifyLoadProfilesCommand = getGroupedDeviceCommand().getVerifyLoadProfileCommand(this, comTaskExecution);

            this.readLegacyLoadProfileLogBooksDataCommand = getGroupedDeviceCommand().getReadLegacyLoadProfileLogBooksDataCommand(this, comTaskExecution);

            if (this.loadProfilesTaskOptions.isMarkIntervalsAsBadTime()) {
                this.timeDifferenceCommand = getGroupedDeviceCommand().getTimeDifferenceCommand(this, comTaskExecution);
                this.markIntervalsAsBadTimeCommand = getGroupedDeviceCommand().getMarkIntervalsAsBadTimeCommand(this, comTaskExecution);
            }

            if (this.loadProfilesTaskOptions.isCreateMeterEventsFromStatusFlags()) {
                this.createMeterEventsFromStatusFlagsCommand = getGroupedDeviceCommand().getCreateMeterEventsFromStatusFlagsCommand(this, comTaskExecution);
            }
            LoadProfileCommandHelper.createLoadProfileReaders(getCommandRoot().getServiceProvider(), loadProfileReaderMap, loadProfilesTask, device, comTaskExecution);
        }

        if (logBooksTask != null) {
            if (readLegacyLoadProfileLogBooksDataCommand == null) {
                this.readLegacyLoadProfileLogBooksDataCommand = getGroupedDeviceCommand().getReadLegacyLoadProfileLogBooksDataCommand(this, comTaskExecution);
            }
            this.logBookReaders.addAll(LogBookCommandHelper.createLogBookReaders(getCommandRoot().getServiceProvider(), logBooksTask, device, comTaskExecution));
        }
    }

    @Override
    public String getDescriptionTitle() {
        return "Executed load profile and logbook protocol tasks";
    }

    /**
     * @return the ComCommandTypes of this command
     */
    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND;
    }

    @Override
    public List<LogBookReader> getLogBookReaders() {
        return logBookReaders;
    }

    /**
     * Get a list of all the {@link LoadProfileReader LoadProfileReaders} for this Command
     *
     * @return the requested list
     */
    @Override
    public List<LoadProfileReader> getLoadProfileReaders() {
        return new ArrayList<>(getLoadProfileReaderMap().keySet());
    }

    /**
     * Get the configured interval of the {@link com.energyict.mdc.upl.meterdata.LoadProfile}
     * corresponding with the given {@link LoadProfileReader}
     *
     * @param loadProfileReader the given LoadProfileReader
     * @return the requested interval in seconds
     */
    @Override
    public int findLoadProfileIntervalForLoadProfileReader(final LoadProfileReader loadProfileReader) {
        if (getLoadProfileReaderMap().containsKey(loadProfileReader)) {
            return Temporals.toTimeDuration(getLoadProfileReaderMap().get(loadProfileReader).interval()).getSeconds();
        }
        return LoadProfileCommand.INVALID_LOAD_PROFILE_INTERVAL;
    }

    /**
     * Remove all given {@link LoadProfileReader loadProfileReaders} from the {@link #loadProfileReaderMap}
     *
     * @param readersToRemove the list of {@link LoadProfileReader loadProfileReaders} to remove
     */
    @Override
    public void removeIncorrectLoadProfileReaders(final List<LoadProfileReader> readersToRemove) {
        for (LoadProfileReader loadProfileReader : readersToRemove) {
            getLoadProfileReaderMap().remove(loadProfileReader);
        }
    }

    protected Map<LoadProfileReader, OfflineLoadProfile> getLoadProfileReaderMap() {
        return loadProfileReaderMap;
    }

    @Override
    public OfflineDevice getOfflineDevice() {
        return device;
    }

    @Override
    public void updateAccordingTo(LoadProfilesTask loadProfilesTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        if (loadProfilesTask != null) {
            this.loadProfilesTaskOptions.setFailIfLoadProfileConfigurationMisMatch(this.loadProfilesTaskOptions.isFailIfLoadProfileConfigurationMisMatch() | loadProfilesTask.failIfLoadProfileConfigurationMisMatch());
            if (loadProfilesTask.isMarkIntervalsAsBadTime()) {
                if (!this.loadProfilesTaskOptions.isMarkIntervalsAsBadTime()) {
                    this.loadProfilesTaskOptions.setMarkIntervalsAsBadTime(true);
                    this.loadProfilesTaskOptions.setMinClockDiffBeforeBadTime(loadProfilesTask.getMinClockDiffBeforeBadTime());
                    this.timeDifferenceCommand = getGroupedDeviceCommand().getTimeDifferenceCommand(this, comTaskExecution);
                    this.markIntervalsAsBadTimeCommand = getGroupedDeviceCommand().getMarkIntervalsAsBadTimeCommand(this, comTaskExecution);
                } else {
                    if (this.loadProfilesTaskOptions.getMinClockDiffBeforeBadTime().orElse(new TimeDuration(0)).getMilliSeconds() > loadProfilesTask.getMinClockDiffBeforeBadTime().orElse(new TimeDuration(0)).getMilliSeconds()) {
                        this.loadProfilesTaskOptions.setMinClockDiffBeforeBadTime(loadProfilesTask.getMinClockDiffBeforeBadTime()); // Set the most strict of the 2 timing
                    }
                }
            }

            if (!this.loadProfilesTaskOptions.isCreateMeterEventsFromStatusFlags() && loadProfilesTask.createMeterEventsFromStatusFlags()) {
                this.loadProfilesTaskOptions.setCreateMeterEventsFromStatusFlags(true);
                this.createMeterEventsFromStatusFlagsCommand = getGroupedDeviceCommand().getCreateMeterEventsFromStatusFlagsCommand(this, comTaskExecution);
            }

            LoadProfileCommandHelper.createLoadProfileReaders(getCommandRoot().getServiceProvider(), loadProfileReaderMap, loadProfilesTask, device, comTaskExecution);
        }
    }

    @Override
    public void updateAccordingTo(LogBooksTask logBooksTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
        List<LogBookReader> newLogBookReaders = LogBookCommandHelper.createLogBookReaders(getCommandRoot().getServiceProvider(), logBooksTask, this.device, comTaskExecution);
        newLogBookReaders.stream().filter(newLogBookReader -> LogBookCommandHelper.canWeAddIt(this.logBookReaders, newLogBookReader)).forEach(newLogBookReader -> {
            this.logBookReaders.add(newLogBookReader);
        });
    }

    @Override
    public VerifyLoadProfilesCommand getVerifyLoadProfilesCommand() {
        return verifyLoadProfilesCommand;
    }

    @Override
    public TimeDifferenceCommand getTimeDifferenceCommand() {
        return timeDifferenceCommand;
    }

    @Override
    public LoadProfilesTaskOptions getLoadProfilesTaskOptions() {
        return loadProfilesTaskOptions;
    }

    public ReadLegacyLoadProfileLogBooksDataCommand getReadLegacyLoadProfileLogBooksDataCommand() {
        return readLegacyLoadProfileLogBooksDataCommand;
    }

    @Override
    public MarkIntervalsAsBadTimeCommand getMarkIntervalsAsBadTimeCommand() {
        return markIntervalsAsBadTimeCommand;
    }

    @Override
    public CreateMeterEventsFromStatusFlagsCommand getCreateMeterEventsFromStatusFlagsCommand() {
        return createMeterEventsFromStatusFlagsCommand;
    }

    @Override
    public List<CollectedData> getCollectedData() {
        List<CollectedData> collectedData = super.getCollectedData();

        boolean failIfLoadProfileConfigurationMisMatch = loadProfilesTaskOptions.isFailIfLoadProfileConfigurationMisMatch();
        if (!failIfLoadProfileConfigurationMisMatch) {
            for (CollectedData data : collectedData) {
                if (data instanceof CollectedLoadProfile) {
                    //((CollectedLoadProfile) data).setAllowIncompleteLoadProfileData(true);  //TODO port EISERVERSG-4162
                }
            }
        }

        return collectedData;
    }
}