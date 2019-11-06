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
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.ReplyType;

import javax.inject.Inject;

import java.util.List;
import java.util.function.Supplier;

public class MeterConfigFaultMessageFactory {
    private final ObjectFactory meterConfigMessageObjectFactory = new ObjectFactory();

    private final Thesaurus thesaurus;
    private final ReplyTypeFactory replyTypeFactory;

    @Inject
    public MeterConfigFaultMessageFactory(Thesaurus thesaurus, ReplyTypeFactory replyTypeFactory) {
        this.thesaurus = thesaurus;
        this.replyTypeFactory = replyTypeFactory;
    }

    public Supplier<FaultMessage> meterConfigFaultMessageSupplier(String meterName, MessageSeeds messageSeed, Object... args) {
        return () -> meterConfigFaultMessage(messageSeed.translate(thesaurus,args), replyTypeFactory.failureReplyType(meterName, messageSeed, args));
    }

    private FaultMessage meterConfigFaultMessage(String message, ReplyType replyType) {  // CONM-842
        MeterConfigFaultMessageType faultMessageType = meterConfigMessageObjectFactory.createMeterConfigFaultMessageType();
        faultMessageType.setReply(replyType);
        return new FaultMessage(message, faultMessageType);
    }

    FaultMessage meterConfigFaultMessage(String meterName, MessageSeeds messageSeed, String message, String errorCode) {
        return meterConfigFaultMessage(messageSeed, replyTypeFactory.failureReplyType(meterName, message, errorCode));
    }

    FaultMessage meterConfigFaultMessage(String meterName, MessageSeeds messageSeed, String message) {
        return meterConfigFaultMessage(meterName, messageSeed, message, messageSeed.getErrorCode());
    }

    FaultMessage meterConfigFaultMessage(String meterName, MessageSeeds messageSeed, MessageSeeds message) {
        return meterConfigFaultMessage(meterName, messageSeed, message.translate(thesaurus), message.getErrorCode());
    }

    private FaultMessage meterConfigFaultMessage(MessageSeeds messageSeed, ReplyType replyType) {
        MeterConfigFaultMessageType faultMessageType = meterConfigMessageObjectFactory.createMeterConfigFaultMessageType();
        faultMessageType.setReply(replyType);
        return new FaultMessage(messageSeed.translate(thesaurus), faultMessageType);
    }

    FaultMessage meterConfigFaultMessage(List<FaultMessage> faults) {
        StringBuilder message = new StringBuilder();
        if(faults.size()==2 && (faults.get(0).getMessage().contains("'Card format'") && faults.get(1).getMessage().contains("'Status'"))){
            faults.forEach(faultMessage -> message.append(faultMessage.getMessage()));
        }else{
            message.append(faults.get(0).getMessage());
        }

        return meterConfigFaultMessage(message.toString(), faults, ReplyType.Result.FAILED);
    }

    FaultMessage meterConfigFaultMessage(MessageSeeds message, List<FaultMessage> faults, ReplyType.Result result) {
        return meterConfigFaultMessage(message.translate(thesaurus), faults, result);
    }

    private FaultMessage meterConfigFaultMessage(String message, List<FaultMessage> faults, ReplyType.Result result) {
        MeterConfigFaultMessageType faultMessageType = meterConfigMessageObjectFactory.createMeterConfigFaultMessageType();
        ErrorType[] errorTypes = faults.stream().flatMap(fault -> fault.getFaultInfo().getReply().getError().stream()).toArray(ErrorType[]::new);
        ReplyType replyType = replyTypeFactory.failureReplyType(result, errorTypes);
        faultMessageType.setReply(replyType);
        return new FaultMessage(message, faultMessageType);
    }
}