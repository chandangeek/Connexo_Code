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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum UsagePointCommand {
    CONNECT((serviceCall, usagePoint, usagePointCommandInfo, usagePointCommandHelper) -> {
        List<CompletionOptions> completionOptionsList = usagePoint.connect(Instant.ofEpochMilli(usagePointCommandInfo.effectiveTimestamp), serviceCall);
        Command.updateCallback(completionOptionsList, serviceCall, usagePointCommandHelper.getDestinationSpec());
        return completionOptionsList;
    }),
    DISCONNECT((serviceCall, usagePoint, usagePointCommandInfo, usagePointCommandHelper) -> {
        List<CompletionOptions> completionOptionsList = usagePoint.disconnect(Instant.ofEpochMilli(usagePointCommandInfo.effectiveTimestamp), serviceCall);
        Command.updateCallback(completionOptionsList, serviceCall, usagePointCommandHelper.getDestinationSpec());
        return completionOptionsList;
    }),
    ENABLELOADLIMIT((serviceCall, usagePoint, usagePointCommandInfo, usagePointCommandHelper) -> {
        List<CompletionOptions> completionOptionsList = usagePoint.enableLoadLimit(Instant.ofEpochMilli(usagePointCommandInfo.effectiveTimestamp), usagePointCommandInfo.loadLimit, serviceCall);
        Command.updateCallback(completionOptionsList, serviceCall, usagePointCommandHelper.getDestinationSpec());
        return completionOptionsList;
    }),
    DISABLELOADLIMIT((serviceCall, usagePoint, usagePointCommandInfo, usagePointCommandHelper) -> {
        List<CompletionOptions> completionOptionsList = usagePoint.disableLoadLimit(Instant.ofEpochMilli(usagePointCommandInfo.effectiveTimestamp), serviceCall);
        Command.updateCallback(completionOptionsList, serviceCall, usagePointCommandHelper.getDestinationSpec());
        return completionOptionsList;
    }),
    READMETERS((serviceCall, usagePoint, usagePointCommandInfo, usagePointCommandHelper) -> {
        List<CompletionOptions> completionOptionsList = usagePoint.readData(Instant.ofEpochMilli(usagePointCommandInfo.effectiveTimestamp),
                usagePointCommandHelper.getReadingTypesToRead(usagePointCommandInfo, usagePoint)
                , serviceCall);
        Command.updateCallback(completionOptionsList, serviceCall, usagePointCommandHelper.getDestinationSpec());
        return completionOptionsList;
    });

    Command usagePointCommand;

    UsagePointCommand(Command usagePointCommand) {
        this.usagePointCommand = usagePointCommand;
    }

    public CommandRunStatusInfo process(UsagePoint usagePoint, UsagePointCommandInfo commandInfo, UsagePointCommandHelper usagePointCommandHelper) {
        long expectedCommands = usagePointCommandHelper.getExpectedNumberOfCommands(usagePoint, Instant.ofEpochMilli(commandInfo.effectiveTimestamp));

        ServiceCall serviceCall = usagePointCommandHelper.createServiceCall(usagePoint, commandInfo);
        List<CompletionOptions> completionOptionsList = usagePointCommand.process(serviceCall, usagePoint, commandInfo, usagePointCommandHelper);

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