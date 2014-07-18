package com.elster.jupiter.issue.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.request.AssignIssueRequest;
import com.elster.jupiter.issue.rest.request.CloseIssueRequest;
import com.elster.jupiter.issue.rest.request.PerformActionRequest;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.GroupQueryBuilder;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.google.common.base.Optional;
import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.issue.rest.request.RequestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class IssueResourceTest extends Mocks {

    @Test
    public void testGetAllIssuesWithoutParameters() {
        Response response = target("/issue").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetAllIssuesWithoutIssueTypeParamter() {
        Response response = target("/issue").queryParam(START, "0").queryParam(LIMIT, "10").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetAllIssuesWithoutStartParamter() {
        Response response = target("/issue").queryParam(ISSUE_TYPE, "datacollection").queryParam(LIMIT, "10").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetAllIssuesWithoutLimitParamter() {
        Response response = target("/issue").queryParam(ISSUE_TYPE, "datacollection").queryParam(START, "0").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetAllIssuesNominalCase() {
        Optional<IssueStatus> status = mockOptional(getDefaultStatus());
        Query<IssueStatus> statusQuery = mock(Query.class);
        when(statusQuery.get(1L)).thenReturn(status);
        when(issueService.query(IssueStatus.class)).thenReturn(statusQuery);

        Query<Issue> issuesQuery = mock(Query.class);
        when(issueService.query(Issue.class, EndDevice.class, User.class, IssueReason.class, IssueStatus.class, AssigneeRole.class, AssigneeTeam.class, IssueType.class))
                .thenReturn(issuesQuery);

        Optional<IssueType> issueType = mockOptional(getDefaultIssueType());
        when(issueService.findIssueType("datacollection")).thenReturn(issueType);

        List<Issue> issues = Arrays.asList(getDefaultIssue());
        when(issuesQuery.select(Matchers.<Condition> anyObject(), Matchers.eq(1), Matchers.eq(1), Matchers.<Order> anyVararg())).thenReturn(issues);

        Map<?, ?> map = target("/issue").queryParam(ISSUE_TYPE, "datacollection").queryParam(STATUS, "1").queryParam(START, "0").queryParam(LIMIT, "1").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(2);

        Map<?, ?> issueMap = (Map<?, ?>) ((List<?>) map.get("data")).get(0);
        assertDefaultIssueMap(issueMap);
    }
    
    @Test
    public void testGetIssueById() {
        Optional<Issue> issue = mockOptional(getDefaultIssue());
        when(issueService.findIssue(1, true)).thenReturn(issue);

        Map<?, ?> map = target("/issue/1").request().get(Map.class);
        Map<?, ?> issueMap = (Map<?, ?>) map.get("data");
        assertDefaultIssueMap(issueMap);
    }
    
    @Test
    public void testGetUnexistingIssueById() {
        Optional<Issue> issue = mock(Optional.class);
        when(issue.isPresent()).thenReturn(false);
        when(issueService.findIssue(1, true)).thenReturn(issue);

         Response response = target("/issue/1").request().get();
         assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
    
    @Test
    public void testGetAllComments() {
        List<IssueComment> comments = Arrays.asList(mockComment(1L, "My comment", getDefaultUser()));

        Query<IssueComment> commentsQuery = mock(Query.class);
        when(commentsQuery.select(Matchers.<Condition> anyObject(), Matchers.<Order> anyVararg())).thenReturn(comments);

        when(issueService.query(IssueComment.class, User.class)).thenReturn(commentsQuery);
        Map<?, ?> map = target("/issue/1/comments").request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);
        
        Map<?, ?> commentMap = (Map<?, ?>) ((List<?>) map.get("data")).get(0);
        
        assertThat(commentMap.get("id")).isEqualTo(1);
        assertThat(commentMap.get("comment")).isEqualTo("My comment");
        assertThat(commentMap.get("creationDate")).isEqualTo(0);
        assertThat(commentMap.get("version")).isEqualTo(1);
        
        Map<?, ?> authorMap = (Map<?, ?>) commentMap.get("author");
        
        assertThat(authorMap.get("id")).isEqualTo(1);
        assertThat(authorMap.get("name")).isEqualTo("Admin");
    }
    
    @Test
    public void testPostComment() {
        Map<String, String> params = new HashMap<>(1);
        params.put("comment", "Comment");
        Entity<Map<String, String>> json = Entity.json(params);
        Response response = target("/issue/1/comments").request().post(json);
        
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }
    
    @Test
    public void testPostEmptyComment() {
        Map<String, String> params = new HashMap<>(0);
        Entity<Map<String, String>> json = Entity.json(params);
        Response response = target("/issue/1/comments").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }
    
    @Test
    public void testCloseAction() {
        Entity<CloseIssueRequest> json = Entity.json(new CloseIssueRequest());
        Response response = target("issue/close").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
    
    @Test
    public void testAssignAction() {
        Entity<AssignIssueRequest> json = Entity.json(new AssignIssueRequest());
        Response response = target("issue/assign").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());   
    }
    
    @Test
    public void testPerformAction() {
        Optional<Issue> issue = mockOptional(getDefaultIssue());
        when(issueService.findIssue(1, true)).thenReturn(issue);
        
        Optional<IssueActionType> mockActionType = mockOptional(getDefaultIssueActionType());
        when(issueActionService.findActionType(1)).thenReturn(mockActionType);
        
        PerformActionRequest request = new PerformActionRequest();
        request.setId(1);
        
        Response response = target("issue/1/action").request().put(Entity.json(request));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
    
    @Test
    public void testPerformActionOnUnexistingIssue() {
        Optional<Issue> issue = mock(Optional.class);
        when(issue.isPresent()).thenReturn(false);
        when(issueService.findIssue(1123, true)).thenReturn(issue);
        
        Response response = target("issue/1123/action").request().put(Entity.json(new PerformActionRequest()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
    
    @Test
    public void testPerformUnexistingAction() {
        Optional<Issue> issue = mockOptional(getDefaultIssue());
        when(issueService.findIssue(1, true)).thenReturn(issue);
        
        Optional<IssueActionType> mockActionType = mock(Optional.class);
        when(mockActionType.isPresent()).thenReturn(false);
        when(issueActionService.findActionType(1)).thenReturn(mockActionType);
        
        PerformActionRequest request = new PerformActionRequest();
        request.setId(1);
        
        Response response = target("issue/1/action").request().put(Entity.json(request));
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
    
    @Test
    public void testGroupedListWithoutParameters() {
        Response response = target("issue/groupedlist").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }
    
    @Test
    public void testGroupedList() {
        GroupByReasonEntity entity = mock(GroupByReasonEntity.class);
        when(entity.getId()).thenReturn(1L);
        when(entity.getReason()).thenReturn("Reason 1");
        when(entity.getCount()).thenReturn(5L);
        
        List<GroupByReasonEntity> groupedList = Arrays.asList(entity);
        when(issueService.getIssueGroupList(Matchers.<GroupQueryBuilder>anyObject())).thenReturn(groupedList);
        
        TransactionContext context = mock(TransactionContext.class);
        when(transactionService.getContext()).thenReturn(context);
        
        Map<?, ?> map = target("issue/groupedlist").queryParam(ISSUE_TYPE, "datacollection")
                .queryParam(START, 0).queryParam(LIMIT, 1).queryParam(FIELD, "reason").request().get(Map.class);
        
        assertThat(map.get("total")).isEqualTo(2);
        
        List<?> groups = (List<?>) map.get("data");
        assertThat(groups).hasSize(1);
        
        Map<?, ?> groupMap = (Map<?, ?>) groups.get(0);
        assertThat(groupMap.get("id")).isEqualTo(1);
        assertThat(groupMap.get("reason")).isEqualTo("Reason 1");
        assertThat(groupMap.get("number")).isEqualTo(5);
    }

    private void assertDefaultIssueMap(Map<?,?> issueMap) {
        assertThat(issueMap.get("id")).isEqualTo(1);
        assertThat(issueMap.get("version")).isEqualTo(1);
        assertThat(issueMap.get("creationDate")).isEqualTo(0);
        assertThat(issueMap.get("dueDate")).isEqualTo(0);

        Map<?, ?> reasonMap = (Map<?, ?>) issueMap.get("reason");
        assertThat(reasonMap.get("id")).isEqualTo(1);
        assertThat(reasonMap.get("name")).isEqualTo("Reason");

        Map<?, ?> statusMap = (Map<?, ?>) issueMap.get("status");
        assertThat(statusMap.get("id")).isEqualTo(1);
        assertThat(statusMap.get("name")).isEqualTo("open");
        assertThat(statusMap.get("allowForClosing")).isEqualTo(false);

        Map<?, ?> assigneeMap = (Map<?, ?>) issueMap.get("assignee");
        assertThat(assigneeMap.get("id")).isEqualTo(1);
        assertThat(assigneeMap.get("name")).isEqualTo("Admin");
        assertThat(assigneeMap.get("type")).isEqualTo(IssueAssignee.Types.USER);

        Map<?, ?> deviceMap = (Map<?, ?>) issueMap.get("device");
        assertThat(deviceMap.get("id")).isEqualTo(1);
        assertThat(deviceMap.get("serialNumber")).isEqualTo("0.0.0.0.0.0.0.0");
        assertThat(deviceMap.get("name")).isEqualTo(null);
        assertThat(deviceMap.get("usagePoint")).isEqualTo(null);
        assertThat(deviceMap.get("serviceLocation")).isEqualTo(null);
        assertThat(deviceMap.get("serviceCategory")).isEqualTo(null);
        assertThat(deviceMap.get("version")).isEqualTo(0);
    }
}
