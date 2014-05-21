package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.CollectedDataFactoryProvider;
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
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.identifiers.LoadProfileDataIdentifier;
import com.energyict.protocolimplv2.identifiers.SerialNumberDeviceIdentifier;
import org.joda.time.DateTimeConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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

    /**
     * The used <code>MeterProtocolClockAdapter</code> for the time handling of the {@link CollectedLoadProfile} interface
     */
    private final MeterProtocolClockAdapter meterProtocolClockAdapter;

    public MeterProtocolLoadProfileAdapter(final MeterProtocol meterProtocol, IssueService issueService) {
        this.meterProtocol = meterProtocol;
        this.issueService = issueService;
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
     *
     * @param loadProfilesToRead the <CODE>List</CODE> of <CODE>LoadProfileReaders</CODE> to indicate which profiles will be read
     * @return a list of <CODE>DeviceLoadProfileConfiguration</CODE> objects corresponding with the meter
     */
    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(final List<LoadProfileReader> loadProfilesToRead) {
        if (loadProfilesToRead != null) {
            CollectedDataFactory collectedDataFactory = this.getCollectedDataFactory();
            List<CollectedLoadProfileConfiguration> loadProfileConfigurations = new ArrayList<>(loadProfilesToRead.size());
            CollectedLoadProfileConfiguration loadProfileConfiguration;
            for (LoadProfileReader loadProfileReader : loadProfilesToRead) {
                if (GENERIC_LOAD_PROFILE_OBISCODE.equalsIgnoreBChannel(loadProfileReader.getProfileObisCode())) {
                    loadProfileConfiguration = collectedDataFactory.createCollectedLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), loadProfileReader.getMeterSerialNumber());
                    loadProfileConfiguration.setChannelInfos(getDefaultChannelInfo());
                    // the B-field will be used as marker for the interval in minutes
                    loadProfileConfiguration.setProfileInterval(loadProfileReader.getProfileObisCode().getB() * DateTimeConstants.SECONDS_PER_MINUTE);
                } else { // if it is not the standard ObisCode, then we indicate the LoadProfile is not supported
                    loadProfileConfiguration = collectedDataFactory.createCollectedLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), loadProfileReader.getMeterSerialNumber(), false);
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
        throw DeviceProtocolAdapterCodingExceptions.unsupportedMethod(this.getClass(), "getLoadProfileData");
    }

    /**
     * Create a list of <code>ChannelInfos</code> based on the requested number of channels we got from the meter.
     * All {@link Unit}s will be set to {@link Unit#getUndefined()} because no validation can be done.
     *
     * @return the created list of <code>ChannelInfos</code>
     */
    protected List<ChannelInfo> getDefaultChannelInfo() {
        try {
            int numberOfChannels = this.meterProtocol.getNumberOfChannels();
            List<ChannelInfo> listOfChannelInfo = new ArrayList<>();
            for (int i = 0; i < numberOfChannels; i++) {
                listOfChannelInfo.add(new ChannelInfo(i, new ObisCode(GENERIC_CHANNEL_OBISCODE, i+1).toString(), Unit.getUndefined()));  //NON-NLS
            }
            return listOfChannelInfo;
        } catch (IOException e) {
            throw DeviceConfigurationException.notAccessible(GENERIC_LOAD_PROFILE_OBISCODE);
        }
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
                        collectedData.addAll(getSingleProfileData(loadProfileReader.getProfileObisCode(), loadProfileReader.getStartReadingTime(), loadProfileReader.getMeterSerialNumber(), logBookReader));
                    } else {
                        collectedData.addAll(getSingleProfileData(loadProfileReader.getProfileObisCode(), loadProfileReader.getStartReadingTime(), loadProfileReader.getMeterSerialNumber()));
                    }
                } else {
                    collectedData.add(createUnSupportedCollectedLoadProfile(loadProfileReader));
                }
            }
        }

        if ((logBookReader != null)&& logBookReadNotRead) {
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
        CollectedLoadProfile deviceLoadProfile = this.getCollectedDataFactory().createCollectedLoadProfile(null);
        deviceLoadProfile.setFailureInformation(ResultType.NotSupported, getIssue(loadProfileReader.getProfileObisCode(), "loadProfileXnotsupported", loadProfileReader.getProfileObisCode()));
        return deviceLoadProfile;
    }

    private List<CollectedLogBook> createUnSupportedCollectedLogBooksForInvalidLogBookReaders(final List<LogBookReader> logBookReaders) {
        CollectedDataFactory collectedDataFactory = this.getCollectedDataFactory();
        List<CollectedLogBook> collectedLogBookList = new ArrayList<>();
        for (LogBookReader reader : logBookReaders) {
            if (!reader.getLogBookObisCode().equals(LogBookFactory.GENERIC_LOGBOOK_TYPE_OBISCODE)) {
                CollectedLogBook deviceLogBook = collectedDataFactory.createCollectedLogBook(reader.getLogBookIdentifier());
                deviceLogBook.setFailureInformation(ResultType.NotSupported, getIssue(reader.getLogBookObisCode(), "logBookXnotsupported", reader.getLogBookObisCode()));
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
     *
     * @param loadProfileObisCode  the ObisCode of the LoadProfile
     * @param startReadingTime the time of the last collected interval
     * @param meterSerialNumber the SerialNumber of the Device collecting the LoadProfile
     * @return a CollectedLoadProfile
     */
    protected List<CollectedData> getSingleProfileData(final ObisCode loadProfileObisCode, final Date startReadingTime, final String meterSerialNumber) {
        List<CollectedData> collectedDataList = new ArrayList<>(1);

        CollectedLoadProfile deviceLoadProfile =
                this.getCollectedDataFactory().createCollectedLoadProfile(
                        new LoadProfileDataIdentifier(
                                loadProfileObisCode,
                                new SerialNumberDeviceIdentifier(meterSerialNumber)));
        try {
            ProfileData profileData = this.meterProtocol.getProfileData(startReadingTime, false);
            deviceLoadProfile.setCollectedData(profileData.getIntervalDatas(), convertToProperChannelInfos(profileData));
            deviceLoadProfile.setDoStoreOlderValues(profileData.shouldStoreOlderValues());
        } catch (IOException e) {
            deviceLoadProfile.setFailureInformation(ResultType.NotSupported, getIssue(loadProfileObisCode, "CouldNotReadoutLoadProfileData", e.getMessage()));
        } catch (IndexOutOfBoundsException e) { // handles parsing errors
            deviceLoadProfile.setFailureInformation(ResultType.InCompatible, getIssue(loadProfileObisCode, "CouldNotParseLoadProfileData"));
        }
        collectedDataList.add(deviceLoadProfile);
        return collectedDataList;
    }

    private List<ChannelInfo> convertToProperChannelInfos(ProfileData profileData) {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        for (ChannelInfo ci : profileData.getChannelInfos()) {
            channelInfos.add(new ChannelInfo(ci.getId(), ci.getChannelId(), new ObisCode(GENERIC_CHANNEL_OBISCODE, (int) (ci.getChannelId()+1)).toString(), ci.getUnit()));
        }
        return channelInfos;
    }

    protected List<CollectedData> getSingleProfileData(final ObisCode loadProfileObisCode, final Date startReadingTime, final String meterSerialNumber, final LogBookReader logBookReader) {
        List<CollectedData> collectedDataList = new ArrayList<>(2);
        Date combinedLastReadingTime = startReadingTime.before(logBookReader.getLastLogBook()) ? startReadingTime : logBookReader.getLastLogBook();

        CollectedDataFactory collectedDataFactory = this.getCollectedDataFactory();
        CollectedLoadProfile deviceLoadProfile =
                collectedDataFactory.createCollectedLoadProfile(
                        new LoadProfileDataIdentifier(
                                loadProfileObisCode,
                                new SerialNumberDeviceIdentifier(meterSerialNumber)));
        CollectedLogBook deviceLogBook = collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
        try {
            ProfileData profileData = this.meterProtocol.getProfileData(combinedLastReadingTime, true);
            deviceLoadProfile.setCollectedData(getIntervalDatas(profileData.getIntervalDatas(), startReadingTime), convertToProperChannelInfos(profileData));
            deviceLoadProfile.setDoStoreOlderValues(profileData.shouldStoreOlderValues());
            deviceLogBook.setMeterEvents(MeterEvent.mapMeterEventsToMeterProtocolEvents(profileData.getMeterEvents()));
        } catch (IOException e) {
            deviceLoadProfile.setFailureInformation(ResultType.NotSupported, getIssue(loadProfileObisCode, "CouldNotReadoutLoadProfileData"));
            deviceLogBook.setFailureInformation(ResultType.NotSupported, getIssue(logBookReader.getLogBookObisCode(), "CouldNotReadoutLogBookData"));
        } catch (IndexOutOfBoundsException e) { // handles parsing errors
            deviceLoadProfile.setFailureInformation(ResultType.InCompatible, getIssue(loadProfileObisCode, "CouldNotParseLoadProfileData"));
            deviceLogBook.setFailureInformation(ResultType.InCompatible, getIssue(logBookReader.getLogBookObisCode(), "CouldNotParseLogBookData"));
        }
        collectedDataList.add(deviceLoadProfile);
        collectedDataList.add(deviceLogBook);
        return collectedDataList;
    }

    protected List<CollectedData> getLogBookData(final LogBookReader logBookReader) {
        List<CollectedData> collectedDataList = new ArrayList<>(1);
        CollectedLogBook deviceLogBook = this.getCollectedDataFactory().createCollectedLogBook(logBookReader.getLogBookIdentifier());
        try {
            ProfileData profileData = this.meterProtocol.getProfileData(logBookReader.getLastLogBook(), true);
            deviceLogBook.setMeterEvents(MeterEvent.mapMeterEventsToMeterProtocolEvents(profileData.getMeterEvents()));
        } catch (IOException e) {
            deviceLogBook.setFailureInformation(ResultType.NotSupported, getIssue(logBookReader.getLogBookObisCode(), "CouldNotReadoutLogBookData"));
        } catch (IndexOutOfBoundsException e) { // handles parsing errors
            deviceLogBook.setFailureInformation(ResultType.InCompatible, getIssue(logBookReader.getLogBookObisCode(), "CouldNotParseLogBookData"));
        }
        collectedDataList.add(deviceLogBook);
        return collectedDataList;
    }

    /**
     * Filter the list of IntervalData objects based on the startReadingTime of the profile.
     * All intervals before this startReadingTime should be ignored.
     *
     * @param allIntervalDatas
     * @param startReadingTime  the startReadingTime of the profile
     * @return
     */
    private List<IntervalData> getIntervalDatas(List<IntervalData> allIntervalDatas, Date startReadingTime) {
        List<IntervalData> filteredIntervalDatas = new ArrayList<>();
        for (IntervalData each : allIntervalDatas) {
            if (each.getEndTime().after(startReadingTime)) {
                filteredIntervalDatas.add(each);
            }
        }
        return filteredIntervalDatas;
    }

    /**
     * @return the actual time of the Device
     */
    @Override
    public Date getTime() {
        return this.meterProtocolClockAdapter.getTime();
    }

    private CollectedDataFactory getCollectedDataFactory() {
        return CollectedDataFactoryProvider.instance.get().getCollectedDataFactory();
    }

    private Issue getIssue(Object source, String description, Object... arguments){
        return this.issueService.newProblem(
                source,
                Environment.DEFAULT.get().getTranslation(description).replaceAll("'", "''"),
                arguments);
    }
}
