/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v1;

import com.elster.jupiter.fsm.State;
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

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EndDeviceResourceTest extends PlatformPublicApiJerseyTest {

    @Mock
    Meter meter;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        State state = mock(State.class);
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
    public void testGetSingleMeterWithFields() throws Exception {
        Response response = target("enddevices/123").queryParam("fields", "id,name").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<Integer>get("$.id")).isEqualTo(123);
        Assertions.assertThat(model.<Integer>get("$.version")).isNull();
        Assertions.assertThat(model.<String>get("$.link")).isNull();
    }

    @Test
    public void testGetSingleMeterAllFields() throws Exception {
        Response response = target("enddevices/123").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<Integer>get("$.id")).isEqualTo(123);
        Assertions.assertThat(model.<Integer>get("$.version")).isEqualTo(1);
        Assertions.assertThat(model.<String>get("$.name")).isEqualTo("testName");
        Assertions.assertThat(model.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        Assertions.assertThat(model.<String>get("$.link.href"))
                .isEqualTo("http://localhost:9998/enddevices/123");
    }

    @Test
    public void testMeterFields() throws Exception {
        Response response = target("enddevices").request("application/json")
                .method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<List>get("$")).hasSize(7);
        Assertions.assertThat(model.<List<String>>get("$"))
                .containsOnly("id", "lifecycleState", "link", "mRID", "name", "serialNumber", "version");
    }


    @Test
    public void testGetReadings() throws Exception {
        Response response = target("enddevices/123/readings").queryParam("from", 1468343333330L).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<List>get("$.readings")).hasSize(1);
        Assertions.assertThat(model.<String>get("$.readings[0].readingType"))
                .isEqualTo("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        Assertions.assertThat(model.<List>get("$.intervalBlocks")).hasSize(1);
        Assertions.assertThat(model.<String>get("$.intervalBlocks[0].readingType"))
                .isEqualTo("0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
    }
}
