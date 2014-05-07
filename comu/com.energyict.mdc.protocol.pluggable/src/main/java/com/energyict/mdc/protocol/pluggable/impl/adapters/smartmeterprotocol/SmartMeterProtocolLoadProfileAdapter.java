package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.LoadProfileConfiguration;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.mdc.protocol.api.exceptions.DataParseException;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.tasks.support.DeviceLoadProfileSupport;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.StackTracePrinter;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.identifiers.LoadProfileDataIdentifier;
import com.energyict.protocolimplv2.identifiers.SerialNumberDeviceIdentifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Adapter between a {@link SmartMeterProtocolAdapter} and a {@link com.energyict.mdc.protocol.api.tasks.support.DeviceLoadProfileSupport}
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

    /**
     * The used {@link SmartMeterProtocolClockAdapter}
     */
    private final SmartMeterProtocolClockAdapter smartMeterProtocolClockAdapter;

    /**
     * Default constructor
     *
     * @param smartMeterProtocol the {@link com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol} to glue
     * @param issueService
     */
    public SmartMeterProtocolLoadProfileAdapter(final SmartMeterProtocol smartMeterProtocol, IssueService issueService) {
        this.smartMeterProtocol = smartMeterProtocol;
        this.issueService = issueService;
        this.smartMeterProtocolClockAdapter = new SmartMeterProtocolClockAdapter(smartMeterProtocol);
    }

    /**
     * <b>Note:</b> This method is only called by the Collection Software if the option to "fail if channel configuration mismatch" is
     * checked on the <code>CommunicationProfile</code>
     * <p/>
     * Get the configuration(interval, number of channels, channelUnits) of all given <code>LoadProfiles</code> from the Device.
     * Build up a list of <CODE>DeviceLoadProfileConfiguration</CODE> objects and return them so the framework can validate them to the configuration
     * in EIServer.
     * <p/>
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
                loadProfileConfigurations = convertToCollectedLoadProfileConfigurations(configurations);
            } catch (IOException e) {
                loadProfileConfigurations = createIssueListForLoadProfileReaders(loadProfilesToRead, e);
            } catch (IndexOutOfBoundsException e) {
                loadProfileConfigurations = createIssueListForLoadProfileReaders(loadProfilesToRead, e);
            }
        }
        return loadProfileConfigurations;
    }

    private List<CollectedLoadProfileConfiguration> createIssueListForLoadProfileReaders(List<LoadProfileReader> loadProfilesToRead, Exception e) {
        CollectedDataFactory collectedDataFactory = this.getCollectedDataFactory();
        List<CollectedLoadProfileConfiguration> configurations = new ArrayList<>();
        for (LoadProfileReader loadProfileReader : loadProfilesToRead) {
            CollectedLoadProfileConfiguration deviceLoadProfileConfiguration = collectedDataFactory.createCollectedLoadProfileConfiguration(loadProfileReader.getProfileObisCode(), loadProfileReader.getMeterSerialNumber(), false);
            deviceLoadProfileConfiguration.setFailureInformation(ResultType.DataIncomplete, getIssue(this, "deviceprotocol.legacy.issue", StackTracePrinter.print(e)));
            configurations.add(deviceLoadProfileConfiguration);
        }
        return configurations;
    }

    private List<CollectedLoadProfileConfiguration> convertToCollectedLoadProfileConfigurations(List<LoadProfileConfiguration> loadProfileConfigurations) {
        CollectedDataFactory collectedDataFactory = this.getCollectedDataFactory();
        List<CollectedLoadProfileConfiguration> collectedLoadProfileConfigurations = new ArrayList<>();
        for (LoadProfileConfiguration loadProfileConfiguration : loadProfileConfigurations) {
            CollectedLoadProfileConfiguration deviceLoadProfileConfiguration = collectedDataFactory.createCollectedLoadProfileConfiguration(loadProfileConfiguration.getObisCode(), loadProfileConfiguration.getMeterSerialNumber());
            deviceLoadProfileConfiguration.setSupportedByMeter(loadProfileConfiguration.isSupportedByMeter());
            deviceLoadProfileConfiguration.setChannelInfos(loadProfileConfiguration.getChannelInfos());
            deviceLoadProfileConfiguration.setProfileInterval(loadProfileConfiguration.getProfileInterval());
            collectedLoadProfileConfigurations.add(deviceLoadProfileConfiguration);
        }
        return collectedLoadProfileConfigurations;
    }

    /**
     * Collect one or more LoadProfiles from a device. Each <CODE>LoadProfileReader</CODE> contains a list of necessary
     * channels({@link LoadProfileReader#channelInfos}) to read. If it is possible then only these channels should be read,
     * if not then all channels may be returned.If {@link LoadProfileReader#channelInfos} contains an empty list
     * or null, then all channels from the corresponding LoadProfile should be fetched
     * <p/>
     * If for a certain <code>LoadProfile</code> not all data since {@link LoadProfileReader#getStartReadingTime() lastReading}
     * can be returned, then a proper {@link ResultType} <b>and</b> {@link com.energyict.mdc.issues.Issue issue}
     * should be set so proper logging of this action can be performed.
     * <p/>
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
                CollectedDataFactory collectedDataFactory = this.getCollectedDataFactory();
                final List<ProfileData> loadProfileData = this.smartMeterProtocol.getLoadProfileData(loadProfiles);
                CollectedLoadProfile deviceLoadProfile;
                for (LoadProfileReader loadProfile : loadProfiles) {
                    final ProfileData profileDataWithLoadProfileId = getProfileDataWithLoadProfileId(loadProfileData, loadProfile.getLoadProfileId());
                    profileDataWithLoadProfileId.sort();
                    deviceLoadProfile =
                            collectedDataFactory.createCollectedLoadProfile(
                                    new LoadProfileDataIdentifier(
                                            loadProfile.getProfileObisCode(),
                                            new SerialNumberDeviceIdentifier(loadProfile.getMeterSerialNumber())));
                    if (!profileDataWithLoadProfileId.equals(INVALID_PROFILE_DATA)) {
                        deviceLoadProfile.setCollectedData(profileDataWithLoadProfileId.getIntervalDatas(), profileDataWithLoadProfileId.getChannelInfos());
                        deviceLoadProfile.setDoStoreOlderValues(profileDataWithLoadProfileId.shouldStoreOlderValues());
                    } else {
                        deviceLoadProfile.setFailureInformation(ResultType.NotSupported, getIssue(loadProfile.getProfileObisCode(), "profileXnotsupported", loadProfile.getProfileObisCode()));
                    }
                    collectedLoadProfiles.add(deviceLoadProfile);
                }
            } catch (IOException e) {
                throw new LegacyProtocolException(e);
            } catch (IndexOutOfBoundsException e) {
                throw new DataParseException(e);
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

    private CollectedDataFactory getCollectedDataFactory() {
        List<CollectedDataFactory> factories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(CollectedDataFactory.class);
        if (factories.isEmpty()) {
            throw CommunicationException.missingModuleException(CollectedDataFactory.class);
        }
        else {
            return factories.get(0);
        }
    }

    private Issue getIssue(Object source, String description, Object... arguments){
        return this.issueService.newProblem(
                source,
                Environment.DEFAULT.get().getTranslation(description).replaceAll("'", "''"),
                arguments);
    }

}
