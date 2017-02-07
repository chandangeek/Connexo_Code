/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CreateMeterEventsFromStatusFlagsCommand;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfilesTaskOptions;
import com.energyict.mdc.engine.impl.commands.collect.MarkIntervalsAsBadTimeCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLoadProfileDataCommand;
import com.energyict.mdc.engine.impl.commands.collect.RegularLoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.TimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.collect.VerifyLoadProfilesCommand;
import com.energyict.mdc.engine.impl.commands.store.core.CompositeComCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;
import com.energyict.mdc.tasks.LoadProfilesTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the RegularLoadProfileCommand interface.
 *
 * @author gna
 * @since 9/05/12 - 16:06
 */
public class LoadProfileCommandImpl extends CompositeComCommandImpl implements RegularLoadProfileCommand {

    private final LoadProfilesTaskOptions loadProfilesTaskOptions;

    /**
     * The used OfflineDevice which contains relevant information for this ComCommand.
     */
    private final OfflineDevice device;

    /**
     * Mapping between the {@link LoadProfileReader LoadProfileReaders} which are used to collect LoadProfileData from the devices and the
     * {@link LoadProfile} from EIServer
     */
    private Map<LoadProfileReader, OfflineLoadProfile> loadProfileReaderMap = new HashMap<>();

    /**
     * The command that will be used to verify the LoadProfile configuration (if necessary).
     */
    private VerifyLoadProfilesCommand verifyLoadProfilesCommand;

    /**
     * The used TimeDifferenceCommand.
     */
    private TimeDifferenceCommand timeDifferenceCommand;

    /**
     * The used ReadLoadProfileDataCommandImpl.
     */
    private ReadLoadProfileDataCommand readLoadProfileDataCommand;

    /**
     * The used MarkIntervalsAsBadTimeCommandImpl.
     */
    private MarkIntervalsAsBadTimeCommand markIntervalsAsBadTimeCommand;

    /**
     * The used CreateMeterEventsFromStatusFlagsCommand.
     */
    private CreateMeterEventsFromStatusFlagsCommand createMeterEventsFromStatusFlagsCommand;

