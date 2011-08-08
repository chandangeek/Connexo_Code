package com.energyict.smartmeterprotocolimpl.elster.apollo;

import com.energyict.cbo.Unit;
import com.energyict.dlms.*;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.Register;
import com.energyict.protocolimpl.dlms.DLMSProfileIntervals;
import com.energyict.smartmeterprotocolimpl.common.composedobjects.ComposedProfileConfig;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 30/06/11
 * Time: 13:49
 */
public class LoadProfileBuilder {

    private List<LoadProfileReader> expectedLoadProfileReaders;
    private List<LoadProfileConfiguration> loadProfileConfigurationList;
    private Map<Register, DLMSAttribute> registerUnitMap = new HashMap<Register, DLMSAttribute>();
    private Map<LoadProfileReader, ComposedProfileConfig> lpConfigMap = new HashMap<LoadProfileReader, ComposedProfileConfig>();
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfoMap = new HashMap<LoadProfileReader, List<ChannelInfo>>();
    private Map<LoadProfileReader, List<Register>> capturedObjectRegisterListMap = new HashMap<LoadProfileReader, List<Register>>();
    private Map<ObisCode, List<CapturedObject>> capturedObjectsToRequest = new HashMap<ObisCode, List<CapturedObject>>();
    private AS300 meterProtocol;
    private int clockMask = 0;
    private int statusMask = 0;

    public LoadProfileBuilder(AS300 meterProtocol) {
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
        this.expectedLoadProfileReaders = loadProfileReaders;
        this.loadProfileConfigurationList = new ArrayList<LoadProfileConfiguration>();

        ComposedCosemObject ccoLpConfigs = constructLoadProfileConfigComposedCosemObject(loadProfileReaders, this.meterProtocol.getProperties().isBulkRequest());
        List<Register> capturedObjectRegisterList = createCapturedObjectRegisterList(ccoLpConfigs);
        ComposedCosemObject ccoCapturedObjectRegisterUnits = constructCapturedObjectRegisterUnitComposedCosemObject(capturedObjectRegisterList, this.meterProtocol.getProperties().isBulkRequest());

        for (LoadProfileReader lpr : this.expectedLoadProfileReaders) {
            LoadProfileConfiguration lpc = new LoadProfileConfiguration(lpr.getProfileObisCode(), meterProtocol.getSerialNumber());

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

    private ComposedCosemObject constructCapturedObjectRegisterUnitComposedCosemObject(List<Register> registers, boolean supportsBulkRequest) {
        if (registers != null) {
            List<DLMSAttribute> dlmsAttributes = new ArrayList<DLMSAttribute>();
            for (Register register : registers) {
                ObisCode rObisCode = register.getObisCode();
                UniversalObject uo = new UniversalObject(rObisCode.getLN(), RegisterReader.getClassId(rObisCode).getClassId(), 0);
                if (isRegisterClass(uo)) {
                    DLMSAttribute registerUnit = new DLMSAttribute(rObisCode, RegisterAttributes.Register_Unit.getAttributeNumber(), uo.getClassID());
                    dlmsAttributes.add(registerUnit);
                    this.registerUnitMap.put(register, registerUnit);
                } else if (isDemandRegisterClass(uo)) {            //Because the unit is attribute 4 for demand registers!
                    DLMSAttribute registerUnit = new DLMSAttribute(rObisCode, DemandRegisterAttributes.Register_Unit.getAttributeNumber(), uo.getClassID());
                    dlmsAttributes.add(registerUnit);
                    this.registerUnitMap.put(register, registerUnit);
                }
            }
            return new ComposedCosemObject(this.meterProtocol.getDlmsSession(), supportsBulkRequest, dlmsAttributes);
        }
        return null;
    }

    private boolean isRegisterClass(UniversalObject uo) {
        return uo.getClassID() == DLMSClassId.REGISTER.getClassId() || uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId();
    }

    private boolean isDemandRegisterClass(UniversalObject uo) {
        return uo.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId();
    }

    private List<ChannelInfo> constructChannelInfos(List<Register> registers, ComposedCosemObject ccoRegisterUnits) throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        for (Register registerUnit : registers) {
            if (isDataObisCode(registerUnit.getObisCode())) {
                if (this.registerUnitMap.containsKey(registerUnit)) {
                    ScalerUnit su = new ScalerUnit(ccoRegisterUnits.getAttribute(this.registerUnitMap.get(registerUnit)));
                    if (su.getUnitCode() != 0) {
                        ChannelInfo ci = new ChannelInfo(channelInfos.size(), registerUnit.getObisCode().toString(), su.getUnit(), registerUnit.getSerialNumber(), true);
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

    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        List<ProfileData> profileDataList = new ArrayList<ProfileData>();
        ProfileGeneric profile;
        ProfileData profileData;
        for (LoadProfileReader lpr : loadProfiles) {
            ObisCode lpObisCode = lpr.getProfileObisCode();
            LoadProfileConfiguration lpc = getLoadProfileConfiguration(lpr);
            if (this.channelInfoMap.containsKey(lpr) && lpc != null) { // otherwise it is not supported by the meter
                profile = this.meterProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpObisCode);
                profileData = new ProfileData(lpr.getLoadProfileId());
                profileData.setChannelInfos(this.channelInfoMap.get(lpr));
                Calendar fromCalendar = Calendar.getInstance(this.meterProtocol.getTimeZone());
                fromCalendar.setTime(lpr.getStartReadingTime());
                Calendar toCalendar = Calendar.getInstance(this.meterProtocol.getTimeZone());
                toCalendar.setTime(lpr.getEndReadingTime());

                List<CapturedObject> channels = capturedObjectsToRequest.get(lpObisCode);
                if (channels == null) {
                    continue;
                }
                DLMSProfileIntervals intervals = new DLMSProfileIntervals(profile.getBufferData(fromCalendar, toCalendar, channels), clockMask, statusMask, -1,  null);
                profileData.setIntervalDatas(intervals.parseIntervals(lpc.getProfileInterval()));

                profileDataList.add(profileData);
            }
        }

        return profileDataList;
    }

    private LoadProfileConfiguration getLoadProfileConfiguration(LoadProfileReader loadProfileReader) {
        for (LoadProfileConfiguration lpc : this.loadProfileConfigurationList) {
            if (loadProfileReader.getProfileObisCode().equals(lpc.getObisCode())  && loadProfileReader.getMeterSerialNumber().equalsIgnoreCase(lpc.getMeterSerialNumber())) {
                return lpc;
            }
        }
        return null;
    }

    private boolean isDataObisCode(ObisCode obisCode) {
        boolean isDataObisCode = true;
        return !Clock.getObisCode().equals(obisCode) && isDataObisCode;
    }


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
                    List<CapturedObject> relevantObjects = new ArrayList<CapturedObject>();
                    for (CapturedObject co : capturedObjects) {
                        if (loadProfileContains(lpr, co.getLogicalName().getObisCode())) {
                            relevantObjects.add(co);
                            Register reg = new Register(co.getLogicalName().getObisCode(), meterProtocol.getSerialNumber());
                            if (!channelRegisters.contains(reg) && isDataObisCode(reg.getObisCode())) {// this way we don't get duplicate registerRequests in one getWithList
                                channelRegisters.add(reg);
                            }
                            coRegisters.add(reg); // we always add it to the list of registers for this CapturedObject
                        } else if (isStatus(co.getLogicalName().getObisCode())) {
                            relevantObjects.add(co);
                            statusMask = DLMSProfileIntervals.DefaultStatusMask;
                        } else if (co.getClassId() == DLMSClassId.CLOCK.getClassId()) {
                            relevantObjects.add(co);
                            clockMask = DLMSProfileIntervals.DefaultClockMask;
                        }
                    }
                    this.capturedObjectRegisterListMap.put(lpr, coRegisters);
                    capturedObjectsToRequest.put(lpr.getProfileObisCode(), relevantObjects);
                }
            }
        } else {
            throw new LoadProfileConfigurationException("ExpectedLoadProfileReaders may not be null");
        }
        return channelRegisters;
    }

