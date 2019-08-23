/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */


package com.elster.jupiter.issue.servicecall.rest;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.issue.servicecall.ServiceCallIssueService;
import com.elster.jupiter.issue.servicecall.rest.impl.ServiceCallIssueApplication;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.users.UserService;

import javax.ws.rs.core.Application;

import org.mockito.Mock;

import static org.mockito.Mockito.when;

public abstract class ServiceCallIssueApplicationJerseyTest extends FelixRestApplicationJerseyTest {

    @Mock
    UserService userService;
    @Mock
    IssueService issueService;
    @Mock
    ServiceCallIssueService serviceCallIssueService;
    @Mock
    IssueActionService issueActionService;
    @Mock
    BpmService bpmService;

    @Override
    protected Application getApplication() {
        ServiceCallIssueApplication application = new ServiceCallIssueApplication();
        when(issueService.getIssueActionService()).thenReturn(issueActionService);
        application.setTransactionService(transactionService);
        application.setIssueService(issueService);
        application.setUserService(userService);
        application.setNlsService(nlsService);
        application.setBpmService(bpmService);
        application.setServiceCallIssueService(serviceCallIssueService);
        return application;
    }
}
