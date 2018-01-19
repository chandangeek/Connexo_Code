/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.impl;

import com.elster.jupiter.fsm.StateTransitionWebServiceClient;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ch.iec.tc57._2011.meterconfigmessage.MeterConfigEventMessageType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigPayloadType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigResponseMessageType;
import ch.iec.tc57._2011.replymeterconfig.FaultMessage;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import ch.iec.tc57._2011.replymeterconfig.MeterConfigPort;
import ch.iec.tc57._2011.replymeterconfig.ReplyMeterConfig;

import javax.xml.ws.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.enddeviceconfig.provider",
        service = {StateTransitionWebServiceClient.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + StateTransitionWebServiceClient.NAME})
public class EndDeviceConfigServiceProvider implements StateTransitionWebServiceClient, OutboundSoapEndPointProvider {

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.meterconfigmessage.ObjectFactory meterConfigMessageObjectFactory = new ch.iec.tc57._2011.meterconfigmessage.ObjectFactory();

    private final List<MeterConfigPort> stateMeterConfigPortServices = new ArrayList<>();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterConfigPortService(MeterConfigPort meterConfigPort) {
        stateMeterConfigPortServices.add(meterConfigPort);
    }

    public void removeMeterConfigPortService(MeterConfigPort meterConfigPort) {
        stateMeterConfigPortServices.remove(meterConfigPort);
    }

    public List<MeterConfigPort> getStateTransitionWebServiceClients() {
        return Collections.unmodifiableList(this.stateMeterConfigPortServices);
    }

    @Override
    public Service get() {
        return new ReplyMeterConfig();
    }

    @Override
    public Class getService() {
        return MeterConfigPort.class;
    }

    @Override
    public String getWebServiceName() {
        return StateTransitionWebServiceClient.NAME;
    }

    @Override
    public boolean call(long id, EndPointConfiguration endPointConfiguration) {
        MeterConfigResponseMessageType meterConfigResponseMessageType = null;
        try {
            MeterConfigEventMessageType meterConfigEventMessageType = new MeterConfigEventMessageType();
            HeaderType header = cimMessageObjectFactory.createHeaderType();
            header.setNoun(getWebServiceName());
            header.setVerb(HeaderType.Verb.CHANGED);
            meterConfigEventMessageType.setHeader(header);

            MeterConfigPayloadType meterConfigPayloadType = new MeterConfigPayloadType();
            meterConfigEventMessageType.setPayload(meterConfigPayloadType);

            ReplyType replyType = cimMessageObjectFactory.createReplyType();
            replyType.setResult(ReplyType.Result.OK);
            meterConfigEventMessageType.setReply(replyType);

            meterConfigResponseMessageType = stateMeterConfigPortServices.get(0).createdMeterConfig(meterConfigEventMessageType);
        } catch (FaultMessage faultMessage) {
            faultMessage.printStackTrace();
        }
        return meterConfigResponseMessageType == null;
    }
}