    private boolean isStatus(ObisCode obisCode) {
        return ObisCodeProvider.LoadProfileStatus30Min.equals(obisCode) || ObisCodeProvider.LoadProfileStatusP2.equals(obisCode);
    }

    private boolean loadProfileContains(LoadProfileReader lpr, ObisCode obisCode) throws IOException {
        for (ChannelInfo channelInfo : lpr.getChannelInfos()) {
            if (channelInfo.getChannelObisCode().equals(obisCode)) {
                return true;
            }
        }
        return false;
    }

    private ComposedCosemObject constructLoadProfileConfigComposedCosemObject(List<LoadProfileReader> loadProfileReaders, boolean supportsBulkRequest) {
        if (loadProfileReaders != null) {
            List<DLMSAttribute> dlmsAttributes = new ArrayList<DLMSAttribute>();
            for (LoadProfileReader lpReader : loadProfileReaders) {
                ObisCode obisCode = lpReader.getProfileObisCode();
                UniversalObject uo = new UniversalObject(obisCode.getLN(), RegisterReader.getClassId(obisCode).getClassId(), 0);
                if (uo.getClassID() == DLMSClassId.PROFILE_GENERIC.getClassId()) {
                    DLMSAttribute loadProfileInterval = new DLMSAttribute(obisCode, DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_CAPTUREPERIOD, uo.getClassID());
                    DLMSAttribute loadProfileCapturedObjects = new DLMSAttribute(obisCode, DLMSCOSEMGlobals.ATTR_PROFILEGENERIC_CAPTUREOBJECTS, uo.getClassID());
                    ComposedProfileConfig cProfileConfig = new ComposedProfileConfig(loadProfileInterval, loadProfileCapturedObjects);
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
}