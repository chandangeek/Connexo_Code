/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public enum UsagePointCommand {
    CONNECT((serviceCall, commandContext) -> {
        List<CompletionOptions> completionOptionsList = commandContext.getUsagePoint()
                .connect(Instant.ofEpochMilli(commandContext.getUsagePointCommandInfo().effectiveTimestamp), serviceCall);
        CommandHelper.updateCallback(completionOptionsList, serviceCall, commandContext.getCommandHelper().getDestinationSpec());
        return completionOptionsList;
    }),
    DISCONNECT((serviceCall, commandContext) -> {
        List<CompletionOptions> completionOptionsList = commandContext.getUsagePoint()
                .disconnect(Instant.ofEpochMilli(commandContext.getUsagePointCommandInfo().effectiveTimestamp), serviceCall);
        CommandHelper.updateCallback(completionOptionsList, serviceCall, commandContext.getCommandHelper().getDestinationSpec());
        return completionOptionsList;
    }),
    ENABLELOADLIMIT((serviceCall, commandContext) -> {
        List<CompletionOptions> completionOptionsList = commandContext.getUsagePoint()
                .enableLoadLimit(Instant.ofEpochMilli(commandContext.getUsagePointCommandInfo().effectiveTimestamp), commandContext.getUsagePointCommandInfo().loadLimit, serviceCall);
        CommandHelper.updateCallback(completionOptionsList, serviceCall, commandContext.getCommandHelper().getDestinationSpec());
        return completionOptionsList;
    }),
    DISABLELOADLIMIT((serviceCall, commandContext) -> {
        List<CompletionOptions> completionOptionsList = commandContext.getUsagePoint()
                .disableLoadLimit(Instant.ofEpochMilli(commandContext.getUsagePointCommandInfo().effectiveTimestamp), serviceCall);
        CommandHelper.updateCallback(completionOptionsList, serviceCall, commandContext.getCommandHelper().getDestinationSpec());
        return completionOptionsList;
    }),
    READMETERS((serviceCall, commandContext) -> {
        List<CompletionOptions> completionOptionsList = commandContext.getUsagePoint().readData(Instant.ofEpochMilli(commandContext.getUsagePointCommandInfo().effectiveTimestamp),
                commandContext.getCommandHelper().getReadingTypesToRead(commandContext.getUsagePointCommandInfo(), commandContext.getUsagePoint())
                , serviceCall);
        CommandHelper.updateCallback(completionOptionsList, serviceCall, commandContext.getCommandHelper().getDestinationSpec());
        return completionOptionsList;
    });

    BiFunction<ServiceCall, UsagePointCommandContext, List<CompletionOptions>> usagePointCommand;

    UsagePointCommand(BiFunction<ServiceCall, UsagePointCommandContext, List<CompletionOptions>> usagePointCommand) {
        this.usagePointCommand = usagePointCommand;
    }

    public CommandRunStatusInfo process(UsagePoint usagePoint, UsagePointCommandInfo commandInfo, CommandHelper commandHelper) {
        long expectedCommands = commandHelper.getExpectedNumberOfCommands(usagePoint, Instant.ofEpochMilli(commandInfo.effectiveTimestamp));

        ServiceCall serviceCall = commandHelper.createServiceCall(usagePoint, commandInfo);
        UsagePointCommandContext usagePointCommandContext = new UsagePointCommandContext(usagePoint, commandInfo, commandHelper);
        List<CompletionOptions> completionOptionsList = usagePointCommand.apply(serviceCall, usagePointCommandContext);

        if (completionOptionsList.isEmpty() || completionOptionsList.size() < expectedCommands) {
            serviceCall.requestTransition(DefaultState.ONGOING);
            serviceCall.requestTransition(DefaultState.FAILED);

            List<CommandRunStatusInfo> childrenCommands = usagePoint.getMeterActivations(Instant.ofEpochMilli(commandInfo.effectiveTimestamp))
                    .stream()
                    .map(MeterActivation::getMeter)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(meter -> new CommandRunStatusInfo(meter.getName(), CommandStatus.FAILED, meter.getHeadEndInterface()
                            .map(HeadEndInterface::getAmrSystem)
                            .orElse(null)))
                    .collect(Collectors.toList());

            CommandRunStatusInfo commandStatus = new CommandRunStatusInfo(usagePoint.getMRID(),
                    CommandStatus.FAILED, childrenCommands.toArray(new CommandRunStatusInfo[childrenCommands
                    .size()]));
            commandStatus.system = childrenCommands.stream()
                    .map(commandRunStatusInfo -> Optional.ofNullable(commandRunStatusInfo.system))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .orElse("MDC");
            return commandStatus;
        } else {

            List<CommandRunStatusInfo> childrenCommands = usagePoint.getMeterActivations(Instant.ofEpochMilli(commandInfo.effectiveTimestamp))
                    .stream()
                    .map(MeterActivation::getMeter)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(meter -> new CommandRunStatusInfo(meter.getName(), CommandStatus.SUCCESS, meter.getHeadEndInterface()
                            .map(HeadEndInterface::getAmrSystem)
                            .orElse(null)))
                    .collect(Collectors.toList());

            return new CommandRunStatusInfo(usagePoint.getMRID(), CommandStatus.SUCCESS, childrenCommands
                    .toArray(new CommandRunStatusInfo[childrenCommands.size()]));
        }
    }
}