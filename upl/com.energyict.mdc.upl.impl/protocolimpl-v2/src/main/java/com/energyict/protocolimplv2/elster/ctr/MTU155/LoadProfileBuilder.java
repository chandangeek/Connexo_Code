package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.energyict.cbo.Unit;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.profile.ProfileChannel;
import com.energyict.protocolimplv2.elster.ctr.MTU155.profile.StartOfGasDayParser;
import com.energyict.protocolimplv2.elster.ctr.MTU155.util.CTRObjectInfo;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierById;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;


/**
 * @author: sva
 * @since: 22/10/12 (13:02)
 */
public class LoadProfileBuilder {

    public static final ObisCode FLOW_MEASUREMENT_PROFILE = ObisCode.fromString("0.0.99.1.0.255");
    public static final ObisCode VOLUME_MEASUREMENT_PROFILE = ObisCode.fromString("0.0.99.2.0.255");
    public static final ObisCode TOTALIZERS_PROFILE = ObisCode.fromString("0.0.99.3.0.255");

    public static final String[] FLOW_MEASUREMENT_OBJECT_IDS = new String[]{"1.0.0", "1.2.0", "4.0.0", "7.0.0"};
    public static final String[] VOLUME_MEASUREMENT_OBJECT_IDS = new String[]{"1.1.0", "1.3.0", "1.F.0"};
    public static final String[] TOTALIZERS_OBJECT_IDS = new String[]{"2.0.0", "2.1.0", "2.3.0", "1.2.3"};
    /**
     * The {@link StartOfGasDayParser} to use
     */
    protected StartOfGasDayParser startOfGasDayParser;
    private MTU155 meterProtocol;
    /**
     * The list of LoadProfileReaders which are expected to be fetched
     */
    private List<LoadProfileReader> expectedLoadProfileReaders;
    /**
     * The list of <CODE>DeviceLoadProfileConfiguration</CODE> objects which are build from the information from the actual device, based on the {@link #expectedLoadProfileReaders}
     */
    private List<CollectedLoadProfileConfiguration> loadProfileConfigurationList;
    /**
     * Keeps track of the list of <CODE>ChannelInfo</CODE> objects for all the LoadProfiles
     */
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfoMap = new HashMap<>();

    public LoadProfileBuilder(MTU155 meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    /**
     * Get the configuration(interval, number of channels, channelUnits) of all given LoadProfiles for the {@link #meterProtocol}
     *
     * @param loadProfileReaders a list of definitions of expected loadProfiles to read
     * @return the list of <CODE>DeviceLoadProfileConfiguration</CODE> objects which are in the device
     * @throws java.io.IOException when error occurred during dataFetching or -Parsing
     */
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) {
        expectedLoadProfileReaders = loadProfileReaders;
        loadProfileConfigurationList = new ArrayList<>();

        for (LoadProfileReader lpr : expectedLoadProfileReaders) {
            this.meterProtocol.getLogger().log(Level.INFO, "Reading configuration from LoadProfile " + lpr);
            CollectedLoadProfileConfiguration lpc = MdcManager.getCollectedDataFactory().createCollectedLoadProfileConfiguration(lpr.getProfileObisCode(), meterProtocol.getOfflineDevice().getSerialNumber());

            try {
                List<ChannelInfo> channelInfos = constructChannelInfos(lpr);
                lpc.setChannelInfos(channelInfos);
                lpc.setProfileInterval(getProfileInterval(lpr.getProfileObisCode()));

                if (!channelInfoMap.containsKey(lpr)) {
                    channelInfoMap.put(lpr, channelInfos);
                }
            } catch (IOException e) {
                meterProtocol.getLogger().info("Failed to fetch LoadProfile configuration for LoadProfile " + lpr.getProfileObisCode() + ": " + e.getMessage());
                lpc.setSupportedByMeter(false);
            } catch (NullPointerException e) {
                lpc.setSupportedByMeter(false);
            }
            loadProfileConfigurationList.add(lpc);
        }
        return loadProfileConfigurationList;
    }

    private int getProfileInterval(ObisCode obisCode) throws IOException {
        if (obisCode.equals(FLOW_MEASUREMENT_PROFILE)) {
            return 3600;    // hourly
        } else if (obisCode.equals(VOLUME_MEASUREMENT_PROFILE)) {
            return 86400;   // daily
        } else if (obisCode.equals(TOTALIZERS_PROFILE)) {
            return 86400;   // daily
        }
        throw new IOException("LoadProfile with Obiscode " + obisCode + " is not supported.");
    }