    public LoadProfileCommandImpl(final GroupedDeviceCommand groupedDeviceCommand, final LoadProfilesTask loadProfilesTask, ComTaskExecution comTaskExecution) {
        super(groupedDeviceCommand);
        if (groupedDeviceCommand == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "commandRoot", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (loadProfilesTask == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "loadProfilesTask", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (groupedDeviceCommand.getOfflineDevice() == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "device", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        this.loadProfilesTaskOptions = new LoadProfilesTaskOptions(loadProfilesTask);
        this.device = groupedDeviceCommand.getOfflineDevice();

        /**
         * It is important that the VerifyLoadProfilesCommand is first added to the command list.
         * The Execute method will chronologically execute all the commands so this one should be first
         */

        this.verifyLoadProfilesCommand = getGroupedDeviceCommand().getVerifyLoadProfileCommand(this, comTaskExecution);

        this.readLoadProfileDataCommand = getGroupedDeviceCommand().getReadLoadProfileDataCommand(this, comTaskExecution);

        if (this.loadProfilesTaskOptions.isMarkIntervalsAsBadTime()) {
            this.timeDifferenceCommand = getGroupedDeviceCommand().getTimeDifferenceCommand(this, comTaskExecution);
            this.markIntervalsAsBadTimeCommand = getGroupedDeviceCommand().getMarkIntervalsAsBadTimeCommand(this, comTaskExecution);
        }

        if (this.loadProfilesTaskOptions.isCreateMeterEventsFromStatusFlags()) {
            this.createMeterEventsFromStatusFlagsCommand = getGroupedDeviceCommand().getCreateMeterEventsFromStatusFlagsCommand(this, comTaskExecution);
        }

        LoadProfileCommandHelper.createLoadProfileReaders(getCommandRoot().getServiceProvider(), loadProfileReaderMap, loadProfilesTask, device, comTaskExecution);
    }

    @Override
    public String getDescriptionTitle() {
        return "Executed load profile protocol task";
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.LOAD_PROFILE_COMMAND;
    }

    /**
     * Get a list of all the {@link LoadProfileReader LoadProfileReaders} for this Command
     *
     * @return the requested list
     */
    public List<LoadProfileReader> getLoadProfileReaders() {
        return new ArrayList<>(getLoadProfileReaderMap().keySet());
    }

    /**
     * Gets the configured interval of the BaseLoadProfile
     * corresponding with the given LoadProfileReader.
     *
     * @param loadProfileReader the given LoadProfileReader
     * @return the requested interval in seconds
     */
    public int findLoadProfileIntervalForLoadProfileReader(final LoadProfileReader loadProfileReader) {
        if (getLoadProfileReaderMap().containsKey(loadProfileReader)) {
            return getLoadProfileReaderMap().get(loadProfileReader).getInterval().getSeconds();
        }
        return LoadProfileCommand.INVALID_LOAD_PROFILE_INTERVAL;
    }

    /**
     * Removes all given LoadProfileReader loadProfileReaders
     *
     * @param readersToRemove the list of LoadProfileReader loadProfileReaders to remove
     */
    public void removeIncorrectLoadProfileReaders(final List<LoadProfileReader> readersToRemove) {
        for (LoadProfileReader loadProfileReader : readersToRemove) {
            getLoadProfileReaderMap().remove(loadProfileReader);
        }
    }

    protected Map<LoadProfileReader, OfflineLoadProfile> getLoadProfileReaderMap() {
        return loadProfileReaderMap;
    }

    @Override
    public LoadProfilesTaskOptions getLoadProfilesTaskOptions() {
        return loadProfilesTaskOptions;
    }

    @Override
    public OfflineDevice getOfflineDevice() {
        return device;
    }

    @Override
    //TODO: maybe split this list > keep a separate list containing for which of the loadProfiles markAsBasTime and/or createMeterEvents should be done?
    public void updateAccordingTo(LoadProfilesTask loadProfilesTask, GroupedDeviceCommand groupedDeviceCommand, ComTaskExecution comTaskExecution) {
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

    @Override
    public List<Issue> getIssues() {
        List<Issue> issues = super.getOwnIssuesIgnoringChildren();  // These are all issues present in the collected data
        for (ComCommand child : this) {
            addAllNewIssues(issues, child);
        }
        return issues;
    }

    private void addAllNewIssues(List<Issue> issues, ComCommand child) {
        for (Issue issue : child.getIssues()) {
            if (!issues.contains(issue)) {
                issues.add(issue);
            }
        }
    }

    @Override
    public List<CollectedData> getCollectedData() {
        List<CollectedData> collectedData = super.getCollectedData();
        collectedData.addAll(getVerifyLoadProfilesCommand().getCollectedData());
        collectedData.addAll(getReadLoadProfileDataCommand().getCollectedData());
        if (getTimeDifferenceCommand() != null) {
            collectedData.addAll(getTimeDifferenceCommand().getCollectedData());
        }
        if (getMarkIntervalsAsBadTimeCommand() != null) {
            collectedData.addAll(getMarkIntervalsAsBadTimeCommand().getCollectedData());
        }
        if (getCreateMeterEventsFromStatusFlagsCommand() != null) {
            collectedData.addAll(getCreateMeterEventsFromStatusFlagsCommand().getCollectedData());
        }

        boolean failIfLoadProfileConfigurationMisMatch = loadProfilesTaskOptions.isFailIfLoadProfileConfigurationMisMatch();
        if (!failIfLoadProfileConfigurationMisMatch) {
            for (CollectedData data : collectedData) {
                if (data instanceof CollectedLoadProfile) {
                    //((CollectedLoadProfile) data).setAllowIncompleteLoadProfileData(true); TODO port EISERVERSG-4162
                }
            }
        }

        return collectedData;
    }

    public VerifyLoadProfilesCommand getVerifyLoadProfilesCommand() {
        return verifyLoadProfilesCommand;
    }

    public TimeDifferenceCommand getTimeDifferenceCommand() {
        return timeDifferenceCommand;
    }

    public ReadLoadProfileDataCommand getReadLoadProfileDataCommand() {
        return readLoadProfileDataCommand;
    }

    public MarkIntervalsAsBadTimeCommand getMarkIntervalsAsBadTimeCommand() {
        return markIntervalsAsBadTimeCommand;
    }

    public CreateMeterEventsFromStatusFlagsCommand getCreateMeterEventsFromStatusFlagsCommand() {
        return createMeterEventsFromStatusFlagsCommand;
    }
}