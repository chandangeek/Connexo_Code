package com.elster.jupiter.kore.api.rest.impl;

import com.elster.jupiter.kore.api.impl.MeterActivationInfo;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.rest.util.hypermedia.Relation;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MeterActivationResourceTest extends PlatformPublicApiJerseyTest {

    private UsagePoint usagePoint;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        usagePoint = mockUsagePoint(16, "up1", 1, ServiceKind.ELECTRICITY);
        MeterActivation meterActivation = mockMeterActivation(11L, 1L, 111L, usagePoint);
        MeterActivation meterActivation2 = mockMeterActivation(12L, 1L, 112L, usagePoint);
        doReturn(Arrays.asList(meterActivation, meterActivation2)).when(usagePoint).getMeterActivations();

    }

    @Test
    public void testAllGetMeterActivationsPaged() throws Exception {
        Response response = target("/usagepoints/16/meteractivations").queryParam("start", 0)
                .queryParam("limit", 10)
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<List>get("link")).hasSize(1);
        Assertions.assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        Assertions.assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        Assertions.assertThat(model.<String>get("link[0].href"))
                .isEqualTo("http://localhost:9998/usagepoints/16/meteractivations?start=0&limit=10");
        Assertions.assertThat(model.<List>get("data")).hasSize(2);
        Assertions.assertThat(model.<Integer>get("data[0].id")).isEqualTo(11);
        Assertions.assertThat(model.<Integer>get("data[0].version")).isEqualTo(1);
        Assertions.assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        Assertions.assertThat(model.<String>get("data[0].link.href"))
                .isEqualTo("http://localhost:9998/usagepoints/16/meteractivations/11");
    }

    protected MeterActivation mockMeterActivation(long id, long version, UsagePoint usagePoint) {
        Instant now = clock.instant();
        Instant later = now.plusMillis(300000);
        return mockMeterActivation(id, version, usagePoint, now, later);
    }

    protected MeterActivation mockMeterActivation(long id, long version, UsagePoint usagePoint, Instant start, Instant end) {
        MeterActivation meterActivation = mock(MeterActivation.class);
        when(meterActivation.getId()).thenReturn(id);
        when(meterActivation.getVersion()).thenReturn(version);
        when(meterActivation.getStart()).thenReturn(start);
        when(meterActivation.getEnd()).thenReturn(end);
        when(meterActivation.getInterval()).thenReturn(Interval.of(Range.closed(start, end)));
        when(meterActivation.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        when(meterActivation.getMeter()).thenReturn(Optional.empty());
        MeterRole meterRole = mock(MeterRole.class);
        when(meterRole.getKey()).thenReturn("meterRole");
        when(meterActivation.getMeterRole()).thenReturn(Optional.of(meterRole));
        return meterActivation;
    }

    protected MeterActivation mockMeterActivation(long id, long version, long meterId, UsagePoint usagePoint) {
        MeterActivation meterActivation = mockMeterActivation(id, version, usagePoint);
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(meterId);
        when(meteringService.findMeter(meterId)).thenReturn(Optional.of(meter));
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        MeterRole meterRole = mock(MeterRole.class);
        when(meterRole.getKey()).thenReturn("meterRole");
        when(meterActivation.getMeterRole()).thenReturn(Optional.of(meterRole));
        return meterActivation;
    }

    @Test
    public void testGetSingleMeterActivationWithFields() throws Exception {
        Response response = target("/usagepoints/16/meteractivations/11").queryParam("fields", "id").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<Integer>get("$.id")).isEqualTo(11);
        Assertions.assertThat(model.<Integer>get("$.version")).isNull();
        Assertions.assertThat(model.<String>get("$.link")).isNull();
        Assertions.assertThat(model.<Object>get("$.interval")).isNull();
    }

    @Test
    public void testGetSingleMeterActivationAllFields() throws Exception {
        Response response = target("/usagepoints/16/meteractivations/11").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<Integer>get("$.id")).isEqualTo(11);
        Assertions.assertThat(model.<Integer>get("$.version")).isEqualTo(1);
        Assertions.assertThat(model.<Long>get("$.interval.start")).isEqualTo(clock.millis());
        Assertions.assertThat(model.<Long>get("$.interval.end")).isEqualTo(clock.millis() + 300000L);
        Assertions.assertThat(model.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        Assertions.assertThat(model.<String>get("$.usagePoint.link.params.rel")).isEqualTo(Relation.REF_PARENT.rel());
        Assertions.assertThat(model.<String>get("$.usagePoint.link.href"))
                .isEqualTo("http://localhost:9998/usagepoints/16");
        Assertions.assertThat(model.<Integer>get("$.usagePoint.id")).isEqualTo(16);
        Assertions.assertThat(model.<String>get("$.link.href"))
                .isEqualTo("http://localhost:9998/usagepoints/16/meteractivations/11");
    }

    @Test
    public void testCreateMeterActivationWithMeter() throws Exception {
        MeterActivationInfo meterActivationInfo = new MeterActivationInfo();
        meterActivationInfo.interval = new IntervalInfo();
        meterActivationInfo.interval.start = clock.millis();
        meterActivationInfo.meter = 123L;

        Meter mock = mock(Meter.class);
        when(mock.getId()).thenReturn(123L);
        when(meteringService.findMeter(123L)).thenReturn(Optional.of(mock));
        MeterActivation meterActivation = mockMeterActivation(1001L, 1L, usagePoint);
        when(meterActivation.getMeter()).thenReturn(Optional.of(mock));
        when(usagePoint.activate(any(), any())).thenReturn(meterActivation);

        target("/usagepoints/16/meteractivations").request().post(Entity.json(meterActivationInfo));

        verify(usagePoint).activate(mock, clock.instant());

    }

    @Test
    public void testCreateMeterActivationWithoutMeter() throws Exception {
        MeterActivationInfo meterActivationInfo = new MeterActivationInfo();
        meterActivationInfo.interval = new IntervalInfo();
        meterActivationInfo.interval.start = clock.millis();

        MeterActivation meterActivation = mockMeterActivation(1001L, 1L, usagePoint);

        Response post = target("/usagepoints/16/meteractivations").request().post(Entity.json(meterActivationInfo));
        assertThat(post.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) post.getEntity());
        Assertions.assertThat(model.<String>get("$.errors[0].id")).isEqualTo("meter");

    }

    @Test
    public void testCreateMeterActivationWithoutStart() throws Exception {
        MeterActivationInfo meterActivationInfo = new MeterActivationInfo();
        meterActivationInfo.interval = new IntervalInfo();
        meterActivationInfo.interval.start = null;

        Response post = target("/usagepoints/16/meteractivations").request().post(Entity.json(meterActivationInfo));
        assertThat(post.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) post.getEntity());
        Assertions.assertThat(model.<String>get("$.errors[0].id")).isEqualTo("interval.start");
    }

    @Test
    public void testCreateMeterActivationWithInvalidStart() throws Exception {
        MeterActivationInfo meterActivationInfo = new MeterActivationInfo();
        meterActivationInfo.interval = new IntervalInfo();
        Instant now = clock.instant();
        meterActivationInfo.interval.start = now.toEpochMilli();
        Instant before = now.minus(5, ChronoUnit.MINUTES);
        Instant before2 = before.minus(5, ChronoUnit.MINUTES);
        Instant soon = now.plus(5, ChronoUnit.MINUTES);
        MeterActivation meterActivation1 = mockMeterActivation(1, 1, usagePoint, before2, before);
        MeterActivation meterActivation2 = mockMeterActivation(1, 1, usagePoint, before, soon);
        MeterActivation meterActivation3 = mockMeterActivation(1, 1, usagePoint, soon, soon.plus(5, ChronoUnit.MINUTES));
        doReturn(Arrays.asList(meterActivation1, meterActivation2, meterActivation3)).when(usagePoint)
                .getMeterActivations();
        Response post = target("/usagepoints/16/meteractivations").request().post(Entity.json(meterActivationInfo));
        assertThat(post.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) post.getEntity());
        Assertions.assertThat(model.<String>get("$.errors[0].id")).isEqualTo("interval.start");
    }

    @Test // CXO-1824
    public void testCreateMeterActivationWithoutPreviousMeterActivation() throws Exception {
        MeterActivationInfo meterActivationInfo = new MeterActivationInfo();
        meterActivationInfo.interval = new IntervalInfo();
        meterActivationInfo.meter = 123456789L;
        Instant now = clock.instant();
        meterActivationInfo.interval.start = now.toEpochMilli();
        Meter mock = mock(Meter.class);
        when(mock.getId()).thenReturn(123456789L);
        when(meteringService.findMeter(123456789L)).thenReturn(Optional.of(mock));

        MeterActivation meterActivation = mockMeterActivation(1001L, 1L, usagePoint);
        when(usagePoint.activate(any(), any())).thenReturn(meterActivation);

        Response post = target("/usagepoints/16/meteractivations").request().post(Entity.json(meterActivationInfo));
        assertThat(post.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testCreateMeterActivationInvalidMeter() throws Exception {
        MeterActivationInfo meterActivationInfo = new MeterActivationInfo();
        meterActivationInfo.interval = new IntervalInfo();
        meterActivationInfo.interval.start = clock.millis();
        meterActivationInfo.meter = 123456789L;

        when(meteringService.findMeter(123456789L)).thenReturn(Optional.empty());

        Response post = target("/usagepoints/16/meteractivations").request().post(Entity.json(meterActivationInfo));
        assertThat(post.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) post.getEntity());
        Assertions.assertThat(model.<String>get("$.errors[0].id")).isEqualTo("meter");
    }

    @Test
    public void testMeterActivationFields() throws Exception {
        Response response = target("/usagepoints/x/meteractivations").request("application/json")
                .method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<List>get("$")).hasSize(8);
        Assertions.assertThat(model.<List<String>>get("$"))
                .containsOnly("endDevice", "id", "interval", "link", "meter", "meterRole", "usagePoint", "version");
    }


}
