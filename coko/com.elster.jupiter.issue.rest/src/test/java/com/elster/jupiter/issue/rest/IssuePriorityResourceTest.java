package com.elster.jupiter.issue.rest;


import com.elster.jupiter.issue.rest.request.SetPriorityRequest;
import com.elster.jupiter.issue.rest.response.PriorityInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueShortInfo;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.Issue;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

public class IssuePriorityResourceTest extends IssueRestApplicationJerseyTest{

    @Test
    public void testSetIssuePriority(){
        Optional<Issue> issue = Optional.of(getDefaultIssue());
        doReturn(issue).when(issueService).findIssue(1);
        doReturn(issue).when(issueService).findAndLockIssueByIdAndVersion(1, 1);

        SetPriorityRequest priorityRequest= new SetPriorityRequest();
        priorityRequest.id = 1L;
        priorityRequest.issue = new IssueShortInfo(1L);
        priorityRequest.priority = new PriorityInfo(Priority.DEFAULT);
        priorityRequest.issue.version = 1L;

        Response response = target("1/priority").request().put(Entity.json(priorityRequest));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

}
