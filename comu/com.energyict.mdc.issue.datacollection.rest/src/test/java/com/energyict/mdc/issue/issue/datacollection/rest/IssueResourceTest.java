package com.energyict.mdc.issue.issue.datacollection.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.request.AssignIssueRequest;
import com.elster.jupiter.issue.rest.request.CloseIssueRequest;
import com.elster.jupiter.issue.rest.request.EntityReference;
import com.elster.jupiter.issue.rest.request.PerformActionRequest;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueGroup;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueGroupFilter;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;

import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.elster.jupiter.issue.rest.request.RequestHelper.FIELD;
import static com.elster.jupiter.issue.rest.request.RequestHelper.ISSUE_TYPE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.LIMIT;
import static com.elster.jupiter.issue.rest.request.RequestHelper.START;
import static com.elster.jupiter.issue.rest.request.RequestHelper.STATUS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IssueResourceTest extends IssueDataCollectionApplicationJerseyTest {

    @Test
    public void testGetAllIssuesWithoutParameters() {
        Response response = target("/issue").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetAllIssuesWithoutStartParamter() {
        Response response = target("/issue").queryParam(LIMIT, "10").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetAllIssuesWithoutLimitParamter() {
        Response response = target("/issue").queryParam(START, "0").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetAllIssuesNominalCase() {
        when(deviceService.findDeviceById(anyLong())).thenReturn(Optional.<Device>empty());
        Optional<IssueStatus> status = Optional.of(getDefaultStatus());
        when(issueService.findStatus("open")).thenReturn(status);

        Query<OpenIssueDataCollection> issuesQuery = mock(Query.class);
        when(issueDataCollectionService.query(OpenIssueDataCollection.class, OpenIssue.class, EndDevice.class, User.class, IssueReason.class, IssueStatus.class, IssueType.class))
                .thenReturn(issuesQuery);

        Optional<IssueType> issueType = Optional.of(getDefaultIssueType());
        when(issueService.findIssueType("datacollection")).thenReturn(issueType);

        List<OpenIssueDataCollection> issues = Arrays.asList(getDefaultIssue(), getDefaultIssue());
        when(issuesQuery.select(Matchers.<Condition>anyObject(), Matchers.eq(1), Matchers.eq(2), Matchers.<Order>anyVararg())).thenReturn(issues);

        Map<?, ?> map = target("/issue").queryParam(STATUS, "open").queryParam(START, "0").queryParam(LIMIT, "1").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(2);

        List<?> data = (List<?>) map.get("data");
        assertThat(data).hasSize(1);

        Map<?, ?> issueMap = (Map<?, ?>) data.get(0);
        assertDefaultIssueMap(issueMap);
    }

    @Test
    public void testGetIssueById() {
        when(deviceService.findDeviceById(anyLong())).thenReturn(Optional.<Device>empty());
        Optional<IssueDataCollection> issue = Optional.of(getDefaultIssue());
        doReturn(issue).when(issueDataCollectionService).findIssue(1);

        Map<?, ?> map = target("/issue/1").request().get(Map.class);
        Map<?, ?> issueMap = (Map<?, ?>) map.get("data");
        assertDefaultIssueMap(issueMap);
    }

    @Test
    public void testGetUnexistingIssueById() {
        when(issueDataCollectionService.findIssue(1)).thenReturn(Optional.empty());

        Response response = target("/issue/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetAllComments() {
        List<IssueComment> comments = Arrays.asList(mockComment(1L, "My comment", getDefaultUser()));

        Query<IssueComment> commentsQuery = mock(Query.class);
        when(commentsQuery.select(Matchers.<Condition>anyObject(), Matchers.<Order>anyVararg())).thenReturn(comments);

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
        CloseIssueRequest request = new CloseIssueRequest();
        EntityReference issueRef = new EntityReference();
        issueRef.setId(1L);
        request.issues = Arrays.asList(issueRef);
        request.status = "resolved";
        IssueStatus status = mockStatus("resolved", "Resolved", true);
        when(issueService.findStatus("resolved")).thenReturn(Optional.of(status));
        OpenIssueDataCollection issueDataCollection = mock(OpenIssueDataCollection.class);
        when(issueDataCollectionService.findOpenIssue(1L)).thenReturn(Optional.of(issueDataCollection));
        when(issueDataCollection.getStatus()).thenReturn(status);
        

        Entity<CloseIssueRequest> json = Entity.json(request);
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
        Optional<IssueDataCollection> issue = Optional.of(getDefaultIssue());
        doReturn(issue).when(issueDataCollectionService).findIssue(1);

        Optional<IssueActionType> mockActionType = Optional.of(getDefaultIssueActionType());
        when(issueActionService.findActionType(1)).thenReturn(mockActionType);

        PerformActionRequest request = new PerformActionRequest();
        request.id = 1;

        Response response = target("issue/1/actions/1").request().put(Entity.json(request));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testPerformActionOnUnexistingIssue() {
        when(issueDataCollectionService.findIssue(1123)).thenReturn(Optional.empty());

        Response response = target("issue/1123/action").request().put(Entity.json(new PerformActionRequest()));
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testPerformUnexistingAction() {
        Optional<IssueDataCollection> issue = Optional.of(getDefaultIssue());
        doReturn(issue).when(issueDataCollectionService).findIssue(1);
        when(issueActionService.findActionType(1)).thenReturn(Optional.empty());

        PerformActionRequest request = new PerformActionRequest();
        request.id = 1;

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
        IssueGroup entity = mock(IssueGroup.class);
        when(entity.getGroupKey()).thenReturn(1L);
        when(entity.getGroupName()).thenReturn("Reason 1");
        when(entity.getCount()).thenReturn(5L);

        List<IssueGroup> groupedList = Arrays.asList(entity);
        when(issueService.getIssueGroupList(Matchers.<IssueGroupFilter>anyObject())).thenReturn(groupedList);

        TransactionContext context = mock(TransactionContext.class);
        when(transactionService.getContext()).thenReturn(context);

        Map<?, ?> map = target("issue/groupedlist").queryParam(ISSUE_TYPE, "datacollection")
                .queryParam(START, 0).queryParam(LIMIT, 1).queryParam(FIELD, "reason").request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);

        List<?> groups = (List<?>) map.get("data");
        assertThat(groups).hasSize(1);

        Map<?, ?> groupMap = (Map<?, ?>) groups.get(0);
        assertThat(groupMap.get("id")).isEqualTo(1);
        assertThat(groupMap.get("reason")).isEqualTo("Reason 1");
        assertThat(groupMap.get("number")).isEqualTo(5);
    }

    private void assertDefaultIssueMap(Map<?, ?> issueMap) {
        assertThat(issueMap.get("id")).isEqualTo(1);
        assertThat(issueMap.get("version")).isEqualTo(1);
        assertThat(issueMap.get("creationDate")).isEqualTo(0);
        assertThat(issueMap.get("dueDate")).isEqualTo(0);

        Map<?, ?> reasonMap = (Map<?, ?>) issueMap.get("reason");
        assertThat(reasonMap.get("id")).isEqualTo("1");
        assertThat(reasonMap.get("name")).isEqualTo("Reason");

        Map<?, ?> statusMap = (Map<?, ?>) issueMap.get("status");
        assertThat(statusMap.get("id")).isEqualTo("1");
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
