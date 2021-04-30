/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.meterprotocol;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.cbo.Unit;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLMeterProtocolAdapter;
import com.energyict.mdc.upl.LoadProfileConfigurationException;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.LogBook;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.tasks.support.DeviceLoadProfileSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.DeviceConfigurationException;
import org.joda.time.DateTimeConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter between a {@link MeterProtocol} and {@link com.energyict.mdc.upl.tasks.support.DeviceLoadProfileSupport}.
 * We use a {@link MeterProtocolClockAdapter} for the timeHandling of the interface.
 *
 * @author gna
 * @since 4/04/12 - 15:57
 */
public class MeterProtocolLoadProfileAdapter implements DeviceLoadProfileSupport {

    /**
     * The used <code>MeterProtocol</code> for which the adapter is working.
     */
    private final MeterProtocol meterProtocol;
    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;
    private final IdentificationService identificationService;
    private OfflineDevice offlineDevice;

    /**
     * The used <code>MeterProtocolClockAdapter</code> for the time handling of the {@link CollectedLoadProfile} interface.
     */
    private final MeterProtocolClockAdapter meterProtocolClockAdapter;

    public MeterProtocolLoadProfileAdapter(final MeterProtocol meterProtocol, IssueService issueService, CollectedDataFactory collectedDataFactory, IdentificationService identificationService) {
        this.meterProtocol = meterProtocol;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
        this.identificationService = identificationService;
        this.meterProtocolClockAdapter = new MeterProtocolClockAdapter(meterProtocol);
    }

    public void setOfflineDevice(OfflineDevice offlineDevice) {
        this.offlineDevice = offlineDevice;
    }

    /**
     * <b>Note:</b> This method is only called by the Collection Software if the option to "fail if channel configuration mismatch" is
     * checked on the <code>CommunicationProfile</code>
     * <p>
     * We will only check 1 <code>LoadProfile</code>({@link com.energyict.mdc.upl.tasks.support.DeviceLoadProfileSupport#GENERIC_LOAD_PROFILE_OBISCODE}) as standard {@link MeterProtocol}s only have support for 1 <code>LoadProfile</code>.
     * <b>We will only be able to verify the number of channels. The interval and channelUnits will not be validated until the actual data is fetched!</b>
     * <i>(even then it is only done by the protocol implementer)</i>
     * <p>
     * If a <code>LoadProfile</code> is not supported (meaning the {@link LoadProfileReader#getProfileObisCode() LoadProfileObisCode} is not
     * equal to {@link com.energyict.mdc.upl.tasks.support.DeviceLoadProfileSupport#GENERIC_LOAD_PROFILE_OBISCODE}), the corresponding boolean in the <code>DeviceLoadProfileConfiguration</code> must
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
                loadProfileConfiguration = collectedDataFactory.createCollectedLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), loadProfileReader.getMeterSerialNumber());
                if (GENERIC_LOAD_PROFILE_OBISCODE.equalsIgnoreBChannel(loadProfileReader.getProfileObisCode())) {
                    loadProfileConfiguration.setChannelInfos(getDefaultChannelInfo(loadProfileReader));
                    // the B-field will be used as marker for the interval in minutes
                    loadProfileConfiguration.setProfileInterval(loadProfileReader.getProfileObisCode().getB() * DateTimeConstants.SECONDS_PER_MINUTE);
                } else { // if it is not the standard ObisCode, then we indicate the LoadProfile is not supported
                    loadProfileConfiguration.setSupportedByMeter(false);
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
     * @param loadProfileReader The LoadProfileReader
     * @return the created list of <code>ChannelInfos</code>
     */
    private List<ChannelInfo> getDefaultChannelInfo(LoadProfileReader loadProfileReader) {
        try {
            int numberOfChannels = this.meterProtocol.getNumberOfChannels();
            List<ChannelInfo> listOfChannelInfo = new ArrayList<>();
            for (int i = 0; i < getMaxChannels(loadProfileReader, numberOfChannels); i++) {
                ChannelInfo configuredChannelInfo = loadProfileReader.getChannelInfos().get(i);
                listOfChannelInfo.add(new ChannelInfo(i, new ObisCode(GENERIC_CHANNEL_OBISCODE, i + 1).toString(), configuredChannelInfo.getUnit(), configuredChannelInfo.getMeterIdentifier(), configuredChannelInfo.getReadingTypeMRID()));  //NON-NLS
            }
            return listOfChannelInfo;
        } catch (IOException e) {
            if (isTimeout(e)) {
                throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
            } else {
                throw new DeviceConfigurationException(e, MessageSeeds.UNEXPECTED_IO_EXCEPTION, e.getMessage());
            }
        }
    }

