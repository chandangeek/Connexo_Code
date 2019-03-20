/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.meterreadings;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.elster.jupiter.nls.Thesaurus;

import ch.iec.tc57._2011.getmeterreadings.FaultMessage;
import ch.iec.tc57._2011.getmeterreadingsmessage.MeterReadingsFaultMessageType;
import ch.iec.tc57._2011.getmeterreadingsmessage.ObjectFactory;
import ch.iec.tc57._2011.schema.message.ReplyType;

import javax.inject.Inject;
import java.util.function.Supplier;

public class MeterReadingFaultMessageFactory {

    private final ObjectFactory meterReadingMessageObjectFactory = new ObjectFactory();

    private final Thesaurus thesaurus;
    private final ReplyTypeFactory replyTypeFactory;

    @Inject
    public MeterReadingFaultMessageFactory(Thesaurus thesaurus, ReplyTypeFactory replyTypeFactory) {
        this.thesaurus = thesaurus;
        this.replyTypeFactory = replyTypeFactory;
    }

    public Supplier<FaultMessage> createMeterReadingFaultMessageSupplier(MessageSeeds messageSeed, Object... args) {
        return () -> createMeterReadingFaultMessage(replyTypeFactory.failureReplyType(messageSeed, args));
    }

    FaultMessage createMeterReadingFaultMessage(String message, String errorCode) {
        return createMeterReadingFaultMessage(replyTypeFactory.failureReplyType(message, errorCode));
    }

    FaultMessage createMeterReadingFaultMessage(String message) {
        return createMeterReadingFaultMessage(message, null);
    }

    private FaultMessage createMeterReadingFaultMessage(ReplyType replyType) {
        MeterReadingsFaultMessageType faultMessageType = meterReadingMessageObjectFactory.createMeterReadingsFaultMessageType();
        faultMessageType.setReply(replyType);
        return new FaultMessage(MessageSeeds.UNABLE_TO_GET_READINGS.translate(thesaurus), faultMessageType);
    }
}