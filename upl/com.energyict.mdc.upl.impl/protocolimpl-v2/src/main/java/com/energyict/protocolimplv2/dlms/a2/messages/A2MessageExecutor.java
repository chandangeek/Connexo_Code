/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.a2.messages;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.tasks.support.DeviceClockSupport;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.AssociationLN;
import com.energyict.dlms.cosem.AutoConnect;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.PPPSetup;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimpl.utils.TempFileLoader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.a2.A2;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareImageType;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.EndOfDSTAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.StartOfDSTAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.TimeZoneOffsetInHoursAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.apnAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.autoConnectCosemSessionRegistrationTimeout;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.autoConnectDayMap;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.autoConnectDestionation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.autoConnectGSMRegistrationTimeout;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.autoConnectMode;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.autoConnectRepetitions;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.autoConnectRepetitionsDelay;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.clientMacAddress;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.communicationWindowStartTime1;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.communicationWindowStartTime2;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.communicationWindowStartTime3;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.communicationWindowStartTime4;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.communicationWindowStopTime1;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.communicationWindowStopTime2;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.communicationWindowStopTime3;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.communicationWindowStopTime4;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorValveEnablePassword;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.enableDSTAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.inactivityTimeoutAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newPasswordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.passwordAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.portNumberAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.sessionTimeoutAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.simPincode;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.usernameAttributeName;


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
        } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.CHANGE_NBIOT_APN_CREDENTIALS)) {
            changeNBIOTandAPNCredentials(pendingMessage);
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
        byte[] imageIdentifier = ProtocolTools.concatByteArrays(kdlBytes, hashBytes, dateBytes, typeBytes);

        ImageTransfer imageTransfer = getCosemObjectFactory().getImageTransfer();
        imageTransfer.setCheckNumberOfBlocksInPreviousSession(false);
        imageTransfer.setTransferBlocks(true);
        List<Structure> imageToActivateInfos;
        byte[] imageIdentifierInDevice = null;
        try {
            imageToActivateInfos = imageTransfer.readImageToActivateInfoStructure();
            Structure structure = imageToActivateInfos.get(0);

            if (structure.nrOfDataTypes() == 3) {
                imageIdentifierInDevice = structure.getDataType(1).getOctetString().getOctetStr();
            } else {
                throw new ProtocolException("Could not parse Structure [" + structure + "] to a valid ImageToActivateInfo, was expecting 3 elements, but structure contains [" + structure.nrOfDataTypes() + "] elements!");
            }
        } catch (DataAccessResultException e) {
            // swallow, this happens when a device has never been upgraded
        }
        int lastTransferredBlockNumber = imageTransfer.readFirstNotTransferedBlockNumber().intValue();
        if (lastTransferredBlockNumber > 0 && Arrays.equals(imageIdentifier, imageIdentifierInDevice)) {
            imageTransfer.setStartIndex(lastTransferredBlockNumber - 1);
        } else {
            imageTransfer.setStartIndexOverride(true);
        }
        ImageTransfer.ImageBlockSupplier dataSupplier = new ImageTransfer.ByteArrayImageBlockSupplier(imageData);
        imageTransfer.enableImageTransfer();
        imageTransfer.initializeAndTransferBlocks(dataSupplier, false, imageIdentifier);
        if (imageTransfer.getImageTransferStatus().getValue() == 1) {
            imageTransfer.checkAndSendMissingBlocks();
        }
        // The device will start verification and activation wil be done on the date that is specified in the imageIdentifier.
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
        String startDate1 = getDeviceMessageOptionalAttributeValue(pendingMessage, communicationWindowStartTime1);
        //String startTime1 = getDeviceMessageOptionalAttributeValue(pendingMessage, communicationWindowStartTime1);
        String stopDate1 = getDeviceMessageOptionalAttributeValue(pendingMessage, communicationWindowStopTime1);
        //String stopTime1 = getDeviceMessageOptionalAttributeValue(pendingMessage, communicationWindowStopTime1);
        //String ignoreTimeFrame2 = getDeviceMessageAttributeValue(pendingMessage, communicationWindowStartDate2IsIgnored);
        String startDate2 = getDeviceMessageOptionalAttributeValue(pendingMessage, communicationWindowStartTime2);
        //String startTime2 = getDeviceMessageOptionalAttributeValue(pendingMessage, communicationWindowStartTime2);
        String stopDate2 = getDeviceMessageOptionalAttributeValue(pendingMessage, communicationWindowStopTime2);
        //String stopTime2 = getDeviceMessageOptionalAttributeValue(pendingMessage, communicationWindowStopTime2);
        //String ignoreTimeFrame3 = getDeviceMessageAttributeValue(pendingMessage, communicationWindowStartDate3IsIgnored);
        String startDate3 = getDeviceMessageOptionalAttributeValue(pendingMessage, communicationWindowStartTime3);
        //String startTime3 = getDeviceMessageOptionalAttributeValue(pendingMessage, communicationWindowStartTime3);
        String stopDate3 = getDeviceMessageOptionalAttributeValue(pendingMessage, communicationWindowStopTime3);
        //String stopTime3 = getDeviceMessageOptionalAttributeValue(pendingMessage, communicationWindowStopTime3);
        //String ignoreTimeFrame4 = getDeviceMessageAttributeValue(pendingMessage, communicationWindowStartDate4IsIgnored);
        String startDate4 = getDeviceMessageOptionalAttributeValue(pendingMessage, communicationWindowStartTime4);
        //String startTime4 = getDeviceMessageOptionalAttributeValue(pendingMessage, communicationWindowStartTime4);
        String stopDate4 = getDeviceMessageOptionalAttributeValue(pendingMessage, communicationWindowStopTime4);
        //String stopTime4 = getDeviceMessageOptionalAttributeValue(pendingMessage, communicationWindowStopTime4);
        String dayMap = getDeviceMessageAttributeValue(pendingMessage, autoConnectDayMap);
        String destination = getDeviceMessageAttributeValue(pendingMessage, autoConnectDestionation);
        String port = getDeviceMessageAttributeValue(pendingMessage, portNumberAttributeName);
        String gsmRegistrationTimeout = getDeviceMessageAttributeValue(pendingMessage, autoConnectGSMRegistrationTimeout);
        String cosemSessionTimeout = getDeviceMessageAttributeValue(pendingMessage, autoConnectCosemSessionRegistrationTimeout);

        AutoConnect autoConnect = getCosemObjectFactory().getAutoConnect();
        NetworkConnectivityMessage.AutoConnectModeA2 autoConnectMode = NetworkConnectivityMessage.AutoConnectModeA2.modeForDescription(mode);
        //autoConnect.writeMode(autoConnectMode.getMode());
       // autoConnect.writeRepetitions(Integer.parseInt(repetitions));
       // autoConnect.writeRepetitionDelay(Integer.parseInt(repetitionsDelay));
        Array windowList = new Array();
        if (!NetworkConnectivityMessage.AutoConnectModeA2.Inactive.equals(autoConnectMode)) {
            if (startDate1 != null && stopDate1 != null) {
                windowList.addDataType(getWindowFrame(startDate1, stopDate1, autoConnectMode));
            } else {
                getProtocol().journal(Level.SEVERE, "Calling window is absent. Please set the boundary dates.");
                throw new IOException("Calling window is absent. Please set the boundary dates.");
            }
            if (startDate2 != null && stopDate2 != null) {
                windowList.addDataType(getWindowFrame(startDate2, stopDate2, autoConnectMode));
            }
            if (startDate3 != null && stopDate3 != null) {
                windowList.addDataType(getWindowFrame(startDate3, stopDate3, autoConnectMode));
            }
            if (startDate4 != null && stopDate4 != null) {
                windowList.addDataType(getWindowFrame(startDate4, stopDate4, autoConnectMode));
            }
        }
        /*windowList.addDataType(getWindowFrame(startDate1, startTime1, stopDate1, stopTime1));
        if (!Boolean.getBoolean(ignoreTimeFrame2)) {
            windowList.addDataType(getWindowFrame(startDate2, startTime2, stopDate2, stopTime2));
        }
        if (!Boolean.getBoolean(ignoreTimeFrame3)) {
            windowList.addDataType(getWindowFrame(startDate3, startTime3, stopDate3, stopTime3));
        }
        if (!Boolean.getBoolean(ignoreTimeFrame4)) {
            windowList.addDataType(getWindowFrame(startDate4, startTime4, stopDate4, stopTime4));
        }*/
        autoConnect.writeCallingWindow(new Array(windowList));
        writeIpAddressAndPort(destination, port, autoConnect);
        autoConnect.writeDayMap(Long.parseLong(dayMap, 16));
        autoConnect.writeGSMRegistrationTimeout(Integer.parseInt(gsmRegistrationTimeout));
        autoConnect.writeCosemSessionTimeout(Integer.parseInt(cosemSessionTimeout));
    }

    private Structure getWindowFrame(String startDateString, String endDateString, NetworkConnectivityMessage.AutoConnectModeA2 modeA2) throws IOException {
        Structure windowFrameStructure = new Structure();
        Date startDate = new Date(Long.parseLong(startDateString));
        Date stopDate = new Date(Long.parseLong(endDateString));

        byte[] startDateBytes = convertDateToBEREncodedByteArray(startDate);
        byte[] stopDateBytes = convertDateToBEREncodedByteArray(stopDate);
        if (NetworkConnectivityMessage.AutoConnectModeA2.Active.equals(modeA2)) {
            windowFrameStructure.addDataType(new OctetString(startDateBytes, 0));
            windowFrameStructure.addDataType(new OctetString(stopDateBytes, 0));
        } else if (NetworkConnectivityMessage.AutoConnectModeA2.DailyActive.equals(modeA2)) {
            for (int i = 2; i < 7; i++) {
                startDateBytes[i] = (byte) 0xFF;
                stopDateBytes[i] = (byte) 0xFF;
            }
            windowFrameStructure.addDataType(new OctetString(startDateBytes, 0));
            windowFrameStructure.addDataType(new OctetString(stopDateBytes, 0));
        }
        return windowFrameStructure;
    }


    /*private Structure getWindowFrame(String startDateString, String startTimeString, String endDateString, String endTimeString) throws IOException {
        Structure windowFrameStructure = new Structure();
        windowFrameStructure.addDataType(new OctetString(getWindowFrame(startDateString, startTimeString), 0));
        windowFrameStructure.addDataType(new OctetString(getWindowFrame(endDateString, endTimeString), 0));
        return windowFrameStructure;
    }


    private byte[] getWindowFrame(String dateString, String timeString) throws IOException {
        Calendar startTimeDeviceTimezone = ProtocolUtils.getCleanCalendar(getProtocol().getTimeZone());
        OctetString startDateOctetString = null;
        OctetString startTimeOctetString = null;

        AXDRTime axdrTime = new AXDRTime();
        axdrTime.decode(timeString);

        if (dateString == null) {
            startDateOctetString = AXDRDate.encode(0xFFFF, 0xFF, 0xFF, 0xFF);
            Calendar startTimeUTC = ProtocolUtils.getCleanCalendar(TimeZone.getTimeZone("UTC"));
            startTimeUTC.setTimeInMillis(0); // 1970-01-01T00:00:00
            startTimeUTC.set(Calendar.HOUR_OF_DAY, axdrTime.getHour());
            startTimeUTC.set(Calendar.MINUTE, axdrTime.getMinutes());
            startTimeUTC.set(Calendar.SECOND, axdrTime.getSeconds());
            startTimeDeviceTimezone.setTimeInMillis(startTimeUTC.getTimeInMillis());
            AXDRTime axdrTimeDeviceTimezone = new AXDRTime();
            axdrTimeDeviceTimezone.decode(startTimeDeviceTimezone.get(Calendar.HOUR_OF_DAY) + ":" + startTimeDeviceTimezone.get(Calendar.MINUTE) + ":" + startTimeDeviceTimezone.get(Calendar.SECOND));
            startTimeOctetString = axdrTimeDeviceTimezone.getOctetString();

        } else {
            Calendar startDateUTC = ProtocolUtils.getCleanCalendar(TimeZone.getTimeZone("UTC"));
            startDateUTC.setTimeInMillis(Long.parseLong(dateString));
            int hourInUtc = startDateUTC.get(Calendar.HOUR_OF_DAY);
            int timezoneShift = 0;
            int shitfDate = 0;

            if (hourInUtc < 12) {
                // Original timezone shift
                timezoneShift = - hourInUtc;

                if (axdrTime.getHour() + timezoneShift < 0) {

                }
            }
            if (hourInUtc > 12) {
                timezoneShift = 24 - hourInUtc;
                if (axdrTime.getHour() + timezoneShift > 24) {
                    shitfDate++;
                }
            }


            startDateUTC.set(Calendar.HOUR_OF_DAY, axdrTime.getHour());
            startDateUTC.set(Calendar.MINUTE, axdrTime.getMinutes());
            startDateUTC.set(Calendar.SECOND, axdrTime.getSeconds());

            startTimeDeviceTimezone.setTimeInMillis(startDateUTC.getTimeInMillis());
            startDateOctetString = AXDRDate.encode(startTimeDeviceTimezone.get(Calendar.YEAR), startTimeDeviceTimezone.get(Calendar.MONTH), startTimeDeviceTimezone.get(Calendar.DAY_OF_MONTH), startTimeDeviceTimezone.get(Calendar.DAY_OF_WEEK));
            AXDRTime axdrTimeDeviceTimezone = new AXDRTime();
            axdrTimeDeviceTimezone.decode(startTimeDeviceTimezone.get(Calendar.HOUR_OF_DAY) + ":" + startTimeDeviceTimezone.get(Calendar.MINUTE) + ":" + startTimeDeviceTimezone.get(Calendar.SECOND));
            startTimeOctetString = axdrTimeDeviceTimezone.getOctetString();
        }

        byte[] startDateOctetStringBytes = startDateOctetString.toByteArray();
        byte[] startTimeOctetStringBytes = startTimeOctetString.toByteArray();
        int headerSize = 2;
        byte[] startDateTimeBytes = new byte[headerSize + startDateOctetStringBytes.length + startTimeOctetStringBytes.length + 3];
        startDateTimeBytes[0] = AxdrType.OCTET_STRING.getTag();
        startDateTimeBytes[1] = 0xC; // size
        System.arraycopy(startDateOctetStringBytes, 0, startDateTimeBytes, headerSize, startDateOctetStringBytes.length);
        System.arraycopy(startTimeOctetStringBytes,0, startDateTimeBytes, headerSize + startDateOctetStringBytes.length, startTimeOctetStringBytes.length);
        int deviationUnspecified = 0x8000;
        startDateTimeBytes[11] = (byte) ((deviationUnspecified>>8)&0xFF);
        startDateTimeBytes[12] = (byte) (deviationUnspecified&0xFF);
        startDateTimeBytes[13] = (byte) 0; //status (DST)

        return startDateTimeBytes;
    }*/

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

    private void changeNBIOTandAPNCredentials(OfflineDeviceMessage pendingMessage) throws IOException {
        String userName = getDeviceMessageAttributeValue(pendingMessage, usernameAttributeName);
        String password = getDeviceMessageAttributeValue(pendingMessage, passwordAttributeName);
        String apn = getDeviceMessageAttributeValue(pendingMessage, apnAttributeName);
        PPPSetup.PPPAuthenticationType pppat = getCosemObjectFactory().getPPPSetup(ObisCode.fromString("0.1.25.3.0.255")).new PPPAuthenticationType();
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
            getCosemObjectFactory().getNbiotModemSetup().writeAPN(apn);
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        Date startDateDST = Date.from(Instant.from(formatter.parse(getDeviceMessageAttributeValue(pendingMessage, StartOfDSTAttributeName))));
        Date endDateDST = Date.from(Instant.from(formatter.parse(getDeviceMessageAttributeValue(pendingMessage, EndOfDSTAttributeName))));
        Clock clock = getProtocol().getDlmsSession().getCosemObjectFactory().getClock();
        clock.setDsDateTimeBegin(convertDateToBEREncodedByteArray(startDateDST));
        clock.setDsDateTimeEnd(convertDateToBEREncodedByteArray(endDateDST));
        clock.enableDisableDs(enable);
    }

    private byte[] convertDateToBEREncodedByteArray(Date date) {
        return new AXDRDateTime(date, getProtocol().getTimeZone()).getBEREncodedByteArray();
    }

    protected String getDeviceMessageOptionalAttributeValue(OfflineDeviceMessage offlineDeviceMessage, String attributeName) throws ProtocolException {
        for (OfflineDeviceMessageAttribute offlineDeviceMessageAttribute : offlineDeviceMessage.getDeviceMessageAttributes()) {
            if (offlineDeviceMessageAttribute.getName().equals(attributeName)) {
                return offlineDeviceMessageAttribute.getValue();
            }
        }
        return null;
    }

}