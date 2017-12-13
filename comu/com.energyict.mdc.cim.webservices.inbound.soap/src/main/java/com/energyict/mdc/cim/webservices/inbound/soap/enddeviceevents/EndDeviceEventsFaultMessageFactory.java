/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddeviceevents;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;

import ch.iec.tc57._2011.getenddeviceevents.FaultMessage;
import ch.iec.tc57._2011.getenddeviceeventsmessage.EndDeviceEventsFaultMessageType;
import ch.iec.tc57._2011.getenddeviceeventsmessage.ObjectFactory;
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

    Supplier<FaultMessage> getEndDeviceEventsFaultMessageSupplier(MessageSeeds messageSeed, Object... args) {
        return () -> getEndDeviceEventsFaultMessage(replyTypeFactory.failureReplyType(messageSeed, args));
    }

    FaultMessage getEndDeviceEventsFaultMessage(String message, String errorCode) {
        return getEndDeviceEventsFaultMessage(replyTypeFactory.failureReplyType(message, errorCode));
    }

    FaultMessage getEndDeviceEventsFaultMessage(String message) {
        return getEndDeviceEventsFaultMessage(message, null);
    }

    private FaultMessage getEndDeviceEventsFaultMessage(ReplyType replyType) {
        EndDeviceEventsFaultMessageType faultMessageType = messageFactory.createEndDeviceEventsFaultMessageType();
        faultMessageType.setReply(replyType);
        return new FaultMessage(MessageSeeds.UNABLE_TO_GET_END_DEVICE_EVENTS.translate(thesaurus), faultMessageType);
    }
}