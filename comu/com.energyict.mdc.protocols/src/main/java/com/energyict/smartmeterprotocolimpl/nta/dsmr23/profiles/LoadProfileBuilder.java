/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.LoadProfileConfiguration;
import com.energyict.mdc.protocol.api.LoadProfileConfigurationException;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.ProfileData;

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
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.protocolimpl.dlms.DLMSProfileIntervals;
import com.energyict.smartmeterprotocolimpl.common.composedobjects.ComposedProfileConfig;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.DSMRProfileIntervalStatusBits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class LoadProfileBuilder {

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
    private final AbstractSmartNtaProtocol meterProtocol;

    /**
     * Keeps track of the link between a {@link LoadProfileReader} and a {@link com.energyict.smartmeterprotocolimpl.common.composedobjects.ComposedProfileConfig}
     */
    private Map<LoadProfileReader, ComposedProfileConfig> lpConfigMap = new HashMap<>();

    /**
     * Keeps track of the link between a LoadProfileReader and a {@link com.energyict.smartmeterprotocolimpl.common.composedobjects.ComposedProfileConfig}
     */
    private Map<LoadProfileReader, Integer> lpIntervals = new HashMap<>();

    /**
     * Keeps track of the link between a LoadProfileReader and a list of Register which
     * will represent the 'data' channels of the Profile
     */
    private Map<LoadProfileReader, List<CapturedRegisterObject>> capturedObjectRegisterListMap = new HashMap<>();

    /**
     * Keeps track of the list of <CODE>ChannelInfo</CODE> objects for all the LoadProfiles
     */
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfoMap = new HashMap<>();

    /**
     * Keep track of a list of statusMask per LoadProfileReader
     */
    private Map<LoadProfileReader, Integer> statusMasksMap = new HashMap<>();

    /**
     * Keep track of a list of channelMask per LoadProfileReader
     */
    protected Map<LoadProfileReader, Integer> channelMaskMap = new HashMap<>();

    /**
     * Keeps track of the link between a {@link com.energyict.mdc.protocol.api.device.BaseRegister}
     * and his {@link com.energyict.dlms.DLMSAttribute} for ComposedCosemObject reads ...
     */
    private Map<CapturedRegisterObject, DLMSAttribute> registerUnitMap = new HashMap<>();

    /**
     * The list of LoadProfileReaders which are expected to be fetched
     */
    private List<LoadProfileReader> expectedLoadProfileReaders;

    /**
     * The list of <CODE>LoadProfileConfiguration</CODE> objects which are build from the information from the actual device, based on the {@link #expectedLoadProfileReaders}
     */
    private List<LoadProfileConfiguration> loadProfileConfigurationList;

    public LoadProfileBuilder(AbstractSmartNtaProtocol meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    /**
     * Get the configuration(interval, number of channels, channelUnits) of all given LoadProfiles for the {@link #meterProtocol}
     *
     * @param loadProfileReaders a list of definitions of expected loadProfiles to read
     * @return the list of <CODE>LoadProfileConfiguration</CODE> objects which are in the device
     * @throws java.io.IOException when error occurred during dataFetching or -Parsing
     */
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) throws IOException {
        this.expectedLoadProfileReaders = filterOutAllInvalidLoadProfiles(loadProfileReaders);
        this.loadProfileConfigurationList = new ArrayList<>();

        ComposedCosemObject ccoLpConfigs = constructLoadProfileConfigComposedCosemObject(expectedLoadProfileReaders, this.meterProtocol.supportsBulkRequests());
        List<CapturedRegisterObject> capturedObjectRegisterList = createCapturedObjectRegisterList(ccoLpConfigs);
        ComposedCosemObject ccoCapturedObjectRegisterUnits = constructCapturedObjectRegisterUnitComposedCosemObject(capturedObjectRegisterList, this.meterProtocol.supportsBulkRequests());

        for (LoadProfileReader lpr : loadProfileReaders) {
            LoadProfileConfiguration lpc = new LoadProfileConfiguration(lpr.getProfileObisCode(), lpr.getDeviceIdentifier());
            if (!expectedLoadProfileReaders.contains(lpr)) {      //Invalid LP, mark as not supported and move on to the next LP
                lpc.setSupportedByMeter(false);
                continue;
            }

            this.meterProtocol.getLogger().log(Level.INFO, "Reading configuration from LoadProfile " + lpr);
            ComposedProfileConfig cpc = lpConfigMap.get(lpr);
            if (cpc != null) {
                try {
                    int profileInterval = readLoadProfileInterval(ccoLpConfigs, cpc);
                    lpIntervals.put(lpr, profileInterval);
                    lpc.setProfileInterval(profileInterval);
                    List<ChannelInfo> channelInfos = constructChannelInfos(capturedObjectRegisterListMap.get(lpr), ccoCapturedObjectRegisterUnits, lpr.getChannelInfos());
                    int statusMask = constructStatusMask(capturedObjectRegisterListMap.get(lpr));
                    int channelMask = constructChannelMask(capturedObjectRegisterListMap.get(lpr));
                    lpc.setChannelInfos(channelInfos);
                    this.channelInfoMap.put(lpr, channelInfos);
                    this.statusMasksMap.put(lpr, statusMask);
                    this.channelMaskMap.put(lpr, channelMask);
                } catch (DataAccessResultException e) {
                    this.meterProtocol.getLogger().log(Level.WARNING, "Failed to read configuration from LoadProfile " + lpr + ": " + e.getMessage());
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
     * Return the load profile interval, in seconds
     */
    protected int readLoadProfileInterval(ComposedCosemObject ccoLpConfigs, ComposedProfileConfig cpc) throws IOException {
        return ccoLpConfigs.getAttribute(cpc.getLoadProfileInterval()).intValue();
    }

    /**
     * From the given list of LoadProfileReaders, filter out all invalid ones. <br></br>
     * A LoadProfileReader is invalid if the physical address of the device, owning the loadProfileReader, could not be fetched.
     * This is the case, when an Mbus device is linked to a master in EIMaster, but in reality not physically connected. <br></br>
     * E.g.: This is the case when the Mbus device in the field has been swapped for a new one, but without changing/correcting the EIMaster configuration.
     *
     * @param loadProfileReaders    the complete list of loadProfileReaders
     * @return  the validated list containing all valid loadProfileReaders
     */
    private List<LoadProfileReader> filterOutAllInvalidLoadProfiles(List<LoadProfileReader> loadProfileReaders) {
        List<LoadProfileReader> validLoadProfileReaders = new ArrayList<>();

        for (LoadProfileReader loadProfileReader : loadProfileReaders) {
            if (this.meterProtocol.getPhysicalAddressFromSerialNumber(loadProfileReader.getMeterSerialNumber()) != -1) {
                validLoadProfileReaders.add(loadProfileReader);
            } else {
                this.meterProtocol.getLogger().severe("LoadProfile " + loadProfileReader.getProfileObisCode() + " is not supported because MbusDevice " + loadProfileReader.getMeterSerialNumber() + " is not installed on the physical device.");
            }
        }
        return validLoadProfileReaders;
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
            List<DLMSAttribute> dlmsAttributes = new ArrayList<>();
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
     * @throws java.io.IOException if an error occurred during dataFetching or -Parsing
     */
    protected List<CapturedRegisterObject> createCapturedObjectRegisterList(ComposedCosemObject ccoLpConfigs) throws IOException {
        List<CapturedRegisterObject> channelRegisters = new ArrayList<>();
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
                    List<CapturedRegisterObject> coRegisters = new ArrayList<>();
                    for (CapturedObject co : capturedObjects) {
                        String deviceSerialNumber = this.meterProtocol.getSerialNumberFromCorrectObisCode(co.getLogicalName().getObisCode());
                        DLMSAttribute dlmsAttribute = new DLMSAttribute(co.getLogicalName().getObisCode(), co.getAttributeIndex(), co.getClassId());
                        CapturedRegisterObject reg = new CapturedRegisterObject(dlmsAttribute, deviceSerialNumber);

                        // Prepare each register only once. This way we don't get duplicate registerRequests in one getWithList
                        if (!channelRegisters.contains(reg) && isDataObisCode(reg.getObisCode(), reg.getSerialNumber())) {
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
    private ComposedCosemObject constructCapturedObjectRegisterUnitComposedCosemObject(List<CapturedRegisterObject> registers, boolean supportsBulkRequest) {
        if (registers != null) {
            List<DLMSAttribute> dlmsAttributes = new ArrayList<>();
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
     * @param configuredChannelInfos
     * @return a constructed list of <CODE>ChannelInfos</CODE>
     * @throws java.io.IOException when an error occurred during dataFetching or -Parsing
     */
    protected List<ChannelInfo> constructChannelInfos(List<CapturedRegisterObject> registers, ComposedCosemObject ccoRegisterUnits, List<ChannelInfo> configuredChannelInfos) throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        for (CapturedRegisterObject registerUnit : registers) {
            if (!"".equalsIgnoreCase(registerUnit.getSerialNumber()) && isDataObisCode(registerUnit.getObisCode(), registerUnit.getSerialNumber())) {
                if (this.registerUnitMap.containsKey(registerUnit)) {
                    ScalerUnit su = new ScalerUnit(ccoRegisterUnits.getAttribute(this.registerUnitMap.get(registerUnit)));
                    if (su.getUnitCode() != 0) {
                        ChannelInfo ci = new ChannelInfo(channelInfos.size(), registerUnit.getObisCode().toString(), su.getEisUnit(), registerUnit.getSerialNumber(), true,
                                getReadingTypeFromConfiguredChannels(registerUnit.getObisCode(), configuredChannelInfos));
                        channelInfos.add(ci);
                    } else {
                        ChannelInfo ci = new ChannelInfo(channelInfos.size(), registerUnit.getObisCode().toString(), Unit.getUndefined(), registerUnit.getSerialNumber(), true,
                                getReadingTypeFromConfiguredChannels(registerUnit.getObisCode(), configuredChannelInfos));
                        channelInfos.add(ci);
                    }
                } else {
                    throw new LoadProfileConfigurationException("Could not fetch a correct Unit for " + registerUnit + " - not in registerUnitMap.");
                }
            }
        }
        return channelInfos;
    }

    protected ReadingType getReadingTypeFromConfiguredChannels(ObisCode obisCode, List<ChannelInfo> configuredChannelInfos) throws LoadProfileConfigurationException {
        return configuredChannelInfos.stream().filter(channelInfo -> channelInfo.getChannelObisCode().equals(obisCode)).findFirst().orElseThrow(() -> new LoadProfileConfigurationException("Could not found a correct ChannelInfo with the obiscode " + obisCode)).getReadingType();
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
            if (!"".equals(registerUnit.getSerialNumber()) && isDataObisCode(registerUnit.getObisCode(), registerUnit.getSerialNumber())) {
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
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        List<ProfileData> profileDataList = new ArrayList<>();
        ProfileGeneric profile;
        ProfileData profileData;
        for (LoadProfileReader lpr : loadProfiles) {
            ObisCode lpObisCode = this.meterProtocol.getPhysicalAddressCorrectedObisCode(lpr.getProfileObisCode(), lpr.getMeterSerialNumber());
            LoadProfileConfiguration lpc = getLoadProfileConfiguration(lpr);
            if (this.channelInfoMap.containsKey(lpr) && lpc != null) { // otherwise it is not supported by the meter
                this.meterProtocol.getLogger().log(Level.INFO, "Getting LoadProfile data for " + lpr + " from " + lpr.getStartReadingTime() + " to " + lpr.getEndReadingTime());
                profile = this.meterProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpObisCode);
                profileData = new ProfileData(lpr.getLoadProfileId());
                profileData.setChannelInfos(this.channelInfoMap.get(lpr));
                Calendar fromCalendar = Calendar.getInstance(this.meterProtocol.getTimeZone());
                fromCalendar.setTimeInMillis(lpr.getStartReadingTime().toEpochMilli());
                Calendar toCalendar = Calendar.getInstance(this.meterProtocol.getTimeZone());
                toCalendar.setTimeInMillis(lpr.getEndReadingTime().toEpochMilli());

                //TODO it is possible that we need to check for the masks ...
                DLMSProfileIntervals intervals = new DLMSProfileIntervals(profile.getBufferData(fromCalendar, toCalendar), DLMSProfileIntervals.DefaultClockMask,
                        this.statusMasksMap.get(lpr), this.channelMaskMap.get(lpr), new DSMRProfileIntervalStatusBits());
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
    protected LoadProfileConfiguration getLoadProfileConfiguration(LoadProfileReader loadProfileReader) {
        for (LoadProfileConfiguration lpc : this.loadProfileConfigurationList) {
            if (loadProfileReader.getProfileObisCode().equals(lpc.getObisCode()) && loadProfileReader.getDeviceIdentifier().equals(lpc.getDeviceIdentifier())) {
                return lpc;
            }
        }
        return null;
    }

    protected Map<LoadProfileReader, List<ChannelInfo>> getChannelInfoMap() {
        return channelInfoMap;
    }

    protected Map<LoadProfileReader, Integer> getStatusMasksMap() {
        return statusMasksMap;
    }

    protected AbstractSmartNtaProtocol getMeterProtocol() {
        return meterProtocol;
    }

    protected Map<LoadProfileReader, ComposedProfileConfig> getLpConfigMap() {
        return lpConfigMap;
    }

    protected Map<LoadProfileReader, List<CapturedRegisterObject>> getCapturedObjectRegisterListMap() {
        return capturedObjectRegisterListMap;
    }

    protected List<LoadProfileReader> getExpectedLoadProfileReaders() {
        return expectedLoadProfileReaders;
    }

    protected Map<CapturedRegisterObject, DLMSAttribute> getRegisterUnitMap() {
        return registerUnitMap;
    }

}