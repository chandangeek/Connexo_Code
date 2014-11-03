package com.energyict.protocolimplv2.nta.dsmr23.profiles;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.CollectedDataFactoryProvider;
import com.energyict.mdc.protocol.api.LoadProfileConfigurationException;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.tasks.support.DeviceLoadProfileSupport;
import com.energyict.protocolimplv2.common.composedobjects.ComposedProfileConfig;
import com.energyict.protocolimplv2.dlms.DLMSProfileIntervals;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractNtaProtocol;
import com.energyict.protocols.mdc.services.impl.Bus;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.DSMRProfileIntervalStatusBits;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.CapturedRegisterObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Provides functionality to fetch and create {@link com.energyict.mdc.protocol.api.device.data.ProfileData} objects for a {@link SmartMeterProtocol}
 * <p/>
 * <pre>
 * Copyrights EnergyICT
 * Date: 3-mrt-2011
 * Time: 17:02:07
 * </pre>
 */
public class LoadProfileBuilder implements DeviceLoadProfileSupport {

    /**
     * Hardcoded ObisCode for the status of the 15min profile
     */
    protected static final ObisCode QuarterlyHourStatusObisCode = ObisCode.fromString("0.0.96.10.1.255");
    /**
     * Hardcoded ObisCode for the status of the daily profile
     */
    protected static final ObisCode DailyStatusObisCode = ObisCode.fromString("0.0.96.10.2.255");
    /**
     * Hardcoded ObisCode for the status of the hourly profile
     */
    protected static final ObisCode HourlyStatusObiscode = ObisCode.fromString("0.0.96.10.3.255");

    /**
     * The used meterProtocol
     */
    private final AbstractNtaProtocol meterProtocol;

    /**
     * Keeps track of the link between a {@link LoadProfileReader} and a {@link ComposedProfileConfig}
     */
    private Map<LoadProfileReader, ComposedProfileConfig> lpConfigMap = new HashMap<LoadProfileReader, ComposedProfileConfig>();

    /**
     * Keeps track of the link between a {@link LoadProfileReader} and a list of {@link com.energyict.mdc.protocol.api.device.data.Register} which
     * will represent the 'data' channels of the Profile
     */
    private Map<LoadProfileReader, List<CapturedRegisterObject>> capturedObjectRegisterListMap = new HashMap<LoadProfileReader, List<CapturedRegisterObject>>();

    /**
     * Keeps track of the list of <CODE>ChannelInfo</CODE> objects for all the LoadProfiles
     */
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfoMap = new HashMap<LoadProfileReader, List<ChannelInfo>>();

    /**
     * Keep track of a list of statusMask per LoadProfileReader
     */
    private Map<LoadProfileReader, Integer> statusMasksMap = new HashMap<LoadProfileReader, Integer>();

    /**
     * Keep track of a list of channelMask per LoadProfileReader
     */
    private Map<LoadProfileReader, Integer> channelMaskMap = new HashMap<LoadProfileReader, Integer>();

    /**
     * Keeps track of the link between a {@link com.energyict.mdc.protocol.api.device.data.Register} and his {@link com.energyict.dlms.DLMSAttribute} for ComposedCosemObject reads ...
     */
    private Map<CapturedRegisterObject, DLMSAttribute> registerUnitMap = new HashMap<CapturedRegisterObject, DLMSAttribute>();

    /**
     * The list of LoadProfileReaders which are expected to be fetched
     */
    private List<LoadProfileReader> expectedLoadProfileReaders;

    /**
     * The list of <CODE>DeviceLoadProfileConfiguration</CODE> objects which are build from the information from the actual device, based on the {@link #expectedLoadProfileReaders}
     */
    private List<CollectedLoadProfileConfiguration> loadProfileConfigurationList;

