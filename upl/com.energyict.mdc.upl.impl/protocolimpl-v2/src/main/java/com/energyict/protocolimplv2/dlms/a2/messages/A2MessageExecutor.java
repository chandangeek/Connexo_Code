package com.energyict.protocolimplv2.dlms.a2.messages;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.tasks.support.DeviceClockSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimpl.utils.TempFileLoader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.a2.A2;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;


/**
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 * @author H236365
 * @since 12/11/2018
 */
public class A2MessageExecutor extends AbstractMessageExecutor {

    private static final ObisCode DISCONNECT_CONTROL_SINGLE_ACTION_SCHEDULE = ObisCode.fromString("0.0.15.0.1.255");
    private static final ObisCode DISCONNECT_CONTROL_SCRIPT_TABLE = ObisCode.fromString("0.0.10.0.106.255");
    private static final ObisCode VALVE_ENABLE_PASSWORD = ObisCode.fromString("0.0.94.39.1.255");
    private static final ObisCode MANAGEMENT_ASSOCIATION = ObisCode.fromString("0.0.40.0.1.255");
    private static final ObisCode INSTALLER_MAINTAINER_ASSOCIATION = ObisCode.fromString("0.0.40.0.3.255");

    public A2MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.getCollectedDataFactory().createCollectedMessageList(pendingMessages);
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            try {
                collectedMessage = executeMessage(pendingMessage, collectedMessage);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
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
        return result;
    }

    protected CollectedMessage executeMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        if (pendingMessage.getSpecification().equals(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE_AND_KDL_AND_HASH_AND_ACTIVATION)) {
            upgradeFirmware(pendingMessage);
            // clock
        } else if (pendingMessage.getSpecification().equals(ClockDeviceMessage.SET_TIMEZONE_OFFSET)) {
            setTimezone(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ClockDeviceMessage.SyncTime)) {
            synchronizeTime();
        } else if (pendingMessage.getSpecification().equals(ClockDeviceMessage.ConfigureDST)) {
            configureDST(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_GPRS_APN_CREDENTIALS)) {
            changeGPRSandAPNCredentials(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_GPRS_IP_ADDRESS_AND_PORT)) {
            changeGprsIpAddressAndPort(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CONFIGURE_AUTO_CONNECT_A2)) {
            configureAutoConnect(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_INACTIVITY_TIMEOUT)) {
            changeInactivityTimeout(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_SIM_PIN)) {
            changeSimPincode(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.ChangeSessionTimeout)) {
            changeSessionTimeout(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE)) {
            openValve(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE)) {
            closeValve(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CONTACTOR_CLOSE_AND_CLOSE_INVOICING_PERIOD_WITH_ACTIVATION_DATE)) {
            closeValveAndInvoicing(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CHANGE_VALVE_ENABLE_PASSWORD)) {
            changeValveEnablePassword(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(SecurityMessage.CHANGE_HLS_SECRET_PASSWORD_FOR_CLIENT)) {
            changeHlsSecret(pendingMessage);
        } else {   //Unsupported message
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
            collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
            collectedMessage.setDeviceProtocolInformation("Message currently not supported by the protocol");
        }
        return collectedMessage;
    }

    private void changeHlsSecret(OfflineDeviceMessage pendingMessage) throws IOException {
        checkDeviceSerialNumber(pendingMessage);
        int client = Integer.valueOf(getDeviceMessageAttributeValue(pendingMessage, clientMacAddress));
        AssociationLN associationLN;
        if (client == A2.MANAGEMENT_CLIENT) {
            associationLN = getCosemObjectFactory().getAssociationLN(MANAGEMENT_ASSOCIATION);
        } else if (client == A2.INSTALLER_MAINTAINER_CLIENT) {
            associationLN = getCosemObjectFactory().getAssociationLN(INSTALLER_MAINTAINER_ASSOCIATION);
        } else {
            throw new ProtocolException(String.join(
                    "", "Unexpected client number: ", Integer.toString(client),
                    ", expected 1 (management client) or 3 (installer/maintainer client)"));
        }
        String key = getDeviceMessageAttributeValue(pendingMessage, newPasswordAttributeName);
        byte[] bytes = ProtocolTools.getBytesFromHexString(key, "");
        associationLN.changeHLSSecret(bytes);
    }

