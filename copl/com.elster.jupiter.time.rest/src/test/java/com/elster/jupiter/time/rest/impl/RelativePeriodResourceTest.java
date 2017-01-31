/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.rest.RelativeDateInfo;
import com.elster.jupiter.time.rest.RelativePeriodInfo;
import com.elster.jupiter.util.conditions.Order;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RelativePeriodResourceTest extends TimeApplicationJerseyTest {

    private RelativePeriod mockRelativePeriod() {
        RelativePeriod period = mock(RelativePeriod.class);
        when(period.getId()).thenReturn(1L);
        when(period.getName()).thenReturn("period");
        when(period.getRelativeDateFrom()).thenReturn(RelativeDate.NOW);
        when(period.getRelativeDateTo()).thenReturn(RelativeDate.NOW);
        when(period.getRelativePeriodCategories()).thenReturn(Collections.emptyList());
        when(period.getVersion()).thenReturn(1L);
        return period;
    }

    @Test
    public void testGetAllRelativePeriods() {
        List<RelativePeriod> periods = new ArrayList<>();
        periods.add(mockRelativePeriod());
        periods.add(mockRelativePeriod());
        Query relativePeriodQuery = mock(Query.class);
        RestQuery restQuery = mock(RestQuery.class);
        when(timeService.getRelativePeriodQuery()).thenReturn(relativePeriodQuery);
        when(restQueryService.wrap(relativePeriodQuery)).thenReturn(restQuery);
        when(restQuery.select(any(QueryParameters.class), any(Order.class))).thenReturn(periods);

        String response = target("/relativeperiods").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(2);
        assertThat(model.<List>get("$.data")).isNotEmpty();
    }

    @Test
    public void testGetRelativePeriodById() {
        RelativePeriod relativePeriod = mockRelativePeriod();
        when(timeService.findRelativePeriod(1L)).thenReturn(Optional.of(relativePeriod));

        String response = target("/relativeperiods/1").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.id")).isEqualTo(1);
        assertThat(model.<String>get("$.name")).isEqualTo("period");
        assertThat(model.<Object>get("$.from")).isNotNull();
        assertThat(model.<Object>get("$.to")).isNotNull();
        assertThat(model.<List>get("$.categories")).isNotNull();
        assertThat(model.<Number>get("$.version")).isEqualTo(1);
    }

    @Test
    public void testUpdateRelativePeriod() {
        RelativePeriod relativePeriod = mockRelativePeriod();
        when(timeService.findAndLockRelativePeriodByIdAndVersion(1L, 1L)).thenReturn(Optional.of(relativePeriod));
        when(timeService.findRelativePeriod(1L)).thenReturn(Optional.of(relativePeriod));
        when(timeService.updateRelativePeriod(anyLong(), anyString(), any(RelativeDate.class), any(RelativeDate.class), any(List.class))).thenReturn(relativePeriod);
        RelativePeriodInfo info = new RelativePeriodInfo();
        info.id = 1L;
        info.version = 1L;
        info.name = "period2";
        info.from = new RelativeDateInfo(RelativeDate.NOW);
        info.to = new RelativeDateInfo(new RelativeDate("3:+:2;9:=:1;"));
        Entity<RelativePeriodInfo> json = Entity.json(info);

        Response response = target("/relativeperiods/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testUpdateRelativePeriodBadVersion() {
        RelativePeriod relativePeriod = mockRelativePeriod();
        when(timeService.findAndLockRelativePeriodByIdAndVersion(1L, 1L)).thenReturn(Optional.empty());
        when(timeService.findRelativePeriod(1L)).thenReturn(Optional.of(relativePeriod));
        RelativePeriodInfo info = new RelativePeriodInfo();
        info.id = 1L;
        info.version = 1L;
        info.name = "period2";
        info.from = new RelativeDateInfo(RelativeDate.NOW);
        info.to = new RelativeDateInfo(new RelativeDate("3:+:2;9:=:1;"));
        Entity<RelativePeriodInfo> json = Entity.json(info);

        Response response = target("/relativeperiods/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testDeleteRelativePeriod() {
        RelativePeriod relativePeriod = mockRelativePeriod();
        when(timeService.findAndLockRelativePeriodByIdAndVersion(1L, 1L)).thenReturn(Optional.of(relativePeriod));
        when(timeService.findRelativePeriod(1L)).thenReturn(Optional.of(relativePeriod));
        RelativePeriodInfo info = new RelativePeriodInfo();
        info.id = 1L;
        info.version = 1L;
        Entity<RelativePeriodInfo> json = Entity.json(info);

        Response response = target("/relativeperiods/1").request().build(HttpMethod.DELETE, json).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testDeleteRelativePeriodBadVersion() {
        RelativePeriod relativePeriod = mockRelativePeriod();
        when(timeService.findAndLockRelativePeriodByIdAndVersion(1L, 1L)).thenReturn(Optional.empty());
        when(timeService.findRelativePeriod(1L)).thenReturn(Optional.of(relativePeriod));
        RelativePeriodInfo info = new RelativePeriodInfo();
        info.id = 1L;
        info.version = 1L;
        Entity<RelativePeriodInfo> json = Entity.json(info);

        Response response = target("/relativeperiods/1").request().build(HttpMethod.DELETE, json).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }
}
