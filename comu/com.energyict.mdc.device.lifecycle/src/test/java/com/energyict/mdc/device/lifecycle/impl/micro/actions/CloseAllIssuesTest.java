package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Copyrights EnergyICT
 * Date: 25/06/15
 * Time: 09:11
 */
@RunWith(MockitoJUnitRunner.class)
public class CloseAllIssuesTest {

    @Mock
    private IssueService issueService;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private IssueProvider issueProvider;
    @Mock
    private Device device;

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

        getTestInstance().execute(device, Collections.emptyList());
        verify(issueService, never()).findStatus(anyString());
        verify(issueService, never()).getIssueProviders();
    }

    @Test
    public void closeIssuesOnDeviceTest() {
        OpenIssue openIssue1 = mock(OpenIssue.class);
        OpenIssue openIssue2 = mock(OpenIssue.class);
        OpenIssueDataCollection openIssueDataCollection1 = mock(OpenIssueDataCollection.class);
        OpenIssueDataCollection openIssueDataCollection2 = mock(OpenIssueDataCollection.class);
        when(issueService.getIssueProviders()).thenReturn(Collections.singletonList(issueProvider));
        doReturn(Optional.of(openIssueDataCollection1)).when(issueProvider).getOpenIssue(openIssue1);
        doReturn(Optional.of(openIssueDataCollection2)).when(issueProvider).getOpenIssue(openIssue2);
        when(device.getOpenIssues()).thenReturn(Arrays.asList(openIssue1, openIssue2));
        IssueStatus wontFix = mock(IssueStatus.class);
        when(issueService.findStatus(IssueStatus.WONT_FIX)).thenReturn(Optional.of(wontFix));

        getTestInstance().execute(device, Collections.emptyList());

        verify(openIssueDataCollection1, times(1)).close(wontFix);
        verify(openIssueDataCollection2, times(1)).close(wontFix);
    }

    public CloseAllIssues getTestInstance(){
        return new CloseAllIssues(issueService);
    }

}