    private void checkDeviceSerialNumber(OfflineDeviceMessage pendingMessage) throws IOException {
        String logicalName = getCosemObjectFactory().getData(A2.COSEM_LOGICAL_DEVICE_NAME).getValueAttr().getOctetString().stringValue();
        String serialNumber = pendingMessage.getDeviceSerialNumber();
        if (!serialNumber.equalsIgnoreCase(logicalName)) {
            throw new ProtocolException(String.join(
                    " ", "Wrong device serial number for key renewal. Expected: ", serialNumber,
                    "found:", logicalName));
        }
    }

    private void changeSimPincode(OfflineDeviceMessage pendingMessage) throws IOException {
        int pin = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, simPincode));
        if (pin > 9999) {
            throw new ProtocolException("Pin code should be between 0000 and 9999.");
        }
        Unsigned16 value = new Unsigned16(pin);
        getCosemObjectFactory().getGPRSModemSetup().writePinCode(value);
    }

    private void changeValveEnablePassword(OfflineDeviceMessage pendingMessage) throws IOException {
        int pin = Integer.parseInt(getDeviceMessageAttributeValue(pendingMessage, contactorValveEnablePassword));
        if (pin > 9999) {
            throw new ProtocolException("Pin code should be between 0000 and 9999.");
        }
        Unsigned16 value = new Unsigned16(pin);
        byte[] bytes = value.getBEREncodedByteArray();
        getCosemObjectFactory().getGenericWrite(VALVE_ENABLE_PASSWORD, 2, 1).write(bytes);
    }

    private void closeValve(OfflineDeviceMessage pendingMessage) throws IOException {
        setValve(pendingMessage, new Unsigned16(1)); // method '1' is the 'Remote closure activation' method
    }

    private void openValve(OfflineDeviceMessage pendingMessage) throws IOException {
        setValve(pendingMessage, new Unsigned16(2)); // method '2' is the 'Remote opening activation' method
    }

    private void closeValveAndInvoicing(OfflineDeviceMessage pendingMessage) throws IOException {
        setValve(pendingMessage, new Unsigned16(3)); // method '3' is the 'Remote closure activation, with simultaneous closure of the invoicing period' method
    }

    private void setValve(OfflineDeviceMessage pendingMessage, Unsigned16 scriptNr) throws IOException {
        String epochTime = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.contactorActivationDateAttributeName).getValue();
        Calendar epochTimeCal = Calendar.getInstance(getProtocol().getTimeZone());
        epochTimeCal.setTimeInMillis(Long.valueOf(epochTime));
        Long epoch = epochTimeCal.getTimeInMillis();

        Structure scriptStructure = new Structure();
        scriptStructure.addDataType(OctetString.fromObisCode(DISCONNECT_CONTROL_SCRIPT_TABLE));
        scriptStructure.addDataType(scriptNr);

        AXDRDateTime dateTime = new AXDRDateTime(epoch);
        Structure actvationDateStructure = new Structure();
        actvationDateStructure.addDataType(dateTime.getCosemTime().getOctetString());
        actvationDateStructure.addDataType(dateTime.getCosemDate().getOctetString());
        Array actvationTimeArray = new Array();
        actvationTimeArray.addDataType(actvationDateStructure);

        SingleActionSchedule actionSchedule = getCosemObjectFactory().getSingleActionSchedule(DISCONNECT_CONTROL_SINGLE_ACTION_SCHEDULE);
        actionSchedule.writeExecutedScript(scriptStructure);
        actionSchedule.writeExecutionTime(actvationTimeArray);
    }

    private void upgradeFirmware(OfflineDeviceMessage pendingMessage) throws IOException {
        String path = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.firmwareUpdateFileAttributeName);
        String activationEpochString = getDeviceMessageAttributeValue(pendingMessage, firmwareUpdateActivationDateAttributeName);
        String hash = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.firmwareUpdateHashAttributeName);
        String kdl = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.firmwareUpdateKDLAttributeName);
        String type = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.firmwareUpdateImageTypeAttributeName);

        byte[] imageData = TempFileLoader.loadTempFile(path);
        byte[] typeBytes = FirmwareImageType.typeForDescription(type).getByteArray();
        byte[] hashBytes = ProtocolTools.getBytesFromHexString(hash, 2);
        byte[] kdlBytes = ProtocolTools.getBytesFromHexString(kdl, 2);
        byte[] dateBytes = new AXDRDateTime(Long.parseLong(activationEpochString), getProtocol().getTimeZone()).getCosemDate().toBytes();
        byte[] initializationBytes = ProtocolTools.concatByteArrays(kdlBytes, hashBytes, dateBytes, typeBytes);
        String imageIdentifier = new String(initializationBytes, StandardCharsets.ISO_8859_1);

        ImageTransfer imageTransfer = getCosemObjectFactory().getImageTransfer();
        imageTransfer.setCharSet(StandardCharsets.ISO_8859_1);
        imageTransfer.setCheckNumberOfBlocksInPreviousSession(false);
        imageTransfer.setTransferBlocks(true);
        List<ImageTransfer.ImageToActivateInfo> imageToActivateInfos = null;
        String imageIdentifierInDevice = "";
        try {
            imageToActivateInfos = imageTransfer.readImageToActivateInfo();
            imageIdentifierInDevice = imageToActivateInfos.get(0).getImageIdentifier();
        } catch (DataAccessResultException e) {
            // swallow, this happens when a device has never been upgraded
        }
        int lastTransferredBlockNumber = imageTransfer.readFirstNotTransferedBlockNumber().intValue();
        if (lastTransferredBlockNumber > 0 && imageIdentifier.equalsIgnoreCase(imageIdentifierInDevice)) {
            imageTransfer.setStartIndex(lastTransferredBlockNumber - 1);
        }
        ImageTransfer.ImageBlockSupplier dataSupplier = new ImageTransfer.ByteArrayImageBlockSupplier(imageData);
        imageTransfer.enableImageTransfer(dataSupplier, imageIdentifier);
        imageTransfer.initializeAndTransferBlocks(dataSupplier, false, imageIdentifier);
        if (imageTransfer.getImageTransferStatus().getValue() == 1) {
            imageTransfer.checkAndSendMissingBlocks();
        }
