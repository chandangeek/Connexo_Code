/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.elster.jupiter.nls.Thesaurus;

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

    Supplier<FaultMessage> createMeterConfigFaultMessageSupplier(MessageSeeds messageSeed, Object... args) {
        return () -> createMeterConfigFaultMessage(replyTypeFactory.failureReplyType(messageSeed, args));
    }

    FaultMessage createMeterConfigFaultMessage(String message, String errorCode) {
        return createMeterConfigFaultMessage(replyTypeFactory.failureReplyType(message, errorCode));
    }

    FaultMessage createMeterConfigFaultMessage(String message) {
        return createMeterConfigFaultMessage(message, null);
    }

    private FaultMessage createMeterConfigFaultMessage(ReplyType replyType) {
        MeterConfigFaultMessageType faultMessageType = meterConfigMessageObjectFactory.createMeterConfigFaultMessageType();
        faultMessageType.setReply(replyType);
        return new FaultMessage(MessageSeeds.UNABLE_TO_CREATE_DEVICE.translate(thesaurus), faultMessageType);
    }
}
