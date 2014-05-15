package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.engine.exceptions.CodingException;
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
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel;
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

    /**
     * The LoadProfilesTask which is used for modeling the actions
     */
    private final LoadProfilesTask loadProfilesTask;

    /**
     * The used OfflineDevice which contains relevant information for this ComCommand
     */
    private final OfflineDevice device;

    /**
     * Mapping between the LoadProfileReader LoadProfileReaders which are used to collect LoadProfileData from the devices and the
     * BaseLoadProfile from EIServer
     */
    private Map<LoadProfileReader, OfflineLoadProfile> loadProfileReaderMap = new HashMap<>();

    /**
     * The command that will be used to verify the LoadProfile configuration (if necessary)
     */
    private VerifyLoadProfilesCommand verifyLoadProfilesCommand;

    /**
     * The used TimeDifferenceCommand
     */
    private TimeDifferenceCommand timeDifferenceCommand;

    /**
     * The used ReadLoadProfileDataCommandImpl
     */
    private ReadLoadProfileDataCommand readLoadProfileDataCommand;

    /**
     * The used MarkIntervalsAsBadTimeCommandImpl
     */
    private MarkIntervalsAsBadTimeCommand markIntervalsAsBadTimeCommand;

    /**
     * The used CreateMeterEventsFromStatusFlagsCommand
     */
    private CreateMeterEventsFromStatusFlagsCommand createMeterEventsFromStatusFlagsCommand;

    public LoadProfileCommandImpl(final LoadProfilesTask loadProfilesTask, final OfflineDevice device, final CommandRoot commandRoot, ComTaskExecution comTaskExecution) {
        super(commandRoot);
        if (loadProfilesTask == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "loadProfilesTask");
        }
        if (device == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "device");
        }
        if (commandRoot == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "commandRoot");
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

        createLoadProfileReaders();
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

    /**
     * @return the ComCommandTypes ComCommandType of this command
     */
    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.LOAD_PROFILE_COMMAND;
    }

    /**
     * Create LoadProfileReaders for this LoadProfileCommand, based on the LoadProfileTypes specified in the {@link #loadProfilesTask}.
     * If no types are specified, then a LoadProfileReader for all
     * of the BaseLoadProfiles of the device will be created.
     */
    protected void createLoadProfileReaders() {
        createLoadProfileReadersForLoadProfilesTask(this.loadProfilesTask);
    }

    private void createLoadProfileReadersForLoadProfilesTask(LoadProfilesTask localLoadProfilesTask) {
        List<OfflineLoadProfile> listOfAllLoadProfiles = this.device.getAllOfflineLoadProfiles();
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
     * Add the given BaseLoadProfile to the {@link #loadProfileReaderMap readerMap}
     *
     * @param loadProfile the loadProfile to add
     */
    protected void addLoadProfileToReaderList(final OfflineLoadProfile loadProfile) {
        LoadProfileReader loadProfileReader =
                new LoadProfileReader(
                        loadProfile.getObisCode(),
                        loadProfile.getLastReading(),
                        null,
                        loadProfile.getLoadProfileId(),
                        loadProfile.getMasterSerialNumber(),
                        createChannelInfos(loadProfile));
        this.loadProfileReaderMap.put(loadProfileReader, loadProfile);
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
                                lpChannel.getMasterSerialNumber()));
            }
        }
        return channelInfos;
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
     * Get the configured interval of the BaseLoadProfile
     * corresponding with the given LoadProfileReader
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
     * Remove all given LoadProfileReader loadProfileReaders from the {@link #loadProfileReaderMap}
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
    public LoadProfilesTask getLoadProfilesTask() {
        return loadProfilesTask;
    }

    @Override
    public OfflineDevice getOfflineDevice() {
        return device;
    }

    @Override
    public void updateLoadProfileReaders(LoadProfilesTask loadProfilesTask) {
        this.createLoadProfileReadersForLoadProfilesTask(loadProfilesTask);
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
    public List<Issue<?>> getIssues() {
        List<Issue<?>> issues = super.getIssues();
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