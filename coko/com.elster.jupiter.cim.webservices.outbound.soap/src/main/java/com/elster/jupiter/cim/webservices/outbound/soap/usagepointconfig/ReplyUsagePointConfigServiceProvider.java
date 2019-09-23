/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.usagepointconfig;

import com.elster.jupiter.cim.webservices.outbound.soap.FailedUsagePointOperation;
import com.elster.jupiter.cim.webservices.outbound.soap.ReplyUsagePointConfigWebService;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractOutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceRequestAttributesNames;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.xml.ws.Service;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component(name = "com.elster.jupiter.cim.webservices.outbound.soap.replyusagepointconfig.provider", service = {
        ReplyUsagePointConfigWebService.class, OutboundSoapEndPointProvider.class }, immediate = true, property = {
                "name=" + ReplyUsagePointConfigWebService.NAME })
public class ReplyUsagePointConfigServiceProvider
        extends AbstractOutboundEndPointProvider<UsagePointConfigPort>
        implements ReplyUsagePointConfigWebService, OutboundSoapEndPointProvider, ApplicationSpecific {

    private static final String NOUN = "UsagePointConfig";
    private static final String RESOURCE_WSDL = "/wsdl/usagepointconfig/ReplyUsagePointConfig.wsdl";

    private volatile CustomPropertySetService customPropertySetService;

    private volatile Clock clock;

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

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addUsagePointConfigPort(UsagePointConfigPort usagePointConfigPort, Map<String, Object> properties) {
        super.doAddEndpoint(usagePointConfigPort, properties);
    }

    @Reference
    public void addWebServicesService(WebServicesService webServicesService) {
        // Just to inject WebServicesService
    }

    @Activate
    public void onActivate() {
        usagePointConfigFactory = new UsagePointConfigFactory(clock, customPropertySetService);
    }

    public void removeUsagePointConfigPort(UsagePointConfigPort usagePointConfigPort) {
        super.doRemoveEndpoint(usagePointConfigPort);
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
    protected String getName() {
        return ReplyUsagePointConfigWebService.NAME;
    }

    @Override
    public void call(EndPointConfiguration endPointConfiguration, String operation,
            List<com.elster.jupiter.metering.UsagePoint> successList, List<FailedUsagePointOperation> failureList,
            BigDecimal expectedNumberOfCalls, String correlationId) {
        String method;
        UsagePointConfigEventMessageType message;
        switch (operation) {
            case "CREATE":
                method = "createdUsagePointConfig";
                message = createResponseMessage(createUsagePointConfig(successList), failureList,
                        expectedNumberOfCalls, HeaderType.Verb.CREATED, correlationId);
                break;
            case "UPDATE":
                method = "changedUsagePointConfig";
                message = createResponseMessage(createUsagePointConfig(successList), failureList,
                        expectedNumberOfCalls, HeaderType.Verb.CHANGED, correlationId);
                break;
            default:
                throw new UnsupportedOperationException(operation + " isn't supported.");
        }

        SetMultimap<String, String> values = HashMultimap.create();

        successList.forEach(up->{
            values.put(WebServiceRequestAttributesNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), up.getMRID());
            values.put(WebServiceRequestAttributesNames.CIM_USAGE_POINT_NAME.getAttributeName(), up.getName());
        });
        failureList.forEach(upO->{
            values.put(WebServiceRequestAttributesNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), upO.getUsagePointMrid());
            values.put(WebServiceRequestAttributesNames.CIM_USAGE_POINT_NAME.getAttributeName(), upO.getUsagePointName());
        });
        using(method)
                .toEndpoints(endPointConfiguration)
                .withRelatedObject(values)
                .send(message);
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

    @Override
    public String getApplication() {
        return WebServiceApplicationName.INSIGHT.getName();
    }
}
