/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointMeterActivator;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.util.IntervalInfo;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MeterActivationResourceTest extends PlatformPublicApiJerseyTest {

    private static final String METER_MRID = "7e1d25cf-c21c-4fe4-899a-3eb07d3f2d23";

    private UsagePoint usagePoint;
    private UsagePointMetrologyConfiguration metrologyConfiguration;

    @Mock
    MeterRole meterRole;
    @Mock
    UsagePointMeterActivator linker;
    @Mock
    EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        usagePoint = mockUsagePoint(MRID, 1, ServiceKind.ELECTRICITY);
        metrologyConfiguration = mockMetrologyConfiguration(1L, "MC", 1L);
        MeterActivation meterActivation = mockMeterActivation(11L, 1L, 111L, usagePoint);
        MeterActivation meterActivation2 = mockMeterActivation(12L, 1L, 112L, usagePoint);
        doReturn(Arrays.asList(meterActivation, meterActivation2)).when(usagePoint).getMeterActivations();
        when(metrologyConfigurationService.findMeterRole("meterRole")).thenReturn(Optional.of(meterRole));
        when(usagePoint.linkMeters()).thenReturn(linker);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfigurationOnUsagePoint));
        when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
    }

    @Test
    public void testAllGetMeterActivationsPaged() throws Exception {
        // Business method
        Response response = target("/usagepoints/" + MRID + "/meteractivations").queryParam("start", 0)
                .queryParam("limit", 10)
                .request()
                .get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/usagepoints/" + MRID + "/meteractivations?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(2);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(11);
        assertThat(model.<Integer>get("data[0].version")).isEqualTo(1);
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/usagepoints/" + MRID + "/meteractivations/11");
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
        when(meteringService.findMeterById(meterId)).thenReturn(Optional.of(meter));
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        MeterRole meterRole = mock(MeterRole.class);
        when(meterRole.getKey()).thenReturn("meterRole");
        when(meterActivation.getMeterRole()).thenReturn(Optional.of(meterRole));
        return meterActivation;
    }

    @Test
    public void testGetSingleMeterActivationWithFields() throws Exception {
        // Business method
        Response response = target("/usagepoints/" + MRID + "/meteractivations/11").queryParam("fields", "id").request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(11);
        assertThat(model.<Integer>get("$.version")).isNull();
        assertThat(model.<String>get("$.link")).isNull();
        assertThat(model.<Object>get("$.interval")).isNull();
    }

    @Test
    public void testGetSingleMeterActivationAllFields() throws Exception {
        when(usagePoint.getId()).thenReturn(13L);
        // Business method
        Response response = target("/usagepoints/" + MRID + "/meteractivations/11").request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(11);
        assertThat(model.<Integer>get("$.version")).isEqualTo(1);
        assertThat(model.<Long>get("$.interval.start")).isEqualTo(clock.millis());
        assertThat(model.<Long>get("$.interval.end")).isEqualTo(clock.millis() + 300000L);
        assertThat(model.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.usagePoint.link.params.rel")).isEqualTo(Relation.REF_PARENT.rel());
        assertThat(model.<String>get("$.usagePoint.link.href")).isEqualTo("http://localhost:9998/usagepoints/" + MRID);
        assertThat(model.<Integer>get("$.usagePoint.id")).isEqualTo(13);
        assertThat(model.<String>get("$.link.href")).isEqualTo("http://localhost:9998/usagepoints/" + MRID + "/meteractivations/11");
    }

    @Test
    public void testCreateMeterActivationWithMeter() throws Exception {
        MeterActivationInfo meterActivationInfo = new MeterActivationInfo();
        meterActivationInfo.interval = new IntervalInfo();
        meterActivationInfo.interval.start = clock.millis();
        meterActivationInfo.meter = METER_MRID;
        meterActivationInfo.meterRole = "meterRole";

        Meter mock = mock(Meter.class);
        when(mock.getId()).thenReturn(123L);
        when(mock.getMRID()).thenReturn(METER_MRID);
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(mock));
        MeterActivation meterActivation = mockMeterActivation(1001L, 1L, usagePoint);
        when(meterActivation.getMeter()).thenReturn(Optional.of(mock));
        when(usagePoint.activate(any(), any())).thenReturn(meterActivation);


        // Business method
        target("/usagepoints/" + MRID + "/meteractivations").request().post(Entity.json(meterActivationInfo));

        // Asserts
        verify(linker).clear(Instant.ofEpochMilli(meterActivationInfo.interval.start), meterRole);
        verify(linker).activate(Instant.ofEpochMilli(meterActivationInfo.interval.start), mock, meterRole);
        verify(linker).complete();
    }

    @Test
    public void testCreateMeterActivationWithoutMeter() throws Exception {
        MeterActivationInfo meterActivationInfo = new MeterActivationInfo();
        meterActivationInfo.interval = new IntervalInfo();
        meterActivationInfo.interval.start = clock.millis();
        mockMeterActivation(1001L, 1L, usagePoint);

        // Business method
        Response post = target("/usagepoints/" + MRID + "/meteractivations").request().post(Entity.json(meterActivationInfo));

        // Asserts
        assertThat(post.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) post.getEntity());
        assertThat(model.<String>get("$.errors[0].id")).isEqualTo("meter");
    }

    @Test
    public void testCreateMeterActivationWithoutStart() throws Exception {
        MeterActivationInfo meterActivationInfo = new MeterActivationInfo();
        meterActivationInfo.interval = new IntervalInfo();
        meterActivationInfo.interval.start = null;

        // Business method
        Response post = target("/usagepoints/" + MRID + "/meteractivations").request().post(Entity.json(meterActivationInfo));

        // Asserts
        assertThat(post.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) post.getEntity());
        assertThat(model.<String>get("$.errors[0].id")).isEqualTo("interval.start");
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
        doReturn(Arrays.asList(meterActivation1, meterActivation2, meterActivation3)).when(usagePoint).getMeterActivations();

        // Business method
        Response post = target("/usagepoints/" + MRID + "/meteractivations").request().post(Entity.json(meterActivationInfo));

        // Asserts
        assertThat(post.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) post.getEntity());
        assertThat(model.<String>get("$.errors[0].id")).isEqualTo("interval.start");
    }

    @Test // CXO-1824
    public void testCreateMeterActivationWithoutPreviousMeterActivation() throws Exception {
        MeterActivationInfo meterActivationInfo = new MeterActivationInfo();
        meterActivationInfo.interval = new IntervalInfo();
        meterActivationInfo.meter = METER_MRID;
        meterActivationInfo.meterRole = "meterRole";
        Instant now = clock.instant();
        meterActivationInfo.interval.start = now.toEpochMilli();
        Meter mock = mock(Meter.class);
        when(mock.getId()).thenReturn(123456789L);
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(mock));

        MeterActivation meterActivation = mockMeterActivation(1001L, 1L, usagePoint);
        when(usagePoint.activate(any(), any())).thenReturn(meterActivation);

        // Business method
        Response post = target("/usagepoints/" + MRID + "/meteractivations").request().post(Entity.json(meterActivationInfo));

        // Asserts
        // Asserts
        verify(linker).clear(Instant.ofEpochMilli(meterActivationInfo.interval.start), meterRole);
        verify(linker).activate(Instant.ofEpochMilli(meterActivationInfo.interval.start), mock, meterRole);
        verify(linker).complete();
    }

    @Test
    public void testCreateMeterActivationInvalidMeter() throws Exception {
        MeterActivationInfo meterActivationInfo = new MeterActivationInfo();
        meterActivationInfo.interval = new IntervalInfo();
        meterActivationInfo.interval.start = clock.millis();
        meterActivationInfo.meter = METER_MRID;
        meterActivationInfo.meterRole = "meterRole";

        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.empty());

        // Business method
        Response post = target("/usagepoints/" + MRID + "/meteractivations").request().post(Entity.json(meterActivationInfo));

        // Asserts
        assertThat(post.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel model = JsonModel.model((ByteArrayInputStream) post.getEntity());
        assertThat(model.<String>get("$.errors[0].id")).isEqualTo("meter");
    }

    @Test
    public void testValidateMeterActivationWithMeter() throws Exception {
        MeterActivationInfo meterActivationInfo = new MeterActivationInfo();
        meterActivationInfo.interval = new IntervalInfo();
        meterActivationInfo.interval.start = clock.millis();
        meterActivationInfo.meter = METER_MRID;
        meterActivationInfo.meterRole = "meterRole";

        Meter mock = mock(Meter.class);
        when(mock.getId()).thenReturn(123L);
        when(mock.getMRID()).thenReturn(METER_MRID);
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(mock));
        MeterActivation meterActivation = mockMeterActivation(1001L, 1L, usagePoint);
        when(meterActivation.getMeter()).thenReturn(Optional.of(mock));
        when(usagePoint.activate(any(), any())).thenReturn(meterActivation);
        when(mock.getHeadEndInterface()).thenReturn(Optional.empty());

        // Business method
        Response response = target("/usagepoints/" + MRID + "/meteractivations").queryParam("validateOnly", true).request().post(Entity.json(meterActivationInfo));

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testValidateMeterActivationWithWrongMeter() throws Exception {
        MeterActivationInfo meterActivationInfo = new MeterActivationInfo();
        meterActivationInfo.interval = new IntervalInfo();
        meterActivationInfo.interval.start = clock.millis();
        meterActivationInfo.meter = METER_MRID;
        meterActivationInfo.meterRole = "meterRole";

        Meter mock = mock(Meter.class);
        when(mock.getId()).thenReturn(123L);
        when(mock.getMRID()).thenReturn(METER_MRID);
        when(meteringService.findMeterByMRID(METER_MRID)).thenReturn(Optional.of(mock));
        MeterActivation meterActivation = mockMeterActivation(1001L, 1L, usagePoint);
        when(meterActivation.getMeter()).thenReturn(Optional.of(mock));
        when(usagePoint.activate(any(), any())).thenReturn(meterActivation);
        when(mock.getHeadEndInterface()).thenReturn(Optional.empty());
        MetrologyContract contract = mock(MetrologyContract.class);
        ReadingTypeRequirement requirement = mock(ReadingTypeRequirement.class);
        MetrologyPurpose purpose = mock(MetrologyPurpose.class);
        when(contract.isMandatory()).thenReturn(true);
        when(contract.getMetrologyPurpose()).thenReturn(purpose);
        when(purpose.getName()).thenReturn("puropse");
        when(metrologyConfiguration.getMeterRoleFor(requirement)).thenReturn(Optional.of(meterRole));
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(contract));
        when(contract.getRequirements()).thenReturn(Collections.singleton(requirement));

        // Business method
        Response response = target("/usagepoints/" + MRID + "/meteractivations").queryParam("validateOnly", true).request().post(Entity.json(meterActivationInfo));

        // Asserts
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(model.<Boolean>get("success")).isEqualTo(false);
        assertThat(model.<List>get("errors")).hasSize(1);
    }

    @Test
    public void testMeterActivationFields() throws Exception {
        // Business method
        Response response = target("/usagepoints/x/meteractivations").request("application/json").method("PROPFIND", Response.class);

        // Asserts
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(8);
        assertThat(model.<List<String>>get("$")).containsOnly(
                "endDevice",
                "id",
                "interval",
                "link",
                "meter",
                "meterRole",
                "usagePoint",
                "version"
        );
    }
}
