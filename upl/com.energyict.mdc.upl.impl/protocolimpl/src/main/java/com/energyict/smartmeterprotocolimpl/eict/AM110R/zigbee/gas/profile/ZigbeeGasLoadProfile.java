package com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.profile;

import com.energyict.cbo.Unit;
import com.energyict.dlms.*;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.Register;
import com.energyict.smartmeterprotocolimpl.common.composedobjects.ComposedProfileConfig;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.ObisCodeProvider;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.gas.ZigbeeGas;

import java.io.IOException;
import java.util.*;
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
public class ZigbeeGasLoadProfile {

    /**
     * The used meterProtocol
     */
    private final ZigbeeGas zigbeeGas;

    /**
     * Keeps track of the link between a {@link com.energyict.protocol.LoadProfileReader} and a {@link com.energyict.smartmeterprotocolimpl.common.composedobjects.ComposedProfileConfig}
     */
    private Map<LoadProfileReader, ComposedProfileConfig> lpConfigMap = new HashMap<LoadProfileReader, ComposedProfileConfig>();

    /**
     * Keeps track of the link between a {@link com.energyict.protocol.LoadProfileReader} and a list of {@link com.energyict.protocol.Register} which
     * will represent the 'data' channels of the Profile
     */
    private Map<LoadProfileReader, List<Register>> capturedObjectRegisterListMap = new HashMap<LoadProfileReader, List<Register>>();

