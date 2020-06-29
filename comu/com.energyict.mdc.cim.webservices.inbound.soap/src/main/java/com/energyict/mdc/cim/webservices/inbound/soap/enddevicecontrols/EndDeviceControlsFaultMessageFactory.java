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

    //createEndDeviceControls
    public Supplier<FaultMessage> createEDCFaultMessageSupplier(MessageSeeds messageSeed, Object... args) {
        return () -> createFaultMessage(MessageSeeds.UNABLE_TO_CREATE_END_DEVICE_CONTROLS, replyTypeFactory.failureReplyType(messageSeed, args));
    }


    public Supplier<? extends FaultMessage> createEDCFaultMessageSupplier(List<ErrorType> errorTypes) {
        return () -> createFaultMessage(MessageSeeds.UNABLE_TO_CREATE_END_DEVICE_CONTROLS,
                replyTypeFactory.failureReplyType(ReplyType.Result.FAILED, errorTypes.stream().toArray(ErrorType[]::new)));
    }

    FaultMessage createEDCFaultMessage(String message, String errorCode) {
        return createFaultMessage(MessageSeeds.UNABLE_TO_CREATE_END_DEVICE_CONTROLS, replyTypeFactory.failureReplyType(message, errorCode));
    }

    FaultMessage createEDCFaultMessage(String message) {
        return createEDCFaultMessage(message, null);
    }

    //changeEndDeviceControls
    //TODO: CXO-12371

    //cancelEndDeviceControls
    //TODO: CXO-12371

    private FaultMessage createFaultMessage(MessageSeeds messageSeed, ReplyType replyType) {
        EndDeviceControlsFaultMessageType faultMessageType = messageObjectFactory.createEndDeviceControlsFaultMessageType();
        faultMessageType.setReply(replyType);
        return new FaultMessage(messageSeed.translate(thesaurus), faultMessageType);
    }
}