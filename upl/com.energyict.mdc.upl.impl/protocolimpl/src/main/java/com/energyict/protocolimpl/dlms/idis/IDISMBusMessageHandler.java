package com.energyict.protocolimpl.dlms.idis;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
    protected static final ObisCode MBUS_CLIENT_OBISCODE = ObisCode.fromString("0.1.24.1.0.255");

    public IDISMBusMessageHandler(IDISMBus idis) {
        super(idis);
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
            } else if (messageEntry.getContent().contains("<SlaveCommission")) {
                return commission(messageEntry);
            } else if (messageEntry.getContent().contains("<SlaveDecommission")) {
                return decommission(messageEntry);
            } else if (messageEntry.getContent().contains("<SetEncryptionKey")) {
                return setEncryptionKey(messageEntry);
            } else if (messageEntry.getContent().contains("<TransferEncryptionKey")) {
                return transferEncryptionKey(messageEntry);
            }
        } catch (NumberFormatException e) {
            idis.getLogger().log(Level.SEVERE, "Error executing message: " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage());
        }
        return MessageResult.createFailed(messageEntry);
    }

    protected MessageResult commission(MessageEntry messageEntry) throws IOException, InterruptedException {
        ObisCode obisCode = MBUS_CLIENT_OBISCODE;
        ProtocolTools.setObisCodeField(obisCode, 1, (byte) idis.getGasSlotId());
        MBusClient mbusClient = idis.getCosemObjectFactory().getMbusClient(obisCode, MbusClientAttributes.VERSION10);
        mbusClient.installSlave(0);
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult decommission(MessageEntry messageEntry) throws IOException, InterruptedException {
        ObisCode obisCode = MBUS_CLIENT_OBISCODE;
        ProtocolTools.setObisCodeField(obisCode, 1, (byte) idis.getGasSlotId());
        MBusClient mbusClient = idis.getCosemObjectFactory().getMbusClient(obisCode, MbusClientAttributes.VERSION10);
        mbusClient.deinstallSlave();
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult setEncryptionKey(MessageEntry messageEntry) throws IOException, InterruptedException {
        String[] parts = messageEntry.getContent().split("=");
        String key = parts[1].substring(1).split("\"")[0];
        ObisCode obisCode = MBUS_CLIENT_OBISCODE;
        ProtocolTools.setObisCodeField(obisCode, 1, (byte) idis.getGasSlotId());
        MBusClient mbusClient = idis.getCosemObjectFactory().getMbusClient(obisCode, MbusClientAttributes.VERSION10);
        mbusClient.setEncryptionKey(key);
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult transferEncryptionKey(MessageEntry messageEntry) throws IOException, InterruptedException {
        String[] parts = messageEntry.getContent().split("=");
        String key = parts[1].substring(1).split("\"")[0];
        ObisCode obisCode = MBUS_CLIENT_OBISCODE;
        ProtocolTools.setObisCodeField(obisCode, 1, (byte) idis.getGasSlotId());
        MBusClient mbusClient = idis.getCosemObjectFactory().getMbusClient(obisCode, MbusClientAttributes.VERSION10);
        mbusClient.setTransportKey(key);
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult timedAction(MessageEntry messageEntry, int action) throws IOException {
        String[] parts = messageEntry.getContent().split("=");
        String dateString = parts[1].substring(1).split("\"")[0];
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date date;
        try {
            date = formatter.parse(dateString);
        } catch (ParseException e) {
            return MessageResult.createFailed(messageEntry);
        }
        SingleActionSchedule singleActionSchedule = idis.getCosemObjectFactory().getSingleActionSchedule(TIMED_CONNECTOR_ACTION_MBUS_OBISCODE);

        Structure scriptStruct = new Structure();
        scriptStruct.addDataType(new OctetString(DISCONNECTOR_SCRIPT_MBUS_OBISCODE.getLN()));
        scriptStruct.addDataType(new Unsigned16(action + (2 * (idis.getGasSlotId() - 1))));     // 1 = disconnect MBus 1, 2 = reconnect MBus 1, 3 = disconnect MBus 2,...

        singleActionSchedule.writeExecutedScript(scriptStruct);
        singleActionSchedule.writeExecutionTime(convertDateToDLMSArray(date));
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult remoteDisconnect(MessageEntry messageEntry) throws IOException {
        ObisCode obisCode = ProtocolTools.setObisCodeField(DISCONNECTOR_CONTROL_MBUS_OBISCODE, 1, (byte) idis.getGasSlotId());
        idis.getCosemObjectFactory().getDisconnector(obisCode).remoteDisconnect();
        return MessageResult.createSuccess(messageEntry);
    }

    protected MessageResult remoteConnect(MessageEntry messageEntry) throws IOException {
        ObisCode obisCode = ProtocolTools.setObisCodeField(DISCONNECTOR_CONTROL_MBUS_OBISCODE, 1, (byte) idis.getGasSlotId());
        idis.getCosemObjectFactory().getDisconnector(obisCode).remoteReconnect();
        return MessageResult.createSuccess(messageEntry);
    }

    public List getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<MessageCategorySpec>();
        MessageCategorySpec cat1 = new MessageCategorySpec("MBus disconnection and reconnection");
        cat1.addMessageSpec(addBasicMsg("Remote controlled disconnection", "RemoteDisconnect", false));
        cat1.addMessageSpec(addBasicMsg("Remote controlled reconnection", "RemoteConnect", false));
        cat1.addMessageSpec(addBasicMsgWithAttributes("Time controlled reconnection", "TimedReconnect", false, "Date (dd/mm/yyyy hh:mm)"));                                                                                                                                                                                                                               //"Emergency profile ID", "Emergency activation time", "Emergency duration", "Emergency profile group id list (comma separated, e.g. 1,2,3", "Active emergency profile (1: yes, 0: no)",
        cat1.addMessageSpec(addBasicMsgWithAttributes("Time controlled disconnection", "TimedDisconnect", false, "Date (dd/mm/yyyy hh:mm)"));
        theCategories.add(cat1);

        MessageCategorySpec cat2 = new MessageCategorySpec("MBus meter registration");
        cat1.addMessageSpec(addBasicMsg("MBus meter commission", "SlaveCommission", false));
        cat1.addMessageSpec(addBasicMsg("MBus meter decommission", "SlaveDecommission", false));
        cat1.addMessageSpec(addBasicMsgWithAttributes("Set encryption key to be used with the M-Bus slave device", "SetEncryptionKey", false, "Encryption key"));
        cat1.addMessageSpec(addBasicMsgWithAttributes("Transfer encryption key to the M-Bus slave", "TransferEncryptionKey", false, "Encryption key"));
        theCategories.add(cat2);

        return theCategories;
    }
}