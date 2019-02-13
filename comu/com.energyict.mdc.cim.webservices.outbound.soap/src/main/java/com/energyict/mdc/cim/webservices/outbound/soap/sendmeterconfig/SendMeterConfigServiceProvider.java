/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.outbound.soap.sendmeterconfig;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.energyict.mdc.cim.webservices.inbound.soap.FailedMeterOperation;
import com.energyict.mdc.cim.webservices.inbound.soap.SendMeterConfigService;
import com.energyict.mdc.device.data.Device;

import ch.iec.tc57._2011.getmeterconfigmessage.GetMeterConfigEventMessageType;
import ch.iec.tc57._2011.getmeterconfigmessage.GetMeterConfigPayloadType;
import ch.iec.tc57._2011.getmeterconfigmessage.ObjectFactory;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.Name;
import ch.iec.tc57._2011.schema.message.ObjectType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import ch.iec.tc57._2011.sendmeterconfig.FaultMessage;
import ch.iec.tc57._2011.sendmeterconfig.MeterConfigPort;
import ch.iec.tc57._2011.sendmeterconfig.SendMeterConfig;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.sendmeterconfig.provider",
        service = {SendMeterConfigService.class, OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=" + SendMeterConfigService.SEND_METER_CONFIG})
public class SendMeterConfigServiceProvider implements SendMeterConfigService, OutboundSoapEndPointProvider{

    private static final String NOUN = "SendMeterConfig";
    private final ObjectFactory getMeterConfigEventMessageObjectFactory = new ObjectFactory();
    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private volatile GetMeterConfigFactory getMeterConfigFactory;
    private final Map<String, MeterConfigPort> ports = new HashMap<>();

    private volatile Thesaurus thesaurus;

    public SendMeterConfigServiceProvider() {
        // for OSGI purposes
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addMeterConfigPort(MeterConfigPort port, Map<String, Object> properties) {
        Optional.ofNullable(properties)
                .map(property -> property.get(SendMeterConfigService.URL))
                .map(String.class::cast)
                .ifPresent(url -> ports.put(url, port));
    }

    public void removeMeterConfigPort(MeterConfigPort port) {
        ports.values().removeIf(entryPort -> port == entryPort);
    }

    @Reference
    public void setThesaurus(WebServiceActivator webServiceActivator) {
        this.thesaurus = webServiceActivator.getThesaurus();
    }

    @Reference
    public void addMeterConfigFactory(GetMeterConfigFactory getMeterConfigFactory) {
        this.getMeterConfigFactory = getMeterConfigFactory;
    }

    @Override
    public Service get() {
        return new SendMeterConfig(this.getClass().getResource(SendMeterConfigService.RESOURCE));
    }

    @Override
    public Class getService() {
        return MeterConfigPort.class;
    }

    @Override
    public void call(List<Device> successfulDevices, List<FailedMeterOperation> failedDevices, BigDecimal expectedNumberOfCalls, String url) {
        try {
            Optional.ofNullable(ports.get(url))
                    .orElseThrow(() -> new SendMeterConfigServiceException(thesaurus, MessageSeeds.NO_WEB_SERVICE_ENDPOINTS))
                    .sendMeterConfig(createResponseMessage(createMeterConfig(successfulDevices), failedDevices, expectedNumberOfCalls));
        } catch (FaultMessage faultMessage) {
            // TODO log
        }
    }

    private MeterConfig createMeterConfig(List<Device> devices) {
        MeterConfig meterConfig = getMeterConfigFactory.asMeterConfig(devices);
        return meterConfig;
    }

    private GetMeterConfigEventMessageType createResponseMessage(MeterConfig meterConfig, List<FailedMeterOperation> failedDevices, BigDecimal expectedNumberOfCalls) {
        GetMeterConfigEventMessageType responseMessage = getMeterConfigEventMessageObjectFactory.createGetMeterConfigEventMessageType();

        // set header
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(HeaderType.Verb.REPLY);
        header.setNoun(NOUN);
        responseMessage.setHeader(header);
        ReplyType replyType = cimMessageObjectFactory.createReplyType();
        if (expectedNumberOfCalls.compareTo(BigDecimal.valueOf(meterConfig.getMeter().size())) == 0) {
            replyType.setResult(ReplyType.Result.OK);
        } else if (expectedNumberOfCalls.compareTo(BigDecimal.valueOf(failedDevices.size())) == 0) {
            replyType.setResult(ReplyType.Result.FAILED);
        } else {
            replyType.setResult(ReplyType.Result.PARTIAL);
        }

        // set errors
        failedDevices.forEach(failedMeterOperation -> {
            ErrorType errorType = new ErrorType();
            errorType.setCode(failedMeterOperation.getErrorCode());
            errorType.setDetails(failedMeterOperation.getErrorMessage());
            ObjectType objectType = new ObjectType();
            objectType.setMRID(failedMeterOperation.getmRID());
            objectType.setObjectType("EndDevice");
            Name name = new Name();
            name.setName(failedMeterOperation.getMeterName());
            objectType.getName().add(name);
            errorType.setObject(objectType);
            replyType.getError().add(errorType);
        });
        responseMessage.setReply(replyType);

        // set payload
        GetMeterConfigPayloadType meterConfigPayload = getMeterConfigEventMessageObjectFactory.createGetMeterConfigPayloadType();
        meterConfigPayload.setMeterConfig(meterConfig);
        responseMessage.setPayload(meterConfigPayload);

        return responseMessage;
    }
}
