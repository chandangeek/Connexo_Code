package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.CreateMeterEventsFromStatusFlagsCommand;
import com.energyict.mdc.engine.impl.commands.collect.LegacyLoadProfileLogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.MarkIntervalsAsBadTimeCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLegacyLoadProfileLogBooksDataCommand;
import com.energyict.mdc.engine.impl.commands.collect.TimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.collect.VerifyLoadProfilesCommand;
import com.energyict.mdc.engine.impl.commands.store.core.CompositeComCommandImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineLoadProfileChannel;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBook;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.LogBooksTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sva
 * @since 14/12/12 - 16:06
 */
public class LegacyLoadProfileLogBooksCommandImpl extends CompositeComCommandImpl implements LegacyLoadProfileLogBooksCommand {

    /**
     * The {@link LoadProfilesTask} which is used for modeling the actions
     */
    private final LoadProfilesTask loadProfilesTask;

    /**
     * The {@link LogBooksTask} which is used for modeling the actions
     */
    private final LogBooksTask logBooksTask;

    /**
     * The used {@link OfflineDevice} which contains relevant information for this ComCommand
     */
    private final OfflineDevice device;

    private List<LogBookReader> logBookReaders = new ArrayList<>();

    /**
     * Mapping between the {@link LoadProfileReader LoadProfileReaders} which are used to collect LoadProfileData from the devices and the
     * {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile} from EIServer
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

    public LegacyLoadProfileLogBooksCommandImpl(LoadProfilesTask loadProfilesTask, LogBooksTask logBooksTask, OfflineDevice device, CommandRoot commandRoot, ComTaskExecution comTaskExecution) {
        super(commandRoot);
        if ((loadProfilesTask == null) && (logBooksTask == null)) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "loadProfilesTask, logBooksTask", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (device == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "device", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        if (commandRoot == null) {
            throw CodingException.methodArgumentCanNotBeNull(getClass(), "constructor", "commandRoot", MessageSeeds.METHOD_ARGUMENT_CAN_NOT_BE_NULL);
        }
        this.loadProfilesTask = loadProfilesTask;
        this.logBooksTask = logBooksTask;
        this.device = device;

        if (this.loadProfilesTask != null) {
            /**
             * It is important that the VerifyLoadProfilesCommand is first added to the command list.
             * The Execute method will chronologically execute all the commands so this one should be first
             */
            this.verifyLoadProfilesCommand = getCommandRoot().getVerifyLoadProfileCommand(this, comTaskExecution);

            this.readLegacyLoadProfileLogBooksDataCommand = getCommandRoot().getReadLegacyLoadProfileLogBooksDataCommand(this, comTaskExecution);

            if (this.loadProfilesTask.isMarkIntervalsAsBadTime()) {
                this.timeDifferenceCommand = getCommandRoot().getTimeDifferenceCommand(this, comTaskExecution);
                this.markIntervalsAsBadTimeCommand = getCommandRoot().getMarkIntervalsAsBadTimeCommand(this, comTaskExecution);
            }

