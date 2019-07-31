/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.usagepointconfig;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.ws.Service;

import org.apache.cxf.jaxws.JaxWsClientProxy;
import org.apache.cxf.message.Message;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.elster.jupiter.cim.webservices.outbound.soap.FailedUsagePointOperation;
import com.elster.jupiter.cim.webservices.outbound.soap.ReplyUsagePointConfigWebService;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import ch.iec.tc57._2011.replyusagepointconfig.FaultMessage;
import ch.iec.tc57._2011.replyusagepointconfig.ReplyUsagePointConfig;
import ch.iec.tc57._2011.replyusagepointconfig.UsagePointConfigPort;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.Name;
import ch.iec.tc57._2011.schema.message.ObjectType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import ch.iec.tc57._2011.usagepointconfig.UsagePointConfig;
import ch.iec.tc57._2011.usagepointconfigmessage.ObjectFactory;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigEventMessageType;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigPayloadType;

@Component(name = "com.elster.jupiter.cim.webservices.outbound.soap.replyusagepointconfig.provider", service = {
        ReplyUsagePointConfigWebService.class, OutboundSoapEndPointProvider.class }, immediate = true, property = {
                "name=" + ReplyUsagePointConfigWebService.NAME })
public class ReplyUsagePointConfigServiceProvider
        implements ReplyUsagePointConfigWebService, OutboundSoapEndPointProvider {

    private static final String NOUN = "UsagePointConfig";
    private static final String URL = "url";
    private static final String RESOURCE_WSDL = "/usagepointconfig/ReplyUsagePointConfig.wsdl";

    private volatile WebServicesService webServicesService;

    private volatile CustomPropertySetService customPropertySetService;

    private volatile Clock clock;

    private final Map<String, UsagePointConfigPort> usagePointConfigPorts = new ConcurrentHashMap<>();

    private final ObjectFactory objectFactory = new ObjectFactory();
    private final ch.iec.tc57._2011.schema.message.ObjectFactory headerTypeFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private UsagePointConfigFactory usagePointConfigFactory;

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService) {
        this.webServicesService = webServicesService;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addUsagePointConfigPort(UsagePointConfigPort usagePointConfigPort, Map<String, Object> properties) {
        usagePointConfigPorts.put(properties.get(URL).toString(), usagePointConfigPort);
    }

    @Activate
    public void onActivate() {
        usagePointConfigFactory = new UsagePointConfigFactory(clock, customPropertySetService);
    }

    public void removeUsagePointConfigPort(UsagePointConfigPort usagePointConfigPort) {
        usagePointConfigPorts.values()
                .removeIf(objUsagePointConfigPort -> objUsagePointConfigPort == usagePointConfigPort);
    }

    public Map<String, UsagePointConfigPort> getUsagePointConfigPorts() {
        return Collections.unmodifiableMap(usagePointConfigPorts);
    }

    @Override
    public Service get() {
        return new ReplyUsagePointConfig(this.getClass().getResource(RESOURCE_WSDL));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getService() {
        return UsagePointConfigPort.class;
    }

    @Override
    public void call(EndPointConfiguration endPointConfiguration, String operation,
            List<com.elster.jupiter.metering.UsagePoint> successList, List<FailedUsagePointOperation> failureList,
            BigDecimal expectedNumberOfCalls, String correlationId) {
        publish(endPointConfiguration);
        try {
            Optional.ofNullable(getUsagePointConfigPorts().get(endPointConfiguration.getUrl()))
                    .filter(usagePointConfigPort -> isValidUsagePointConfigPortService(usagePointConfigPort))
                    .ifPresent(service -> {
                        try {
                            switch (operation) {
                            case "CREATE":
                                service.createdUsagePointConfig(
                                        createResponseMessage(createUsagePointConfig(successList), failureList,
                                                expectedNumberOfCalls, HeaderType.Verb.CREATED, correlationId));
                                break;
                            case "UPDATE":
                                service.changedUsagePointConfig(
                                        createResponseMessage(createUsagePointConfig(successList), failureList,
                                                expectedNumberOfCalls, HeaderType.Verb.CHANGED, correlationId));
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

    private void publish(EndPointConfiguration endPointConfiguration) {
        if (endPointConfiguration.isActive() && !webServicesService.isPublished(endPointConfiguration)) {
            webServicesService.publishEndPoint(endPointConfiguration);
        }
    }

    boolean isValidUsagePointConfigPortService(UsagePointConfigPort usagePointConfigPort) {
        return ((JaxWsClientProxy) Proxy.getInvocationHandler(usagePointConfigPort)).getRequestContext()
                .containsKey(Message.ENDPOINT_ADDRESS);
    }

    private UsagePointConfig createUsagePointConfig(List<com.elster.jupiter.metering.UsagePoint> successfulOperations) {
        final UsagePointConfig config = usagePointConfigFactory.configFrom(successfulOperations);
        return config;
    }

    private UsagePointConfigEventMessageType createResponseMessage(UsagePointConfig usagePointConfig,
            HeaderType.Verb verb, String correlationId) {
        UsagePointConfigEventMessageType usagePointConfigEventMessageType = new UsagePointConfigEventMessageType();

        // set header
        HeaderType header = headerTypeFactory.createHeaderType();
        header.setNoun(NOUN);
        header.setVerb(verb);
        header.setCorrelationID(correlationId);
        usagePointConfigEventMessageType.setHeader(header);

        // set reply
        ReplyType replyType = headerTypeFactory.createReplyType();
        replyType.setResult(ReplyType.Result.OK);
        usagePointConfigEventMessageType.setReply(replyType);

        // set payload
        UsagePointConfigPayloadType payloadType = objectFactory.createUsagePointConfigPayloadType();
        usagePointConfigEventMessageType.setPayload(payloadType);
        payloadType.setUsagePointConfig(usagePointConfig);
        usagePointConfigEventMessageType.setPayload(payloadType);

        return usagePointConfigEventMessageType;
    }

    private UsagePointConfigEventMessageType createResponseMessage(UsagePointConfig usagePointConfig,
            List<FailedUsagePointOperation> failedDevices, BigDecimal expectedNumberOfCalls, HeaderType.Verb verb, String correlationId) {
        UsagePointConfigEventMessageType usagePointConfigEventMessageType = createResponseMessage(usagePointConfig,
                verb, correlationId);

        // set reply
        ReplyType replyType = headerTypeFactory.createReplyType();
        if (expectedNumberOfCalls.compareTo(BigDecimal.valueOf(usagePointConfig.getUsagePoint().size())) == 0) {
            replyType.setResult(ReplyType.Result.OK);
        } else if (expectedNumberOfCalls.compareTo(BigDecimal.valueOf(failedDevices.size())) == 0) {
            replyType.setResult(ReplyType.Result.FAILED);
        } else {
            replyType.setResult(ReplyType.Result.PARTIAL);
        }

        // set errors
        failedDevices.forEach(failedUsagePointOperation -> {
            ErrorType errorType = new ErrorType();
            errorType.setCode(failedUsagePointOperation.getErrorCode());
            errorType.setDetails(failedUsagePointOperation.getErrorMessage());
            ObjectType objectType = new ObjectType();
            objectType.setMRID(failedUsagePointOperation.getUsagePointMrid());
            objectType.setObjectType("UsagePoint");
            Name name = new Name();
            name.setName(failedUsagePointOperation.getUsagePointName());
            objectType.getName().add(name);
            errorType.setObject(objectType);
            replyType.getError().add(errorType);
        });

        usagePointConfigEventMessageType.setReply(replyType);

        return usagePointConfigEventMessageType;
    }
}
