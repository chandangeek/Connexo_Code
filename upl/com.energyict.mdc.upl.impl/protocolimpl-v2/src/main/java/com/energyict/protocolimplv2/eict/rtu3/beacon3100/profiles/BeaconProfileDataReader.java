package com.energyict.protocolimplv2.eict.rtu3.beacon3100.profiles;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.identifiers.LoadProfileIdentifierById;
import com.energyict.mdc.upl.NotInObjectListException;
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
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ProtocolExceptionMessageSeeds;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.DLMSProfileIntervals;

import java.io.IOException;
import java.util.*;

public class BeaconProfileDataReader {

    private static final ObisCode GSM_DIAGNOSTICS_PROFILE           = ObisCode.fromString("0.0.99.13.0.255");
    private static final ObisCode LTE_MONITORING_PROFILE            = ObisCode.fromString("0.0.99.18.0.255");
    private static final ObisCode WWAN_STATE_TRANSITION_PROFILE     = ObisCode.fromString("0.162.96.208.0.255");
    private static final ObisCode METROLOGY_PROFILE                 = ObisCode.fromString("1.0.99.1.0.255");
    private static final ObisCode TEMPERATURE_PROFILE               = ObisCode.fromString("6.0.99.1.0.255");
    private static final ObisCode CPU_STATISTICS_PROFILE            = ObisCode.fromString("0.194.96.160.0.255");
    private static final ObisCode OS_MEMORY_STATISTICS_PROFILE      = ObisCode.fromString("0.194.96.161.0.255");


    protected final AbstractDlmsProtocol protocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    protected final List<ObisCode> supportedLoadProfiles;

    private Map<LoadProfileReader, List<ChannelInfo>> channelInfosMap;
    private Map<ObisCode, Integer> capturePeriodMap;
    private Map<ObisCode, Boolean> hasStatus = new HashMap<>();
    private List<CollectedLoadProfileConfiguration> loadProfileConfigurations;

