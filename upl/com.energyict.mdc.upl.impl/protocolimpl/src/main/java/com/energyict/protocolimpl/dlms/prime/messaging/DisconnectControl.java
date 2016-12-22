package com.energyict.protocolimpl.dlms.prime.messaging;

import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageResult;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 27/02/12
 * Time: 14:32
 */
public class DisconnectControl extends PrimeMessageExecutor {

    private static final ObisCode TIMED_CONNECTOR_ACTION_OBISCODE = ObisCode.fromString("0.0.15.0.1.255");
    private static final ObisCode DISCONNECTOR_SCRIPT_OBISCODE = ObisCode.fromString("0.0.10.0.106.255");

    private static final ObisCode DISCONNECT_CONTROL_MAIN_OBIS = ObisCode.fromString("0.0.96.3.10.255");
    private static final ObisCode DISCONNECT_CONTROL_RELAY1_OBIS = ObisCode.fromString("0.128.5.0.0.255");
    private static final ObisCode DISCONNECT_CONTROL_RELAY2_OBIS = ObisCode.fromString("0.128.5.1.0.255");

    private static final String CONNECT_MAIN_TAG = "ConnectMain";
    private static final String DISCONNECT_MAIN_TAG = "DisconnectMain";

    private static final String CONNECT_RELAY1_TAG = "ConnectRelay1";
    private static final String DISCONNECT_RELAY1_TAG = "DisconnectRelay1";

    private static final String CONNECT_RELAY2_TAG = "ConnectRelay2";
    private static final String DISCONNECT_RELAY2_TAG = "DisconnectRelay2";

    public DisconnectControl(DlmsSession session) {
        super(session);
    }

    public static MessageCategorySpec getCategorySpec() {
        MessageCategorySpec spec = new MessageCategorySpec("Disconnect control");

        // Main contactor
        spec.addMessageSpec(addBasicMsg("Connect main contactor", CONNECT_MAIN_TAG, false));
        spec.addMessageSpec(addBasicMsg("Disconnect main contactor", DISCONNECT_MAIN_TAG, false));

        // Load management relay 1
        spec.addMessageSpec(addBasicMsg("Connect relay 1", CONNECT_RELAY1_TAG, false));
        spec.addMessageSpec(addBasicMsg("Disconnect relay 1", DISCONNECT_RELAY1_TAG, false));

        // Load management relay 2
        spec.addMessageSpec(addBasicMsg("Connect relay 2", CONNECT_RELAY2_TAG, false));
        spec.addMessageSpec(addBasicMsg("Disconnect relay 2", DISCONNECT_RELAY2_TAG, false));
        return spec;
    }

    public boolean canHandle(MessageEntry messageEntry) {
        boolean canHandle = false;
        canHandle |= isMessageTag(CONNECT_MAIN_TAG, messageEntry);
        canHandle |= isMessageTag(DISCONNECT_MAIN_TAG, messageEntry);
        canHandle |= isMessageTag(CONNECT_RELAY1_TAG, messageEntry);
        canHandle |= isMessageTag(DISCONNECT_RELAY1_TAG, messageEntry);
        canHandle |= isMessageTag(CONNECT_RELAY2_TAG, messageEntry);
        canHandle |= isMessageTag(DISCONNECT_RELAY2_TAG, messageEntry);
        return canHandle;
    }

    private String stripOffTag(String content) {
        return content.substring(content.indexOf(">") + 1, content.lastIndexOf("<"));
    }

