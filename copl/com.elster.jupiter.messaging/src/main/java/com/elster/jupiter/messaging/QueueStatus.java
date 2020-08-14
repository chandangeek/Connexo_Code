/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging;

import com.google.common.primitives.Longs;

public class QueueStatus {
    String queueName;
    long messagesCount;
    long errorsCount;

    public QueueStatus(String queueName, String messagesCount, String errorsCount) {
        this.queueName = queueName;
        this.messagesCount = toLong(messagesCount);
        this.errorsCount = toLong(errorsCount);
    }

    private static long toLong(String value){
        if(value != null && value.trim().length() > 0){
            return Longs.tryParse(value);
        }
        return 0;
    }

    public String getQueueName() {
        return queueName;
    }

    public long getMessagesCount() {
        return messagesCount;
    }

    public long getErrorsCount() {
        return errorsCount;
    }
}
