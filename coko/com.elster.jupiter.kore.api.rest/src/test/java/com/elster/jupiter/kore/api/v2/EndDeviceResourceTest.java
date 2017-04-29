/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeSlice;
import com.elster.jupiter.fsm.StateTimeline;
import com.elster.jupiter.kore.api.impl.servicecall.EndDeviceCommand;
import com.elster.jupiter.kore.api.impl.servicecall.EndDeviceCommandInfo;
import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandCallbackInfo;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.ami.EndDeviceCapabilities;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.servicecall.ServiceCall;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
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
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EndDeviceResourceTest extends PlatformPublicApiJerseyTest {

    private static final String METER_MRID = "7e1d25cf-c21c-4fe4-899a-3eb07d3f2d23";

    @Mock
    Meter meter;
    @Mock
    StateTimeline timeline;
    @Mock
    StateTimeSlice timeSlice;
    @Mock
    State state;
    @Mock
    Stage stage;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        State state = mock(State.class);
        when(state.getName()).thenReturn("testState");
        when(meter.getState()).thenReturn(Optional.of(state));
        when(meter.getState(any(Instant.class))).thenReturn(Optional.of(state));
        when(meter.getId()).thenReturn(123L);
        when(meter.getMRID()).thenReturn(METER_MRID);
        when(meter.getName()).thenReturn("testName");
        when(meter.getVersion()).thenReturn(1L);
        when(meter.getStateTimeline()).thenReturn(Optional.of(timeline));
        when(timeline.getSlices()).thenReturn(Collections.singletonList(timeSlice));
        when(timeSlice.getPeriod()).thenReturn(Range.atLeast(Instant.EPOCH));
        when(timeSlice.getState()).thenReturn(state);
        when(state.getName()).thenReturn("state");
        when(state.getStage()).thenReturn(Optional.of(stage));
        when(stage.getName()).thenReturn("stage");
        when(meteringService.findMeterById(123)).thenReturn(Optional.of(meter));
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(meter));
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
        HeadEndInterface headEndInterface = mock(HeadEndInterface.class);
        CompletionOptions completionOptions = mock(CompletionOptions.class);
        when(meter.getHeadEndInterface()).thenReturn(Optional.of(headEndInterface));
        when(headEndInterface.scheduleMeterRead(eq(meter), any(), any(Instant.class), any(ServiceCall.class))).thenReturn(completionOptions);
        EndDeviceCapabilities capabilities = new EndDeviceCapabilities(Collections.emptyList(), Collections.emptyList());
        when(headEndInterface.getCapabilities(meter)).thenReturn(capabilities);
    }

    @Test
    public void testGetSingleMeterWithFields() throws Exception {
        Response response = target("enddevices/" + METER_MRID).queryParam("fields", "id,name").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(123);
        assertThat(model.<Integer>get("$.version")).isNull();
        assertThat(model.<String>get("$.link")).isNull();
    }

    @Test
    public void testGetSingleMeterAllFields() throws Exception {
        Response response = target("enddevices/"  + METER_MRID).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(123);
        assertThat(model.<Integer>get("$.version")).isEqualTo(1);
        assertThat(model.<String>get("$.name")).isEqualTo("testName");
        assertThat(model.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.link.href")).isEqualTo("http://localhost:9998/enddevices/123");
        assertThat(model.<String>get("$.lifecycleState.stage")).isEqualTo("stage");
        assertThat(model.<String>get("$.lifecycleState.name")).isEqualTo("state");
        assertThat(model.<Integer>get("$.lifecycleState.interval.start")).isEqualTo(0);
    }

    @Test
    public void testMeterFields() throws Exception {
        Response response = target("enddevices").request("application/json")
                .method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(7);
        assertThat(model.<List<String>>get("$"))
                .containsOnly("id", "lifecycleState", "link", "mRID", "name", "serialNumber", "version");
    }


    @Test
    public void testGetReadings() throws Exception {
        Response response = target("enddevices/" + METER_MRID + "/readings").queryParam("from", 1468343333330L).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$.readings")).hasSize(1);
        assertThat(model.<String>get("$.readings[0].readingType"))
                .isEqualTo("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        assertThat(model.<List>get("$.intervalBlocks")).hasSize(1);
        assertThat(model.<String>get("$.intervalBlocks[0].readingType"))
                .isEqualTo("0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
    }

    @Test
    public void testCommand() throws Exception {
        mockCommands();
        EndDeviceCommandInfo info = new EndDeviceCommandInfo();
        info.command = EndDeviceCommand.READMETER;
        info.effectiveTimestamp = 1491944400000L;
        info.httpCallBack = new UsagePointCommandCallbackInfo();
        info.httpCallBack.method = "POST";
        info.httpCallBack.successURL = "http://success";
        info.httpCallBack.partialSuccessURL = "http://successPartial";
        info.httpCallBack.failureURL = "http://fail";


        // Business method
        Response response = target("/enddevices/" + METER_MRID + "/commands").request().put(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<String>get("status")).isEqualTo("SUCCESS");
        assertThat(model.<String>get("id")).isEqualTo(METER_MRID);
    }
}
