package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.LoadProfileConfigurationException;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.LogBookFactory;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.tasks.support.DeviceLoadProfileSupport;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.exception.MessageSeed;
import org.joda.time.DateTimeConstants;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter between a {@link MeterProtocol} and {@link DeviceLoadProfileSupport}.
 * We use a {@link MeterProtocolClockAdapter} for the timeHandling of the interface.
 *
 * @author gna
 * @since 4/04/12 - 15:57
 */
public class MeterProtocolLoadProfileAdapter implements DeviceLoadProfileSupport {

    /**
     * The used <code>MeterProtocol</code> for which the adapter is working
     */
    private final MeterProtocol meterProtocol;
    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;
    private final MeteringService meteringService;

    /**
     * The used <code>MeterProtocolClockAdapter</code> for the time handling of the {@link CollectedLoadProfile} interface
     */
    private final MeterProtocolClockAdapter meterProtocolClockAdapter;

    public MeterProtocolLoadProfileAdapter(final MeterProtocol meterProtocol, IssueService issueService, CollectedDataFactory collectedDataFactory, MeteringService meteringService) {
        this.meterProtocol = meterProtocol;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
        this.meteringService = meteringService;
        this.meterProtocolClockAdapter = new MeterProtocolClockAdapter(meterProtocol);
    }

