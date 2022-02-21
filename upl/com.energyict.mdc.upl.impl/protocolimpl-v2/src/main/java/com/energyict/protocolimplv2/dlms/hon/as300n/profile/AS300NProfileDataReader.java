package com.energyict.protocolimplv2.dlms.hon.as300n.profile;


import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.identifiers.LoadProfileIdentifierById;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ProtocolExceptionMessageSeeds;
import com.energyict.protocolimpl.dlms.as220.ProfileLimiter;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.hon.as300n.AS300N;
import com.energyict.protocolimplv2.dlms.idis.am500.properties.IDISProperties;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class AS300NProfileDataReader {

    private static final ObisCode QUARTER_HOURLY_LOAD_PROFILE_OBISCODE = ObisCode.fromString("1.0.99.1.0.255");
    private static final ObisCode DAILY_LOAD_PROFILE_OBISCODE = ObisCode.fromString("1.0.99.2.0.255");
    private static final ObisCode BILLING_LOAD_PROFILE_OBISCODE = ObisCode.fromString("0.0.98.1.0.255");
    private static final ObisCode OBISCODE_MBUS_LOAD_PROFILE = ObisCode.fromString("0.x.24.3.0.255");
    private static final ObisCode PQ_PROFILE_OBISCODE = ObisCode.fromString("1.0.99.14.0.255");

    private static final ObisCode OBISCODE_NR_OF_POWER_FAILURES = ObisCode.fromString("0.0.96.7.9.255");
    private static final int DO_NOT_LIMIT_MAX_NR_OF_DAYS = 0;
    protected final AbstractDlmsProtocol protocol;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    protected final List<ObisCode> supportedLoadProfiles;
    private final long limitMaxNrOfDays;
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfosMap;
    private Map<ObisCode, Integer> intervalMap;
    private Map<ObisCode, Boolean> hasStatus = new HashMap<>();


    public AS300NProfileDataReader(AS300N protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this(protocol, DO_NOT_LIMIT_MAX_NR_OF_DAYS, collectedDataFactory, issueFactory);
    }

    public AS300NProfileDataReader(AS300N protocol, long limitMaxNrOfDays, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.protocol = protocol;
        this.limitMaxNrOfDays = limitMaxNrOfDays;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;

        supportedLoadProfiles = new ArrayList<>();
        supportedLoadProfiles.add(QUARTER_HOURLY_LOAD_PROFILE_OBISCODE);
        supportedLoadProfiles.add(DAILY_LOAD_PROFILE_OBISCODE);
        supportedLoadProfiles.add(OBISCODE_MBUS_LOAD_PROFILE);
        supportedLoadProfiles.add(BILLING_LOAD_PROFILE_OBISCODE);
        supportedLoadProfiles.add(PQ_PROFILE_OBISCODE);
    }

    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfileReaders) {
        List<CollectedLoadProfile> result = new ArrayList<>();

        for (LoadProfileReader loadProfileReader : loadProfileReaders) {
            CollectedLoadProfile collectedLoadProfile = this.collectedDataFactory.createCollectedLoadProfile(new LoadProfileIdentifierById(loadProfileReader.getLoadProfileId(), loadProfileReader.getProfileObisCode(), protocol.getOfflineDevice().getDeviceIdentifier()));

            List<ChannelInfo> channelInfos = getChannelInfosMap().get(loadProfileReader);
            ObisCode correctedLoadProfileObisCode = getCorrectedLoadProfileObisCode(loadProfileReader);
            if (isSupported(loadProfileReader) && (channelInfos != null)) {

                try {
                    ProfileGeneric profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(correctedLoadProfileObisCode, protocol.useDsmr4SelectiveAccessFormat());
                    DataContainer buffer = profileGeneric.getBuffer(getFromCalendar(loadProfileReader), getToCalendar(loadProfileReader));
                    Object[] loadProfileEntries = buffer.getRoot().getElements();
                    List<IntervalData> intervalDatas = new ArrayList<>();
                    IntervalValue value;

                    Date previousTimeStamp = null;
                    for (int index = 0; index < loadProfileEntries.length; index++) {
                        int status = 0;
                        int offset = 1;
                        DataStructure structure = buffer.getRoot().getStructure(index);
                        Date timeStamp;
                        if (structure.isOctetString(0)) {
                            OctetString octetString = structure.getOctetString(0);
                            timeStamp = octetString.toDate(AXDRDateTimeDeviationType.Negative, protocol.getTimeZone());
                        } else if (previousTimeStamp != null) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(previousTimeStamp);
                            cal.add(Calendar.SECOND, getIntervalMap().get(correctedLoadProfileObisCode));
                            timeStamp = cal.getTime();
                        } else {
                            Issue problem = issueFactory.createProblem(loadProfileReader, "Invalid interval data, timestamp should be the first captured object of type OctetString or NullData", correctedLoadProfileObisCode);
                            collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                            break;  //Stop parsing, move on
                        }
                        previousTimeStamp = timeStamp;

                        if (hasStatusInformation(correctedLoadProfileObisCode)) {
                            status = structure.getInteger(1);
                            offset = 2;
                        } else {
                            // no status is expected in this load profile - i.e. billing profile
                        }

                        final List<IntervalValue> values = new ArrayList<>();

                        for (int channel = 0; channel < channelInfos.size(); channel++) {
                            if (structure.isBigDecimal(channel + offset)) {
                                value = new IntervalValue(structure.getBigDecimalValue(channel + offset), status, getEiServerStatus(status));
                            } else {
                                // unwanted channel (like a date), will be removed by com.energyict.comserver.commands.core.SimpleComCommand.removeUnwantedChannels()
                                value = new IntervalValue(BigDecimal.ZERO, 0, 0);
                            }

                            values.add(value);
                        }

                        intervalDatas.add(new IntervalData(timeStamp, 0, 0, 0, values));
                    }

                    collectedLoadProfile.setCollectedIntervalData(intervalDatas, channelInfos);
                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
                        Issue problem = this.issueFactory.createProblem(loadProfileReader, e.getMessage(), correctedLoadProfileObisCode);
                        collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                    }
                }
            } else {
                Issue problem = this.issueFactory.createWarning(loadProfileReader, "loadProfileXnotsupported", correctedLoadProfileObisCode);
                collectedLoadProfile.setFailureInformation(ResultType.NotSupported, problem);
            }
            //TODO: see how to handle this validation in Connexo
            //validateLoadProfileData(collectedLoadProfile, loadProfileReader);
            result.add(collectedLoadProfile);
        }

        return result;
    }

    /**
     * Indicates whether or not we are the mirror connection to read load profile.
     */
    protected  boolean isNotMirroredOnDC() {
        return !((IDISProperties)protocol.getDlmsSessionProperties()).useBeaconMirrorDeviceDialect();
    }

    /*
     * Add a warning if no LP intervals were read out from a mirror logical device
      */
    protected void validateLoadProfileData(CollectedLoadProfile collectedLoadProfile, LoadProfileReader loadProfileReader) {
        if (isNotMirroredOnDC())
            return;
        if (collectedLoadProfile.getCollectedIntervalData().isEmpty() ||
                intervalWarningElapsed(collectedLoadProfile, loadProfileReader)) {
            ObisCode profileObisCode = collectedLoadProfile.getLoadProfileIdentifier().getLoadProfileObisCode();
            collectedLoadProfile.setFailureInformation(ResultType.Other, issueFactory.createWarning(profileObisCode, "loadProfileXIssue", profileObisCode, "Received 0 LP intervals from the mirror device. This could mean that the Beacon DC was not able to read out new LP data from the actual device. Please check the logbook of the Beacon device for issues."));
        }
    }

    private boolean intervalWarningElapsed(CollectedLoadProfile collectedLoadProfile, LoadProfileReader loadProfileReader) {
        Calendar intervalRangeEnd =  Calendar.getInstance();
        intervalRangeEnd.setTime(Date.from(collectedLoadProfile.getCollectedIntervalDataRange().upperEndpoint()));
        //TODO: see how to handle getBeaconWarningPeriod() in Connexo
//        getBeaconWarningPeriod().addTo(intervalRangeEnd);
        return Calendar.getInstance().compareTo(intervalRangeEnd) > 0;
    }