            if (this.loadProfilesTask.createMeterEventsFromStatusFlags()) {
                this.createMeterEventsFromStatusFlagsCommand = getCommandRoot().getCreateMeterEventsFromStatusFlagsCommand(this, comTaskExecution);
            }
            createLoadProfileReaders(comTaskExecution.getDevice().getmRID());
        }

        if (this.logBooksTask != null) {
            /* Adding it a second time is oké, the root will check if it exists and return the existing one */
            this.readLegacyLoadProfileLogBooksDataCommand = getCommandRoot().getReadLegacyLoadProfileLogBooksDataCommand(this, comTaskExecution);
            createLogBookReaders(this.device, comTaskExecution.getDevice().getmRID());
        }
    }

    @Override
    public String getDescriptionTitle() {
        return "Executed load profile and logbook protocol tasks";
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (this.logBooksTask != null) {
            PropertyDescriptionBuilder logbookObisCodesBuilder = builder.addListProperty("logbookObisCodes");
            this.logBookObisCodes(logbookObisCodesBuilder);
            this.loadProfileObisCodes(builder);
        }
    }

    private void logBookObisCodes(PropertyDescriptionBuilder builder) {
        if (!logBookReaders.isEmpty()) {
            this.doLogBookObisCodes(builder);
        } else {
            builder.append("none");
        }
    }

    private void doLogBookObisCodes(PropertyDescriptionBuilder builder) {
        for (LogBookReader logBookReader : logBookReaders) {
            builder = builder.append(logBookReader.getLogBookObisCode()).next();
        }
    }

    private void loadProfileObisCodes(DescriptionBuilder builder) {
        if (loadProfilesTask != null &&
                !loadProfilesTask.getLoadProfileTypes().isEmpty()) {
            this.doLoadProfileObisCodes(builder.addListProperty("loadProfileObisCodes"));
            builder.addProperty("markAsBadTime").append(this.loadProfilesTask.isMarkIntervalsAsBadTime());
            builder.addProperty("createEventsFromStatusFlag").append(this.loadProfilesTask.createMeterEventsFromStatusFlags());
        } else {
            builder.addProperty("loadProfileObisCodes").append("none");
        }
    }

    private void doLoadProfileObisCodes(PropertyDescriptionBuilder builder) {
        for (LoadProfileType loadProfileType : loadProfilesTask.getLoadProfileTypes()) {
            builder = builder.append(loadProfileType.getObisCode());
        }
    }

    /**
     * @return the ComCommandTypes of this command
     */
    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND;
    }

    /**
     * Create LoadProfileReaders for this LoadProfileCommand, based on the {@link LoadProfileType}s specified in the {@link #loadProfilesTask}.
     * If no types are specified, then a {@link LoadProfileReader} for all
     * of the {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile}s of the device will be created.
     * @param deviceMrid
     */
    protected void createLoadProfileReaders(String deviceMrid) {
        createLoadProfileReadersForLoadProfilesTask(this.loadProfilesTask, deviceMrid);
    }

    private void createLoadProfileReadersForLoadProfilesTask(LoadProfilesTask localLoadProfilesTask, String deviceMrid) {
        List<OfflineLoadProfile> listOfAllLoadProfiles = this.device.getAllOfflineLoadProfilesForMRID(deviceMrid);
        if (localLoadProfilesTask.getLoadProfileTypes().isEmpty()) {
            listOfAllLoadProfiles.forEach(this::addLoadProfileToReaderList);
        } else {  // Read out the specified load profile types
            for (LoadProfileType lpt : localLoadProfilesTask.getLoadProfileTypes()) {
                listOfAllLoadProfiles
                        .stream()
                        .filter(loadProfile -> lpt.getId() == loadProfile.getLoadProfileTypeId())
                        .forEach(this::addLoadProfileToReaderList);
            }
        }
    }

    /**
     * Add the given {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile} to the {@link #loadProfileReaderMap readerMap}
     *
     * @param loadProfile the loadProfile to add
     */
    protected void addLoadProfileToReaderList(final OfflineLoadProfile loadProfile) {
        LoadProfileReader loadProfileReader =
                new LoadProfileReader(
                        this.getClock(),
                        loadProfile.getObisCode(),
                        loadProfile.getLastReading().orElse(null),
                        null,
                        loadProfile.getLoadProfileId(),
                        loadProfile.getDeviceIdentifier(),
                createChannelInfos(loadProfile), loadProfile.getMasterSerialNumber(), loadProfile.getLoadProfileIdentifier());
        this.loadProfileReaderMap.put(loadProfileReader, loadProfile);
    }

    /**
     * Create a <CODE>List</CODE> of <CODE>ChannelInfos</CODE> for the given <CODE>LoadProfile</CODE>.
     * If the channel has the {@link OfflineLoadProfileChannel#isStoreData()} boolean checked, then we can add it.
     * If it is not checked then it is not required for the protocol read the channel.
     *
     * @param offlineLoadProfile the given <CODE>LoadProfile</CODE>
     * @return the new List
     */
    protected List<ChannelInfo> createChannelInfos(final OfflineLoadProfile offlineLoadProfile) {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        for (OfflineLoadProfileChannel lpChannel : offlineLoadProfile.getAllChannels()) {
            if (lpChannel.isStoreData()) {
                channelInfos.add(new ChannelInfo(channelInfos.size(), lpChannel.getObisCode().toString(),
                        lpChannel.getUnit(), lpChannel.getMasterSerialNumber(),
                        lpChannel.getReadingType()));
            }
        }
        return channelInfos;
    }

    /**
     * Create {@link LogBookReader}q for this LogBooksCommand, based on the {@link LogBookType}s specified in the {@link #logBooksTask}.
     * If no types are specified, then a {@link LogBookReader} for all
     * of the {@link com.energyict.mdc.protocol.api.device.BaseLogBook}s of the device will be created.
     *
     * @param device the <i>Master</i> Device for which LoadProfileReaders should be created
     * @param deviceMrid
     */
    protected void createLogBookReaders(final OfflineDevice device, String deviceMrid) {
        List<OfflineLogBook> listOfAllLogBooks = device.getAllOfflineLogBooksForMRID(deviceMrid);
        if (this.logBooksTask.getLogBookTypes().isEmpty()) {
            listOfAllLogBooks.forEach(this::addLogBookToReaderList);
        } else {
            for (LogBookType logBookType : this.logBooksTask.getLogBookTypes()) {
                listOfAllLogBooks
                        .stream()
                        .filter(logBook -> logBookType.getId() == logBook.getLogBookTypeId())
                        .forEach(this::addLogBookToReaderList);
            }
        }
    }

    /**
     * Add the given {@link com.energyict.mdc.protocol.api.device.BaseLogBook} to the {@link #logBookReaders list}
     *
     * @param logBook the logBook to add
     */
    protected void addLogBookToReaderList(final OfflineLogBook logBook) {
        LogBookReader logBookReader = new LogBookReader(this.getClock(), logBook.getObisCode(), logBook.getLastLogBook(), logBook.getLogBookIdentifier(), logBook.getDeviceIdentifier());
        this.logBookReaders.add(logBookReader);
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
     * Get the configured interval of the {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile}
     * corresponding with the given {@link LoadProfileReader}
     *
     * @param loadProfileReader the given LoadProfileReader
     * @return the requested interval in seconds
     */
    @Override
    public int findLoadProfileIntervalForLoadProfileReader(final LoadProfileReader loadProfileReader) {
        if (getLoadProfileReaderMap().containsKey(loadProfileReader)) {
            return getLoadProfileReaderMap().get(loadProfileReader).getInterval().getSeconds();
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
    public LoadProfilesTask getLoadProfilesTask() {
        return loadProfilesTask;
    }

    @Override
    public OfflineDevice getOfflineDevice() {
        return device;
    }

    @Override
    public void updateLoadProfileReaders(LoadProfilesTask loadProfilesTask, String deviceMrid) {
        createLoadProfileReadersForLoadProfilesTask(loadProfilesTask, deviceMrid);
    }

    @Override
    public LogBooksTask getLogBooksTask() {
        return logBooksTask;
    }

    @Override
    public VerifyLoadProfilesCommand getVerifyLoadProfilesCommand() {
        return verifyLoadProfilesCommand;
    }

    @Override
    public TimeDifferenceCommand getTimeDifferenceCommand() {
        return timeDifferenceCommand;
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

}