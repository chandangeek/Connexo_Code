package com.energyict.protocolimpl.dlms.elster.as300d.messaging;

import com.energyict.dlms.DlmsSession;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.messaging.MessageCategorySpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 27/02/12
 * Time: 13:57
 */
public class AS300DMessaging {

    private final DlmsSession session;
    private final DisconnectControl disconnectControl;

    public AS300DMessaging(DlmsSession session) {
        this.session = session;
        this.disconnectControl = new DisconnectControl(session);
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        if (disconnectControl.canHandle(messageEntry)) {
            return disconnectControl.execute(messageEntry);
        }
        session.getLogger().severe("Unable to handle message [" + messageEntry.getContent() + "]!");
        return MessageResult.createFailed(messageEntry);
    }

    public static List<MessageCategorySpec> getMessageCategories() {
        List<MessageCategorySpec> specs = new ArrayList<MessageCategorySpec>();
        specs.add(DisconnectControl.getCategorySpec());
        return specs;
    }

}
