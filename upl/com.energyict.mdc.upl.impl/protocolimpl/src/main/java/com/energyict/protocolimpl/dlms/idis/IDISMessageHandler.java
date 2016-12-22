package com.energyict.protocolimpl.dlms.idis;

import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageAttributeSpec;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageElement;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.GenericInvoke;
import com.energyict.dlms.cosem.GenericWrite;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.Limiter;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.RegisterMonitor;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController;
import com.energyict.protocolimpl.dlms.idis.xml.XMLParser;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 20/09/11
 * Time: 17:22
 */
public class IDISMessageHandler extends GenericMessaging implements MessageProtocol {

    private static final ObisCode RELAY_CONTROL_OBISCODE = ObisCode.fromString("0.0.96.3.10.255");
    private static final ObisCode TIMED_CONNECTOR_ACTION_OBISCODE = ObisCode.fromString("0.0.15.0.1.255");
    private static final ObisCode DISCONNECTOR_SCRIPT_OBISCODE = ObisCode.fromString("0.0.10.0.106.255");
    private static final ObisCode ERROR_BITS_OBISCODE = ObisCode.fromString("0.0.97.97.0.255");
    private static final ObisCode ALARM_BITS_OBISCODE = ObisCode.fromString("0.0.97.98.0.255");
    private static final ObisCode ALARM_FILTER_OBISCODE = ObisCode.fromString("0.0.97.98.10.255");
    protected static final ObisCode MBUS_CLIENT_OBISCODE = ObisCode.fromString("0.1.24.1.0.255");
    private static final String NORESUME = "noresume";

    public static final String RAW_CONTENT = "RawContent";
    public static final String INCLUDED_USERFILE_TAG = "IncludedFile";
    private static final String TIMED_DISCONNECT = "TimedDisconnect";
    private static final String TIMED_RECONNECT = "TimedReconnect";
    private static final String TIMEZONE = "TimeZone";
    private static final String DATE_DD_MM_YYYY_HH_MM = "Date (dd/mm/yyyy hh:mm)";
    private static final String CONFIGURATION_DOWNLOAD = "Configuration download";
    private static final String CONFIGURATION_USER_FILE = "Configuration user file";
    protected IDIS idis;

    public IDISMessageHandler(IDIS idis) {
        this.idis = idis;
    }

