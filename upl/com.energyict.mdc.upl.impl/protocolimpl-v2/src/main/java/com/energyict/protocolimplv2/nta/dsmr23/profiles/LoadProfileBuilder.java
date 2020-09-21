package com.energyict.protocolimplv2.nta.dsmr23.profiles;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.identifiers.LoadProfileIdentifierById;
import com.energyict.mdc.upl.LoadProfileConfigurationException;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.tasks.support.DeviceLoadProfileSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimpl.base.ProfileIntervalStatusBits;
import com.energyict.protocolimplv2.common.composedobjects.ComposedProfileConfig;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.DLMSProfileIntervals;
import com.energyict.protocolimplv2.nta.abstractnta.DSMRProfileIntervalStatusBits;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import static com.energyict.protocolimplv2.nta.dsmr40.common.profiles.Dsmr40LoadProfileBuilder.MBUS_LP_DUPLICATED_CHANNEL;
import static com.energyict.protocolimplv2.nta.esmr50.common.loadprofiles.ESMR50LoadProfileBuilder.*;

/**
 * Provides functionality to fetch and create {@link com.energyict.protocol.ProfileData} objects for a {@link com.energyict.mdc.upl.DeviceProtocol}
 * <p>
 * <pre>
 * Copyrights EnergyICT
 * Date: 3-mrt-2011
 * Time: 17:02:07
 * </pre>
 */
public class LoadProfileBuilder<T extends AbstractDlmsProtocol> implements DeviceLoadProfileSupport {

    private static final ObisCode MBUS_LP_COMBINED_CHANNEL = ObisCode.fromString("0.x.24.2.1.255");

    /**
     * Hardcoded ObisCode for the status of the 15min profile
     */
    protected static final ObisCode QuarterlyHourStatusObisCode = ObisCode.fromString("0.x.96.10.1.255");

    protected static final ObisCode EdpQuarterlyHourStatusObisCode = ObisCode.fromString("0.x.96.10.7.255");

    public static final ObisCode MBUS_HOURLY_LP_OBISCODE = ObisCode.fromString("0.x.24.3.0.255");
    public static final ObisCode MBUS_DAILY_LP_OBISCODE = ObisCode.fromString("1.x.99.2.0.255");
    public static final ObisCode MBUS_MONTHLY_LP_OBISCODE = ObisCode.fromString("0.x.98.1.0.255");

    /**
     * Hardcoded ObisCode for the status of the daily profile
     */
    protected static final ObisCode DailyStatusObisCode = ObisCode.fromString("0.x.96.10.2.255");
    /**
     * Hardcoded ObisCode for the status of the hourly profile
     */
    protected static final ObisCode HourlyStatusObiscode = ObisCode.fromString("0.x.96.10.3.255");

    /**
     * MBus master value obis code,
     * used to filter load profile channels used by slave devices
     */
    public static final ObisCode MBUS_MASTER_VALUE = ObisCode.fromString("0.x.24.2.1.255");

    /**
     * The used meterProtocol
     */
    private final T meterProtocol;
    /**
     * Keep track of a list of channelMask per LoadProfileReader
     */
    protected Map<LoadProfileReader, Integer> channelMaskMap = new HashMap<>();
    /**
     * Keeps track of the link between a {@link com.energyict.protocol.LoadProfileReader} and a {@link ComposedProfileConfig}
     */
    private Map<LoadProfileReader, ComposedProfileConfig> lpConfigMap = new HashMap<>();
    /**
     * Keeps track of the link between a {@link com.energyict.protocol.LoadProfileReader} and a list of {@link com.energyict.protocol.Register} which
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
     * Keeps track of the link between a {@link com.energyict.protocol.Register} and his {@link com.energyict.dlms.DLMSAttribute} for ComposedCosemObject reads ...
     */
    private Map<CapturedRegisterObject, DLMSAttribute> registerUnitMap = new HashMap<>();

    /**
     * The list of LoadProfileReaders which are expected to be fetched
     */
    private List<LoadProfileReader> expectedLoadProfileReaders;

    /**
     * The list of <CODE>DeviceLoadProfileConfiguration</CODE> objects which are build from the information from the actual device, based on the {@link #expectedLoadProfileReaders}
     */
    private List<CollectedLoadProfileConfiguration> loadProfileConfigurationList;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public LoadProfileBuilder(T meterProtocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.meterProtocol = meterProtocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
    }

    protected CollectedDataFactory getCollectedDataFactory() {
        return collectedDataFactory;
    }

    protected IssueFactory getIssueFactory() {
        return issueFactory;
    }

