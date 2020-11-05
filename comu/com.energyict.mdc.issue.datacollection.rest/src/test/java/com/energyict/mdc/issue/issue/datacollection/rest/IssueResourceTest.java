/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.issue.datacollection.rest;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmServer;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.energyict.mdc.issue.datacollection.rest.IssueProcessInfos;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IssueResourceTest extends IssueDataCollectionApplicationJerseyTest {

    @Test
    public void testGetIssueById() {
        when(deviceService.findDeviceById(anyLong())).thenReturn(Optional.empty());
        Optional<IssueDataCollection> issue = Optional.of(getDefaultIssue());
        doReturn(issue).when(issueDataCollectionService).findIssue(1);
        //TODO: refactor kore so that usage point only dirrectly accessible from MDM issues
        when(issue.get().getUsagePoint()).thenReturn(Optional.empty());

        Map<?, ?> map = target("/issues/1").request().get(Map.class);
        Map<?, ?> issueMap = (Map<?, ?>) map.get("data");
        assertDefaultIssueMap(issueMap);
    }

    @Test
    public void testGetUnexistingIssueById() {
        when(issueDataCollectionService.findIssue(1)).thenReturn(Optional.empty());

        Response response = target("issues/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetProcesses() {
        BpmServer bpmServer = mock(BpmServer.class);
        BpmProcessDefinition bpmProcessDefinition = mock(BpmProcessDefinition.class);
        //IssueProcessInfos issueProcessInfos=mock(IssueProcessInfos.class);

        when(bpmService.getActiveBpmProcessDefinitions()).thenReturn(Arrays.asList(bpmProcessDefinition));
        when(bpmProcessDefinition.getProcessName()).thenReturn("Name03");
        when(bpmProcessDefinition.getVersion()).thenReturn("1.0");
        when(bpmService.getBpmServer()).thenReturn(bpmServer);
        when(bpmServer.doGet(anyString(), anyString())).thenReturn("{\n" +
                "  \"total\": 3,\n" +
                "  \"processInstances\": [\n" +
                "    {\n" +
                "      \"name\": \"Name01\",\n" +
                "      \"startDate\": 1456744223497,\n" +
                "      \"version\": \"1.0\",\n" +
                "      \"startedBy\": \"admin\",\n" +
                "      \"processId\": \"1\",\n" +
                "      \"status\": 2,\n" +
                "      \"openTasks\": []\n" +
                "    },\n" +
                "    {\n" +
                "    \"name\": \"Name02\",\n" +
                "    \"startDate\": 1456747692220,\n" +
                "    \"version\": \"2.0\",\n" +
                "    \"startedBy\": \"admin\",\n" +
                "    \"processId\": \"2\",\n" +
                "    \"status\": 3,\n" +
                "    \"openTasks\": []\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Name03\",\n" +
                "      \"startDate\": 1456821940996,\n" +
                "      \"version\": \"1.0\",\n" +
                "      \"startedBy\": \"admin\",\n" +
                "      \"processId\": \"3\",\n" +
                "      \"status\": 1,\n" +
                "      \"openTasks\": [\n" +
                "        {\n" +
                "          \"id\": 04,\n" +
                "          \"priority\": 0,\n" +
                "          \"name\": \"Task01\",\n" +
                "          \"processName\": \"Name03\",\n" +
                "          \"deploymentId\": \"com.elster.test:1.0\",\n" +
                "          \"dueDate\": null,\n" +
                "          \"createdOn\": 1456821941001,\n" +
                "          \"status\": \"Reserved\",\n" +
                "          \"actualOwner\": \"admin\",\n" +
                "          \"processInstanceId\": 3\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}");
       // when(issueProcessInfos.processes.get(0)).thenReturn(bpmService.)
        IssueProcessInfos issueProcessInfos = target("/issues/1/processes").queryParam("variableid", "issueid")
                .queryParam("variablevalue", "1")
                .request()
                .get(IssueProcessInfos.class);


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