//        The device will start verification and activation wil be done on the date that is specified in the imageIdentifier.
    }

    private void changeSessionTimeout(OfflineDeviceMessage pendingMessage) throws IOException {
        String value = getDeviceMessageAttributeValue(pendingMessage, sessionTimeoutAttributeName);
        AutoConnect autoConnect = getCosemObjectFactory().getAutoConnect();
        autoConnect.writeCosemSessionTimeout(Integer.parseInt(value));
    }

    private void changeInactivityTimeout(OfflineDeviceMessage pendingMessage) throws IOException {
        String value = getDeviceMessageAttributeValue(pendingMessage, inactivityTimeoutAttributeName);
        getCosemObjectFactory().getTCPUDPSetup().writeInactivityTimeout(Integer.parseInt(value));
    }

    private void configureAutoConnect(OfflineDeviceMessage pendingMessage) throws IOException {
        String mode = getDeviceMessageAttributeValue(pendingMessage, autoConnectMode);
        String repetitions = getDeviceMessageAttributeValue(pendingMessage, autoConnectRepetitions);
        String repetitionsDelay = getDeviceMessageAttributeValue(pendingMessage, autoConnectRepetitionsDelay);
        String window = getDeviceMessageAttributeValue(pendingMessage, windowAttributeName);
        String dayMap = getDeviceMessageAttributeValue(pendingMessage, autoConnectDayMap);
        String destination = getDeviceMessageAttributeValue(pendingMessage, autoConnectDestionation1);
        String port = getDeviceMessageAttributeValue(pendingMessage, portNumberAttributeName);
        String gsmRegistrationTimeout = getDeviceMessageAttributeValue(pendingMessage, autoConnectGSMRegistrationTimeout);
        String cosemSessinTimeout = getDeviceMessageAttributeValue(pendingMessage, autoConnectCosemSessionRegistrationTimeout);

        AutoConnect autoConnect = getCosemObjectFactory().getAutoConnect();
        autoConnect.writeMode(NetworkConnectivityMessage.AutoConnectMode.modeForDescription(mode).getMode());
        autoConnect.writeRepetitions(Integer.parseInt(repetitions));
        autoConnect.writeRepetitionDelay(Integer.parseInt(repetitionsDelay));
        Array windowList = new Array();
        windowList.addDataType(OctetString.fromString(window));
        autoConnect.writeCallingWindow(new Array(windowList));
        writeIpAddressAndPort(destination, port, autoConnect);
        autoConnect.writeDayMap(Long.parseLong(dayMap));
        autoConnect.writeGSMRegistrationTimeout(Integer.parseInt(gsmRegistrationTimeout));
        autoConnect.writeCosemSessionTimeout(Integer.parseInt(cosemSessinTimeout));
    }

    private void writeIpAddressAndPort(String destination, String port, AutoConnect autoConnect) throws IOException {
        ProtocolUtils.validateIpAddress(destination);
        ProtocolUtils.validatePortNumber(port);
        Array destinationList = new Array();
        String ipAddressAndPort = String.join(":", destination, port);
        byte[] bytes = ipAddressAndPort.getBytes(StandardCharsets.ISO_8859_1);
        OctetString octetString = OctetString.fromByteArray(bytes);
        destinationList.addDataType(octetString);
        autoConnect.writeDestinationList(destinationList);
    }

    private void changeGPRSandAPNCredentials(OfflineDeviceMessage pendingMessage) throws IOException {
        String userName = getDeviceMessageAttributeValue(pendingMessage, usernameAttributeName);
        String password = getDeviceMessageAttributeValue(pendingMessage, passwordAttributeName);
        String apn = getDeviceMessageAttributeValue(pendingMessage, apnAttributeName);
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
        if (apn != null) {
            getCosemObjectFactory().getGPRSModemSetup().writeAPN(apn);
        }
    }

    private void changeGprsIpAddressAndPort(OfflineDeviceMessage pendingMessage) throws IOException {
        String ipAddress = getDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.ipAddressAttributeName);
        String tcpPort = getDeviceMessageAttributeValue(pendingMessage, portNumberAttributeName);
        writeIpAddressAndPort(ipAddress, tcpPort, getCosemObjectFactory().getAutoConnect());
    }

    private void synchronizeTime() {
        Date date = new Date();
        getProtocol().setTime(date, DeviceClockSupport.ClockChangeMode.SYNC);
    }

    private void setTimezone(OfflineDeviceMessage pendingMessage) throws IOException {
        String value = getDeviceMessageAttributeValue(pendingMessage, TimeZoneOffsetInHoursAttributeName);
        if (value != null) {
            int intValue = Integer.parseInt(value) * 60; // hours to minutes
            getProtocol().getDlmsSession().getCosemObjectFactory().getClock().setTimeZone(intValue);
        }
        getProtocol().getLogger().fine("Timezone set to " + value);
    }

    private void configureDST(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enable = Boolean.parseBoolean(getDeviceMessageAttributeValue(pendingMessage, enableDSTAttributeName));
        Date startDateDST = new Date(Long.valueOf(getDeviceMessageAttributeValue(pendingMessage, StartOfDSTAttributeName)));
        Date endDateDST = new Date(Long.valueOf(getDeviceMessageAttributeValue(pendingMessage, EndOfDSTAttributeName)));
        Clock clock = getProtocol().getDlmsSession().getCosemObjectFactory().getClock();
        clock.setDsDateTimeBegin(convertDateToBEREncodedByteArray(startDateDST));
        clock.setDsDateTimeEnd(convertDateToBEREncodedByteArray(endDateDST));
        clock.enableDisableDs(enable);
    }

    private byte[] convertDateToBEREncodedByteArray(Date date) {
        return new AXDRDateTime(date, getProtocol().getTimeZone()).getBEREncodedByteArray();
    }

}
