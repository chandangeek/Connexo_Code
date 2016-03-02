package com.energyict.mdc.issue.issue.datacollection.rest;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmServer;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.request.AssignIssueRequest;
import com.elster.jupiter.issue.rest.request.CloseIssueRequest;
import com.elster.jupiter.issue.rest.request.EntityReference;
import com.elster.jupiter.issue.rest.request.PerformActionRequest;
import com.elster.jupiter.issue.rest.response.issue.IssueShortInfo;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionFilter;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.rest.IssueProcessInfos;
import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.*;

import static com.elster.jupiter.issue.rest.request.RequestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

public class IssueResourceTest extends IssueDataCollectionApplicationJerseyTest {

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
        when(deviceService.findDeviceById(anyLong())).thenReturn(Optional.<Device>empty());
        Optional<IssueStatus> status = Optional.of(getDefaultStatus());
        when(issueService.findStatus("open")).thenReturn(status);

        Finder<? extends IssueDataCollection> issueFinder = mock(Finder.class);
        doReturn(issueFinder).when(issueDataCollectionService).findIssues(any(IssueDataCollectionFilter.class), anyVararg());

        Optional<IssueType> issueType = Optional.of(getDefaultIssueType());
        when(issueService.findIssueType("datacollection")).thenReturn(issueType);

        List<? extends IssueDataCollection> issues = Arrays.asList(getDefaultIssue(), getDefaultIssue());
        doReturn(issues).when(issueFinder).find();

        String filter = URLEncoder.encode("[{\"property\":\"status\",\"value\":[\"open\"]}]");
        Map<?, ?> map = target("/issues").queryParam(FILTER, filter).queryParam(START, "0").queryParam(LIMIT, "1").request().get(Map.class);
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