    private Map<LoadProfileReader, List<Integer>> maskMask = new HashMap<LoadProfileReader, List<Integer>>();

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
     * @param zigbeeGas the {@link #zigbeeGas}
     */
    public ZigbeeGasLoadProfile(ZigbeeGas zigbeeGas) {
        this.zigbeeGas = zigbeeGas;
    }

    /**
     * Get the configuration(interval, number of channels, channelUnits) of all given LoadProfiles for the {@link #zigbeeGas}
     *
     * @param loadProfileReaders a list of definitions of expected loadProfiles to read
     * @return the list of <CODE>LoadProfileConfiguration</CODE> objects which are in the device
     * @throws java.io.IOException when error occurred during dataFetching or -Parsing
     */
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) throws IOException {
        this.expectedLoadProfileReaders = loadProfileReaders;
        this.loadProfileConfigurationList = new ArrayList<LoadProfileConfiguration>();

        ComposedCosemObject ccoLpConfigs = constructLoadProfileConfigComposedCosemObject(loadProfileReaders, this.zigbeeGas.supportsBulkRequests());
        List<Register> capturedObjectRegisterList = createCapturedObjectRegisterList(ccoLpConfigs);
        ComposedCosemObject ccoCapturedObjectRegisterUnits = constructCapturedObjectRegisterUnitComposedCosemObject(capturedObjectRegisterList, this.zigbeeGas.supportsBulkRequests());

        for (LoadProfileReader lpr : this.expectedLoadProfileReaders) {
            this.zigbeeGas.getLogger().log(Level.INFO, "Reading configuration from LoadProfile " + lpr);
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
                ObisCode obisCode = lpReader.getProfileObisCode();
                UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.zigbeeGas.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), obisCode);
                if (uo != null) {
                    ComposedProfileConfig cProfileConfig = new ComposedProfileConfig(
                            new DLMSAttribute(obisCode, DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_CAPTUREPERIOD, uo.getClassID()),
                            new DLMSAttribute(obisCode, DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_CAPTUREOBJECTS, uo.getClassID()));
                    dlmsAttributes.add(cProfileConfig.getLoadProfileInterval());
                    dlmsAttributes.add(cProfileConfig.getLoadProfileCapturedObjects());
                    this.lpConfigMap.put(lpReader, cProfileConfig);
                } else {
                    this.zigbeeGas.getLogger().log(Level.INFO, "LoadProfile with ObisCode " + obisCode + " for meter " + lpReader.getMeterSerialNumber() + " is not supported.");
                }
            }
            return new ComposedCosemObject(this.zigbeeGas.getDlmsSession(), supportsBulkRequest, dlmsAttributes);
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
    private List<Register> createCapturedObjectRegisterList(ComposedCosemObject ccoLpConfigs) throws IOException {
        List<Register> channelRegisters = new ArrayList<Register>();
        if (this.expectedLoadProfileReaders != null) {
            for (LoadProfileReader lpr : this.expectedLoadProfileReaders) {
                ComposedProfileConfig cpc = this.lpConfigMap.get(lpr);
                if (cpc != null) {
                    DataContainer dc = new DataContainer();
                    dc.parseObjectList(ccoLpConfigs.getAttribute(cpc.getLoadProfileCapturedObjects()).getBEREncodedByteArray(), this.zigbeeGas.getLogger());
                    ProfileGeneric pg = new ProfileGeneric(this.zigbeeGas.getDlmsSession(), null);
                    List<CapturedObject> capturedObjects = pg.getCapturedObjectsFromDataContainter(dc);
                    List<Register> coRegisters = new ArrayList<Register>();
                    int index = -1;
                    int clockMask = 0;
                    int statusMask = 0;
                    int channelMask = 0;
                    for (CapturedObject co : capturedObjects) {
                        index++;
                        final ObisCode obisCode = co.getLogicalName().getObisCode();
                        if (isClockObisCode(obisCode)) {
                            clockMask = (int) Math.pow(2, index);
                        } else if (isStatusObisCode(obisCode)) {
                            statusMask = (int) Math.pow(2, index);
                        }   else if (loadProfileContains(lpr, obisCode)) {
                            channelMask += (int) Math.pow(2, index);
                            Register reg = new Register(-1, obisCode, this.zigbeeGas.getProperties().getSerialNumber());
                            if (!channelRegisters.contains(reg) && isDataObisCode(reg.getObisCode())) {// this way we don't get duplicate registerRequests in one getWithList
                                channelRegisters.add(reg);
                            }
                            coRegisters.add(reg); // we always add it to the list of registers for this CapturedObject
                        }
                    }
                    this.capturedObjectRegisterListMap.put(lpr, coRegisters);

                    List<Integer> maskList = new ArrayList<Integer>(3);
                    maskList.add(clockMask);
                    maskList.add(statusMask);
                    maskList.add(channelMask);
                    this.maskMask.put(lpr, maskList);
                }
            }
        } else {
            throw new LoadProfileConfigurationException("ExpectedLoadProfileReaders may not be null");
        }
        return channelRegisters;
    }

    private boolean loadProfileContains(LoadProfileReader lpr, ObisCode obisCode) throws IOException {
        for (ChannelInfo channelInfo : lpr.getChannelInfos()) {
            if (channelInfo.getChannelObisCode().equals(obisCode)) {
                return true;
            }
        }
        return false;
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
                ObisCode rObisCode = register.getObisCode();

                UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.zigbeeGas.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), rObisCode);
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
                    this.zigbeeGas.getLogger().log(Level.INFO, "Register with ObisCode " + rObisCode + " is not supported.");
                }
            }
            return new ComposedCosemObject(this.zigbeeGas.getDlmsSession(), supportsBulkRequest, dlmsAttributes);
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
     * @throws java.io.IOException when an error occurred during dataFetching or -Parsing
     */
    private List<ChannelInfo> constructChannelInfos(List<Register> registers, ComposedCosemObject ccoRegisterUnits) throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        for (Register registerUnit : registers) {
            if (isDataObisCode(registerUnit.getObisCode())) {
                if (this.registerUnitMap.containsKey(registerUnit)) {
                    ScalerUnit su = new ScalerUnit(ccoRegisterUnits.getAttribute(this.registerUnitMap.get(registerUnit)));
                    if (su.getUnitCode() != 0) {
                        ChannelInfo ci = new ChannelInfo(channelInfos.size(), registerUnit.getObisCode().toString(), su.getEisUnit(), registerUnit.getSerialNumber(), true);
                        channelInfos.add(ci);
                    } else {
                        ChannelInfo ci = new ChannelInfo(channelInfos.size(), registerUnit.getObisCode().toString(), Unit.getUndefined(), registerUnit.getSerialNumber(), true);
                        channelInfos.add(ci);
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
     * @param obisCode the obiscode to check
     * @return true if the obisCode is not a {@link com.energyict.dlms.cosem.Clock} object nor a Status object
     */
    protected boolean isDataObisCode(ObisCode obisCode) {
        return !(Clock.getDefaultObisCode().equals(obisCode) || obisCode.equalsIgnoreBChannel(ObisCodeProvider.GENERAL_LP_STATUS_OBISCODE));
    }

    protected boolean isClockObisCode(ObisCode obisCode) {
        return Clock.getDefaultObisCode().equals(obisCode);
    }

    protected boolean isStatusObisCode(ObisCode obisCode) {
        return obisCode.equalsIgnoreBChannel(ObisCodeProvider.GENERAL_LP_STATUS_OBISCODE);
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
     * @return a list of <CODE>ProfileData</CODE> objects containing interval records
     * @throws java.io.IOException if a communication or parsing error occurred
     */
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        List<ProfileData> profileDataList = new ArrayList<ProfileData>();
        ProfileGeneric profile;
        ProfileData profileData;
        for (LoadProfileReader lpr : loadProfiles) {
            ObisCode lpObisCode = lpr.getProfileObisCode();
            LoadProfileConfiguration lpc = getLoadProfileConfiguration(lpr);
            if (this.channelInfoMap.containsKey(lpr) && lpc != null) { // otherwise it is not supported by the meter
                this.zigbeeGas.getLogger().log(Level.INFO, "Getting LoadProfile data for " + lpr + " from " + lpr.getStartReadingTime() + " to " + lpr.getEndReadingTime());
                profile = this.zigbeeGas.getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpObisCode);
                profileData = new ProfileData(lpr.getLoadProfileId());
                profileData.setChannelInfos(this.channelInfoMap.get(lpr));
                Calendar fromCalendar = Calendar.getInstance(this.zigbeeGas.getTimeZone());
                fromCalendar.setTime(lpr.getStartReadingTime());
                Calendar toCalendar = Calendar.getInstance(this.zigbeeGas.getTimeZone());
                toCalendar.setTime(lpr.getEndReadingTime());

                List<Integer> maskList = this.maskMask.get(lpr);
                ZigbeeGasDLMSProfileIntervals intervals = new ZigbeeGasDLMSProfileIntervals(profile.getBufferData(fromCalendar, toCalendar),  maskList.get(0), maskList.get(1), maskList.get(2), null);
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
