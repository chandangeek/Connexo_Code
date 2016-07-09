package com.elster.jupiter.kore.api.impl.servicecall;


import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.servicecall.ServiceCall;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum UsagePointCommand {
    CONNECT((usagePoint, usagePointCommandInfo,usagePointCommandHelper) -> {
        ServiceCall serviceCall = usagePointCommandHelper.createServiceCall(usagePoint, usagePointCommandInfo);
        List<CompletionOptions> completionOptionsList = usagePoint.connect(Instant.ofEpochMilli(usagePointCommandInfo.effectiveTimestamp), serviceCall);
        Command.updateCallback(completionOptionsList, serviceCall, usagePointCommandHelper.getDestinationSpec());
        return completionOptionsList;
    }),
    DISCONNECT((usagePoint, usagePointCommandInfo, usagePointCommandHelper) -> {
        ServiceCall serviceCall = usagePointCommandHelper.createServiceCall(usagePoint, usagePointCommandInfo);
        List<CompletionOptions> completionOptionsList = usagePoint.disconnect(Instant.ofEpochMilli(usagePointCommandInfo.effectiveTimestamp), serviceCall);
        Command.updateCallback(completionOptionsList, serviceCall, usagePointCommandHelper.getDestinationSpec());
        return completionOptionsList;
    }),
    ENABLELOADLIMIT((usagePoint, usagePointCommandInfo, usagePointCommandHelper) -> {
        ServiceCall serviceCall = usagePointCommandHelper.createServiceCall(usagePoint, usagePointCommandInfo);
        List<CompletionOptions> completionOptionsList = usagePoint.enableLoadLimit(Instant.ofEpochMilli(usagePointCommandInfo.effectiveTimestamp), usagePointCommandInfo.loadLimit, serviceCall);
        Command.updateCallback(completionOptionsList, serviceCall, usagePointCommandHelper.getDestinationSpec());
        return completionOptionsList;
    }),
    DISABLELOADLIMIT((usagePoint, usagePointCommandInfo, usagePointCommandHelper) -> {
        ServiceCall serviceCall = usagePointCommandHelper.createServiceCall(usagePoint, usagePointCommandInfo);
        List<CompletionOptions> completionOptionsList = usagePoint.disableLoadLimit(Instant.ofEpochMilli(usagePointCommandInfo.effectiveTimestamp), serviceCall);
        Command.updateCallback(completionOptionsList, serviceCall, usagePointCommandHelper.getDestinationSpec());
        return completionOptionsList;
    }),
    READMETERS((usagePoint, usagePointCommandInfo, usagePointCommandHelper) -> {
        ServiceCall serviceCall = usagePointCommandHelper.createServiceCall(usagePoint, usagePointCommandInfo);
        List<CompletionOptions> completionOptionsList = usagePoint.readData(Instant.ofEpochMilli(usagePointCommandInfo.effectiveTimestamp),
                usagePointCommandInfo.readingTypes.stream().map(usagePointCommandHelper.getMeteringService()::getReadingType)
                        .flatMap(rt -> rt.isPresent() ? Stream.of(rt.get()) : Stream.empty()).collect(Collectors.toList())
                , serviceCall);
        Command.updateCallback(completionOptionsList, serviceCall, usagePointCommandHelper.getDestinationSpec());
        return completionOptionsList;
    });

    Command usagePointCommand;

    UsagePointCommand(Command usagePointCommand) {
        this.usagePointCommand = usagePointCommand;
    }

    public CommandRunStatusInfo process(UsagePoint usagePoint, UsagePointCommandInfo commandInfo, UsagePointCommandHelper usagePointCommandHelper){

        List<CommandRunStatusInfo> childrenCommands =  usagePoint.getMeterActivations(Instant.ofEpochMilli(commandInfo.effectiveTimestamp))
                .stream()
                .map(MeterActivation::getMeter)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(meter -> meter.getHeadEndInterface().isPresent() ? new CommandRunStatusInfo(meter.getId(), CommandStatus.SUCCESS) : new CommandRunStatusInfo(meter.getId(), CommandStatus.FAILED))
                .collect(Collectors.toList());


        long expectedCommands = usagePointCommandHelper.getExpectedNumberOfCommands(usagePoint, Instant.ofEpochMilli(commandInfo.effectiveTimestamp));

        List<CompletionOptions> completionOptionsList = usagePointCommand.process(usagePoint,commandInfo, usagePointCommandHelper);

        if(completionOptionsList.size()<expectedCommands){

            CommandRunStatusInfo commandRunStatusInfo = new CommandRunStatusInfo(usagePoint.getId(), CommandStatus.FAILED, childrenCommands.toArray(new CommandRunStatusInfo[childrenCommands.size()]));

            commandRunStatusInfo.system =  usagePoint.getMeterActivations(Instant.ofEpochMilli(commandInfo.effectiveTimestamp))
                    .stream()
                    .flatMap(meterActivation -> meterActivation.getMeter()
                            .isPresent() ? Stream.of(meterActivation.getMeter().get()) : Stream.empty())
                    .filter(meter -> !meter.getHeadEndInterface().isPresent()).findFirst().map(EndDevice::getAmrId).orElse("MDC");

            return commandRunStatusInfo;

        } else {
            return new CommandRunStatusInfo(usagePoint.getId(), CommandStatus.SUCCESS, childrenCommands
                    .toArray(new CommandRunStatusInfo[childrenCommands.size()]));

        }
    }
}

interface Command{
    List<CompletionOptions> process(UsagePoint usagePoint, UsagePointCommandInfo usagePointCommandInfo, UsagePointCommandHelper usagePointCommandHelper);

    static void updateCallback(List<CompletionOptions> completionOptionsList, ServiceCall serviceCall, DestinationSpec destinationSpec){
        for (CompletionOptions options : completionOptionsList) {
            if (options != null) {
                options.whenFinishedSend("{\"id\":"+serviceCall.getId()+" , \"success\":true}", destinationSpec);
            }
        }
    }
}