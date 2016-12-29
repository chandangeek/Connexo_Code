package com.energyict.protocolimpl.dlms.idis;

import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.MessageAttributeSpec;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 20/09/11
 * Time: 17:22
 */
public class IDISMBusMessageHandler extends IDISMessageHandler {

    private static final ObisCode TIMED_CONNECTOR_ACTION_MBUS_OBISCODE = ObisCode.fromString("0.1.15.0.1.255");
    protected static final ObisCode DISCONNECTOR_SCRIPT_MBUS_OBISCODE = ObisCode.fromString("0.1.10.0.106.255");
    protected static final ObisCode DISCONNECTOR_CONTROL_MBUS_OBISCODE = ObisCode.fromString("0.0.24.4.0.255");

    public IDISMBusMessageHandler(IDISMBus idis, TariffCalendarFinder calendarFinder, Extractor extractor) {
        super(idis, calendarFinder, extractor);
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        try {
            if (messageEntry.getContent().contains("<RemoteDisconnect")) {
                return remoteDisconnect(messageEntry);
            } else if (messageEntry.getContent().contains("<RemoteConnect")) {
                return remoteConnect(messageEntry);
            } else if (messageEntry.getContent().contains("<TimedReconnect")) {
                return timedAction(messageEntry, 2);
            } else if (messageEntry.getContent().contains("<TimedDisconnect")) {
                return timedAction(messageEntry, 1);
            } else if (messageEntry.getContent().contains("<SlaveDecommission")) {
                return decommission(messageEntry);
            } else if (messageEntry.getContent().contains("<SetEncryptionKey")) {
                return setEncryptionKey(messageEntry);
            } else if (messageEntry.getContent().contains("<TransferEncryptionKey")) {
                return transferEncryptionKey(messageEntry);
            } else if (messageEntry.getContent().contains("<ConfigureMBusLoadProfileCapturedObjects")) {
                return configureMBusLoadProfileCapturedObjects(messageEntry);
            } else if (messageEntry.getContent().contains("<WriteCaptureDefinition")) {
                return writeCaptureDefinition(messageEntry);
            } else if (messageEntry.getContent().contains("<WriteCapturePeriod")) {
                return writeCapturePeriod(messageEntry);
            }
        } catch (DataAccessResultException e) {
            idis.getLogger().log(Level.SEVERE, "Error executing message: " + e.getMessage());
            return MessageResult.createFailed(messageEntry, e.getMessage());
        } catch (NumberFormatException e) {
            idis.getLogger().log(Level.SEVERE, "Error executing message: " + e.getMessage());
            return MessageResult.createFailed(messageEntry, e.getMessage());
        }
        return MessageResult.createFailed(messageEntry);
    }