    public final MessageResult execute(MessageEntry messageEntry) throws IOException {
        try {
            if (isMessageTag(CONNECT_MAIN_TAG, messageEntry)) {
                return connect(messageEntry, "Main breaker", DISCONNECT_CONTROL_MAIN_OBIS);
            } else if (isMessageTag(DISCONNECT_MAIN_TAG, messageEntry)) {
                return disconnect(messageEntry, "Main breaker", DISCONNECT_CONTROL_MAIN_OBIS);
            }

            if (isMessageTag(CONNECT_RELAY1_TAG, messageEntry)) {
                return connect(messageEntry, "Relay 1", DISCONNECT_CONTROL_RELAY1_OBIS);
            } else if (isMessageTag(DISCONNECT_RELAY1_TAG, messageEntry)) {
                return disconnect(messageEntry, "Relay 1", DISCONNECT_CONTROL_RELAY1_OBIS);
            }

            if (isMessageTag(CONNECT_RELAY2_TAG, messageEntry)) {
                return connect(messageEntry, "Relay 2", DISCONNECT_CONTROL_RELAY2_OBIS);
            } else if (isMessageTag(DISCONNECT_RELAY2_TAG, messageEntry)) {
                return disconnect(messageEntry, "Relay 2", DISCONNECT_CONTROL_RELAY2_OBIS);
            }
        } catch (IOException e) {
            getLogger().severe("An error occurred while handling message [" + messageEntry.getContent() + "]: " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
        getLogger().severe("Unable to handle message [" + messageEntry.getContent() + "] in [" + getClass().getSimpleName() + "]");
        return MessageResult.createFailed(messageEntry);
    }

    private final MessageResult connect(MessageEntry messageEntry, String breakerName, ObisCode obis) throws IOException {
        Date activationDate = parseActivationDate(messageEntry);
        if (activationDate == null) {
            getLogger().severe("Connecting " + breakerName + " in AS300D now ...");
            getSession().getCosemObjectFactory().getDisconnector(obis).remoteReconnect();
            getLogger().severe(breakerName + " successfully reconnected.");
        } else {
            //Only for reconnecting the MAIN breaking
            getLogger().severe("Scheduling a reconnect for " + breakerName + " in AS300D for " + activationDate.toString());
            timedAction(activationDate, 2);
            getLogger().info("Reconnect was successfully scheduled");
        }
        return MessageResult.createSuccess(messageEntry);
    }

    private final MessageResult disconnect(MessageEntry messageEntry, String breakerName, ObisCode obis) throws IOException {
        Date activationDate = parseActivationDate(messageEntry);
        if (activationDate == null) {
            getLogger().severe("Disconnecting " + breakerName + " in AS300D now ...");
            getSession().getCosemObjectFactory().getDisconnector(obis).remoteDisconnect();
            getLogger().severe(breakerName + " successfully disconnected.");
        } else {
            //Only for disconnecting the MAIN breaking
            getLogger().severe("Scheduling a disconnect for " + breakerName + " in AS300D for " + activationDate.toString());
            timedAction(activationDate, 1);
            getLogger().info("Disconnect was successfully scheduled");
        }
        return MessageResult.createSuccess(messageEntry);
    }

    private void timedAction(Date date, int action) throws IOException {
        SingleActionSchedule singleActionSchedule = getSession().getCosemObjectFactory().getSingleActionSchedule(TIMED_CONNECTOR_ACTION_OBISCODE);
        Structure scriptStruct = new Structure();
        scriptStruct.addDataType(new OctetString(DISCONNECTOR_SCRIPT_OBISCODE.getLN()));
        scriptStruct.addDataType(new Unsigned16(action));     // 1 = disconnect, 2 = connect
        singleActionSchedule.writeExecutedScript(scriptStruct);
        singleActionSchedule.writeExecutionTime(convertDateToDLMSArray(date));
    }

    private Date parseActivationDate(MessageEntry messageEntry) {
        try {
            String value = stripOffTag(messageEntry.getContent()).trim();
            return new Date(Long.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private byte getDLMSDayOfWeek(Calendar cal) {
        int dow = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (dow == 0) {
            dow = 7;
        }
        return (byte) dow;
    }

    private Array convertDateToDLMSArray(Date executionDate) throws IOException {
        Calendar cal = Calendar.getInstance(getSession().getTimeZone());
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
}