    /**
     * Default constructor
     *
     * @param meterProtocol the {@link #meterProtocol}
     */
    public LoadProfileBuilder(AbstractNtaProtocol meterProtocol) {
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
        this.expectedLoadProfileReaders = loadProfileReaders;
        this.loadProfileConfigurationList = new ArrayList<>();

        try {
            ComposedCosemObject ccoLpConfigs = constructLoadProfileConfigComposedCosemObject(loadProfileReaders, this.meterProtocol.supportsBulkRequests());
            List<CapturedRegisterObject> capturedObjectRegisterList = createCapturedObjectRegisterList(ccoLpConfigs);
            ComposedCosemObject ccoCapturedObjectRegisterUnits = constructCapturedObjectRegisterUnitComposedCosemObject(capturedObjectRegisterList, this.meterProtocol.supportsBulkRequests());

            for (LoadProfileReader lpr : this.expectedLoadProfileReaders) {
                this.meterProtocol.getLogger().log(Level.INFO, "Reading configuration from LoadProfile " + lpr);
                CollectedLoadProfileConfiguration lpc =
                        this.getCollectedDataFactory().
                                createCollectedLoadProfileConfiguration(lpr.getProfileObisCode(), lpr.getDeviceIdentifier());

                ComposedProfileConfig cpc = lpConfigMap.get(lpr);
                if (cpc != null) {
                    try {
                        lpc.setProfileInterval(ccoLpConfigs.getAttribute(cpc.getLoadProfileInterval()).intValue());
                        List<ChannelInfo> channelInfos = constructChannelInfos(lpr, ccoCapturedObjectRegisterUnits);
                        int statusMask = constructStatusMask(capturedObjectRegisterListMap.get(lpr));
                        int channelMask = constructChannelMask(capturedObjectRegisterListMap.get(lpr));
                        lpc.setChannelInfos(channelInfos);
                        this.channelInfoMap.put(lpr, channelInfos);
                        this.statusMasksMap.put(lpr, statusMask);
                        this.channelMaskMap.put(lpr, channelMask);
                    } catch (IOException e) {
                        lpc.setSupportedByMeter(false);
                    }
                } else {
                    lpc.setSupportedByMeter(false);
                }
                this.loadProfileConfigurationList.add(lpc);
            }
            return this.loadProfileConfigurationList;
        } catch (IOException e) {
            throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }
    }

