/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.rest;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmServer;
import com.elster.jupiter.issue.servicecall.rest.impl.IssueProcessInfos;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceCallIssueResourceTest extends ServiceCallIssueApplicationJerseyTest {

    @Test
    public void testGetUnexistingIssueById() {
        when(serviceCallIssueService.findIssue(1)).thenReturn(Optional.empty());

        Response response = target("issues/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetProcesses() {
        BpmServer bpmServer = mock(BpmServer.class);
        BpmProcessDefinition bpmProcessDefinition = mock(BpmProcessDefinition.class);

        when(bpmService.getActiveBpmProcessDefinitions()).thenReturn(Arrays.asList(bpmProcessDefinition));
        when(bpmProcessDefinition.getProcessName()).thenReturn("Name3");
        when(bpmProcessDefinition.getVersion()).thenReturn("1.0");
        when(bpmService.getBpmServer()).thenReturn(bpmServer);
        when(bpmServer.doGet(anyString(), anyString())).thenReturn("{\n" +
                "  \"total\": 3,\n" +
                "  \"processInstances\": [\n" +
                "    {\n" +
                "      \"status\": 2,\n" +
                "      \"processInstanceId\": 1,\n" +
                "      \"processId\": \"ID01\",\n" +
                "      \"processName\": \"Name1\",\n" +
                "      \"processVersion\": \"1.0\",\n" +
                "      \"userIdentity\": \"admin\",\n" +
                "      \"startDate\": 1456744223497,\n" +
                "      \"tasks\": []\n" +
                "    },\n" +
                "    {\n" +
                "      \"status\": 3,\n" +
                "      \"processInstanceId\": 2,\n" +
                "      \"processId\": \"ID02\",\n" +
                "      \"processName\": \"Name2\",\n" +
                "      \"processVersion\": \"2.0\",\n" +
                "      \"userIdentity\": \"admin\",\n" +
                "      \"startDate\": 1456747692220,\n" +
                "      \"tasks\": []\n" +
                "    },\n" +
                "    {\n" +
                "      \"status\": 1,\n" +
                "      \"processInstanceId\": 3,\n" +
                "      \"processId\": \"ID03\",\n" +
                "      \"processName\": \"Name3\",\n" +
                "      \"processVersion\": \"1.0\",\n" +
                "      \"userIdentity\": \"admin\",\n" +
                "      \"startDate\": 1456821940996,\n" +
                "      \"tasks\": [\n" +
                "        {\n" +
                "          \"id\": 04,\n" +
                "          \"name\": \"Task01\",\n" +
                "          \"processName\": \"Name3\",\n" +
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
        IssueProcessInfos issueProcessInfos = target("/issues/1/processes").queryParam("variableid", "issueid")
                .queryParam("variablevalue", "1")
                .request()
                .get(IssueProcessInfos.class);

        assertThat(issueProcessInfos.processes.size()).isEqualTo(3);
        assertThat(issueProcessInfos.processes.get(0).name).isEqualTo("Name1");
        assertThat(issueProcessInfos.processes.get(0).processId).isEqualTo("1");
        assertThat(issueProcessInfos.processes.get(0).startedBy).isEqualTo("admin");
        assertThat(issueProcessInfos.processes.get(0).version).isEqualTo("1.0");
        assertThat(issueProcessInfos.processes.get(0).startDate).isEqualTo("1456744223497");
        assertThat(issueProcessInfos.processes.get(1).name).isEqualTo("Name2");
        assertThat(issueProcessInfos.processes.get(1).processId).isEqualTo("2");
        assertThat(issueProcessInfos.processes.get(1).startedBy).isEqualTo("admin");
        assertThat(issueProcessInfos.processes.get(1).version).isEqualTo("2.0");
        assertThat(issueProcessInfos.processes.get(1).startDate).isEqualTo("1456747692220");
        assertThat(issueProcessInfos.processes.get(2).name).isEqualTo("Name3");
        assertThat(issueProcessInfos.processes.get(2).processId).isEqualTo("3");
        assertThat(issueProcessInfos.processes.get(2).startedBy).isEqualTo("admin");
        assertThat(issueProcessInfos.processes.get(2).version).isEqualTo("1.0");
        assertThat(issueProcessInfos.processes.get(2).startDate).isEqualTo("1456821940996");
        assertThat(issueProcessInfos.processes.get(2).openTasks.size()).isEqualTo(1);
        assertThat(issueProcessInfos.processes.get(2).openTasks.get(0).actualOwner).isEqualTo("admin");
        assertThat(issueProcessInfos.processes.get(2).openTasks.get(0).id).isEqualTo("4");
        assertThat(issueProcessInfos.processes.get(2).openTasks.get(0).priority).isEqualTo("0");
        assertThat(issueProcessInfos.processes.get(2).openTasks.get(0).name).isEqualTo("Task01");
        assertThat(issueProcessInfos.processes.get(2).openTasks.get(0).processName).isEqualTo("Name3");
        assertThat(issueProcessInfos.processes.get(2).openTasks.get(0).deploymentId).isEqualTo("com.elster.test:1.0");
        assertThat(issueProcessInfos.processes.get(2).openTasks.get(0).status).isEqualTo("Reserved");
        assertThat(issueProcessInfos.processes.get(2).openTasks.get(0).createdOn).isEqualTo("1456821941001");
    }
}
