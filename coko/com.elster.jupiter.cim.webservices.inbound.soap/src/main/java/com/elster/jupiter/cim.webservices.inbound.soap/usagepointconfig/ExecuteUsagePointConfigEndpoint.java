/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.usagepointconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.EndPointHelper;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.ReplyTypeFactory;
import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import ch.iec.tc57._2011.executeusagepointconfig.FaultMessage;
import ch.iec.tc57._2011.executeusagepointconfig.UsagePointConfigPort;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.usagepointconfig.UsagePoint;
import ch.iec.tc57._2011.usagepointconfig.UsagePointConfig;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigPayloadType;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigRequestMessageType;
import ch.iec.tc57._2011.usagepointconfigmessage.UsagePointConfigResponseMessageType;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class ExecuteUsagePointConfigEndpoint implements UsagePointConfigPort {
    private static final String NOUN = "UsagePointConfig";
    private final EndPointHelper endPointHelper;
    private final ReplyTypeFactory replyTypeFactory;
    private final UsagePointConfigFaultMessageFactory messageFactory;
    private final UsagePointConfigFactory usagePointConfigFactory;
    private final TransactionService transactionService;
    private final Provider<UsagePointBuilder> usagePointBuilderProvider;
    private final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory = new ch.iec.tc57._2011.schema.message.ObjectFactory();
    private final ch.iec.tc57._2011.usagepointconfigmessage.ObjectFactory usagePointConfigMessageObjectFactory
            = new ch.iec.tc57._2011.usagepointconfigmessage.ObjectFactory();

    @Inject
    ExecuteUsagePointConfigEndpoint(EndPointHelper endPointHelper,
                                    ReplyTypeFactory replyTypeFactory,
                                    UsagePointConfigFaultMessageFactory messageFactory,
                                    UsagePointConfigFactory usagePointConfigFactory,
                                    TransactionService transactionService,
                                    Provider<UsagePointBuilder> usagePointBuilderProvider) {
        this.endPointHelper = endPointHelper;
        this.replyTypeFactory = replyTypeFactory;
        this.messageFactory = messageFactory;
        this.usagePointConfigFactory = usagePointConfigFactory;
        this.transactionService = transactionService;
        this.usagePointBuilderProvider = usagePointBuilderProvider;
    }

    @Override
    public UsagePointConfigResponseMessageType createUsagePointConfig(UsagePointConfigRequestMessageType createUsagePointConfigRequestMessage) throws FaultMessage {
        endPointHelper.setSecurityContext();
        try (TransactionContext context = transactionService.getContext()) {
            List<UsagePoint> usagePoints = retrieveUsagePoints(createUsagePointConfigRequestMessage.getPayload(), MessageSeeds.UNABLE_TO_CREATE_USAGE_POINT);
            UsagePoint usagePoint = usagePoints.stream().findFirst()
                    .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(MessageSeeds.UNABLE_TO_CREATE_USAGE_POINT,
                            MessageSeeds.EMPTY_LIST, "UsagePointConfig.UsagePoint"));
            UsagePointBuilder.PreparedUsagePointBuilder builder = usagePointBuilderProvider.get()
                    .from(usagePoint, 0); // bulk operation is not supported, only first element is processed
            retrieveRequestTimestamp(createUsagePointConfigRequestMessage).ifPresent(builder::at);
            com.elster.jupiter.metering.UsagePoint created = builder.create();

            context.commit();
            return createResponse(created, HeaderType.Verb.CREATED, usagePoints.size() > 1);
        } catch (VerboseConstraintViolationException e) {
            throw messageFactory.usagePointConfigFaultMessage(MessageSeeds.UNABLE_TO_CREATE_USAGE_POINT, e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw messageFactory.usagePointConfigFaultMessage(MessageSeeds.UNABLE_TO_CREATE_USAGE_POINT, e.getLocalizedMessage(), e.getErrorCode());
        }
    }

    private Optional<Instant> retrieveRequestTimestamp(UsagePointConfigRequestMessageType request) {
        return Optional.ofNullable(request.getHeader().getTimestamp());
    }

    private UsagePointConfigResponseMessageType createResponse(com.elster.jupiter.metering.UsagePoint usagePoint, HeaderType.Verb verb, boolean bulkRequested) {
        UsagePointConfigResponseMessageType usagePointConfigResponseMessageType = usagePointConfigMessageObjectFactory.createUsagePointConfigResponseMessageType();
        HeaderType header = cimMessageObjectFactory.createHeaderType();
        header.setVerb(verb);
        header.setNoun(NOUN);
        usagePointConfigResponseMessageType.setHeader(header);
        usagePointConfigResponseMessageType.setReply(bulkRequested ?
                replyTypeFactory.partialFailureReplyType(MessageSeeds.UNSUPPORTED_BULK_OPERATION, "UsagePointConfig.UsagePoint") :
                replyTypeFactory.okReplyType());
        usagePointConfigResponseMessageType.setPayload(usagePointConfigMessageObjectFactory.createUsagePointConfigPayloadType());
        usagePointConfigResponseMessageType.getPayload().setUsagePointConfig(usagePointConfigFactory.configFrom(usagePoint));
        return usagePointConfigResponseMessageType;
    }

    @Override
    public UsagePointConfigResponseMessageType changeUsagePointConfig(UsagePointConfigRequestMessageType changeUsagePointConfigRequestMessage) throws FaultMessage {
        endPointHelper.setSecurityContext();
        try (TransactionContext context = transactionService.getContext()) {
            List<UsagePoint> usagePoints = retrieveUsagePoints(changeUsagePointConfigRequestMessage.getPayload(), MessageSeeds.UNABLE_TO_UPDATE_USAGE_POINT);
            UsagePoint usagePoint = usagePoints.stream().findFirst()
                    .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(MessageSeeds.UNABLE_TO_UPDATE_USAGE_POINT,
                            MessageSeeds.EMPTY_LIST, "UsagePointConfig.UsagePoint"));
            com.elster.jupiter.metering.UsagePoint updated = usagePointBuilderProvider.get()
                    .from(usagePoint, 0) // bulk operation is not supported, only first element is processed
                    .update();

            context.commit();
            return createResponse(updated, HeaderType.Verb.CHANGED, usagePoints.size() > 1);
        } catch (VerboseConstraintViolationException e) {
            throw messageFactory.usagePointConfigFaultMessage(MessageSeeds.UNABLE_TO_UPDATE_USAGE_POINT, e.getLocalizedMessage());
        } catch (LocalizedException e) {
            throw messageFactory.usagePointConfigFaultMessage(MessageSeeds.UNABLE_TO_UPDATE_USAGE_POINT, e.getLocalizedMessage(), e.getErrorCode());
        }
    }

    private List<UsagePoint> retrieveUsagePoints(UsagePointConfigPayloadType payload, MessageSeeds basicFaultMessage) throws FaultMessage {
        if (payload == null) {
            throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage, MessageSeeds.MISSING_ELEMENT, "Payload").get();
        }
        UsagePointConfig usagePointConfig = payload.getUsagePointConfig();
        if (usagePointConfig == null) {
            throw messageFactory.usagePointConfigFaultMessageSupplier(basicFaultMessage, MessageSeeds.MISSING_ELEMENT, "UsagePointConfig").get();
        }
        return usagePointConfig.getUsagePoint();
    }

    @Override
    public UsagePointConfigResponseMessageType cancelUsagePointConfig(UsagePointConfigRequestMessageType cancelUsagePointConfigRequestMessage) throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public UsagePointConfigResponseMessageType closeUsagePointConfig(UsagePointConfigRequestMessageType closeUsagePointConfigRequestMessage) throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public UsagePointConfigResponseMessageType deleteUsagePointConfig(UsagePointConfigRequestMessageType deleteUsagePointConfigRequestMessage) throws FaultMessage {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public UsagePointConfigResponseMessageType getUsagePointConfig(UsagePointConfigRequestMessageType getUsagePointConfigRequestMessage) throws FaultMessage {
        endPointHelper.setSecurityContext();
        List<UsagePoint> usagePoints = retrieveUsagePoints(getUsagePointConfigRequestMessage.getPayload(), MessageSeeds.UNABLE_TO_GET_USAGE_POINT);
        UsagePoint usagePoint = usagePoints.stream().findFirst()
                .orElseThrow(messageFactory.usagePointConfigFaultMessageSupplier(MessageSeeds.UNABLE_TO_GET_USAGE_POINT,
                        MessageSeeds.EMPTY_LIST, "UsagePointConfig.UsagePoint"));
        com.elster.jupiter.metering.UsagePoint retrieved = usagePointBuilderProvider.get()
                .from(usagePoint, 0) // bulk operation is not supported, only first element is processed
                .get();

        return createResponse(retrieved, HeaderType.Verb.REPLY, usagePoints.size() > 1);
    }
}
