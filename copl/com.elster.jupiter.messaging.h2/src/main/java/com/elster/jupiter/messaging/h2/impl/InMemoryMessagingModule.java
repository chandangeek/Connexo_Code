/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.messaging.h2.impl;

import com.elster.jupiter.messaging.MessageService;

import com.google.inject.AbstractModule;

import static com.elster.jupiter.messaging.MessageService.QueueTable.JUPITEREVENTS_RAW_QUEUE_TABLE;

public class InMemoryMessagingModule extends AbstractModule {

    @Override
    protected void configure() {
        TransientMessageService instance = new TransientMessageService();
        instance.createQueueTableSpec("MSG_RAWQUEUETABLE", "RAW", null, false);
        instance.createQueueTableSpec("MSG_RAWTOPICTABLE", "RAW", null, true);
        instance.createQueueTableSpec(MessageService.PRIORITIZED_RAW_QUEUE_TABLE, "RAW", null, false, true);
        instance.createQueueTableSpec(JUPITEREVENTS_RAW_QUEUE_TABLE.getQueueTableName(), "RAW", JUPITEREVENTS_RAW_QUEUE_TABLE.getStorageClause(), true);
        bind(MessageService.class).toInstance(instance);
    }

}
