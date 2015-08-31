package com.energyict.protocolimplv2.nta.abstractnta.messages;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.DeviceMessageIdentifierById;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Provides functionality to handle the {@link com.energyict.mdc.messages.DeviceMessageSpec}s.
 * <p/>
 *
 * @author sva
 * @since 29/11/13 - 15:20
 */
public abstract class AbstractMessageExecutor {

    private static final byte[] DEFAULT_MONITORED_ATTRIBUTE = new byte[]{1, 0, 90, 7, 0, (byte) 255};    // Total current, instantaneous value

    private AbstractDlmsProtocol protocol;

    public AbstractMessageExecutor(AbstractDlmsProtocol protocol) {
        this.protocol = protocol;
    }

    public abstract CollectedMessageList executePendingMessages(final List<OfflineDeviceMessage> pendingMessages);

    /**
     * Nothing to do here. Sub classes can override.
     */
    public CollectedMessageList updateSentMessages(final List<OfflineDeviceMessage> sentMessages) {
        return MdcManager.getCollectedDataFactory().createEmptyCollectedMessageList();  //Nothing to do here
    }

    protected CosemObjectFactory getCosemObjectFactory() {
        return getProtocol().getDlmsSession().getCosemObjectFactory();
    }

    protected DLMSMeterConfig getMeterConfig() {
        return getProtocol().getDlmsSession().getMeterConfig();
    }

    public AbstractDlmsProtocol getProtocol() {
        return protocol;
    }

    /**
     * Searches for the {@link com.energyict.mdw.offline.OfflineDeviceMessageAttribute}
     * in the given {@link OfflineDeviceMessage} which corresponds
     * with the provided name. If no match is found, then an IOException is thrown
     * attribute is returned
     *
     * @param offlineDeviceMessage the offlineDeviceMessage to search in
     * @param attributeName        the name of the OfflineDeviceMessageAttribute to return
     * @return the requested OfflineDeviceMessageAttribute
     */
    protected String getDeviceMessageAttributeValue(OfflineDeviceMessage offlineDeviceMessage, String attributeName) throws IOException {
        for (OfflineDeviceMessageAttribute offlineDeviceMessageAttribute : offlineDeviceMessage.getDeviceMessageAttributes()) {
            if (offlineDeviceMessageAttribute.getName().equals(attributeName)) {
                return offlineDeviceMessageAttribute.getDeviceMessageAttributeValue();
            }
        }
        throw MdcManager.getComServerExceptionFactory().createProtocolParseException(new ProtocolException("DeviceMessage didn't contain a value found for MessageAttribute " + attributeName));
    }

    protected int getMbusAddress(String serialNumber) {
        return this.protocol.getPhysicalAddressFromSerialNumber(serialNumber) - 1;
    }

    protected MBusClient getMBusClient(String serialNumber) throws IOException {
        return getCosemObjectFactory().getMbusClient(getMbusClientObisCode(serialNumber), MbusClientAttributes.VERSION9);
    }

    private ObisCode getMbusClientObisCode(String serialNumber) throws IOException {
        return getMeterConfig().getMbusClient(getMbusAddress(serialNumber)).getObisCode();
    }

    protected Issue createUnsupportedWarning(OfflineDeviceMessage pendingMessage) {
        return MdcManager.getIssueCollector().addWarning(pendingMessage, "DeviceMessage.notSupported",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName());
    }

