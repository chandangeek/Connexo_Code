/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.rest;

import com.elster.jupiter.webservice.issue.WebServiceIssue;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
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
        assertThat(jsonModel.<Number>get("$.device")).isNull();
        assertThat(jsonModel.<Number>get("$.webServiceCallOccurrenceId")).isEqualTo(33);
    }

    @Test
    public void testGetNonexistentIssueById() {
        when(webServiceIssueService.findIssue(1)).thenReturn(Optional.empty());

        Response response = target("/issues/1").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
}
