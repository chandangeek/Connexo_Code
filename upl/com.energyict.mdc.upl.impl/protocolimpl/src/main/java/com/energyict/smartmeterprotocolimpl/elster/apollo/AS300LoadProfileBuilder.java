package com.energyict.smartmeterprotocolimpl.elster.apollo;

import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 30/06/11
 * Time: 13:49
 */
public class AS300LoadProfileBuilder {

    private List<LoadProfileReader> expectedLoadProfileReaders;
    private List<LoadProfileConfiguration> loadProfileConfigurationList;
    private Map<Register, DLMSAttribute> registerUnitMap = new HashMap<Register, DLMSAttribute>();
    private Map<LoadProfileReader, ComposedProfileConfig> lpConfigMap = new HashMap<LoadProfileReader, ComposedProfileConfig>();
    private Map<LoadProfileReader, List<Register>> capturedObjectRegisterListMap = new HashMap<LoadProfileReader, List<Register>>();
    protected Map<LoadProfileReader, List<ChannelInfo>> channelInfoMap = new HashMap<LoadProfileReader, List<ChannelInfo>>();
    protected Map<ObisCode, List<CapturedObject>> capturedObjectsToRequest = new HashMap<ObisCode, List<CapturedObject>>();
    protected Map<LoadProfileReader, ProfileMasks> masks = new HashMap<LoadProfileReader, ProfileMasks>();
    protected Map<ObisCode, List<CapturedObject>> capturedObjectsMap = new HashMap<ObisCode, List<CapturedObject>>();
    protected AS300 meterProtocol;

    public AS300LoadProfileBuilder(AS300 meterProtocol) {
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
            this.meterProtocol.getLogger().log(Level.INFO, "Reading configuration from LoadProfile " + lpr);
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
                    DLMSAttribute registerUnit = new DLMSAttribute(rObisCode, RegisterAttributes.SCALER_UNIT.getAttributeNumber(), uo.getClassID());
                    dlmsAttributes.add(registerUnit);
                    this.registerUnitMap.put(register, registerUnit);
                } else if (isDemandRegisterClass(uo)) {            //Because the unit is attribute 4 for demand registers!
                    DLMSAttribute registerUnit = new DLMSAttribute(rObisCode, DemandRegisterAttributes.UNIT.getAttributeNumber(), uo.getClassID());
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
     * <p>
     * Fetches one or more LoadProfiles from the device. Each <CODE>LoadProfileReader</CODE> contains a list of necessary
     * channels({@link com.energyict.protocol.LoadProfileReader#channelInfos}) to read. If it is possible then only these channels should be read,
     * if not then all channels may be returned in the <CODE>ProfileData</CODE>. If {@link LoadProfileReader#channelInfos} contains an empty list
     * or null, then all channels from the corresponding LoadProfile should be fetched.
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
            ObisCode lpObisCode = lpr.getProfileObisCode();
            LoadProfileConfiguration lpc = getLoadProfileConfiguration(lpr);
            if (this.channelInfoMap.containsKey(lpr) && lpc != null) { // otherwise it is not supported by the meter
                this.meterProtocol.getLogger().log(Level.INFO, "Getting LoadProfile data for " + lpr + " from " + lpr.getStartReadingTime() + " to " + lpr.getEndReadingTime());
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
                DLMSProfileIntervals intervals = new DLMSProfileIntervals(profile.getBufferData(fromCalendar, toCalendar, channels), masks.get(lpr).getClockMask(), masks.get(lpr).getStatusMask(), -1, new AS300ProfileIntervalStatusBits());
                profileData.setIntervalDatas(intervals.parseIntervals(lpc.getProfileInterval()));

                profileDataList.add(profileData);
            }
        }

        return profileDataList;
    }

    protected LoadProfileConfiguration getLoadProfileConfiguration(LoadProfileReader loadProfileReader) {
        for (LoadProfileConfiguration lpc : this.loadProfileConfigurationList) {
            if (loadProfileReader.getProfileObisCode().equals(lpc.getObisCode()) && loadProfileReader.getMeterSerialNumber().equalsIgnoreCase(lpc.getMeterSerialNumber())) {
                return lpc;
            }
        }
        return null;
    }

    protected boolean isDataObisCode(ObisCode obisCode) {
        boolean isDataObisCode = true;
        return !Clock.getDefaultObisCode().equals(obisCode) && isDataObisCode;
    }

    private List<Register> createCapturedObjectRegisterList(ComposedCosemObject ccoLpConfigs) throws IOException {
        List<Register> channelRegisters = new ArrayList<Register>();
        if (this.expectedLoadProfileReaders != null) {
            for (LoadProfileReader lpr : this.expectedLoadProfileReaders) {
                int statusMask = 0;
                int clockMask = 0;
                ComposedProfileConfig cpc = this.lpConfigMap.get(lpr);
                if (cpc != null) {
                    DataContainer dc = new DataContainer();
                    dc.parseObjectList(ccoLpConfigs.getAttribute(cpc.getLoadProfileCapturedObjects()).getBEREncodedByteArray(), this.meterProtocol.getLogger());
                    ProfileGeneric pg = new ProfileGeneric(this.meterProtocol.getDlmsSession(), null);
                    List<CapturedObject> capturedObjects = pg.getCapturedObjectsFromDataContainter(dc);
                    capturedObjectsMap.put(lpr.getProfileObisCode(), capturedObjects);  //Remember the captured_objects of this LP
                    List<Register> coRegisters = new ArrayList<Register>();
                    List<CapturedObject> relevantObjects = new ArrayList<CapturedObject>();
                    for (CapturedObject co : capturedObjects) {
                        if (loadProfileContains(lpr, co.getLogicalName().getObisCode())) {
                            relevantObjects.add(co);
                            Register reg = new Register(-1, co.getLogicalName().getObisCode(), meterProtocol.getSerialNumber());
                            if (!channelRegisters.contains(reg) && isDataObisCode(reg.getObisCode())) {// this way we don't get duplicate registerRequests in one getWithList
                                channelRegisters.add(reg);
                            }
                            coRegisters.add(reg); // we always add it to the list of registers for this CapturedObject
                        } else if (isStatus(co.getLogicalName().getObisCode())) {
                            relevantObjects.add(co);
                            statusMask = DLMSProfileIntervals.DefaultStatusMask;
                        } else if (isClock(co)) {
                            relevantObjects.add(co);
                            clockMask = DLMSProfileIntervals.DefaultClockMask;
                        }
                    }
                    this.capturedObjectRegisterListMap.put(lpr, coRegisters);
                    capturedObjectsToRequest.put(lpr.getProfileObisCode(), relevantObjects);
                    masks.put(lpr, new ProfileMasks(clockMask, statusMask));
                }
            }
        } else {
            throw new LoadProfileConfigurationException("ExpectedLoadProfileReaders may not be null");
        }
        return channelRegisters;
    }

    protected boolean isClock(CapturedObject co) {
        return co.getClassId() == DLMSClassId.CLOCK.getClassId();
    }

    protected boolean isStatus(ObisCode obisCode) {
        return AS300ObisCodeProvider.LoadProfileStatus30Min.equals(obisCode) || AS300ObisCodeProvider.LoadProfileStatusP2.equals(obisCode);
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

    protected class ProfileMasks {
        private final int clockMask;
        private final int statusMask;

        public ProfileMasks(final int clockMask, final int statusMask){
            this.clockMask = clockMask;
            this.statusMask = statusMask;
        }

        public int getClockMask() {
            return clockMask;
        }

        public int getStatusMask() {
            return statusMask;
        }
    }
}