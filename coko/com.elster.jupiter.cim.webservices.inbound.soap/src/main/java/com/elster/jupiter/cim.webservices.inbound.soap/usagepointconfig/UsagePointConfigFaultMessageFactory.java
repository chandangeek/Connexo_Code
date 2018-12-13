/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.elster.jupiter.nls.Thesaurus;

import ch.iec.tc57._2011.executeusagepointconfig.FaultMessage;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import ch.iec.tc57._2011.usagepointconfigmessage.ObjectFactory;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigFaultMessageType;

import javax.inject.Inject;
import java.util.function.Supplier;

class UsagePointConfigFaultMessageFactory {
    private final ObjectFactory usagePointConfigMessageObjectFactory = new ObjectFactory();

    private final Thesaurus thesaurus;
    private final ReplyTypeFactory replyTypeFactory;

    @Inject
    UsagePointConfigFaultMessageFactory(Thesaurus thesaurus, ReplyTypeFactory replyTypeFactory) {
        this.thesaurus = thesaurus;
        this.replyTypeFactory = replyTypeFactory;
    }

    Supplier<FaultMessage> usagePointConfigFaultMessageSupplier(MessageSeeds basicMessage, MessageSeeds messageSeed, Object... args) {
        return () -> usagePointConfigFaultMessage(basicMessage, replyTypeFactory.failureReplyType(messageSeed, args));
    }

    FaultMessage usagePointConfigFaultMessage(MessageSeeds basicMessage, String message, String errorCode) {
        return usagePointConfigFaultMessage(basicMessage, replyTypeFactory.failureReplyType(message, errorCode));
    }

    FaultMessage usagePointConfigFaultMessage(MessageSeeds basicMessage, String message) {
        return usagePointConfigFaultMessage(basicMessage, message, null);
    }

    FaultMessage usagePointConfigFaultMessage(MessageSeeds basicMessage, ErrorType... errors) {
        ReplyType reply = replyTypeFactory.failureReplyType(ReplyType.Result.FAILED, errors);
        return usagePointConfigFaultMessage(basicMessage, reply);
    }

    private FaultMessage usagePointConfigFaultMessage(MessageSeeds basicMessage, ReplyType replyType) {
        UsagePointConfigFaultMessageType faultMessageType = usagePointConfigMessageObjectFactory.createUsagePointConfigFaultMessageType();
        faultMessageType.setReply(replyType);
        return new FaultMessage(basicMessage.translate(thesaurus), faultMessageType);
    }
}