    private int getMaxChannels(LoadProfileReader loadProfileReader, int numberOfChannels) {
        return numberOfChannels <= loadProfileReader.getChannelInfos().size() ? numberOfChannels : loadProfileReader.getChannelInfos().size();
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
                    if (logBookReader != null && !((UPLMeterProtocolAdapter)meterProtocol).getActual().hasSupportForSeparateEventsReading()) {
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

    public LogBookReader getValidLogBook(List<LogBookReader> logBookReaders) {
        LogBookReader validLogBook = null;
        for (LogBookReader logBookReader : logBookReaders) {
            if (logBookReader.getLogBookObisCode().equals(LogBook.GENERIC_LOGBOOK_TYPE_OBISCODE)) {
                validLogBook = logBookReader;
            }
        }
        return validLogBook;
    }

    /**
     * Create a simple not-supported {@link CollectedLoadProfile} object.
     *
     * @param loadProfileReader the reader which triggered the collection
     * @return a {@link CollectedLoadProfile} with proper failure information
     */
    private CollectedLoadProfile createUnSupportedCollectedLoadProfile(final LoadProfileReader loadProfileReader) {
        CollectedLoadProfile deviceLoadProfile = this.collectedDataFactory.createCollectedLoadProfile(null);
        deviceLoadProfile.setFailureInformation(ResultType.NotSupported, getProblem(loadProfileReader.getProfileObisCode(), MessageSeeds.LOADPROFILE_NOT_SUPPORTED, loadProfileReader.getProfileObisCode()));
        return deviceLoadProfile;
    }

    private List<CollectedLogBook> createUnSupportedCollectedLogBooksForInvalidLogBookReaders(final List<LogBookReader> logBookReaders) {
        CollectedDataFactory collectedDataFactory = this.collectedDataFactory;
        List<CollectedLogBook> collectedLogBookList = new ArrayList<>();
        for (LogBookReader reader : logBookReaders) {
            if (!reader.getLogBookObisCode().equals(LogBook.GENERIC_LOGBOOK_TYPE_OBISCODE)) {
                CollectedLogBook deviceLogBook = collectedDataFactory.createCollectedLogBook(reader.getLogBookIdentifier());
                deviceLogBook.setFailureInformation(ResultType.NotSupported, getWarning(reader.getLogBookObisCode(), MessageSeeds.LOGBOOK_NOT_SUPPORTED, reader.getLogBookObisCode()));
                collectedLogBookList.add(deviceLogBook);
            }
        }
        return collectedLogBookList;
    }

    /**
     * Collect a single <code>ProfileData</code> object from the Device.
     * The returned object will contain one of the below possibilities:
     * <ul>
     * <li>Correct {@link com.energyict.protocol.IntervalData IntervalData} and {@link ChannelInfo}</li>
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
                        this.identificationService.createLoadProfileIdentifierByDatabaseId(
                                loadProfileReader.getLoadProfileId(),
                                loadProfileReader.getProfileObisCode(),
                                offlineDevice.getDeviceIdentifier()));
        try {
            ProfileData profileData = this.meterProtocol.getProfileData(loadProfileReader.getStartReadingTime(), false);
            deviceLoadProfile.setCollectedIntervalData(profileData.getIntervalDatas(), convertToProperChannelInfos(profileData, loadProfileReader.getChannelInfos(), deviceLoadProfile));
            deviceLoadProfile.setDoStoreOlderValues(profileData.shouldStoreOlderValues());
        } catch (IOException e) {
            if (isTimeout(e)) {
                throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
            } else {
                deviceLoadProfile.setFailureInformation(ResultType.NotSupported, getProblem(loadProfileReader.getProfileObisCode(), MessageSeeds.COULD_NOT_READOUT_LOADPROFILE_DATA, e.getMessage()));
            }
        } catch (IndexOutOfBoundsException e) { // handles parsing errors
            deviceLoadProfile.setFailureInformation(
                    ResultType.InCompatible,
                    getProblem(
                            loadProfileReader.getProfileObisCode(),
                            MessageSeeds.COULD_NOT_PARSE_LOADPROFILE_DATA,
                            e.toString()));
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
                ChannelInfo convertedChannelInfo = new ChannelInfo(configuredChannelInfo.getId(), channelObisCode.toString(), ci.getUnit(), configuredChannelInfo.getMeterIdentifier(), configuredChannelInfo.getReadingTypeMRID());
                convertedChannelInfo.setMultiplier(ci.getMultiplier());
                convertedChannelInfo.setCumulativeWrapValue(ci.getCumulativeWrapValue());
                if (ci.isCumulative()) {
                    convertedChannelInfo.setCumulative();
                }
                convertedChannelInfos.add(convertedChannelInfo);
            } catch (LoadProfileConfigurationException e) {
                collectedLoadProfile.setFailureInformation(ResultType.ConfigurationMisMatch, getWarning(profileData, MessageSeeds.UNSUPPORTED_CHANNEL_INFO, ci));
            }
        }
        return convertedChannelInfos;
    }

    protected ChannelInfo getChannelInfoFromConfiguredChannels(ObisCode obisCode, List<ChannelInfo> configuredChannelInfos) throws LoadProfileConfigurationException {
        return configuredChannelInfos.stream().filter(channelInfo -> channelInfo.getChannelObisCode().equals(obisCode)).findFirst().orElseThrow(() -> new LoadProfileConfigurationException("Could not found a correct ChannelInfo with the obiscode " + obisCode));
    }

    private List<CollectedData> getSingleProfileData(LoadProfileReader loadProfileReader, final LogBookReader logBookReader) {
        List<CollectedData> collectedDataList = new ArrayList<>(2);
        Date combinedLastReadingTime = loadProfileReader.getStartReadingTime().before(logBookReader.getLastLogBook()) ? loadProfileReader.getStartReadingTime() : logBookReader.getLastLogBook();

        CollectedDataFactory collectedDataFactory = this.collectedDataFactory;
        CollectedLoadProfile deviceLoadProfile =
                collectedDataFactory.createCollectedLoadProfile(
                        this.identificationService.createLoadProfileIdentifierByDatabaseId(
                                loadProfileReader.getLoadProfileId(),
                                loadProfileReader.getProfileObisCode(),
                                offlineDevice.getDeviceIdentifier()));
        CollectedLogBook deviceLogBook = collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
        try {
            ProfileData profileData = this.meterProtocol.getProfileData(combinedLastReadingTime, true);
            deviceLoadProfile.setCollectedIntervalData(getIntervalDatas(profileData.getIntervalDatas(), loadProfileReader.getStartReadingTime()), convertToProperChannelInfos(profileData, loadProfileReader.getChannelInfos(), deviceLoadProfile));
            deviceLoadProfile.setDoStoreOlderValues(profileData.shouldStoreOlderValues());
            deviceLogBook.setCollectedMeterEvents(MeterEvent.mapMeterEventsToMeterProtocolEvents(profileData.getMeterEvents()));
        } catch (IOException e) {
            if (isTimeout(e)) {
                throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
            } else {
                deviceLoadProfile.setFailureInformation(
                        ResultType.NotSupported,
                        getProblem(
                                loadProfileReader.getProfileObisCode(),
                                MessageSeeds.COULD_NOT_READOUT_LOADPROFILE_DATA,
                                e.getMessage()));
                deviceLogBook.setFailureInformation(
                        ResultType.NotSupported,
                        getProblem(
                                logBookReader.getLogBookObisCode(),
                                MessageSeeds.COULD_NOT_READOUT_LOGBOOK_DATA,
                                e.getMessage()));
            }
        } catch (IndexOutOfBoundsException e) { // handles parsing errors
            deviceLoadProfile.setFailureInformation(
                    ResultType.InCompatible,
                    getProblem(
                            loadProfileReader.getProfileObisCode(),
                            MessageSeeds.COULD_NOT_PARSE_LOADPROFILE_DATA,
                            e.toString()));
            deviceLogBook.setFailureInformation(
                    ResultType.InCompatible,
                    getProblem(
                            logBookReader.getLogBookObisCode(),
                            MessageSeeds.COULD_NOT_PARSE_LOGBOOK_DATA,
                            e.toString()));
        }
        collectedDataList.add(deviceLoadProfile);
        collectedDataList.add(deviceLogBook);
        return collectedDataList;
    }

    protected List<CollectedData> getLogBookData(final LogBookReader logBookReader) {
        List<CollectedData> collectedDataList = new ArrayList<>(1);
        CollectedLogBook deviceLogBook = this.collectedDataFactory.createCollectedLogBook(logBookReader.getLogBookIdentifier());
        try {
            if(((UPLMeterProtocolAdapter)meterProtocol).getActual().hasSupportForSeparateEventsReading()) { //read meter events separately, without reading the load profile
                List<MeterEvent> meterEvents = this.meterProtocol.getMeterEvents(logBookReader.getLastLogBook());
                deviceLogBook.setCollectedMeterEvents(MeterEvent.mapMeterEventsToMeterProtocolEvents(meterEvents));
            } else {
                ProfileData profileData = this.meterProtocol.getProfileData(logBookReader.getLastLogBook(), true);
                deviceLogBook.setCollectedMeterEvents(MeterEvent.mapMeterEventsToMeterProtocolEvents(profileData.getMeterEvents()));
            }
        } catch (IOException e) {
            if (isTimeout(e)) {
                throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
            } else {
                deviceLogBook.setFailureInformation(
                        ResultType.NotSupported,
                        getProblem(
                                logBookReader.getLogBookObisCode(),
                                MessageSeeds.COULD_NOT_READOUT_LOGBOOK_DATA,
                                e.getMessage()));
            }
        } catch (IndexOutOfBoundsException e) { // handles parsing errors
            deviceLogBook.setFailureInformation(
                    ResultType.InCompatible,
                    getProblem(
                            logBookReader.getLogBookObisCode(),
                            MessageSeeds.COULD_NOT_PARSE_LOGBOOK_DATA,
                            e.toString()));
        }
        collectedDataList.add(deviceLogBook);
        return collectedDataList;
    }

    /**
     * Filter the list of IntervalData objects based on the startReadingTime of the profile.
     * All intervals before this startReadingTime should be ignored.
     *
     * @param allIntervalDatas The List of IntervalData
     * @param startReadingTime the startReadingTime of the profile
     * @return The List of IntervalData
     */
    private List<IntervalData> getIntervalDatas(List<IntervalData> allIntervalDatas, Date startReadingTime) {
        return allIntervalDatas
                .stream()
                .filter(each -> each.getEndTime().after(startReadingTime))
                .collect(Collectors.toList());
    }

    @Override
    public Date getTime() {
        return this.meterProtocolClockAdapter.getTime();
    }

    private Issue getProblem(Object source, MessageSeed messageSeed, Object... arguments) {
        return this.issueService.newProblem(source, messageSeed, arguments);
    }

    private Issue getWarning(Object source, MessageSeed messageSeed, Object... arguments) {
        return this.issueService.newWarning(source, messageSeed, arguments);
    }

}