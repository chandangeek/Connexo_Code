package com.energyict.mdc.protocol.inbound.idis;

import com.energyict.mdc.protocol.inbound.g3.EventPushNotificationParser;
import com.energyict.mdc.protocol.inbound.g3.PushEventNotification;

public class AECPushEventNotification extends PushEventNotification {
    @Override
    protected EventPushNotificationParser getEventPushNotificationParser() {
        if (parser == null) {
            parser = new AECPushEventNotificationParser(comChannel, getContext());
        }
        return parser;
    }

    @Override
    // TODO
    public String getVersion() {
        return "$Date: 2021-09-11$";
    }
}
