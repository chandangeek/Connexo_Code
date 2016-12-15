package com.energyict.protocolimplv2.dlms.idis.am130.messages;

import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.IDISMessageExecutor;
import com.energyict.protocolimplv2.messages.AlarmConfigurationMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.enums.ClientSecuritySetup;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/02/2015 - 15:10
 */
public class AM130MessageExecutor extends IDISMessageExecutor {

    protected static final ObisCode ALARM_BITS_OBISCODE_1 = ObisCode.fromString("0.0.97.98.0.255");
    protected static final ObisCode ALARM_FILTER_OBISCODE_1 = ObisCode.fromString("0.0.97.98.10.255");
    protected static final ObisCode ALARM_DESCRIPTOR_OBISCODE_1 = ObisCode.fromString("0.0.97.98.20.255");

    protected static final ObisCode ALARM_BITS_OBISCODE_2 = ObisCode.fromString("0.0.97.98.1.255");
    protected static final ObisCode ALARM_FILTER_OBISCODE_2 = ObisCode.fromString("0.0.97.98.11.255");
    protected static final ObisCode ALARM_DESCRIPTOR_OBISCODE_2 = ObisCode.fromString("0.0.97.98.21.255");
    private static final int MAX_MBUS_SLAVES = 6;

    public AM130MessageExecutor(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    protected int getMaxMBusSlaves() {
        return MAX_MBUS_SLAVES;
    }

    @Override
    protected CollectedMessage executeMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.RESET_DESCRIPTOR_FOR_ALARM_REGISTER_1_OR_2)) {
            collectedMessage = resetAlarmDescriptor(pendingMessage, collectedMessage);
        } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.RESET_BITS_IN_ALARM_REGISTER_1_OR_2)) {
            collectedMessage = resetAlarmBits(pendingMessage, collectedMessage);
        } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.RESET_ALL_ALARM_BITS)) {
            collectedMessage = resetAllAlarmBits1and2(collectedMessage);
        } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.WRITE_FILTER_FOR_ALARM_REGISTER_1_OR_2)) {
            collectedMessage = writeFilterForAlarm1or2(pendingMessage, collectedMessage);
        } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.FULLY_CONFIGURE_PUSH_EVENT_NOTIFICATION)) {
            collectedMessage = configurePushSetup(pendingMessage, collectedMessage);
        } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION_OBJECT_DEFINITIONS)) {
            collectedMessage = configurePushSetupObjectDefinitions(pendingMessage, collectedMessage);
        } else if (pendingMessage.getSpecification().equals(AlarmConfigurationMessage.CONFIGURE_PUSH_EVENT_NOTIFICATION_SEND_DESTINATION)) {
            collectedMessage = configurePushSetupSendDestination(pendingMessage, collectedMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.SetIPAddress)) {
            writeIpAddress(pendingMessage, collectedMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.SetSubnetMask)) {
            writeSubNetMask(pendingMessage, collectedMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.SetGateway)) {
            writeGatewayIPAddress(pendingMessage, collectedMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.SetPrimaryDNSAddress)) {
            writePrimaryDNSAddress(pendingMessage, collectedMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.SetSecondaryDNSAddress)) {
            writeSecondaryDNSAddress(pendingMessage, collectedMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.SetUseDHCPFlag)) {
            setUseDHCPFlag(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS)) {
            changeGPRSParameters(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.ADD_PHONENUMBERS_TO_WHITE_LIST)) {
            addPhoneNumberToWhiteList(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.ClearWhiteList)) {
            clearWhiteList();
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.SetAutoConnectMode)) {
            setAutoConnectMode(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS)) {
            changeAuthenticationKeyAndUseNewKey(collectedMessage, pendingMessage);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS)) {
            changeEncryptionKeyAndUseNewKey(collectedMessage, pendingMessage);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_MASTER_KEY_WITH_NEW_KEYS)) {
            changeMasterKeyAndUseNewKey(collectedMessage, pendingMessage);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_AUTHENTICATION_KEY_WITH_NEW_KEYS_FOR_PREDEFINED_CLIENT)) {
            changeAuthenticationKeyAndUseNewKey(collectedMessage, pendingMessage);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_ENCRYPTION_KEY_WITH_NEW_KEYS_FOR_PREDEFINED_CLIENT)) {
            changeEncryptionKeyAndUseNewKey(collectedMessage, pendingMessage);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_MASTER_KEY_WITH_NEW_KEYS_FOR_PREDEFINED_CLIENT)) {
            changeMasterKeyAndUseNewKey(collectedMessage, pendingMessage);
        } else {
            collectedMessage = super.executeMessage(pendingMessage, collectedMessage);
        }
        return collectedMessage;
    }


    private CollectedMessage resetAllAlarmBits1and2(CollectedMessage collectedMessage) {
        StringBuilder   sb = new StringBuilder();
        ObisCode alarmBitsObisCodes[] = {ALARM_BITS_OBISCODE_1, ALARM_BITS_OBISCODE_2};

        for (ObisCode alarmBitsObisCode : alarmBitsObisCodes) {
            try {
                resetAllAlarmBits(alarmBitsObisCode);
                sb.append("Alarm bits reset for ").append(alarmBitsObisCode.toString()).append("; ");
            } catch (IOException e) {
                sb.append(e.getMessage()).append(" while performing alarm bits reset for ").append(alarmBitsObisCode);
            }
        }

        collectedMessage.setDeviceProtocolInformation(sb.toString());
        return collectedMessage;
    }

    private CollectedMessage configurePushSetupSendDestination(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        String setupType = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.typeAttributeName).getDeviceMessageAttributeValue();
        ObisCode pushSetupObisCode = EventPushNotificationConfig.getDefaultObisCode();
        pushSetupObisCode.setB(AlarmConfigurationMessage.PushType.valueOf(setupType).getId());
        EventPushNotificationConfig eventPushNotificationConfig = getCosemObjectFactory().getEventPushNotificationConfig(pushSetupObisCode);

        String transportTypeString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.transportTypeAttributeName).getDeviceMessageAttributeValue();
        int transportType = AlarmConfigurationMessage.TransportType.valueOf(transportTypeString).getId();

        String destinationAddress = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.destinationAddressAttributeName).getDeviceMessageAttributeValue();

        String messageTypeString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.messageTypeAttributeName).getDeviceMessageAttributeValue();
        int messageType = AlarmConfigurationMessage.MessageType.valueOf(messageTypeString).getId();

        eventPushNotificationConfig.writeSendDestinationAndMethod(transportType, destinationAddress, messageType);
        return collectedMessage;
    }

    private CollectedMessage configurePushSetupObjectDefinitions(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        String setupType = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.typeAttributeName).getDeviceMessageAttributeValue();
        ObisCode pushSetupObisCode = EventPushNotificationConfig.getDefaultObisCode();
        pushSetupObisCode.setB(AlarmConfigurationMessage.PushType.valueOf(setupType).getId());
        EventPushNotificationConfig eventPushNotificationConfig = getCosemObjectFactory().getEventPushNotificationConfig(pushSetupObisCode);

        String objectDefinitionsAttributeValue = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.objectDefinitionsAttributeName).getDeviceMessageAttributeValue();
        List<ObjectDefinition> objectDefinitions;
        try {
            objectDefinitions = composePushSetupObjectDefinitions(pushSetupObisCode, objectDefinitionsAttributeValue);
        } catch (Throwable e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String msg = "The object definition attribute has a wrong format: " + e.getMessage();
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, msg));
            collectedMessage.setDeviceProtocolInformation(msg);
            return collectedMessage;
        }

        eventPushNotificationConfig.writePushObjectList(objectDefinitions);
        return collectedMessage;
    }

    private CollectedMessage configurePushSetup(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        String setupType = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.typeAttributeName).getDeviceMessageAttributeValue();
        ObisCode pushSetupObisCode = EventPushNotificationConfig.getDefaultObisCode();
        pushSetupObisCode.setB(AlarmConfigurationMessage.PushType.valueOf(setupType).getId());
        EventPushNotificationConfig eventPushNotificationConfig = getCosemObjectFactory().getEventPushNotificationConfig(pushSetupObisCode);

        String objectDefinitionsAttributeValue = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.objectDefinitionsAttributeName).getDeviceMessageAttributeValue();
        List<ObjectDefinition> objectDefinitions;
        try {
            objectDefinitions = composePushSetupObjectDefinitions(pushSetupObisCode, objectDefinitionsAttributeValue);
        } catch (Throwable e) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String msg = "The object definition attribute has a wrong format: " + e.getMessage();
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, msg));
            collectedMessage.setDeviceProtocolInformation(msg);
            return collectedMessage;
        }

        String transportTypeString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.transportTypeAttributeName).getDeviceMessageAttributeValue();
        int transportType = AlarmConfigurationMessage.TransportType.valueOf(transportTypeString).getId();

        String destinationAddress = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.destinationAddressAttributeName).getDeviceMessageAttributeValue();

        String messageTypeString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.messageTypeAttributeName).getDeviceMessageAttributeValue();
        int messageType = AlarmConfigurationMessage.MessageType.valueOf(messageTypeString).getId();

        eventPushNotificationConfig.writePushObjectList(objectDefinitions);
        eventPushNotificationConfig.writeSendDestinationAndMethod(transportType, destinationAddress, messageType);
        return collectedMessage;
    }

    protected List<ObjectDefinition> composePushSetupObjectDefinitions(ObisCode pushSetupObisCode, String objectDefinitionsAttributeValue) throws ProtocolException {
        List<ObjectDefinition> objectDefinitions = new ArrayList<>();
        addObjectDefinitionsToConfig(objectDefinitions, pushSetupObisCode, DLMSClassId.PUSH_EVENT_NOTIFICATION_SETUP.getClassId(), 1);
        for (String definition : objectDefinitionsAttributeValue.split(";")) {
            ObisCode obisCode = ObisCode.fromString(definition);
            int classId = getCosemObjectFactory().getProtocolLink().getMeterConfig().getClassId(obisCode);
            switch (classId) {
                case 1:
                    addObjectDefinitionsToConfig(objectDefinitions, obisCode, classId, 1, 2); // Logical name, value
                    break;
                case 3:
                    addObjectDefinitionsToConfig(objectDefinitions, obisCode, classId, 1, 2, 3); // Logical name, valua, scaler_unit
                    break;
                case 4:
                    addObjectDefinitionsToConfig(objectDefinitions, obisCode, classId, 1, 2, 3, 5); // Logical name, value, scaler_unit, capture_time
                case 5:
                    addObjectDefinitionsToConfig(objectDefinitions, obisCode, classId, 1, 2, 4, 6); // Logical name, value, scaler_unit, capture_time
                    break;
                default:
                    throw new ProtocolException("Object " + obisCode + " is of class " + classId + ", only objects of classes 1, 3, 4 and 5 are supported.");
            }
        }
        return objectDefinitions;
    }

    protected void addObjectDefinitionsToConfig(List<ObjectDefinition> objectDefinitions, ObisCode obisCode, int classId, int... attributes) {
        for (int attribute : attributes) {
            objectDefinitions.add(new ObjectDefinition(classId, obisCode, attribute, 0));
        }
    }

    protected CollectedMessage changeMasterKeyAndUseNewKey(CollectedMessage collectedMessage,OfflineDeviceMessage pendingMessage) throws IOException {
        String newKey = getDeviceMessageAttributeValue(pendingMessage, newMasterKeyAttributeName);
        changeKeyAndUseNewKey(pendingMessage, SecurityMessage.KeyID.MASTER_KEY.getId(), newWrappedMasterKeyAttributeName);

        //Update the key in the security provider, it is used instantly
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeMasterKey(ProtocolTools.getBytesFromHexString(newKey, ""));
        return collectedMessage;
    }

    protected CollectedMessage changeAuthenticationKeyAndUseNewKey(CollectedMessage collectedMessage,OfflineDeviceMessage pendingMessage) throws IOException {
        String newKey = getDeviceMessageAttributeValue(pendingMessage, newAuthenticationKeyAttributeName);
        changeKeyAndUseNewKey(pendingMessage, SecurityMessage.KeyID.AUTHENTICATION_KEY.getId(), newWrappedAuthenticationKeyAttributeName);

        //Update the key in the security provider, it is used instantly
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeAuthenticationKey(ProtocolTools.getBytesFromHexString(newKey, ""));
        return collectedMessage;
    }

    protected CollectedMessage changeEncryptionKeyAndUseNewKey(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) throws IOException {
        String newKey = getDeviceMessageAttributeValue(pendingMessage, newEncryptionKeyAttributeName);
        changeKeyAndUseNewKey(pendingMessage, SecurityMessage.KeyID.GLOBAL_UNICAST_ENCRYPTION_KEY.getId(), newWrappedEncryptionKeyAttributeName);

        //Update the key in the security provider, it is used instantly
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeEncryptionKey(ProtocolTools.getBytesFromHexString(newKey, ""));

        //Reset frame counter, only if a different key has been written
        SecurityContext securityContext = getProtocol().getDlmsSession().getAso().getSecurityContext();
        securityContext.setFrameCounter(1);
        securityContext.getSecurityProvider().getRespondingFrameCounterHandler().setRespondingFrameCounter(-1);

        return collectedMessage;
    }

    protected void changeKeyAndUseNewKey(OfflineDeviceMessage pendingMessage, int keyId, String wrappedKeyAttributeName) throws IOException {
        String newWrappedKey = getDeviceMessageAttributeValue(pendingMessage, wrappedKeyAttributeName);
        byte[] keyBytes = ProtocolTools.getBytesFromHexString(newWrappedKey, "");
        ObisCode clientSecuritySetupObis = getClientSecuritySetupObis(pendingMessage);

        Array keyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(keyId));
        keyData.addDataType(OctetString.fromByteArray(keyBytes));
        keyArray.addDataType(keyData);

        SecuritySetup ss = getCosemObjectFactory().getSecuritySetup(clientSecuritySetupObis);
        ss.transferGlobalKey(keyArray);
    }

    protected void clearWhiteList() throws IOException {
        getCosemObjectFactory().getAutoAnswer().writeListOfAllowedCallers(new ArrayList<String>());
    }

    protected void setAutoConnectMode(OfflineDeviceMessage pendingMessage) throws IOException {
        int mode = new BigDecimal(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue()).intValue();
        getCosemObjectFactory().getAutoConnect().writeMode(mode);
    }

    protected void addPhoneNumberToWhiteList(OfflineDeviceMessage pendingMessage) throws IOException {
        //semicolon separated list of phone numbers
        String phoneNumbers = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, whiteListPhoneNumbersAttributeName).getDeviceMessageAttributeValue();
        getCosemObjectFactory().getAutoAnswer().addListOfAllowedCallers(Arrays.asList(phoneNumbers.split(";")));
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

    private void setUseDHCPFlag(OfflineDeviceMessage pendingMessage) throws IOException {
        Boolean dhcp = Boolean.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        getIPv4Setup().setDHCPFlag(dhcp);
    }

    private CollectedMessage writeIpAddress(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        Long ipAddress = parseIpAddressAttribute(pendingMessage, collectedMessage);
        if (ipAddress == null) {
            return collectedMessage;    //Parsing failed, return the proper error message
        } else {
            getIPv4Setup().writeIPAddress(new Unsigned32(ipAddress));
            return collectedMessage;
        }
    }

    private IPv4Setup getIPv4Setup() throws ProtocolException {
        return getCosemObjectFactory().getIPv4Setup();
    }

    private CollectedMessage writeSubNetMask(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        Long ipAddress = parseIpAddressAttribute(pendingMessage, collectedMessage);
        if (ipAddress == null) {
            return collectedMessage;    //Parsing failed, return the proper error message
        } else {
            getIPv4Setup().writeSubnetMask(new Unsigned32(ipAddress));
            return collectedMessage;
        }
    }

    private CollectedMessage writeGatewayIPAddress(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        Long ipAddress = parseIpAddressAttribute(pendingMessage, collectedMessage);
        if (ipAddress == null) {
            return collectedMessage;    //Parsing failed, return the proper error messagev
        } else {
            getIPv4Setup().writeGatewayIPAddress(new Unsigned32(ipAddress));
            return collectedMessage;
        }
    }

    private CollectedMessage writePrimaryDNSAddress(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        Long ipAddress = parseIpAddressAttribute(pendingMessage, collectedMessage);
        if (ipAddress == null) {
            return collectedMessage;    //Parsing failed, return the proper error message
        } else {
            getIPv4Setup().writePrimaryDNSAddress(new Unsigned32(ipAddress));
            return collectedMessage;
        }
    }

    private CollectedMessage writeSecondaryDNSAddress(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        Long ipAddress = parseIpAddressAttribute(pendingMessage, collectedMessage);
        if (ipAddress == null) {
            return collectedMessage;    //Parsing failed, return the proper error message
        } else {
            getIPv4Setup().writeSecondaryDNSAddress(new Unsigned32(ipAddress));
            return collectedMessage;
        }
    }

    private Long parseIpAddressAttribute(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) {
        String ipAddressString = pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue();
        String[] split = ipAddressString.split("\\.");
        List<Integer> result = new ArrayList<>();

        for (String ipNumberString : split) {
            try {
                Integer ipNumber = Integer.valueOf(ipNumberString);
                if (ipNumber <= 255 && ipNumber >= 0) {
                    result.add(ipNumber);
                } else {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    String msg = "Invalid numbers in IP address '" + ipAddressString + "'";
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, msg));
                    collectedMessage.setDeviceProtocolInformation(msg);
                    return null;
                }
            } catch (NumberFormatException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                String msg = "IP address attribute should be 4 numbers separated by a dot";
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, msg));
                collectedMessage.setDeviceProtocolInformation(msg);
                return null;
            }
        }

        if (result.size() != 4) {
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            String msg = "IP address attribute should be 4 numbers separated by a dot";
            collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, msg));
            collectedMessage.setDeviceProtocolInformation(msg);
            return null;
        }

        return (long) ((result.get(0) << 24) + (result.get(1) << 16) + (result.get(2) << 8) + (result.get(3)));
    }

    private CollectedMessage writeFilterForAlarm1or2(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        int register = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.alarmRegisterAttributeName).getDeviceMessageAttributeValue());
        BigDecimal filter = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.alarmFilterAttributeName).getDeviceMessageAttributeValue());
        ObisCode alarmFilterObisCode;
        switch (register) {
            case 1:
                alarmFilterObisCode = ALARM_FILTER_OBISCODE_1;
                break;
            case 2:
                alarmFilterObisCode = ALARM_FILTER_OBISCODE_2;
                break;
            default:
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                String msg = "Unsupported value '" + register + "' for attribute " + DeviceMessageConstants.alarmRegisterAttributeName + ", expected '1' or '2'.";
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, msg));
                collectedMessage.setDeviceProtocolInformation(msg);
                return collectedMessage;
        }

        writeAlarmFilter(alarmFilterObisCode, filter.longValue());
        return collectedMessage;
    }

    private CollectedMessage resetAlarmBits(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        int register = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.alarmRegisterAttributeName).getDeviceMessageAttributeValue());
        ObisCode alarmRegisterObisCode;
        switch (register) {
            case 1:
                alarmRegisterObisCode = ALARM_BITS_OBISCODE_1;
                break;
            case 2:
                alarmRegisterObisCode = ALARM_BITS_OBISCODE_2;
                break;
            default:
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                String msg = "Unsupported value '" + register + "' for attribute " + DeviceMessageConstants.alarmRegisterAttributeName + ", expected '1' or '2'.";
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, msg));
                collectedMessage.setDeviceProtocolInformation(msg);
                return collectedMessage;
        }

        resetAllAlarmBits(alarmRegisterObisCode);
        collectedMessage.setDeviceProtocolInformation("Alarm bits reset for "+alarmRegisterObisCode.toString());
        return collectedMessage;
    }

    private CollectedMessage resetAlarmDescriptor(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        int register = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.alarmRegisterAttributeName).getDeviceMessageAttributeValue());
        BigDecimal alarmBits = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.alarmBitMaskAttributeName).getDeviceMessageAttributeValue());
        ObisCode alarmDescriptorObisCode;
        switch (register) {
            case 1:
                alarmDescriptorObisCode = ALARM_DESCRIPTOR_OBISCODE_1;
                break;
            case 2:
                alarmDescriptorObisCode = ALARM_DESCRIPTOR_OBISCODE_2;
                break;
            default:
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                String msg = "Unsupported value '" + register + "' for attribute " + DeviceMessageConstants.alarmRegisterAttributeName + ", expected '1' or '2'.";
                collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, msg));
                collectedMessage.setDeviceProtocolInformation(msg);
                return collectedMessage;
        }

        getCosemObjectFactory().getData(alarmDescriptorObisCode).setValueAttr(new Unsigned32(alarmBits.longValue()));
        return collectedMessage;
    }

    protected ObisCode getClientSecuritySetupObis(OfflineDeviceMessage pendingMessage){
        String client = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.client).getDeviceMessageAttributeValue();
        if (client!=null && !client.isEmpty()) {
            try{
                return ClientSecuritySetup.valueOf(client).getSecuritySetupOBIS();
            } catch (Exception ex){
                // ignore
            }
        }
        return ClientSecuritySetup.Management.getSecuritySetupOBIS();
    }

    protected int getClientId(OfflineDeviceMessage pendingMessage){
        String client = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.client).getDeviceMessageAttributeValue();
        if (client!=null && !client.isEmpty()) {
            try{
                return ClientSecuritySetup.valueOf(client).getID();
            } catch (Exception ex){
                // ignore
            }
        }
        return ClientSecuritySetup.Management.getID();
    }

}