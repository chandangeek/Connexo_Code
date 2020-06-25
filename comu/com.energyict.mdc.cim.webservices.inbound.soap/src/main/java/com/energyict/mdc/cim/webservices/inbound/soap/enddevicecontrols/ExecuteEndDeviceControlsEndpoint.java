/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.enddevicecontrols;

import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.ReplyTypeFactory;

import ch.iec.tc57._2011.enddevicecontrolsmessage.EndDeviceControlsRequestMessageType;
import ch.iec.tc57._2011.enddevicecontrolsmessage.EndDeviceControlsResponseMessageType;
import ch.iec.tc57._2011.executeenddevicecontrols.EndDeviceControlsPort;
import ch.iec.tc57._2011.executeenddevicecontrols.FaultMessage;
import ch.iec.tc57._2011.schema.message.HeaderType;

import javax.inject.Inject;

public class ExecuteEndDeviceControlsEndpoint extends AbstractInboundEndPoint implements EndDeviceControlsPort, ApplicationSpecific {
    private static final String NOUN = "EndDeviceControls";

    private final ReplyTypeFactory replyTypeFactory;

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory
            = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.enddevicecontrolsmessage.ObjectFactory endDeviceControlsMessageObjectFactory
            = new ch.iec.tc57._2011.enddevicecontrolsmessage.ObjectFactory();

    @Inject
    ExecuteEndDeviceControlsEndpoint(ReplyTypeFactory replyTypeFactory) {
       this.replyTypeFactory = replyTypeFactory;
    }

    @Override
    public EndDeviceControlsResponseMessageType createEndDeviceControls(EndDeviceControlsRequestMessageType createEndDeviceControlsRequestMessage)
            throws FaultMessage {
        return runInTransactionWithOccurrence(() -> {
            // TODO: stub

            return createResponseMessage(HeaderType.Verb.CREATED);
        });
    }

    @Override
    public EndDeviceControlsResponseMessageType changeEndDeviceControls(EndDeviceControlsRequestMessageType changeEndDeviceControlsRequestMessage)
            throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public EndDeviceControlsResponseMessageType cancelEndDeviceControls(EndDeviceControlsRequestMessageType cancelEndDeviceControlsRequestMessage)
            throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public EndDeviceControlsResponseMessageType closeEndDeviceControls(EndDeviceControlsRequestMessageType closeEndDeviceControlsRequestMessage)
            throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public EndDeviceControlsResponseMessageType deleteEndDeviceControls(EndDeviceControlsRequestMessageType deleteEndDeviceControlsRequestMessage)
            throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private EndDeviceControlsResponseMessageType createResponseMessage(HeaderType.Verb verb) {
        EndDeviceControlsResponseMessageType responseMessage = endDeviceControlsMessageObjectFactory.createEndDeviceControlsResponseMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(verb);
        //header.setCorrelationID(correlationId);

        responseMessage.setHeader(header);

        responseMessage.setReply(replyTypeFactory.okReplyType());
        return responseMessage;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }
}
