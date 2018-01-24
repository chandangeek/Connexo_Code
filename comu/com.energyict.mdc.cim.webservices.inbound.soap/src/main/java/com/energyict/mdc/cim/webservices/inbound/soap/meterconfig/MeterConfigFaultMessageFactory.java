/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigFaultMessageType;
import ch.iec.tc57._2011.meterconfigmessage.ObjectFactory;
import ch.iec.tc57._2011.schema.message.ReplyType;

import javax.inject.Inject;
import java.util.function.Supplier;

class MeterConfigFaultMessageFactory {
    private final ObjectFactory meterConfigMessageObjectFactory = new ObjectFactory();

    private final Thesaurus thesaurus;
    private final ReplyTypeFactory replyTypeFactory;

    @Inject
    MeterConfigFaultMessageFactory(Thesaurus thesaurus, ReplyTypeFactory replyTypeFactory) {
        this.thesaurus = thesaurus;
        this.replyTypeFactory = replyTypeFactory;
    }

    Supplier<FaultMessage> meterConfigFaultMessageSupplier(MessageSeeds messageSeed, Object... args) {
        return () -> meterConfigFaultMessage(messageSeed, replyTypeFactory.failureReplyType(messageSeed, args));
    }

    FaultMessage meterConfigFaultMessage(MessageSeeds messageSeed, String message, String errorCode) {
        return meterConfigFaultMessage(messageSeed, replyTypeFactory.failureReplyType(message, errorCode));
    }

    FaultMessage meterConfigFaultMessage(MessageSeeds messageSeed, String message) {
        return meterConfigFaultMessage(messageSeed, message, null);
    }

    private FaultMessage meterConfigFaultMessage(MessageSeeds messageSeed, ReplyType replyType) {
        MeterConfigFaultMessageType faultMessageType = meterConfigMessageObjectFactory.createMeterConfigFaultMessageType();
        faultMessageType.setReply(replyType);
        return new FaultMessage(messageSeed.translate(thesaurus), faultMessageType);
    }
}