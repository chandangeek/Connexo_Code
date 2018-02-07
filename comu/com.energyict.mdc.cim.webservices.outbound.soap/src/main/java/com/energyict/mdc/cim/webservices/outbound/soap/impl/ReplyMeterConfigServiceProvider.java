/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.impl;

import com.elster.jupiter.fsm.StateTransitionWebServiceClient;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.cim.webservices.inbound.soap.ReplyMeterConfigWebService;
import com.energyict.mdc.cim.webservices.outbound.soap.meterconfig.MeterConfigFactory;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigEventMessageType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigPayloadType;
import ch.iec.tc57._2011.replymeterconfig.FaultMessage;
import ch.iec.tc57._2011.replymeterconfig.MeterConfigPort;
import ch.iec.tc57._2011.replymeterconfig.ReplyMeterConfig;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import org.apache.cxf.jaxws.JaxWsClientProxy;
import org.apache.cxf.message.Message;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.xml.ws.Service;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.replymeterconfig.provider",
        service = {ReplyMeterConfigWebService.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + ReplyMeterConfigWebService.NAME})
public class ReplyMeterConfigServiceProvider implements ReplyMeterConfigWebService, OutboundSoapEndPointProvider {

    private final String RESOURCE_WSDL = "/meterconfig/ReplyMeterConfig.wsdl";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.meterconfigmessage.ObjectFactory meterConfigMessageObjectFactory = new ch.iec.tc57._2011.meterconfigmessage.ObjectFactory();
    private final List<MeterConfigPort> stateMeterConfigPortServices = new ArrayList<>();
    private final MeterConfigFactory meterConfigFactory = new MeterConfigFactory();

    private volatile DeviceService deviceService;

    public ReplyMeterConfigServiceProvider() {
        // for OSGI purposes
    }

    @Inject
    public ReplyMeterConfigServiceProvider(DeviceService deviceService) {
        this();
        setDeviceService(deviceService);
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

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
        return new ReplyMeterConfig(this.getClass().getResource(RESOURCE_WSDL));
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
    public void call(EndPointConfiguration endPointConfiguration, List<Device> successfulDevices, Map<String, String> failedDevices) {
        try {
            stateMeterConfigPortServices.stream()
                    .filter(meterConfigPort -> isValidReplyMeterConfigService(meterConfigPort, endPointConfiguration.getUrl()))
                    .findFirst()
                    .ifPresent(meterConfigPortService -> {
                        try {
                            meterConfigPortService.changedMeterConfig(createResponseMessage(successfulDevices, failedDevices));
                        } catch (FaultMessage faultMessage) {
                            endPointConfiguration.log(faultMessage.getMessage(), faultMessage);
                        }
                    });
        } catch (RuntimeException ex) {
            endPointConfiguration.log(LogLevel.SEVERE, ex.getMessage());
        }
    }

    private MeterConfigEventMessageType createResponseMessage(List<Device> successfulDevices, Map<String, String> failedDevices) {
        MeterConfigEventMessageType meterConfigEventMessageType = new MeterConfigEventMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(getWebServiceName());
        header.setVerb(HeaderType.Verb.REPLY);
        meterConfigEventMessageType.setHeader(header);

        // set reply
        ReplyType replyType = cimMessageObjectFactory.createReplyType();
        replyType.setResult(ReplyType.Result.OK);

        // set errors
        failedDevices.entrySet().stream().forEach(entry -> {
            ErrorType errorType = new ErrorType();
            errorType.setCode(entry.getKey());
            errorType.setDetails(entry.getValue());
            replyType.getError().add(errorType);
        });

        meterConfigEventMessageType.setReply(replyType);

        // set payload
        MeterConfigPayloadType payloadType = meterConfigMessageObjectFactory.createMeterConfigPayloadType();
        meterConfigEventMessageType.setPayload(payloadType);
        MeterConfig meterConfig = meterConfigFactory.asMeterConfig(successfulDevices);
        payloadType.setMeterConfig(meterConfig);
        meterConfigEventMessageType.setPayload(payloadType);

        return meterConfigEventMessageType;
    }

    private boolean isValidReplyMeterConfigService(MeterConfigPort meterConfigPort, String url) throws RuntimeException {
        return url.contains((String) ((JaxWsClientProxy) (Proxy.getInvocationHandler(stateMeterConfigPortServices.get(0)))).getRequestContext().get(Message.ENDPOINT_ADDRESS));
    }
}