    /**
     * Construct a list of <CODE>ChannelInfos</CODE>.
     */
    private List<ChannelInfo> constructChannelInfos(LoadProfileReader lpr) throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<>();

        for (int i = 0; i < lpr.getChannelInfos().size(); i++) {
            ChannelInfo lprChannelInfo = lpr.getChannelInfos().get(i);
            ObisCode obisCode = lprChannelInfo.getChannelObisCode();
            CTRRegisterMapping ctrRegisterMapping = meterProtocol.getObisCodeMapper().searchRegisterMapping(obisCode);
            if (ctrRegisterMapping != null && ctrRegisterMapping.getObjectId() != null) {
                // Correct the Channel object ID, cause the ID retrieved from the RegisterMapping refers to the instant object and not the hourly/daily one
                CTRObjectID objectId = getCorrectedChannelObjectID(ctrRegisterMapping.getObjectId(), getProfileInterval(lpr.getProfileObisCode()));
                Unit unit = CTRObjectInfo.getUnit(objectId.toString());
                channelInfos.add(new ChannelInfo(i, obisCode.toString(), unit, lpr.getMeterSerialNumber()));
            } else {
                throw new CTRException("Channel with obisCode " + obisCode + " is not supported by this profile!");
            }
        }
        return channelInfos;
    }

    private CTRObjectID getCorrectedChannelObjectID(ObisCode obisCode, int profileInterval) throws CTRException {
        CTRRegisterMapping ctrRegisterMapping = meterProtocol.getObisCodeMapper().searchRegisterMapping(obisCode);
        CTRObjectID instantValueObjectId = ctrRegisterMapping.getObjectId();    // This is the object ID of the instant register
        if (ctrRegisterMapping != null && instantValueObjectId != null) {
            if (profileInterval == 3600) {
                return new CTRObjectID(instantValueObjectId.getX(), instantValueObjectId.getY(), 2);    // Y-field 2 for hourly profile
            } else if (profileInterval == 86400) {
                return new CTRObjectID(instantValueObjectId.getX(), instantValueObjectId.getY(), 3);    // Y-field 3 for daily profile
            }
        }
        throw new CTRException("Channel with obisCode " + obisCode + " is not supported by this profile!");
    }

    private CTRObjectID getCorrectedChannelObjectID(CTRObjectID objectID, int profileInterval) throws CTRException {
        if (profileInterval == 3600) {
            return new CTRObjectID(objectID.getX(), objectID.getY(), 2);    // Y-field 2 for hourly profile
        } else if (profileInterval == 86400) {
            return new CTRObjectID(objectID.getX(), objectID.getY(), 3);    // Y-field 3 for daily profile
        }
        throw new CTRException("Channel for objectID " + objectID + " is not supported by this profile!");
    }

    /**
     * <p>
     * Fetches one or more LoadProfiles from the device. Each <CODE>LoadProfileReader</CODE> contains a list of necessary
     * channels({@link com.energyict.protocol.LoadProfileReader#channelInfos}) to read. If it is possible then only these channels should be read,
     * if not then all channels may be returned in the <CODE>ProfileData</CODE>. If {@link com.energyict.protocol.LoadProfileReader#channelInfos} contains an empty list
     * or null, then all channels from the corresponding LoadProfile should be fetched.
     * </p>
     * <p>
     * <b>Implementors should throw an exception if all data since {@link com.energyict.protocol.LoadProfileReader#getStartReadingTime()} can NOT be fetched</b>,
     * as the collecting system will update its lastReading setting based on the returned ProfileData
     * </p>
     *
     * @param loadProfiles a list of <CODE>LoadProfileReader</CODE> which have to be read
     * @return a list of <CODE>CollectedLoadProfile</CODE> objects containing interval records
     */
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        List<CollectedLoadProfile> collectedLoadProfileList = new ArrayList<>();
        for (LoadProfileReader lpr : loadProfiles) {
            CollectedLoadProfileConfiguration lpc = getLoadProfileConfiguration(lpr);
            if (this.channelInfoMap.containsKey(lpr) && lpc != null) { // otherwise it is not supported by the meter
                List<ChannelInfo> channelInfos = this.channelInfoMap.get(lpr);
                CollectedLoadProfile collectedLoadProfile = MdcManager.getCollectedDataFactory().createCollectedLoadProfile(new LoadProfileIdentifierById(lpr.getLoadProfileId(), lpr.getProfileObisCode()));
                List<IntervalData> collectedIntervalData = new ArrayList<>();

                for (ChannelInfo channel : channelInfos) {
                    List<IntervalData> channelIntervalData;
                    try {
                        int profileInterval = getProfileInterval(lpr.getProfileObisCode());
                        CTRObjectID objectID = getCorrectedChannelObjectID(channel.getChannelObisCode(), profileInterval);
                        ProfileChannel profileChannel = new ProfileChannel(meterProtocol.getRequestFactory(), getStartOfGasDayParser(), objectID, profileInterval, lpr.getStartReadingTime(), lpr.getEndReadingTime());
                        meterProtocol.getLogger().info("Reading profile for channel [" + channel.getName() + "]");
                        channelIntervalData = profileChannel.getProfileData().getIntervalDatas();
                        collectedIntervalData = mergeChannelIntervalData(collectedIntervalData, channelIntervalData);
                    } catch (IOException e) {   // A non-blocking issue occurred during readout of this loadProfile, but it is still possible to read out the other loadProfiles.
                        collectedLoadProfile.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueFactory().createProblem(lpr, "loadProfileXChannelYIssue", lpr.getProfileObisCode(), e));
                        collectedIntervalData.clear();
                        break;
                    }
                }

                collectedLoadProfile.setCollectedIntervalData(collectedIntervalData, channelInfos);
                collectedLoadProfileList.add(collectedLoadProfile);
            } else {
                CollectedLoadProfile collectedLoadProfile = MdcManager.getCollectedDataFactory().createCollectedLoadProfile(new LoadProfileIdentifierById(lpr.getLoadProfileId(), lpr.getProfileObisCode()));
                Issue<LoadProfileReader> problem = MdcManager.getIssueFactory().createWarning(lpr, "loadProfileXnotsupported", lpr.getProfileObisCode());
                collectedLoadProfile.setFailureInformation(ResultType.NotSupported, problem);
                collectedLoadProfileList.add(collectedLoadProfile);
            }
        }
        return collectedLoadProfileList;
    }

    /**
     * Merge two sets of IntervalData together.
     *
     * @param collectedIntervalData The set of IntervalData, who contains values of all channels
     * @param channelIntervalData   The set of IntervalData, only containing values for one channel
     * @return the merged set of IntervalData
     */
    private List<IntervalData> mergeChannelIntervalData(List<IntervalData> collectedIntervalData, List<IntervalData> channelIntervalData) throws IOException {
        if (collectedIntervalData.size() == 0) {
            return channelIntervalData;
        } else if (collectedIntervalData.size() == channelIntervalData.size()) {
            for (int i = 0; i < collectedIntervalData.size(); i++) {
                IntervalData collectedData = collectedIntervalData.get(i);
                IntervalData channelData = channelIntervalData.get(i);
                IntervalValue channelValue = (IntervalValue) channelData.getIntervalValues().get(0);
                collectedData.addValue(channelValue.getNumber(), channelValue.getProtocolStatus(), channelValue.getEiStatus());
            }
            return collectedIntervalData;
        } else {
            throw new IOException("Failed to merge the interval data of the different channels.");
        }
    }

    /**
     * Look for the <CODE>DeviceLoadProfileConfiguration</CODE> in the previously build up list
     *
     * @param loadProfileReader the reader linking to the <CODE>DeviceLoadProfileConfiguration</CODE>
     * @return requested configuration
     */
    private CollectedLoadProfileConfiguration getLoadProfileConfiguration(LoadProfileReader loadProfileReader) {
        for (CollectedLoadProfileConfiguration lpc : this.loadProfileConfigurationList) {
            if (loadProfileReader.getProfileObisCode().equals(lpc.getObisCode()) && loadProfileReader.getMeterSerialNumber().equalsIgnoreCase(lpc.getMeterSerialNumber())) {
                return lpc;
            }
        }
        return null;
    }

    protected StartOfGasDayParser getStartOfGasDayParser() {
        if (startOfGasDayParser == null) {
            try {
                startOfGasDayParser = new StartOfGasDayParser(meterProtocol.getRequestFactory());
            } catch (CTRException e) {
                meterProtocol.getLogger().severe("Failed to read DST parameters: " + e.getMessage());
                throw MdcManager.getComServerExceptionFactory().createUnexpectedResponse(e);
            }
        }
        return startOfGasDayParser;
    }
}
