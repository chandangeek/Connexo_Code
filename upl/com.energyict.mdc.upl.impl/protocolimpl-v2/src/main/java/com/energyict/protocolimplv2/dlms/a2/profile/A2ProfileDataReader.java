package com.energyict.protocolimplv2.dlms.a2.profile;

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
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.dlms.as220.ProfileLimiter;
import com.energyict.protocolimplv2.dlms.a2.A2;
import com.energyict.protocolimplv2.dlms.a2.registers.FirmwareVersion;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Level;

public class A2ProfileDataReader {
    private static final ObisCode HOURLY_LOAD_PROFILE_OBISCODE = ObisCode.fromString("7.0.99.99.2.255");
    private static final ObisCode DAILY_LOAD_PROFILE_OBISCODE = ObisCode.fromString("7.0.99.99.3.255");

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
        supportedLoadProfiles.add(HOURLY_LOAD_PROFILE_OBISCODE);
        supportedLoadProfiles.add(DAILY_LOAD_PROFILE_OBISCODE);
    }

    // use this constructor to create a Reader which will read only one loadProfile
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
                    getChannelInfosMap().put(lpr, channelInfos);    //Remember these, they are re-used in method #getLoadProfileData();
                } catch (IOException e) {   //Object not found in IOL, should never happen
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

                    //     1: determine type lp
                    // 2: split to 10 record lenght intervals
                    // 3: itterate
                    // 4: conatenate
                    FirmwareVersion firmwareVersion = new FirmwareVersion(protocol.getDlmsSession().getCosemObjectFactory().getData(ObisCode.fromString("7.1.0.2.1.255")).getValueAttr().getOctetString());

                    Calendar fromCalendar = getFromCalendar(loadProfileReader);
                    Calendar toCalendar = getToCalendar(loadProfileReader);
                    List<IntervalData> intervalData = new ArrayList<>();
                    ProfileGeneric profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(correctedLoadProfileObisCode);
                    profileGeneric.setDsmr4SelectiveAccessFormat(protocol.useDsmr4SelectiveAccessFormat());
                    DataContainer buffer;
                    if (HOURLY_LOAD_PROFILE_OBISCODE.equals(loadProfileReader.getProfileObisCode()) && firmwareVersion.getMajor()==1 && firmwareVersion.getMinor()==4) {
                        Calendar actualCalendar = Calendar.getInstance(protocol.getTimeZone());
                        SelectiveEntryFilter filter = new SelectiveEntryFilter(fromCalendar, toCalendar, actualCalendar);
                        protocol.getLogger().log(Level.INFO, "From Calendar " + fromCalendar.getTime());
                        protocol.getLogger().log(Level.INFO, "To Calendar " + toCalendar.getTime());
                        protocol.getLogger().log(Level.INFO, "Actual Calendar " + actualCalendar.getTime());
                        protocol.getLogger().log(Level.INFO, "From Index : " + filter.getFromIndex() + ", To Index " + filter.getToIndex());
                        buffer = profileGeneric.getBuffer(filter.getFromIndex(), filter.getToIndex(), 1, 0);
                    }
                    else {
                        buffer = profileGeneric.getBuffer(fromCalendar, toCalendar);
                    }

                    intervalData.addAll(readIntervalDataFromBuffer(correctedLoadProfileObisCode, buffer));
                    collectedLoadProfile.setCollectedIntervalData(intervalData, channelInfos);
                    collectedLoadProfile.setDoStoreOlderValues(true);
                }catch (DataAccessResultException e){
                    // this can happen when the load profile is read twice in the same time window (day for daily lp), than the data block is not accessible. It could also happen when the load profile is not configured properly.
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
                        String message = String.join(" ","Load profile was probably already read today, try modifying the 'last reading' date in the load profile properties.", e.getMessage());
                        Issue problem = issueFactory.createWarning(loadProfileReader, "loadProfileXBlockingIssue", correctedLoadProfileObisCode, message);
                        collectedLoadProfile.setFailureInformation(ResultType.DataIncomplete, problem);
                    }
                }
                catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
                        Issue problem = issueFactory.createProblem(loadProfileReader, "loadProfileXBlockingIssue", correctedLoadProfileObisCode, e.getMessage());
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

        IntervalValue value;

        for (int index = 0; index < loadProfileEntries.length; index++) {
            int status = 0;
            int offset = 0;
            DataStructure structure = buffer.getRoot().getStructure(index);
            int bufferSize = structure.getNrOfElements();
            Date timeStamp = null;
            //Timestamp should be at index 0
            if (structure.isInteger(offset)) {
                int unixTime = structure.getInteger(offset);
                timeStamp = new Date(((long) unixTime) * 1000L);
                offset++;
            }
            if (hasStatusInformation(correctedLoadProfileObisCode)) {
                status = structure.getInteger(offset);
                offset++;
            }

            final List<IntervalValue> values = new ArrayList<>();

            for (int bufferIndex = offset; bufferIndex < bufferSize; bufferIndex++) {
                if (structure.isBigDecimal(bufferIndex)) {
                    value = new IntervalValue(structure.getBigDecimalValue(bufferIndex), status, getEiServerStatus(status));
                    values.add(value);
                }
            }

            intervalData.add(new IntervalData(timeStamp, 0, 0, 0, values));
        }
        return intervalData;
    }

    protected ObisCode getCorrectedLoadProfileObisCode(LoadProfileReader loadProfileReader) {
        return protocol.getPhysicalAddressCorrectedObisCode(loadProfileReader.getProfileObisCode(), loadProfileReader.getMeterSerialNumber());
    }

    protected int getEiServerStatus(int protocolStatus) {
        int status = IntervalStateBits.OK;
        BigInteger protocolStatusBig = BigInteger.valueOf(protocolStatus);
        if (protocolStatusBig.testBit(7)) {
            status = status | IntervalStateBits.CORRUPTED;
        }
        if (protocolStatusBig.testBit(6)) {
            status = status | IntervalStateBits.SHORTLONG;
        }
        if (protocolStatusBig.testBit(5)) {
            status = status | IntervalStateBits.OVERFLOW;
        }
        if (protocolStatusBig.testBit(4)) {
            status = status | IntervalStateBits.REVISED;
        }
        if (protocolStatusBig.testBit(3)) {
            status = status | IntervalStateBits.CONFIGURATIONCHANGE;
        }
        if (protocolStatusBig.testBit(2)) {
            status = status | IntervalStateBits.CURRENTFAILVALIDATION;
        }
        if (protocolStatusBig.testBit(1)) {
            status = status | IntervalStateBits.POWERDOWN;
        }
        if (protocolStatusBig.testBit(0)) {
            status = status | IntervalStateBits.PHASEFAILURE;
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
        }
        return channelInfos;
    }

    /**
     * @param obisCode         the load profile obiscode. If it is not null, this implementation will additionally read out
     *                         its interval (attribute 4) and cache it in the intervalMap
     * @param channelObisCodes the obiscodes of the channels that we should read out the units for
     */
    private Map<ObisCode, Unit> readUnits(ObisCode obisCode, List<ObisCode> channelObisCodes) throws ProtocolException {
        Map<ObisCode, Unit> result = new HashMap<>();

        Map<ObisCode, DLMSAttribute> attributes = new HashMap<>();
        for (ObisCode channelObisCode : channelObisCodes) {
            UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.protocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), channelObisCode);
            if (uo != null) {
                DLMSAttribute unitAttribute = null;
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
        if (obisCode != null) {
            profileIntervalAttribute = new DLMSAttribute(obisCode, 4, DLMSClassId.PROFILE_GENERIC);
            attributes.put(obisCode, profileIntervalAttribute);
        }

        ComposedCosemObject composedCosemObject = new ComposedCosemObject(protocol.getDlmsSession(), protocol.getDlmsSessionProperties().isBulkRequest(), new ArrayList<>(attributes.values()));

        if (obisCode != null) {
            try {
                AbstractDataType attribute = composedCosemObject.getAttribute(profileIntervalAttribute);
                getIntervalMap().put(obisCode, attribute.intValue());
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
                    }
                    throw ConnectionCommunicationException.unExpectedProtocolError(e);
                }
            } else {
                final String message = "The OBIS code " + channelObisCode + " found in the meter load profile capture objects list, is NOT supported by the meter itself." +
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
        if (!isCaptureTime(capturedObject) && (classId == DLMSClassId.REGISTER.getClassId() || classId == DLMSClassId.EXTENDED_REGISTER.getClassId() || classId == DLMSClassId.DEMAND_REGISTER.getClassId())) {
            return true;
        }
        if (isClock(obisCode) || isCaptureTime(capturedObject) || (isStartOfBillingPeriod(capturedObject) && !isProfileStatus(obisCode))) {
            return false;
        }
        if (isProfileStatus(obisCode)) {
            hasStatus.put(loadProfileObisCode, true);
            return false;
        }
        throw new ProtocolException("Unexpected captured_object in load profile '" + loadProfileObisCode + "': " + capturedObject.toString());
    }

    private boolean isProfileStatus(ObisCode obisCode) {
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
