package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.kore.api.impl.utils.MessageSeeds;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.stream.Stream;

public class UsagePointCommandHelper {
    private final CustomPropertySetService customPropertySetService;
    private final ServiceCallService serviceCallService;
    private final MessageService messageService;
    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public UsagePointCommandHelper(CustomPropertySetService customPropertySetService, ServiceCallService serviceCallService, MessageService messageService, MeteringService meteringService, ExceptionFactory exceptionFactory) {
        this.customPropertySetService = customPropertySetService;
        this.serviceCallService = serviceCallService;
        this.messageService = messageService;
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
    }


    public ServiceCall createServiceCall(UsagePoint usagePoint, UsagePointCommandInfo commandInfo) {
        UsagePointCommandDomainExtension usagePointCommandDomainExtension = new UsagePointCommandDomainExtension();
        usagePointCommandDomainExtension.setExpectedNumberOfCommands(new BigDecimal(getExpectedNumberOfCommands(usagePoint, Instant.ofEpochMilli(commandInfo.effectiveTimestamp))));
        usagePointCommandDomainExtension.setCallbackHttpMethod(commandInfo.httpCallBack.method);
        usagePointCommandDomainExtension.setCallbackSuccessURL(commandInfo.httpCallBack.successURL);
        usagePointCommandDomainExtension.setCallbackPartialSuccessURL(commandInfo.httpCallBack.partialSuccessURL);
        usagePointCommandDomainExtension.setCallbackFailureURL(commandInfo.httpCallBack.failureURL);

        ServiceCallType serviceCallType = serviceCallService.findServiceCallType(UsagePointCommandHandler.USAGE_POINT_COMMAND_HANDLER_NAME, UsagePointCommandHandler.USAGE_POINT_COMMAND_HANDLER_VERSION)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_SERVICE_CALL_TYPE, UsagePointCommandHandler.USAGE_POINT_COMMAND_HANDLER_NAME));

        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .extendedWith(usagePointCommandDomainExtension)
                .targetObject(usagePoint)
                .create();
        serviceCall.requestTransition(DefaultState.PENDING);
        return serviceCall;
    }

    public long getExpectedNumberOfCommands(UsagePoint usagePoint, Instant when) {
        return usagePoint.getMeterActivations(when)
                .stream()
                .flatMap(meterActivation -> meterActivation.getMeter()
                        .isPresent() ? Stream.of(meterActivation.getMeter().get()) : Stream.empty()).count();
    }

    public DestinationSpec getDestinationSpec() {
        return messageService.getDestinationSpec("CommandCallback").orElseGet(this::createDestinationSpec);
    }

    private DestinationSpec createDestinationSpec() {
        QueueTableSpec queueTableSpec = messageService.getQueueTableSpec("MSG_RAWQUEUETABLE").get();
        DestinationSpec destinationSpec = queueTableSpec.createDestinationSpec("CommandCallback", 20);
        destinationSpec.save();
        destinationSpec.activate();
        destinationSpec.subscribe("CommandCallback");
        return destinationSpec;
    }

    public MeteringService getMeteringService() {
        return meteringService;
    }
}
