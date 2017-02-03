package com.energyict.smartmeterprotocolimpl.eict.webrtuz3.profiles;

import com.energyict.cbo.Unit;
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
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.mdc.upl.LoadProfileConfigurationException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.Register;
import com.energyict.protocolimpl.dlms.DLMSProfileIntervals;
import com.energyict.smartmeterprotocolimpl.common.composedobjects.ComposedProfileConfig;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.WebRTUZ3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Provides functionality to fetch and create {@link com.energyict.protocol.ProfileData} objects for a {@link com.energyict.protocol.SmartMeterProtocol}
 * <p/>
 * <pre>
 * Copyrights EnergyICT
 * Date: 3-mrt-2011
 * Time: 17:02:07
 * </pre>
 */
public class LoadProfileBuilder {

    /**
     * Hardcoded ObisCode for the status of an Emeter profile
     */
    protected static final ObisCode EmeterStatusObisCode = ObisCode.fromString("0.0.96.10.1.255");
    /**
     * Hardcoded ObisCode for the status of an Mbus profile
     */
    protected static final ObisCode MbusMeterStatusObisCode = ObisCode.fromString("0.0.96.10.3.255");

    /**
     * The used meterProtocol
     */
    private final WebRTUZ3 meterProtocol;

    /**
     * Keeps track of the link between a {@link com.energyict.protocol.LoadProfileReader} and a {@link com.energyict.smartmeterprotocolimpl.common.composedobjects.ComposedProfileConfig}
     */
    private Map<LoadProfileReader, ComposedProfileConfig> lpConfigMap = new HashMap<LoadProfileReader, ComposedProfileConfig>();

    /**
     * Keeps track of the link between a {@link com.energyict.protocol.LoadProfileReader} and a list of {@link com.energyict.protocol.Register} which
     * will represent the 'data' channels of the Profile
     */
    private Map<LoadProfileReader, List<Register>> capturedObjectRegisterListMap = new HashMap<LoadProfileReader, List<Register>>();

    /**
     * Keeps track of the list of <CODE>ChannelInfo</CODE> objects for all the LoadProfiles
     */
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfoMap = new HashMap<LoadProfileReader, List<ChannelInfo>>();

    /**
     * Keeps track of the link between a {@link com.energyict.protocol.Register} and his {@link com.energyict.dlms.DLMSAttribute} for ComposedCosemObject reads ...
     */
    private Map<Register, DLMSAttribute> registerUnitMap = new HashMap<Register, DLMSAttribute>();

    /**
     * The list of LoadProfileReaders which are expected to be fetched
     */
    private List<LoadProfileReader> expectedLoadProfileReaders;

    /**
     * The list of <CODE>LoadProfileConfiguration</CODE> objects which are build from the information from the actual device, based on the {@link #expectedLoadProfileReaders}
     */
    private List<LoadProfileConfiguration> loadProfileConfigurationList;

