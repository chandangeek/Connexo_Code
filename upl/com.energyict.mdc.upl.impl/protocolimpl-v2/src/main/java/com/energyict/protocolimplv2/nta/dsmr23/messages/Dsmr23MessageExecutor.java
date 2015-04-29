package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.User;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * @author sva
 * @since 29/11/13 - 16:04
 */
public class Dsmr23MessageExecutor extends AbstractMessageExecutor {

    public static final String SEPARATOR = ";";
    private static final ObisCode MBUS_CLIENT_OBISCODE = ObisCode.fromString("0.1.24.1.0.255");
    private Dsmr23MbusMessageExecutor mbusMessageExecutor;

    public Dsmr23MessageExecutor(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = MdcManager.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        List<OfflineDeviceMessage> masterMessages = getMessagesOfMaster(pendingMessages);
        List<OfflineDeviceMessage> mbusMessages = getMbusMessages(pendingMessages);
        if (!mbusMessages.isEmpty()) {
            // Execute messages for MBus devices
            result.addCollectedMessages(getMbusMessageExecutor().executePendingMessages(mbusMessages));
        }

        for (OfflineDeviceMessage pendingMessage : masterMessages) {
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
                    changeEncryptionKey(pendingMessage, 0);
                } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY)) {
                    changeAuthenticationKey(pendingMessage, 2);
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
                } else if (pendingMessage.getSpecification().equals(MBusSetupDeviceMessage.Commission_With_Channel)) {
                    mbusCommission(pendingMessage);
                } else {   //Unsupported message
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
                    collectedMessage.setDeviceProtocolInformation("Message is currently not supported by the protocol");
                }
            } catch (IOException e) {
                if (IOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSession())) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                }
            }
            result.addCollectedMessage(collectedMessage);
        }
        return result;
    }

    private void changeDefaultResetWindow(OfflineDeviceMessage pendingMessage) throws IOException {
        Integer defaultResetWindow = Integer.valueOf(getDeviceMessageAttributeValue(pendingMessage, DefaultResetWindowAttributeName));
        getCosemObjectFactory().getData(ObisCode.fromString("0.0.96.50.5.255")).setValueAttr(new Unsigned32(defaultResetWindow));
    }

    private void resetAlarmRegister() throws IOException {
        getCosemObjectFactory().getData(ObisCode.fromString("0.0.97.98.0.255")).setValueAttr(new Unsigned32(0));
    }

    private void setTime(OfflineDeviceMessage pendingMessage) throws IOException {
        Date time = new Date(Long.valueOf(getDeviceMessageAttributeValue(pendingMessage, meterTimeAttributeName)));
        getProtocol().setTime(time);
    }

    private void xmlConfiguration(OfflineDeviceMessage pendingMessage) throws IOException {
        String xml = getDeviceMessageAttributeValue(pendingMessage, xmlConfigAttributeName);
        getCosemObjectFactory().getData(getMeterConfig().getXMLConfig().getObisCode()).setValueAttr(OctetString.fromString(xml));
    }

    private void mbusCommission(OfflineDeviceMessage pendingMessage) throws IOException {
        ObisCode mbusClientObisCode = ProtocolTools.setObisCodeField(MBUS_CLIENT_OBISCODE, 1, (byte) getIntegerAttribute(pendingMessage));
        MBusClient mbusClient = getCosemObjectFactory().getMbusClient(mbusClientObisCode, MbusClientAttributes.VERSION9);
        mbusClient.installSlave(0);     //Means: pick the next primary address that is available
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

    protected CollectedMessage loadProfileRegisterRequest(OfflineDeviceMessage pendingMessage) throws IOException {
        String loadProfileContent = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, loadProfileAttributeName).getDeviceMessageAttributeValue();
        String fromDateEpoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, fromDateAttributeName).getDeviceMessageAttributeValue();

        String fullLoadProfileContent = LoadProfileMessageUtils.createLoadProfileRegisterMessage(
                "LoadProfileRegister",
                getDefaultDateFormatter().format(new Date(Long.parseLong(fromDateEpoch))),
                loadProfileContent
        );
        Date fromDate = new Date(Long.valueOf(fromDateEpoch));
        try {
            LegacyLoadProfileRegisterMessageBuilder builder = new LegacyLoadProfileRegisterMessageBuilder();
            builder = (LegacyLoadProfileRegisterMessageBuilder) builder.fromXml(fullLoadProfileContent);
            if (builder.getRegisters() == null || builder.getRegisters().isEmpty()) {
                CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(ResultType.ConfigurationMisMatch, createMessageFailedIssue(pendingMessage, "Unable to execute the message, there are no channels attached under LoadProfile " + builder.getProfileObisCode() + "!"));
            }

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

    private DateFormat getDefaultDateFormatter() {
        User user = MeteringWarehouse.getCurrentUser();
        DateFormat formatter = new SimpleDateFormat(user.getDateFormat() + " " + user.getLongTimeFormat());
        formatter.setTimeZone(MeteringWarehouse.getCurrent().getSystemTimeZone());
        return formatter;
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

    private void changeEncryptionKey(OfflineDeviceMessage pendingMessage, int type) throws IOException {
        Array globalKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(type));    // 0 means keyType: global unicast encryption key, 2 means keyType: authenticationKey
        byte[] key = ProtocolTools.getBytesFromHexString(getDeviceMessageAttributeValue(pendingMessage, newEncryptionKeyAttributeName), "");
        keyData.addDataType(OctetString.fromByteArray(key));
        globalKeyArray.addDataType(keyData);

        SecuritySetup ss = getCosemObjectFactory().getSecuritySetup();
        ss.transferGlobalKey(globalKeyArray);
    }

    private void changeAuthenticationKey(OfflineDeviceMessage pendingMessage, int type) throws IOException {
        Array globalKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(type));    // 0 means keyType: global unicast encryption key, 2 means keyType: authenticationKey
        byte[] key = ProtocolTools.getBytesFromHexString(getDeviceMessageAttributeValue(pendingMessage, newAuthenticationKeyAttributeName), "");
        keyData.addDataType(OctetString.fromByteArray(key));
        globalKeyArray.addDataType(keyData);

        SecuritySetup ss = getCosemObjectFactory().getSecuritySetup();
        ss.transferGlobalKey(globalKeyArray);
    }

    private void globalMeterReset() throws IOException {
        ScriptTable globalResetST = getCosemObjectFactory().getGlobalMeterResetScriptTable();
        globalResetST.invoke(1);    // execute script one
    }

    private void codeToP1(OfflineDeviceMessage pendingMessage) throws IOException {
        Data dataCode = getCosemObjectFactory().getData(getMeterConfig().getConsumerMessageCode().getObisCode());
        dataCode.setValueAttr(OctetString.fromString(getDeviceMessageAttributeValue(pendingMessage, p1InformationAttributeName)));
    }

    private void textToP1(OfflineDeviceMessage pendingMessage) throws IOException {
        Data dataCode = getCosemObjectFactory().getData(getMeterConfig().getConsumerMessageText().getObisCode());
        dataCode.setValueAttr(OctetString.fromString(getDeviceMessageAttributeValue(pendingMessage, p1InformationAttributeName)));

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

    protected void addPhoneNumberToWhiteList(OfflineDeviceMessage pendingMessage) throws IOException {
        //semicolon separated list of phone numbers
        String numbers = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, whiteListPhoneNumbersAttributeName).getDeviceMessageAttributeValue();
        Array list = new Array();
        for (String number : numbers.split(SEPARATOR)) {
            list.addDataType(OctetString.fromString(number));
        }
        getCosemObjectFactory().getAutoConnect().writeDestinationList(list);
    }

    protected void activateWakeUp() throws IOException {   //Enable the wake up via SMS
        getCosemObjectFactory().getAutoConnect().writeMode(4);
    }

    protected void deactivateWakeUp() throws IOException {   //Disable the wake up via SMS
        getCosemObjectFactory().getAutoConnect().writeMode(1);
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

    private void changePassword(OfflineDeviceMessage pendingMessage) throws IOException {
        byte[] newPassword = ProtocolTools.getBytesFromHexString(getDeviceMessageAttributeValue(pendingMessage, newPasswordAttributeName), "");
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

    protected void upgradeFirmwareWithActivationDate(OfflineDeviceMessage pendingMessage) throws IOException {
        String userFile = getDeviceMessageAttributeValue(pendingMessage, firmwareUpdateUserFileAttributeName);
        String activationDate = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateActivationDateAttributeName).getDeviceMessageAttributeValue();
        byte[] image = ProtocolTools.getBytesFromHexString(userFile, "");

        ImageTransfer it = getCosemObjectFactory().getImageTransfer();
        it.upgrade(image);
        SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
        Array dateArray = convertEpochToDateTimeArray(activationDate);
        sas.writeExecutionTime(dateArray);
    }

    protected void upgradeFirmware(OfflineDeviceMessage pendingMessage) throws IOException {
        String attributeValue = getDeviceMessageAttributeValue(pendingMessage, firmwareUpdateUserFileAttributeName);
        byte[] image = ProtocolTools.getBytesFromHexString(attributeValue, "");

        ImageTransfer it = getCosemObjectFactory().getImageTransfer();
        it.upgrade(image);
        it.imageActivation();
    }


    private void activateDlmsEncryption(OfflineDeviceMessage pendingMessage) throws IOException {
        int level = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, encryptionLevelAttributeName));
        getCosemObjectFactory().getSecuritySetup().activateSecurity(new TypeEnum(level));
    }

    private void changeAuthLevel(OfflineDeviceMessage pendingMessage) throws IOException {
        int newAuthLevel = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, authenticationLevelAttributeName));
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

    protected void activityCalendar(OfflineDeviceMessage pendingMessage) throws IOException {
        String calendarName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarNameAttributeName).getDeviceMessageAttributeValue();
        String activityCalendarContents = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarCodeTableAttributeName).getDeviceMessageAttributeValue();
        if (calendarName.length() > 8) {
            calendarName = calendarName.substring(0, 8);
        }

        ActivityCalendarController activityCalendarController = new DLMSActivityCalendarController(getCosemObjectFactory(), getProtocol().getDlmsSession().getTimeZone(), false);
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

    protected void activityCalendarWithActivationDate(OfflineDeviceMessage pendingMessage) throws IOException {
        String calendarName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarNameAttributeName).getDeviceMessageAttributeValue();
        String epoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarActivationDateAttributeName).getDeviceMessageAttributeValue();
        String activityCalendarContents = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarCodeTableAttributeName).getDeviceMessageAttributeValue();
        if (calendarName.length() > 8) {
            calendarName = calendarName.substring(0, 8);
        }

        ActivityCalendarController activityCalendarController = new DLMSActivityCalendarController(getCosemObjectFactory(), getProtocol().getDlmsSession().getTimeZone(), false);
        activityCalendarController.parseContent(activityCalendarContents);
        activityCalendarController.writeCalendarName(calendarName);
        activityCalendarController.writeCalendar(); //Does not activate it yet
        Calendar activationCal = Calendar.getInstance(getProtocol().getTimeZone());
        activationCal.setTimeInMillis(Long.parseLong(epoch));
        activityCalendarController.writeCalendarActivationTime(activationCal);   //Activate now
    }

    private void changeControlMode(OfflineDeviceMessage pendingMessage) throws IOException {
        int controlMode = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, contactorModeAttributeName));
        Disconnector connectorMode = getCosemObjectFactory().getDisconnector();
        connectorMode.writeControlMode(new TypeEnum(controlMode));
    }

    private void doTimedControlAction(OfflineDeviceMessage pendingMessage, int action) throws IOException {
        Array executionTimeArray = convertEpochToDateTimeArray(getDeviceMessageAttributeValue(pendingMessage, contactorActivationDateAttributeName));
        SingleActionSchedule sasDisconnect = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getDisconnectControlSchedule().getObisCode());

        ScriptTable disconnectorScriptTable = getCosemObjectFactory().getScriptTable(getMeterConfig().getDisconnectorScriptTable().getObisCode());
        byte[] scriptLogicalName = disconnectorScriptTable.getObjectReference().getLn();
        Structure scriptStruct = new Structure();
        scriptStruct.addDataType(OctetString.fromByteArray(scriptLogicalName));
        scriptStruct.addDataType(new Unsigned16(action));    // method '1' is the 'remote_disconnect' method , '2' is the 'remote_connect' method.

        sasDisconnect.writeExecutedScript(scriptStruct);
        sasDisconnect.writeExecutionTime(executionTimeArray);
    }

    private void doDisconnect() throws IOException {
        Disconnector disconnector = getCosemObjectFactory().getDisconnector();
        disconnector.remoteDisconnect();
    }

    private void doConnect() throws IOException {
        Disconnector disconnector = getCosemObjectFactory().getDisconnector();
        disconnector.remoteReconnect();
    }

    /**
     * Extract all OfflineDeviceMessages who belong to the master device
     *
     * @param deviceMessages a List of all OfflineDeviceMessages
     * @return the list of OfflineDeviceMessages who belong to the master device
     */
    protected List<OfflineDeviceMessage> getMessagesOfMaster(List<OfflineDeviceMessage> deviceMessages) {
        List<OfflineDeviceMessage> messages = new ArrayList<>();

        for (OfflineDeviceMessage pendingMessage : deviceMessages) {
            if (getProtocol().getSerialNumber().equals(pendingMessage.getDeviceSerialNumber())) {
                messages.add(pendingMessage);
            }
        }
        return messages;
    }

    /**
     * Extract all OfflineDeviceMessages who belong to a MBus slave device
     *
     * @param deviceMessages a List of all OfflineDeviceMessages
     * @return the list of OfflineDeviceMessages who belong to a MBus slave device
     */
    protected List<OfflineDeviceMessage> getMbusMessages(List<OfflineDeviceMessage> deviceMessages) {
        List<OfflineDeviceMessage> mbusMessages = new ArrayList<>();

        for (OfflineDeviceMessage pendingMessage : deviceMessages) {
            if (!getProtocol().getSerialNumber().equals(pendingMessage.getDeviceSerialNumber())) {
                mbusMessages.add(pendingMessage);
            }
        }
        return mbusMessages;
    }

    protected AbstractMessageExecutor getMbusMessageExecutor() {
        if (this.mbusMessageExecutor == null) {
            this.mbusMessageExecutor = new Dsmr23MbusMessageExecutor(getProtocol());
        }
        return this.mbusMessageExecutor;
    }
}