    public void applyMessages(List messageEntries) throws IOException {
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        try {
            if (messageEntry.getContent() != null) {
                if (messageEntry.getContent().contains("<RemoteDisconnect")) {
                    return remoteDisconnect(messageEntry);
                } else if (messageEntry.getContent().contains("<RemoteConnect")) {
                    return remoteConnect(messageEntry);
                } else if (messageEntry.getContent().contains("<SetControlMode")) {
                    return setControlMode(messageEntry);
                } else if (messageEntry.getContent().contains("<OpenRelay")) {
                    return openRelay(messageEntry);
                } else if (messageEntry.getContent().contains("<CloseRelay")) {
                    return closeRelay(messageEntry);
                } else if (messageEntry.getContent().contains("<ResetAllAlarmBits")) {
                    return resetAllAlarmBits(messageEntry);
                } else if (messageEntry.getContent().contains("<ResetAllErrorBits")) {
                    return resetAllErrorBits(messageEntry);
                } else if (messageEntry.getContent().contains("<ForceTime")) {
                    return forceTime(messageEntry);
                } else if (messageEntry.getContent().contains("<SetTimeOutNotAddressed")) {
                    return setTimeoutNotAddressed(messageEntry);
                } else if (messageEntry.getContent().contains("<SlaveCommission")) {
                    return commission(messageEntry);
                } else if (messageEntry.getContent().contains("<WriteAlarmFilter")) {
                    return writeAlarmFilter(messageEntry);
                } else if (messageEntry.getContent().contains("<" + TIMED_RECONNECT)) {
                    return timedAction(messageEntry, 2);
                } else if (messageEntry.getContent().contains("<" + TIMED_DISCONNECT)) {
                    return timedAction(messageEntry, 1);
                } else if (messageEntry.getContent().contains("<FirmwareUpdate")) {
                    return firmwareUpgrade(messageEntry);
                } else if (messageEntry.getContent().contains("<LoadControlledConnect")) {
                    return loadControlledConnect(messageEntry);
                } else if (messageEntry.getContent().contains("<ConfigureLoadProfile1CapturedObjects") || messageEntry.getContent().contains("<ConfigureLoadProfile2CapturedObjects")) {
                    return writeLoadProfileCapturedObjects(messageEntry);
                } else if (messageEntry.getContent().contains("<ConfigureBillingLoadProfileCapturedObjects")) {
                    return writeBillingLoadProfileCapturedObjects(messageEntry);
                } else if (messageEntry.getContent().contains("<WriteLP1CapturePeriod")) {
                    return writeCapturePeriod(messageEntry, 1);
                } else if (messageEntry.getContent().contains("<WriteLP2CapturePeriod")) {
                    return writeCapturePeriod(messageEntry, 2);
                } else if (messageEntry.getContent().contains("<SuperVision")) {
                    return superVision(messageEntry);
                } else if (messageEntry.getContent().contains(RtuMessageConstant.TOU_ACTIVITY_CAL)) {
                    return writeActivityCalendar(messageEntry);
                } else if (messageEntry.getContent().contains(RtuMessageConstant.TOU_SPECIAL_DAYS)) {
                    return writeSpecialDays(messageEntry);
                } else if (messageEntry.getContent().contains("<" + CONFIGURATION_DOWNLOAD)) {
                    return configurationDownload(messageEntry);
                }
            } else {
                idis.getLogger().log(Level.SEVERE, "Error executing message - the message content is empty, probably wrong user file id specified.");
                return MessageResult.createFailed(messageEntry);
            }
        } catch (DataAccessResultException e) {
            idis.getLogger().log(Level.SEVERE, "Error executing message: " + e.getMessage());
            return MessageResult.createFailed(messageEntry, e.getMessage());
        } catch (NumberFormatException e) {
            idis.getLogger().log(Level.SEVERE, "Error executing message: " + e.getMessage());
            return MessageResult.createFailed(messageEntry, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
        idis.getLogger().log(Level.SEVERE, "Unexpected message: " + messageEntry.getContent());
        return MessageResult.createFailed(messageEntry);
    }

    private MessageResult commission(MessageEntry messageEntry) throws IOException {
        for (int channel = 1; channel < 5; channel++) {                     //Check the available 4 channels, install the slave meter on a free channel client.
            ObisCode obisCode = ProtocolTools.setObisCodeField(MBUS_CLIENT_OBISCODE, 1, (byte) channel);   //Find the right MBus client object
            MBusClient mbusClient = idis.getCosemObjectFactory().getMbusClient(obisCode, MbusClientAttributes.VERSION10);
            if (mbusClient.getIdentificationNumber().getValue() == 0) {     //Find a free channel client
                mbusClient.invoke(1, new Unsigned8(0).getBEREncodedByteArray());
                idis.getLogger().log(Level.INFO, "MBus slave was commissioned on channel " + channel);
                return MessageResult.createSuccess(messageEntry);
            }
        }
        idis.getLogger().log(Level.INFO, "Couldn't commission the new MBus meter, no free channels available.");
        return MessageResult.createFailed(messageEntry);
    }

    private MessageResult writeActivityCalendar(MessageEntry messageEntry) throws IOException {
        try {
            ActivityCalendarController activityCalendarController = new DLMSActivityCalendarController(idis.getCosemObjectFactory(), idis.getTimeZone());
            activityCalendarController.parseContent(messageEntry.getContent());
            activityCalendarController.writeCalendarName("");
            activityCalendarController.writeCalendar();
            idis.getLogger().log(Level.INFO, "Activity calendar was successfully written");
            return MessageResult.createSuccess(messageEntry);
        } catch (DataAccessResultException e) {
            idis.getLogger().severe("Writing of the activity calendar failed: " + e.getMessage());
            return MessageResult.createFailed(messageEntry, e.getMessage());   //Meter did not accept the specified code table, set message to failed
        }
    }

    private MessageResult writeSpecialDays(MessageEntry messageEntry) throws IOException {
        try {
            ActivityCalendarController activityCalendarController = new DLMSActivityCalendarController(idis.getCosemObjectFactory(), idis.getTimeZone());
            activityCalendarController.parseContent(messageEntry.getContent());
            activityCalendarController.writeSpecialDaysTable();
            idis.getLogger().log(Level.INFO, "Special days were successfully written");
            return MessageResult.createSuccess(messageEntry);
        } catch (DataAccessResultException e) {
            idis.getLogger().severe("Writing of the special days table failed: " + e.getMessage());
            return MessageResult.createFailed(messageEntry, e.getMessage());   //Meter did not accept the specified code table, set message to failed
        }
    }

    protected MessageResult remoteDisconnect(MessageEntry messageEntry) throws IOException {
        idis.getCosemObjectFactory().getDisconnector().remoteDisconnect();
        idis.getLogger().log(Level.INFO, "Remote disconnect message was successful");
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult forceTime(MessageEntry messageEntry) throws IOException {
        idis.setTime();
        idis.getLogger().log(Level.INFO, "Force clock was successful");
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult writeCapturePeriod(MessageEntry messageEntry, int dField) throws IOException {
        ObisCode lpObisCode = ObisCode.fromString("1.0.99.1.0.255");
        lpObisCode = ProtocolTools.setObisCodeField(lpObisCode, 3, (byte) dField);  //1 or 2 in D-field, selects the LP
        String[] parts = messageEntry.getContent().split("=");
        int interval = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        idis.getCosemObjectFactory().getProfileGeneric(lpObisCode).setCapturePeriodAttr(new Unsigned32(interval));
        idis.getLogger().log(Level.INFO, "Successfully set LP interval to " + interval);
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult setTimeoutNotAddressed(MessageEntry messageEntry) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        int timeout = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        idis.getCosemObjectFactory().getSFSKSyncTimeouts().setTimeoutNotAddressed(timeout);
        idis.getLogger().log(Level.INFO, "Successfully set 'timeout_not_addressed' attribute to " + String.valueOf(timeout));
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult remoteConnect(MessageEntry messageEntry) throws IOException {
        idis.getCosemObjectFactory().getDisconnector().remoteReconnect();
        idis.getLogger().log(Level.INFO, "Remote connect message was successful");
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult setControlMode(MessageEntry messageEntry) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        int controlMode = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        idis.getCosemObjectFactory().getDisconnector().writeControlMode(new TypeEnum(controlMode));
        idis.getLogger().log(Level.INFO, "Successfully set control_mode to " + controlMode);
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult openRelay(MessageEntry messageEntry) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        int relayControlNumber = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        ObisCode obisCode = RELAY_CONTROL_OBISCODE;
        obisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) relayControlNumber);
        idis.getCosemObjectFactory().getDisconnector(obisCode).remoteDisconnect();
        idis.getLogger().log(Level.INFO, "Open relay message was successful");
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult closeRelay(MessageEntry messageEntry) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        int relayControlNumber = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        ObisCode obisCode = RELAY_CONTROL_OBISCODE;
        obisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) relayControlNumber);
        idis.getCosemObjectFactory().getDisconnector(obisCode).remoteReconnect();
        idis.getLogger().log(Level.INFO, "Close relay message was successful");
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult resetAllAlarmBits(MessageEntry messageEntry) throws IOException {
        long alarmBits = idis.readRegister(ALARM_BITS_OBISCODE).getQuantity().getAmount().longValue();
        Data data = idis.getCosemObjectFactory().getData(ALARM_BITS_OBISCODE);
        data.setValueAttr(new Unsigned32(alarmBits));

        idis.getLogger().log(Level.INFO, "Alarm bits are successfully cleared");
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult resetAllErrorBits(MessageEntry messageEntry) throws IOException {
        Data data = idis.getCosemObjectFactory().getData(ERROR_BITS_OBISCODE);
        long errorBits = data.getValueAttr().longValue();
        data.setValueAttr(new Unsigned32(errorBits));

        idis.getLogger().log(Level.INFO, "Error bits are successfully cleared");
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult writeAlarmFilter(MessageEntry messageEntry) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        long filterValue = Integer.parseInt(parts[1].substring(1).split("\"")[0]);

        Data data = idis.getCosemObjectFactory().getData(ALARM_FILTER_OBISCODE);
        data.setValueAttr(new Unsigned32(filterValue));
        idis.getLogger().log(Level.INFO, "Alarm filter is successfully written");
        return MessageResult.createSuccess(messageEntry);
    }

    private String getIncludedContent(final String content) {
        int begin = content.indexOf(INCLUDED_USERFILE_TAG) + INCLUDED_USERFILE_TAG.length() + 1;
        int end = content.indexOf(INCLUDED_USERFILE_TAG, begin) - 2;
        return content.substring(begin, end);
    }

    protected MessageResult firmwareUpgrade(MessageEntry messageEntry) throws IOException, InterruptedException {
        boolean resume = true;
        String trackingId = messageEntry.getTrackingId();
        if ((trackingId != null) && trackingId.toLowerCase().contains(NORESUME)) {
            resume = false;
        }

        String imageData = getIncludedContent(messageEntry.getContent());
        Base64EncoderDecoder decoder = new Base64EncoderDecoder();
        byte[] binaryImage = decoder.decode(imageData);
        String firmwareIdentifier;
        int length = binaryImage[0];
        firmwareIdentifier = new String(ProtocolTools.getSubArray(binaryImage, 1, 1 + length));   //The image_identifier is included in the header of the bin file

        ImageTransfer imageTransfer = idis.getCosemObjectFactory().getImageTransfer();
        if (resume) {
            int lastTransferredBlockNumber = imageTransfer.readFirstNotTransferedBlockNumber().intValue();
            if (lastTransferredBlockNumber > 0) {
                imageTransfer.setStartIndex(lastTransferredBlockNumber - 1);
            }
        }

        imageTransfer.upgrade(binaryImage, false, firmwareIdentifier, false);
        imageTransfer.imageActivation();
        idis.getLogger().log(Level.INFO, "Firmware upgrade message was successful");
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult configurationDownload(MessageEntry messageEntry) {
        try {
            idis.getLogger().log(Level.INFO, "Configuration download message received.");
            String xmlData = getIncludedContent(messageEntry.getContent());
            XMLParser parser = new XMLParser(idis.getLogger(), idis.getCosemObjectFactory());

            parser.parseXML(xmlData);
            List<Object[]> parsedObjects = parser.getParsedObjects();

            boolean encounteredError = false;
            idis.getLogger().info("Transferring the objects to the device.");
            for (Object[] each : parsedObjects) {
                AbstractDataType value = (AbstractDataType) each[1];
                if (each[0].getClass().equals(GenericWrite.class)) {
                    GenericWrite genericWrite = (GenericWrite) each[0];
                    try {
                        genericWrite.write(value.getBEREncodedByteArray());
                    } catch (DataAccessResultException e) {
                        encounteredError = true;
                        idis.getLogger().severe("ERROR: Failed to write DLMS object " + genericWrite.getObjectReference() + " , attribute " + genericWrite.getAttr() + " : " + e);
                    }
                } else if (each[0].getClass().equals(GenericInvoke.class)) {
                    GenericInvoke genericInvoke = (GenericInvoke) each[0];
                    try {
                        genericInvoke.invoke(value.getBEREncodedByteArray());
                    } catch (DataAccessResultException e) {
                        encounteredError = true;
                        idis.getLogger().severe("ERROR: Failed to execute action on DLMS object " + genericInvoke.getObjectReference() + " , method " + genericInvoke.getMethod() + " : " + e);
                    }
                }
            }

            idis.getLogger().log(Level.INFO, "Configuration download message finished.");
            return encounteredError ? MessageResult.createFailed(messageEntry) : MessageResult.createSuccess(messageEntry);
        } catch (IOException e) {
            idis.getLogger().log(Level.SEVERE, "Configuration download message failed: " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
    }

    protected MessageResult timedAction(MessageEntry messageEntry, int action) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        String dateString = parts[1].substring(1).split("\"")[0];
        String timeZoneID = parts[2].substring(1).split("\"")[0];
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneID);
        formatter.setTimeZone(timeZone == null ? TimeZone.getDefault() : timeZone);
        Date date;
        try {
            date = formatter.parse(dateString);
        } catch (ParseException e) {
            idis.getLogger().log(Level.SEVERE, "Error parsing the given date: " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
        SingleActionSchedule singleActionSchedule = idis.getCosemObjectFactory().getSingleActionSchedule(TIMED_CONNECTOR_ACTION_OBISCODE);

        Structure scriptStruct = new Structure();
        scriptStruct.addDataType(new OctetString(DISCONNECTOR_SCRIPT_OBISCODE.getLN()));
        scriptStruct.addDataType(new Unsigned16(action));     // 1 = disconnect, 2 = connect

        singleActionSchedule.writeExecutedScript(scriptStruct);
        singleActionSchedule.writeExecutionTime(convertDateToDLMSArray(date));
        idis.getLogger().log(Level.INFO, "Timed dis/connect message was successful");
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult loadControlledConnect(MessageEntry messageEntry) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        int monitoredValue = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        int normalThreshold = Integer.parseInt(parts[2].substring(1).split("\"")[0]);
        int emergencyThreshold = Integer.parseInt(parts[3].substring(1).split("\"")[0]);
        int overDuration = Integer.parseInt(parts[4].substring(1).split("\"")[0]);
        int underDuration = Integer.parseInt(parts[5].substring(1).split("\"")[0]);
        int emergencyProfileId = Integer.parseInt(parts[6].substring(1).split("\"")[0]);
        String emergencyActivationTime = parts[7].substring(1).split("\"")[0];

        Date date;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            date = formatter.parse(emergencyActivationTime);
        } catch (ParseException e) {
            idis.getLogger().log(Level.SEVERE, "Error parsing the given date: " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }

        int emergencyDuration = Integer.parseInt(parts[8].substring(1).split("\"")[0]);
        String[] profile_id_list = parts[9].substring(1).split("\"")[0].split(",");
        int actionUnderThreshold = Integer.parseInt(parts[10].substring(1).split("\"")[0]);

        Limiter limiter = idis.getCosemObjectFactory().getLimiter();
        setMonitoredValue(limiter, monitoredValue);
        writeNormalThreshold(monitoredValue, normalThreshold, limiter);
        writeEmergencyThreshold(monitoredValue, emergencyThreshold, limiter);
        limiter.writeMinOverThresholdDuration(new Unsigned32(overDuration));
        limiter.writeMinUnderThresholdDuration(new Unsigned32(underDuration));
        writeEmergencyProfile(emergencyProfileId, date, emergencyDuration, limiter);

        Array groupIdList = new Array();
        for (String id : profile_id_list) {
            try {
                groupIdList.addDataType(new Unsigned16(Integer.parseInt(id)));
            } catch (NumberFormatException e) {
                idis.getLogger().log(Level.INFO, "Error parsing the profile id list: " + e.getMessage());
                return MessageResult.createFailed(messageEntry);
            }
        }
        limiter.writeEmergencyProfileGroupIdList(groupIdList);

        writeActions(actionUnderThreshold, limiter);

        idis.getLogger().log(Level.INFO, "Load controlled connect message was successful");
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult writeLoadProfileCapturedObjects(MessageEntry messageEntry) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        String loadProfileObisCode = parts[1].substring(1).split("\"")[0];
        List<String> capturedObjectDefinitions = new ArrayList<>();
        int index = 2;
        while (true) {
            try {
                capturedObjectDefinitions.add(parts[index].substring(1).split("\"")[0]);
                index++;
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }
        ProfileGeneric profileGeneric = idis.getCosemObjectFactory().getProfileGeneric(ObisCode.fromString(loadProfileObisCode));
        if (profileGeneric == null) {
            idis.getLogger().log(Level.SEVERE, "Profile for obis code " + loadProfileObisCode + " is null");
            return MessageResult.createFailed(messageEntry);
        }

        Array capturedObjects = new Array();
        for (String capturedObjectDefinition : capturedObjectDefinitions) {
            String[] definitionParts = capturedObjectDefinition.split(",");
            try {
                int dlmsClassId = Integer.parseInt(definitionParts[0]);
                ObisCode obisCode = ObisCode.fromString(definitionParts[1]);
                int attribute = Integer.parseInt(definitionParts[2]);
                int dataIndex = Integer.parseInt(definitionParts[3]);
                Structure definition = new Structure();
                definition.addDataType(new Unsigned16(dlmsClassId));
                definition.addDataType(OctetString.fromObisCode(obisCode));
                definition.addDataType(new Integer8(attribute));
                definition.addDataType(new Unsigned16(dataIndex));
                capturedObjects.addDataType(definition);
            } catch (IndexOutOfBoundsException e) {
                idis.getLogger().log(Level.SEVERE, e.getMessage());
                return MessageResult.createFailed(messageEntry);
            } catch (NumberFormatException e) {
                idis.getLogger().log(Level.SEVERE, e.getMessage());
                return MessageResult.createFailed(messageEntry);
            } catch (IllegalArgumentException e) {
                idis.getLogger().log(Level.SEVERE, e.getMessage());
                return MessageResult.createFailed(messageEntry);
            }
        }
        profileGeneric.setCaptureObjectsAttr(capturedObjects);

        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult writeBillingLoadProfileCapturedObjects(MessageEntry messageEntry) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        String loadProfileObisCode = parts[1].substring(1).split("\"")[0];
        List<String> capturedObjectDefinitions = new ArrayList<>();
        int index = 2;
        while (true) {
            try {
                capturedObjectDefinitions.add(parts[index].substring(1).split("\"")[0]);
                index++;
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }
        ProfileGeneric profileGeneric = idis.getCosemObjectFactory().getProfileGeneric(ObisCode.fromString(loadProfileObisCode));
        if (profileGeneric == null) {
            idis.getLogger().log(Level.SEVERE, "Profile for obis code " + loadProfileObisCode + " is null");
            return MessageResult.createFailed(messageEntry);
        }

        Array capturedObjects = new Array();
        for (String capturedObjectDefinition : capturedObjectDefinitions) {
            String[] definitionParts = capturedObjectDefinition.split(",");
            try {
                int dlmsClassId = Integer.parseInt(definitionParts[0]);
                ObisCode obisCode = ObisCode.fromString(definitionParts[1]);
                int attribute = Integer.parseInt(definitionParts[2]);
                int dataIndex = Integer.parseInt(definitionParts[3]);
                Structure definition = new Structure();
                definition.addDataType(new Unsigned16(dlmsClassId));
                definition.addDataType(OctetString.fromObisCode(obisCode));
                definition.addDataType(new Integer8(attribute));
                definition.addDataType(new Unsigned16(dataIndex));
                capturedObjects.addDataType(definition);
            } catch (IndexOutOfBoundsException e) {
                idis.getLogger().log(Level.SEVERE, e.getMessage());
                return MessageResult.createFailed(messageEntry);
            } catch (NumberFormatException e) {
                idis.getLogger().log(Level.SEVERE, e.getMessage());
                return MessageResult.createFailed(messageEntry);
            } catch (IllegalArgumentException e) {
                idis.getLogger().log(Level.SEVERE, e.getMessage());
                return MessageResult.createFailed(messageEntry);
            }
        }
        profileGeneric.setCaptureObjectsAttr(capturedObjects);

        return MessageResult.createSuccess(messageEntry);
    }

    private void writeEmergencyProfile(int emergencyProfileId, Date date, int emergencyDuration, Limiter limiter) throws IOException {
        Limiter.EmergencyProfile emergencyProfile = limiter.new EmergencyProfile();
        emergencyProfile.addDataType(new Unsigned16(emergencyProfileId));
        emergencyProfile.addDataType(new OctetString(ProtocolTools.getSubArray(new AXDRDateTime(date).getBEREncodedByteArray(), 2)));
        emergencyProfile.addDataType(new Unsigned32(emergencyDuration));
        limiter.writeEmergencyProfile(emergencyProfile);
    }

    private MessageResult superVision(MessageEntry messageEntry) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        int phase = Integer.parseInt(parts[1].substring(1).split("\"")[0]);
        long threshold = Long.parseLong(parts[2].substring(1).split("\"")[0]);
        if (threshold > 0xFFFFFFFF) {
            idis.getLogger().log(Level.SEVERE, "Invalid threshold value, should be smaller than 4294967296");
            return MessageResult.createFailed(messageEntry);
        }

        ObisCode obisCode;
        switch (phase) {
            case 1:
                obisCode = ObisCode.fromString("1.0.31.4.0.255");
                break;
            case 2:
                obisCode = ObisCode.fromString("1.0.51.4.0.255");
                break;
            case 3:
                obisCode = ObisCode.fromString("1.0.71.4.0.255");
                break;
            default:
                idis.getLogger().log(Level.SEVERE, "Unexpected phase number, should be 1, 2 or 3");
                return MessageResult.createFailed(messageEntry);
        }
        RegisterMonitor registerMonitor = idis.getCosemObjectFactory().getRegisterMonitor(obisCode);
        Array thresholds = new Array();
        thresholds.addDataType(new Unsigned32(threshold));
        registerMonitor.writeThresholds(thresholds);

        idis.getLogger().log(Level.INFO, "Phase supervision message was successful");
        return MessageResult.createSuccess(messageEntry);
    }

    private void writeActions(int actionUnderThreshold, Limiter limiter) throws IOException {
        Limiter.ActionItem action1 = limiter.new ActionItem(OctetString.fromByteArray(DISCONNECTOR_SCRIPT_OBISCODE.getLN(), 6), new Unsigned16(1));
        Limiter.ActionItem action2 = limiter.new ActionItem(OctetString.fromByteArray(DISCONNECTOR_SCRIPT_OBISCODE.getLN(), 6), new Unsigned16(actionUnderThreshold));

        Limiter.ActionType actions = limiter.new ActionType(action1, action2);
        limiter.writeActions(actions);
    }

    private void writeNormalThreshold(int monitoredValue, int activeThreshold, Limiter limiter) throws IOException {
        if (monitoredValue == 1) {
            limiter.writeThresholdNormal(new Unsigned16(activeThreshold));
        } else {
            limiter.writeThresholdNormal(new Unsigned32(activeThreshold));
        }
    }

    private void writeEmergencyThreshold(int monitoredValue, int activeThreshold, Limiter limiter) throws IOException {
        if (monitoredValue == 1) {
            limiter.writeThresholdEmergency(new Unsigned16(activeThreshold));
        } else {
            limiter.writeThresholdEmergency(new Unsigned32(activeThreshold));
        }
    }

    private void setMonitoredValue(Limiter limiter, int monitoredValue) throws IOException {
        byte[] monitoredAttribute = new byte[]{1, 0, 15, 24, 0, (byte) 255};
        int classId = DLMSClassId.DEMAND_REGISTER.getClassId();
        if (monitoredValue == 1) {
            monitoredAttribute = new byte[]{1, 0, 90, 7, 0, (byte) 255};
            classId = DLMSClassId.REGISTER.getClassId();
        }
        if (monitoredValue == 2) {
            monitoredAttribute = new byte[]{1, 0, 1, 24, 0, (byte) 255};
        }

        Limiter.ValueDefinitionType vdt = limiter.new ValueDefinitionType();
        vdt.addDataType(new Unsigned16(classId));
        OctetString os = new OctetString(monitoredAttribute);
        vdt.addDataType(os);
        vdt.addDataType(new Integer8(2));
        limiter.writeMonitoredValue(vdt);
    }

    private byte getDLMSDayOfWeek(Calendar cal) {
        int dow = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (dow == 0) {
            dow = 7;
        }
        return (byte) dow;
    }

    protected Array convertDateToDLMSArray(Date executionDate) throws IOException {
        Calendar cal = Calendar.getInstance(idis.getTimeZone());
        cal.setTime(executionDate);
        byte[] dateBytes = new byte[5];
        dateBytes[0] = (byte) ((cal.get(Calendar.YEAR) >> 8) & 0xFF);
        dateBytes[1] = (byte) (cal.get(Calendar.YEAR) & 0xFF);
        dateBytes[2] = (byte) ((cal.get(Calendar.MONTH) & 0xFF) + 1);
        dateBytes[3] = (byte) (cal.get(Calendar.DAY_OF_MONTH) & 0xFF);
        dateBytes[4] = getDLMSDayOfWeek(cal);
        OctetString date = new OctetString(dateBytes);
        byte[] timeBytes = new byte[4];
        timeBytes[0] = (byte) cal.get(Calendar.HOUR_OF_DAY);
        timeBytes[1] = (byte) cal.get(Calendar.MINUTE);
        timeBytes[2] = (byte) 0x00;
        timeBytes[3] = (byte) 0x00;
        OctetString time = new OctetString(timeBytes);

        Array dateTimeArray = new Array();
        Structure strDateTime = new Structure();
        strDateTime.addDataType(time);
        strDateTime.addDataType(date);
        dateTimeArray.addDataType(strDateTime);
        return dateTimeArray;
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<>();
        MessageCategorySpec cat1 = new MessageCategorySpec("Disconnection and reconnection");
        cat1.addMessageSpec(addBasicMsg("Remote controlled disconnection", "RemoteDisconnect", false));
        cat1.addMessageSpec(addBasicMsg("Remote controlled reconnection", "RemoteConnect", false));
        cat1.addMessageSpec(addBasicMsgWithAttributes("Write control_mode of disconnect control", "SetControlMode", true, "Control mode (range 0 - 6)"));
        cat1.addMessageSpec(addBasicMsgWithAttributes("Open relay", "OpenRelay", false, "Relay number (1 or 2)"));
        cat1.addMessageSpec(addBasicMsgWithAttributes("Close relay", "CloseRelay", false, "Relay number (1 or 2)"));
        cat1.addMessageSpec(addBasicMsgWithAttributes("Load control configuration (limiter)", "LoadControlledConnect", false, "Monitored value (1: Total inst. current, 2: Avg A+ (sliding demand), 3: Avg total A (sliding demand))", "Normal threshold", "Emergency threshold", "Minimal over threshold duration (seconds)", "Minimal under threshold duration (seconds)", "Emergency profile ID", "Emergency activation time (dd/mm/yyyy hh:mm:ss)", "Emergency duration (seconds)", "Emergency profile group id list (comma separated, e.g. 1,2,3)", "Action when under threshold (0: nothing, 2: reconnect)"));
        cat1.addMessageSpec(addBasicMsgWithAttributes("Supervision monitor", "SuperVision", false, "Phase (1, 2 or 3)", "Threshold (ampere)"));
        cat1.addMessageSpec(addBasicMsgWithAttributes("Time controlled reconnection", TIMED_RECONNECT, false, DATE_DD_MM_YYYY_HH_MM));
        cat1.addMessageSpec(addBasicMsgWithAttributes("Time controlled disconnection", TIMED_DISCONNECT, false, DATE_DD_MM_YYYY_HH_MM));
        theCategories.add(cat1);

        MessageCategorySpec cat2 = new MessageCategorySpec("Load profile configuration");
        cat2.addMessageSpec(addBasicMsgWithOptionalAttributes(1, "Write captured objects for LP1", "ConfigureLoadProfile1CapturedObjects", true, "Load profile obis code", "Captured object definition 1", "Captured object definition 2", "Captured object definition 3", "Captured object definition 4", "Captured object definition 5", "Captured object definition 6", "Captured object definition 7", "Captured object definition 8"));
        cat2.addMessageSpec(addBasicMsgWithOptionalAttributes(2, "Write captured objects for LP2", "ConfigureLoadProfile2CapturedObjects", true, "Load profile obis code", "Captured object definition 1", "Captured object definition 2", "Captured object definition 3", "Captured object definition 4", "Captured object definition 5", "Captured object definition 6", "Captured object definition 7", "Captured object definition 8"));
        cat2.addMessageSpec(addBasicMsgWithOptionalAttributes("Write captured objects for Billing LP", "ConfigureBillingLoadProfileCapturedObjects", true, "Load profile obis code", "Captured object definition 1", "Captured object definition 2", "Captured object definition 3", "Captured object definition 4", "Captured object definition 5", "Captured object definition 6", "Captured object definition 7", "Captured object definition 8"));
        cat2.addMessageSpec(addBasicMsgWithAttributes("Write LP1 capture period", "WriteLP1CapturePeriod", false, "Capture period (seconds)"));
        cat2.addMessageSpec(addBasicMsgWithAttributes("Write LP2 capture period", "WriteLP2CapturePeriod", false, "Capture period (seconds)"));
        theCategories.add(cat2);

        MessageCategorySpec cat3 = new MessageCategorySpec("Alarm configuration");
        cat3.addMessageSpec(addBasicMsg("Reset all alarm bits", "ResetAllAlarmBits", false));
        cat3.addMessageSpec(addBasicMsg("Reset all error bits", "ResetAllErrorBits", false));
        cat3.addMessageSpec(addBasicMsgWithAttributes("Write alarm filter", "WriteAlarmFilter", false, "Alarm filter (decimal value)"));
        theCategories.add(cat3);

        MessageCategorySpec cat4 = new MessageCategorySpec("Set time");
        cat4.addMessageSpec(addBasicMsg("Force time", "ForceTime", false));
        theCategories.add(cat4);

        MessageCategorySpec cat5 = new MessageCategorySpec("MBus slave commission");
        cat5.addMessageSpec(addBasicMsg("MBus meter commission", "SlaveCommission", false));
        theCategories.add(cat5);

        MessageCategorySpec cat6 = new MessageCategorySpec("PLC parameters");
        cat6.addMessageSpec(addBasicMsgWithAttributes("Set timeout_not_addressed attribute", "SetTimeOutNotAddressed", false, "timeout_not_addressed"));
        theCategories.add(cat6);

        MessageCategorySpec cat7 = new MessageCategorySpec("Configuration");
        cat7.addMessageSpec(addBasicMsgWithAttributes("Configuration download", CONFIGURATION_DOWNLOAD, false, CONFIGURATION_USER_FILE));
        theCategories.add(cat7);

        theCategories.add(getActivityCalendarCategory());

        return theCategories;
    }

    protected MessageSpec addBasicMsg(final String keyId, final String tagName, final boolean advanced) {
        final MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        final MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWithAttributes(final String keyId, final String tagName, final boolean advanced, String... attr) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        for (String attribute : attr) {
            tagSpec.add(new MessageAttributeSpec(attribute, true));
        }
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWithOptionalAttributes(int loadProfileIndex, final String keyId, final String tagName, final boolean advanced, String... attr) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        int index = 0;
        for (String attribute : attr) {
            MessageAttributeSpec attributeSpec = new MessageAttributeSpec(attribute, false);
            if (index == 0) {
                attributeSpec.setValue("1.0.99." + String.valueOf(loadProfileIndex) + ".0.255");
            }
            if (index == 1) {
                attributeSpec.setValue("8,0.0.1.0.0.255,2,0");
            }
            if (index == 2) {
                attributeSpec.setValue("1,0.0.96.10." + String.valueOf(loadProfileIndex) + ".255,2,0");
            }
            tagSpec.add(attributeSpec);
            index++;
        }
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    protected MessageSpec addBasicMsgWithOptionalAttributes(final String keyId, final String tagName, final boolean advanced, String... attr) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        int index = 0;
        for (String attribute : attr) {
            MessageAttributeSpec attributeSpec = new MessageAttributeSpec(attribute, false);
            if (index == 0) {
                attributeSpec.setValue("0.0.98.1.0.255");
            }
            if (index == 1) {
                attributeSpec.setValue("8,0.0.1.0.0.255,2,0");
            }
            tagSpec.add(attributeSpec);
            index++;
        }
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    public String writeTag(MessageTag msgTag) {

        if (msgTag.getName().equalsIgnoreCase(TIMED_DISCONNECT) || msgTag.getName().equalsIgnoreCase(TIMED_RECONNECT)) {
            StringBuilder builder = new StringBuilder();

            // a. Opening tag
            builder.append("<");
            builder.append(msgTag.getName());
            builder.append(" ");

            // b. Add all attributes
            for (MessageAttribute att : msgTag.getAttributes()) {
                builder.append(att.getSpec().getName()).append("=\"").append(att.getValue()).append("\" ");
            }

            // c. Add extra child containing system timezone offset
            TimeZone systemTimeZone = TimeZone.getDefault();
            builder.append(TIMEZONE).append("=\"").append(systemTimeZone.getID()).append("\" ");

            // d. Closing tag
            builder.append(">");
            builder.append("</");
            builder.append(msgTag.getName());
            builder.append(">");

            return builder.toString();
        } else if (msgTag.getName().equalsIgnoreCase(RtuMessageConstant.TOU_ACTIVITY_CAL) || msgTag.getName().equalsIgnoreCase(RtuMessageConstant.TOU_SPECIAL_DAYS)) {
            StringBuilder builder = new StringBuilder();

            // a. Opening tag
            builder.append("<");
            builder.append(msgTag.getName());
            builder.append(">");

            String name = "";
            String activationDate = "1";
            int codeId = 0;

            // b. Attributes
            for (MessageAttribute att : msgTag.getAttributes()) {
                if (RtuMessageConstant.TOU_ACTIVITY_NAME.equalsIgnoreCase(att.getSpec().getName())) {
                    if (att.getValue() != null) {
                        name = att.getValue();
                    }
                } else if (RtuMessageConstant.TOU_ACTIVITY_DATE.equalsIgnoreCase(att.getSpec().getName())) {
                    if (att.getValue() != null) {
                        activationDate = att.getValue();
                    }
                } else if (RtuMessageConstant.TOU_ACTIVITY_CODE_TABLE.equalsIgnoreCase(att.getSpec().getName())) {
                    if (att.getValue() != null) {
                        codeId = Integer.valueOf(att.getValue());
                    }
                }
            }

            Date actDate = new Date(Long.valueOf(activationDate));
            if (codeId > 0) {
                try {
                    String xmlContent = CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(codeId, Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime().before(actDate) ? actDate.getTime() : 1, name);
                    addChildTag(builder, RAW_CONTENT, ProtocolTools.compress(xmlContent));
                } catch (ParserConfigurationException e) {
                    idis.getLogger().severe(e.getMessage());
                } catch (IOException e) {
                    idis.getLogger().severe(e.getMessage());
                }
            }

            // d. Closing tag
            builder.append("</");
            builder.append(msgTag.getName());
            builder.append(">");
            return builder.toString();
        } else if (msgTag.getName().equalsIgnoreCase(CONFIGURATION_DOWNLOAD)) {
            StringBuilder builder = new StringBuilder();
            int codeId = 0;

            // a. Opening tag
            builder.append("<");
            builder.append(msgTag.getName());
            builder.append(">");

            // b. Attributes
            for (MessageAttribute att : msgTag.getAttributes()) {
                if (CONFIGURATION_USER_FILE.equalsIgnoreCase(att.getSpec().getName())) {
                    if (att.getValue() != null) {
                        codeId = Integer.valueOf(att.getValue());
                    }
                }
            }

            if (codeId > 0) {
                builder.append("<IncludedFile><includeFile fileId=\"").append(codeId).append("\"/></IncludedFile>");
            }

            // c. Closing tag
            builder.append("</");
            builder.append(msgTag.getName());
            builder.append(">");
            return builder.toString();
        } else {
            StringBuilder buf = new StringBuilder();

            // a. Opening tag
            buf.append("<");
            buf.append(msgTag.getName());

            // b. Attributes
            for (MessageAttribute att : msgTag.getAttributes()) {
                if (att.getValue() == null || att.getValue().isEmpty()) {
                    continue;
                }
                buf.append(" ").append(att.getSpec().getName());
                buf.append("=").append('"').append(att.getValue()).append('"');
            }
            buf.append(">");

            // c. sub elements
            for (Object o : msgTag.getSubElements()) {
                MessageElement elt = (MessageElement) o;
                if (elt.isTag()) {
                    buf.append(writeTag((MessageTag) elt));
                } else if (elt.isValue()) {
                    String value = writeValue((MessageValue) elt);
                    if (value == null || value.isEmpty()) {
                        return "";
                    }
                    buf.append(value);
                }
            }

            // d. Closing tag
            buf.append("</");
            buf.append(msgTag.getName());
            buf.append(">");

            return buf.toString();
        }
    }

    /**
     * Adds a child tag to the given {@link StringBuffer}.
     *
     * @param buf     The string builder to whose contents the child tag needs to be added.
     * @param tagName The name of the child tag to add.
     * @param value   The contents (value) of the tag.
     */
    protected void addChildTag(StringBuilder buf, String tagName, Object value) {
        buf.append(System.getProperty("line.separator"));
        buf.append("<");
        buf.append(tagName);
        buf.append(">");
        buf.append(value);
        buf.append("</");
        buf.append(tagName);
        buf.append(">");
    }
}