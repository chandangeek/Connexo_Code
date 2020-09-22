package com.energyict.protocolimplv2.nta.esmr50.common.loadprofiles;


import com.energyict.mdc.identifiers.LoadProfileIdentifierById;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.dlms.DLMSProfileIntervals;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.CapturedRegisterObject;
import com.energyict.protocolimplv2.nta.dsmr40.common.profiles.Dsmr40LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.esmr50.common.ESMR50Protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

public class ESMR50LoadProfileBuilder<T extends ESMR50Protocol> extends Dsmr40LoadProfileBuilder<ESMR50Protocol> {

    /*
      15 minutes load profiles
    */
    public static final ObisCode AMR_PROFILE_STATUS_CODE_E_METER_15_MIN = ObisCode.fromString("0.0.96.10.2.255");
    /*
       Hourly load profiles
    */
    public static final ObisCode AMR_PROFILE_STATUS_CODE_MBUS_HOURLY = ObisCode.fromString("0.0.96.10.3.255");
    /*
       Daily load profiles
    */
    public static final ObisCode AMR_PROFILE_STATUS_CODE_E_METER_DAILY = ObisCode.fromString("0.0.96.10.4.255");
    public static final ObisCode AMR_PROFILE_STATUS_CODE_MBUS_DAILY = ObisCode.fromString("0.0.96.10.5.255");
    /*
       Monthly billing profiles
    */
    public static final ObisCode AMR_PROFILE_STATUS_CODE_E_METER_MONTHLY = ObisCode.fromString("0.0.96.10.6.255");
    public static final ObisCode AMR_PROFILE_STATUS_CODE_MBUS_MONTHLY = ObisCode.fromString("0.0.96.10.7.255");
    /*
      Power quality profile status 1 profiles
    */
    public static final ObisCode AMR_PROFILE_STATUS_CODE_E_METER_QUALITY_STATUS_1 = ObisCode.fromString("0.0.96.10.8.255");
    /*
      Power quality profile status 2 profiles
    */
    public static final ObisCode AMR_PROFILE_STATUS_CODE_E_METER_QUALITY_STATUS_2 = ObisCode.fromString("0.0.96.10.9.255");
    /*
      Definable load profile
    */
    public static final ObisCode DEFINABLE_LOAD_PROFILE = ObisCode.fromString("0.1.94.31.6.255");
    public static final ObisCode DEFINABLE_LOAD_PROFILE_STATUS = ObisCode.fromString("0.96.10.6.0.255");
    /*
      LTE Monitoring load profile
    */
    public static final ObisCode LTE_MONITORING_LOAD_PROFILE = ObisCode.fromString("0.0.99.18.0.255");

    private static final ObisCode MBUS_LP_DUPLICATED_CHANNEL = ObisCode.fromString("0.x.24.2.3.255");

    private static final boolean DO_IGNORE_DST_STATUS_CODE = true;

    /**
     * Default constructor
     *
     */
    public ESMR50LoadProfileBuilder(ESMR50Protocol meterProtocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(meterProtocol, collectedDataFactory, issueFactory);
    }

