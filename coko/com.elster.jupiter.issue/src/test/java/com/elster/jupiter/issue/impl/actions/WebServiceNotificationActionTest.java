/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.actions;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.impl.service.BaseTest;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueWebServiceClient;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebServiceNotificationActionTest extends BaseTest {

    IssueAction action;

    @Before
    public void setUp() throws Exception {
        action = getDefaultActionsFactory().createIssueAction(WebServiceNotificationAction.class.getName());
    }

    @Test
    @Transactional
    public void testCreateAndExecuteAction() {
        Issue issue = createIssueMinInfo();
        EndPointConfiguration endPointConfiguration = mock(EndPointConfiguration.class);
        when(endPointConfiguration.isActive()).thenReturn(true);
        when(endPointConfiguration.getWebServiceName()).thenReturn("WSNAME");

        Map<String, Object> properties = new HashMap<>();
        WebServiceNotificationAction.EndPoint endPoint = new WebServiceNotificationAction.EndPoint(endPointConfiguration);
        properties.put(WebServiceNotificationAction.WEBSERVICE, endPoint);
        properties.put(WebServiceNotificationAction.CLOSE, true);

        Finder<EndPointConfiguration> endPointFinder = mockFinder(Collections.singletonList(endPointConfiguration));
        when(endPointFinder.stream()).thenReturn(Stream.of(endPointConfiguration));
        when(endPointConfigurationService.findEndPointConfigurations()).thenReturn(endPointFinder);

        IssueWebServiceClient issueWebServiceClient = mock(IssueWebServiceClient.class);
        when(issueWebServiceClient.getWebServiceName()).thenReturn("WSNAME");
        when(issueWebServiceClient.call(issue, endPointConfiguration)).thenReturn(true);

        ((IssueServiceImpl) issueService).addIssueWebServiceClient(issueWebServiceClient);
        IssueActionResult actionResult = action.initAndValidate(properties).execute(issue);

        assertThat(issue.getStatus().getKey()).isEqualToIgnoringCase(IssueStatus.FORWARDED);
        assertThat(issue.getStatus().isHistorical()).isTrue();
        assertThat(actionResult.isSuccess()).isTrue();
    }

    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(JsonQueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        return finder;
    }
}