    /**
     * Get the configuration(interval, number of channels, channelUnits) of all given LoadProfiles for the {@link #meterProtocol}
     *
     * @param allLoadProfileReaders a list of definitions of expected loadProfiles to read
     * @return the list of <CODE>DeviceLoadProfileConfiguration</CODE> objects which are in the device
     */
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> allLoadProfileReaders) {
        this.expectedLoadProfileReaders = filterOutAllInvalidLoadProfiles(allLoadProfileReaders);
        this.loadProfileConfigurationList = new ArrayList<>();

        ComposedCosemObject ccoLpConfigs = constructLoadProfileConfigComposedCosemObject(expectedLoadProfileReaders, meterProtocol.getDlmsSessionProperties().isBulkRequest());

        List<CapturedRegisterObject> capturedObjectRegisterList;
        try {
            capturedObjectRegisterList = createCapturedObjectRegisterList(ccoLpConfigs);
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getMeterProtocol().getDlmsSessionProperties().getRetries() + 1);
        }
        ComposedCosemObject ccoCapturedObjectRegisterUnits = constructCapturedObjectRegisterUnitComposedCosemObject(capturedObjectRegisterList, meterProtocol.getDlmsSessionProperties().isBulkRequest());

        for (LoadProfileReader lpr : allLoadProfileReaders) {
            CollectedLoadProfileConfiguration lpc = this.collectedDataFactory.createCollectedLoadProfileConfiguration(lpr.getProfileObisCode(), lpr.getMeterSerialNumber());
            if (!expectedLoadProfileReaders.contains(lpr)) {      //Invalid LP, mark as not supported and move on to the next LP
                lpc.setSupportedByMeter(false);
                continue;
            }

            this.meterProtocol.journal( "Reading configuration from LoadProfile " + lpr);
            ComposedProfileConfig cpc = lpConfigMap.get(lpr);
            if (cpc != null) {
                try {
                    lpc.setProfileInterval(ccoLpConfigs.getAttribute(cpc.getLoadProfileInterval()).intValue());
                    List<ChannelInfo> channelInfos = constructChannelInfos(capturedObjectRegisterListMap.get(lpr), ccoCapturedObjectRegisterUnits);

                    if (lpc.getObisCode().equalsIgnoreBChannel(MBUS_HOURLY_LP_OBISCODE) ||
                        lpc.getObisCode().equalsIgnoreBChannel(MBUS_DAILY_LP_OBISCODE) ||
                        lpc.getObisCode().equalsIgnoreBChannel(MBUS_MONTHLY_LP_OBISCODE)) {
                        // remap duplicated 0.x.24.2.1.255 (timestamp) to 0.x.24.2.5.255
                        channelInfos.stream().filter(
                                ci -> ci.getChannelObisCode().equalsIgnoreBChannel(MBUS_LP_DUPLICATED_CHANNEL) &&
                                        ci.getUnit().equals(Unit.get(BaseUnit.SECOND))
                        ).forEach(
                                ci -> ci.setName( ObisCode.setFieldAndGet(ObisCode.fromString(ci.getName()), 5, 5).toString() )
                        );
                    }

                    int statusMask = constructStatusMask(capturedObjectRegisterListMap.get(lpr));
                    int channelMask = constructChannelMask(capturedObjectRegisterListMap.get(lpr));
                    lpc.setChannelInfos(channelInfos);
                    this.channelInfoMap.put(lpr, channelInfos);
                    this.statusMasksMap.put(lpr, statusMask);
                    this.channelMaskMap.put(lpr, channelMask);
                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getMeterProtocol().getDlmsSessionProperties().getRetries() + 1)) {
                        lpc.setSupportedByMeter(false);
                        getMeterProtocol().journal("Load profile "+lpr+" is not supported by the meter: "+e.getLocalizedMessage());
                    }
                }
            } else {
                lpc.setSupportedByMeter(false);
                getMeterProtocol().journal("Load profile configuration for "+lpr+" could not be retrieved from local configuration.");
            }
            this.loadProfileConfigurationList.add(lpc);
        }
        return this.loadProfileConfigurationList;
    }

    /**
     * From the given list of LoadProfileReaders, filter out all invalid ones. <br></br>
     * A LoadProfileReader is invalid if the physical address of the device, owning the loadProfileReader, could not be fetched.
     * This is the case, when an Mbus device is linked to a master in EIMaster, but in reality not physically connected. <br></br>
     * E.g.: This is the case when the Mbus device in the field has been swapped for a new one, but without changing/correcting the EIMaster configuration.
     *
     * @param loadProfileReaders the complete list of loadProfileReaders
     * @return the validated list containing all valid loadProfileReaders
     */
    private List<LoadProfileReader> filterOutAllInvalidLoadProfiles(List<LoadProfileReader> loadProfileReaders) {
        List<LoadProfileReader> validLoadProfileReaders = new ArrayList<>();

        for (LoadProfileReader loadProfileReader : loadProfileReaders) {
            if (this.meterProtocol.getPhysicalAddressFromSerialNumber(loadProfileReader.getMeterSerialNumber()) != -1) {
                validLoadProfileReaders.add(loadProfileReader);
            } else {
                this.meterProtocol.journal("LoadProfile " + loadProfileReader.getProfileObisCode() + " is not supported because MbusDevice " + loadProfileReader.getMeterSerialNumber() + " is not installed on the physical device.");
            }
        }
        return validLoadProfileReaders;
    }

    protected ObisCode fixObisCodeForSlaveLoadProfile( ObisCode obisCode )
    {
        // special case for combined load profile
        if( obisCode.equalsIgnoreBChannel(MBUS_MONTHLY_LP_OBISCODE) || obisCode.equalsIgnoreBChannel(MBUS_DAILY_LP_OBISCODE) ) {
            obisCode = obisCode.setB(0);
        }

        return obisCode;
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

                obisCode = fixObisCodeForSlaveLoadProfile( obisCode );

                UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.meterProtocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), obisCode);
                if (uo != null) {
                    ComposedProfileConfig cProfileConfig = new ComposedProfileConfig(
                            new DLMSAttribute(obisCode, DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_CAPTUREPERIOD, uo.getClassID()),
                            new DLMSAttribute(obisCode, DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_CAPTUREOBJECTS, uo.getClassID()));
                    dlmsAttributes.add(cProfileConfig.getLoadProfileInterval());
                    dlmsAttributes.add(cProfileConfig.getLoadProfileCapturedObjects());
                    this.lpConfigMap.put(lpReader, cProfileConfig);
                    this.meterProtocol.journal( "LoadProfile with ObisCode " + obisCode + " is in the meter object list");
                } else {
                    this.meterProtocol.journal( "LoadProfile with ObisCode " + obisCode + " for meter '" + lpReader.getMeterSerialNumber() + "' is not supported.");
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
                        if ((deviceSerialNumber != null) && (!deviceSerialNumber.equals(""))) {
                            DLMSAttribute dlmsAttribute = new DLMSAttribute(co.getLogicalName().getObisCode(), co.getAttributeIndex(), co.getClassId());
                            CapturedRegisterObject reg = new CapturedRegisterObject(dlmsAttribute, deviceSerialNumber);

                            // Prepare each register only once. This way we don't get duplicate registerRequests in one getWithList
                            if (!channelRegisters.contains(reg) && isDataObisCode(reg.getObisCode(), reg.getSerialNumber())) {
                                channelRegisters.add(reg);
                            }
                            coRegisters.add(reg); // we always add it to the list of registers for this CapturedObject
                        } else {
                            if (co.getLogicalName().getObisCode().equalsIgnoreBChannel(MBUS_MASTER_VALUE)) {
                                // For some profiles we expect MBus channels stored which are not found
                                // Ex:  if there is only one Mbus channel, only 0.1.24.2.1.255 obis will be present,
                                //      but not 0.2.24.2.1.255, 0.3.24.2.1.255, 0.4.24.2.1.255
                                // so swallow this case and don't alert the user
                            } else {
                                // this is a genuine warning, we found a wrong channel
                                getMeterProtocol().journal(Level.WARNING, "Unexpected channel found in device which could not be mapped to the master device or to any of the slave devices: " + co.getLogicalName());
                            }
                        }
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
                    } else if (uo.getDLMSClassId() == DLMSClassId.LTE_MONITORING) {
                        DLMSAttribute unitAttribute = new DLMSAttribute(rObisCode, register.getAttribute(), uo.getClassID());
                        dlmsAttributes.add(unitAttribute);
                        this.registerUnitMap.put(register, unitAttribute);
                    }else if (uo.getDLMSClassId() == DLMSClassId.GSM_DIAGNOSTICS) {
                        DLMSAttribute unitAttribute = new DLMSAttribute(rObisCode, register.getAttribute(), uo.getClassID());
                        dlmsAttributes.add(unitAttribute);
                        this.registerUnitMap.put(register, unitAttribute);
                    }else if (uo.getDLMSClassId() == DLMSClassId.DATA) {
                        DLMSAttribute unitAttribute = new DLMSAttribute(rObisCode, register.getAttribute(), uo.getClassID());
                        dlmsAttributes.add(unitAttribute);
                        this.registerUnitMap.put(register, unitAttribute);
                    }
                } else {
                    this.meterProtocol.journal( "LoadProfile channel with ObisCode " + (rObisCode != null ? rObisCode : register.getObisCode()) + " is not supported.");
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
     * @throws java.io.IOException when an error occurred during dataFetching or -Parsing
     */
    protected List<ChannelInfo> constructChannelInfos(List<CapturedRegisterObject> registers, ComposedCosemObject ccoRegisterUnits) throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        for (CapturedRegisterObject registerUnit : registers) {
            if (!"".equalsIgnoreCase(registerUnit.getSerialNumber()) && isDataObisCode(registerUnit.getObisCode(), registerUnit.getSerialNumber())) {
                if (this.registerUnitMap.containsKey(registerUnit)) {
                    DLMSAttribute scalerUnitAttribute = this.registerUnitMap.get(registerUnit);
                    AbstractDataType rawValue = ccoRegisterUnits.getAttribute(scalerUnitAttribute);
                    ScalerUnit su;
                    if (rawValue.isStructure()){
                        su = new ScalerUnit(rawValue.getStructure());
                    } else {
                        su = new ScalerUnit(0, rawValue.intValue());
                    }
                    if (su.getUnitCode() != 0) {
                        ChannelInfo ci = new ChannelInfo(channelInfos.size(), registerUnit.getObisCode().toString(), su.getEisUnit(), registerUnit.getSerialNumber(), isCumulativeChannel(registerUnit.getObisCode()));
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
        int channelMask;

        if (isCombinedLoadProfile(registers)) {
            channelMask = getCombinedLoadProfileChannelMask(registers);
        } else {
            channelMask = getLoadProfileChannelMask(registers);
        }
        return channelMask;
    }

    private boolean isCombinedLoadProfile(List<CapturedRegisterObject> capturedRegisterObjectList) {
        return capturedRegisterObjectList.stream().anyMatch(cro -> cro.getObisCode().equalsIgnoreBChannel(MBUS_LP_COMBINED_CHANNEL));
    }

    private int getCombinedLoadProfileChannelMask(List<CapturedRegisterObject> registers) {
        int channelMask = 0;
        int counter = 0;
        int mbusCounter = 1;

        for (CapturedRegisterObject register : registers) {
            if (isValidMasterRegister(register)) {
                channelMask |= (int) Math.pow(2, counter);
            } else if (isMbusRegister(register)) {
                final int bField = register.getObisCode().getB();

                if (bField > mbusCounter) {
                    final int difference = (bField - mbusCounter) * getNumberOfChannels(registers, register.getObisCode());

                    for (int i = 0; i < difference; i++) {
                        counter++;
                    }
                } else if (bField < mbusCounter) {
                    mbusCounter--;
                }
                channelMask |= (int) Math.pow(2, counter);

                mbusCounter++;
            }
            counter++;
        }

        return channelMask;
    }

    private int getNumberOfChannels(List<CapturedRegisterObject> registers, ObisCode currentRegisterObisCode) {
        return (int) registers.stream()
                .filter(cro -> cro.getObisCode().equals(currentRegisterObisCode)).count();
    }

    private int getLoadProfileChannelMask(List<CapturedRegisterObject> registers) {
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

    private boolean isMbusRegister(CapturedRegisterObject register) {
        return register.getObisCode().equalsIgnoreBChannel(MBUS_LP_COMBINED_CHANNEL);
    }

    private boolean isValidMasterRegister(CapturedRegisterObject register) {
        return !"".equals(register.getSerialNumber()) &&
                isDataObisCode(register.getObisCode(), register.getSerialNumber()) &&
                !isMbusRegister(register);
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

        testObisCode = this.meterProtocol.getPhysicalAddressCorrectedObisCode(EdpQuarterlyHourStatusObisCode, serialNumber);
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

        testObisCode = this.meterProtocol.getPhysicalAddressCorrectedObisCode(AMR_PROFILE_STATUS_CODE_MBUS_HOURLY, serialNumber);
        if (testObisCode != null) {
            isStatusObisCode |= testObisCode.equals(obisCode);
        } else {
            return false;
        }

        testObisCode = this.meterProtocol.getPhysicalAddressCorrectedObisCode(AMR_PROFILE_STATUS_CODE_MBUS_DAILY, serialNumber);
        if (testObisCode != null) {
            isStatusObisCode |= testObisCode.equals(obisCode);
        } else {
            return false;
        }

        testObisCode = this.meterProtocol.getPhysicalAddressCorrectedObisCode(AMR_PROFILE_STATUS_CODE_MBUS_MONTHLY, serialNumber);
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
     * channels(com.energyict.protocol.LoadProfileReader#channelInfos}) to read. If it is possible then only these channels should be read,
     * if not then all channels may be returned in the <CODE>ProfileData</CODE>.
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
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        List<CollectedLoadProfile> collectedLoadProfileList = new ArrayList<>();
        ProfileGeneric profile;

        for (LoadProfileReader lpr : loadProfiles) {
            ObisCode lpObisCode = this.meterProtocol.getPhysicalAddressCorrectedObisCode(lpr.getProfileObisCode(), lpr.getMeterSerialNumber());
            lpObisCode = fixObisCodeForSlaveLoadProfile(lpObisCode);

            CollectedLoadProfileConfiguration lpc = getLoadProfileConfiguration(lpr);
            CollectedLoadProfile collectedLoadProfile = this.collectedDataFactory.createCollectedLoadProfile(new LoadProfileIdentifierById(lpr.getLoadProfileId(), lpr.getProfileObisCode(), getMeterProtocol().getOfflineDevice().getDeviceIdentifier()));

            if (this.channelInfoMap.containsKey(lpr) && lpc != null) { // otherwise it is not supported by the meter
                this.meterProtocol.journal( "Getting LoadProfile data for " + lpr + " from " + lpr.getStartReadingTime() + " to " + lpr.getEndReadingTime());

                try {
                    List<ChannelInfo> channelInfos = this.channelInfoMap.get(lpr);
                    profile = this.meterProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpObisCode);

                    Calendar fromCalendar = Calendar.getInstance(this.meterProtocol.getTimeZone());
                    fromCalendar.setTime(lpr.getStartReadingTime());
                    Calendar toCalendar = Calendar.getInstance(this.meterProtocol.getTimeZone());
                    toCalendar.setTime(lpr.getEndReadingTime());

                    DLMSProfileIntervals intervals = new DLMSProfileIntervals(profile.getBufferData(fromCalendar, toCalendar), DLMSProfileIntervals.DefaultClockMask,
                            this.statusMasksMap.get(lpr), this.channelMaskMap.get(lpr), getIntervalStatusBits());
                    List<IntervalData> collectedIntervalData = intervals.parseIntervals(lpc.getProfileInterval());

                    this.getMeterProtocol().journal(" > load profile intervals parsed: " + collectedIntervalData.size());
                    collectedLoadProfile.setCollectedIntervalData(collectedIntervalData, channelInfos);
                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getMeterProtocol().getDlmsSessionProperties().getRetries() + 1)) {
                        Issue problem = this.issueFactory.createProblem(lpr, "loadProfileXIssue", lpr.getProfileObisCode(), e);
                        collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                    }
                }
            } else {
                Issue problem = this.issueFactory.createWarning(lpr, "loadProfileXnotsupported", lpr.getProfileObisCode());
                collectedLoadProfile.setFailureInformation(ResultType.NotSupported, problem);
            }
            collectedLoadProfileList.add(collectedLoadProfile);
        }
        return collectedLoadProfileList;
    }

    protected ProfileIntervalStatusBits getIntervalStatusBits() {
        return new DSMRProfileIntervalStatusBits(getMeterProtocol().getDlmsSessionProperties().isIgnoreDstStatusCode());
    }

    /**
     * Look for the <CODE>DeviceLoadProfileConfiguration</CODE> in the previously build up list
     *
     * @param loadProfileReader the reader linking to the <CODE>DeviceLoadProfileConfiguration</CODE>
     * @return requested configuration
     */
    protected CollectedLoadProfileConfiguration getLoadProfileConfiguration(LoadProfileReader loadProfileReader) {
        if( this.loadProfileConfigurationList != null ) {
            for (CollectedLoadProfileConfiguration lpc : this.loadProfileConfigurationList) {
                if (loadProfileReader.getProfileObisCode().equals(lpc.getObisCode()) && loadProfileReader.getMeterSerialNumber().equalsIgnoreCase(lpc.getMeterSerialNumber())) {
                    return lpc;
                }
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

    public Map<LoadProfileReader, Integer> getChannelMaskMap() {
        return channelMaskMap;
    }

    protected T getMeterProtocol() {
        return meterProtocol;
    }

    protected Map<CapturedRegisterObject, DLMSAttribute> getRegisterUnitMap() {
        return registerUnitMap;
    }

    protected boolean isCumulativeChannel(ObisCode obisCode) {
        return ParseUtils.isObisCodeCumulative(obisCode);
    }
}