    /**
     * <b>Note:</b> This method is only called by the Collection Software if the option to "fail if channel configuration mismatch" is
     * checked on the <code>CommunicationProfile</code>
     * <p/>
     * We will only check 1 <code>LoadProfile</code>({@link com.energyict.mdc.protocol.api.tasks.support.DeviceLoadProfileSupport#GENERIC_LOAD_PROFILE_OBISCODE}) as standard {@link MeterProtocol}s only have support for 1 <code>LoadProfile</code>.
     * <b>We will only be able to verify the number of channels. The interval and channelUnits will not be validated until the actual data is fetched!</b>
     * <i>(even then it is only done by the protocol implementer)</i>
     * <p/>
     * If a <code>LoadProfile</code> is not supported (meaning the {@link LoadProfileReader#getProfileObisCode() LoadProfileObisCode} is not
     * equal to {@link com.energyict.mdc.protocol.api.tasks.support.DeviceLoadProfileSupport#GENERIC_LOAD_PROFILE_OBISCODE}), the corresponding boolean in the <code>DeviceLoadProfileConfiguration</code> must
     * be set to false.
     *
     * @param loadProfilesToRead the <CODE>List</CODE> of <CODE>LoadProfileReaders</CODE> to indicate which profiles will be read
     * @return a list of <CODE>DeviceLoadProfileConfiguration</CODE> objects corresponding with the meter
     */
    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(final List<LoadProfileReader> loadProfilesToRead) {
        if (loadProfilesToRead != null) {
            CollectedDataFactory collectedDataFactory = this.collectedDataFactory;
            List<CollectedLoadProfileConfiguration> loadProfileConfigurations = new ArrayList<>(loadProfilesToRead.size());
            CollectedLoadProfileConfiguration loadProfileConfiguration;
            for (LoadProfileReader loadProfileReader : loadProfilesToRead) {
                if (GENERIC_LOAD_PROFILE_OBISCODE.equalsIgnoreBChannel(loadProfileReader.getProfileObisCode())) {
                    loadProfileConfiguration = collectedDataFactory.createCollectedLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), loadProfileReader.getDeviceIdentifier());
                    loadProfileConfiguration.setChannelInfos(getDefaultChannelInfo(loadProfileReader));
                    // the B-field will be used as marker for the interval in minutes
                    loadProfileConfiguration.setProfileInterval(loadProfileReader.getProfileObisCode().getB() * DateTimeConstants.SECONDS_PER_MINUTE);
                } else { // if it is not the standard ObisCode, then we indicate the LoadProfile is not supported
                    loadProfileConfiguration = collectedDataFactory.createCollectedLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), loadProfileReader.getDeviceIdentifier(), false);
                }
                loadProfileConfigurations.add(loadProfileConfiguration);
            }
            return loadProfileConfigurations;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        throw DeviceProtocolAdapterCodingExceptions.unsupportedMethod(MessageSeeds.UNSUPPORTED_METHOD, this.getClass(), "getLoadProfileData");
    }

    /**
     * Create a list of <code>ChannelInfos</code> based on the requested number of channels we got from the meter.
     * All {@link Unit}s will be set to {@link Unit#getUndefined()} because no validation can be done.
     *
     * @return the created list of <code>ChannelInfos</code>
     * @param loadProfileReader
     */
    private List<ChannelInfo> getDefaultChannelInfo(LoadProfileReader loadProfileReader) {
        try {
            int numberOfChannels = this.meterProtocol.getNumberOfChannels();
            List<ChannelInfo> listOfChannelInfo = new ArrayList<ChannelInfo>();
            for (int i = 0; i < getMaxChannels(loadProfileReader, numberOfChannels); i++) {
                ChannelInfo configuredChannelInfo = loadProfileReader.getChannelInfos().get(i);
                listOfChannelInfo.add(new ChannelInfo(i, new ObisCode(GENERIC_CHANNEL_OBISCODE, i + 1).toString(), configuredChannelInfo.getUnit(), configuredChannelInfo.getMeterIdentifier(), configuredChannelInfo.getReadingType()));  //NON-NLS
            }
            return listOfChannelInfo;
        } catch (IOException e) {
            if (isTimeout(e)) {
                throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
            } else {
                throw new DeviceConfigurationException(MessageSeeds.CONFIG_NOT_ACCESSIBLE, GENERIC_LOAD_PROFILE_OBISCODE);
            }
        }
    }

    private int getMaxChannels(LoadProfileReader loadProfileReader, int numberOfChannels) {
        return numberOfChannels <= loadProfileReader.getChannelInfos().size()?numberOfChannels:loadProfileReader.getChannelInfos().size();
    }

    private boolean isTimeout(IOException e) {
        return (e.getMessage() != null) && e.getMessage().toLowerCase().contains("timeout");
    }

    public List<CollectedData> getLoadProfileLogBookData(final List<LoadProfileReader> loadProfiles, final List<LogBookReader> logBookReaders) {
        LogBookReader logBookReader = getValidLogBook(logBookReaders);
        boolean logBookReadNotRead = true;
        List<CollectedData> collectedData = new ArrayList<>();
        if (loadProfiles != null) {
            for (LoadProfileReader loadProfileReader : loadProfiles) {
                if (GENERIC_LOAD_PROFILE_OBISCODE.equalsIgnoreBChannel(loadProfileReader.getProfileObisCode())) {
                    if (logBookReader != null) {
                        logBookReadNotRead = false;
                        collectedData.addAll(getSingleProfileData(loadProfileReader, logBookReader));
                    } else {
                        collectedData.addAll(getSingleProfileData(loadProfileReader));
                    }
                } else {
                    collectedData.add(createUnSupportedCollectedLoadProfile(loadProfileReader));
                }
            }
        }

        if ((logBookReader != null) && logBookReadNotRead) {
            collectedData.addAll(getLogBookData(logBookReader));
        }
        collectedData.addAll(createUnSupportedCollectedLogBooksForInvalidLogBookReaders(logBookReaders));
        return collectedData;
    }

    private LogBookReader getValidLogBook(List<LogBookReader> logBookReaders) {
        LogBookReader validLogBook = null;
        for (LogBookReader logBookReader : logBookReaders) {
            if (logBookReader.getLogBookObisCode().equals(LogBookFactory.GENERIC_LOGBOOK_TYPE_OBISCODE)) {
                validLogBook = logBookReader;
            }
        }
        return validLogBook;
    }

    /**
     * Create a simple not-supported {@link CollectedLoadProfile} object
     *
     * @param loadProfileReader the reader which triggered the collection
     * @return a {@link CollectedLoadProfile} with proper failure information
     */
    private CollectedLoadProfile createUnSupportedCollectedLoadProfile(final LoadProfileReader loadProfileReader) {
        CollectedLoadProfile deviceLoadProfile = this.collectedDataFactory.createCollectedLoadProfile(null);
        deviceLoadProfile.setFailureInformation(ResultType.NotSupported, getProblem(loadProfileReader.getProfileObisCode(), com.energyict.mdc.protocol.api.MessageSeeds.LOADPROFILE_NOT_SUPPORTED, loadProfileReader.getProfileObisCode()));
        return deviceLoadProfile;
    }

    private List<CollectedLogBook> createUnSupportedCollectedLogBooksForInvalidLogBookReaders(final List<LogBookReader> logBookReaders) {
        CollectedDataFactory collectedDataFactory = this.collectedDataFactory;
        List<CollectedLogBook> collectedLogBookList = new ArrayList<>();
        for (LogBookReader reader : logBookReaders) {
            if (!reader.getLogBookObisCode().equals(LogBookFactory.GENERIC_LOGBOOK_TYPE_OBISCODE)) {
                CollectedLogBook deviceLogBook = collectedDataFactory.createCollectedLogBook(reader.getLogBookIdentifier());
                deviceLogBook.setFailureInformation(ResultType.NotSupported, getWarning(reader.getLogBookObisCode(), com.energyict.mdc.protocol.api.MessageSeeds.LOGBOOK_NOT_SUPPORTED, reader.getLogBookObisCode()));
                collectedLogBookList.add(deviceLogBook);
            }
        }
        return collectedLogBookList;
    }

    /**
     * Collect a single <code>ProfileData</code> object from the Device.
     * The returned object will contain one of the below possibilities:
     * <ul>
     * <li>Correct {@link com.energyict.mdc.protocol.api.device.data.IntervalData IntervalData} and {@link ChannelInfo}</li>
     * <li>A Not supported resultType if we could not read the data</li>
     * <li>An incompatible resultType if we could not correctly parse the received data</li>
     * </ul>
     *
     * @param loadProfileReader@return a CollectedLoadProfile
     */
    private List<CollectedData> getSingleProfileData(LoadProfileReader loadProfileReader) {
        List<CollectedData> collectedDataList = new ArrayList<>(1);

        CollectedLoadProfile deviceLoadProfile =
                this.collectedDataFactory.createCollectedLoadProfile(
                        loadProfileReader.getLoadProfileIdentifier());
        try {
            ProfileData profileData = this.meterProtocol.getProfileData(Date.from(loadProfileReader.getStartReadingTime()), false);
            deviceLoadProfile.setCollectedData(profileData.getIntervalDatas(), convertToProperChannelInfos(profileData, loadProfileReader.getChannelInfos(), deviceLoadProfile));
            deviceLoadProfile.setDoStoreOlderValues(profileData.shouldStoreOlderValues());
        } catch (IOException e) {
            if (isTimeout(e)) {
                throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
            } else {
                deviceLoadProfile.setFailureInformation(ResultType.NotSupported, getProblem(loadProfileReader.getProfileObisCode(), com.energyict.mdc.protocol.api.MessageSeeds.COULD_NOT_READOUT_LOADPROFILE_DATA, e.getMessage()));
            }
        } catch (IndexOutOfBoundsException e) { // handles parsing errors
            deviceLoadProfile.setFailureInformation(ResultType.InCompatible, getProblem(loadProfileReader.getProfileObisCode(), com.energyict.mdc.protocol.api.MessageSeeds.COULD_NOT_PARSE_LOADPROFILE_DATA));
        }
        collectedDataList.add(deviceLoadProfile);
        return collectedDataList;
    }

    private List<ChannelInfo> convertToProperChannelInfos(ProfileData profileData, List<ChannelInfo> configuredChannelInfos, CollectedLoadProfile collectedLoadProfile) {
        List<ChannelInfo> convertedChannelInfos = new ArrayList<>();
        for (ChannelInfo ci : profileData.getChannelInfos()) {
            ObisCode channelObisCode = new ObisCode(GENERIC_CHANNEL_OBISCODE, ci.getChannelId() + 1);
            try {
                ChannelInfo configuredChannelInfo = getChannelInfoFromConfiguredChannels(channelObisCode, configuredChannelInfos);
                convertedChannelInfos.add(new ChannelInfo(configuredChannelInfo.getId(), channelObisCode.toString(), ci.getUnit(), configuredChannelInfo.getMeterIdentifier(), configuredChannelInfo.getReadingType()));
            } catch (LoadProfileConfigurationException e) {
                collectedLoadProfile.setFailureInformation(ResultType.ConfigurationMisMatch, getWarning(profileData, com.energyict.mdc.protocol.api.MessageSeeds.UNSUPPORTED_CHANNEL_INFO, ci));
            }
        }
        return convertedChannelInfos;
    }

    protected ChannelInfo getChannelInfoFromConfiguredChannels(ObisCode obisCode, List<ChannelInfo> configuredChannelInfos) throws LoadProfileConfigurationException {
        return configuredChannelInfos.stream().filter(channelInfo -> channelInfo.getChannelObisCode().equals(obisCode)).findFirst().orElseThrow(() -> new LoadProfileConfigurationException("Could not found a correct ChannelInfo with the obiscode " + obisCode));
    }

    private List<CollectedData> getSingleProfileData(LoadProfileReader loadProfileReader, final LogBookReader logBookReader) {
        List<CollectedData> collectedDataList = new ArrayList<>(2);
        Instant combinedLastReadingTime = loadProfileReader.getStartReadingTime().isBefore(logBookReader.getLastLogBook()) ? loadProfileReader.getStartReadingTime() : logBookReader.getLastLogBook();

        CollectedDataFactory collectedDataFactory = this.collectedDataFactory;
        CollectedLoadProfile deviceLoadProfile =
                collectedDataFactory.createCollectedLoadProfile(
                        loadProfileReader.getLoadProfileIdentifier());
        CollectedLogBook deviceLogBook = collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
        try {
            ProfileData profileData = this.meterProtocol.getProfileData(Date.from(combinedLastReadingTime), true);
            deviceLoadProfile.setCollectedData(getIntervalDatas(profileData.getIntervalDatas(), Date.from(loadProfileReader.getStartReadingTime())), convertToProperChannelInfos(profileData, loadProfileReader.getChannelInfos(), deviceLoadProfile));
            deviceLoadProfile.setDoStoreOlderValues(profileData.shouldStoreOlderValues());
            deviceLogBook.setMeterEvents(MeterEvent.mapMeterEventsToMeterProtocolEvents(profileData.getMeterEvents(), this.meteringService));
        } catch (IOException e) {
            if (isTimeout(e)) {
                throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
            } else {
                deviceLoadProfile.setFailureInformation(ResultType.NotSupported, getProblem(loadProfileReader.getProfileObisCode(), com.energyict.mdc.protocol.api.MessageSeeds.COULD_NOT_READOUT_LOADPROFILE_DATA));
                deviceLogBook.setFailureInformation(ResultType.NotSupported, getProblem(logBookReader.getLogBookObisCode(), com.energyict.mdc.protocol.api.MessageSeeds.COULD_NOT_READOUT_LOGBOOK_DATA));
            }
        } catch (IndexOutOfBoundsException e) { // handles parsing errors
            deviceLoadProfile.setFailureInformation(ResultType.InCompatible, getProblem(loadProfileReader.getProfileObisCode(), com.energyict.mdc.protocol.api.MessageSeeds.COULD_NOT_PARSE_LOADPROFILE_DATA));
            deviceLogBook.setFailureInformation(ResultType.InCompatible, getProblem(logBookReader.getLogBookObisCode(), com.energyict.mdc.protocol.api.MessageSeeds.COULD_NOT_PARSE_LOGBOOK_DATA));
        }
        collectedDataList.add(deviceLoadProfile);
        collectedDataList.add(deviceLogBook);
        return collectedDataList;
    }

    protected List<CollectedData> getLogBookData(final LogBookReader logBookReader) {
        List<CollectedData> collectedDataList = new ArrayList<>(1);
        CollectedLogBook deviceLogBook = this.collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
        try {
            ProfileData profileData = this.meterProtocol.getProfileData(Date.from(logBookReader.getLastLogBook()), true);
            deviceLogBook.setMeterEvents(MeterEvent.mapMeterEventsToMeterProtocolEvents(profileData.getMeterEvents(), this.meteringService));
        } catch (IOException e) {
            if (isTimeout(e)) {
                throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
            } else {
                deviceLogBook.setFailureInformation(ResultType.NotSupported, getProblem(logBookReader.getLogBookObisCode(), com.energyict.mdc.protocol.api.MessageSeeds.COULD_NOT_READOUT_LOGBOOK_DATA));
            }
        } catch (IndexOutOfBoundsException e) { // handles parsing errors
            deviceLogBook.setFailureInformation(ResultType.InCompatible, getProblem(logBookReader.getLogBookObisCode(), com.energyict.mdc.protocol.api.MessageSeeds.COULD_NOT_PARSE_LOGBOOK_DATA));
        }
        collectedDataList.add(deviceLogBook);
        return collectedDataList;
    }

    /**
     * Filter the list of IntervalData objects based on the startReadingTime of the profile.
     * All intervals before this startReadingTime should be ignored.
     *
     * @param allIntervalDatas
     * @param startReadingTime the startReadingTime of the profile
     * @return
     */
    private List<IntervalData> getIntervalDatas(List<IntervalData> allIntervalDatas, Date startReadingTime) {
        return allIntervalDatas
                .stream()
                .filter(each -> each.getEndTime().after(startReadingTime))
                .collect(Collectors.toList());
    }

    /**
     * @return the actual time of the Device
     */
    @Override
    public Date getTime() {
        return this.meterProtocolClockAdapter.getTime();
    }

    private Issue getProblem(Object source, MessageSeed messageSeed, Object... arguments) {
        return this.issueService.newProblem(source, messageSeed.getKey(), arguments);
    }

    private Issue getWarning(Object source, MessageSeed messageSeed, Object... arguments) {
        return this.issueService.newWarning(source, messageSeed.getKey(), arguments);
    }

}