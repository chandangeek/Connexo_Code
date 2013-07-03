package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.energyict.cbo.Unit;
import com.energyict.mdc.exceptions.ComServerExecutionException;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.profile.ProfileChannel;
import com.energyict.protocolimplv2.elster.ctr.MTU155.profile.StartOfGasDayParser;
import com.energyict.protocolimplv2.elster.ctr.MTU155.util.CTRObjectInfo;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierByObisCodeAndDevice;

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

    private MTU155 meterProtocol;

    /**
     * The list of LoadProfileReaders which are expected to be fetched
     */
    private List<LoadProfileReader> expectedLoadProfileReaders;

    /**
     * The list of <CODE>LoadProfileConfiguration</CODE> objects which are build from the information from the actual device, based on the {@link #expectedLoadProfileReaders}
     */
    private List<LoadProfileConfiguration> loadProfileConfigurationList;

    /**
     * Keeps track of the list of <CODE>ChannelInfo</CODE> objects for all the LoadProfiles
     */
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfoMap = new HashMap<>();

    /**
     * The {@link StartOfGasDayParser} to use
     */
    protected StartOfGasDayParser startOfGasDayParser;

    public LoadProfileBuilder(MTU155 meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    /**
     * Get the configuration(interval, number of channels, channelUnits) of all given LoadProfiles for the {@link #meterProtocol}
     *
     * @param loadProfileReaders a list of definitions of expected loadProfiles to read
     * @return the list of <CODE>LoadProfileConfiguration</CODE> objects which are in the device
     * @throws java.io.IOException when error occurred during dataFetching or -Parsing
     */
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) {
        expectedLoadProfileReaders = loadProfileReaders;
        loadProfileConfigurationList = new ArrayList<>();

        for (LoadProfileReader lpr : expectedLoadProfileReaders) {
            this.meterProtocol.getLogger().log(Level.INFO, "Reading configuration from LoadProfile " + lpr);
            LoadProfileConfiguration lpc = new LoadProfileConfiguration(lpr.getProfileObisCode(), meterProtocol.getOfflineDevice().getSerialNumber());

            try {
                List<ChannelInfo> channelInfos = constructChannelInfos(lpr);
                lpc.setChannelInfos(channelInfos);
                lpc.setProfileInterval(getProfileInterval(lpr.getProfileObisCode()));

                if (! channelInfoMap.containsKey(lpr)){
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
                CTRObjectID objectId = ctrRegisterMapping.getObjectId();
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
        /** While reading one of the loadProfiles, a blocking issue was encountered; this indicates it makes no sense to try to read out the other loadProfiles **/
        boolean blockingIssueEncountered = false;
        /** The blocking Communication Exception **/
        ComServerExecutionException blockingIssue = null;

        List<CollectedLoadProfile> collectedLoadProfileList = new ArrayList<>();
        for (LoadProfileReader lpr : loadProfiles) {
            LoadProfileConfiguration lpc = getLoadProfileConfiguration(lpr);
            if (!blockingIssueEncountered) {
                if (this.channelInfoMap.containsKey(lpr) && lpc != null) { // otherwise it is not supported by the meter
                    List<ChannelInfo> channelInfos = this.channelInfoMap.get(lpr);
                    LoadProfileIdentifier loadProfileIdentifier = new LoadProfileIdentifierByObisCodeAndDevice(lpc.getObisCode(), new DeviceIdentifierBySerialNumber(lpr.getMeterSerialNumber()));
                    CollectedLoadProfile collectedLoadProfile = MdcManager.getCollectedDataFactory().createCollectedLoadProfile(loadProfileIdentifier);
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
                            Issue<LoadProfileReader> problem = MdcManager.getIssueCollector().addProblem(lpr, "loadProfileXChannelYIssue", lpr.getProfileObisCode(), e);
                            collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                            collectedIntervalData.clear();
                            break;
                        } catch (ComServerExecutionException e) {   // A blocking issue which blocks the whole communication (e.g.: a timeout)
                            blockingIssueEncountered = true;
                            blockingIssue = e;
                            Issue<LoadProfileReader> problem = MdcManager.getIssueCollector().addProblem(lpr, "loadProfileXBlockingIssue", lpr.getProfileObisCode(), e.getCause());
                            collectedLoadProfile.setFailureInformation(ResultType.DataIncomplete, problem);
                            collectedIntervalData.clear();
                            break;
                        }
                    }

                    collectedLoadProfile.setCollectedData(collectedIntervalData, channelInfos);
                    collectedLoadProfileList.add(collectedLoadProfile);
                } else {
                    LoadProfileIdentifier loadProfileIdentifier = new LoadProfileIdentifierByObisCodeAndDevice(lpc.getObisCode(), new DeviceIdentifierBySerialNumber(lpr.getMeterSerialNumber()));
                    CollectedLoadProfile collectedLoadProfile = MdcManager.getCollectedDataFactory().createCollectedLoadProfile(loadProfileIdentifier);
                    Issue<LoadProfileReader> problem = MdcManager.getIssueCollector().addProblem(lpr, "loadProfileXnotsupported", lpr.getProfileObisCode());
                    collectedLoadProfile.setFailureInformation(ResultType.NotSupported, problem);
                    collectedLoadProfileList.add(collectedLoadProfile);
                }
            } else {
                LoadProfileIdentifier loadProfileIdentifier = new LoadProfileIdentifierByObisCodeAndDevice(lpc.getObisCode(), new DeviceIdentifierBySerialNumber(lpr.getMeterSerialNumber()));
                CollectedLoadProfile collectedLoadProfile = MdcManager.getCollectedDataFactory().createCollectedLoadProfile(loadProfileIdentifier);
                CTRException cause = (CTRException) blockingIssue.getCause();
                Issue<LoadProfileReader> problem = MdcManager.getIssueCollector().addProblem(lpr, "loadProfileXBlockingIssue", lpr.getProfileObisCode(), cause);
                collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                collectedLoadProfileList.add(collectedLoadProfile);
            }
        }
        return collectedLoadProfileList;
    }

    /**
     * Merge two sets of IntervalData together.
     *
     * @param collectedIntervalData The set of IntervalData, who contains values of all channels
     * @param channelIntervalData The set of IntervalData, only containing values for one channel
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
     * Look for the <CODE>LoadProfileConfiguration</CODE> in the previously build up list
     *
     * @param loadProfileReader the reader linking to the <CODE>LoadProfileConfiguration</CODE>
     * @return requested configuration
     */
    private LoadProfileConfiguration getLoadProfileConfiguration(LoadProfileReader loadProfileReader) {
        for (LoadProfileConfiguration lpc : this.loadProfileConfigurationList) {
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
                throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(e);
            }
        }
        return startOfGasDayParser;
    }
}