    /**
     * Construct a <CODE>ComposedCosemObject</CODE> which will allow to read all the expected LoadProfileIntervals and -CapturedObjects
     *
     * @param loadProfileReaders  a list of definitions of expected LoadProfiles
     * @param supportsBulkRequest indication whether we may use the DLMS bulkRequest
     * @return the constructed object
     */
    protected ComposedCosemObject constructLoadProfileConfigComposedCosemObject(List<LoadProfileReader> loadProfileReaders, boolean supportsBulkRequest) {
        if (loadProfileReaders != null) {
            List<DLMSAttribute> dlmsAttributes = new ArrayList<DLMSAttribute>();
            for (LoadProfileReader lpReader : loadProfileReaders) {
                ObisCode obisCode = this.meterProtocol.getPhysicalAddressCorrectedObisCode(lpReader.getProfileObisCode(), lpReader.getMeterSerialNumber());
                UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.meterProtocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), obisCode);
                if (uo != null) {
                    ComposedProfileConfig cProfileConfig = new ComposedProfileConfig(
                            new DLMSAttribute(obisCode, DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_CAPTUREPERIOD, uo.getClassID()),
                            new DLMSAttribute(obisCode, DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_CAPTUREOBJECTS, uo.getClassID()));
                    dlmsAttributes.add(cProfileConfig.getLoadProfileInterval());
                    dlmsAttributes.add(cProfileConfig.getLoadProfileCapturedObjects());
                    this.lpConfigMap.put(lpReader, cProfileConfig);
                } else {
                    this.meterProtocol.getLogger().log(Level.INFO, "LoadProfile with ObisCode " + obisCode + " for meter " + lpReader.getDeviceIdentifier() + " is not supported.");
                }
            }
            return new ComposedCosemObject(this.meterProtocol.getDlmsSession(), supportsBulkRequest, dlmsAttributes);
        }
        return null;
    }

    /**
     * Create a list of <CODE>Registers</CODE> from all the dataChannels from all the receive CapturedObject list from all the expected LoadProfiles
     *
     * @param ccoLpConfigs provides the captured objects from the meters
     * @return a list of Registers
     * @throws java.io.IOException if an error occurred during dataFetching or -Parsing
     */
    private List<CapturedRegisterObject> createCapturedObjectRegisterList(ComposedCosemObject ccoLpConfigs) throws IOException {
        List<CapturedRegisterObject> channelRegisters = new ArrayList<CapturedRegisterObject>();
        if (this.expectedLoadProfileReaders != null) {
            for (LoadProfileReader lpr : this.expectedLoadProfileReaders) {
                ComposedProfileConfig cpc = this.lpConfigMap.get(lpr);
                if (cpc != null) {
                    // Fetch the raw captured object list from the device
                    DLMSAttribute colAttribute = cpc.getLoadProfileCapturedObjects();
                    byte[] rawCapturedObjectList = ccoLpConfigs.getAttribute(colAttribute).getBEREncodedByteArray();

                    // Store the data in a new data container, so it can be used further on
                    DataContainer dc = new DataContainer();
                    dc.parseObjectList(rawCapturedObjectList, this.meterProtocol.getLogger());

                    // Use a dummy profile generic object to get a list of CapturedObjects from the previously created data container
                    ProfileGeneric pg = new ProfileGeneric(this.meterProtocol.getDlmsSession(), null);
                    List<CapturedObject> capturedObjects = pg.getCapturedObjectsFromDataContainter(dc);

                    // Convert each captured object to a register (DLMSAttribute + device serial number)
                    List<CapturedRegisterObject> coRegisters = new ArrayList<CapturedRegisterObject>();
                    for (CapturedObject co : capturedObjects) {
                        String deviceSerialNumber = this.meterProtocol.getSerialNumberFromCorrectObisCode(co.getLogicalName().getObisCode());
//                        if ((deviceSerialNumber != null) && (!deviceSerialNumber.equals(""))) {
                        DLMSAttribute dlmsAttribute = new DLMSAttribute(co.getLogicalName().getObisCode(), co.getAttributeIndex(), co.getClassId());
                        CapturedRegisterObject reg = new CapturedRegisterObject(dlmsAttribute, deviceSerialNumber);

                        // Prepare each register only once. This way we don't get duplicate registerRequests in one getWithList
                        if (!channelRegisters.contains(reg) && isDataObisCode(reg.getObisCode(), reg.getSerialNumber())) {
                            channelRegisters.add(reg);
                        }
                        coRegisters.add(reg); // we always add it to the list of registers for this CapturedObject
//                        }
                    }
                    this.capturedObjectRegisterListMap.put(lpr, coRegisters);
                } //TODO should we log this if we didn't get it???
            }
        } else {
            throw new LoadProfileConfigurationException("ExpectedLoadProfileReaders may not be null");
        }
        return channelRegisters;
    }

    /**
     * Construct a <CODE>ComposedCosemObject</CODE> which will allow to read all the Units of the dataChannels of all expected LoadProfiles.
     *
     * @param registers           a list containing all the needed registers
     * @param supportsBulkRequest indication whether we may use the DLMS bulkRequest
     * @return the constructed <CODE>ComposedCosemObject</CODE>
     */
    private ComposedCosemObject constructCapturedObjectRegisterUnitComposedCosemObject(List<CapturedRegisterObject> registers, boolean supportsBulkRequest) {
        if (registers != null) {
            List<DLMSAttribute> dlmsAttributes = new ArrayList<DLMSAttribute>();
            for (CapturedRegisterObject register : registers) {
                ObisCode rObisCode = getCorrectedRegisterObisCode(register);

                UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.meterProtocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), rObisCode);
                if (uo != null) {
                    if (uo.getDLMSClassId() == DLMSClassId.REGISTER) {
                        DLMSAttribute unitAttribute = new DLMSAttribute(rObisCode, RegisterAttributes.SCALER_UNIT.getAttributeNumber(), uo.getClassID());
                        dlmsAttributes.add(unitAttribute);
                        this.registerUnitMap.put(register, unitAttribute);
                    } else if (uo.getDLMSClassId() == DLMSClassId.EXTENDED_REGISTER) {
                        DLMSAttribute unitAttribute = new DLMSAttribute(rObisCode, ExtendedRegisterAttributes.UNIT.getAttributeNumber(), uo.getClassID());
                        dlmsAttributes.add(unitAttribute);
                        this.registerUnitMap.put(register, unitAttribute);
                    } else if (uo.getDLMSClassId() == DLMSClassId.DEMAND_REGISTER) {
                        DLMSAttribute unitAttribute = new DLMSAttribute(rObisCode, DemandRegisterAttributes.UNIT.getAttributeNumber(), uo.getClassID());
                        dlmsAttributes.add(unitAttribute);
                        this.registerUnitMap.put(register, unitAttribute);
                    }
                } else {
                    this.meterProtocol.getLogger().log(Level.INFO, "LoadProfile channel with ObisCode " + (rObisCode != null ? rObisCode : register.getObisCode()) + " is not supported.");
                }
            }
            return new ComposedCosemObject(this.meterProtocol.getDlmsSession(), supportsBulkRequest, dlmsAttributes);
        }
        return null;
    }

    /**
     * Construct a list of <CODE>ChannelInfos</CODE>.
     * If a given register is not available, then the corresponding profile may not be fetched.
     *
     * @param ccoRegisterUnits the {@link com.energyict.dlms.cosem.ComposedCosemObject} which groups the reading of all the registers
     * @return a constructed list of <CODE>ChannelInfos</CODE>
     * @throws java.io.IOException when an error occurred during dataFetching or -Parsing
     */
    protected List<ChannelInfo> constructChannelInfos(LoadProfileReader loadProfileReader, ComposedCosemObject ccoRegisterUnits) throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        for (CapturedRegisterObject registerUnit : capturedObjectRegisterListMap.get(loadProfileReader)) {
            if (!registerUnit.getSerialNumber().equalsIgnoreCase("") && isDataObisCode(registerUnit.getObisCode(), registerUnit.getSerialNumber())) {
                if (this.registerUnitMap.containsKey(registerUnit)) {
                    ChannelInfo configuredChannelInfo = getConfiguredChannelInfo(loadProfileReader, registerUnit);
                    ScalerUnit su = new ScalerUnit(ccoRegisterUnits.getAttribute(this.registerUnitMap.get(registerUnit)));
                    if (su.getUnitCode() != 0) {
                        ChannelInfo ci = ChannelInfo.ChannelInfoBuilder.fromObisCode(registerUnit.getObisCode())
                                .meterIdentifier(registerUnit.getSerialNumber())
                                .unit(su.getEisUnit())
                                .cumulative(true)
                                .readingType(configuredChannelInfo.getReadingType()).build();
                        channelInfos.add(ci);
                    } else {
                        //TODO CHECK if this is still correct!
                        ChannelInfo ci = ChannelInfo.ChannelInfoBuilder.fromObisCode(registerUnit.getObisCode())
                                .meterIdentifier(registerUnit.getSerialNumber())
                                .unit(Unit.getUndefined())
                                .cumulative(true)
                                .readingType(configuredChannelInfo.getReadingType()).build();
                        channelInfos.add(ci);
//                        throw new LoadProfileConfigurationException("Could not fetch a correct Unit for " + registerUnit + " - unitCode was 0.");
                    }
                } else {
                    throw new LoadProfileConfigurationException("Could not fetch a correct Unit for " + registerUnit + " - not in registerUnitMap.");
                }
            }
        }
        return channelInfos;
    }

    protected ChannelInfo getConfiguredChannelInfo(LoadProfileReader loadProfileReader, Register registerUnit) throws IOException {
        for (ChannelInfo channelInfo : loadProfileReader.getChannelInfos()) {
            if (channelInfo.getChannelObisCode().equals(registerUnit.getObisCode())) {
                return channelInfo;
            }
        }
        return null;
    }

    private int constructStatusMask(List<CapturedRegisterObject> registers) {
        int statusMask = 0;
        int counter = 0;
        for (CapturedRegisterObject registerUnit : registers) {
            if (isStatusObisCode(registerUnit.getObisCode(), registerUnit.getSerialNumber())) {
                statusMask |= (int) Math.pow(2, counter);
            }
            counter++;
        }
        return statusMask;
    }

    private int constructChannelMask(List<CapturedRegisterObject> registers) {
        int channelMask = 0;
        int counter = 0;
        for (CapturedRegisterObject registerUnit : registers) {
            if (!registerUnit.getSerialNumber().equals("") && isDataObisCode(registerUnit.getObisCode(), registerUnit.getSerialNumber())) {
                channelMask |= (int) Math.pow(2, counter);
            }
            counter++;
        }
        return channelMask;
    }

    /**
     * Checks if the given ObisCode/Serialnumber combination is a valid profileChannel. Checks are done based on the the StatusObisCodes and ClockObisCode
     *
     * @param obisCode     the obiscode to check
     * @param serialNumber the serialNumber of the meter, related to the given obisCode
     * @return true if the obisCode is not a {@link com.energyict.dlms.cosem.Clock} object nor a Status object
     */
    protected boolean isDataObisCode(ObisCode obisCode, String serialNumber) {
        return !(Clock.isClockObisCode(obisCode) || isStatusObisCode(obisCode, serialNumber));
    }

    protected boolean isStatusObisCode(ObisCode obisCode, String serialNumber) {
        boolean isStatusObisCode = false;
        ObisCode testObisCode;
        if (obisCode.getB() != this.meterProtocol.getPhysicalAddressFromSerialNumber(serialNumber)) {
            return false;
        }

        testObisCode = this.meterProtocol.getPhysicalAddressCorrectedObisCode(QuarterlyHourStatusObisCode, serialNumber);
        if (testObisCode != null) {
            isStatusObisCode |= testObisCode.equals(obisCode);
        } else {
            return false;
        }

        testObisCode = this.meterProtocol.getPhysicalAddressCorrectedObisCode(DailyStatusObisCode, serialNumber);
        if (testObisCode != null) {
            isStatusObisCode |= testObisCode.equals(obisCode);
        } else {
            return false;
        }

        testObisCode = this.meterProtocol.getPhysicalAddressCorrectedObisCode(HourlyStatusObiscode, serialNumber);
        if (testObisCode != null) {
            isStatusObisCode |= testObisCode.equals(obisCode);
        } else {
            return false;
        }
        return isStatusObisCode;
    }

    /**
     * Get the corrected ObisCode from the given CapturedRegisterObject. With correction we mean changing the B-field of the ObisCode according to the logical index in the meter
     *
     * @param register the register which contains an ObisCode which needs channelCorrection
     * @return the corrected ObisCode
     */
    public ObisCode getCorrectedRegisterObisCode(CapturedRegisterObject register) {
        return this.meterProtocol.getPhysicalAddressCorrectedObisCode(register.getObisCode(), register.getSerialNumber());
    }

    /**
     * <p>
     * Fetches one or more LoadProfiles from the device. Each <CODE>LoadProfileReader</CODE> contains a list of necessary
     * channels({@link LoadProfileReader#channelInfos}) to read. If it is possible then only these channels should be read,
     * if not then all channels may be returned in the <CODE>ProfileData</CODE>.
     * </p>
     * <p>
     * <b>Implementors should throw an exception if all data since {@link LoadProfileReader#getStartReadingTime()} can NOT be fetched</b>,
     * as the collecting system will update its lastReading setting based on the returned ProfileData
     * </p>
     *
     * @param loadProfiles a list of <CODE>LoadProfileReader</CODE> which have to be read
     * @return a list of <CODE>ProfileData</CODE> objects containing interval records
     * @throws java.io.IOException if a communication or parsing error occurred
     */
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {    // ToDo: blocking issues are not caught! When a blocking issue was encountered, it makes no sense to try to read out other stuff!
        List<CollectedLoadProfile> collectedLoadProfileList = new ArrayList<CollectedLoadProfile>();
        ProfileGeneric profile;
        ProfileData profileData;

        for (LoadProfileReader lpr : loadProfiles) {
            ObisCode lpObisCode = this.meterProtocol.getPhysicalAddressCorrectedObisCode(lpr.getProfileObisCode(), lpr.getMeterSerialNumber());
            CollectedLoadProfileConfiguration lpc = getLoadProfileConfiguration(lpr);
            CollectedLoadProfile collectedLoadProfile = this.getCollectedDataFactory().createCollectedLoadProfile(lpr.getLoadProfileIdentifier());

            if (this.channelInfoMap.containsKey(lpr) && lpc != null) { // otherwise it is not supported by the meter
                this.meterProtocol.getLogger().log(Level.INFO, "Getting LoadProfile data for " + lpr + " from " + lpr.getStartReadingTime() + " to " + lpr.getEndReadingTime());

                try {
                    List<ChannelInfo> channelInfos = this.channelInfoMap.get(lpr);
                    profile = this.meterProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpObisCode);
                    profileData = new ProfileData(lpr.getLoadProfileId());
                    profileData.setChannelInfos(channelInfos);
                    Calendar fromCalendar = Calendar.getInstance(this.meterProtocol.getTimeZone());
                    fromCalendar.setTime(lpr.getStartReadingTime());
                    Calendar toCalendar = Calendar.getInstance(this.meterProtocol.getTimeZone());
                    toCalendar.setTime(lpr.getEndReadingTime());

                    //TODO it is possible that we need to check for the masks ...
                    DLMSProfileIntervals intervals = new DLMSProfileIntervals(profile.getBufferData(fromCalendar, toCalendar), DLMSProfileIntervals.DefaultClockMask,
                            this.statusMasksMap.get(lpr), this.channelMaskMap.get(lpr), new DSMRProfileIntervalStatusBits());
                    List<IntervalData> collectedIntervalData = intervals.parseIntervals(lpc.getProfileInterval());

                    collectedLoadProfile.setCollectedData(collectedIntervalData, channelInfos);
                } catch (IOException e) {
                    Issue problem = Bus.getIssueService().newIssueCollector().addProblem(lpr, "loadProfileXIssue", lpr.getProfileObisCode(), e);
                    collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                }
            } else {
                Issue problem = Bus.getIssueService().newIssueCollector().addProblem(lpr, "loadProfileXnotsupported", lpr.getProfileObisCode());
                collectedLoadProfile.setFailureInformation(ResultType.NotSupported, problem);
            }
            collectedLoadProfileList.add(collectedLoadProfile);
        }
        return collectedLoadProfileList;
    }

    /**
     * Look for the <CODE>DeviceLoadProfileConfiguration</CODE> in the previously build up list
     *
     * @param loadProfileReader the reader linking to the <CODE>DeviceLoadProfileConfiguration</CODE>
     * @return requested configuration
     */
    protected CollectedLoadProfileConfiguration getLoadProfileConfiguration(LoadProfileReader loadProfileReader) {
        for (CollectedLoadProfileConfiguration lpc : this.loadProfileConfigurationList) {
            if (loadProfileReader.getProfileObisCode().equals(lpc.getObisCode()) && loadProfileReader.getDeviceIdentifier().getIdentifier().equals(lpc.getDeviceIdentifier().getIdentifier())) {
                return lpc;
            }
        }
        return null;
    }

    @Override
    public Date getTime() {
        return meterProtocol.getTime();
    }

    protected Map<LoadProfileReader, List<ChannelInfo>> getChannelInfoMap() {
        return channelInfoMap;
    }

    protected Map<LoadProfileReader, Integer> getStatusMasksMap() {
        return statusMasksMap;
    }

    protected AbstractNtaProtocol getMeterProtocol() {
        return meterProtocol;
    }

    private CollectedDataFactory getCollectedDataFactory() {
        return CollectedDataFactoryProvider.instance.get().getCollectedDataFactory();
    }

}