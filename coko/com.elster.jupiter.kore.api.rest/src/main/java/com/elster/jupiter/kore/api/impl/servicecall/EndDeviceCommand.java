/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.impl.servicecall;


import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public enum EndDeviceCommand {
    READMETER((serviceCall, commandContext) -> {
        Optional<CompletionOptions> completionOptions = commandContext.getEndDevice().getHeadEndInterface()
                .map(headEndInterface -> headEndInterface.scheduleMeterRead((Meter) commandContext.getEndDevice(),
                        Instant.ofEpochMilli(commandContext.getEndDeviceCommandInfo().effectiveTimestamp),
                        serviceCall));
        completionOptions.ifPresent(co -> co.whenFinishedSendCompletionMessageWith(String.valueOf(serviceCall.getId()), commandContext.getCommandHelper().getDestinationSpec()));
        return completionOptions;
    });

    BiFunction<ServiceCall, EndDeviceCommandContext, Optional<CompletionOptions>> endDeviceCommand;

    EndDeviceCommand(BiFunction<ServiceCall, EndDeviceCommandContext, Optional<CompletionOptions>> endDeviceCommand) {
        this.endDeviceCommand = endDeviceCommand;
    }

    public CommandRunStatusInfo process(EndDevice endDevice, EndDeviceCommandInfo commandInfo, CommandHelper commandHelper) {

        ServiceCall serviceCall = commandHelper.createServiceCall(endDevice, commandInfo);
        EndDeviceCommandContext commandContext = new EndDeviceCommandContext(endDevice, commandInfo, commandHelper);
        Optional<CompletionOptions> completionOptions = endDeviceCommand.apply(serviceCall, commandContext);

        if (!completionOptions.isPresent()) {
            serviceCall.requestTransition(DefaultState.ONGOING);
            serviceCall.requestTransition(DefaultState.FAILED);
            return new CommandRunStatusInfo(endDevice.getMRID(), CommandStatus.FAILED, endDevice.getHeadEndInterface()
                    .map(HeadEndInterface::getAmrSystem)
                    .orElse(null));
        } else {
            return new CommandRunStatusInfo(endDevice.getMRID(), CommandStatus.SUCCESS, endDevice.getHeadEndInterface()
                    .map(HeadEndInterface::getAmrSystem)
                    .orElse(null));
        }
    }
}
