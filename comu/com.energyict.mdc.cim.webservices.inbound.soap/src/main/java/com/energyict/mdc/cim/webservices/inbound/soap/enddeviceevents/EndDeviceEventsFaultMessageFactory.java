/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddeviceevents;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;

import ch.iec.tc57._2011.enddeviceeventsmessage.EndDeviceEventsFaultMessageType;
import ch.iec.tc57._2011.enddeviceeventsmessage.ObjectFactory;
import ch.iec.tc57._2011.receiveenddeviceevents.FaultMessage;
import ch.iec.tc57._2011.schema.message.ReplyType;

import javax.inject.Inject;
import java.util.function.Supplier;

class EndDeviceEventsFaultMessageFactory {
    private final ObjectFactory messageFactory = new ObjectFactory();
    private final ReplyTypeFactory replyTypeFactory;
    private final Thesaurus thesaurus;

    @Inject
    EndDeviceEventsFaultMessageFactory(Thesaurus thesaurus, ReplyTypeFactory replyTypeFactory) {
        this.thesaurus = thesaurus;
        this.replyTypeFactory = replyTypeFactory;
    }

    Supplier<FaultMessage> createEndDeviceEventsFaultMessageSupplier(MessageSeeds messageSeed, Object... args) {
        return () -> createEndDeviceEventsFaultMessage(MessageSeeds.INVALID_CREATED_END_DEVICE_EVENTS, replyTypeFactory.failureReplyType(messageSeed, args));
    }

    Supplier<FaultMessage> createEndDeviceEventsFaultMessageSupplier(MessageSeeds basicMessage, MessageSeeds messageSeed, Object... args) {
        return () -> createEndDeviceEventsFaultMessage(basicMessage, replyTypeFactory.failureReplyType(messageSeed, args));
    }

    FaultMessage createEndDeviceEventsFaultMessage(MessageSeeds basicMessage, String message, String errorCode) {
        return createEndDeviceEventsFaultMessage(basicMessage, replyTypeFactory.failureReplyType(message, errorCode));
    }

    FaultMessage createEndDeviceEventsFaultMessage(MessageSeeds basicMessage, String message) {
        return createEndDeviceEventsFaultMessage(basicMessage, message, null);
    }

    private FaultMessage createEndDeviceEventsFaultMessage(MessageSeeds basicMessage, ReplyType replyType) {
        EndDeviceEventsFaultMessageType faultMessageType = messageFactory.createEndDeviceEventsFaultMessageType();
        faultMessageType.setReply(replyType);
        return new FaultMessage(basicMessage.translate(thesaurus), faultMessageType);
    }
}