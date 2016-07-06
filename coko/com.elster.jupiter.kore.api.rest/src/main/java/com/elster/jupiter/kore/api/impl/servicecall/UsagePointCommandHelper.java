package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UsagePointCommandHelper {
    private final CustomPropertySetService customPropertySetService;
    private final ServiceCallService serviceCallService;
    private final MessageService messageService;
    private final MeteringService meteringService;


    @Inject
    public UsagePointCommandHelper(CustomPropertySetService customPropertySetService, ServiceCallService serviceCallService, MessageService messageService, MeteringService meteringService) {
        this.customPropertySetService = customPropertySetService;
        this.serviceCallService = serviceCallService;
        this.messageService = messageService;
        this.meteringService = meteringService;
    }

    public CommandRunStatusInfo checkHeadEndSupport(UsagePoint usagePoint, UsagePointCommandInfo commandInfo){
        List<CommandRunStatusInfo> childrenCommands =  usagePoint.getMeterActivations(Instant.ofEpochMilli(commandInfo.effectiveTimestamp))
                .stream()
                .flatMap(meterActivation -> meterActivation.getMeter()
                        .isPresent() ? Stream.of(meterActivation.getMeter().get()) : Stream.empty())
                .map(meter -> meter.getHeadEndInterface().isPresent() ? new CommandRunStatusInfo(meter.getId(), CommandStatus.SUCCESS) : new CommandRunStatusInfo(meter.getId(), CommandStatus.FAILED))
                .collect(Collectors.toList());

        long expectedCommands = getExpectedNumberOfCommands(usagePoint, Instant.ofEpochMilli(commandInfo.effectiveTimestamp));

        if(childrenCommands.size()<expectedCommands){

            CommandRunStatusInfo commandRunStatusInfo = new CommandRunStatusInfo(usagePoint.getId(), CommandStatus.FAILED, childrenCommands.toArray(new CommandRunStatusInfo[childrenCommands.size()]));

           commandRunStatusInfo.system =  usagePoint.getMeterActivations(Instant.ofEpochMilli(commandInfo.effectiveTimestamp))
                    .stream()
                    .flatMap(meterActivation -> meterActivation.getMeter()
                            .isPresent() ? Stream.of(meterActivation.getMeter().get()) : Stream.empty())
                    .filter(meter -> !meter.getHeadEndInterface().isPresent()).findFirst().map(EndDevice::getAmrId).orElse("MDC");

            return commandRunStatusInfo;

        } else {
            return new CommandRunStatusInfo(usagePoint.getId(), childrenCommands.size() < expectedCommands ? CommandStatus.FAILED : CommandStatus.SUCCESS, childrenCommands
                    .toArray(new CommandRunStatusInfo[childrenCommands.size()]));

        }

    }

    public ServiceCall createServiceCall(UsagePoint usagePoint, UsagePointCommandInfo commandInfo) {
        RegisteredCustomPropertySet customPropertySet = customPropertySetService.findActiveCustomPropertySets(ServiceCall.class)
                .stream()
                .filter(cps -> cps.getCustomPropertySet()
                        .getId()
                        .equals(UsagePointCommandDomainExtension.class.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find command custom property set"));

        UsagePointCommandDomainExtension usagePointCommandDomainExtension = new UsagePointCommandDomainExtension();
        usagePointCommandDomainExtension.setExpectedNumberOfCommands(new BigDecimal(getExpectedNumberOfCommands(usagePoint, Instant.ofEpochMilli(commandInfo.effectiveTimestamp))));
        usagePointCommandDomainExtension.setCallbackHttpMethod(commandInfo.httpCallBack.method);
        usagePointCommandDomainExtension.setCallbackSuccessURL(commandInfo.httpCallBack.successURL);
        usagePointCommandDomainExtension.setCallbackPartialSuccessURL(commandInfo.httpCallBack.partialSuccessURL);
        usagePointCommandDomainExtension.setCallbackFailureURL(commandInfo.httpCallBack.failureURL);

        ServiceCallType serviceCallType = serviceCallService.findServiceCallType("UsagePointCommandHandler", "v1.0")
                .orElseGet(() -> serviceCallService.createServiceCallType("UsagePointCommandHandler", "v1.0")
                        .handler("UsagePointCommandHandler")
                        .customPropertySet(customPropertySet)
                        .create());

        ServiceCall serviceCall = serviceCallType.newServiceCall()
                .extendedWith(usagePointCommandDomainExtension)
                .targetObject(usagePoint)
                .create();
        serviceCall.requestTransition(DefaultState.PENDING);
        return serviceCall;
    }

    private long getExpectedNumberOfCommands(UsagePoint usagePoint, Instant when){
        return usagePoint.getMeterActivations(when)
                .stream()
                .flatMap(meterActivation -> meterActivation.getMeter()
                        .isPresent() ? Stream.of(meterActivation.getMeter().get()) : Stream.empty()).count();
    }

    public DestinationSpec getDestinationSpec(){
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