        Map<?, ?> map = target("/issues/1").request().get(Map.class);
        Map<?, ?> issueMap = (Map<?, ?>) map.get("data");
        assertDefaultIssueMap(issueMap);
    }

    @Test
    public void testGetUnexistingIssueById() {
        when(issueDataCollectionService.findIssue(1)).thenReturn(Optional.empty());

        Response response = target("/issues/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetAllComments() {
        OpenIssueDataCollection issue = getDefaultIssue();
        doReturn(Optional.of(issue)).when(issueDataCollectionService).findIssue(1);

        List<IssueComment> comments = Arrays.asList(mockComment(1L, "My comment", getDefaultUser()));

        Query<IssueComment> commentsQuery = mock(Query.class);
        when(commentsQuery.select(Matchers.<Condition>anyObject(), Matchers.<Order>anyVararg())).thenReturn(comments);

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
        OpenIssueDataCollection issue = mock(OpenIssueDataCollection.class);
        doReturn(Optional.of(issue)).when(issueDataCollectionService).findIssue(1L);
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
    public void testGetProcesses(){
        BpmServer bpmServer = mock(BpmServer.class);
        BpmProcessDefinition bpmProcessDefinition = mock(BpmProcessDefinition.class);

        when(bpmService.getActiveBpmProcessDefinitions()).thenReturn(Arrays.asList(bpmProcessDefinition));
        when(bpmProcessDefinition.getProcessName()).thenReturn("Name03");
        when(bpmProcessDefinition.getVersion()).thenReturn("1.0");
        when(bpmService.getBpmServer()).thenReturn(bpmServer);
        when(bpmServer.doGet(anyString(), anyString())).thenReturn("{\n" +
                "  \"total\": 3,\n" +
                "  \"processInstances\": [\n" +
                "    {\n" +
                "      \"status\": 2,\n" +
                "      \"processInstanceId\": 1,\n" +
                "      \"processId\": \"ID01\",\n" +
                "      \"processName\": \"Name01\",\n" +
                "      \"processVersion\": \"1.0\",\n" +
                "      \"userIdentity\": \"admin\",\n" +
                "      \"startDate\": 1456744223497,\n" +
                "      \"tasks\": []\n" +
                "    },\n" +
                "    {\n" +
                "      \"status\": 3,\n" +
                "      \"processInstanceId\": 2,\n" +
                "      \"processId\": \"ID02\",\n" +
                "      \"processName\": \"Name02\",\n" +
                "      \"processVersion\": \"2.0\",\n" +
                "      \"userIdentity\": \"admin\",\n" +
                "      \"startDate\": 1456747692220,\n" +
                "      \"tasks\": []\n" +
                "    },\n" +
                "    {\n" +
                "      \"status\": 1,\n" +
                "      \"processInstanceId\": 3,\n" +
                "      \"processId\": \"ID03\",\n" +
                "      \"processName\": \"Name03\",\n" +
                "      \"processVersion\": \"1.0\",\n" +
                "      \"userIdentity\": \"admin\",\n" +
                "      \"startDate\": 1456821940996,\n" +
                "      \"tasks\": [\n" +
                "        {\n" +
                "          \"id\": 04,\n" +
                "          \"name\": \"Task01\",\n" +
                "          \"processName\": \"Name03\",\n" +
                "          \"deploymentId\": \"com.elster.test:1.0\",\n" +
                "          \"dueDate\": null,\n" +
                "          \"createdOn\": 1456821941001,\n" +
                "          \"priority\": 0,\n" +
                "          \"status\": \"Reserved\",\n" +
                "          \"actualOwner\": \"admin\",\n" +
                "          \"processInstanceId\": 3\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}");
        IssueProcessInfos issueProcessInfos = target("/issues/1/processes").queryParam("variableid", "issueid").queryParam("variablevalue", "1").request().get(IssueProcessInfos.class);

        assertThat(issueProcessInfos.processes.size()).isEqualTo(3);
        assertThat(issueProcessInfos.processes.get(0).name).isEqualTo("Name01");
        assertThat(issueProcessInfos.processes.get(0).processId).isEqualTo("1");
        assertThat(issueProcessInfos.processes.get(0).startedBy).isEqualTo("admin");
        assertThat(issueProcessInfos.processes.get(0).version).isEqualTo("1.0");
        assertThat(issueProcessInfos.processes.get(0).startDate).isEqualTo("1456744223497");
        assertThat(issueProcessInfos.processes.get(1).name).isEqualTo("Name02");
        assertThat(issueProcessInfos.processes.get(1).processId).isEqualTo("2");
        assertThat(issueProcessInfos.processes.get(1).startedBy).isEqualTo("admin");
        assertThat(issueProcessInfos.processes.get(1).version).isEqualTo("2.0");
        assertThat(issueProcessInfos.processes.get(1).startDate).isEqualTo("1456747692220");
        assertThat(issueProcessInfos.processes.get(2).name).isEqualTo("Name03");
        assertThat(issueProcessInfos.processes.get(2).processId).isEqualTo("3");
        assertThat(issueProcessInfos.processes.get(2).startedBy).isEqualTo("admin");
        assertThat(issueProcessInfos.processes.get(2).version).isEqualTo("1.0");
        assertThat(issueProcessInfos.processes.get(2).startDate).isEqualTo("1456821940996");
        assertThat(issueProcessInfos.processes.get(2).openTasks.size()).isEqualTo(1);
        assertThat(issueProcessInfos.processes.get(2).openTasks.get(0).actualOwner).isEqualTo("admin");
        assertThat(issueProcessInfos.processes.get(2).openTasks.get(0).id).isEqualTo("4");
        assertThat(issueProcessInfos.processes.get(2).openTasks.get(0).priority).isEqualTo("0");
        assertThat(issueProcessInfos.processes.get(2).openTasks.get(0).name).isEqualTo("Task01");
        assertThat(issueProcessInfos.processes.get(2).openTasks.get(0).processName).isEqualTo("Name03");
        assertThat(issueProcessInfos.processes.get(2).openTasks.get(0).deploymentId).isEqualTo("com.elster.test:1.0");
        assertThat(issueProcessInfos.processes.get(2).openTasks.get(0).status).isEqualTo("Reserved");
        assertThat(issueProcessInfos.processes.get(2).openTasks.get(0).createdOn).isEqualTo("1456821941001");
    }

    @Test
    public void testPostEmptyComment() {
        OpenIssueDataCollection issue = mock(OpenIssueDataCollection.class);
        doReturn(Optional.of(issue)).when(issueDataCollectionService).findIssue(1L);
        IssueComment comment = mock(IssueComment.class);
        when(issue.addComment(Matchers.anyString(), Matchers.any())).thenReturn(Optional.empty());
        Map<String, String> params = new HashMap<>(0);
        Entity<Map<String, String>> json = Entity.json(params);

        Response response = target("/issues/1/comments").request().post(json);

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
        Response response = target("issues/close").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testAssignAction() {
        Entity<AssignIssueRequest> json = Entity.json(new AssignIssueRequest());
        Response response = target("issues/assign").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testPerformAction() {
        Optional<IssueDataCollection> issue = Optional.of(getDefaultIssue());
        doReturn(issue).when(issueDataCollectionService).findIssue(1);
        doReturn(issue).when(issueDataCollectionService).findAndLockIssueDataCollectionByIdAndVersion(1, 1);

        Optional<IssueActionType> mockActionType = Optional.of(getDefaultIssueActionType());
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
        when(issueDataCollectionService.findIssue(1123)).thenReturn(Optional.empty());
        when(issueDataCollectionService.findAndLockIssueDataCollectionByIdAndVersion(1123, 1)).thenReturn(Optional.empty());

        PerformActionRequest info = new PerformActionRequest();
        info.issue = new IssueShortInfo();
        info.issue.version = 1L;
        Response response = target("issues/1123/actions/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testPerformUnexistingAction() {
        Optional<IssueDataCollection> issue = Optional.of(getDefaultIssue());
        doReturn(issue).when(issueDataCollectionService).findIssue(1);
        when(issueActionService.findActionType(1)).thenReturn(Optional.empty());

        PerformActionRequest request = new PerformActionRequest();
        request.id = 1;

        Response response = target("issues/1/action").request().put(Entity.json(request));
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
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