    public BeaconProfileDataReader(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {

        this.protocol = protocol;

        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;

        supportedLoadProfiles = new ArrayList<>();
        supportedLoadProfiles.add(GSM_DIAGNOSTICS_PROFILE);
        supportedLoadProfiles.add(LTE_MONITORING_PROFILE);
        supportedLoadProfiles.add(WWAN_STATE_TRANSITION_PROFILE);
        supportedLoadProfiles.add(METROLOGY_PROFILE);
        supportedLoadProfiles.add(TEMPERATURE_PROFILE);
        supportedLoadProfiles.add(CPU_STATISTICS_PROFILE);
        supportedLoadProfiles.add(OS_MEMORY_STATISTICS_PROFILE);
    }



    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfileReaders) {
        List<CollectedLoadProfile> result = new ArrayList<>();

        fetchLoadProfileConfiguration(loadProfileReaders);

        for (LoadProfileReader loadProfileReader : loadProfileReaders) {

            CollectedLoadProfile collectedLoadProfile = this.collectedDataFactory.createCollectedLoadProfile(
                    new LoadProfileIdentifierById(loadProfileReader.getLoadProfileId(),
                            loadProfileReader.getProfileObisCode(),
                            protocol.getOfflineDevice().getDeviceIdentifier()));

            ObisCode profileObisCode = loadProfileReader.getProfileObisCode();

            if (isSupported(loadProfileReader)) {
                try {
                    CollectedLoadProfileConfiguration lpc = getLoadProfileConfiguration(loadProfileReader);
                    List<IntervalData> intervalDatas = new ArrayList<>();

                    Calendar fromCalendar = getFromCalendar(loadProfileReader);
                    Calendar toCalendar = getToCalendar(loadProfileReader);

                    ProfileGeneric profileGeneric = null;
                    profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(profileObisCode);

                    DLMSProfileIntervals intervalParser = null;

                    byte[] encodedData = profileGeneric.getBufferData(fromCalendar, toCalendar);

                    if (profileObisCode.equals(LTE_MONITORING_PROFILE)) {
                        intervalParser = new LTEMonitoringProfile(encodedData);
                    } else if (profileObisCode.equals(GSM_DIAGNOSTICS_PROFILE)) {
                        intervalParser = new GSMDiagnosticsProfile(encodedData);
                    } else if (profileObisCode.equals(WWAN_STATE_TRANSITION_PROFILE)) {
                        intervalParser = new WWANStateTransitionProfile(encodedData);
                    } else if (profileObisCode.equals(OS_MEMORY_STATISTICS_PROFILE)) {
                        intervalParser = new OSMemoryStatisticsProfile(encodedData);
                    } else {
                        //default parser with no hard-coded channels, so the information about each can be extracted
                        intervalParser = new DLMSProfileIntervals(encodedData, DLMSProfileIntervals.DefaultClockMask, 0, 0xfffe, null);
                    }

                    intervalDatas = intervalParser.parseIntervals(lpc.getProfileInterval(), protocol.getTimeZone());
                    collectedLoadProfile.setCollectedIntervalData(intervalDatas, getChannelInfosMap().get(loadProfileReader));

                }  catch (NotInObjectListException e) {
                    Issue problem = this.issueFactory.createWarning(loadProfileReader, "Object not in DLMS list: "+e.getLocalizedMessage(), profileObisCode);
                    collectedLoadProfile.setFailureInformation(ResultType.NotSupported, problem);
                }  catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
                        Issue problem = this.issueFactory.createProblem(loadProfileReader, e.getMessage(), profileObisCode);
                        collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                    }
                }
            } else {
                Issue problem = this.issueFactory.createWarning(loadProfileReader, "Load profile not supported: "+profileObisCode, profileObisCode);
                collectedLoadProfile.setFailureInformation(ResultType.NotSupported, problem);
            }
            result.add(collectedLoadProfile);
        }

        return result;
    }

    private CollectedLoadProfileConfiguration getLoadProfileConfiguration(LoadProfileReader loadProfileReader) throws ProtocolException {
        for (CollectedLoadProfileConfiguration loadProfileConfig : loadProfileConfigurations){
            if (loadProfileConfig.getObisCode().equals(loadProfileReader.getProfileObisCode())){
                return loadProfileConfig;
            }
        }
        throw new ProtocolException("Cannot find load profile in cached configuration: " + loadProfileReader.getProfileObisCode());
    }



    private Calendar getFromCalendar(LoadProfileReader profileReader) {
        Calendar fromCal = Calendar.getInstance(protocol.getTimeZone());
        fromCal.setTime(profileReader.getStartReadingTime());
        fromCal.set(Calendar.SECOND, 0);
        return fromCal;
    }

    private Calendar getToCalendar(LoadProfileReader profileReader) {
        Calendar toCal = Calendar.getInstance(protocol.getTimeZone());
        toCal.setTime(profileReader.getEndReadingTime());
        toCal.set(Calendar.SECOND, 0);
        return toCal;
    }


    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) {
        if (loadProfileConfigurations!=null ){
            // already collected, return cached value
            return loadProfileConfigurations;
        }

        loadProfileConfigurations = new ArrayList<>();

        for (LoadProfileReader lpr : loadProfileReaders) {
            CollectedLoadProfileConfiguration lpc = this.collectedDataFactory.createCollectedLoadProfileConfiguration(lpr.getProfileObisCode(), lpr.getMeterSerialNumber());
            if (isSupported(lpr)) {
                List<ChannelInfo> channelInfos;
                ObisCode profileObisCode = lpc.getObisCode();

                readCapturePeriod(profileObisCode);

                if (profileObisCode.equals(LTE_MONITORING_PROFILE)){
                    // use custom-mapped channels
                    channelInfos = LTEMonitoringProfile.getHardcodedChannelInfos(lpc);
                } else if (profileObisCode.equals(GSM_DIAGNOSTICS_PROFILE)) {
                    channelInfos = GSMDiagnosticsProfile.getHardcodedChannelInfos(lpc);
                } else if (profileObisCode.equals(WWAN_STATE_TRANSITION_PROFILE)) {
                    channelInfos = WWANStateTransitionProfile.getHardcodedChannelInfos(lpc);
                } else if (profileObisCode.equals(OS_MEMORY_STATISTICS_PROFILE)){
                    channelInfos = OSMemoryStatisticsProfile.getHardcodedChannelInfos(lpc);
                }  else {
                    // standard read-out without mapping
                    try {
                        ProfileGeneric profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(profileObisCode);
                        channelInfos = getStandardProfileChannelInfo(profileGeneric.getCaptureObjects(), lpc.getMeterSerialNumber(), profileObisCode);
                    } catch (IOException e) {   //Object not found in IOL, should never happen
                        throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries() + 1);
                    }
                }

                getChannelInfosMap().put(lpr, channelInfos);
                lpc.setChannelInfos(channelInfos);
                lpc.setSupportedByMeter(true);
                lpc.setProfileInterval(getCapturePeriodMap().get(profileObisCode));
            } else {
                lpc.setSupportedByMeter(false);
            }
            loadProfileConfigurations.add(lpc);
        }
        return loadProfileConfigurations;
    }

    private void readCapturePeriod(ObisCode profileObisCode) {
        try {
            ProfileGeneric profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(profileObisCode);
            int capturePeriod = profileGeneric.getCapturePeriod();

            getCapturePeriodMap().put(profileObisCode, capturePeriod);
        } catch (IOException e) {
                throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries() + 1);
        }
    }

    private Map<LoadProfileReader, List<ChannelInfo>> getChannelInfosMap() {
        if (channelInfosMap == null) {
            channelInfosMap = new HashMap<>();
        }
        return channelInfosMap;
    }

    public Map<ObisCode, Integer> getCapturePeriodMap() {
        if (capturePeriodMap == null) {
            capturePeriodMap = new HashMap<>();
        }
        return capturePeriodMap;
    }

    protected List<ChannelInfo> getStandardProfileChannelInfo(List<CapturedObject> capturedObjects, String serialNumber, ObisCode profileObisCode) throws ProtocolException {
        List<ObisCode> channelObisCodes = new ArrayList<>();
        for (CapturedObject capturedObject : capturedObjects) {
            if (isChannel(capturedObject, profileObisCode)) {
                channelObisCodes.add(capturedObject.getLogicalName().getObisCode());
            }
        }

        Map<ObisCode, Unit> unitMap = readUnitsFromStandardProfile(profileObisCode, channelObisCodes);

        List<ChannelInfo> channelInfos = new ArrayList<>();
        int counter = 0;
        for (ObisCode obisCode : channelObisCodes) {
            Unit unit = unitMap.get(obisCode);
            ChannelInfo channelInfo = new ChannelInfo(counter, obisCode.toString(), unit == null ? Unit.get(BaseUnit.UNITLESS) : unit, serialNumber);
            channelInfos.add(channelInfo);
            counter++;
        }
        return channelInfos;
    }

    //TODO: refactor and move this duplicate code into some higher class
    protected Map<ObisCode, Unit> readUnitsFromStandardProfile(ObisCode profileObisCode, List<ObisCode> channelObisCodes) throws ProtocolException {
        Map<ObisCode, Unit> result = new HashMap<>();

        Map<ObisCode, DLMSAttribute> attributes = new HashMap<>();
        for (ObisCode channelObisCode : channelObisCodes) {
            UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.protocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), channelObisCode);
            if (uo != null) {
                DLMSAttribute unitAttribute;
                if (uo.getDLMSClassId() == DLMSClassId.REGISTER) {
                    unitAttribute = new DLMSAttribute(channelObisCode, RegisterAttributes.SCALER_UNIT.getAttributeNumber(), uo.getClassID());
                } else if (uo.getDLMSClassId() == DLMSClassId.EXTENDED_REGISTER) {
                    unitAttribute = new DLMSAttribute(channelObisCode, ExtendedRegisterAttributes.UNIT.getAttributeNumber(), uo.getClassID());
                } else if (uo.getDLMSClassId() == DLMSClassId.DEMAND_REGISTER) {
                    unitAttribute = new DLMSAttribute(channelObisCode, DemandRegisterAttributes.UNIT.getAttributeNumber(), uo.getClassID());
                } else {
                    throw new ProtocolException("Unexpected captured_object in load profile: " + uo.getDescription());
                }
                attributes.put(channelObisCode, unitAttribute);
            }
        }

        //Also read out the profile interval in this bulk request
        DLMSAttribute profileIntervalAttribute = null;
        if (profileObisCode != null) {
            profileIntervalAttribute = new DLMSAttribute(profileObisCode, 4, DLMSClassId.PROFILE_GENERIC);
            attributes.put(profileObisCode, profileIntervalAttribute);
        }

        ComposedCosemObject composedCosemObject = new ComposedCosemObject(protocol.getDlmsSession(), protocol.getDlmsSessionProperties().isBulkRequest(), new ArrayList<>(attributes.values()));

        if (profileObisCode != null) {
            try {
                AbstractDataType attribute = composedCosemObject.getAttribute(profileIntervalAttribute);
                getCapturePeriodMap().put(profileObisCode, attribute.intValue());
            } catch (IOException e) {
                throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries() + 1);
            }
        }

        for (ObisCode channelObisCode : channelObisCodes) {
            DLMSAttribute dlmsAttribute = attributes.get(channelObisCode);
            if (dlmsAttribute != null) {
                try {
                    result.put(channelObisCode, new ScalerUnit(composedCosemObject.getAttribute(dlmsAttribute)).getEisUnit());
                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
                        throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries() + 1);
                    } //Else: throw ConnectionCommunicationException
                } catch (IllegalArgumentException e) {
                    throw new CommunicationException(ProtocolExceptionMessageSeeds.UNEXPECTED_RESPONSE);
                }
            } else {
                String message = "The OBIS code " + channelObisCode + " found in the meter load profile capture objects list, is NOT supported by the meter itself." +
                        " If ReadCache property is not active, try again with this property enabled. Otherwise, please reprogram the meter with a valid set of capture objects.";

                if (protocol.getDlmsSessionProperties().validateLoadProfileChannels()) {
                    throw new ProtocolException(message);
                } else {
                    protocol.getLogger().warning(message);
                }
            }
        }
        return result;
    }


    private boolean isChannel(CapturedObject capturedObject, ObisCode correctedLoadProfileObisCode) throws ProtocolException {
        int classId = capturedObject.getClassId();
        ObisCode obisCode = capturedObject.getLogicalName().getObisCode();
        if (    !isCaptureTime(capturedObject)
                && (   classId == DLMSClassId.REGISTER.getClassId()
                    || classId == DLMSClassId.EXTENDED_REGISTER.getClassId()
                    || classId == DLMSClassId.DEMAND_REGISTER.getClassId())) {
            return true;
        }

        if (isClock(obisCode) || isCaptureTime(capturedObject)) {
            return false;
        }

        return true;
    }

    private boolean isClock(ObisCode obisCode) {
        return (Clock.getDefaultObisCode().equals(obisCode));
    }

    private boolean isCaptureTime(CapturedObject capturedObject) {
        return (capturedObject.getAttributeIndex() == 5 && capturedObject.getClassId() == DLMSClassId.EXTENDED_REGISTER.getClassId()) || (capturedObject.getAttributeIndex() == 6 && capturedObject.getClassId() == DLMSClassId.DEMAND_REGISTER.getClassId());
    }

    private boolean isSupported(LoadProfileReader lpr) {
        for (ObisCode supportedLoadProfile : supportedLoadProfiles) {
            if (lpr.getProfileObisCode().equalsIgnoreBChannel(supportedLoadProfile)) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasStatusInformation(ObisCode correctedLoadProfileObisCode) {
        if (hasStatus.containsKey(correctedLoadProfileObisCode)) {
            return hasStatus.get(correctedLoadProfileObisCode);
        }

        return false;
    }
}