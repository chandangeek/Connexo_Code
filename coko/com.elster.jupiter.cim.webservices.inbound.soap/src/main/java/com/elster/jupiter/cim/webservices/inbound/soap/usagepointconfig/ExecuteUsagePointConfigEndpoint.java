/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.elster.jupiter.cim.webservices.inbound.soap.servicecall.ServiceCallCommands;
import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.metering.CimUsagePointAttributeNames;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.Checks;

import ch.iec.tc57._2011.executeusagepointconfig.FaultMessage;
import ch.iec.tc57._2011.executeusagepointconfig.UsagePointConfigPort;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.usagepointconfig.UsagePoint;
import ch.iec.tc57._2011.usagepointconfig.UsagePointConfig;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigPayloadType;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigRequestMessageType;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigResponseMessageType;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class ExecuteUsagePointConfigEndpoint extends AbstractInboundEndPoint implements UsagePointConfigPort, ApplicationSpecific {
    private static final String NOUN = "UsagePointConfig";
    private final ReplyTypeFactory replyTypeFactory;
    private final UsagePointConfigFaultMessageFactory messageFactory;
    private final UsagePointConfigFactory usagePointConfigFactory;
    private final Provider<UsagePointBuilder> usagePointBuilderProvider;
    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.usagepointconfigmessage.ObjectFactory usagePointConfigMessageObjectFactory = new ch.iec.tc57._2011.usagepointconfigmessage.ObjectFactory();
    private final EndPointConfigurationService endPointConfigurationService;
    private final WebServicesService webServicesService;
    private final ServiceCallCommands serviceCallCommands;

    @FunctionalInterface
    private interface ThrowingFunction<T, R> {
        R apply(T t) throws FaultMessage;
    }

    @Inject
    ExecuteUsagePointConfigEndpoint(ReplyTypeFactory replyTypeFactory,
            UsagePointConfigFaultMessageFactory messageFactory, UsagePointConfigFactory usagePointConfigFactory,
            Provider<UsagePointBuilder> usagePointBuilderProvider,
            EndPointConfigurationService endPointConfigurationService, WebServicesService webServicesService,
            ServiceCallCommands serviceCallCommands) {
        this.replyTypeFactory = replyTypeFactory;
        this.messageFactory = messageFactory;
        this.usagePointConfigFactory = usagePointConfigFactory;
        this.usagePointBuilderProvider = usagePointBuilderProvider;
        this.endPointConfigurationService = endPointConfigurationService;
        this.webServicesService = webServicesService;
        this.serviceCallCommands = serviceCallCommands;
    }

    @Override
    public UsagePointConfigResponseMessageType createUsagePointConfig(UsagePointConfigRequestMessageType message)
            throws FaultMessage {
        return process(message, Action.CREATE, MessageSeeds.UNABLE_TO_CREATE_USAGE_POINT, HeaderType.Verb.CREATED,
                usagePoint -> {
                    UsagePointBuilder.PreparedUsagePointBuilder builder = usagePointBuilderProvider.get()
                            .from(usagePoint, 0); // bulk operation is not supported, only first element is processed
                    retrieveRequestTimestamp(message).ifPresent(builder::at);
                    return builder.create();
                });
    }

    @Override
    public UsagePointConfigResponseMessageType changeUsagePointConfig(UsagePointConfigRequestMessageType message)
            throws FaultMessage {

        return process(message, Action.UPDATE, MessageSeeds.UNABLE_TO_UPDATE_USAGE_POINT, HeaderType.Verb.CHANGED,
                usagePoint -> usagePointBuilderProvider.get().from(usagePoint, 0).update() // bulk operation is not supported, only first element is processed
        );
    }

    private UsagePointConfigResponseMessageType process(UsagePointConfigRequestMessageType message, Action action,
            MessageSeeds messageSeed, HeaderType.Verb verb,
            ThrowingFunction<UsagePoint, com.elster.jupiter.metering.UsagePoint> synchronousProcessor)
            throws FaultMessage {
        return runInTransactionWithOccurrence(() -> {
            try {
                if (message.getHeader() == null) {
                    throw messageFactory
                            .usagePointConfigFaultMessageSupplier(messageSeed, MessageSeeds.MISSING_ELEMENT, "Header")
                            .get();
                }

                SetMultimap<String, String> values = HashMultimap.create();
                Optional.ofNullable(message.getPayload()).map(UsagePointConfigPayloadType::getUsagePointConfig).ifPresent(uspConfig->uspConfig.getUsagePoint().forEach(usagePoint -> {
                        if (!usagePoint.getNames().isEmpty()){
                            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), usagePoint.getNames().get(0).getName());
                        }
                        if (usagePoint.getMRID() != null){
                            values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), usagePoint.getMRID());
                        }
                }));

                saveRelatedAttributes(values);

                if (Boolean.TRUE.equals(message.getHeader().isAsyncReplyFlag())) {
                    return processAsynchronously(message, action);
                }
                List<UsagePoint> usagePoints = retrieveUsagePoints(message.getPayload(), messageSeed);
                UsagePoint usagePoint = usagePoints.stream().findFirst()
                        .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(messageSeed,
                                MessageSeeds.EMPTY_LIST, "UsagePointConfig.UsagePoint"));
                com.elster.jupiter.metering.UsagePoint connexoUsagePoint = synchronousProcessor.apply(usagePoint);
            String correlationId = message.getHeader() == null ? null : message.getHeader().getCorrelationID();
            return createResponse(connexoUsagePoint, verb, usagePoints.size() > 1, correlationId);

            } catch (VerboseConstraintViolationException e) {
                throw messageFactory.usagePointConfigFaultMessage(messageSeed, e.getLocalizedMessage());
            } catch (LocalizedException e) {
                throw messageFactory.usagePointConfigFaultMessage(messageSeed, e.getLocalizedMessage(), e.getErrorCode());
            }
        });
    }

    private UsagePointConfigResponseMessageType processAsynchronously(UsagePointConfigRequestMessageType message,
            Action action) throws FaultMessage {
        Optional<EndPointConfiguration> outboundEndPointConfiguration;
        String replyAddress = getReplyAddress(message);
        if (Checks.is(replyAddress).emptyOrOnlyWhiteSpace()) {
            outboundEndPointConfiguration = Optional.empty();
        } else {
            outboundEndPointConfiguration = Optional.of(getOutboundEndPointConfiguration(action, replyAddress));
        }
        ServiceCall serviceCall = serviceCallCommands.createUsagePointConfigMasterServiceCall(message,
                outboundEndPointConfiguration, action);
        serviceCallCommands.requestTransition(serviceCall, DefaultState.PENDING);
        String correlationId = message.getHeader() == null ? null : message.getHeader().getCorrelationID();
        return createQuickResponse(HeaderType.Verb.REPLY, correlationId);
    }

    private EndPointConfiguration getOutboundEndPointConfiguration(Action action, String url) throws FaultMessage {
        EndPointConfiguration endPointConfig = endPointConfigurationService.getOutboundEndpointConfigurationByUrl(url)
                .filter(EndPointConfiguration::isActive)
                .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(action.getBasicSeed(), MessageSeeds.NO_END_POINT_WITH_URL, url));
        if (!webServicesService.isPublished(endPointConfig)) {
            webServicesService.publishEndPoint(endPointConfig);
        }
        if (!webServicesService.isPublished(endPointConfig)) {
            throw messageFactory.usagePointConfigFaultMessageSupplier(action.getBasicSeed(),
                    MessageSeeds.NO_PUBLISHED_END_POINT_WITH_URL, url).get();
        }
        return endPointConfig;
    }

    private String getReplyAddress(UsagePointConfigRequestMessageType requestMessage) {
        return requestMessage.getHeader().getReplyAddress();
    }

    private Optional<Instant> retrieveRequestTimestamp(UsagePointConfigRequestMessageType request) {
        return Optional.ofNullable(request.getHeader().getTimestamp());
    }

    private UsagePointConfigResponseMessageType createResponse(com.elster.jupiter.metering.UsagePoint usagePoint,
            HeaderType.Verb verb, boolean bulkRequested, String correlationId) {
        UsagePointConfigResponseMessageType usagePointConfigResponseMessageType = usagePointConfigMessageObjectFactory
                .createUsagePointConfigResponseMessageType();
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(verb);
        header.setNoun(NOUN);
        header.setCorrelationID(correlationId);

        usagePointConfigResponseMessageType.setHeader(header);
        usagePointConfigResponseMessageType.setReply(
                bulkRequested ? replyTypeFactory.partialFailureReplyType(MessageSeeds.UNSUPPORTED_BULK_OPERATION,
                        "UsagePointConfig.UsagePoint") : replyTypeFactory.okReplyType());
        usagePointConfigResponseMessageType
                .setPayload(usagePointConfigMessageObjectFactory.createUsagePointConfigPayloadType());
        usagePointConfigResponseMessageType.getPayload()
                .setUsagePointConfig(usagePointConfigFactory.configFrom(usagePoint));
        return usagePointConfigResponseMessageType;
    }

    private UsagePointConfigResponseMessageType createQuickResponse(HeaderType.Verb verb, String correlationId) {
        UsagePointConfigResponseMessageType usagePointConfigResponseMessageType = usagePointConfigMessageObjectFactory
                .createUsagePointConfigResponseMessageType();
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(verb);
        header.setNoun(NOUN);
        header.setCorrelationID(correlationId);

        usagePointConfigResponseMessageType.setHeader(header);
        usagePointConfigResponseMessageType.setReply(replyTypeFactory.okReplyType());
        return usagePointConfigResponseMessageType;
    }

    private List<UsagePoint> retrieveUsagePoints(UsagePointConfigPayloadType payload, MessageSeeds basicFaultMessage)
            throws FaultMessage {
        if (payload == null) {
            throw messageFactory
                    .usagePointConfigFaultMessageSupplier(basicFaultMessage, MessageSeeds.MISSING_ELEMENT, "Payload")
                    .get();
        }
        UsagePointConfig usagePointConfig = payload.getUsagePointConfig();
        if (usagePointConfig == null) {
            throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage, MessageSeeds.MISSING_ELEMENT,
                    "UsagePointConfig").get();
        }
        return usagePointConfig.getUsagePoint();
    }

    @Override
    public UsagePointConfigResponseMessageType cancelUsagePointConfig(
            UsagePointConfigRequestMessageType cancelUsagePointConfigRequestMessage) throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public UsagePointConfigResponseMessageType closeUsagePointConfig(
            UsagePointConfigRequestMessageType closeUsagePointConfigRequestMessage) throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public UsagePointConfigResponseMessageType deleteUsagePointConfig(
            UsagePointConfigRequestMessageType deleteUsagePointConfigRequestMessage) throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public UsagePointConfigResponseMessageType getUsagePointConfig(
            UsagePointConfigRequestMessageType getUsagePointConfigRequestMessage) throws FaultMessage {
        return runInTransactionWithOccurrence(() -> {
            List<UsagePoint> usagePoints = retrieveUsagePoints(getUsagePointConfigRequestMessage.getPayload(),
                    MessageSeeds.UNABLE_TO_GET_USAGE_POINT);

            SetMultimap<String, String> values = HashMultimap.create();
            usagePoints.forEach(usp->{
                if (!usp.getNames().isEmpty()) {
                    values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_NAME.getAttributeName(), usp.getNames().get(0).getName());
                }
                values.put(CimUsagePointAttributeNames.CIM_USAGE_POINT_MR_ID.getAttributeName(), usp.getMRID());
            });

            saveRelatedAttributes(values);

            UsagePoint usagePoint = usagePoints.stream().findFirst()
                    .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(MessageSeeds.UNABLE_TO_GET_USAGE_POINT,
                            MessageSeeds.EMPTY_LIST, "UsagePointConfig.UsagePoint"));
            com.elster.jupiter.metering.UsagePoint retrieved = usagePointBuilderProvider.get().from(usagePoint, 0) // bulk operation is not supported, only first element is processed
                    .get();
        String correlationId  = getUsagePointConfigRequestMessage.getHeader() == null ? null : getUsagePointConfigRequestMessage.getHeader().getCorrelationID();
        return createResponse(retrieved, HeaderType.Verb.REPLY, usagePoints.size() > 1, correlationId);
        });
    }

    @Override
    public String getApplication() {
        return WebServiceApplicationName.INSIGHT.getName();
    }
}