    protected boolean isStatusObisCode(ObisCode obisCode, String serialNumber) {
        boolean isStatusObisCode = super.isStatusObisCode(obisCode, serialNumber);
        ObisCode testObisCode;

        testObisCode = this.getMeterProtocol().getPhysicalAddressCorrectedObisCode(AMR_PROFILE_STATUS_CODE_E_METER_15_MIN, serialNumber);
        if (testObisCode != null) {
            isStatusObisCode |= testObisCode.equals(obisCode);
        } else {
            return false;
        }

        testObisCode = this.getMeterProtocol().getPhysicalAddressCorrectedObisCode(AMR_PROFILE_STATUS_CODE_MBUS_HOURLY, serialNumber);
        if (testObisCode != null) {
            isStatusObisCode |= testObisCode.equals(obisCode);
        } else {
            return false;
        }

        testObisCode = this.getMeterProtocol().getPhysicalAddressCorrectedObisCode(AMR_PROFILE_STATUS_CODE_E_METER_DAILY, serialNumber);
        if (testObisCode != null) {
            isStatusObisCode |= testObisCode.equals(obisCode);
        } else {
            return false;
        }

        testObisCode = this.getMeterProtocol().getPhysicalAddressCorrectedObisCode(AMR_PROFILE_STATUS_CODE_MBUS_DAILY, serialNumber);
        if (testObisCode != null) {
            isStatusObisCode |= testObisCode.equals(obisCode);
        } else {
            return false;
        }


        testObisCode = this.getMeterProtocol().getPhysicalAddressCorrectedObisCode(AMR_PROFILE_STATUS_CODE_E_METER_MONTHLY, serialNumber);
        if (testObisCode != null) {
            isStatusObisCode |= testObisCode.equals(obisCode);
        } else {
            return false;
        }

        testObisCode = this.getMeterProtocol().getPhysicalAddressCorrectedObisCode(AMR_PROFILE_STATUS_CODE_MBUS_MONTHLY, serialNumber);
        if (testObisCode != null) {
            isStatusObisCode |= testObisCode.equals(obisCode);
        } else {
            return false;
        }

        testObisCode = this.getMeterProtocol().getPhysicalAddressCorrectedObisCode(AMR_PROFILE_STATUS_CODE_E_METER_QUALITY_STATUS_1, serialNumber);
        if (testObisCode != null) {
            isStatusObisCode |= testObisCode.equals(obisCode);
        } else {
            return false;
        }

        testObisCode = this.getMeterProtocol().getPhysicalAddressCorrectedObisCode(AMR_PROFILE_STATUS_CODE_E_METER_QUALITY_STATUS_2, serialNumber);
        if (testObisCode != null) {
            isStatusObisCode |= testObisCode.equals(obisCode);
        } else {
            return false;
        }

        testObisCode = this.getMeterProtocol().getPhysicalAddressCorrectedObisCode(DEFINABLE_LOAD_PROFILE_STATUS, serialNumber);
        if (testObisCode != null) {
            isStatusObisCode |= testObisCode.equals(obisCode);
        } else {
            return false;
        }

        return isStatusObisCode;
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        List<CollectedLoadProfile> collectedLoadProfileList = new ArrayList<>();
        ProfileGeneric profile;

            for (LoadProfileReader lpr : loadProfiles) {
                ObisCode lpObisCode = this.getMeterProtocol().getPhysicalAddressCorrectedObisCode(lpr.getProfileObisCode(), lpr.getMeterSerialNumber());
                CollectedLoadProfileConfiguration lpc = getLoadProfileConfiguration(lpr);
                CollectedLoadProfile collectedLoadProfile = this.getCollectedDataFactory().createCollectedLoadProfile(new LoadProfileIdentifierById(lpr.getLoadProfileId(), lpr.getProfileObisCode(), getMeterProtocol().getOfflineDevice().getDeviceIdentifier()));
                try {
                    if (getChannelInfoMap().containsKey(lpr) && lpc != null) { // otherwise it is not supported by the meter
                    getMeterProtocol().journal(Level.INFO, "Getting LoadProfile [" + lpObisCode + "] data for " + lpr + " from " + lpr.getStartReadingTime() + " to " + lpr.getEndReadingTime());
                    profile = getMeterProtocol().getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpObisCode);
                    Calendar fromCalendar = Calendar.getInstance(getMeterProtocol().getTimeZone());
                    fromCalendar.setTime(lpr.getStartReadingTime());
                    Calendar toCalendar = Calendar.getInstance(getMeterProtocol().getTimeZone());
                    toCalendar.setTime(lpr.getEndReadingTime());

                    DLMSProfileIntervals intervals;

                    if (lpObisCode.equals(LTE_MONITORING_LOAD_PROFILE)) {
                        intervals = new LTEMonitoringProfileIntervals(
                                profile.getBufferData(fromCalendar, toCalendar),
                                getStatusMasksMap().get(lpr),
                                getChannelMaskMap().get(lpr),
                                new ESMR5ProfileIntervalStatusBits(getIgnoreDstStatusCode()),
                                getMeterProtocol().getLogger()); //TODO: check what to do whith this DST status
                    } else {
                        intervals = new DLMSProfileIntervals(
                                profile.getBufferData(fromCalendar, toCalendar),
                                DLMSProfileIntervals.DefaultClockMask,
                                getStatusMasksMap().get(lpr),
                                getChannelMaskMap().get(lpr),
                                new ESMR5ProfileIntervalStatusBits(getIgnoreDstStatusCode())); //TODO: check what to do whith this DST status
                    }

                    TimeZone timeZone = getMeterProtocol().getTimeZone();
                    if (mustUseSlaveTimeZone(lpr.getProfileObisCode())) {
                        // Enexis:
                        //          It has been approved that the E-meter already converts the UTC time to the local time for ESMR 5.0 meters.
                        //          So this means that the ESMR 5.0 protocols should not change these MBus capture times anymore.
                        //          So take them as it is.
                        // But:
                        //      - timestamps already contain an unwanted deviation, so will use MBus RMR timeZone to adjust
                    }
                    List<IntervalData> parsedIntervals = intervals.parseIntervals(lpc.getProfileInterval(), timeZone);
                    this.getMeterProtocol().journal(" > load profile intervals parsed: " + parsedIntervals.size());

                        if (lpObisCode.equals(LTE_MONITORING_LOAD_PROFILE)) {
                            collectedLoadProfile.setCollectedIntervalData(parsedIntervals, lpc.getChannelInfos());
                        } else {
                            collectedLoadProfile.setCollectedIntervalData(parsedIntervals, getChannelInfoMap().get(lpr));
                        }

                } else {
                    this.getMeterProtocol().journal(Level.WARNING, "Configuration for LoadProfile " + lpObisCode + " not found, will be skipped!");
                }
                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getMeterProtocol().getDlmsSessionProperties().getRetries() + 1)) {
                        Issue problem = this.getIssueFactory().createProblem(lpr, "loadProfileXIssue", lpr.getProfileObisCode(), e);
                        collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                    }
                }
                collectedLoadProfileList.add(collectedLoadProfile);
            }

        return collectedLoadProfileList;
    }

    @Override
    protected ObisCode fixObisCodeForSlaveLoadProfile( ObisCode obisCode )
    {
        return obisCode;
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders){
        List<CollectedLoadProfileConfiguration> loadProfileConfigurationList = super.fetchLoadProfileConfiguration(loadProfileReaders);

        for (CollectedLoadProfileConfiguration lpc : loadProfileConfigurationList) {
            if (lpc.getObisCode().equals(LTE_MONITORING_LOAD_PROFILE)) {
                lpc.setChannelInfos(getLTEMonitoringChannelInfos(lpc));
            } else if (lpc.getObisCode().equalsIgnoreBChannel(ESMR50Protocol.MBUS_HOURLY_LP_OBISCODE) ||
                       lpc.getObisCode().equalsIgnoreBChannel(ESMR50Protocol.MBUS_DAILY_LP_OBISCODE_SAME_AS_EMETER_LP2) ||
                       lpc.getObisCode().equalsIgnoreBChannel(ESMR50Protocol.MBUS_MONTHLY_LP_OBISCODE_SAME_AS_EMETER_LP3)) {
                List<ChannelInfo> channelInfos = lpc.getChannelInfos();
                // remap duplicated 0.x.24.2.3.255 (timestamp) to 0.x.24.2.5.255
                channelInfos.stream().filter(
                        ci -> ci.getChannelObisCode().equalsIgnoreBChannel(MBUS_LP_DUPLICATED_CHANNEL) &&
                              ci.getUnit().equals(Unit.get(BaseUnit.SECOND))
                ).forEach(
                        ci -> ci.setName( ObisCode.setFieldAndGet(ObisCode.fromString(ci.getName()), 5, 5).toString() )
                );
            }
        }

        return loadProfileConfigurationList;
    }

    protected List<ChannelInfo> getLTEMonitoringChannelInfos(CollectedLoadProfileConfiguration lpc) {
        return LTEMonitoringProfileVersion0.getLTEMonitoringChannelInfos(lpc);
    }

    private boolean mustUseSlaveTimeZone(ObisCode profileObisCode) {
        if (ESMR50Protocol.MBUS_HOURLY_LP_OBISCODE.equals(profileObisCode)){
            return true;
        }

        if (ESMR50Protocol.MBUS_DAILY_LP_OBISCODE_SAME_AS_EMETER_LP2.equals(profileObisCode)){
            return true;
        }

        if (ESMR50Protocol.MBUS_MONTHLY_LP_OBISCODE_SAME_AS_EMETER_LP3.equals(profileObisCode)){
            return true;
        }
        return false;
    }

    public boolean getIgnoreDstStatusCode() {
        return getMeterProtocol().getDlmsSessionProperties().isIgnoreDstStatusCode();
    }

    /**
     * Create a list of <CODE>Registers</CODE> from all the dataChannels from all the receive CapturedObject list from all the expected LoadProfiles
     *
     * @param ccoLpConfigs provides the captured objects from the meters
     * @return a list of Registers
     * @throws java.io.IOException if an error occurred during dataFetching or -Parsing
     */
    protected List<CapturedRegisterObject> createCapturedObjectRegisterList(ComposedCosemObject ccoLpConfigs) throws IOException {
        List<CapturedRegisterObject> channelRegisters = super.createCapturedObjectRegisterList(ccoLpConfigs);
        DLMSAttribute[] dlmsAttributesList = ccoLpConfigs.getDlmsAttributesList();
        if(dlmsAttributesList.length > 0 && dlmsAttributesList[0].getObisCode().equals(LTE_MONITORING_LOAD_PROFILE)){
            return lteMonitoringLoadProfileMapping(channelRegisters);
        }
        return channelRegisters;
    }

    private List<CapturedRegisterObject> lteMonitoringLoadProfileMapping(List<CapturedRegisterObject> capturedRegisters) {
        getMeterProtocol().journal("Processing LTE Monitoring Profile captured objects:");
        ArrayList<CapturedRegisterObject> processedCapturedRegisterObjects = new ArrayList<CapturedRegisterObject>();
        for(CapturedRegisterObject capturedRegisterObject : capturedRegisters){
            ObisCode obisCode = capturedRegisterObject.getObisCode();
            getMeterProtocol().journal(" - "+obisCode.toString());
            //ObisCode processedObisCode = new ObisCode(obisCode.getA(), obisCode.getB(), obisCode.getC(), obisCode.getD(), 0, obisCode.getF());
            ObisCode processedObisCode = obisCode;
            //TODO removed capturedRegisterObject.getDlmsAttribute().getSnAttribute(), check if needed
            capturedRegisterObject = new CapturedRegisterObject(processedObisCode, this.getMeterProtocol().getSerialNumber(), obisCode.getE(), DLMSClassId.findById(capturedRegisterObject.getClassId()));
            processedCapturedRegisterObjects.add(capturedRegisterObject);
        }
        return processedCapturedRegisterObjects;
    }

    @Override
    protected List<ChannelInfo> constructChannelInfos(List<CapturedRegisterObject> registers, ComposedCosemObject ccoRegisterUnits) throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        for (CapturedRegisterObject registerObject : registers) {
            if (registerObject.equals(DEFINABLE_LOAD_PROFILE_STATUS) ||
                    isLTEMonitoringRegister(registerObject)) {
                ChannelInfo ci = new ChannelInfo(channelInfos.size(), registerObject.getObisCode().toString(), Unit.getUndefined(), getMeterProtocol().getSerialNumber(), true);
                channelInfos.add(ci);
                continue;
            }
            if (!registerObject.getSerialNumber().equalsIgnoreCase("") && isDataObisCode(registerObject.getObisCode(), registerObject.getSerialNumber())) {
                if (this.getRegisterUnitMap().containsKey(registerObject)) {
                    registerObject.getAttribute();
                    ScalerUnit su = getScalerUnitForCapturedRegisterObject(registerObject, ccoRegisterUnits);
                    if (su.getUnitCode() != 0) {
                        ChannelInfo ci = new ChannelInfo(channelInfos.size(), registerObject.getObisCode().toString(), su.getEisUnit(), registerObject.getSerialNumber(), isCumulativeChannel(registerObject));
                        channelInfos.add(ci);
                    } else {
//                        ChannelInfo ci = new ChannelInfo(channelInfos.size(), registerObject.getObisCode().toString(), Unit.getUndefined(), registerObject.getSerialNumber(), true);
//                        channelInfos.add(ci);
                        throw new ProtocolException("Could not fetch a correct Unit for " + registerObject + " - unitCode was 0.");
                    }
                }
            }
        }
        return channelInfos;
    }

    private boolean isLTEMonitoringRegister(CapturedRegisterObject registerObject) {
        return  registerObject.getObisCode().toString().contains("0.1.25.11.") ||
                registerObject.getObisCode().toString().contains("0.1.25.6.") ||
                registerObject.getObisCode().toString().contains("0.1.94.31.");
    }

}