    public Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, Exception e) {
        return createMessageFailedIssue(pendingMessage, e.getMessage());
    }

    protected Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, String message) {
        return MdcManager.getIssueCollector().addWarning(pendingMessage, "DeviceMessage.failed",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName(),
                message);
    }

    protected CollectedMessage createCollectedMessage(OfflineDeviceMessage message) {
        return MdcManager.getCollectedDataFactory().createCollectedMessage(new DeviceMessageIdentifierById(message.getDeviceMessageId()));
    }

    protected CollectedMessage createCollectedMessageWithLoadProfileData(OfflineDeviceMessage message, CollectedLoadProfile collectedLoadProfile) {
        return MdcManager.getCollectedDataFactory().createCollectedMessageWithLoadProfileData(new DeviceMessageIdentifierById(message.getDeviceMessageId()), collectedLoadProfile);
    }

    protected CollectedMessage createCollectedMessageWithRegisterData(OfflineDeviceMessage message, List<CollectedRegister> collectedRegisters) {
        return MdcManager.getCollectedDataFactory().createCollectedMessageWithRegisterData(new DeviceIdentifierById(message.getDeviceId()), new DeviceMessageIdentifierById(message.getDeviceMessageId()), collectedRegisters);
    }

    protected CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineDeviceMessage pendingMessage) {
        CollectedRegister deviceRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(new RegisterDataIdentifierByObisCodeAndDevice(registerValue.getObisCode(), new DeviceIdentifierById(pendingMessage.getDeviceId())));
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime());
        return deviceRegister;
    }

    protected Array convertEpochToDateTimeArray(String epoch) {
        Calendar cal = Calendar.getInstance(getProtocol().getTimeZone());
        cal.setTimeInMillis(Long.parseLong(epoch));
        return convertDateToDLMSArray(cal);
    }

    protected Array convertDateToDLMSArray(Calendar cal) {
        byte[] dateBytes = new byte[5];
        dateBytes[0] = (byte) ((cal.get(Calendar.YEAR) >> 8) & 0xFF);
        dateBytes[1] = (byte) (cal.get(Calendar.YEAR) & 0xFF);
        dateBytes[2] = (byte) ((cal.get(Calendar.MONTH) & 0xFF) + 1);
        dateBytes[3] = (byte) (cal.get(Calendar.DAY_OF_MONTH) & 0xFF);
        dateBytes[4] = getDLMSDayOfWeek(cal);
        OctetString date = OctetString.fromByteArray(dateBytes);
        byte[] timeBytes = new byte[4];
        timeBytes[0] = (byte) cal.get(Calendar.HOUR_OF_DAY);
        timeBytes[1] = (byte) cal.get(Calendar.MINUTE);
        timeBytes[2] = (byte) 0x00;
        timeBytes[3] = (byte) 0x00;
        OctetString time = OctetString.fromByteArray(timeBytes);

        Array dateTimeArray = new Array();
        Structure strDateTime = new Structure();
        strDateTime.addDataType(time);
        strDateTime.addDataType(date);
        dateTimeArray.addDataType(strDateTime);
        return dateTimeArray;
    }

    private byte getDLMSDayOfWeek(Calendar cal) {
        int dow = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (dow == 0) {
            dow = 7;
        }
        return (byte) dow;
    }

    protected int getIntegerAttribute(OfflineDeviceMessage pendingMessage) {
        return Integer.parseInt(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
    }

    protected boolean getBooleanAttribute(OfflineDeviceMessage pendingMessage) {
        return Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
    }


    protected void setEmergencyProfileGroupIds(OfflineDeviceMessage pendingMessage) throws IOException {
        String[] groupIds = getDeviceMessageAttributeValue(pendingMessage, emergencyProfileGroupIdListAttributeName).split(";");
        Array idArray = new Array();
        for (String groupId : groupIds) {
            idArray.addDataType(new Unsigned16(Integer.valueOf(groupId)));

        }
        getCosemObjectFactory().getLimiter().writeEmergencyProfileGroupIdList(idArray);
    }

    // first do it the Iskra way, if it fails do it our way
    protected void clearLoadLimitConfiguration() throws IOException {
        Limiter clearLLimiter = getCosemObjectFactory().getLimiter();
        Structure emptyStruct = new Structure();
        emptyStruct.addDataType(new Unsigned16(0));
        emptyStruct.addDataType(OctetString.fromByteArray(new byte[14]));
        emptyStruct.addDataType(new Unsigned32(0));
        try {
            clearLLimiter.writeEmergencyProfile(clearLLimiter.new EmergencyProfile(emptyStruct.getBEREncodedByteArray(), 0, 0));
        } catch (DataAccessResultException e) {
            if (e.getDataAccessResult() == DataAccessResultCode.TYPE_UNMATCHED.getResultCode()) {
                emptyStruct = new Structure();
                emptyStruct.addDataType(new NullData());
                emptyStruct.addDataType(new NullData());
                emptyStruct.addDataType(new NullData());
                clearLLimiter.writeEmergencyProfile(clearLLimiter.new EmergencyProfile(emptyStruct.getBEREncodedByteArray(), 0, 0));
            } else {
                throw e;
            }
        }
    }

    protected void configureLoadLimitParameters(OfflineDeviceMessage pendingMessage) throws IOException {
        String normalThreshold = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, normalThresholdAttributeName).getDeviceMessageAttributeValue();
        String emergencyThreshold = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, emergencyThresholdAttributeName).getDeviceMessageAttributeValue();
        String overThresholdDuration = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, overThresholdDurationAttributeName).getDeviceMessageAttributeValue();
        String emergencyProfileId = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, emergencyProfileIdAttributeName).getDeviceMessageAttributeValue();
        String emergencyProfileActivationDate = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, emergencyProfileActivationDateAttributeName).getDeviceMessageAttributeValue();
        String emergencyProfileDuration = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, emergencyProfileDurationAttributeName).getDeviceMessageAttributeValue();

        byte theMonitoredAttributeType = -1;
        Limiter loadLimiter = getCosemObjectFactory().getLimiter();

        if (theMonitoredAttributeType == -1) {    // check for the type of the monitored value
            Limiter.ValueDefinitionType valueDefinitionType = loadLimiter.getMonitoredValue();
            if (valueDefinitionType.getClassId().getValue() == 0) {
                setMonitoredValue(loadLimiter);
                valueDefinitionType = loadLimiter.readMonitoredValue();
            }
            theMonitoredAttributeType = getMonitoredAttributeType(valueDefinitionType);
        }

        // Write the normalThreshold
        if (normalThreshold != null) {
            loadLimiter.writeThresholdNormal(convertToMonitoredType(theMonitoredAttributeType, normalThreshold));
        }

        // Write the emergencyThreshold
        if (emergencyThreshold != null) {
            loadLimiter.writeThresholdEmergency(convertToMonitoredType(theMonitoredAttributeType, emergencyThreshold));
        }

        // Write the minimumOverThresholdDuration
        if (overThresholdDuration != null) {
            loadLimiter.writeMinOverThresholdDuration(new Unsigned32(Integer.parseInt(overThresholdDuration)));
        }

        // Construct the emergencyProfile
        Structure emergencyProfile = new Structure();
        if (emergencyProfileId != null) {    // The EmergencyProfileID
            emergencyProfile.addDataType(new Unsigned16(Integer.parseInt(emergencyProfileId)));
        }
        if (emergencyProfileActivationDate != null) {    // The EmergencyProfileActivationTime
            emergencyProfile.addDataType(new OctetString(getEmergencyProfileActivationAXDRDateTime(emergencyProfileActivationDate).getBEREncodedByteArray(), 0, true));
        }
        if (emergencyProfileDuration != null) {        // The EmergencyProfileDuration
            emergencyProfile.addDataType(new Unsigned32(Integer.parseInt(emergencyProfileDuration)));
        }
        if ((emergencyProfile.nrOfDataTypes() > 0) && (emergencyProfile.nrOfDataTypes() != 3)) {    // If all three elements are correct, then send it, otherwise throw error
            throw new ProtocolException("The complete emergecy profile must be filled in before sending it to the meter.");
        } else {
            if (emergencyProfile.nrOfDataTypes() > 0) {
                loadLimiter.writeEmergencyProfile(emergencyProfile.getBEREncodedByteArray());
            }
        }
    }

    protected AXDRDateTime getEmergencyProfileActivationAXDRDateTime(String emergencyProfileActivationDate) {
        return convertUnixToGMTDateTime(emergencyProfileActivationDate);
    }

    /**
     * Convert a given epoch timestamp in SECONDS to an {@link com.energyict.dlms.axrdencoding.util.AXDRDateTime} object
     *
     * @param time - the time in seconds sinds 1th jan 1970 00:00:00
     * @return the AXDRDateTime of the given time
     */
    public AXDRDateTime convertUnixToGMTDateTime(String time) {
        return convertUnixToDateTime(time, TimeZone.getTimeZone("GMT"));
    }

    public AXDRDateTime convertUnixToDateTime(String time, TimeZone timeZone) {
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTimeInMillis(Long.parseLong(time) * 1000);
        return new AXDRDateTime(cal);
    }

    /**
     * Convert the value to write to the Limiter object to the correct monitored value type ...
     */
    protected AbstractDataType convertToMonitoredType(byte theMonitoredAttributeType, String value) throws ProtocolException {

        final AxdrType axdrType = AxdrType.fromTag(theMonitoredAttributeType);
        switch (axdrType) {
            case NULL: {
                return new NullData();
            }
            case BOOLEAN: {
                return new BooleanObject(value.equalsIgnoreCase("1"));
            }
            case BIT_STRING: {
                return new BitString(Integer.parseInt(value));
            }
            case DOUBLE_LONG: {
                return new Integer32(Integer.parseInt(value));
            }
            case DOUBLE_LONG_UNSIGNED: {
                return new Unsigned32(Integer.parseInt(value));
            }
            case OCTET_STRING: {
                return OctetString.fromString(value);
            }
            case VISIBLE_STRING: {
                return new VisibleString(value);
            }
            case INTEGER: {
                return new Integer8(Integer.parseInt(value));
            }
            case LONG: {
                return new Integer16(Integer.parseInt(value));
            }
            case UNSIGNED: {
                return new Unsigned8(Integer.parseInt(value));
            }
            case LONG_UNSIGNED: {
                return new Unsigned16(Integer.parseInt(value));
            }
            case LONG64: {
                return new Integer64(Integer.parseInt(value));
            }
            case ENUM: {
                return new TypeEnum(Integer.parseInt(value));
            }
            default:
                throw new ProtocolException("convertToMonitoredtype error, unknown type.");
        }
    }

    private byte getMonitoredAttributeType(Limiter.ValueDefinitionType vdt) throws IOException {

        if (getMeterConfig().getClassId(vdt.getObisCode()) == Register.CLASSID) {
            return getCosemObjectFactory().getRegister(vdt.getObisCode()).getAttrbAbstractDataType(vdt.getAttributeIndex().getValue()).getBEREncodedByteArray()[0];
        } else if (getMeterConfig().getClassId(vdt.getObisCode()) == ExtendedRegister.CLASSID) {
            return getCosemObjectFactory().getExtendedRegister(vdt.getObisCode()).getAttrbAbstractDataType(vdt.getAttributeIndex().getValue()).getBEREncodedByteArray()[0];
        } else if (getMeterConfig().getClassId(vdt.getObisCode()) == DLMSClassId.DEMAND_REGISTER.getClassId()) {
            return getCosemObjectFactory().getDemandRegister(vdt.getObisCode()).getAttrbAbstractDataType(vdt.getAttributeIndex().getValue()).getBEREncodedByteArray()[0];
        } else if (getMeterConfig().getClassId(vdt.getObisCode()) == Data.CLASSID) {
            return getCosemObjectFactory().getData(vdt.getObisCode()).getAttrbAbstractDataType(vdt.getAttributeIndex().getValue()).getBEREncodedByteArray()[0];
        } else {
            throw new ProtocolException("WebRtuKP, getMonitoredAttributeType, invalid classID " + getMeterConfig().getClassId(vdt.getObisCode()) + " for obisCode " + vdt.getObisCode().toString());
        }
    }

    private void setMonitoredValue(Limiter loadLimiter) throws IOException {
        Limiter.ValueDefinitionType vdt = loadLimiter.new ValueDefinitionType();
        vdt.addDataType(new Unsigned16(3));
        OctetString os = OctetString.fromByteArray(DEFAULT_MONITORED_ATTRIBUTE);
        vdt.addDataType(os);
        vdt.addDataType(new Integer8(2));
        loadLimiter.writeMonitoredValue(vdt);
    }

}