    protected MessageResult decommission(MessageEntry messageEntry) throws IOException {
        getMBusClient().invoke(2, new Unsigned8(0).getBEREncodedByteArray());
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult setEncryptionKey(MessageEntry messageEntry) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        String key = parts[1].substring(1).split("\"")[0];
        getMBusClient().setEncryptionKey(key);
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult transferEncryptionKey(MessageEntry messageEntry) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        String key = parts[1].substring(1).split("\"")[0];
        getMBusClient().setTransportKey(key);
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult writeCaptureDefinition(MessageEntry messageEntry) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        byte[] dib1Bytes = ProtocolTools.getBytesFromHexString(parts[1].substring(1).split("\"")[0], "$");
        OctetString dib1 = OctetString.fromByteArray(dib1Bytes, dib1Bytes.length);

        byte[] vib1Bytes = ProtocolTools.getBytesFromHexString(parts[2].substring(1).split("\"")[0], "$");
        OctetString vib1 = OctetString.fromByteArray(vib1Bytes, vib1Bytes.length);

        byte[] dib2Bytes = ProtocolTools.getBytesFromHexString(parts[3].substring(1).split("\"")[0], "$");
        OctetString dib2 = OctetString.fromByteArray(dib2Bytes, dib2Bytes.length);

        byte[] vib2Bytes = ProtocolTools.getBytesFromHexString(parts[4].substring(1).split("\"")[0], "$");
        OctetString vib2 = OctetString.fromByteArray(vib2Bytes, vib2Bytes.length);

        byte[] dib3Bytes = ProtocolTools.getBytesFromHexString(parts[5].substring(1).split("\"")[0], "$");
        OctetString dib3 = OctetString.fromByteArray(dib3Bytes, dib3Bytes.length);

        byte[] vib3Bytes = ProtocolTools.getBytesFromHexString(parts[6].substring(1).split("\"")[0], "$");
        OctetString vib3 = OctetString.fromByteArray(vib3Bytes, vib3Bytes.length);

        byte[] dib4Bytes = ProtocolTools.getBytesFromHexString(parts[7].substring(1).split("\"")[0], "$");
        OctetString dib4 = OctetString.fromByteArray(dib4Bytes, dib4Bytes.length);

        byte[] vib4Bytes = ProtocolTools.getBytesFromHexString(parts[8].substring(1).split("\"")[0], "$");
        OctetString vib4 = OctetString.fromByteArray(vib4Bytes, vib4Bytes.length);

        Structure element1 = new Structure();
        element1.addDataType(dib1);
        element1.addDataType(vib1);
        Structure element2 = new Structure();
        element2.addDataType(dib2);
        element2.addDataType(vib2);
        Structure element3 = new Structure();
        element3.addDataType(dib3);
        element3.addDataType(vib3);
        Structure element4 = new Structure();
        element4.addDataType(dib4);
        element4.addDataType(vib4);

        Array capture_definition = new Array();
        capture_definition.addDataType(element1);
        capture_definition.addDataType(element2);
        capture_definition.addDataType(element3);
        capture_definition.addDataType(element4);

        getMBusClient().writeCaptureDefinition(capture_definition);
        return MessageResult.createSuccess(messageEntry);
    }

    private MessageResult configureMBusLoadProfileCapturedObjects(MessageEntry messageEntry) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        String loadProfileObisCodeString = parts[1].substring(1).split("\"")[0];
        ObisCode loadProfileObisCode = ProtocolTools.setObisCodeField(ObisCode.fromString(loadProfileObisCodeString), 1, (byte) idis.getGasSlotId());
        List<String> capturedObjectDefinitions = new ArrayList<String>();
        int index = 2;
        while (true) {
            try {
                capturedObjectDefinitions.add(parts[index].substring(1).split("\"")[0]);
                index++;
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }
        ProfileGeneric profileGeneric = idis.getCosemObjectFactory().getProfileGeneric(loadProfileObisCode);
        if (profileGeneric == null) {
            idis.getLogger().log(Level.SEVERE, "Profile for obis code " + loadProfileObisCode.toString() + " is null");
            return MessageResult.createFailed(messageEntry);
        }

        Array capturedObjects = new Array();
        for (String capturedObjectDefinition : capturedObjectDefinitions) {
            String[] definitionParts = capturedObjectDefinition.split(",");
            try {
                int dlmsClassId = Integer.parseInt(definitionParts[0]);
                ObisCode obisCode = ObisCode.fromString(definitionParts[1]);
                if (dlmsClassId != DLMSClassId.CLOCK.getClassId()) {      //Don't change the B-field of the clock obis code
                    obisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) idis.getGasSlotId());
                }
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

    protected MessageResult writeCapturePeriod(MessageEntry messageEntry) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        int period = Integer.parseInt(parts[1].substring(1).split("\"")[0]);

        getMBusClient().setCapturePeriod(period);
        return MessageResult.createSuccess(messageEntry);
    }

    private MBusClient getMBusClient() throws IOException {
        return idis.getCosemObjectFactory().getMbusClient(getMBusClientObisCode(), MbusClientAttributes.VERSION10);
    }

