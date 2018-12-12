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

    Supplier<FaultMessage> endDeviceEventsFaultMessageSupplier(MessageSeeds messageSeed, Object... args) {
        return () -> endDeviceEventsFaultMessage(messageSeed, replyTypeFactory.failureReplyType(messageSeed, args));
    }

    FaultMessage endDeviceEventsFaultMessage(MessageSeeds messageSeed, String message, String errorCode) {
        return endDeviceEventsFaultMessage(messageSeed, replyTypeFactory.failureReplyType(message, errorCode));
    }

    FaultMessage endDeviceEventsFaultMessage(MessageSeeds basicMessage, String message) {
        return endDeviceEventsFaultMessage(basicMessage, message, null);
    }

    private FaultMessage endDeviceEventsFaultMessage(MessageSeeds basicMessage, ReplyType replyType) {
        EndDeviceEventsFaultMessageType faultMessageType = messageFactory.createEndDeviceEventsFaultMessageType();
        faultMessageType.setReply(replyType);
        return new FaultMessage(basicMessage.translate(thesaurus), faultMessageType);
    }
}