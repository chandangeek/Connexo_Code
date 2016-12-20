package com.energyict.protocolimplv2.eict.webrtuz3.messages;

import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.AssociationLN;
import com.energyict.dlms.cosem.AssociationSN;
import com.energyict.dlms.cosem.AutoConnect;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.PPPSetup;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.dlms.cosem.SecuritySetup;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.SpecialDaysTable;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.eict.webrtuz3.messages.emeter.WebRTUZ3EMeterMessageExecutor;
import com.energyict.protocolimplv2.eict.webrtuz3.messages.mbus.WebRTUZ3MBusMessageExecutor;
import com.energyict.protocolimplv2.eict.webrtuz3.topology.WebRTUZ3MeterTopology;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.AdvancedTestMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DisplayDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoadBalanceDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import com.energyict.smartmeterprotocolimpl.common.topology.DeviceMapping;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.apnAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorModeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.digitalOutputAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.passwordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.usernameAttributeName;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 6/01/2015 - 15:31
 */
public class WebRTUZ3MessageExecutor extends AbstractMessageExecutor {

    private WebRTUZ3EMeterMessageExecutor eMeterMessageExecutor;
    private WebRTUZ3MBusMessageExecutor mBusMessageExecutor;

    public WebRTUZ3MessageExecutor(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = MdcManager.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        List<OfflineDeviceMessage> mbusMessages = new ArrayList<>();
        List<OfflineDeviceMessage> emeterMessages = new ArrayList<>();

        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            if (isMbusMessage(pendingMessage.getDeviceSerialNumber())) {
                mbusMessages.add(pendingMessage);
            } else if (isEmeterMessage(pendingMessage.getDeviceSerialNumber())) {
                emeterMessages.add(pendingMessage);
            } else {
                CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
                try {
                    collectedMessage = executeMessage(pendingMessage, collectedMessage);
                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSessionProperties().getRetries() + 1)) {
                        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                        collectedMessage.setDeviceProtocolInformation(e.getMessage());
                        collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                    }   //Else: throw communication exception
                } catch (IndexOutOfBoundsException | NumberFormatException e) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                }
                result.addCollectedMessage(collectedMessage);
            }
        }

        if (!mbusMessages.isEmpty()) {
            CollectedMessageList collectedMBusMessages = getMBusMessageExecutor().executePendingMessages(mbusMessages);
            result.addCollectedMessages(collectedMBusMessages);
        }

        if (!emeterMessages.isEmpty()) {
            CollectedMessageList collectedEMeterMessages = getEMeterMessageExecutor().executePendingMessages(emeterMessages);
            result.addCollectedMessages(collectedEMeterMessages);
        }

        return result;
    }

    private WebRTUZ3MBusMessageExecutor getMBusMessageExecutor() {
        if (mBusMessageExecutor == null) {
            mBusMessageExecutor = new WebRTUZ3MBusMessageExecutor(getProtocol());
        }
        return mBusMessageExecutor;
    }

    private WebRTUZ3EMeterMessageExecutor getEMeterMessageExecutor() {
        if (eMeterMessageExecutor == null) {
            eMeterMessageExecutor = new WebRTUZ3EMeterMessageExecutor(getProtocol());
        }
        return eMeterMessageExecutor;
    }

    private boolean isEmeterMessage(final String serialNumber) {
        for (DeviceMapping deviceMapping : ((WebRTUZ3MeterTopology) getProtocol().getMeterTopology()).geteMeterMap()) {
            if (deviceMapping.getSerialNumber().equalsIgnoreCase(serialNumber)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMbusMessage(final String serialNumber) {
        for (DeviceMapping deviceMapping : ((WebRTUZ3MeterTopology) getProtocol().getMeterTopology()).getMbusMap()) {
            if (deviceMapping.getSerialNumber().equalsIgnoreCase(serialNumber)) {
                return true;
            }
        }
        return false;
    }

    protected CollectedMessage executeMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_OUTPUT)) {
            contactorClose(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_OUTPUT)) {
            contactorOpen(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_OUTPUT_AND_ACTIVATION_DATE)) {
            contactorCloseWithActivationDate(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_OUTPUT_AND_ACTIVATION_DATE)) {
            contactorOpenWithActivationDate(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE)) {
            changeConnectControlMode(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE)) {
            upgradeFirmware(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE)) {
            upgradeFirmware(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND)) {
            writeActivityCalendar(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_SEND_WITH_DATETIME)) {
            writeActivityCalendar(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ActivityCalendarDeviceMessage.SPECIAL_DAY_CALENDAR_SEND)) {
            writeSpecialDays(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.ACTIVATE_DLMS_ENCRYPTION)) {
            activateDlmsEncryption(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEY)) {
            changeKey(pendingMessage, 0);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEY)) {
            changeKey(pendingMessage, 2);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_PASSWORD_WITH_NEW_PASSWORD)) {
            changeHlsSecret(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ClockDeviceMessage.SET_TIME)) {
            setTime(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.ACTIVATE_WAKEUP_MECHANISM)) {
            activateWakeUpMechanism();
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.DEACTIVATE_SMS_WAKEUP)) {
            disableWakeUpMechanism();
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_GPRS_USER_CREDENTIALS)) {
            changeGPRSSettings(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS)) {
            changeGPRSSettings(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST)) {
            addPhoneNumbersToWhiteList(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(DisplayDeviceMessage.CONSUMER_MESSAGE_TEXT_TO_PORT_P1)) {
            sendTextToP1(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(DisplayDeviceMessage.CONSUMER_MESSAGE_CODE_TO_PORT_P1)) {
            sendCodeToP1(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.GLOBAL_METER_RESET)) {
            globalMeterReset();
        } else if (pendingMessage.getSpecification().equals(AdvancedTestMessage.XML_CONFIG)) {
            xmlConfig(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.CONFIGURE_LOAD_LIMIT_PARAMETERS)) {
            configureLoadLimitParameters(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.SET_EMERGENCY_PROFILE_GROUP_IDS)) {
            setEmergencyProfileGroupIds(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(LoadBalanceDeviceMessage.CLEAR_LOAD_LIMIT_CONFIGURATION)) {
            clearLoadLimitConfiguration();
        } else {   //Unsupported message
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
            collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol");
        }
        return collectedMessage;
    }

    @Override
    protected AXDRDateTime getEmergencyProfileActivationAXDRDateTime(String emergencyProfileActivationDate) {
        return convertUnixToDateTime(emergencyProfileActivationDate, getProtocol().getTimeZone());
    }

    private void xmlConfig(OfflineDeviceMessage pendingMessage) throws IOException {
        String xml = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        getCosemObjectFactory().getData(getMeterConfig().getXMLConfig().getObisCode()).setValueAttr(OctetString.fromString(xml));
    }

    private void globalMeterReset() throws IOException {
        ScriptTable globalResetST = getCosemObjectFactory().getGlobalMeterResetScriptTable();
        globalResetST.invoke(1);    // execute script one
    }

    private void sendTextToP1(OfflineDeviceMessage pendingMessage) throws IOException {
        String text = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        Data dataCode = getCosemObjectFactory().getData(getMeterConfig().getConsumerMessageText().getObisCode());
        dataCode.setValueAttr(OctetString.fromString(text));
    }

    private void sendCodeToP1(OfflineDeviceMessage pendingMessage) throws IOException {
        String code = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        Data dataCode = getCosemObjectFactory().getData(getMeterConfig().getConsumerMessageCode().getObisCode());
        dataCode.setValueAttr(OctetString.fromString(code));
    }

    private void addPhoneNumbersToWhiteList(OfflineDeviceMessage pendingMessage) throws IOException {
        String[] phoneNumbers = pendingMessage.getDeviceMessageAttributes().get(0).getValue().split(";");

        AutoConnect autoConnect = getCosemObjectFactory().getAutoConnect();
        Array array = new Array();
        for (String phoneNumber : phoneNumbers) {
            array.addDataType(OctetString.fromString(phoneNumber));
        }
        autoConnect.writeDestinationList(array);
    }

    private void changeGPRSSettings(OfflineDeviceMessage pendingMessage) throws IOException {
        String user = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, usernameAttributeName).getValue();
        String pass = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, passwordAttributeName).getValue();
        String apn = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, apnAttributeName).getValue();

        PPPSetup.PPPAuthenticationType pppat = getCosemObjectFactory().getPPPSetup().new PPPAuthenticationType();
        pppat.setAuthenticationType(PPPSetup.LCPOptionsType.AUTH_PAP);
        pppat.setUserName(user);
        pppat.setPassWord(pass);
        getCosemObjectFactory().getPPPSetup().writePPPAuthenticationType(pppat);

        if (apn != null && !apn.isEmpty()) {
            getCosemObjectFactory().getGPRSModemSetup().writeAPN(apn);
        }
    }

    private void activateWakeUpMechanism() throws IOException {
        getCosemObjectFactory().getAutoConnect().writeMode(4);
    }

    private void disableWakeUpMechanism() throws IOException {
        getCosemObjectFactory().getAutoConnect().writeMode(1);
    }

    private void setTime(OfflineDeviceMessage pendingMessage) throws IOException {
        long epoch = Long.parseLong(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
        getProtocol().getDlmsSession().getCosemObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(new Date(epoch), getProtocol().getTimeZone()));
    }

    private void changeHlsSecret(OfflineDeviceMessage pendingMessage) throws IOException {
        String hexHlsSecret = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        byte[] hlsSecret = ProtocolTools.getBytesFromHexString(hexHlsSecret, "");

        if (getProtocol().getDlmsSession().getReference() == ProtocolLink.LN_REFERENCE) {
            AssociationLN aln = getCosemObjectFactory().getAssociationLN();

            // We just return the byteArray because it is possible that the berEncoded octetString contains
            // extra check bits ...
            aln.changeHLSSecret(hlsSecret);
        } else if (getProtocol().getDlmsSession().getReference() == ProtocolLink.SN_REFERENCE) {
            AssociationSN asn = getCosemObjectFactory().getAssociationSN();

            // We just return the byteArray because it is possible that the berEncoded octetString contains
            // extra check bits ...
            //TODO low lever security should set the value directly to the secret attribute of the SNAssociation
            asn.changeSecret(hlsSecret);
        }
    }

    private void changeKey(OfflineDeviceMessage pendingMessage, int type) throws IOException {
        Array globalKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(type));    // 0 means keyType: global unicast encryption key, 2 means keyType: authenticationKey
        String wrappedHexKey = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        byte[] key = ProtocolTools.getBytesFromHexString(wrappedHexKey, "");
        keyData.addDataType(OctetString.fromByteArray(key));
        globalKeyArray.addDataType(keyData);

        SecuritySetup ss = getCosemObjectFactory().getSecuritySetup();
        ss.transferGlobalKey(globalKeyArray);
    }

    private void activateDlmsEncryption(OfflineDeviceMessage pendingMessage) throws IOException {
        int level = Integer.parseInt(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
        getCosemObjectFactory().getSecuritySetup().activateSecurity(new TypeEnum(level));
    }

    private void writeSpecialDays(OfflineDeviceMessage pendingMessage) throws IOException {
        SpecialDaysTable specialDaysTable = getCosemObjectFactory().getSpecialDaysTable(getMeterConfig().getSpecialDaysTable().getObisCode());
        String specialDaysHex = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        Array specialDaysArray = new Array(ProtocolTools.getBytesFromHexString(specialDaysHex, ""), 0, 0);
        specialDaysTable.writeSpecialDays(specialDaysArray);
    }

    private void writeActivityCalendar(OfflineDeviceMessage pendingMessage) throws IOException {
        String calendarName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarNameAttributeName).getValue();
        String profiles = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarCodeTableAttributeName).getValue();
        String epoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarActivationDateAttributeName).getValue();

        String[] profilesSplit = profiles.split("\\|");
        String dayProfileHex = profilesSplit[0];
        String weekProfileHex = profilesSplit[1];
        String seasonProfileHex = profilesSplit[2];

        ActivityCalendar activityCalendar = getCosemObjectFactory().getActivityCalendar(getMeterConfig().getActivityCalendar().getObisCode());
        activityCalendar.writeSeasonProfilePassive(new Array(ProtocolTools.getBytesFromHexString(seasonProfileHex, ""), 0, 0));
        activityCalendar.writeWeekProfileTablePassive(new Array(ProtocolTools.getBytesFromHexString(weekProfileHex, ""), 0, 0));
        activityCalendar.writeDayProfileTablePassive(new Array(ProtocolTools.getBytesFromHexString(dayProfileHex, ""), 0, 0));

        if (calendarName.length() > 8) {
            calendarName = calendarName.substring(0, 8);
        }
        activityCalendar.writeCalendarNamePassive(OctetString.fromString(calendarName));

        if (epoch.isEmpty()) {
            activityCalendar.activateNow();
        } else {
            Calendar cal = Calendar.getInstance(getProtocol().getTimeZone());
            cal.setTime(new Date(Long.parseLong(epoch)));
            activityCalendar.writeActivatePassiveCalendarTime(new OctetString(new AXDRDateTime(cal).getBEREncodedByteArray(), 0));
        }
    }

    private void upgradeFirmware(OfflineDeviceMessage pendingMessage) throws IOException {
        String hexUserFileContent = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateUserFileAttributeName).getValue();
        String activationEpochString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, firmwareUpdateActivationDateAttributeName).getValue();

        byte[] imageData = ProtocolTools.getBytesFromHexString(hexUserFileContent, "");
        ImageTransfer it = getCosemObjectFactory().getImageTransfer();
        it.upgrade(imageData);

        if (activationEpochString.isEmpty()) { // Do an execute now
            it.imageActivation();
        } else {
            SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
            Array dateArray = convertEpochToDateTimeArray(activationEpochString);
            sas.writeExecutionTime(dateArray);
        }
    }

    private void changeConnectControlMode(OfflineDeviceMessage pendingMessage) throws IOException {
        int outputId = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, digitalOutputAttributeName).getValue()).intValue();
        int mode = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, contactorModeAttributeName).getValue()).intValue();

        Disconnector connectorMode = getCosemObjectFactory().getDisconnector(getDisconnectorObisCode(outputId));
        connectorMode.writeControlMode(new TypeEnum(mode));
    }

    private void contactorOpen(OfflineDeviceMessage pendingMessage) throws IOException {
        int outputId = new BigDecimal(pendingMessage.getDeviceMessageAttributes().get(0).getValue()).intValue();
        Disconnector disconnector = getCosemObjectFactory().getDisconnector(getDisconnectorObisCode(outputId));
        disconnector.remoteDisconnect();
    }

    private void contactorClose(OfflineDeviceMessage pendingMessage) throws IOException {
        int outputId = new BigDecimal(pendingMessage.getDeviceMessageAttributes().get(0).getValue()).intValue();
        Disconnector disconnector = getCosemObjectFactory().getDisconnector(getDisconnectorObisCode(outputId));
        disconnector.remoteReconnect();
    }

    private void contactorCloseWithActivationDate(OfflineDeviceMessage pendingMessage) throws IOException {
        int outputId = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, digitalOutputAttributeName).getValue()).intValue();
        String epoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, contactorActivationDateAttributeName).getValue();

        Array executionTimeArray = convertEpochToDateTimeArray(epoch);
        SingleActionSchedule sasConnect = getCosemObjectFactory().getSingleActionSchedule(getDisconnectControlScheduleObis(outputId));

        ScriptTable disconnectorScriptTable = getCosemObjectFactory().getScriptTable(getDisconnectorScriptTableObis(outputId));
        byte[] scriptLogicalName = disconnectorScriptTable.getObjectReference().getLn();
        Structure scriptStruct = new Structure();
        scriptStruct.addDataType(OctetString.fromByteArray(scriptLogicalName));
        scriptStruct.addDataType(new Unsigned16(2));     // method '2' is the 'remote_connect' method

        sasConnect.writeExecutedScript(scriptStruct);
        sasConnect.writeExecutionTime(executionTimeArray);
    }

    private void contactorOpenWithActivationDate(OfflineDeviceMessage pendingMessage) throws IOException {
        int outputId = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, digitalOutputAttributeName).getValue()).intValue();
        String epoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, contactorActivationDateAttributeName).getValue();

        Array executionTimeArray = convertEpochToDateTimeArray(epoch);
        SingleActionSchedule sasDisconnect = getCosemObjectFactory().getSingleActionSchedule(getDisconnectControlScheduleObis(outputId));

        ScriptTable disconnectorScriptTable = getCosemObjectFactory().getScriptTable(getDisconnectorScriptTableObis(outputId));
        byte[] scriptLogicalName = disconnectorScriptTable.getObjectReference().getLn();
        Structure scriptStruct = new Structure();
        scriptStruct.addDataType(OctetString.fromByteArray(scriptLogicalName));
        scriptStruct.addDataType(new Unsigned16(1));     // method '1' is the 'remote_disconnect' method

        sasDisconnect.writeExecutedScript(scriptStruct);
        sasDisconnect.writeExecutionTime(executionTimeArray);
    }

    private ObisCode getDisconnectControlScheduleObis(int outputId) throws IOException {
        return ObisCode.fromString("0.0.15.0." + outputId + ".255");
    }

    private ObisCode getDisconnectorScriptTableObis(int outputId) throws IOException {
        return ObisCode.fromString("0.0.10.0." + (105 + outputId) + ".255");
    }

    private ObisCode getDisconnectorObisCode(int outputId) throws IOException {
        return ObisCode.fromString("0.0.96.3." + (9 + outputId) + ".255");
    }
}