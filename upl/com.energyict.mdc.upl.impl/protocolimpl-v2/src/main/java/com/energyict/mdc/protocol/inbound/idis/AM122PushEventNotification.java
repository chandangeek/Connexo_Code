package com.energyict.mdc.protocol.inbound.idis;

import com.energyict.mdc.protocol.inbound.g3.EventPushNotificationParser;
import com.energyict.mdc.protocol.inbound.g3.PushEventNotification;

public class AM122PushEventNotification extends PushEventNotification {

    @Override
    protected EventPushNotificationParser getEventPushNotificationParser() {
        if (parser == null) {
            parser = new AM122PushEventNotificationParser(comChannel, getContext());
        }
        return parser;
    }

    @Override
    public String getVersion() {
        return "$Date: 2022-10-12$";
    }

}
