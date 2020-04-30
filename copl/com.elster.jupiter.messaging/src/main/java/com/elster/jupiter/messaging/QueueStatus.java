/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging;

public class QueueStatus {
    String queueName;
    long messagesCount;
    long errorsCount;

    public QueueStatus(String queueName, long messagesCount, long errorsCount) {
        this.queueName = queueName;
        this.messagesCount = messagesCount;
        this.errorsCount = errorsCount;
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
