/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v1;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.api.util.v1.RangeInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.BoundType;
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

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 6/2/16.
 */
public class ElectricityDetailResourceTest extends PlatformPublicApiJerseyTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Finder<UsagePoint> finder = mockFinder(Collections.emptyList());
        when(meteringService.getUsagePoints(any())).thenReturn(finder);
    }

    @Test
    public void testGetDetails() throws Exception {
        mockUsagePoint(MRID, 2L, ServiceKind.ELECTRICITY);

        // Business method
        Response response = target("/usagepoints/" + MRID + "/details/1").request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

    }

    @Test
    public void testUpdateElectricityUsagePoint() throws Exception {
        Instant now = Instant.now(clock);
        ElectricityDetailInfo info = new ElectricityDetailInfo();
        info.effectivity = new RangeInfo();
        info.effectivity.lowerEnd = now;
        info.version = 2L;

        info.collar = YesNoAnswer.YES;
        info.grounded = YesNoAnswer.YES;
        info.estimatedLoad = Quantity.create(BigDecimal.valueOf(1), "W");
        info.interruptible = YesNoAnswer.YES;
        info.limiter = YesNoAnswer.YES;
        info.loadLimit = Quantity.create(BigDecimal.valueOf(2), "W");
        info.loadLimiterType = "typel";
        info.nominalServiceVoltage = Quantity.create(BigDecimal.valueOf(3), "W");
        info.phaseCode = PhaseCode.AB;
        info.ratedCurrent = Quantity.create(BigDecimal.valueOf(4), "W");
        info.ratedPower = Quantity.create(BigDecimal.valueOf(5), "W");

        UsagePoint usagePoint = mockUsagePoint(MRID, 2L, ServiceKind.ELECTRICITY);
        ElectricityDetail electricityDetail = mock(ElectricityDetail.class);
        when(electricityDetail.getRange()).thenReturn(Range.downTo(now, BoundType.CLOSED));
        ElectricityDetailBuilder electricityDetailBuilder = FakeBuilder.initBuilderStub(electricityDetail, ElectricityDetailBuilder.class);
        when(usagePoint.newElectricityDetailBuilder(any())).thenReturn(electricityDetailBuilder);
        when(usagePoint.getId()).thenReturn(13L);
        when(meteringService.findAndLockUsagePointByIdAndVersion(13L, 2L)).thenReturn(Optional.of(usagePoint));

        // Business method
        Response response = target("/usagepoints/" + MRID + "/details").request().post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(response.getHeaderString("location")).isEqualTo("http://localhost:9998/usagepoints/" + MRID + "/details/" + now.toEpochMilli());
        verify(electricityDetailBuilder).withCollar(YesNoAnswer.YES);
        verify(electricityDetailBuilder).withGrounded(YesNoAnswer.YES);
        verify(electricityDetailBuilder).withEstimatedLoad(info.estimatedLoad);
        verify(electricityDetailBuilder).withInterruptible(YesNoAnswer.YES);
        verify(electricityDetailBuilder).withLimiter(YesNoAnswer.YES);
        verify(electricityDetailBuilder).withLoadLimit(info.loadLimit);
        verify(electricityDetailBuilder).withLoadLimiterType("typel");
        verify(electricityDetailBuilder).withNominalServiceVoltage(info.nominalServiceVoltage);
        verify(electricityDetailBuilder).withPhaseCode(PhaseCode.AB);
        verify(electricityDetailBuilder).withRatedCurrent(info.ratedCurrent);
        verify(electricityDetailBuilder).withRatedPower(info.ratedPower);
        verify(electricityDetailBuilder).create();
    }

    @Test
    public void testUpdateElectricityUsagePointMissingEffectivity() throws Exception {
        Instant now = Instant.now(clock);
        ElectricityDetailInfo info = new ElectricityDetailInfo();
        info.id = now;
        info.version = 2L;
        info.effectivity = null;

        UsagePoint usagePoint = mockUsagePoint(MRID, 2L, ServiceKind.ELECTRICITY);
        when(usagePoint.getId()).thenReturn(13L);
        when(meteringService.findAndLockUsagePointByIdAndVersion(13L, 2L)).thenReturn(Optional.of(usagePoint));

        // Business method
        Response response = target("/usagepoints/" + MRID + "/details").request().post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("effectivity.lowerEnd");
    }

    @Test
    public void testUpdateElectricityUsagePointMissingVersion() throws Exception {
        Instant now = Instant.now(clock);
        ElectricityDetailInfo info = new ElectricityDetailInfo();
        info.id = now;
        info.version = null;

        mockUsagePoint(MRID, 2L, ServiceKind.ELECTRICITY);

        // Business method
        Response response = target("/usagepoints/" + MRID + "/details").request().post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("version");
    }

    @Test
    public void testGetSingleElectricityUsagePointAllFields() throws Exception {
        ElectricityDetail electricityDetail = mock(ElectricityDetail.class);
        BigDecimal value1 = BigDecimal.valueOf(201);
        BigDecimal value2 = BigDecimal.valueOf(202);
        BigDecimal value3 = BigDecimal.valueOf(203);
        when(electricityDetail.isCollarInstalled()).thenReturn(YesNoAnswer.YES);
        when(electricityDetail.isLimiter()).thenReturn(YesNoAnswer.YES);
        when(electricityDetail.isInterruptible()).thenReturn(YesNoAnswer.YES);
        when(electricityDetail.isGrounded()).thenReturn(YesNoAnswer.YES);
        when(electricityDetail.isCurrent()).thenReturn(Boolean.TRUE);
        when(electricityDetail.getLoadLimit()).thenReturn(Quantity.create(value1, "W"));
        when(electricityDetail.getLoadLimiterType()).thenReturn("LLT");
        when(electricityDetail.getNominalServiceVoltage()).thenReturn(Quantity.create(value2, "m"));
        when(electricityDetail.getEstimatedLoad()).thenReturn(Quantity.create(value3, "g"));
        when(electricityDetail.getRange()).thenReturn(Range.downTo(clock.instant(), BoundType.CLOSED));
        mockUsagePoint(MRID, 2L, ServiceKind.ELECTRICITY, electricityDetail);

        // Business method
        Response response = target("/usagepoints/" + MRID + "/details").request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Long>get("$.id")).isEqualTo(clock.millis());
        assertThat(model.<Integer>get("$.version")).isEqualTo(2);
        assertThat(model.<String>get("$.collar")).isEqualTo("YES");
        assertThat(model.<String>get("$.limiter")).isEqualTo("YES");
        assertThat(model.<Boolean>get("$.current")).isTrue();
        assertThat(model.<String>get("$.interruptible")).isEqualTo("YES");
        assertThat(model.<String>get("$.grounded")).isEqualTo("YES");
        assertThat(model.<Integer>get("$.loadLimit.value")).isEqualTo(201);
        assertThat(model.<Integer>get("$.loadLimit.multiplier")).isEqualTo(0);
        assertThat(model.<String>get("$.loadLimit.unit")).isEqualTo("W");
        assertThat(model.<String>get("$.loadLimiterType")).isEqualTo("LLT");
        assertThat(model.<Integer>get("$.nominalServiceVoltage.value")).isEqualTo(202);
        assertThat(model.<String>get("$.nominalServiceVoltage.unit")).isEqualTo("m");
        assertThat(model.<Integer>get("$.estimatedLoad.value")).isEqualTo(203);
        assertThat(model.<String>get("$.estimatedLoad.unit")).isEqualTo("g");
        assertThat(model.<Long>get("$.effectivity.lowerEnd")).isEqualTo(clock.millis());
        assertThat(model.<Long>get("$.effectivity.upperEnd")).isNull();
        assertThat(model.<List>get("$.link")).hasSize(1);
        assertThat(model.<String>get("$.link[0].params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.link[0].href")).isEqualTo("http://localhost:9998/usagepoints/" + MRID + "/details/1462104000000");
    }

    @Test
    public void testUsagePointFields() throws Exception {
        mockUsagePoint(MRID, 1L, ServiceKind.ELECTRICITY);

        // Business method
        Response response = target("/usagepoints/" + MRID + "/details").request("application/json").method("PROPFIND", Response.class);

        // Asserts
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<List>get("$")).hasSize(16);
        Assertions.assertThat(model.<List<String>>get("$")).containsOnly(
                "collar",
                "effectivity",
                "estimatedLoad",
                "grounded",
                "id",
                "interruptible",
                "limiter",
                "link",
                "loadLimit",
                "loadLimiterType",
                "nominalServiceVoltage",
                "phaseCode",
                "ratedCurrent",
                "ratedPower",
                "version",
                "current"
        );
    }


}
