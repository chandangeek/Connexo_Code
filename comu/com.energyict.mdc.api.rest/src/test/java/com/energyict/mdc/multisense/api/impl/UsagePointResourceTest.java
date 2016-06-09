package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.GasDetailBuilder;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsagePointResourceTest extends MultisensePublicApiJerseyTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Finder<UsagePoint> finder = mockFinder(Collections.emptyList());
        when(meteringService.getUsagePoints(any())).thenReturn(finder);
    }

    @Test
    public void testAllGetUsagePointsPaged() throws Exception {
        Response response = target("/usagepoints").queryParam("start", 0).queryParam("limit", 10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetSingleUsagePointWithFields() throws Exception {
        UsagePoint usagePoint = mockUsagePoint(31L, "usage point", 2L, ServiceKind.ELECTRICITY);
        Response response = target("/usagepoints/31").queryParam("fields", "id,name").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<Integer>get("$.version")).isNull();
        assertThat(model.<String>get("$.name")).isEqualTo("usage point");
        assertThat(model.<String>get("$.link")).isNull();
        assertThat(model.<String>get("$.readRoute")).isNull();
    }

    @Test
    public void testGetSingleGasUsagePointAllFields() throws Exception {
        GasDetail gasDetail = mock(GasDetail.class);
        when(gasDetail.getBypassStatus()).thenReturn(BypassStatus.CLOSED);
        BigDecimal value1 = BigDecimal.valueOf(101);
        BigDecimal value2 = BigDecimal.valueOf(102);
        BigDecimal value3 = BigDecimal.valueOf(103);
        when(gasDetail.getLoadLimit()).thenReturn(Quantity.create(value1, "W"));
        when(gasDetail.getLoadLimiterType()).thenReturn("LLT");
        when(gasDetail.getPhysicalCapacity()).thenReturn(Quantity.create(value2, "m"));
        when(gasDetail.getPressure()).thenReturn(Quantity.create(value3, "kg"));
        when(gasDetail.isBypassInstalled()).thenReturn(YesNoAnswer.YES);
        when(gasDetail.isCapped()).thenReturn(YesNoAnswer.YES);
        when(gasDetail.isClamped()).thenReturn(YesNoAnswer.YES);
        when(gasDetail.isGrounded()).thenReturn(YesNoAnswer.YES);
        when(gasDetail.isLimiter()).thenReturn(YesNoAnswer.YES);
        when(gasDetail.isValveInstalled()).thenReturn(YesNoAnswer.YES);
        when(gasDetail.isInterruptible()).thenReturn(YesNoAnswer.YES);
        when(gasDetail.isCollarInstalled()).thenReturn(YesNoAnswer.YES);
        UsagePoint usagePoint = mockUsagePoint(31L, "usage point", 2L, ServiceKind.GAS, gasDetail);
        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(13L, "metro", 1);

        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        Response response = target("/usagepoints/31").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<Integer>get("$.version")).isEqualTo(2);
        assertThat(model.<String>get("$.name")).isEqualTo("usage point");
        assertThat(model.<String>get("$.location")).isEqualTo("location");
        assertThat(model.<String>get("$.mrid")).isEqualTo("MRID");
        assertThat(model.<String>get("$.readRoute")).isEqualTo("read route");
        assertThat(model.<String>get("$.serviceKind")).isEqualTo("Gas");
        assertThat(model.<String>get("$.bypass")).isEqualTo("YES");
        assertThat(model.<String>get("$.capped")).isEqualTo("YES");
        assertThat(model.<String>get("$.collar")).isEqualTo("YES");
        assertThat(model.<String>get("$.interruptible")).isEqualTo("YES");
        assertThat(model.<String>get("$.clamped")).isEqualTo("YES");
        assertThat(model.<String>get("$.grounded")).isEqualTo("YES");
        assertThat(model.<String>get("$.limiter")).isEqualTo("YES");
        assertThat(model.<String>get("$.valve")).isEqualTo("YES");
        assertThat(model.<String>get("$.bypassStatus")).isEqualTo("CLOSED");
        assertThat(model.<Integer>get("$.loadLimit.value")).isEqualTo(101);
        assertThat(model.<Integer>get("$.loadLimit.multiplier")).isEqualTo(0);
        assertThat(model.<String>get("$.loadLimit.unit")).isEqualTo("W");
        assertThat(model.<String>get("$.loadLimiterType")).isEqualTo("LLT");
        assertThat(model.<Integer>get("$.physicalCapacity.value")).isEqualTo(102);
        assertThat(model.<Integer>get("$.physicalCapacity.multiplier")).isEqualTo(0);
        assertThat(model.<String>get("$.physicalCapacity.unit")).isEqualTo("m");
        assertThat(model.<Integer>get("$.pressure.value")).isEqualTo(103);
        assertThat(model.<Integer>get("$.pressure.multiplier")).isEqualTo(0);
        assertThat(model.<String>get("$.pressure.unit")).isEqualTo("kg");
        assertThat(model.<Integer>get("$.metrologyConfiguration.id")).isEqualTo(13);
        assertThat(model.<String>get("$.metrologyConfiguration.link.href")).isEqualTo("http://localhost:9998/metrologyconfigurations/13");
        assertThat(model.<Long>get("$.installationTime")).isEqualTo(LocalDateTime.of(2016, 3, 20, 11, 0)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli());
        assertThat(model.<String>get("$.description")).isEqualTo("usage point desc");
        assertThat(model.<String>get("$.serviceDeliveryRemark")).isEqualTo("remark");
        assertThat(model.<String>get("$.servicePriority")).isEqualTo("service priority");
        assertThat(model.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.link.href")).isEqualTo("http://localhost:9998/usagepoints/31");
    }

    @Test
    public void testGetSingleWaterUsagePointAllFields() throws Exception {
        WaterDetail waterDetail = mock(WaterDetail.class);
        BigDecimal value1 = BigDecimal.valueOf(201);
        BigDecimal value2 = BigDecimal.valueOf(202);
        BigDecimal value3 = BigDecimal.valueOf(203);
        when(waterDetail.getBypassStatus()).thenReturn(BypassStatus.CLOSED);
        when(waterDetail.getLoadLimit()).thenReturn(Quantity.create(value1, "W"));
        when(waterDetail.getLoadLimiterType()).thenReturn("LLT");
        when(waterDetail.getPhysicalCapacity()).thenReturn(Quantity.create(value2, "m"));
        when(waterDetail.getPressure()).thenReturn(Quantity.create(value3, "kg"));
        when(waterDetail.isBypassInstalled()).thenReturn(YesNoAnswer.YES);
        when(waterDetail.isCapped()).thenReturn(YesNoAnswer.YES);
        when(waterDetail.isClamped()).thenReturn(YesNoAnswer.YES);
        when(waterDetail.isGrounded()).thenReturn(YesNoAnswer.YES);
        when(waterDetail.isLimiter()).thenReturn(YesNoAnswer.YES);
        when(waterDetail.isValveInstalled()).thenReturn(YesNoAnswer.YES);
        when(waterDetail.isCollarInstalled()).thenReturn(YesNoAnswer.YES);
        UsagePoint usagePoint = mockUsagePoint(31L, "usage point", 2L, ServiceKind.WATER, waterDetail);

        Response response = target("/usagepoints/31").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<Integer>get("$.version")).isEqualTo(2);
        assertThat(model.<String>get("$.name")).isEqualTo("usage point");
        assertThat(model.<String>get("$.location")).isEqualTo("location");
        assertThat(model.<String>get("$.mrid")).isEqualTo("MRID");
        assertThat(model.<String>get("$.readRoute")).isEqualTo("read route");
        assertThat(model.<String>get("$.serviceKind")).isEqualTo("Water");
        assertThat(model.<String>get("$.bypass")).isEqualTo("YES");
        assertThat(model.<String>get("$.collar")).isEqualTo("YES");
        assertThat(model.<String>get("$.capped")).isEqualTo("YES");
        assertThat(model.<String>get("$.clamped")).isEqualTo("YES");
        assertThat(model.<String>get("$.grounded")).isEqualTo("YES");
        assertThat(model.<String>get("$.limiter")).isEqualTo("YES");
        assertThat(model.<String>get("$.valve")).isEqualTo("YES");
        assertThat(model.<String>get("$.bypassStatus")).isEqualTo("CLOSED");
        assertThat(model.<Integer>get("$.loadLimit.value")).isEqualTo(201);
        assertThat(model.<Integer>get("$.loadLimit.multiplier")).isEqualTo(0);
        assertThat(model.<String>get("$.loadLimit.unit")).isEqualTo("W");
        assertThat(model.<String>get("$.loadLimiterType")).isEqualTo("LLT");
        assertThat(model.<Integer>get("$.physicalCapacity.value")).isEqualTo(202);
        assertThat(model.<Integer>get("$.physicalCapacity.multiplier")).isEqualTo(0);
        assertThat(model.<String>get("$.physicalCapacity.unit")).isEqualTo("m");
        assertThat(model.<Integer>get("$.pressure.value")).isEqualTo(203);
        assertThat(model.<Integer>get("$.pressure.multiplier")).isEqualTo(0);
        assertThat(model.<String>get("$.pressure.unit")).isEqualTo("kg");
        assertThat(model.<Long>get("$.installationTime")).isEqualTo(LocalDateTime.of(2016, 3, 20, 11, 0)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli());
        assertThat(model.<String>get("$.description")).isEqualTo("usage point desc");
        assertThat(model.<String>get("$.serviceDeliveryRemark")).isEqualTo("remark");
        assertThat(model.<String>get("$.servicePriority")).isEqualTo("service priority");
        assertThat(model.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.link.href")).isEqualTo("http://localhost:9998/usagepoints/31");
    }

    @Test
    public void testGetSingleHeatUsagePointAllFields() throws Exception {
        HeatDetail heatDetail = mock(HeatDetail.class);
        BigDecimal value1 = BigDecimal.valueOf(301);
        BigDecimal value2 = BigDecimal.valueOf(302);
        when(heatDetail.getBypassStatus()).thenReturn(BypassStatus.CLOSED);
        when(heatDetail.getPressure()).thenReturn(Quantity.create(value1, "kg"));
        when(heatDetail.getPhysicalCapacity()).thenReturn(Quantity.create(value2, "m"));
        when(heatDetail.isBypassInstalled()).thenReturn(YesNoAnswer.YES);
        when(heatDetail.isCollarInstalled()).thenReturn(YesNoAnswer.YES);
        when(heatDetail.isValveInstalled()).thenReturn(YesNoAnswer.YES);
        UsagePoint usagePoint = mockUsagePoint(31L, "usage point", 2L, ServiceKind.HEAT, heatDetail);

        Response response = target("/usagepoints/31").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<Integer>get("$.version")).isEqualTo(2);
        assertThat(model.<String>get("$.name")).isEqualTo("usage point");
        assertThat(model.<String>get("$.location")).isEqualTo("location");
        assertThat(model.<String>get("$.mrid")).isEqualTo("MRID");
        assertThat(model.<String>get("$.valve")).isEqualTo("YES");
        assertThat(model.<String>get("$.collar")).isEqualTo("YES");
        assertThat(model.<String>get("$.bypass")).isEqualTo("YES");
        assertThat(model.<String>get("$.readRoute")).isEqualTo("read route");
        assertThat(model.<String>get("$.serviceKind")).isEqualTo("Heat");
        assertThat(model.<String>get("$.bypassStatus")).isEqualTo("CLOSED");
        assertThat(model.<Integer>get("$.physicalCapacity.value")).isEqualTo(302);
        assertThat(model.<Integer>get("$.physicalCapacity.multiplier")).isEqualTo(0);
        assertThat(model.<String>get("$.physicalCapacity.unit")).isEqualTo("m");
        assertThat(model.<Integer>get("$.pressure.value")).isEqualTo(301);
        assertThat(model.<Integer>get("$.pressure.multiplier")).isEqualTo(0);
        assertThat(model.<String>get("$.pressure.unit")).isEqualTo("kg");
        assertThat(model.<Long>get("$.installationTime")).isEqualTo(LocalDateTime.of(2016, 3, 20, 11, 0)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli());
        assertThat(model.<String>get("$.description")).isEqualTo("usage point desc");
        assertThat(model.<String>get("$.serviceDeliveryRemark")).isEqualTo("remark");
        assertThat(model.<String>get("$.servicePriority")).isEqualTo("service priority");
        assertThat(model.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.link.href")).isEqualTo("http://localhost:9998/usagepoints/31");
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
        when(electricityDetail.getLoadLimit()).thenReturn(Quantity.create(value1, "W"));
        when(electricityDetail.getLoadLimiterType()).thenReturn("LLT");
        when(electricityDetail.getNominalServiceVoltage()).thenReturn(Quantity.create(value2, "m"));
        when(electricityDetail.getEstimatedLoad()).thenReturn(Quantity.create(value3, "g"));
        UsagePoint usagePoint = mockUsagePoint(31L, "usage point", 2L, ServiceKind.ELECTRICITY, electricityDetail);

        Response response = target("/usagepoints/31").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<Integer>get("$.version")).isEqualTo(2);
        assertThat(model.<String>get("$.name")).isEqualTo("usage point");
        assertThat(model.<String>get("$.location")).isEqualTo("location");
        assertThat(model.<String>get("$.mrid")).isEqualTo("MRID");
        assertThat(model.<String>get("$.collar")).isEqualTo("YES");
        assertThat(model.<String>get("$.limiter")).isEqualTo("YES");
        assertThat(model.<String>get("$.interruptible")).isEqualTo("YES");
        assertThat(model.<String>get("$.grounded")).isEqualTo("YES");
        assertThat(model.<String>get("$.readRoute")).isEqualTo("read route");
        assertThat(model.<String>get("$.serviceKind")).isEqualTo("Electricity");
        assertThat(model.<Integer>get("$.loadLimit.value")).isEqualTo(201);
        assertThat(model.<Integer>get("$.loadLimit.multiplier")).isEqualTo(0);
        assertThat(model.<String>get("$.loadLimit.unit")).isEqualTo("W");
        assertThat(model.<String>get("$.loadLimiterType")).isEqualTo("LLT");
        assertThat(model.<Integer>get("$.nominalServiceVoltage.value")).isEqualTo(202);
        assertThat(model.<String>get("$.nominalServiceVoltage.unit")).isEqualTo("m");
        assertThat(model.<Integer>get("$.estimatedLoad.value")).isEqualTo(203);
        assertThat(model.<String>get("$.estimatedLoad.unit")).isEqualTo("g");
        assertThat(model.<Long>get("$.installationTime")).isEqualTo(LocalDateTime.of(2016, 3, 20, 11, 0)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli());
        assertThat(model.<String>get("$.description")).isEqualTo("usage point desc");
        assertThat(model.<String>get("$.serviceDeliveryRemark")).isEqualTo("remark");
        assertThat(model.<String>get("$.servicePriority")).isEqualTo("service priority");
        assertThat(model.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.link.href")).isEqualTo("http://localhost:9998/usagepoints/31");
    }

    @Test
    public void testUpdateElectricityUsagePoint() throws Exception {
        Instant now = Instant.now(clock);
        ElectricityUsagePointInfo info = new ElectricityUsagePointInfo();
        info.id = 999L;
        info.version = 2L;
        info.aliasName = "alias";
        info.description = "desc";
        info.installationTime = now;
        info.location = "here";
        info.mrid = "mmmmm";
        info.name = "naam";
        info.outageRegion = "outage";
        info.serviceDeliveryRemark = "remark";
        info.servicePriority = "prio1";

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
        ElectricityDetailBuilder electricityDetailBuilder = FakeBuilder.initBuilderStub(electricityDetail, ElectricityDetailBuilder.class);
        when(usagePoint.newElectricityDetailBuilder(any())).thenReturn(electricityDetailBuilder);

        Response response = target("/usagepoints/11").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(usagePoint).setName("naam");
        verify(usagePoint).setAliasName("alias");
        verify(usagePoint).setDescription("desc");
        verify(usagePoint).setInstallationTime(now);
        verify(usagePoint).setMRID("mmmmm");
        verify(usagePoint).setOutageRegion("outage");
        verify(usagePoint).setServiceDeliveryRemark("remark");
        verify(usagePoint).setServicePriority("prio1");
        verify(usagePoint).update();
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
    public void testNoUpdateMetrologyWithIdenticalIds() throws Exception {
        ElectricityUsagePointInfo info = new ElectricityUsagePointInfo();
        info.id = 999L;
        info.version = 2L;
        info.metrologyConfiguration = new LinkInfo<>();
        info.metrologyConfiguration.id = 234L;

        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(234L, "metro", 1);
        UsagePoint usagePoint = mockUsagePoint(11L, "usage point", 2L, ServiceKind.ELECTRICITY);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        ElectricityDetail electricityDetail = mock(ElectricityDetail.class);
        ElectricityDetailBuilder electricityDetailBuilder = FakeBuilder.initBuilderStub(electricityDetail, ElectricityDetailBuilder.class);
        when(usagePoint.newElectricityDetailBuilder(any())).thenReturn(electricityDetailBuilder);

        Response response = target("/usagepoints/11").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(usagePoint, never()).removeMetrologyConfiguration(clock.instant());
        verify(usagePoint, never()).apply(metrologyConfiguration, clock.instant());
    }

    @Test
    public void testAddMetrologyIfOneExisted() throws Exception {
        ElectricityUsagePointInfo info = new ElectricityUsagePointInfo();
        info.id = 999L;
        info.version = 2L;
        info.metrologyConfiguration = new LinkInfo<>();
        info.metrologyConfiguration.id = 235L;

        UsagePointMetrologyConfiguration oldMetrologyConfiguration = mockMetrologyConfiguration(234L, "metro", 1);
        UsagePointMetrologyConfiguration newMetrologyConfiguration = mockMetrologyConfiguration(235L, "metro", 1);
        UsagePoint usagePoint = mockUsagePoint(11L, "usage point", 2L, ServiceKind.ELECTRICITY);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(oldMetrologyConfiguration);
        ElectricityDetail electricityDetail = mock(ElectricityDetail.class);
        ElectricityDetailBuilder electricityDetailBuilder = FakeBuilder.initBuilderStub(electricityDetail, ElectricityDetailBuilder.class);
        when(usagePoint.newElectricityDetailBuilder(any())).thenReturn(electricityDetailBuilder);

        Response response = target("/usagepoints/11").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(usagePoint).removeMetrologyConfiguration(clock.instant());
        verify(usagePoint).apply(newMetrologyConfiguration, clock.instant());
    }

    @Test
    public void testRemoveMetrologyIfNoneSpecified() throws Exception {
        ElectricityUsagePointInfo info = new ElectricityUsagePointInfo();
        info.id = 999L;
        info.version = 2L;
        info.metrologyConfiguration = new LinkInfo<>();
        info.metrologyConfiguration.id = null;

        UsagePointMetrologyConfiguration oldMetrologyConfiguration = mockMetrologyConfiguration(234L, "metro", 1);
        UsagePoint usagePoint = mockUsagePoint(11L, "usage point", 2L, ServiceKind.ELECTRICITY);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(oldMetrologyConfiguration);
        ElectricityDetail electricityDetail = mock(ElectricityDetail.class);
        ElectricityDetailBuilder electricityDetailBuilder = FakeBuilder.initBuilderStub(electricityDetail, ElectricityDetailBuilder.class);
        when(usagePoint.newElectricityDetailBuilder(any())).thenReturn(electricityDetailBuilder);

        Response response = target("/usagepoints/11").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(usagePoint).removeMetrologyConfiguration(clock.instant());
        verify(usagePoint, never()).apply(any(), any());
    }

    @Test
    public void testAddMetrologyIfNoneExisted() throws Exception {
        ElectricityUsagePointInfo info = new ElectricityUsagePointInfo();
        info.id = 999L;
        info.version = 2L;
        info.metrologyConfiguration = new LinkInfo<>();
        info.metrologyConfiguration.id = 235L;

        UsagePointMetrologyConfiguration newMetrologyConfiguration = mockMetrologyConfiguration(235L, "metro", 1);
        UsagePoint usagePoint = mockUsagePoint(11L, "usage point", 2L, ServiceKind.ELECTRICITY);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());
        ElectricityDetail electricityDetail = mock(ElectricityDetail.class);
        ElectricityDetailBuilder electricityDetailBuilder = FakeBuilder.initBuilderStub(electricityDetail, ElectricityDetailBuilder.class);
        when(usagePoint.newElectricityDetailBuilder(any())).thenReturn(electricityDetailBuilder);

        Response response = target("/usagepoints/11").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(usagePoint, never()).removeMetrologyConfiguration(clock.instant());
        verify(usagePoint).apply(newMetrologyConfiguration, clock.instant());
    }

    @Test
    public void testCreateUsagePointWithDetails() throws Exception {
        Instant now = Instant.now(clock);
        GasUsagePointInfo info = new GasUsagePointInfo();
        info.aliasName = "alias";
        info.description = "desc";
        info.installationTime = now;
        info.location = "here";
        info.mrid = "mmmmm";
        info.name = "naam";
        info.outageRegion = "outage";
        info.serviceDeliveryRemark = "remark";
        info.servicePriority = "prio1";
        info.readRoute = "route";
        info.collar = YesNoAnswer.YES;
        info.capped = YesNoAnswer.YES;
        info.clamped = YesNoAnswer.YES;
        info.bypass = YesNoAnswer.YES;
        info.bypassStatus = BypassStatus.CLOSED;
        info.grounded = YesNoAnswer.YES;
        info.interruptible = YesNoAnswer.YES;
        info.limiter = YesNoAnswer.YES;
        info.loadLimit = Quantity.create(BigDecimal.ONE, "Wh");
        info.grounded = YesNoAnswer.NO;

        UsagePoint usagePoint = mock(UsagePoint.class);
        when(usagePoint.getId()).thenReturn(6L);
        GasDetail gasDetail = mock(GasDetail.class);
        GasDetailBuilder gasDetailBuilder = FakeBuilder.initBuilderStub(gasDetail, GasDetailBuilder.class);
        when(usagePoint.newGasDetailBuilder(any())).thenReturn(gasDetailBuilder);
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        UsagePointBuilder usagePointBuilder = FakeBuilder.initBuilderStub(usagePoint, UsagePointBuilder.class);
        when(serviceCategory.newUsagePoint(any(), any())).thenReturn(usagePointBuilder);
        when(meteringService.getServiceCategory(ServiceKind.GAS)).thenReturn(Optional.of(serviceCategory));

        Response response = target("/usagepoints").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(response.getLocation()).isEqualTo(new URI("http://localhost:9998/usagepoints/6"));
        verify(usagePointBuilder).withName("naam");
        verify(usagePointBuilder).withAliasName("alias");
        verify(usagePointBuilder).withDescription("desc");
        verify(usagePointBuilder).withOutageRegion("outage");
        verify(usagePointBuilder).withServiceDeliveryRemark("remark");
        verify(usagePointBuilder).withServicePriority("prio1");
        verify(usagePointBuilder).withServiceLocationString("here");
        verify(usagePointBuilder).withReadRoute("route");
        verify(usagePoint).newGasDetailBuilder(any());
        verify(usagePointBuilder).create();
    }

    @Test
    public void testCreateUsagePointWithoutDetails() throws Exception {
        Instant now = Instant.now(clock);
        GasUsagePointInfo info = new GasUsagePointInfo();
        info.aliasName = "alias";
        info.description = "desc";
        info.installationTime = now;
        info.location = "here";
        info.mrid = "mmmmm";
        info.name = "naam";
        info.outageRegion = "outage";
        info.serviceDeliveryRemark = "remark";
        info.servicePriority = "prio1";
        info.readRoute = "route";

        UsagePoint usagePoint = mock(UsagePoint.class);
        when(usagePoint.getId()).thenReturn(6L);
        GasDetail gasDetail = mock(GasDetail.class);
        GasDetailBuilder gasDetailBuilder = FakeBuilder.initBuilderStub(gasDetail, GasDetailBuilder.class);
        when(usagePoint.newGasDetailBuilder(any())).thenReturn(gasDetailBuilder);
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        UsagePointBuilder usagePointBuilder = FakeBuilder.initBuilderStub(usagePoint, UsagePointBuilder.class);
        when(serviceCategory.newUsagePoint(any(), any())).thenReturn(usagePointBuilder);
        when(meteringService.getServiceCategory(ServiceKind.GAS)).thenReturn(Optional.of(serviceCategory));

        Response response = target("/usagepoints").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(response.getLocation()).isEqualTo(new URI("http://localhost:9998/usagepoints/6"));
        verify(usagePointBuilder).withName("naam");
        verify(usagePointBuilder).withAliasName("alias");
        verify(usagePointBuilder).withDescription("desc");
        verify(usagePointBuilder).withOutageRegion("outage");
        verify(usagePointBuilder).withServiceDeliveryRemark("remark");
        verify(usagePointBuilder).withServicePriority("prio1");
        verify(usagePointBuilder).withServiceLocationString("here");
        verify(usagePointBuilder).withReadRoute("route");
    }

    @Test
    public void testUsagePointFields() throws Exception {
        Response response = target("/usagepoints").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(33);
        assertThat(model.<List<String>>get("$")).containsOnly(
                "aliasName",
                "bypass",
                "bypassStatus",
                "capped",
                "collar",
                "clamped",
                "description",
                "estimatedLoad",
                "grounded",
                "id",
                "installationTime",
                "interruptible",
                "limiter",
                "link",
                "loadLimit",
                "loadLimiterType",
                "location",
                "mrid",
                "name",
                "nominalServiceVoltage",
                "outageRegion",
                "phaseCode",
                "physicalCapacity",
                "pressure",
                "ratedCurrent",
                "ratedPower",
                "readRoute",
                "serviceDeliveryRemark",
                "servicePriority",
                "valve",
                "version",
                "serviceKind",
                "metrologyConfiguration"
        );
    }
}
