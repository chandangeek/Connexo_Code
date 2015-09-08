package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.CreateMeterEventsFromStatusFlagsCommand;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.MarkIntervalsAsBadTimeCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLoadProfileDataCommand;
import com.energyict.mdc.engine.impl.commands.collect.RegularLoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.TimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.collect.VerifyLoadProfilesCommand;
import com.energyict.mdc.engine.impl.commands.store.core.CompositeComCommandImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel;
import com.energyict.mdc.tasks.LoadProfilesTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the RegularLoadProfileCommand interface.
 *
 * @author gna
 * @since 9/05/12 - 16:06
 */
public class LoadProfileCommandImpl extends CompositeComCommandImpl implements RegularLoadProfileCommand {

    /**
     * The LoadProfilesTask which is used for modeling the actions.
     */
    private final LoadProfilesTask loadProfilesTask;

    /**
     * The used OfflineDevice which contains relevant information for this ComCommand.
     */
    private final OfflineDevice device;

    /**
     * Summary of loadProfileReaders.
     */
    private List<LoadProfileReader> loadProfileReaders = new ArrayList<>();

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

    public LoadProfileCommandImpl(final LoadProfilesTask loadProfilesTask, final OfflineDevice device, final CommandRoot commandRoot, ComTaskExecution comTaskExecution) {
        super(commandRoot);
        if (loadProfilesTask == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "loadProfilesTask", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (device == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "device", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (commandRoot == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "commandRoot", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        this.loadProfilesTask = loadProfilesTask;
        this.device = device;

        /**
         * It is important that the VerifyLoadProfilesCommand is first added to the command list.
         * The Execute method will chronologically execute all the commands so this one should be first
         */

        this.verifyLoadProfilesCommand = getCommandRoot().getVerifyLoadProfileCommand(this, comTaskExecution);

        this.readLoadProfileDataCommand = getCommandRoot().getReadLoadProfileDataCommand(this, comTaskExecution);

        if (this.loadProfilesTask.isMarkIntervalsAsBadTime()) {
            this.timeDifferenceCommand = getCommandRoot().getTimeDifferenceCommand(this, comTaskExecution);
            this.markIntervalsAsBadTimeCommand = getCommandRoot().getMarkIntervalsAsBadTimeCommand(this, comTaskExecution);
        }

        if (this.loadProfilesTask.createMeterEventsFromStatusFlags()) {
            this.createMeterEventsFromStatusFlagsCommand = getCommandRoot().getCreateMeterEventsFromStatusFlagsCommand(this, comTaskExecution);
        }

        createLoadProfileReaders(comTaskExecution.getDevice().getmRID());
    }

    @Override
    public String getDescriptionTitle() {
        return "Executed load profile protocol task";
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (!loadProfilesTask.getLoadProfileTypes().isEmpty()) {
            PropertyDescriptionBuilder loadProfileObisCodesBuilder = builder.addListProperty("loadProfileObisCodes");
            this.appendLoadProfileObisCodes(loadProfileObisCodesBuilder);
            builder.addProperty("markAsBadTime").append(this.loadProfilesTask.isMarkIntervalsAsBadTime());
            builder.addProperty("createEventsFromStatusFlag").append(this.loadProfilesTask.createMeterEventsFromStatusFlags());
        }
    }

    private void appendLoadProfileObisCodes (PropertyDescriptionBuilder builder) {
        for (LoadProfileType loadProfileType : this.loadProfilesTask.getLoadProfileTypes()) {
            builder = builder.append(loadProfileType.getObisCode()).next();
        }
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.LOAD_PROFILE_COMMAND;
    }

    /**
     * Creates LoadProfileReaders for this LoadProfileCommand, based on the LoadProfileTypes specified in the {@link #loadProfilesTask}.
     * If no types are specified, then a LoadProfileReader for all
     * of the BaseLoadProfiles of the device will be created.
     */
    protected void createLoadProfileReaders(String deviceMrid) {
        createLoadProfileReadersForLoadProfilesTask(this.loadProfilesTask, deviceMrid);
    }

    private void createLoadProfileReadersForLoadProfilesTask(LoadProfilesTask localLoadProfilesTask, String deviceMrid) {
        List<OfflineLoadProfile> listOfAllLoadProfiles = this.device.getAllOfflineLoadProfilesForMRID(deviceMrid);
        if (localLoadProfilesTask.getLoadProfileTypes().isEmpty()) {
            for (OfflineLoadProfile loadProfile : listOfAllLoadProfiles) {
                addLoadProfileToReaderList(loadProfile);
            }
        } else {  // Read out the specified load profile types
            for (LoadProfileType lpt : localLoadProfilesTask.getLoadProfileTypes()) {
                for (OfflineLoadProfile loadProfile : listOfAllLoadProfiles) {
                    if (lpt.getId() == loadProfile.getLoadProfileTypeId()) {
                        addLoadProfileToReaderList(loadProfile);
                    }
                }
            }
        }
    }

    /**
     * Adds the given BaseLoadProfile to the {@link #loadProfileReaders readerMap}.
     *
     * @param offlineLoadProfile the loadProfile to add
     */
    protected void addLoadProfileToReaderList(final OfflineLoadProfile offlineLoadProfile) {
        LoadProfileReader loadProfileReader =
                new LoadProfileReader(
                        this.getClock(),
                        offlineLoadProfile.getObisCode(),
                        offlineLoadProfile.getLastReading().orElse(null),
                        null,
                        offlineLoadProfile.getLoadProfileId(),
                        offlineLoadProfile.getDeviceIdentifier(),
                        createChannelInfos(offlineLoadProfile), offlineLoadProfile.getMasterSerialNumber(),
                        offlineLoadProfile.getLoadProfileIdentifier());
        this.loadProfileReaders.add(loadProfileReader);
    }

    /**
     * Create a <CODE>List</CODE> of <CODE>ChannelInfos</CODE> for the given <CODE>LoadProfile</CODE>.
     * If the channel has the BaseChannel#isStoreData() boolean checked, then we can add it.
     * If it is not checked then it is not required for the protocol read the channel.
     *
     * @param offlineLoadProfile the given <CODE>LoadProfile</CODE>
     * @return the new List
     */
    protected List<ChannelInfo> createChannelInfos(final OfflineLoadProfile offlineLoadProfile) {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        for (OfflineLoadProfileChannel lpChannel : offlineLoadProfile.getAllChannels()) {
            if (lpChannel.isStoreData()) {
                channelInfos.add(
                        new ChannelInfo(
                                channelInfos.size(),
                                lpChannel.getObisCode().toString(),
                                lpChannel.getUnit(),
                                lpChannel.getMasterSerialNumber(),
                                lpChannel.getReadingType()));
            }
        }
        return channelInfos;
    }

    /**
     * Gets the configured interval of the BaseLoadProfile
     * corresponding with the given LoadProfileReader.
     *
     * @param loadProfileReader the given LoadProfileReader
     * @return the requested interval in seconds
     */
    public int findLoadProfileIntervalForLoadProfileReader(final LoadProfileReader loadProfileReader) {
        if (getLoadProfileReaders().contains(loadProfileReader)) {
            return this.device.getAllOfflineLoadProfiles().stream()
                    .filter(offlineLoadProfile -> offlineLoadProfile.getObisCode().equals(loadProfileReader.getProfileObisCode()))
                    .findFirst().get().getInterval().getSeconds();
        }
        return LoadProfileCommand.INVALID_LOAD_PROFILE_INTERVAL;
    }

    /**
     * Removes all given LoadProfileReader loadProfileReaders from the {@link #loadProfileReaders}.
     *
     * @param readersToRemove the list of LoadProfileReader loadProfileReaders to remove
     */
    public void removeIncorrectLoadProfileReaders(final List<LoadProfileReader> readersToRemove) {
        for (LoadProfileReader loadProfileReader : readersToRemove) {
            getLoadProfileReaders().remove(loadProfileReader);
        }
    }

    public List<LoadProfileReader> getLoadProfileReaders() {
        return loadProfileReaders;
    }

    @Override
    public LoadProfilesTask getLoadProfilesTask() {
        return loadProfilesTask;
    }

    @Override
    public OfflineDevice getOfflineDevice() {
        return device;
    }

    @Override
    public void updateLoadProfileReaders(LoadProfilesTask loadProfilesTask, String deviceMrid) {
        this.createLoadProfileReadersForLoadProfilesTask(loadProfilesTask, deviceMrid);
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

    @Override
    public List<Issue> getIssues() {
        List<Issue> issues = super.getIssues();
        if(getVerifyLoadProfilesCommand() != null) {
            issues.addAll(getVerifyLoadProfilesCommand().getIssues());
        }
        if(getReadLoadProfileDataCommand() != null) {
            issues.addAll(getReadLoadProfileDataCommand().getIssues());
        }
        if(getCreateMeterEventsFromStatusFlagsCommand() != null) {
            issues.addAll(getCreateMeterEventsFromStatusFlagsCommand().getIssues());
        }
        if(getMarkIntervalsAsBadTimeCommand() != null ) {
            issues.addAll(getMarkIntervalsAsBadTimeCommand().getIssues());
        }
        return issues;
    }

}