/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.issue.IssueServiceCallService;
import com.elster.jupiter.util.json.JsonService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.servicecall.issue.impl",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=SerrviceCallStateChangeHandler")
public class SerrviceCallStateChangeHandler implements ServiceCallHandler {

    private MessageService messageService;
    private JsonService jsonService;
    private IssueServiceCallService issueServiceCallService;

    public SerrviceCallStateChangeHandler() {
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Reference
    public void setIssueServiceCallService(IssueServiceCallService issueServiceCallService) {
        this.issueServiceCallService = issueServiceCallService;
    }

    @Override
    public void onChildStateChange(ServiceCall parentServiceCall, ServiceCall childServiceCall, DefaultState oldState, DefaultState newState) {

    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        if (!newState.isOpen()) {
            return;
        }
    }
}
