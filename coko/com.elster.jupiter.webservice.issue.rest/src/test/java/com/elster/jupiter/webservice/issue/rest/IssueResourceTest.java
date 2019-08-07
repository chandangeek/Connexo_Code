/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.rest;

import com.elster.jupiter.webservice.issue.WebServiceIssue;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class IssueResourceTest extends WebServiceIssueApplicationJerseyTest {

    @Test
    public void testGetIssueById() {
        WebServiceIssue issue = getDefaultIssue();
        doReturn(Optional.of(issue)).when(webServiceIssueService).findIssue(1);
        when(issue.getUsagePoint()).thenReturn(Optional.empty());

        String response = target("/issues/1").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.version")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.creationDate")).isEqualTo(0);
        assertThat(jsonModel.<Number>get("$.dueDate")).isEqualTo(0);
        assertThat(jsonModel.<String>get("$.reason.id")).isEqualTo("1");
        assertThat(jsonModel.<String>get("$.reason.name")).isEqualTo("Reason");
        assertThat(jsonModel.<String>get("$.status.id")).isEqualTo("1");
        assertThat(jsonModel.<String>get("$.status.name")).isEqualTo("open");
        assertThat(jsonModel.<Boolean>get("$.status.allowForClosing")).isEqualTo(false);
        assertThat(jsonModel.<Number>get("$.assignee.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.assignee.name")).isEqualTo("Admin");
        assertThat(jsonModel.<Object>get("$.device")).isNull();
        assertThat(jsonModel.<Object>get("$.webServiceCallOccurrence")).isNotNull();
        assertThat(jsonModel.<Number>get("$.webServiceCallOccurrence.id")).isEqualTo(33);
        assertThat(jsonModel.<Object>get("$.webServiceCallOccurrence.endpoint")).isNotNull();
        assertThat(jsonModel.<Number>get("$.webServiceCallOccurrence.endpoint.id")).isEqualTo(42);
        assertThat(jsonModel.<String>get("$.webServiceCallOccurrence.endpoint.name")).isEqualTo("MyService");
        assertThat(jsonModel.<String>get("$.webServiceCallOccurrence.webServiceName")).isEqualTo("Service");
        assertThat(jsonModel.<Number>get("$.webServiceCallOccurrence.startTime")).isEqualTo(0);
        assertThat(jsonModel.<Number>get("$.webServiceCallOccurrence.endTime")).isEqualTo(2000);
        assertThat(jsonModel.<Object>get("$.webServiceCallOccurrence.status")).isNotNull();
        assertThat(jsonModel.<String>get("$.webServiceCallOccurrence.status.id")).isEqualTo("SUCCESSFUL");
        assertThat(jsonModel.<String>get("$.webServiceCallOccurrence.status.name")).isEqualTo("Successful");
        assertThat(jsonModel.<List>get("$.webServiceCallOccurrence.logs")).hasSize(2);
        assertThat(jsonModel.<Number>get("$.webServiceCallOccurrence.logs[0].timestamp")).isEqualTo(2000);
        assertThat(jsonModel.<String>get("$.webServiceCallOccurrence.logs[0].message")).isEqualTo("This is the end");
        assertThat(jsonModel.<Object>get("$.webServiceCallOccurrence.logs[0].logLevel")).isNotNull();
        assertThat(jsonModel.<String>get("$.webServiceCallOccurrence.logs[0].logLevel.id")).isEqualTo("INFO");
        assertThat(jsonModel.<String>get("$.webServiceCallOccurrence.logs[0].logLevel.name")).isEqualTo("Information");
        assertThat(jsonModel.<Number>get("$.webServiceCallOccurrence.logs[1].timestamp")).isEqualTo(0);
        assertThat(jsonModel.<String>get("$.webServiceCallOccurrence.logs[1].message")).isEqualTo("Starting");
        assertThat(jsonModel.<Object>get("$.webServiceCallOccurrence.logs[1].logLevel")).isNotNull();
        assertThat(jsonModel.<String>get("$.webServiceCallOccurrence.logs[1].logLevel.id")).isEqualTo("INFO");
        assertThat(jsonModel.<String>get("$.webServiceCallOccurrence.logs[1].logLevel.name")).isEqualTo("Information");
    }

    @Test
    public void testGetNonexistentIssueById() {
        when(webServiceIssueService.findIssue(1)).thenReturn(Optional.empty());

        Response response = target("/issues/1").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
}
