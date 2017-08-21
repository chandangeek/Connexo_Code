/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.event;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;

import com.google.inject.Injector;

public class DelayedIssueEventHandler implements MessageHandler {
    private final Injector injector;

    public DelayedIssueEventHandler(Injector injector) {
        this.injector = injector;
    }

    @Override
    public void process(Message message) {

    }
}
