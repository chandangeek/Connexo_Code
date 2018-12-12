/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.getenddeviceevents;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;

import ch.iec.tc57._2011.getenddeviceevents.FaultMessage;
import ch.iec.tc57._2011.getenddeviceeventsmessage.EndDeviceEventsFaultMessageType;
import ch.iec.tc57._2011.getenddeviceeventsmessage.ObjectFactory;
import ch.iec.tc57._2011.schema.message.ReplyType;

import javax.inject.Inject;
import java.util.function.Supplier;

public class EndDeviceEventsFaultMessageFactory {
    private final ObjectFactory messageFactory = new ObjectFactory();
    private final ReplyTypeFactory replyTypeFactory;
    private final Thesaurus thesaurus;

    @Inject
    public EndDeviceEventsFaultMessageFactory(Thesaurus thesaurus, ReplyTypeFactory replyTypeFactory) {
        this.thesaurus = thesaurus;
        this.replyTypeFactory = replyTypeFactory;
    }

    Supplier<FaultMessage> createEndDeviceEventsFaultMessageSupplier(MessageSeeds messageSeed, Object... args) {
        return () -> createEndDeviceEventsFaultMessage(replyTypeFactory.failureReplyType(messageSeed, args));
    }

    FaultMessage createEndDeviceEventsFaultMessage(String message, String errorCode) {
        return createEndDeviceEventsFaultMessage(replyTypeFactory.failureReplyType(message, errorCode));
    }

    FaultMessage createEndDeviceEventsFaultMessage(String message) {
        return createEndDeviceEventsFaultMessage(message, null);
    }

    FaultMessage createEndDeviceEventsFaultMessage(MessageSeeds messageSeed) {
        return createEndDeviceEventsFaultMessage(messageSeed.translate(thesaurus), messageSeed.getErrorCode());
    }

    private FaultMessage createEndDeviceEventsFaultMessage(ReplyType replyType) {
        EndDeviceEventsFaultMessageType faultMessageType = messageFactory.createEndDeviceEventsFaultMessageType();
        faultMessageType.setReply(replyType);
        return new FaultMessage(MessageSeeds.UNABLE_TO_GET_END_DEVICE_EVENTS.translate(thesaurus), faultMessageType);
    }
}