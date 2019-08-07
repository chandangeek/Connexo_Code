package com.energyict.protocolimplv2.nta.esmr50.common.loadprofiles;


import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.DLMSProfileIntervals;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierById;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.CapturedRegisterObject;
import com.energyict.protocolimplv2.nta.dsmr40.common.profiles.Dsmr40LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.esmr50.common.ESMR50Protocol;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.ESMR50RegisterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

public class ESMR50LoadProfileBuilder extends Dsmr40LoadProfileBuilder {

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
       Montly billing profiles
    */
    public static final ObisCode AMR_PROFILE_STATUS_CODE_E_METER_MONTLY = ObisCode.fromString("0.0.96.10.6.255");
    public static final ObisCode AMR_PROFILE_STATUS_CODE_MBUS_MONTLY = ObisCode.fromString("0.0.96.10.7.255");
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

    private static final boolean DO_IGNORE_DST_STATUS_CODE = true;

    /**
     * Default constructor
     *
     * @param meterProtocol the {@link #meterProtocol}
     */
    public ESMR50LoadProfileBuilder(AbstractDlmsProtocol meterProtocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
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


        testObisCode = this.getMeterProtocol().getPhysicalAddressCorrectedObisCode(AMR_PROFILE_STATUS_CODE_E_METER_MONTLY, serialNumber);
        if (testObisCode != null) {
            isStatusObisCode |= testObisCode.equals(obisCode);
        } else {
            return false;
        }

        testObisCode = this.getMeterProtocol().getPhysicalAddressCorrectedObisCode(AMR_PROFILE_STATUS_CODE_MBUS_MONTLY, serialNumber);
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
                                DLMSProfileIntervals.DefaultClockMask,
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
//                    if (lpObisCode.equals(LTE_MONITORING_LOAD_PROFILE)) {
//                        profileData.setChannelInfos(lpr.getChannelInfos()); TODO LTE_MONITORING_LOAD_PROFILE must be tested to see if it reads the channels correctly
//                    }

                    collectedLoadProfile.setCollectedIntervalData(parsedIntervals, getChannelInfoMap().get(lpr));
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
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders){
        List<CollectedLoadProfileConfiguration> loadProfileConfigurationList = super.fetchLoadProfileConfiguration(loadProfileReaders);

        for (CollectedLoadProfileConfiguration lpc : loadProfileConfigurationList){
            if (lpc.getObisCode().equals(LTE_MONITORING_LOAD_PROFILE)){
                lpc.setChannelInfos(getLTEMonitoringChannelInfos(lpc));
            }
        }

        return loadProfileConfigurationList;
    }

    /** Hardcoded profiles for LTE Monitoring

     Clock 8, 0.0.1.0.0.255, 2, 0 (like all loadprofiles)
     GSM diagnostic, operator 151, 0.1.25.11.0.255, 2, 0. This attribute consists itself of a structure of five (so this will mean 5 channels in EIServer):
         T3402: long-unsigned (timer in seconds, used on PLMN selection procedure and sent by the network to the modem)
         T3412: long-unsigned (timer in seconds used to manage the periodic tracking area updating procedure and sent by the network to the modem)
         RSRQ: unsigned (represents the signal quality)
         RSRP: unsigned (represents the signal level )
         qRxlevMin: integer (specifies the minimum required Rx level in the cell in dBm)
     GSM diagnostic, cell_info 47, 0.1.25.6.0.255, 6, 1. This attribute consists itself of a structure of seven (so this will mean 7 channels in EIServer):
         cell_ID: long-unsigned (Four-byte cell ID in hexadecimal format)
         location_ID: long-unsigned (Two-byte location area code (LAC) in the case of GSM networks or Tracking Area Code (TAC) in the case of UMTS, CDMA or LTE networks in hexadecimal format )
         signal_quality: unsigned (Represents the signal quality)
         ber: unsigned (Bit Error Rate (BER) measurement in percent)
         mcc: long-unsigned (Mobile Country Code of the serving network)
         mnc: long-unsigned (Mobile Network Code of the serving network)
         channel_number double-long-unsigned (Represents the absolute radio-frequency channel number (ARFCN or eaRFCN for LTE network))
     GSM diagnostic, adjacent_cells 47, 0.1.25.6.0.255, 7, 1. This attribute is an array and within each cell of the array is a structure of two. Because the number of entries in the array will never be more than 3, this will be 6 channels in EIServer). The structure of two:
         cell_ID: double-long-unsigned (Four-byte cell ID in hexadecimal format)
         signal_quality: unsigned (Represents the signal quality)
     LTE connection rejection 1, 0.1.94.31.7.255, 2, 0. This attribute consists itself of a structure of four (so this will mean 4 channels in EIServer):
         last_reject_cause unsigned (provides the last rejected cause on network)
         last_rejected_mcc long-unsigned (Mobile Country Code of the last rejected network)
         last_rejected_mnc long-unsigned (Mobile Network Code of the last rejected network)
         timestamp_last_rejection date_time (specifies the date and time of the rejection). This date_time can be stored in EIServer in epoch format.
     *
     * @param lpc
     */
    private List<ChannelInfo> getLTEMonitoringChannelInfos(CollectedLoadProfileConfiguration lpc) {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();

        int ch = 0;
        channelInfos.add(new ChannelInfo(ch++, Clock.getDefaultObisCode().toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), true));

        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_MONITORING_LTE_QUALITY_OF_SERVICE_T3402.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_MONITORING_LTE_QUALITY_OF_SERVICE_T3412.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_MONITORING_LTE_QUALITY_OF_SERVICE_RSRQ.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_MONITORING_LTE_QUALITY_OF_SERVICE_RSRP.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_MONITORING_LTE_QUALITY_OF_SERVICE_Q_RXLEV_MIN.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));

        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_CELL_INFO_CELL_ID.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_CELL_INFO_LOCATION_ID.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_CELL_INFO_SIGNAL_QUALITY.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_CELL_INFO_BER.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_CELL_INFO_MCC.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_CELL_INFO_MNC.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_CELL_INFO_CHANNEL_NUMBER.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));

        //1
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_ADJACENT_CELLS_CELLID.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_ADJACENT_CELLS_SIGNAL_QUALITY.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        //2
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_ADJACENT_CELLS_CELLID_2.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_ADJACENT_CELLS_SIGNAL_QUALITY_2.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        //3
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_ADJACENT_CELLS_CELLID_3.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.GSM_DIAGNOSTIC_ADJACENT_CELLS_SIGNAL_QUALITY_3.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));

        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_CONNECTION_REJECTION_LAST_REJECTED_CAUSE.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_CONNECTION_REJECTION_LAST_REJECTED_MCC.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_CONNECTION_REJECTION_LAST_REJECTED_MNC.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));
        channelInfos.add(new ChannelInfo(ch++, ESMR50RegisterFactory.LTE_CONNECTION_REJECTION_TIMESTAMP_LAST_REJECTION.toString(), Unit.getUndefined(), lpc.getMeterSerialNumber(), false));

        return channelInfos;
    }

    private boolean mustUseSlaveTimeZone(ObisCode profileObisCode) {
        if (ESMR50Protocol.MBUS_LP1_OBISCODE.equals(profileObisCode)){
            return true;
        }

        if (ESMR50Protocol.MBUS_LP2_OBISCODE_SAME_AS_EMETER_LP2.equals(profileObisCode)){
            return true;
        }

        if (ESMR50Protocol.MBUS_LP3_OBISCODE_SAME_AS_EMETER_LP3.equals(profileObisCode)){
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
