/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.servicecall.ServiceCall;

import java.util.List;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-09-26 (12:55)
 */
interface Command {
    List<CompletionOptions> process(ServiceCall serviceCall, UsagePoint usagePoint, UsagePointCommandInfo usagePointCommandInfo, UsagePointCommandHelper usagePointCommandHelper);

    static void updateCallback(List<CompletionOptions> completionOptionsList, ServiceCall serviceCall, DestinationSpec destinationSpec) {
        for (CompletionOptions options : completionOptionsList) {
            if (options != null) {
                options.whenFinishedSendCompletionMessageWith(String.valueOf(serviceCall.getId()), destinationSpec);
            }
        }
    }
}