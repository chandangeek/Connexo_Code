package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.StackTracePrinter;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.LoadProfileConfiguration;
import com.energyict.mdc.protocol.api.exceptions.DataParseException;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.tasks.support.DeviceLoadProfileSupport;

import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter between a {@link SmartMeterProtocolAdapterImpl} and a {@link com.energyict.mdc.upl.tasks.support.DeviceLoadProfileSupport}
 *
 * @author gna
 * @since 5/04/12 - 13:56
 */
public class SmartMeterProtocolLoadProfileAdapter implements DeviceLoadProfileSupport {

    /**
     * Object to use as an invalid {@link ProfileData} object
     */
    protected static final ProfileData INVALID_PROFILE_DATA = new ProfileData(-1);

    /**
     * The used {@link SmartMeterProtocol}
     */
    private final SmartMeterProtocol smartMeterProtocol;
    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;
    private final IdentificationService identificationService;

    /**
     * The used {@link SmartMeterProtocolClockAdapter}
     */
    private final SmartMeterProtocolClockAdapter smartMeterProtocolClockAdapter;

    public SmartMeterProtocolLoadProfileAdapter(final SmartMeterProtocol smartMeterProtocol, IssueService issueService, CollectedDataFactory collectedDataFactory, IdentificationService identificationService) {
        this.smartMeterProtocol = smartMeterProtocol;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
        this.identificationService = identificationService;
        this.smartMeterProtocolClockAdapter = new SmartMeterProtocolClockAdapter(smartMeterProtocol);
    }

    /**
     * <b>Note:</b> This method is only called by the Collection Software if the option to "fail if channel configuration mismatch" is
     * checked on the <code>CommunicationProfile</code>
     * <p>
     * Get the configuration(interval, number of channels, channelUnits) of all given <code>LoadProfiles</code> from the Device.
     * Build up a list of <CODE>DeviceLoadProfileConfiguration</CODE> objects and return them so the framework can validate them to the configuration
     * in EIServer.
     * <p>
     * If a <code>LoadProfile</code> is not supported, the corresponding boolean in the <code>DeviceLoadProfileConfiguration</code> should be set to false.
     *
     * @param loadProfilesToRead the <CODE>List</CODE> of <CODE>LoadProfileReaders</CODE> to indicate which profiles will be read
     * @return a list of <CODE>DeviceLoadProfileConfiguration</CODE> objects corresponding with the meter
     */
    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(final List<LoadProfileReader> loadProfilesToRead) {
        List<CollectedLoadProfileConfiguration> loadProfileConfigurations = new ArrayList<>();
        if (loadProfilesToRead != null) {
            try {
                List<LoadProfileConfiguration> configurations = this.smartMeterProtocol.fetchLoadProfileConfiguration(loadProfilesToRead);
                loadProfileConfigurations = convertToCollectedLoadProfileConfigurations(configurations, this.meterSerialNumber(loadProfilesToRead));
            } catch (IOException | IndexOutOfBoundsException e) {
                loadProfileConfigurations = createIssueListForLoadProfileReaders(loadProfilesToRead, e);
            }
        }
        return loadProfileConfigurations;
    }

    private List<CollectedLoadProfileConfiguration> createIssueListForLoadProfileReaders(List<LoadProfileReader> loadProfilesToRead, Exception e) {
        CollectedDataFactory collectedDataFactory = this.collectedDataFactory;
        return loadProfilesToRead
                .stream()
                .map(loadProfileReader -> {
                    CollectedLoadProfileConfiguration deviceLoadProfileConfiguration =
                            collectedDataFactory.createCollectedLoadProfileConfiguration(
                                    loadProfileReader.getProfileObisCode(),
                                    loadProfileReader.getMeterSerialNumber());
                    deviceLoadProfileConfiguration.setSupportedByMeter(false);
                    deviceLoadProfileConfiguration.setFailureInformation(
                            ResultType.DataIncomplete,
                            getIssue(this, com.energyict.mdc.protocol.api.MessageSeeds.DEVICEPROTOCOL_LEGACY_ISSUE, StackTracePrinter.print(e)));
                    return deviceLoadProfileConfiguration;
                })
                .collect(Collectors.toList());
    }

    private List<CollectedLoadProfileConfiguration> convertToCollectedLoadProfileConfigurations(List<LoadProfileConfiguration> loadProfileConfigurations, String meterSerialNumber) {
        CollectedDataFactory collectedDataFactory = this.collectedDataFactory;
        return loadProfileConfigurations
                .stream()
                .map(loadProfileConfiguration -> {
                    CollectedLoadProfileConfiguration deviceLoadProfileConfiguration =
                            collectedDataFactory.createCollectedLoadProfileConfiguration(
                                    loadProfileConfiguration.getObisCode(),
                                    loadProfileConfiguration.getDeviceIdentifier(),
                                    meterSerialNumber);
                    deviceLoadProfileConfiguration.setSupportedByMeter(loadProfileConfiguration.isSupportedByMeter());
                    deviceLoadProfileConfiguration.setChannelInfos(loadProfileConfiguration.getChannelInfos());
                    deviceLoadProfileConfiguration.setProfileInterval(loadProfileConfiguration.getProfileInterval());
                    return deviceLoadProfileConfiguration;
                })
                .collect(Collectors.toList());
    }

