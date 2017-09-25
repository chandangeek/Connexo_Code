/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueWebServiceClient;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.impl.DeviceAlarmServiceImpl;
import com.energyict.mdc.device.alarms.impl.actions.WebServiceNotificationAlarmAction;

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

/**
 * Created by H165696 on 9/7/2017.
 */
public class WebServiceNotificationAlarmActionTest extends BaseTest {

    IssueAction action;

    @Before
    public void setUp() throws Exception {
        action = getDefaultActionsFactory().createIssueAction(WebServiceNotificationAlarmAction.class.getName());
    }

    @Test
    @Transactional
    public void testCreateAndExecuteAction() {
        DeviceAlarm issue = createAlarmMinInfo();
        EndPointConfiguration endPointConfiguration = mock(EndPointConfiguration.class);
        when(endPointConfiguration.isActive()).thenReturn(true);
        when(endPointConfiguration.getWebServiceName()).thenReturn("WSNAME");

        Map<String, Object> properties = new HashMap<>();
        WebServiceNotificationAlarmAction.EndPoint endPoint = new WebServiceNotificationAlarmAction.EndPoint(endPointConfiguration);
        properties.put(WebServiceNotificationAlarmAction.WEBSERVICE, endPoint);
        properties.put(WebServiceNotificationAlarmAction.CLOSE, true);

        Finder<EndPointConfiguration> endPointFinder = mockFinder(Collections.singletonList(endPointConfiguration));
        when(endPointFinder.stream()).thenReturn(Stream.of(endPointConfiguration));
        when(endPointConfigurationService.findEndPointConfigurations()).thenReturn(endPointFinder);

        IssueWebServiceClient issueWebServiceClient = mock(IssueWebServiceClient.class);
        when(issueWebServiceClient.getWebServiceName()).thenReturn("WSNAME");
        when(issueWebServiceClient.call(issue, endPointConfiguration)).thenReturn(true);

        ((DeviceAlarmServiceImpl) deviceAlarmService).addIssueWebServiceClient(issueWebServiceClient);
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
