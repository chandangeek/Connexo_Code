package com.energyict.protocolimpl.dlms.elster.as300d.messaging;

import com.energyict.dlms.DlmsSession;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTagSpec;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 27/02/12
 * Time: 14:32
 */
public class DisconnectControl {

    private static final ObisCode DISCONNECT_CONTROL_MAIN_OBIS = ObisCode.fromString("0.0.96.3.10.255");
    private static final ObisCode DISCONNECT_CONTROL_RELAY1_OBIS = ObisCode.fromString("0.128.5.0.0.255");
    private static final ObisCode DISCONNECT_CONTROL_RELAY2_OBIS = ObisCode.fromString("0.128.5.1.0.255");

    private static final String CONNECT_MAIN_TAG = "ConnectMain";
    private static final String DISCONNECT_MAIN_TAG = "DisconnectMain";

    private static final String CONNECT_RELAY1_TAG = "ConnectRelay1";
    private static final String DISCONNECT_RELAY1_TAG = "DisconnectRelay1";

    private static final String CONNECT_RELAY2_TAG = "ConnectRelay2";
    private static final String DISCONNECT_RELAY2_TAG = "DisconnectRelay2";

    private final DlmsSession session;

    public DisconnectControl(DlmsSession session) {
        this.session = session;
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

    private static MessageSpec addBasicMsg(String displayName, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(displayName, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
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

    public MessageResult execute(MessageEntry messageEntry) throws IOException {
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
            getLogger().severe("An error occured while handling message [" + messageEntry.getContent() + "]: " + e.getMessage());
            return MessageResult.createFailed(messageEntry);
        }
        getLogger().severe("Unable to handle message [" + messageEntry.getContent() + "] in [" + getClass().getSimpleName() + "]");
        return MessageResult.createFailed(messageEntry);
    }

    private MessageResult connect(MessageEntry messageEntry, String breakerName, ObisCode obis) throws IOException {
        getLogger().severe("Connecting " + breakerName + " in AS300D ...");
        session.getCosemObjectFactory().getDisconnector(obis).remoteReconnect();
        getLogger().severe(breakerName + " successfully reconnected.");
        return MessageResult.createSuccess(messageEntry);
    }

    private Logger getLogger() {
        return session.getLogger();
    }

    private MessageResult disconnect(MessageEntry messageEntry, String breakerName, ObisCode obis) throws IOException {
        getLogger().severe("Disconnecting " + breakerName + " in AS300D ...");
        session.getCosemObjectFactory().getDisconnector(obis).remoteDisconnect();
        getLogger().severe(breakerName + " successfully disconnected.");
        return MessageResult.createSuccess(messageEntry);
    }

    protected boolean isMessageTag(String tag, MessageEntry messageEntry) {
        return (messageEntry.getContent().indexOf("<" + tag) >= 0);
    }

}
