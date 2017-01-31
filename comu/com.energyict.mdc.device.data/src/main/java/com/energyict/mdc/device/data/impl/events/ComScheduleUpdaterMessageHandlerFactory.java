/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.energyict.mdc.device.data.impl.Installer;

import org.osgi.service.component.annotations.Component;

@Component(name="com.energyict.mdc.device.data.comschedule.update.messagehandler",
        service = MessageHandlerFactory.class,
        property = {"subscriber="+Installer.COMSCHEDULE_RECALCULATOR_MESSAGING_NAME, "destination="+Installer.COMSCHEDULE_RECALCULATOR_MESSAGING_NAME},
        immediate = true)
public class ComScheduleUpdaterMessageHandlerFactory implements MessageHandlerFactory {
    private static final ComScheduleUpdatedMessageHandler MESSAGE_HANDLER = new ComScheduleUpdatedMessageHandler();
    @Override
    public MessageHandler newMessageHandler() {
        return MESSAGE_HANDLER;
    }
}