//    private Duration getBeaconWarningPeriod() {
//        //TODO To change the string value to the SystemParameterFactory.BEACON_DATA_WARNING_PERIOD constant
//        String beaconPeriod = MeteringWarehouse.getCurrent().getSystemProperty("beacon.data.warning period");
//        if (beaconPeriod == null) {
//            return new Duration(1, Duration.DAYS);
//        }
//        try {
//            return Duration.fromSystemParameterString(beaconPeriod);
//        } catch (NumberFormatException e) {
//            return new Duration(1, Duration.DAYS);
//        }
//    }


    private Calendar getFromCalendar(LoadProfileReader loadProfileReader) {
        ProfileLimiter profileLimiter = new ProfileLimiter(loadProfileReader.getStartReadingTime(), loadProfileReader.getEndReadingTime(), (int) getLimitMaxNrOfDays());
        Calendar fromCal = Calendar.getInstance(protocol.getTimeZone());
        fromCal.setTime(profileLimiter.getFromDate());
        fromCal.set(Calendar.SECOND, 0);
        return fromCal;
    }

    private Calendar getToCalendar(LoadProfileReader loadProfileReader) {
        ProfileLimiter profileLimiter = new ProfileLimiter(loadProfileReader.getStartReadingTime(), loadProfileReader.getEndReadingTime(), (int) getLimitMaxNrOfDays());
        Calendar toCal = Calendar.getInstance(protocol.getTimeZone());
        toCal.setTime(profileLimiter.getToDate());
        toCal.set(Calendar.SECOND, 0);
        return toCal;
    }

    protected ObisCode getCorrectedLoadProfileObisCode(LoadProfileReader loadProfileReader) {
        return protocol.getPhysicalAddressCorrectedObisCode(loadProfileReader.getProfileObisCode(), loadProfileReader.getMeterSerialNumber());
    }

    protected int getEiServerStatus(int protocolStatus) {
        int status = IntervalStateBits.OK;
        if ((protocolStatus & 0x80) == 0x80) {
            status = status | IntervalStateBits.POWERDOWN;
        }
        if ((protocolStatus & 0x20) == 0x20) {
            status = status | IntervalStateBits.BADTIME;
        }
        if ((protocolStatus & 0x04) == 0x04) {
            status = status | IntervalStateBits.CORRUPTED;
        }
        if ((protocolStatus & 0x02) == 0x02) {
            status = status | IntervalStateBits.BADTIME;
        }
        if ((protocolStatus & 0x01) == 0x01) {
            status = status | IntervalStateBits.CORRUPTED;
        }
        return status;
    }

    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) {
        List<CollectedLoadProfileConfiguration> result = new ArrayList<>();

        for (LoadProfileReader lpr : loadProfileReaders) {
            CollectedLoadProfileConfiguration lpc = this.collectedDataFactory.createCollectedLoadProfileConfiguration(lpr.getProfileObisCode(), lpr.getMeterSerialNumber());
            if (isSupported(lpr)) {
                List<ChannelInfo> channelInfos;
                ObisCode correctedLoadProfileObisCode = getCorrectedLoadProfileObisCode(lpr);
                try {
                    ProfileGeneric profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(correctedLoadProfileObisCode);
                    channelInfos = getChannelInfo(profileGeneric.getCaptureObjects(), lpc.getMeterSerialNumber(), correctedLoadProfileObisCode);
                    getChannelInfosMap().put(lpr, channelInfos);    //Remember these, they are re-used in method #getLoadProfileData();
                } catch (IOException e) {   //Object not found in IOL, should never happen
                    throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries() + 1);
                }
                lpc.setChannelInfos(channelInfos);
                lpc.setSupportedByMeter(true);
                lpc.setProfileInterval(getIntervalMap().get(correctedLoadProfileObisCode));
            } else {
                lpc.setSupportedByMeter(false);
            }
            result.add(lpc);
        }

        return result;
    }

    private Map<LoadProfileReader, List<ChannelInfo>> getChannelInfosMap() {
        if (channelInfosMap == null) {
            channelInfosMap = new HashMap<>();
        }
        return channelInfosMap;
    }

    public Map<ObisCode, Integer> getIntervalMap() {
        if (intervalMap == null) {
            intervalMap = new HashMap<>();
        }
        return intervalMap;
    }

    protected List<ChannelInfo> getChannelInfo(List<CapturedObject> capturedObjects, String serialNumber, ObisCode correctedLoadProfileObisCode) throws ProtocolException {
        List<ObisCode> channelObisCodes = new ArrayList<>();
        for (CapturedObject capturedObject : capturedObjects) {
            if (isChannel(capturedObject, correctedLoadProfileObisCode)) {
                channelObisCodes.add(capturedObject.getLogicalName().getObisCode());
            }
        }

        Map<ObisCode, Unit> unitMap = readUnits(correctedLoadProfileObisCode, channelObisCodes);

        List<ChannelInfo> channelInfos = new ArrayList<>();
        int counter = 0;
        for (ObisCode obisCode : channelObisCodes) {
            Unit unit = unitMap.get(obisCode);
            ChannelInfo channelInfo = new ChannelInfo(counter, obisCode.toString(), unit == null ? Unit.get(BaseUnit.UNITLESS) : unit, serialNumber);
            if (isCumulative(obisCode)) {
                channelInfo.setCumulative();
            }
            channelInfos.add(channelInfo);
            counter++;
        }
        return channelInfos;
    }

    /**
     * @param correctedLoadProfileObisCode the load profile obiscode. If it is not null, this implementation will additionally read out
     *                                     its interval (attribute 4) and cache it in the intervalMap
     * @param channelObisCodes             the obiscodes of the channels that we should read out the units for
     */
    public Map<ObisCode, Unit> readUnits(ObisCode correctedLoadProfileObisCode, List<ObisCode> channelObisCodes) throws ProtocolException {
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
        if (correctedLoadProfileObisCode != null) {
            profileIntervalAttribute = new DLMSAttribute(correctedLoadProfileObisCode, 4, DLMSClassId.PROFILE_GENERIC);
            attributes.put(correctedLoadProfileObisCode, profileIntervalAttribute);
        }

        ComposedCosemObject composedCosemObject = new ComposedCosemObject(protocol.getDlmsSession(), protocol.getDlmsSessionProperties().isBulkRequest(), new ArrayList<>(attributes.values()));

        if (correctedLoadProfileObisCode != null) {
            try {
                AbstractDataType attribute = composedCosemObject.getAttribute(profileIntervalAttribute);
                getIntervalMap().put(correctedLoadProfileObisCode, attribute.intValue());
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

    private boolean isCumulative(ObisCode obisCode) {
        return ParseUtils.isObisCodeCumulative(obisCode) || isNrOfPowerFailures(obisCode);
    }

    private boolean isNrOfPowerFailures(ObisCode obisCode) {
        return OBISCODE_NR_OF_POWER_FAILURES.equals(obisCode);
    }

    /**
     * Check if the captured_object can be considered as an EIServer channel.
     * Registers, extended registers and demand registers are used as channels.
     * Captured_objects with the obiscode of the clock (0.0.1.0.0.255), or the status register (0.x.96.10.x.255) are not considered as channels.
     * <p/>
     * In case of an unknown dlms class, or an unknown obiscode, a proper exception is thrown.
     */
    private boolean isChannel(CapturedObject capturedObject, ObisCode correctedLoadProfileObisCode) throws ProtocolException {
        int classId = capturedObject.getClassId();
        ObisCode obisCode = capturedObject.getLogicalName().getObisCode();
        if (!isCaptureTime(capturedObject) && (classId == DLMSClassId.REGISTER.getClassId() || classId == DLMSClassId.EXTENDED_REGISTER.getClassId() || classId == DLMSClassId.DEMAND_REGISTER.getClassId())) {
            return true;
        }
        if (isClock(obisCode) || isCaptureTime(capturedObject)) {
            return false;
        }
        if (isProfileStatus(obisCode)) {
            hasStatus.put(correctedLoadProfileObisCode, true);
            return false;
        }
        throw new ProtocolException("Unexpected captured_object in load profile '" + correctedLoadProfileObisCode + "': " + capturedObject.toString());
    }

    private boolean isProfileStatus(ObisCode obisCode) {
        return (obisCode.getA() == 0 && (obisCode.getB() >= 0 && obisCode.getB() <= 6) && obisCode.getC() == 96 && obisCode.getD() == 10 && (obisCode.getE() == 1 || obisCode.getE() == 2 || obisCode.getE() == 3) && obisCode.getF() == 255);
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

    public long getLimitMaxNrOfDays() {
        return limitMaxNrOfDays;
    }

    protected boolean hasStatusInformation(ObisCode correctedLoadProfileObisCode) {
        if (hasStatus.containsKey(correctedLoadProfileObisCode)) {
            return hasStatus.get(correctedLoadProfileObisCode);
        }

        return false;
    }
}