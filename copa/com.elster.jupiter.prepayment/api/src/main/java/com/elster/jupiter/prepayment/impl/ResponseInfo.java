/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.prepayment.impl;

import static com.elster.jupiter.metering.ami.CompletionMessageInfo.CompletionMessageStatus;
import static com.elster.jupiter.metering.ami.CompletionMessageInfo.FailureReason;

/**
 * @author sva
 * @since 18/07/2016 - 10:22
 */
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