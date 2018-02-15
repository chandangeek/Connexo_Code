/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.meterconfig;

import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.cim.webservices.inbound.soap.OperationEnum;
import com.energyict.mdc.cim.webservices.inbound.soap.ReplyMeterConfigWebService;
import com.energyict.mdc.device.data.Device;

import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigEventMessageType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigPayloadType;
import ch.iec.tc57._2011.replymeterconfig.FaultMessage;
import ch.iec.tc57._2011.replymeterconfig.MeterConfigPort;
import ch.iec.tc57._2011.replymeterconfig.ReplyMeterConfig;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.replymeterconfig.provider",
        service = {ReplyMeterConfigWebService.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + ReplyMeterConfigWebService.NAME})
public class ReplyMeterConfigServiceProvider implements ReplyMeterConfigWebService, OutboundSoapEndPointProvider {

    private static final String NOUN = "ReplyMeterConfig";
    private static final String RESOURCE_WSDL = "/meterconfig/ReplyMeterConfig.wsdl";

    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.meterconfigmessage.ObjectFactory meterConfigMessageObjectFactory = new ch.iec.tc57._2011.meterconfigmessage.ObjectFactory();

    private final List<MeterConfigPort> meterConfigPorts = new ArrayList<>();
    private final MeterConfigFactory meterConfigFactory = new MeterConfigFactory();

    public ReplyMeterConfigServiceProvider() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterConfigPort(MeterConfigPort meterConfigPort) {
        meterConfigPorts.add(meterConfigPort);
    }

    public void removeMeterConfigPort(MeterConfigPort meterConfigPort) {
        meterConfigPorts.remove(meterConfigPort);
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
    public void call(EndPointConfiguration endPointConfiguration, OperationEnum operation,
                     List<Device> successfulDevices, Map<String, String> failedDevices, BigDecimal expectedNumberOfCalls) {
        try {
            meterConfigPorts.stream()
                    .forEach(meterConfigPortService -> {
                        try {
                            switch (operation) {
                                case CREATE:
                                    meterConfigPortService.createdMeterConfig(createResponseMessage(successfulDevices, failedDevices, expectedNumberOfCalls));
                                    break;
                                case UPDATE:
                                    meterConfigPortService.changedMeterConfig(createResponseMessage(successfulDevices, failedDevices, expectedNumberOfCalls));
                                    break;
                            }
                        } catch (FaultMessage faultMessage) {
                            endPointConfiguration.log(faultMessage.getMessage(), faultMessage);
                        }
                    });
        } catch (RuntimeException ex) {
            endPointConfiguration.log(LogLevel.SEVERE, ex.getMessage());
        }
    }

    private MeterConfigEventMessageType createResponseMessage(List<Device> successfulDevices, Map<String, String> failedDevices, BigDecimal expectedNumberOfCalls) {
        MeterConfigEventMessageType meterConfigEventMessageType = new MeterConfigEventMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(HeaderType.Verb.REPLY);
        meterConfigEventMessageType.setHeader(header);

        // set reply
        ReplyType replyType = cimMessageObjectFactory.createReplyType();
        if (expectedNumberOfCalls.compareTo(BigDecimal.valueOf(successfulDevices.size())) == 0) {
            replyType.setResult(ReplyType.Result.OK);
        } else if (expectedNumberOfCalls.compareTo(BigDecimal.valueOf(failedDevices.size())) == 0) {
            replyType.setResult(ReplyType.Result.FAILED);
        } else {
            replyType.setResult(ReplyType.Result.PARTIAL);
        }

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
}