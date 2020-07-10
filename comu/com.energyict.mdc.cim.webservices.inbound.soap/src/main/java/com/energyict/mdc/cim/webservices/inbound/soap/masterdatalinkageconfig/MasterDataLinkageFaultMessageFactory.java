/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.masterdatalinkageconfig;

import com.elster.jupiter.nls.Thesaurus;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;

import ch.iec.tc57._2011.executemasterdatalinkageconfig.FaultMessage;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.MasterDataLinkageConfigFaultMessageType;
import ch.iec.tc57._2011.masterdatalinkageconfigmessage.ObjectFactory;
import ch.iec.tc57._2011.schema.message.ReplyType;

import javax.inject.Inject;

import java.util.function.Supplier;

public class MasterDataLinkageFaultMessageFactory {
    private final ObjectFactory masterDataLinkageMessageObjectFactory = new ObjectFactory();

    private final Thesaurus thesaurus;
    private final ReplyTypeFactory replyTypeFactory;

    @Inject
    MasterDataLinkageFaultMessageFactory(Thesaurus thesaurus, ReplyTypeFactory replyTypeFactory) {
        this.thesaurus = thesaurus;
        this.replyTypeFactory = replyTypeFactory;
    }

    Supplier<FaultMessage> createMasterDataLinkageFaultMessageSupplier(MasterDataLinkageAction action,
            MessageSeeds messageSeed, Object... args) {
        return () -> createMasterDataLinkageFaultMessage(action, messageSeed, args);
    }

    public FaultMessage createMasterDataLinkageFaultMessage(MasterDataLinkageAction action, MessageSeeds messageSeed,
            Object... args) {
        return createMasterDataLinkageFaultMessage(action, replyTypeFactory.failureReplyType(messageSeed, args));
    }

    FaultMessage createMasterDataLinkageFaultMessage(MasterDataLinkageAction action, String message, String errorCode) {
        return createMasterDataLinkageFaultMessage(action, replyTypeFactory.failureReplyType(message, errorCode));
    }

    FaultMessage createMasterDataLinkageFaultMessage(MasterDataLinkageAction action, String message) {
        return createMasterDataLinkageFaultMessage(action, message, null);
    }

    private FaultMessage createMasterDataLinkageFaultMessage(MasterDataLinkageAction action, ReplyType replyType) {
        MasterDataLinkageConfigFaultMessageType faultMessageType = masterDataLinkageMessageObjectFactory
                .createMasterDataLinkageConfigFaultMessageType();
        faultMessageType.setReply(replyType);
        return new FaultMessage(action.getBasicSeed().translate(thesaurus), faultMessageType);
    }
}
