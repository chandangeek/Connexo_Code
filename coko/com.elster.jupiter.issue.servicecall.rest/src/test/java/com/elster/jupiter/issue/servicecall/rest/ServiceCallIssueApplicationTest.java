/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.rest;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.issue.servicecall.ServiceCallIssueService;
import com.elster.jupiter.issue.servicecall.rest.impl.ServiceCallIssueApplication;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.UserService;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.mockito.Mock;

import static com.elster.jupiter.issue.servicecall.rest.impl.ServiceCallIssueApplication.SERVICE_CALL_ISSUE_REST_COMPONENT;
import static org.mockito.Mockito.when;

public class ServiceCallIssueApplicationTest extends FelixRestApplicationJerseyTest {

    @Mock
    IssueService issueService;
    @Mock
    UserService userService;
    @Mock
    Thesaurus thesaurus;
    @Mock
    NlsService nlsService;
    @Mock
    BpmService bpmService;
    @Mock
    IssueActionService issueActionService;
    @Mock
    ServiceCallIssueService serviceCallIssueService;
    @Mock
    IssueCreationService issueCreationService;
    @Mock
    IssueAssignmentService issueAssignmentService;
    @Mock
    static SecurityContext securityContext;

    @Provider
    @Priority(Priorities.AUTHORIZATION)
    private static class SecurityRequestFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            requestContext.setSecurityContext(securityContext);
        }
    }

    @Override
    protected Application getApplication() {
        ServiceCallIssueApplication serviceCallIssueApplication = new ServiceCallIssueApplication() {
            //to mock security context
            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> hashSet = new HashSet<>(super.getClasses());
                hashSet.add(SecurityRequestFilter.class);
                return Collections.unmodifiableSet(hashSet);
            }
        };
        when(thesaurus.join(thesaurus)).thenReturn(thesaurus);
        when(nlsService.getThesaurus(SERVICE_CALL_ISSUE_REST_COMPONENT, Layer.REST)).thenReturn(thesaurus);
        when(nlsService.getThesaurus(ServiceCallIssueService.COMPONENT_NAME, Layer.DOMAIN)).thenReturn(thesaurus);
        when(issueService.getIssueActionService()).thenReturn(issueActionService);
        when(issueService.getIssueCreationService()).thenReturn(issueCreationService);
        when(issueService.getIssueAssignmentService()).thenReturn(issueAssignmentService);

        serviceCallIssueApplication.setTransactionService(transactionService);
        serviceCallIssueApplication.setIssueService(issueService);
        serviceCallIssueApplication.setUserService(userService);
        serviceCallIssueApplication.setNlsService(nlsService);
        serviceCallIssueApplication.setBpmService(bpmService);
        serviceCallIssueApplication.setServiceCallIssueService(serviceCallIssueService);
        return serviceCallIssueApplication;
    }
}