    private String meterSerialNumber(List<LoadProfileReader> readers) {
        return readers.stream().map(LoadProfileReader::getMeterSerialNumber).findAny().orElse("");
    }

    /**
     * Collect one or more LoadProfiles from a device. Each <CODE>LoadProfileReader</CODE> contains a list of necessary
     * channels({@link LoadProfileReader#channelInfos}) to read. If it is possible then only these channels should be read,
     * if not then all channels may be returned.If {@link LoadProfileReader#channelInfos} contains an empty list
     * or null, then all channels from the corresponding LoadProfile should be fetched
     * <p>
     * If for a certain <code>LoadProfile</code> not all data since {@link LoadProfileReader#getStartReadingTime() lastReading}
     * can be returned, then a proper {@link ResultType} <b>and</b> {@link com.energyict.mdc.upl.issue.Issue issue}
     * should be set so proper logging of this action can be performed.
     * <p>
     * In essence, the size of the returned <code>List</code> should be the same as the size of the given argument <code>List</code>.
     *
     * @param loadProfiles a list of <CODE>LoadProfileReader</CODE> which have to be read
     * @return a list of <CODE>CollectedLoadProfile</CODE> objects containing interval records
     */
    @Override
    public List<CollectedLoadProfile> getLoadProfileData(final List<LoadProfileReader> loadProfiles) {
        if (loadProfiles != null) {
            List<CollectedLoadProfile> collectedLoadProfiles = new ArrayList<>();
            try {
                CollectedDataFactory collectedDataFactory = this.collectedDataFactory;
                final List<ProfileData> loadProfileData = this.smartMeterProtocol.getLoadProfileData(loadProfiles);
                CollectedLoadProfile deviceLoadProfile;
                for (LoadProfileReader loadProfileReader : loadProfiles) {
                    final ProfileData profileDataWithLoadProfileId = getProfileDataWithLoadProfileId(loadProfileData, loadProfileReader.getLoadProfileId());
                    profileDataWithLoadProfileId.sort();
                    deviceLoadProfile =
                            collectedDataFactory.createCollectedLoadProfile(
                                    this.identificationService.createLoadProfileIdentifierByDatabaseId(
                                            loadProfileReader.getLoadProfileId(),
                                            loadProfileReader.getProfileObisCode()));
                    if (!profileDataWithLoadProfileId.equals(INVALID_PROFILE_DATA)) {
                        deviceLoadProfile.setCollectedIntervalData(profileDataWithLoadProfileId.getIntervalDatas(), profileDataWithLoadProfileId.getChannelInfos());
                        deviceLoadProfile.setDoStoreOlderValues(profileDataWithLoadProfileId.shouldStoreOlderValues());
                    } else {
                        deviceLoadProfile.setFailureInformation(
                                ResultType.NotSupported,
                                getIssue(
                                        loadProfileReader.getProfileObisCode(),
                                        com.energyict.mdc.protocol.api.MessageSeeds.LOADPROFILE_NOT_SUPPORTED,
                                        loadProfileReader.getProfileObisCode()));
                    }
                    collectedLoadProfiles.add(deviceLoadProfile);
                }
            } catch (IOException e) {
                throw new LegacyProtocolException(MessageSeeds.LEGACY_IO, e);
            } catch (IndexOutOfBoundsException e) {
                throw new DataParseException(e, MessageSeeds.INDEX_OUT_OF_BOUND_DATA_PARSE_EXCEPTION);
            }
            return collectedLoadProfiles;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Search for a {@link ProfileData} object in the given list with the {@link ProfileData#loadProfileId} equal to the given argument
     *
     * @param profileDataList the ProfileData list to search
     * @param loadProfileId   the ID to look for
     * @return the requested ProfileData or the {@link #INVALID_PROFILE_DATA} when the loadProfileId is not found
     */
    protected ProfileData getProfileDataWithLoadProfileId(final List<ProfileData> profileDataList, long loadProfileId) {
        if (profileDataList != null) {
            for (ProfileData profileData : profileDataList) {
                if (profileData.getLoadProfileId() == loadProfileId) {
                    return profileData;
                }
            }
            return INVALID_PROFILE_DATA;
        } else {
            return INVALID_PROFILE_DATA;
        }
    }

    /**
     * @return the actual time of the Device
     */
    @Override
    public Date getTime() {
        return this.smartMeterProtocolClockAdapter.getTime();
    }

    private Issue getIssue(Object source, MessageSeed messageSeed, Object... arguments) {
        return this.issueService.newProblem(source, messageSeed, arguments);
    }

}