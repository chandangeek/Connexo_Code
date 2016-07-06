package com.elster.jupiter.kore.api.impl.servicecall;


import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.ami.CompletionMessageInfo;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.servicecall.ServiceCall;

import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum UsagePointCommand {
    CONNECT((usagePoint, usagePointCommandInfo,usagePointCommandHelper) -> {
        ServiceCall serviceCall = usagePointCommandHelper.createServiceCall(usagePoint, usagePointCommandInfo);
        for (CompletionOptions options : usagePoint.connect(Instant.ofEpochMilli(usagePointCommandInfo.effectiveTimestamp), serviceCall)) {
            if (options != null) {
                options.whenFinishedSend(new CompletionMessageInfo("success", serviceCall.getId(), true), usagePointCommandHelper
                        .getDestinationSpec());
            }
        }
    }),
    DISCONNECT((usagePoint, usagePointCommandInfo, usagePointCommandHelper) -> {
        ServiceCall serviceCall = usagePointCommandHelper.createServiceCall(usagePoint, usagePointCommandInfo);
        for (CompletionOptions options : usagePoint.disconnect(Instant.ofEpochMilli(usagePointCommandInfo.effectiveTimestamp), serviceCall)) {
            if (options != null) {
                options.whenFinishedSend(new CompletionMessageInfo("success", serviceCall.getId(), true), usagePointCommandHelper
                        .getDestinationSpec());
            }
        }
    }),
    ENABLELOADLIMIT((usagePoint, usagePointCommandInfo, usagePointCommandHelper) -> {
        ServiceCall serviceCall = usagePointCommandHelper.createServiceCall(usagePoint, usagePointCommandInfo);
        for (CompletionOptions options : usagePoint.enableLoadLimit(Instant.ofEpochMilli(usagePointCommandInfo.effectiveTimestamp), usagePointCommandInfo.loadLimit, serviceCall)) {
            if (options != null) {
                options.whenFinishedSend(new CompletionMessageInfo("success", serviceCall.getId(), true), usagePointCommandHelper
                        .getDestinationSpec());
            }
        }
    }),
    DISABLELOADLIMIT((usagePoint, usagePointCommandInfo, usagePointCommandHelper) -> {
        ServiceCall serviceCall = usagePointCommandHelper.createServiceCall(usagePoint, usagePointCommandInfo);
        for (CompletionOptions options : usagePoint.disableLoadLimit(Instant.ofEpochMilli(usagePointCommandInfo.effectiveTimestamp), serviceCall)) {
            if (options != null) {
                options.whenFinishedSend(new CompletionMessageInfo("success", serviceCall.getId(), true), usagePointCommandHelper
                        .getDestinationSpec());
            }
        }
    }),
    READMETERS((usagePoint, usagePointCommandInfo, usagePointCommandHelper) -> {
        ServiceCall serviceCall = usagePointCommandHelper.createServiceCall(usagePoint, usagePointCommandInfo);
        for (CompletionOptions options : usagePoint.readData(Instant.ofEpochMilli(usagePointCommandInfo.effectiveTimestamp),
                usagePointCommandInfo.readingTypes.stream().map(usagePointCommandHelper.getMeteringService()::getReadingType)
                        .flatMap(rt -> rt.isPresent() ? Stream.of(rt.get()) : Stream.empty()).collect(Collectors.toList())
                , serviceCall)) {
            if (options != null) {
                options.whenFinishedSend(new CompletionMessageInfo("success", serviceCall.getId(), true), usagePointCommandHelper
                        .getDestinationSpec());
            }
        }
    });

    Command usagePointCommand;

    UsagePointCommand(Command usagePointCommand) {
        this.usagePointCommand = usagePointCommand;
    }

    public void process(UsagePoint usagePoint, UsagePointCommandInfo usagePointCommandInfo, UsagePointCommandHelper usagePointCommandHelper){
        usagePointCommand.process(usagePoint,usagePointCommandInfo, usagePointCommandHelper);

    }
}

interface Command{
    void process(UsagePoint usagePoint, UsagePointCommandInfo usagePointCommandInfo, UsagePointCommandHelper usagePointCommandHelper);
}