/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.data.Device;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CloseAllIssuesTest {

    @Mock
    private IssueService issueService;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private Device device;
    @Mock
    private Thesaurus thesaurus;

    @Test
    public void testGetPropertySpecsDelegatesToPropertySpecService() {
        CloseAllIssues closeAllIssues = this.getTestInstance();

        // Business method
        List<PropertySpec> propertySpecs = closeAllIssues.getPropertySpecs(this.propertySpecService);

        // Asserts
        assertThat(propertySpecs).isEmpty();
    }

    @Test
    public void noIssuesOnDeviceTest() {
        when(device.getOpenIssues()).thenReturn(Collections.emptyList());

        getTestInstance().execute(device, Instant.now(), Collections.emptyList());
        verify(issueService, never()).findStatus(anyString());
        verify(issueService, never()).getIssueProviders();
    }

    @Test
    public void closeIssuesOnDeviceTest() {
        OpenIssue openIssue1 = mock(OpenIssue.class);
        OpenIssue openIssue2 = mock(OpenIssue.class);
        when(device.getOpenIssues()).thenReturn(Arrays.asList(openIssue1, openIssue2));
        IssueStatus wontFix = mock(IssueStatus.class);
        when(issueService.findStatus(IssueStatus.WONT_FIX)).thenReturn(Optional.of(wontFix));

        getTestInstance().execute(device, Instant.now(), Collections.emptyList());

        verify(openIssue1, times(1)).close(wontFix);
        verify(openIssue2, times(1)).close(wontFix);
    }

    public CloseAllIssues getTestInstance(){
        return new CloseAllIssues(thesaurus, issueService);
    }

}