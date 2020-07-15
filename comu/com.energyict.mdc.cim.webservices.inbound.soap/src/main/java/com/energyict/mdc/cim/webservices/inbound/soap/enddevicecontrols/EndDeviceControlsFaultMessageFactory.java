/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddevicecontrols;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;

import ch.iec.tc57._2011.enddevicecontrolsmessage.EndDeviceControlsFaultMessageType;
import ch.iec.tc57._2011.executeenddevicecontrols.FaultMessage;
import ch.iec.tc57._2011.enddevicecontrolsmessage.ObjectFactory;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.ReplyType;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Supplier;

public class EndDeviceControlsFaultMessageFactory {

    private final ObjectFactory messageObjectFactory = new ObjectFactory();
    private final Thesaurus thesaurus;
    private final ReplyTypeFactory replyTypeFactory;

    @Inject
    public EndDeviceControlsFaultMessageFactory(Thesaurus thesaurus, ReplyTypeFactory replyTypeFactory) {
        this.thesaurus = thesaurus;
        this.replyTypeFactory = replyTypeFactory;
    }

    public Supplier<? extends FaultMessage> edcFaultMessageSupplier(MessageSeeds basicMessageSeed, List<ErrorType> errorTypes) {
        return () -> faultMessage(basicMessageSeed,
                replyTypeFactory.failureReplyType(ReplyType.Result.FAILED, errorTypes.stream().toArray(ErrorType[]::new)));
    }

    FaultMessage edcFaultMessage(MessageSeeds basicMessageSeed, String message, String errorCode) {
        return faultMessage(basicMessageSeed, replyTypeFactory.failureReplyType(message, errorCode));
    }

    FaultMessage edcFaultMessage(MessageSeeds basicMessageSeed, String message) {
        return edcFaultMessage(basicMessageSeed, message, null);
    }

    private FaultMessage faultMessage(MessageSeeds basicMessageSeed, ReplyType replyType) {
        EndDeviceControlsFaultMessageType faultMessageType = messageObjectFactory.createEndDeviceControlsFaultMessageType();
        faultMessageType.setReply(replyType);
        return new FaultMessage(basicMessageSeed.translate(thesaurus), faultMessageType);
    }
}