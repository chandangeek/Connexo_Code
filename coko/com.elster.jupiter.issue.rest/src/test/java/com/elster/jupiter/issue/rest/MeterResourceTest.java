/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterFilter;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static com.elster.jupiter.issue.rest.request.RequestHelper.LIKE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.LIMIT;
import static com.elster.jupiter.issue.rest.request.RequestHelper.START;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MeterResourceTest extends IssueRestApplicationJerseyTest {

    @Test
    public void testGetMetersWithoutParams() {
        Response response = target("/meters").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetMetersWithoutLike() {
        List<Meter> meters = new ArrayList<>();
        meters.add(mockMeter(1, "0.0.1.2"));
        meters.add(mockMeter(2, "0.0.1.8"));
        meters.add(mockMeter(2, "0.1.1.8"));

        Finder<Meter> finder = mock(Finder.class);
        when(meteringService.findMeters(any(MeterFilter.class))).thenReturn(finder);
        when(finder.paged(0, 10)).thenReturn(finder);
        when(finder.find()).thenReturn(meters);

        Map<String, Object> map = target("/meters")
                .queryParam(START, 0)
                .queryParam(LIMIT, 10).request().get(Map.class);

        // Asserts
        ArgumentCaptor<MeterFilter> argument = ArgumentCaptor.forClass(MeterFilter.class);
        verify(meteringService).findMeters(argument.capture());
        assertThat(argument.getValue().getName()).isEqualTo("*");
        assertThat(argument.getValue().getExcludedStates()).isEmpty();

        assertThat(map.get("total")).isEqualTo(3);
        List data = (List) map.get("data");
        assertThat(data).hasSize(3);
    }

    @Test
    public void testGetMeters() {
        List<Meter> meters = new ArrayList<>();
        meters.add(mockMeter(1, "0.0.1.2"));
        meters.add(mockMeter(2, "0.0.1.8"));

        Finder<Meter> finder = mock(Finder.class);
        when(meteringService.findMeters(any(MeterFilter.class))).thenReturn(finder);
        when(finder.paged(0, 10)).thenReturn(finder);
        when(finder.find()).thenReturn(meters);

        // Business method
        Map<String, Object> map = target("/meters")
                .queryParam(START, 0)
                .queryParam(LIMIT, 10)
                .queryParam(LIKE, "0.0.").request().get(Map.class);

        // Asserts
        ArgumentCaptor<MeterFilter> argument = ArgumentCaptor.forClass(MeterFilter.class);
        verify(meteringService).findMeters(argument.capture());
        assertThat(argument.getValue().getName()).isEqualTo("*0.0.*");
        assertThat(argument.getValue().getExcludedStates()).isEmpty();

        assertThat(map.get("total")).isEqualTo(2);
        List data = (List) map.get("data");
        assertThat(data).hasSize(2);
        assertThat(((Map) data.get(1)).get("name")).isEqualTo("0.0.1.8");
    }

    @Test
    public void testGetMeterUnexisting() {
        when(meteringService.findMeterByName("test_meter")).thenReturn(Optional.empty());
        Response response = target("/meters/test_meter").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetMeter() {
        Meter meter = mockMeter(1, "0.0.0.1");
        when(meteringService.findMeterByName("0.0.0.1")).thenReturn(Optional.of(meter));

        // Business method
        Map<String, Object> map = target("/meters/0.0.0.1").request().get(Map.class);

        // Asserts
        assertThat(((Map) map.get("data")).get("id")).isEqualTo(1);
    }
}