    /**
     * Returns the obiscode of the MBus-client object for a specific MBus meter.
     */
    private ObisCode getMBusClientObisCode() {
        return ProtocolTools.setObisCodeField(MBUS_CLIENT_OBISCODE, 1, (byte) idis.getGasSlotId());
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
        SingleActionSchedule singleActionSchedule = idis.getCosemObjectFactory().getSingleActionSchedule(TIMED_CONNECTOR_ACTION_MBUS_OBISCODE);

        Structure scriptStruct = new Structure();
        scriptStruct.addDataType(new OctetString(DISCONNECTOR_SCRIPT_MBUS_OBISCODE.getLN()));
        scriptStruct.addDataType(new Unsigned16(action + (2 * (idis.getGasSlotId() - 1))));     // 1 = disconnect MBus 1, 2 = reconnect MBus 1, 3 = disconnect MBus 2,...

        singleActionSchedule.writeExecutedScript(scriptStruct);
        singleActionSchedule.writeExecutionTime(convertDateToDLMSArray(date));
        idis.getLogger().log(Level.INFO, "Timed dis/connect message was successful");
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult remoteDisconnect(MessageEntry messageEntry) throws IOException {
        ObisCode obisCode = ProtocolTools.setObisCodeField(DISCONNECTOR_CONTROL_MBUS_OBISCODE, 1, (byte) idis.getGasSlotId());
        idis.getCosemObjectFactory().getDisconnector(obisCode).remoteDisconnect();
        idis.getLogger().log(Level.INFO, "Remote disconnect message was successful");
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult remoteConnect(MessageEntry messageEntry) throws IOException {
        ObisCode obisCode = ProtocolTools.setObisCodeField(DISCONNECTOR_CONTROL_MBUS_OBISCODE, 1, (byte) idis.getGasSlotId());
        idis.getCosemObjectFactory().getDisconnector(obisCode).remoteReconnect();
        idis.getLogger().log(Level.INFO, "Remote connect message was successful");
        return MessageResult.createSuccess(messageEntry);
    }


    protected MessageSpec addBasicMsgWithOptionalAttributes(final String keyId, final String tagName, final boolean advanced, String... attr) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        int index = 0;
        for (String attribute : attr) {
            MessageAttributeSpec attributeSpec = new MessageAttributeSpec(attribute, false);
            if (index == 0) {
                attributeSpec.setValue("0.X.24.3.0.255");
            }
            if (index == 1) {
                attributeSpec.setValue("8,0.0.1.0.0.255,2,0");
            }
            if (index == 2) {
                attributeSpec.setValue("1,0.X.96.10.3.255,2,0");
            }
            tagSpec.add(attributeSpec);
            index++;
        }
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<MessageCategorySpec>();
        MessageCategorySpec cat1 = new MessageCategorySpec("MBus disconnection and reconnection");
        cat1.addMessageSpec(addBasicMsg("Remote controlled disconnection", "RemoteDisconnect", false));
        cat1.addMessageSpec(addBasicMsg("Remote controlled reconnection", "RemoteConnect", false));
        cat1.addMessageSpec(addBasicMsgWithAttributes("Time controlled reconnection", "TimedReconnect", false, "Date (dd/mm/yyyy hh:mm)"));
        cat1.addMessageSpec(addBasicMsgWithAttributes("Time controlled disconnection", "TimedDisconnect", false, "Date (dd/mm/yyyy hh:mm)"));
        theCategories.add(cat1);

        MessageCategorySpec cat2 = new MessageCategorySpec("MBus meter registration");
        cat2.addMessageSpec(addBasicMsg("MBus meter decommission", "SlaveDecommission", false));
        cat2.addMessageSpec(addBasicMsgWithAttributes("Set encryption key to be used with the M-Bus slave device", "SetEncryptionKey", false, "Encryption key"));
        cat2.addMessageSpec(addBasicMsgWithAttributes("Transfer encryption key to the M-Bus slave", "TransferEncryptionKey", false, "Encryption key"));
        cat2.addMessageSpec(addBasicMsgWithAttributes("Configure capture_definition of the MBus client", "WriteCaptureDefinition", false, "Instance 1 DIB", "Instance 1 VIB", "Instance 2 DIB", "Instance 2 VIB", "Instance 3 DIB", "Instance 3 VIB", "Instance 4 DIB", "Instance 4 VIB"));
        cat2.addMessageSpec(addBasicMsgWithAttributes("Configure capture_period of the MBus client", "WriteCapturePeriod", false, "Capture period (in seconds)"));
        theCategories.add(cat2);

        MessageCategorySpec cat3 = new MessageCategorySpec("Load profile configuration");
        cat3.addMessageSpec(addBasicMsgWithOptionalAttributes("Write captured_objects for MBus LP", "ConfigureMBusLoadProfileCapturedObjects", true, "Load profile obis code", "Captured object definition 1", "Captured object definition 2", "Captured object definition 3", "Captured object definition 4", "Captured object definition 5", "Captured object definition 6"));
        theCategories.add(cat3);

        return theCategories;
    }
}