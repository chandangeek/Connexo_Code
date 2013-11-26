package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.energyict.cbo.*;
import com.energyict.cpo.PropertySpec;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.Register;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.meterdata.identifiers.DeviceMessageIdentifierById;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdw.core.*;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.energyict.protocolimplv2.messages.enums.DlmsAuthenticationLevelMessageValues;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.abstractnta.NtaProtocol;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.*;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Class that:
 * - Formats the device message attributes from objects to proper string values
 * - Executes a given message
 * - Has a list of all supported device message specs
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/11/13
 * Time: 11:32
 * Author: khe
 */
public class Dsmr23Messaging extends AbstractDsmrMessaging implements DeviceMessageSupport {

    private final static List<DeviceMessageSpec> supportedMessages;
    private static final byte[] defaultMonitoredAttribute = new byte[]{1, 0, 90, 7, 0, (byte) 255};    // Total current, instantaneous value
    public static final String SEPARATOR = ";";

    static {
        supportedMessages = new ArrayList<>();

        // contactor related
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE);
        supportedMessages.add(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE);
        supportedMessages.add(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE);

        // firmware upgrade related
        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE);
        supportedMessages.add(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE);

        // activity calendar related
        supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND);
        supportedMessages.add(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME);
        supportedMessages.add(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND);

        // security related
        supportedMessages.add(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION);
        supportedMessages.add(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL);
        supportedMessages.add(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY);
        supportedMessages.add(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY);
        supportedMessages.add(SecurityMessage.CHANGE_PASSWORD_WITH_NEW_PASSWORD);

        // network and connectivity
        supportedMessages.add(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM);
        supportedMessages.add(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP);
        supportedMessages.add(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS);
        supportedMessages.add(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS);
        supportedMessages.add(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST);

        // display P1
        supportedMessages.add(DisplayDeviceMessage.CONSUMER_MESSAGE_CODE_TO_PORT_P1);
        supportedMessages.add(DisplayDeviceMessage.CONSUMER_MESSAGE_TEXT_TO_PORT_P1);

        // Device Actions
        supportedMessages.add(DeviceActionMessage.GLOBAL_METER_RESET);

        // Load balance
        supportedMessages.add(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS);
        supportedMessages.add(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS);
        supportedMessages.add(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION);

        // Advanced test
        supportedMessages.add(AdvancedTestMessage.XML_CONFIG);

        // LoadProfiles
        supportedMessages.add(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST);
        supportedMessages.add(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST);

        // clock related
        supportedMessages.add(ClockDeviceMessage.SET_TIME);

        // reset
        supportedMessages.add(ConfigurationChangeDeviceMessage.ChangeDefaultResetWindow);
        supportedMessages.add(DeviceActionMessage.ALARM_REGISTER_RESET);
    }

    public Dsmr23Messaging(NtaProtocol protocol) {
        super(protocol);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return supportedMessages;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case UserFileConfigAttributeName:
            case firmwareUpdateUserFileAttributeName:
                return ProtocolTools.getHexStringFromBytes(((UserFile) messageAttribute).loadFileInByteArray(), "");
            case activityCalendarCodeTableAttributeName:
                return convertCodeTableToXML((Code) messageAttribute);
            case authenticationLevelAttributeName:
                return String.valueOf(DlmsAuthenticationLevelMessageValues.getValueFor(messageAttribute.toString()));
            case emergencyProfileGroupIdListAttributeName:
                return convertLookupTable((Lookup) messageAttribute);
            case encryptionLevelAttributeName:
                return String.valueOf(DlmsEncryptionLevelMessageValues.getValueFor(messageAttribute.toString()));
            case overThresholdDurationAttributeName:
                return String.valueOf(((TimeDuration) messageAttribute).getSeconds());
            case newEncryptionKeyAttributeName:
            case newPasswordAttributeName:
            case newAuthenticationKeyAttributeName:
                return ((Password) messageAttribute).getValue();
            case meterTimeAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());
            case specialDaysCodeTableAttributeName:
                return parseSpecialDays(((Code) messageAttribute));
            case loadProfileAttributeName:
                return LoadProfileMessageUtils.formatLoadProfile((LoadProfile) messageAttribute);
            case fromDateAttributeName:
            case toDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());
            case contactorActivationDateAttributeName:
            case activityCalendarActivationDateAttributeName:
            case firmwareUpdateActivationDateAttributeName:
                return String.valueOf(((Date) messageAttribute).getTime());  //Epoch (millis)
            default:
                return messageAttribute.toString();  //Used for String and BigDecimal attributes
        }
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = MdcManager.getCollectedDataFactory().createCollectedMessageList(pendingMessages);
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_OPEN)) {
                    doDisconnect();
                } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE)) {
                    doTimedControlAction(pendingMessage, 1);
                } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_CLOSE)) {
                    doConnect();
                } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE)) {
                    doTimedControlAction(pendingMessage, 2);
                } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE)) {
                    changeControlMode(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE)) {
                    upgradeFirmware(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE)) {
                    upgradeFirmwareWithActivationDate(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND)) {
                    activityCalendar(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME)) {
                    activityCalendarWithActivationDate(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND)) {
                    writeSpecialDays(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION)) {
                    activateDlmsEncryption(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_DLMS_AUTHENTICATION_LEVEL)) {
                    changeAuthLevel(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY)) {
                    changeKey(pendingMessage, 0);
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY)) {
                    changeKey(pendingMessage, 2);
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_PASSWORD_WITH_NEW_PASSWORD)) {
                    changePassword(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM)) {
                    activateWakeUp();
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP)) {
                    deactivateWakeUp();
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS)) {
                    changeGPRSSettings(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS)) {
                    changeGPRSParameters(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST)) {
                    addPhoneNumberToWhiteList(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(DisplayDeviceMessage.CONSUMER_MESSAGE_CODE_TO_PORT_P1)) {
                    codeToP1(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(DisplayDeviceMessage.CONSUMER_MESSAGE_TEXT_TO_PORT_P1)) {
                    textToP1(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.GLOBAL_METER_RESET)) {
                    globalMeterReset();
                } else if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS)) {
                    configureLoadLimitParameters(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS)) {
                    setEmergencyProfileGroupIds(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION)) {
                    clearLoadLimitConfiguration();
                } else if (pendingMessage.getSpecification().equals(AdvancedTestMessage.XML_CONFIG)) {
                    xmlConfiguration(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(LoadProfileMessage.PARTIAL_LOAD_PROFILE_REQUEST)) {
                    collectedMessage = partialLoadProfileRequest(pendingMessage);    //This message returns a result
                } else if (pendingMessage.getSpecification().equals(LoadProfileMessage.LOAD_PROFILE_REGISTER_REQUEST)) {
                    collectedMessage = loadProfileRegisterRequest(pendingMessage);    //This message returns a result
                } else if (pendingMessage.getSpecification().equals(ClockDeviceMessage.SET_TIME)) {
                    setTime(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ChangeDefaultResetWindow)) {
                    changeDefaultResetWindow(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.ALARM_REGISTER_RESET)) {
                    resetAlarmRegister();
                } else {   //Unsupported message
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedIssue(pendingMessage));
                }
            } catch (IOException e) {
                if (IOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSession())) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                }
            }
            result.addCollectedMessages(collectedMessage);
        }
        return result;
    }

    private String convertLookupTable(Lookup messageAttribute) {
        StringBuilder result = new StringBuilder();
        for (LookupEntry entry : messageAttribute.getEntries()) {
            if (result.length() > 0) {
                result.append(SEPARATOR);
            }
            result.append(entry.getKey());
        }
        return result.toString();
    }

    private void changeDefaultResetWindow(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer defaultResetWindow = Integer.valueOf(getAttributeValue(pendingMessage));
        getCosemObjectFactory().getData(ObisCode.fromString("0.0.96.50.5.255")).setValueAttr(new Unsigned32(defaultResetWindow));
    }

    private void resetAlarmRegister() throws IOException {
        getCosemObjectFactory().getData(ObisCode.fromString("0.0.97.98.0.255")).setValueAttr(new Unsigned32(0));
    }

    private void setTime(OfflineDeviceMessage pendingMessage) throws IOException {
        Date time = new Date(Long.valueOf(getAttributeValue(pendingMessage)));
        getProtocol().setTime(time);
    }

    private void xmlConfiguration(OfflineDeviceMessage pendingMessage) throws IOException {
        String xml = getAttributeValue(pendingMessage);
        getCosemObjectFactory().getData(getMeterConfig().getXMLConfig().getObisCode()).setValueAttr(OctetString.fromString(xml));
    }

    /**
     * Substracts 5 seconds from the startReadingTime and adds 5 seconds to the endReadingTime
     *
     * @param loadProfileReader the reader
     * @return the reader with the adjested times
     */
    protected LoadProfileReader constructDateTimeCorrectdLoadProfileReader(final LoadProfileReader loadProfileReader) {
        Date from = new Date(loadProfileReader.getStartReadingTime().getTime() - 5000);
        Date to = new Date(loadProfileReader.getEndReadingTime().getTime() + 5000);
        return new LoadProfileReader(loadProfileReader.getProfileObisCode(), from, to, loadProfileReader.getLoadProfileId(), loadProfileReader.getMeterSerialNumber(), loadProfileReader.getChannelInfos());
    }

    private CollectedMessage loadProfileRegisterRequest(OfflineDeviceMessage pendingMessage) throws IOException {
        String loadProfileContent = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, loadProfileAttributeName).getDeviceMessageAttributeValue();
        String fromDateEpoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, fromDateAttributeName).getDeviceMessageAttributeValue();
        String fullLoadProfileContent = LoadProfileMessageUtils.createLoadProfileRegisterMessage("LoadProfileRegister", "fromDate", loadProfileContent);
        Date fromDate = new Date(Long.valueOf(fromDateEpoch));
        try {
            LegacyLoadProfileRegisterMessageBuilder builder = new LegacyLoadProfileRegisterMessageBuilder();
            builder = (LegacyLoadProfileRegisterMessageBuilder) builder.fromXml(fullLoadProfileContent);

            LoadProfileReader lpr = checkLoadProfileReader(constructDateTimeCorrectdLoadProfileReader(builder.getLoadProfileReader()), builder.getMeterSerialNumber());
            LoadProfileReader fullLpr = new LoadProfileReader(lpr.getProfileObisCode(), fromDate, new Date(), lpr.getLoadProfileId(), lpr.getMeterSerialNumber(), lpr.getChannelInfos());

            List<CollectedLoadProfileConfiguration> collectedLoadProfileConfigurations = getProtocol().fetchLoadProfileConfiguration(Arrays.asList(fullLpr));
            for (CollectedLoadProfileConfiguration config : collectedLoadProfileConfigurations) {
                if (!config.isSupportedByMeter()) {   //LP not supported
                    CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.NotSupported, createMessageFailedIssue(pendingMessage, "Load profile with obiscode " + config.getObisCode() + " is not supported by the device"));
                    return collectedMessage;
                }
            }

            List<CollectedLoadProfile> loadProfileData = getProtocol().getLoadProfileData(Arrays.asList(fullLpr));

            CollectedLoadProfile collectedLoadProfile = loadProfileData.get(0);
            IntervalData intervalDatas = null;
            for (IntervalData intervalData : collectedLoadProfile.getCollectedIntervalData()) {
                if (intervalData.getEndTime().equals(builder.getStartReadingTime())) {
                    intervalDatas = intervalData;
                }
            }

            if (intervalDatas == null) {
                CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(ResultType.DataIncomplete, createMessageFailedIssue(pendingMessage, "Didn't receive data for requested interval (" + builder.getStartReadingTime() + ")"));
                return collectedMessage;
            }

            com.energyict.protocol.Register previousRegister = null;
            List<CollectedRegister> collectedRegisters = new ArrayList<>();
            for (com.energyict.protocol.Register register : builder.getRegisters()) {
                if (register.equals(previousRegister)) {
                    continue;    //Don't add the same intervals twice if there's 2 channels with the same obiscode
                }
                for (int i = 0; i < collectedLoadProfile.getChannelInfo().size(); i++) {
                    final ChannelInfo channel = collectedLoadProfile.getChannelInfo().get(i);
                    if (register.getObisCode().equalsIgnoreBChannel(ObisCode.fromString(channel.getName())) && register.getSerialNumber().equals(channel.getMeterIdentifier())) {
                        final RegisterValue registerValue = new RegisterValue(register, new Quantity(intervalDatas.get(i), channel.getUnit()), intervalDatas.getEndTime(), null, intervalDatas.getEndTime(), new Date(), builder.getRtuRegisterIdForRegister(register));
                        collectedRegisters.add(createCollectedRegister(registerValue, pendingMessage));
                    }
                }
                previousRegister = register;
            }
            CollectedMessage collectedMessage = createCollectedMessageWithRegisterData(pendingMessage, collectedRegisters);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            return collectedMessage;
        } catch (SAXException e) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.Other, createMessageFailedIssue(pendingMessage, e));
            return collectedMessage;
        }
    }

    private CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineDeviceMessage pendingMessage) {
        CollectedRegister deviceRegister = new DefaultDeviceRegister(new RegisterDataIdentifierByObisCodeAndDevice(registerValue.getObisCode(), new DeviceIdentifierById(pendingMessage.getDeviceId())));
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime());
        return deviceRegister;
    }

    private CollectedMessage partialLoadProfileRequest(OfflineDeviceMessage pendingMessage) throws IOException {
        try {
            String loadProfileContent = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, loadProfileAttributeName).getDeviceMessageAttributeValue();
            String fromDateEpoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, fromDateAttributeName).getDeviceMessageAttributeValue();
            String toDateEpoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, toDateAttributeName).getDeviceMessageAttributeValue();
            String fullLoadProfileContent = LoadProfileMessageUtils.createPartialLoadProfileMessage("PartialLoadProfile", "fromDate", "toDate", loadProfileContent);
            Date fromDate = new Date(Long.valueOf(fromDateEpoch));
            Date toDate = new Date(Long.valueOf(toDateEpoch));

            LegacyLoadProfileRegisterMessageBuilder builder = new LegacyLoadProfileRegisterMessageBuilder();
            builder = (LegacyLoadProfileRegisterMessageBuilder) builder.fromXml(fullLoadProfileContent);

            LoadProfileReader lpr = builder.getLoadProfileReader();  //Does not contain the correct from & to date yet, they were stored in separate attributes
            LoadProfileReader fullLpr = new LoadProfileReader(lpr.getProfileObisCode(), fromDate, toDate, lpr.getLoadProfileId(), lpr.getMeterSerialNumber(), lpr.getChannelInfos());

            fullLpr = checkLoadProfileReader(fullLpr, builder.getMeterSerialNumber());
            List<CollectedLoadProfileConfiguration> collectedLoadProfileConfigurations = getProtocol().fetchLoadProfileConfiguration(Arrays.asList(fullLpr));
            for (CollectedLoadProfileConfiguration config : collectedLoadProfileConfigurations) {
                if (!config.isSupportedByMeter()) {   //LP not supported
                    CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.NotSupported, createMessageFailedIssue(pendingMessage, "Load profile with obiscode " + config.getObisCode() + " is not supported by the device"));
                    return collectedMessage;
                }
            }

            List<CollectedLoadProfile> loadProfileData = getProtocol().getLoadProfileData(Arrays.asList(fullLpr));
            CollectedMessage collectedMessage = createCollectedMessageWithLoadProfileData(pendingMessage, loadProfileData.get(0));
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
            return collectedMessage;
        } catch (SAXException e) {              //Failed to parse XML data
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.Other, createMessageFailedIssue(pendingMessage, e));
            return collectedMessage;
        }
    }

    /**
     * The Mbus Hourly gasProfile needs to change the B-field in the ObisCode to readout the correct profile. Herefor we use the serialNumber of the Message.
     *
     * @param lpr the reader to change
     * @return the addapted LoadProfileReader
     */
    private LoadProfileReader checkLoadProfileReader(final LoadProfileReader lpr, String serialNumber) {
        if (lpr.getProfileObisCode().equalsIgnoreBChannel(ObisCode.fromString("0.x.24.3.0.255"))) {
            return new LoadProfileReader(lpr.getProfileObisCode(), lpr.getStartReadingTime(), lpr.getEndReadingTime(), lpr.getLoadProfileId(), serialNumber, lpr.getChannelInfos());
        } else {
            return lpr;
        }
    }

    private void changeKey(OfflineDeviceMessage pendingMessage, int type) throws IOException {
        Array globalKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(type));    // 0 means keyType: global unicast encryption key, 2 means keyType: authenticationKey
        byte[] key = ProtocolTools.getBytesFromHexString(getAttributeValue(pendingMessage), "");
        keyData.addDataType(OctetString.fromByteArray(key));
        globalKeyArray.addDataType(keyData);

        SecuritySetup ss = getCosemObjectFactory().getSecuritySetup();
        ss.transferGlobalKey(globalKeyArray);
    }

    private void globalMeterReset() throws IOException {
        ScriptTable globalResetST = getCosemObjectFactory().getGlobalMeterResetScriptTable();
        globalResetST.invoke(1);    // execute script one
    }

    private void setEmergencyProfileGroupIds(OfflineDeviceMessage pendingMessage) throws IOException {
        String[] groupIds = getAttributeValue(pendingMessage).split(SEPARATOR);
        Array idArray = new Array();
        for (String groupId : groupIds) {
            idArray.addDataType(new Unsigned16(Integer.valueOf(groupId)));

        }
        getCosemObjectFactory().getLimiter().writeEmergencyProfileGroupIdList(idArray);
    }

    // first do it the Iskra way, if it fails do it our way
    private void clearLoadLimitConfiguration() throws IOException {
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

    private void configureLoadLimitParameters(OfflineDeviceMessage pendingMessage) throws IOException {

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
            emergencyProfile.addDataType(new OctetString(convertUnixToGMTDateTime(emergencyProfileActivationDate).getBEREncodedByteArray(), 0, true));
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

    /**
     * Convert a given epoch timestamp in SECONDS to an {@link AXDRDateTime} object
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
        OctetString os = OctetString.fromByteArray(defaultMonitoredAttribute);
        vdt.addDataType(os);
        vdt.addDataType(new Integer8(2));
        loadLimiter.writeMonitoredValue(vdt);
    }

    private void codeToP1(OfflineDeviceMessage pendingMessage) throws IOException {
        Data dataCode = getCosemObjectFactory().getData(getMeterConfig().getConsumerMessageCode().getObisCode());
        dataCode.setValueAttr(OctetString.fromString(getAttributeValue(pendingMessage)));
    }

    private void textToP1(OfflineDeviceMessage pendingMessage) throws IOException {
        Data dataCode = getCosemObjectFactory().getData(getMeterConfig().getConsumerMessageText().getObisCode());
        dataCode.setValueAttr(OctetString.fromString(getAttributeValue(pendingMessage)));

    }

    private void changeGPRSSettings(OfflineDeviceMessage pendingMessage) throws IOException {
        String userName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, usernameAttributeName).getDeviceMessageAttributeValue();
        String password = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, passwordAttributeName).getDeviceMessageAttributeValue();
        writeGprsSettings(userName, password);
    }

    private void changeGPRSParameters(OfflineDeviceMessage pendingMessage) throws IOException {
        String userName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, usernameAttributeName).getDeviceMessageAttributeValue();
        String password = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, passwordAttributeName).getDeviceMessageAttributeValue();
        String apn = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, apnAttributeName).getDeviceMessageAttributeValue();
        writeGprsSettings(userName, password);
        if (apn != null) {
            getCosemObjectFactory().getGPRSModemSetup().writeAPN(apn);
        }
    }

    private void addPhoneNumberToWhiteList(OfflineDeviceMessage pendingMessage) throws IOException {
        //semicolon separated list of phone numbers
        String numbers = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, whiteListPhoneNumbersAttributeName).getDeviceMessageAttributeValue();
        Array list = new Array();
        for (String number : numbers.split(SEPARATOR)) {
            list.addDataType(OctetString.fromString(number));
        }
        getCosemObjectFactory().getAutoConnect().writeDestinationList(list);
    }

    private void activateWakeUp() throws IOException {   //Enable the wake up via SMS
        getCosemObjectFactory().getAutoConnect().writeMode(4);
    }

    private void writeGprsSettings(String userName, String password) throws IOException {
        PPPSetup.PPPAuthenticationType pppat = getCosemObjectFactory().getPPPSetup().new PPPAuthenticationType();
        pppat.setAuthenticationType(PPPSetup.LCPOptionsType.AUTH_PAP);
        if (userName != null) {
            pppat.setUserName(userName);
        }
        if (password != null) {
            pppat.setPassWord(password);
        }
        if ((userName != null) || (password != null)) {
            getCosemObjectFactory().getPPPSetup().writePPPAuthenticationType(pppat);
        }
    }

    private void deactivateWakeUp() throws IOException {   //Disable the wake up via SMS
        getCosemObjectFactory().getAutoConnect().writeMode(1);
    }

    private void changePassword(OfflineDeviceMessage pendingMessage) throws IOException {
        byte[] newPassword = ProtocolTools.getBytesFromHexString(getAttributeValue(pendingMessage), "");
        if (getProtocol().getDlmsSession().getReference() == ProtocolLink.LN_REFERENCE) {
            AssociationLN aln = getCosemObjectFactory().getAssociationLN();

            // We just return the byteArray because it is possible that the berEncoded octetString contains
            // extra check bits ...
            aln.changeHLSSecret(newPassword);
        } else if (getProtocol().getDlmsSession().getReference() == ProtocolLink.SN_REFERENCE) {
            AssociationSN asn = getCosemObjectFactory().getAssociationSN();

            // We just return the byteArray because it is possible that the berEncoded octetString contains
            // extra check bits ...
            asn.changeSecret(newPassword);
        }
    }

    private void upgradeFirmwareWithActivationDate(OfflineDeviceMessage pendingMessage) throws IOException {
        String userFile = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateUserFileAttributeName).getDeviceMessageAttributeValue();
        String activationDate = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateActivationDateAttributeName).getDeviceMessageAttributeValue();

        ImageTransfer it = getCosemObjectFactory().getImageTransfer();
        it.upgrade(ProtocolTools.getBytesFromHexString(userFile, ""));
        SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
        Array dateArray = convertEpochToDateTimeArray(activationDate);
        sas.writeExecutionTime(dateArray);
    }

    private void upgradeFirmware(OfflineDeviceMessage pendingMessage) throws IOException {
        String attributeValue = getAttributeValue(pendingMessage);
        byte[] image = ProtocolTools.getBytesFromHexString(attributeValue, "");

        ImageTransfer it = getCosemObjectFactory().getImageTransfer();
        it.upgrade(image);
        it.imageActivation();
    }

    /**
     * Parse the special days of the given code table into the proper AXDR array.
     */
    private String parseSpecialDays(Code codeTable) {
        List<CodeCalendar> calendars = codeTable.getCalendars();
        Array result = new Array();
        for (CodeCalendar cc : calendars) {
            if (cc.getSeason() == 0) {
                OctetString os = OctetString.fromByteArray(new byte[]{(byte) ((cc.getYear() == -1) ? 0xff : ((cc.getYear() >> 8) & 0xFF)), (byte) ((cc.getYear() == -1) ? 0xff : (cc.getYear()) & 0xFF),
                        (byte) ((cc.getMonth() == -1) ? 0xFF : cc.getMonth()), (byte) ((cc.getDay() == -1) ? 0xFF : cc.getDay()),
                        (byte) ((cc.getDayOfWeek() == -1) ? 0xFF : cc.getDayOfWeek())});
                Unsigned8 dayType = new Unsigned8(cc.getDayType().getId());
                Structure struct = new Structure();
                AXDRDateTime dt = null;
                try {
                    dt = new AXDRDateTime(new byte[]{(byte) 0x09, (byte) 0x0C, (byte) ((cc.getYear() == -1) ? 0x07 : ((cc.getYear() >> 8) & 0xFF)), (byte) ((cc.getYear() == -1) ? 0xB2 : (cc.getYear()) & 0xFF),
                            (byte) ((cc.getMonth() == -1) ? 0xFF : cc.getMonth()), (byte) ((cc.getDay() == -1) ? 0xFF : cc.getDay()),
                            (byte) ((cc.getDayOfWeek() == -1) ? 0xFF : cc.getDayOfWeek()), 0, 0, 0, 0, 0, 0, 0});
                } catch (IOException e) {
                    throw MdcManager.getComServerExceptionFactory().createGeneralParseException(e);
                }
                long days = dt.getValue().getTimeInMillis() / 1000 / 60 / 60 / 24;
                struct.addDataType(new Unsigned16((int) days));
                struct.addDataType(os);
                struct.addDataType(dayType);
                result.addDataType(struct);
            }
        }
        return ProtocolTools.getHexStringFromBytes(result.getBEREncodedByteArray(), "");
    }

    private void activateDlmsEncryption(OfflineDeviceMessage pendingMessage) throws IOException {
        int level = Integer.parseInt(getAttributeValue(pendingMessage));
        getCosemObjectFactory().getSecuritySetup().activateSecurity(new TypeEnum(level));
    }

    private void changeAuthLevel(OfflineDeviceMessage pendingMessage) throws IOException {
        int newAuthLevel = Integer.parseInt(getAttributeValue(pendingMessage));
        AssociationLN aln = getCosemObjectFactory().getAssociationLN();
        AbstractDataType adt = aln.readAuthenticationMechanismName();
        if (adt.isOctetString()) {
            byte[] octets = ((OctetString) adt).getOctetStr();
            if (octets[octets.length - 1] != newAuthLevel) {
                octets[octets.length - 1] = (byte) newAuthLevel;
                aln.writeAuthenticationMechanismName(new OctetString(octets, 0));
            }
        } else if (adt.isStructure()) {
            Structure struct = (Structure) adt;
            Unsigned8 u8 = (Unsigned8) struct.getDataType(struct.nrOfDataTypes() - 1);
            if (u8.intValue() != newAuthLevel) {
                u8 = new Unsigned8(newAuthLevel);
                struct.setDataType(struct.nrOfDataTypes() - 1, u8);
                aln.writeAuthenticationMechanismName(struct);
            }
        } else {
            throw new ProtocolException("Returned AuthenticationMechanismName is not of the type OctetString, nor Structure.");
        }
    }

    private void activityCalendar(OfflineDeviceMessage pendingMessage) throws IOException {
        String calendarName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarNameAttributeName).getDeviceMessageAttributeValue();
        String activityCalendarContents = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarCodeTableAttributeName).getDeviceMessageAttributeValue();
        if (calendarName.length() > 8) {
            calendarName = calendarName.substring(0, 8);
        }

        ActivityCalendarController activityCalendarController = new DLMSActivityCalendarController(getCosemObjectFactory(), getProtocol().getDlmsSession().getTimeZone());
        activityCalendarController.parseContent(activityCalendarContents);
        activityCalendarController.writeCalendarName(calendarName);
        activityCalendarController.writeCalendar(); //Does not activate it yet
        activityCalendarController.writeCalendarActivationTime(null);   //Activate now
    }

    private void writeSpecialDays(OfflineDeviceMessage pendingMessage) throws IOException {
        String specialDayArrayBEREncodedBytes = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarCodeTableAttributeName).getDeviceMessageAttributeValue();
        Array sdArray = AXDRDecoder.decode(ProtocolTools.getBytesFromHexString(specialDayArrayBEREncodedBytes, ""), Array.class);
        SpecialDaysTable sdt = getCosemObjectFactory().getSpecialDaysTable(getMeterConfig().getSpecialDaysTable().getObisCode());

        if (sdArray.nrOfDataTypes() != 0) {
            sdt.writeSpecialDays(sdArray);
        }
    }

    private void activityCalendarWithActivationDate(OfflineDeviceMessage pendingMessage) throws IOException {
        String calendarName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarNameAttributeName).getDeviceMessageAttributeValue();
        String epoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarActivationDateAttributeName).getDeviceMessageAttributeValue();
        String activityCalendarContents = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarCodeTableAttributeName).getDeviceMessageAttributeValue();
        if (calendarName.length() > 8) {
            calendarName = calendarName.substring(0, 8);
        }

        ActivityCalendarController activityCalendarController = new DLMSActivityCalendarController(getCosemObjectFactory(), getProtocol().getDlmsSession().getTimeZone());
        activityCalendarController.parseContent(activityCalendarContents);
        activityCalendarController.writeCalendarName(calendarName);
        activityCalendarController.writeCalendar(); //Does not activate it yet
        Calendar activationCal = Calendar.getInstance(getProtocol().getTimeZone());
        activationCal.setTimeInMillis(Long.parseLong(epoch));
        activityCalendarController.writeCalendarActivationTime(activationCal);   //Activate now
    }

    private void changeControlMode(OfflineDeviceMessage pendingMessage) throws IOException {
        int controlMode = Integer.parseInt(getAttributeValue(pendingMessage));
        Disconnector connectorMode = getCosemObjectFactory().getDisconnector();
        connectorMode.writeControlMode(new TypeEnum(controlMode));
    }

    private void doTimedControlAction(OfflineDeviceMessage pendingMessage, int action) throws IOException {

        Array executionTimeArray = convertEpochToDateTimeArray(getAttributeValue(pendingMessage));
        SingleActionSchedule sasDisconnect = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getDisconnectControlSchedule().getObisCode());

        ScriptTable disconnectorScriptTable = getCosemObjectFactory().getScriptTable(getMeterConfig().getDisconnectorScriptTable().getObisCode());
        byte[] scriptLogicalName = disconnectorScriptTable.getObjectReference().getLn();
        Structure scriptStruct = new Structure();
        scriptStruct.addDataType(OctetString.fromByteArray(scriptLogicalName));
        scriptStruct.addDataType(new Unsigned16(action));    // method '1' is the 'remote_disconnect' method , '2' is the 'remote_connect' method.

        sasDisconnect.writeExecutedScript(scriptStruct);
        sasDisconnect.writeExecutionTime(executionTimeArray);
    }

    /**
     * Get the first attribute value of the given message
     */
    private String getAttributeValue(OfflineDeviceMessage pendingMessage) {
        return pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
    }

    private void doDisconnect() throws IOException {
        Disconnector disconnector = getCosemObjectFactory().getDisconnector();
        disconnector.remoteDisconnect();
    }

    private void doConnect() throws IOException {
        Disconnector disconnector = getCosemObjectFactory().getDisconnector();
        disconnector.remoteReconnect();
    }

    private Issue createUnsupportedIssue(OfflineDeviceMessage pendingMessage) {
        return MdcManager.getIssueCollector().addProblem(pendingMessage, "DeviceMessage.notSupported",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName());
    }

    private Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, Exception e) {
        return createMessageFailedIssue(pendingMessage, e.getMessage());
    }

    private Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, String message) {
        return MdcManager.getIssueCollector().addWarning(pendingMessage, "DeviceMessage.failed",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName(),
                message);
    }

    private CollectedMessage createCollectedMessage(OfflineDeviceMessage message) {
        return MdcManager.getCollectedDataFactory().createCollectedMessage(new DeviceMessageIdentifierById(message.getDeviceMessageId()));
    }

    private CollectedMessage createCollectedMessageWithLoadProfileData(OfflineDeviceMessage message, CollectedLoadProfile collectedLoadProfile) {
        return MdcManager.getCollectedDataFactory().createCollectedMessageWithLoadProfileData(new DeviceMessageIdentifierById(message.getDeviceMessageId()), collectedLoadProfile);
    }

    private CollectedMessage createCollectedMessageWithRegisterData(OfflineDeviceMessage message, List<CollectedRegister> collectedRegisters) {
        return MdcManager.getCollectedDataFactory().createCollectedMessageWithRegisterData(new DeviceIdentifierById(message.getDeviceId()), new DeviceMessageIdentifierById(message.getDeviceMessageId()), collectedRegisters);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return MdcManager.getCollectedDataFactory().createEmptyCollectedMessageList();  //Nothing to do here
    }
}