    /**
     * Default constructor
     *
     * @param meterProtocol the {@link #meterProtocol}
     */
    public LoadProfileBuilder(WebRTUZ3 meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    /**
     * Get the configuration(interval, number of channels, channelUnits) of all given LoadProfiles for the {@link #meterProtocol}
     *
     * @param loadProfileReaders a list of definitions of expected loadProfiles to read
     * @return the list of <CODE>LoadProfileConfiguration</CODE> objects which are in the device
     * @throws IOException when error occurred during dataFetching or -Parsing
     */
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) throws IOException {
        this.expectedLoadProfileReaders = loadProfileReaders;
        this.loadProfileConfigurationList = new ArrayList<LoadProfileConfiguration>();

        ComposedCosemObject ccoLpConfigs = constructLoadProfileConfigComposedCosemObject(loadProfileReaders, this.meterProtocol.supportsBulkRequests());
        List<Register> capturedObjectRegisterList = createCapturedObjectRegisterList(ccoLpConfigs);
        ComposedCosemObject ccoCapturedObjectRegisterUnits = constructCapturedObjectRegisterUnitComposedCosemObject(capturedObjectRegisterList, this.meterProtocol.supportsBulkRequests());

        for (LoadProfileReader lpr : this.expectedLoadProfileReaders) {
            LoadProfileConfiguration lpc = new LoadProfileConfiguration(lpr.getProfileObisCode(), lpr.getMeterSerialNumber());

            ComposedProfileConfig cpc = lpConfigMap.get(lpr);
            if (cpc != null) {
                try {
                    lpc.setProfileInterval(ccoLpConfigs.getAttribute(cpc.getLoadProfileInterval()).intValue());
                    List<ChannelInfo> channelInfos = constructChannelInfos(capturedObjectRegisterListMap.get(lpr), ccoCapturedObjectRegisterUnits);
                    lpc.setChannelInfos(channelInfos);
                    this.channelInfoMap.put(lpr, channelInfos);
                } catch (IOException e) {
                    lpc.setSupportedByMeter(false);
                }
            } else {
                lpc.setSupportedByMeter(false);
            }
            this.loadProfileConfigurationList.add(lpc);
        }
        return this.loadProfileConfigurationList;
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
                    this.meterProtocol.getLogger().log(Level.INFO, "LoadProfile with ObisCode " + obisCode + " for meter " + lpReader.getMeterSerialNumber() + " is not supported.");
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
     * @throws IOException if an error occurred during dataFetching or -Parsing
     */
    private List<Register> createCapturedObjectRegisterList(ComposedCosemObject ccoLpConfigs) throws IOException {
        List<Register> channelRegisters = new ArrayList<Register>();
        if (this.expectedLoadProfileReaders != null) {
            for (LoadProfileReader lpr : this.expectedLoadProfileReaders) {
                ComposedProfileConfig cpc = this.lpConfigMap.get(lpr);
                if (cpc != null) {
                    DataContainer dc = new DataContainer();
                    dc.parseObjectList(ccoLpConfigs.getAttribute(cpc.getLoadProfileCapturedObjects()).getBEREncodedByteArray(), this.meterProtocol.getLogger());
                    ProfileGeneric pg = new ProfileGeneric(this.meterProtocol.getDlmsSession(), null);
                    List<CapturedObject> capturedObjects = pg.getCapturedObjectsFromDataContainter(dc);
                    List<Register> coRegisters = new ArrayList<Register>();
                    for (CapturedObject co : capturedObjects) {
                        Register reg = new Register(-1, co.getLogicalName().getObisCode(), this.meterProtocol.getSerialNumberFromCorrectObisCode(co.getLogicalName().getObisCode()));
                        if (!channelRegisters.contains(reg) && isDataObisCode(reg.getObisCode(), reg.getSerialNumber())) {// this way we don't get duplicate registerRequests in one getWithList
                            channelRegisters.add(reg);
                        }
                        coRegisters.add(reg); // we always add it to the list of registers for this CapturedObject
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
    private ComposedCosemObject constructCapturedObjectRegisterUnitComposedCosemObject(List<Register> registers, boolean supportsBulkRequest) {
        if (registers != null) {
            List<DLMSAttribute> dlmsAttributes = new ArrayList<DLMSAttribute>();
            for (Register register : registers) {
                ObisCode rObisCode = getCorrectedRegisterObisCode(register);

                UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.meterProtocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), rObisCode);
                if (uo != null) {
                    //TODO do we need to check the classId???
                    if (uo.getClassID() == DLMSClassId.REGISTER.getClassId() || uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                        DLMSAttribute registerUnit = new DLMSAttribute(rObisCode, RegisterAttributes.SCALER_UNIT.getAttributeNumber(), uo.getClassID());
                        dlmsAttributes.add(registerUnit);
                        this.registerUnitMap.put(register, registerUnit);
                    }
                } else {
                    this.meterProtocol.getLogger().log(Level.INFO, "Register with ObisCode " + rObisCode + " is not supported.");
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
     * @param registers        a list or <CODE>Registers</CODE> which have to be converted to a list of <CODE>ChannelInfos</CODE>
     * @param ccoRegisterUnits the {@link com.energyict.dlms.cosem.ComposedCosemObject} which groups the reading of all the registers
     * @return a constructed list of <CODE>ChannelInfos</CODE>
     * @throws IOException when an error occurred during dataFetching or -Parsing
     */
    private List<ChannelInfo> constructChannelInfos(List<Register> registers, ComposedCosemObject ccoRegisterUnits) throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        for (Register registerUnit : registers) {
            if (isDataObisCode(registerUnit.getObisCode(), registerUnit.getSerialNumber())) {
                if (this.registerUnitMap.containsKey(registerUnit)) {
                    ScalerUnit su = new ScalerUnit(ccoRegisterUnits.getAttribute(this.registerUnitMap.get(registerUnit)));
                    if (su.getUnitCode() != 0) {
                        ChannelInfo ci = new ChannelInfo(channelInfos.size(), registerUnit.getObisCode().toString(), su.getEisUnit(), registerUnit.getSerialNumber(), true);
                        channelInfos.add(ci);
                    } else {
                        //TODO CHECK if this is still correct!
                        ChannelInfo ci = new ChannelInfo(channelInfos.size(), registerUnit.getObisCode().toString(), Unit.getUndefined(), registerUnit.getSerialNumber(), true);
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

    /**
     * Checks if the given ObisCode/Serialnumber combination is a valid profileChannel. Checks are done based on the the StatusObisCodes and ClockObisCode
     *
     * @param obisCode     the obiscode to check
     * @param serialNumber the serialNumber of the meter, related to the given obisCode
     * @return true if the obisCode is not a {@link com.energyict.dlms.cosem.Clock} object nor a Status object
     */
    protected boolean isDataObisCode(ObisCode obisCode, String serialNumber) {
        boolean isDataObisCode = true;
        ObisCode testObisCode;

        if (obisCode.getB() != this.meterProtocol.getPhysicalAddressFromSerialNumber(serialNumber)) {
            return false;
        }

        testObisCode = this.meterProtocol.getPhysicalAddressCorrectedObisCode(EmeterStatusObisCode, serialNumber);
        if (testObisCode != null) {
            isDataObisCode &= !testObisCode.equals(obisCode);
        } else {
            return false;
        }

        testObisCode = this.meterProtocol.getPhysicalAddressCorrectedObisCode(MbusMeterStatusObisCode, serialNumber);
        if (testObisCode != null) {
            isDataObisCode &= !testObisCode.equals(obisCode);
        } else {
            return false;
        }

        return !Clock.getDefaultObisCode().equals(obisCode) && isDataObisCode;
    }

    /**
     * Get the corrected ObisCode from the given Register. With correction we mean changing the B-field of the ObisCode according to the logical index in the meter
     *
     * @param register the register which contains an ObisCode which needs channelCorrection
     * @return the corrected ObisCode
     */
    public ObisCode getCorrectedRegisterObisCode(Register register) {
        return this.meterProtocol.getPhysicalAddressCorrectedObisCode(register.getObisCode(), register.getSerialNumber());
    }

    /**
     * <p>
     * Fetches one or more LoadProfiles from the device. Each <CODE>LoadProfileReader</CODE> contains a list of necessary
     * channels({@link com.energyict.protocol.LoadProfileReader#channelInfos}) to read. If it is possible then only these channels should be read,
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
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        List<ProfileData> profileDataList = new ArrayList<ProfileData>();
        ProfileGeneric profile;
        ProfileData profileData;
        for (LoadProfileReader lpr : loadProfiles) {
            ObisCode lpObisCode = this.meterProtocol.getPhysicalAddressCorrectedObisCode(lpr.getProfileObisCode(), lpr.getMeterSerialNumber());
            LoadProfileConfiguration lpc = getLoadProfileConfiguration(lpr);
            if (this.channelInfoMap.containsKey(lpr) && lpc != null) { // otherwise it is not supported by the meter
                profile = this.meterProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpObisCode);
                profileData = new ProfileData(lpr.getLoadProfileId());
                profileData.setChannelInfos(this.channelInfoMap.get(lpr));
                Calendar fromCalendar = Calendar.getInstance(this.meterProtocol.getTimeZone());
                fromCalendar.setTime(lpr.getStartReadingTime());
                Calendar toCalendar = Calendar.getInstance(this.meterProtocol.getTimeZone());
                toCalendar.setTime(lpr.getEndReadingTime());

                //TODO it is possible that we need to check for the masks ...
                DLMSProfileIntervals intervals = new DLMSProfileIntervals(profile.getBufferData(fromCalendar, toCalendar), null);
                profileData.setIntervalDatas(intervals.parseIntervals(lpc.getProfileInterval()));

                profileDataList.add(profileData);
            }
        }

        return profileDataList;
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

}
