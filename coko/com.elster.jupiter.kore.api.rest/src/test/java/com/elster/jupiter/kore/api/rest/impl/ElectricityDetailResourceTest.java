package com.elster.jupiter.kore.api.rest.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.kore.api.impl.ElectricityDetailInfo;
import com.elster.jupiter.kore.api.impl.utils.RangeInfo;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.hypermedia.Relation;
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
        UsagePoint usagePoint = mockUsagePoint(31L, "usage point", 2L, ServiceKind.ELECTRICITY);
        Response response = target("/usagepoints/31/details/1").request().get();
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

        UsagePoint usagePoint = mockUsagePoint(11L, "usage point", 2L, ServiceKind.ELECTRICITY);
        ElectricityDetail electricityDetail = mock(ElectricityDetail.class);
        when(electricityDetail.getRange()).thenReturn(Range.downTo(now, BoundType.CLOSED));
        ElectricityDetailBuilder electricityDetailBuilder = FakeBuilder.initBuilderStub(electricityDetail, ElectricityDetailBuilder.class);
        when(usagePoint.newElectricityDetailBuilder(any())).thenReturn(electricityDetailBuilder);

        Response response = target("/usagepoints/11/details").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(response.getHeaderString("location")).isEqualTo("http://localhost:9998/usagepoints/11/details/" + now
                .toEpochMilli());
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

        UsagePoint usagePoint = mockUsagePoint(11L, "usage point", 2L, ServiceKind.ELECTRICITY);

        Response response = target("/usagepoints/11/details").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("effectivity.lowerEnd");
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
        UsagePoint usagePoint = mockUsagePoint(31L, "usage point", 2L, ServiceKind.ELECTRICITY, electricityDetail);

        Response response = target("/usagepoints/31/details").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<Long>get("$.id")).isEqualTo(clock.millis());
        Assertions.assertThat(model.<Integer>get("$.version")).isEqualTo(2);
        Assertions.assertThat(model.<String>get("$.collar")).isEqualTo("YES");
        Assertions.assertThat(model.<String>get("$.limiter")).isEqualTo("YES");
        Assertions.assertThat(model.<Boolean>get("$.current")).isTrue();
        Assertions.assertThat(model.<String>get("$.interruptible")).isEqualTo("YES");
        Assertions.assertThat(model.<String>get("$.grounded")).isEqualTo("YES");
        Assertions.assertThat(model.<Integer>get("$.loadLimit.value")).isEqualTo(201);
        Assertions.assertThat(model.<Integer>get("$.loadLimit.multiplier")).isEqualTo(0);
        Assertions.assertThat(model.<String>get("$.loadLimit.unit")).isEqualTo("W");
        Assertions.assertThat(model.<String>get("$.loadLimiterType")).isEqualTo("LLT");
        Assertions.assertThat(model.<Integer>get("$.nominalServiceVoltage.value")).isEqualTo(202);
        Assertions.assertThat(model.<String>get("$.nominalServiceVoltage.unit")).isEqualTo("m");
        Assertions.assertThat(model.<Integer>get("$.estimatedLoad.value")).isEqualTo(203);
        Assertions.assertThat(model.<String>get("$.estimatedLoad.unit")).isEqualTo("g");
        Assertions.assertThat(model.<Long>get("$.effectivity.lowerEnd")).isEqualTo(clock.millis());
        Assertions.assertThat(model.<Long>get("$.effectivity.upperEnd")).isNull();
        Assertions.assertThat(model.<List>get("$.link")).hasSize(1);
        Assertions.assertThat(model.<String>get("$.link[0].params.rel")).isEqualTo(Relation.REF_SELF.rel());
        Assertions.assertThat(model.<String>get("$.link[0].href"))
                .isEqualTo("http://localhost:9998/usagepoints/31/details/1462104000000");
    }

    @Test
    public void testUsagePointFields() throws Exception {
        mockUsagePoint(1, "test", 1L, ServiceKind.ELECTRICITY);
        Response response = target("/usagepoints/1/details").request("application/json")
                .method("PROPFIND", Response.class);
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
