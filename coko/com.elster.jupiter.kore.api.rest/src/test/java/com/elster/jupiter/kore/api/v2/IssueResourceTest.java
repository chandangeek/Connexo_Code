/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IssueResourceTest extends PlatformPublicApiJerseyTest {

 /*   @Mock
    IssueFilter issueFilter;
    @Mock
    IssueType issueType;
    @Mock
    Issue issue;
    @Mock
    Finder finder;
    @Mock
    IssueStatus issueStatus;


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(issueService.newIssueFilter()).thenReturn(issueFilter);
        when(issueService.findIssueType(anyString())).thenReturn(Optional.of(issueType));
        when(issueService.findIssues(issueFilter)).thenReturn(finder);
        when(issue.getStatus()).thenReturn(issueStatus);
        when(issueStatus.isHistorical()).thenReturn(false);*/
        /* State state = mock(State.class);
        when(state.getName()).thenReturn("testState");
        when(meter.getState()).thenReturn(Optional.of(state));
        when(meter.getState(any(Instant.class))).thenReturn(Optional.of(state));
        when(meter.getId()).thenReturn(123L);
        when(meter.getName()).thenReturn("testName");
        when(meter.getVersion()).thenReturn(1L);
        when(meteringService.findMeterById(123)).thenReturn(Optional.of(meter));
        ReadingType readingType1 = mockReadingType("0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        when(readingType1.isRegular()).thenReturn(true);
        ReadingType readingType2 = mockReadingType("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        when(readingType2.isRegular()).thenReturn(false);
        when(meter.getReadingTypes(any(Range.class))).thenReturn(Stream.of(readingType1, readingType2)
                .collect(Collectors.toSet()));
        ReadingRecord record1 = mock(ReadingRecord.class);
        when(record1.getValue()).thenReturn(BigDecimal.TEN);
        when(record1.getReadingType()).thenReturn(readingType2);
        ReadingRecord record2 = mock(ReadingRecord.class);
        when(record2.getValue()).thenReturn(BigDecimal.ONE);
        when(record2.getReadingType()).thenReturn(readingType2);
        doReturn(Collections.singletonList(record1)).when(meter).getReadings(any(Range.class), eq(readingType1));
        doReturn(Collections.singletonList(record1)).when(meter).getReadings(any(Range.class), eq(readingType2));

    }

    @Test
    public void testGetAllIssues() throws Exception {
        Response response = target("issues").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.version")).isEqualTo(1);
        assertThat(model.<String>get("$.name")).isEqualTo("testName");
        assertThat(model.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.link.href")).isEqualTo("http://localhost:9998/issues");
    }

   @Test
    public void testIssueFields() throws Exception {
        Response response = target("issues").request("application/json")
                .method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(14);
        assertThat(model.<List<String>>get("$"))
                .containsOnly("id", "title", "issueId", "issueType", "reason", "status", "priority", "priorityValue", "userAssignee", "workGroupAssignee", "device", "dueDate", "creationDate", "version");
    }
*/
/*
    @Test
    public void testGetReadings() throws Exception {
        Response response = target("enddevices/123/readings").queryParam("from", 1468343333330L).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$.readings")).hasSize(1);
        assertThat(model.<String>get("$.readings[0].readingType"))
                .isEqualTo("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        assertThat(model.<List>get("$.intervalBlocks")).hasSize(1);
        assertThat(model.<String>get("$.intervalBlocks[0].readingType"))
                .isEqualTo("0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
    } */
}