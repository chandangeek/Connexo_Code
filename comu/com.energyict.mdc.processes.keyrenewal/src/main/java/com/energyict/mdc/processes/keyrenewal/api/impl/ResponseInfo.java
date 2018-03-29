/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api.impl;

import static com.elster.jupiter.metering.ami.CompletionMessageInfo.CompletionMessageStatus;
import static com.elster.jupiter.metering.ami.CompletionMessageInfo.FailureReason;

public class ResponseInfo {

    public CompletionMessageStatus status;
    public FailureReason reason;

    @Override
    public String toString() {
        return "ResponseInfo{" +
                "status=" + status +
                ", reason=" + reason +
                '}';
    }
}