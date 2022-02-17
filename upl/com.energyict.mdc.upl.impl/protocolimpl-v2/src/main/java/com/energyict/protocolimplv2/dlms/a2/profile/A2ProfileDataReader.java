package com.energyict.protocolimplv2.dlms.a2.profile;

import com.energyict.mdc.identifiers.LoadProfileIdentifierById;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineDevice;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.DateTimeOctetString;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.dlms.as220.ProfileLimiter;
import com.energyict.protocolimplv2.dlms.a2.A2;
import com.energyict.protocolimplv2.dlms.a2.registers.FirmwareVersion;
import com.energyict.protocolimplv2.dlms.ei7.profiles.EI7LoadProfileDataReader;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class A2ProfileDataReader {

    private static final ObisCode HOURLY_LOAD_PROFILE  = ObisCode.fromString("7.0.99.99.2.255");
    private static final ObisCode DAILY_LOAD_PROFILE   = ObisCode.fromString("7.0.99.99.3.255");
    private static final ObisCode MONTHLY_LOAD_PROFILE = ObisCode.fromString("7.0.99.99.4.255");
    private static final ObisCode CURRENT_DIAGNOSTIC_OBISCODE = ObisCode.fromString( "7.0.96.5.1.255");

    private static final ObisCode MAXIMUM_CONVENTIONAL_CONV_GAS_FLOW      = ObisCode.fromString("7.0.43.45.0.255");
    private static final ObisCode MAXIMUM_CONVENTIONAL_CONV_GAS_FLOW_TIME = ObisCode.fromString("7.0.43.45.5.255");

    private static final int DAILY_LOAD_PROFILE_ONEMORE_INTERVAL = -1;

    private final A2 protocol;
    private final List<ObisCode> supportedLoadProfiles;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final OfflineDevice offlineDevice;
    private final long limitMaxNrOfDays;

    private Map<ObisCode, Boolean> hasStatus = new HashMap<>();
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfosMap;
    private Map<ObisCode, Integer> intervalMap;

    public A2ProfileDataReader(A2 protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, OfflineDevice offlineDevice, long limitMaxNrOfDays) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.offlineDevice = offlineDevice;
        this.limitMaxNrOfDays = limitMaxNrOfDays;
        supportedLoadProfiles = new ArrayList<>();
        supportedLoadProfiles.add(HOURLY_LOAD_PROFILE);
        supportedLoadProfiles.add(DAILY_LOAD_PROFILE);
        supportedLoadProfiles.add(MONTHLY_LOAD_PROFILE);
    }

    // use this constructor to create a Reader which will read only specified supported load profiles
    public A2ProfileDataReader(A2 protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, OfflineDevice offlineDevice, long limitMaxNrOfDays, List<ObisCode> supportedLoadProfiles) {
        this.protocol = protocol;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.offlineDevice = offlineDevice;
        this.limitMaxNrOfDays = limitMaxNrOfDays;
        this.supportedLoadProfiles = supportedLoadProfiles;
    }

    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) {
        List<CollectedLoadProfileConfiguration> result = new ArrayList<>();

        for (LoadProfileReader lpr : loadProfileReaders) {
            CollectedLoadProfileConfiguration lpc = collectedDataFactory.createCollectedLoadProfileConfiguration(lpr.getProfileObisCode(), offlineDevice.getDeviceIdentifier(), lpr.getMeterSerialNumber());
            if (isSupported(lpr)) {
                List<ChannelInfo> channelInfos;
                try {
                    ProfileGeneric profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpr.getProfileObisCode());
                    channelInfos = getChannelInfo(profileGeneric.getCaptureObjects(), lpr.getMeterSerialNumber(), lpr.getProfileObisCode());
                    getChannelInfosMap().put(lpr, channelInfos); // Remember these, they are re-used in method #getLoadProfileData();
                } catch (IOException e) {   // Object not found in IOL, should never happen
                    throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries() + 1);
                }
                lpc.setChannelInfos(channelInfos);
                lpc.setSupportedByMeter(true);
                lpc.setProfileInterval(getIntervalMap().get(lpr.getProfileObisCode()));
            } else {
                lpc.setSupportedByMeter(false);
            }
            result.add(lpc);
        }
        return result;
    }

    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfileReaders) {
        List<CollectedLoadProfile> result = new ArrayList<>();
        for (LoadProfileReader loadProfileReader : loadProfileReaders) {
            CollectedLoadProfile collectedLoadProfile = collectedDataFactory.createCollectedLoadProfile(new LoadProfileIdentifierById(loadProfileReader.getLoadProfileId(), loadProfileReader.getProfileObisCode(), offlineDevice.getDeviceIdentifier()));
            List<ChannelInfo> channelInfos = getChannelInfosMap().get(loadProfileReader);
            ObisCode correctedLoadProfileObisCode = getCorrectedLoadProfileObisCode(loadProfileReader);
            if (isSupported(loadProfileReader) && (channelInfos != null)) {
                try {
                    FirmwareVersion firmwareVersion = new FirmwareVersion(protocol.getDlmsSession()
                            .getCosemObjectFactory()
                            .getData(ObisCode.fromString("7.1.0.2.1.255"))
                            .getValueAttr()
                            .getOctetString());

                    Calendar fromCalendar = getFromCalendar(loadProfileReader);
                    Calendar toCalendar = getToCalendar(loadProfileReader);
                    ProfileGeneric profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(correctedLoadProfileObisCode, protocol.useDsmr4SelectiveAccessFormat());
                    DataContainer buffer;
                    if (DAILY_LOAD_PROFILE.equals(loadProfileReader.getProfileObisCode())) {
                        fromCalendar.add(Calendar.DAY_OF_YEAR, DAILY_LOAD_PROFILE_ONEMORE_INTERVAL);
                    }

                    protocol.journal("From Calendar " + fromCalendar.getTime());
                    protocol.journal("To Calendar " + toCalendar.getTime());

                    if (HOURLY_LOAD_PROFILE.equals(loadProfileReader.getProfileObisCode()) && firmwareVersion.getMajor()==1 && firmwareVersion.getMinor()==4) {
                        Calendar actualCalendar = Calendar.getInstance(protocol.getTimeZone());
                        SelectiveEntryFilter filter = new SelectiveEntryFilter(fromCalendar, toCalendar, actualCalendar);
                        protocol.journal("Actual Calendar " + actualCalendar.getTime());
                        protocol.journal("From Index : " + filter.getFromIndex() + ", To Index " + filter.getToIndex());
                        buffer = profileGeneric.getBuffer(filter.getFromIndex(), filter.getToIndex(), 1, 0);
                    }
                    else {
                        buffer = profileGeneric.getBuffer(fromCalendar, toCalendar);
                    }

                    List<IntervalData> intervalData = readIntervalDataFromBuffer(correctedLoadProfileObisCode, buffer);

                    protocol.journal("[" + correctedLoadProfileObisCode + "] " + "Parsed intervals: " + intervalData.size());

                    collectedLoadProfile.setCollectedIntervalData(intervalData, channelInfos);
                    collectedLoadProfile.setDoStoreOlderValues(true);
                } catch (DataAccessResultException e) {
                    // this can happen when the load profile is read twice in the same time window (day for daily lp), then the data block is not accessible.
                    // It could also happen when the load profile is not configured properly.
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
                        String message = String.join(" ","Load profile was probably already read today, try modifying the 'last reading' date in the load profile properties.", e.getMessage());
                        Issue problem = issueFactory.createWarning(loadProfileReader, message, correctedLoadProfileObisCode);
                        collectedLoadProfile.setFailureInformation(ResultType.DataIncomplete, problem);
                    }
                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
                        Issue problem = issueFactory.createProblem(loadProfileReader, e.getMessage(), correctedLoadProfileObisCode);
                        collectedLoadProfile.setFailureInformation(ResultType.InCompatible, problem);
                    }
                }
            } else {
                Issue problem = issueFactory.createWarning(loadProfileReader, "loadProfileXnotsupported", correctedLoadProfileObisCode);
                collectedLoadProfile.setFailureInformation(ResultType.NotSupported, problem);
            }
            result.add(collectedLoadProfile);
        }

        return result;
    }

    private List<IntervalData> readIntervalDataFromBuffer(ObisCode correctedLoadProfileObisCode, DataContainer buffer) throws ProtocolException {
        List<IntervalData> intervalData = new ArrayList<>();
        Object[] loadProfileEntries;

        loadProfileEntries = buffer.getRoot().getElements();

        for (int index = 0; index < loadProfileEntries.length; index++) {
            int status = 0;
            int offset = 0;
            DataStructure structure = buffer.getRoot().getStructure(index);
            int bufferSize = structure.getNrOfElements();
            Date timeStamp = null;
            // Timestamp should be at index 0
            if (structure.isLong(offset)) {
                long unixTime = structure.getLong(offset);
                timeStamp = truncateSeconds(unixTime * 1000L);
                offset++;
            }
            if (hasStatusInformation(correctedLoadProfileObisCode)) {
                status = structure.getInteger(offset);
                offset++;
            }

            final List<IntervalValue> values = new ArrayList<>();

            for (int bufferIndex = offset; bufferIndex < bufferSize; bufferIndex++) {
                if (structure.isBigDecimal(bufferIndex)) {
                    if (!isMonthlyExtraStatus(correctedLoadProfileObisCode, bufferIndex)) {
                        IntervalValue value = new IntervalValue(structure.getBigDecimalValue(bufferIndex), status, getEiServerStatus(status));
                        values.add(value);
                    }
                } else if (structure.isOctetString(bufferIndex)) {
                    if (MONTHLY_LOAD_PROFILE.equals(correctedLoadProfileObisCode)) {
                        final byte[] dateOctetString = structure.getOctetString(bufferIndex).getArray();
                        final com.energyict.dlms.axrdencoding.OctetString axdrOctetString =
                                new com.energyict.dlms.axrdencoding.OctetString(dateOctetString);
                        final DateTimeOctetString dateTimeOctetString = axdrOctetString.getDateTime(protocol.getTimeZone());
                        if (dateTimeOctetString != null) {
                            IntervalValue value = new IntervalValue(dateTimeOctetString.getValue().getTimeInMillis(), status, getEiServerStatus(status));
                            values.add(value);
                        }
                    }
                }
            }

            int tariffCode = 0;
            if (EI7LoadProfileDataReader.HALF_HOUR_LOAD_PROFILE.equals(correctedLoadProfileObisCode)) {
                tariffCode = structure.getInteger(4);
            }

            intervalData.add(new IntervalData(timeStamp, 0, 0, tariffCode, values));
        }
        return intervalData;
    }

    // skip these temporary
    private boolean isMonthlyExtraStatus(ObisCode loadProfile, int bufferIndex) {
        return MONTHLY_LOAD_PROFILE.equals(loadProfile) && (bufferIndex == 2 || bufferIndex == 3 || bufferIndex == 11 || bufferIndex == 12);
    }

    private Date truncateSeconds(long unixTime) {
        Calendar calendar = Calendar.getInstance(protocol.getTimeZone());
        calendar.setTimeInMillis(unixTime);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    protected ObisCode getCorrectedLoadProfileObisCode(LoadProfileReader loadProfileReader) {
        return protocol.getPhysicalAddressCorrectedObisCode(loadProfileReader.getProfileObisCode(), loadProfileReader.getMeterSerialNumber());
    }

   /*
    !!! USED ALSO BY EI7 PROTOCOL !!!
    bit 0 - Clock Synchronisation Failed
    bit 1 - Metrological Event Log Full
    bit 2 - Metrological Event Log >= 90 %
    bit 3 - Measurement algorithm failure
    bit 4 - Device General Failure
            Severe software error
    bit 5 - Gas Flow Error : overflow detected
            Gas Flow Error : reverse flow detected
    bit 6 - Memory failure
    bit 7 - Less Significant Bit of UNI-TS Status
    bit 8 - Battery Level Below 10%
    bit 9 - Battery Critical Level
    bit 10 - Device Tamper Detection
    bit 11 - DST (Daylight Saving Time) active
    bit 12 - Valve closed Because of Leakage
             Valid Invalid Valve Password
             Valve is Closed Because No Communication For a configurable time
             Valve is closed but leakage is presents
             Valve: cannot open or close
    bit 13 - Reserved
    bit 14 - Reserved
    bit 15 - Reserved
    */
   public static int getEiServerStatus(int intervalStatus) {
       int status = IntervalStateBits.OK;
       BigInteger intervalStatusBig = BigInteger.valueOf(intervalStatus);
       if (intervalStatusBig.testBit(0)) {
           status = status | IntervalStateBits.OTHER;
       }
       if (intervalStatusBig.testBit(1)) {
           status = status | IntervalStateBits.OTHER;
       }
       if (intervalStatusBig.testBit(2)) {
           status = status | IntervalStateBits.OTHER;
       }
       if (intervalStatusBig.testBit(3)) {
           status = status | IntervalStateBits.DEVICE_ERROR;
       }
       if (intervalStatusBig.testBit(4)) {
           status = status | IntervalStateBits.DEVICE_ERROR;
       }
       if (intervalStatusBig.testBit(5)) {
           status = status | IntervalStateBits.OVERFLOW;
       }
       if (intervalStatusBig.testBit(6)) {
           status = status | IntervalStateBits.DEVICE_ERROR;
       }
       if (intervalStatusBig.testBit(7)) {
           status = status | IntervalStateBits.OTHER;
       }
       if (intervalStatusBig.testBit(8)) {
           status = status | IntervalStateBits.BATTERY_LOW;
       }
       if (intervalStatusBig.testBit(9)) {
           status = status | IntervalStateBits.BATTERY_LOW;
       }
       if (intervalStatusBig.testBit(10)) {
           status = status | IntervalStateBits.OTHER;
       }
       if (intervalStatusBig.testBit(11)) {
           status = status | IntervalStateBits.OTHER;
       }
       if (intervalStatusBig.testBit(12)) {
           status = status | IntervalStateBits.OTHER;
       }
       return status;
   }

    private Calendar getFromCalendar(LoadProfileReader loadProfileReader) {
        ProfileLimiter profileLimiter = new ProfileLimiter(loadProfileReader.getStartReadingTime(), loadProfileReader.getEndReadingTime(), (int) limitMaxNrOfDays);
        Calendar fromCal = Calendar.getInstance(protocol.getTimeZone());
        fromCal.setTime(profileLimiter.getFromDate());
        fromCal.set(Calendar.SECOND, 0);// dont bother about the seconds
        return fromCal;
    }

    private Calendar getToCalendar(LoadProfileReader loadProfileReader) {
        ProfileLimiter profileLimiter = new ProfileLimiter(loadProfileReader.getStartReadingTime(), loadProfileReader.getEndReadingTime(), (int) limitMaxNrOfDays);
        Calendar toCal = Calendar.getInstance(protocol.getTimeZone());
        toCal.setTime(profileLimiter.getToDate());
        toCal.set(Calendar.SECOND, 0);
        return toCal;
    }

    public List<ChannelInfo> getChannelInfo(List<CapturedObject> capturedObjects, String serialNumber, ObisCode loadProfileObisCode) throws ProtocolException {
        List<ObisCode> channelObisCodes = new ArrayList<>();
        for (CapturedObject capturedObject : capturedObjects) {
            if (isChannel(capturedObject, loadProfileObisCode)) {
                channelObisCodes.add(capturedObject.getLogicalName().getObisCode());
            }
        }
        Map<ObisCode, Unit> unitMap = readUnits(loadProfileObisCode, channelObisCodes);

        List<ChannelInfo> channelInfos = new ArrayList<>();
        int counter = 0;
        for (ObisCode obisCode : channelObisCodes) {
            Unit unit = unitMap.get(obisCode);
            String newOBIS = obisCode.toString();
            ChannelInfo channelInfo = new ChannelInfo(counter, newOBIS, unit == null ? Unit.get(BaseUnit.UNITLESS) : unit, serialNumber);
            channelInfos.add(channelInfo);
            counter++;
            if (MONTHLY_LOAD_PROFILE.equals(loadProfileObisCode) && MAXIMUM_CONVENTIONAL_CONV_GAS_FLOW.equals(obisCode)) {
                // artificially add the timestamp channel
                final ChannelInfo timestampChannel = new ChannelInfo(
                        counter, MAXIMUM_CONVENTIONAL_CONV_GAS_FLOW_TIME.toString(), Unit.get("ms"), serialNumber
                );
                channelInfos.add(timestampChannel);
                counter++;
            }
        }
        return channelInfos;
    }

    /**
     * @param loadProfileObisCode   the load profile ObisCode. If it is not null, this implementation will additionally read out
     *                              its interval (attribute 4) and cache it in the intervalMap
     * @param channelObisCodes      the ObisCodes of the channels that we should read out the units for
     */
    public Map<ObisCode, Unit> readUnits(ObisCode loadProfileObisCode, List<ObisCode> channelObisCodes) throws ProtocolException {
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
                } else if (uo.getDLMSClassId() == DLMSClassId.DATA) {
                    unitAttribute = new DLMSAttribute(channelObisCode, DataAttributes.VALUE.getAttributeNumber(), uo.getClassID());
                } else if (uo.getDLMSClassId() == DLMSClassId.MANUFACTURER_SPECIFIC_8192) {
                    unitAttribute = new DLMSAttribute(channelObisCode, 3, uo.getClassID());
                } else {
                    throw new ProtocolException("Unexpected captured_object in load profile: " + uo.getDescription());
                }
                attributes.put(channelObisCode, unitAttribute);
            }
        }

        // Also read out the profile interval in this bulk request
        DLMSAttribute profileIntervalAttribute = new DLMSAttribute(loadProfileObisCode, 4, DLMSClassId.PROFILE_GENERIC);
        attributes.put(loadProfileObisCode, profileIntervalAttribute);

        ComposedCosemObject composedCosemObject = new ComposedCosemObject(protocol.getDlmsSession(), protocol.getDlmsSessionProperties().isBulkRequest(), new ArrayList<>(attributes.values()));

        if (loadProfileObisCode != null) {
            try {
                AbstractDataType attribute = composedCosemObject.getAttribute(profileIntervalAttribute);
                getIntervalMap().put(loadProfileObisCode, attribute.intValue());
            } catch (IOException e) {
                throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries() + 1);
            }
        }

        for (ObisCode channelObisCode : channelObisCodes) {
            DLMSAttribute dlmsAttribute = attributes.get(channelObisCode);
            try {
                if(channelObisCode.equals(CURRENT_DIAGNOSTIC_OBISCODE)){
                    result.put(channelObisCode, Unit.getUndefined());
                } else {
                    Structure structure = composedCosemObject.getAttribute(dlmsAttribute).getStructure();
                    result.put(channelObisCode, structure != null ? new ScalerUnit(structure).getEisUnit() : Unit.getUndefined());
                }
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
                    throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries() + 1);
                }
                throw ConnectionCommunicationException.unExpectedProtocolError(e);
            }
        }
        return result;
    }

    /**
     * Check if the captured_object can be considered as an EIServer channel.
     * Registers, extended registers and demand registers are used as channels.
     * Captured_objects with the obiscode of the clock (0.0.1.0.0.255), or the status register (0.x.96.10.x.255) are not considered as channels.
     * <p/>
     * In case of an unknown dlms class, or an unknown obiscode, a proper exception is thrown.
     */
    private boolean isChannel(CapturedObject capturedObject, ObisCode loadProfileObisCode) throws ProtocolException {
        int classId = capturedObject.getClassId();
        ObisCode obisCode = capturedObject.getLogicalName().getObisCode();
        // Firmware bug: check if the channel is a status first, because for some reason
        // the status in EI7 is reported as a Class ID 3 (Register), instead of Class ID 1 (Data)
        if (isProfileStatus(obisCode)) {
            hasStatus.put(loadProfileObisCode, true);
            return false;
        }
        if (!isCaptureTime(capturedObject) && (classId == DLMSClassId.REGISTER.getClassId() || classId == DLMSClassId.EXTENDED_REGISTER.getClassId() || classId == DLMSClassId.DEMAND_REGISTER.getClassId())) {
            return true;
        }
        if (isClock(obisCode) || isCaptureTime(capturedObject) || (isStartOfBillingPeriod(capturedObject) && !isProfileStatus(obisCode))) {
            return false;
        }
        if (DLMSClassId.MANUFACTURER_SPECIFIC_8192.getClassId() == classId) {
            return false;
        }
        throw new ProtocolException("Unexpected captured_object in load profile '" + loadProfileObisCode + "': " + capturedObject.toString());
    }

    protected boolean isProfileStatus(ObisCode obisCode) {
        return (obisCode.getA() == 0 && obisCode.getB() >= 0 && obisCode.getC() == 96 && obisCode.getD() == 10 && obisCode.getE() == 7 && obisCode.getF() == 255);
    }

    private boolean isClock(ObisCode obisCode) {
        return (Clock.getDefaultObisCode().equals(obisCode));
    }

    private boolean isCaptureTime(CapturedObject capturedObject) {
        return (capturedObject.getAttributeIndex() == 5 && capturedObject.getClassId() == DLMSClassId.EXTENDED_REGISTER.getClassId()) || (capturedObject.getAttributeIndex() == 6 && capturedObject.getClassId() == DLMSClassId.DEMAND_REGISTER.getClassId());
    }

    private boolean isStartOfBillingPeriod(CapturedObject capturedObject) {
        return (capturedObject.getAttributeIndex() == 2 && capturedObject.getClassId() == DLMSClassId.DATA.getClassId());
    }

    private boolean isSupported(LoadProfileReader lpr) {
        for (ObisCode supportedLoadProfile : supportedLoadProfiles) {
            if (lpr.getProfileObisCode().equalsIgnoreBChannel(supportedLoadProfile)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasStatusInformation(ObisCode correctedLoadProfileObisCode) {
        if (hasStatus.containsKey(correctedLoadProfileObisCode)) {
            return hasStatus.get(correctedLoadProfileObisCode);
        }

        return false;
    }

    private Map<LoadProfileReader, List<ChannelInfo>> getChannelInfosMap() {
        if (channelInfosMap == null) {
            channelInfosMap = new HashMap<>();
        }
        return channelInfosMap;
    }

    private Map<ObisCode, Integer> getIntervalMap() {
        if (intervalMap == null) {
            intervalMap = new HashMap<>();
        }
        return intervalMap;
    }

}
