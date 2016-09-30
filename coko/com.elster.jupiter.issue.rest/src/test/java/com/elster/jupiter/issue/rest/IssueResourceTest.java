package com.elster.jupiter.issue.rest;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.request.AssignIssueRequest;
import com.elster.jupiter.issue.rest.request.PerformActionRequest;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueShortInfo;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.IssueGroupFilter;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueGroup;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Order;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import static com.elster.jupiter.issue.rest.request.RequestHelper.FILTER;
import static com.elster.jupiter.issue.rest.request.RequestHelper.LIMIT;
import static com.elster.jupiter.issue.rest.request.RequestHelper.START;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IssueResourceTest extends IssueRestApplicationJerseyTest {
    @Mock
    IssueProvider issueProvider;

    @Mock
    InfoFactory infoFactory;

    @Mock
    IssueFilter issueFilter;

    @Test
    public void testGetAllIssuesWithoutParameters() {
        Response response = target("/issues").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetAllIssuesWithoutStartParamter() {
        Response response = target("/issues").queryParam(LIMIT, "10").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetAllIssuesWithoutLimitParamter() {
        Response response = target("/issues").queryParam(START, "0").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetAllIssuesNominalCase() {
        Optional<IssueStatus> status = Optional.of(getDefaultStatus());
        when(issueService.findStatus("open")).thenReturn(status);
        when(issueService.newIssueFilter()).thenReturn(issueFilter);
        Finder<? extends Issue> issueFinder = mock(Finder.class);
        doReturn(issueFinder).when(issueService).findIssues(any(IssueFilter.class), anyVararg());

        Optional<IssueType> issueType = Optional.of(getDefaultIssueType());
        when(issueService.findIssueType("datacollection")).thenReturn(issueType);

        List<? extends Issue> issues = Arrays.asList(getDefaultIssue(), getDefaultIssue());
        doReturn(issues).when(issueFinder).find();

        List<IssueProvider> issueProviders = Arrays.asList(issueProvider);
        doReturn(issueProviders).when(issueService).getIssueProviders();
        Optional<? extends Issue> issueRef = Optional.of(issues.get(0));
        doReturn(issueRef).when(issueProvider).findIssue(1L);
        IssueInfo issueInfo = new IssueInfo<>(issues.get(0), DeviceInfo.class);
        when(infoFactory.from(issues.get(0))).thenReturn(issueInfo);
        when(issueInfoFactoryService.getInfoFactoryFor(issues.get(0))).thenReturn(infoFactory);

        String filter = URLEncoder.encode("[{\"property\":\"status\",\"value\":[\"open\"]}]");
        Map<?, ?> map = target("/issues").queryParam(FILTER, filter).queryParam(START, "0").queryParam(LIMIT, "1").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(2);

        List<?> data = (List<?>) map.get("data");
        assertThat(data).hasSize(1);

        Map<?, ?> issueMap = (Map<?, ?>) data.get(0);
        assertDefaultIssueMap(issueMap);
    }

    @Test
    public void testGetAllComments() {
        OpenIssue issue = getDefaultIssue();
        doReturn(Optional.of(issue)).when(issueService).findIssue(1);

        List<IssueComment> comments = Arrays.asList(mockComment(1L, "My comment", getDefaultUser()));

        Query<IssueComment> commentsQuery = mock(Query.class);
        when(commentsQuery.select(Matchers.anyObject(), Matchers.<Order>anyVararg())).thenReturn(comments);

        when(issueService.query(IssueComment.class, User.class)).thenReturn(commentsQuery);
        Map<?, ?> map = target("/issues/1/comments").request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);

        Map<?, ?> commentMap = (Map<?, ?>) ((List<?>) map.get("comments")).get(0);

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
        OpenIssue issue = mock(OpenIssue.class);
        doReturn(Optional.of(issue)).when(issueService).findIssue(1L);
        IssueComment comment = mock(IssueComment.class);
        when(issue.addComment(Matchers.anyString(), Matchers.any())).thenReturn(Optional.of(comment));
        when(comment.getCreateTime()).thenReturn(Instant.now());
        User user = mockUser(1, "user");
        when(comment.getUser()).thenReturn(user);

        Map<String, String> params = new HashMap<>(1);
        params.put("comment", "Comment");
        Entity<Map<String, String>> json = Entity.json(params);

        Response response = target("/issues/1/comments").request().post(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testPostEmptyComment() {
        OpenIssue issue = mock(OpenIssue.class);
        doReturn(Optional.of(issue)).when(issueService).findIssue(1L);
        IssueComment comment = mock(IssueComment.class);
        when(issue.addComment(Matchers.anyString(), Matchers.any())).thenReturn(Optional.empty());
        Map<String, String> params = new HashMap<>(0);
        Entity<Map<String, String>> json = Entity.json(params);

        Response response = target("/issues/1/comments").request().post(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testAssignAction() {
        Entity<AssignIssueRequest> json = Entity.json(new AssignIssueRequest());
        Response response = target("issues/assign").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testPerformAction() {
        Optional<Issue> issue = Optional.of(getDefaultIssue());
        doReturn(issue).when(issueService).findIssue(1);
        doReturn(issue).when(issueService).findAndLockIssueByIdAndVersion(1, 1);

        Optional<IssueActionType> mockActionType = Optional.of(getCloseIssueActionType());
        when(issueActionService.findActionType(1)).thenReturn(mockActionType);

        PerformActionRequest request = new PerformActionRequest();
        request.id = 1;
        request.issue = new IssueShortInfo();
        request.issue.version = 1L;

        Response response = target("issues/1/actions/1").request().put(Entity.json(request));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testPerformActionOnUnexistingIssue() {
        when(issueService.findIssue(1123)).thenReturn(Optional.empty());
        when(issueService.findAndLockIssueByIdAndVersion(1123, 1)).thenReturn(Optional.empty());

        PerformActionRequest info = new PerformActionRequest();
        info.issue = new IssueShortInfo();
        info.issue.version = 1L;
        Response response = target("issues/1123/actions/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testPerformUnexistingAction() {
        Optional<Issue> issue = Optional.of(getDefaultIssue());
        doReturn(issue).when(issueService).findIssue(1);
        when(issueActionService.findActionType(1)).thenReturn(Optional.empty());

        PerformActionRequest request = new PerformActionRequest();
        request.id = 1;

        Response response = target("issues/1/action").request().put(Entity.json(request));
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGroupedList() {
        IssueGroup entity = mock(IssueGroup.class);
        when(entity.getGroupKey()).thenReturn(1L);
        when(entity.getGroupName()).thenReturn("Reason 1");
        when(entity.getCount()).thenReturn(5L);

        List<IssueGroup> groupedList = Arrays.asList(entity);
        IssueGroupFilter issueGroupFilter = mockIssueGroupFilter();
        when(issueService.newIssueGroupFilter()).thenReturn(issueGroupFilter);
        doReturn(groupedList).when(issueService).getIssueGroupList(issueGroupFilter);
        TransactionContext context = mock(TransactionContext.class);
        when(transactionService.getContext()).thenReturn(context);

        String filter = URLEncoder.encode("[{\"property\":\"field\",\"value\":\"reason\"},{\"property\":\"issueType\",\"value\":[\"datacollection\"]}]");

        Map<?, ?> map = target("issues/groupedlist")
                .queryParam("start", 0).queryParam("limit", 1).queryParam("filter", filter).request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);

        List<?> groups = (List<?>) map.get("issueGroups");
        assertThat(groups).hasSize(1);

        Map<?, ?> groupMap = (Map<?, ?>) groups.get(0);
        assertThat(groupMap.get("id")).isEqualTo(1);
        assertThat(groupMap.get("description")).isEqualTo("Reason 1");
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
        assertThat(deviceMap.get("name")).isEqualTo("DefaultDevice");
        assertThat(deviceMap.get("usagePoint")).isEqualTo(null);
        assertThat(deviceMap.get("serviceLocation")).isEqualTo(null);
        assertThat(deviceMap.get("serviceCategory")).isEqualTo(null);
        assertThat(deviceMap.get("version")).isEqualTo(0);
    }
}
