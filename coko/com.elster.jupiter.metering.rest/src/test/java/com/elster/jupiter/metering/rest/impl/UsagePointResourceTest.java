package com.elster.jupiter.metering.rest.impl;


import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationStatus;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.users.User;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsagePointResourceTest extends MeteringApplicationJerseyTest {
    private static final String USAGE_POINT_NAME = "El nombre";
    private static final Instant NOW = ZonedDateTime.of(2015, 12, 10, 10, 43, 13, 0, ZoneId.systemDefault()).toInstant();

    @Mock
    private User principal;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private ServiceCategory serviceCategory;
    @Mock
    private UsagePointBuilder usagePointBuilder;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private Meter meter;
    @Mock
    private AmrSystem amrSystem;

    @Before
    public void setUp1() {
        when(meteringService.findUsagePointById(1L)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(meteringService.getServiceCategory(ServiceKind.ELECTRICITY)).thenReturn(Optional.of(serviceCategory));
        when(serviceCategory.newUsagePoint(eq("test"), any(Instant.class))).thenReturn(usagePointBuilder);
        when(serviceCategory.getKind()).thenReturn(ServiceKind.ELECTRICITY);
        when(usagePointBuilder.create()).thenReturn(usagePoint);
        when(clock.instant()).thenReturn(NOW);
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(usagePoint.getInstallationTime()).thenReturn(Instant.EPOCH);
        when(usagePoint.getId()).thenReturn(1L);
        when(usagePoint.getVersion()).thenReturn(1L);
        when(usagePoint.getCreateDate()).thenReturn(Instant.EPOCH);
        when(usagePoint.getModificationDate()).thenReturn(Instant.EPOCH);
        when(usagePoint.getLocation()).thenReturn(Optional.empty());
        when(usagePoint.getSpatialCoordinates()).thenReturn(Optional.empty());
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint =  mockEffectiveMetrologyConfiguration();
        List<EffectiveMetrologyConfigurationOnUsagePoint> configs = Collections.singletonList(effectiveMetrologyConfigurationOnUsagePoint);
        when(usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(configs);
        when(usagePoint.getEffectiveMetrologyConfiguration(Instant.EPOCH)).thenReturn(Optional.of(effectiveMetrologyConfigurationOnUsagePoint));
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfigurationOnUsagePoint));
        when(usagePoint.getCurrentMeterActivations()).thenReturn(Collections.singletonList(meterActivation));
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(meter.getAmrSystem()).thenReturn(amrSystem);
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        when(meterActivation.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getSpatialCoordinates()).thenReturn(Optional.empty());
    }

    private EffectiveMetrologyConfigurationOnUsagePoint mockEffectiveMetrologyConfiguration() {
        UsagePointMetrologyConfiguration config = mockMetrologyConfiguration(1L, "MC-1", "13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
        EffectiveMetrologyConfigurationOnUsagePoint mock = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(mock.getMetrologyConfiguration()).thenReturn(config);
        when(mock.getStart()).thenReturn(Instant.EPOCH);
        when(mock.getEnd()).thenReturn(Instant.EPOCH);
        when(mock.getRange()).thenReturn(Range.all());
        return mock;
    }

    private UsagePointMetrologyConfiguration mockMetrologyConfiguration(long id, String name, String readingTypeMRID) {
        UsagePointMetrologyConfiguration mock = mock(UsagePointMetrologyConfiguration.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getVersion()).thenReturn(1L);
        when(mock.getName()).thenReturn(name);
        when(mock.getDescription()).thenReturn("some description");
        ReadingType readingType = mockReadingType(readingTypeMRID);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        when(deliverable.getReadingType()).thenReturn(readingType);
        when(mock.getDeliverables()).thenReturn(Collections.singletonList(deliverable));
        when(mock.getStatus()).thenReturn(MetrologyConfigurationStatus.ACTIVE);
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(serviceCategory.getKind()).thenReturn(ServiceKind.ELECTRICITY);
        when(serviceCategory.getName()).thenReturn("Electricity");
        when(mock.getServiceCategory()).thenReturn(serviceCategory);
        return mock;
    }

    @Test
    public void testGetUsagePointInfo() {
        when(principal.hasPrivilege(any(String.class), any(String.class))).thenReturn(true);
        UsagePointInfo response = target("usagepoints/" + USAGE_POINT_NAME).request().get(UsagePointInfo.class);
        assertThat(response.id).isEqualTo(1L);
    }

    @Test
    public void testUsagePointCreating() {
        UsagePointInfo info = new UsagePointInfo();
        info.name = "test";
        info.installationTime = Instant.EPOCH.toEpochMilli();
        info.serviceCategory = ServiceKind.ELECTRICITY;
        info.extendedLocation = new EditLocationInfo();
        info.extendedLocation.locationId = null;
        info.extendedGeoCoordinates = null;
        Response response = target("usagepoints").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(201);

        verify(serviceCategory).newUsagePoint("test", Instant.EPOCH);
    }

    @Test
    public void testUsagePointUpdate() {
        when(meteringService.findUsagePointById(1L)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByIdAndVersion(1L, 1L)).thenReturn(Optional.of(usagePoint));
        UsagePointInfo info = new UsagePointInfo();
        info.id = 1L;
        info.name = "upd";
        info.installationTime = Instant.EPOCH.toEpochMilli();
        info.version = 1L;
        info.extendedLocation = new EditLocationInfo();
        info.extendedLocation.locationId = null;
        info.extendedGeoCoordinates = null;

        Response response = target("usagepoints/" + USAGE_POINT_NAME).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);
        verify(usagePoint, times(1)).setName("upd");
        verify(usagePoint, times(1)).setInstallationTime(any(Instant.class));
        verify(usagePoint, times(1)).update();
    }

    @Test
    public void testGetUsagePointMetrologyConfigurationHistory() {
        String json = target("usagepoints/" + USAGE_POINT_NAME + "/history/metrologyconfigurations").request().get(String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.metrologyConfigurationVersions[0].metrologyConfiguration.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.metrologyConfigurationVersions[0].metrologyConfiguration.name")).isEqualTo("MC-1");
    }

    @Test
    public void testUpdateMetrologyConfigurationVersions() {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = mockEffectiveMetrologyConfiguration();
        UsagePointMetrologyConfiguration config = mockMetrologyConfiguration(1L, "MC-1", "13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
        when(meteringService.findAndLockUsagePointByIdAndVersion(1L, 1L)).thenReturn(Optional.of(usagePoint));
        when(metrologyConfigurationService.findMetrologyConfiguration(1L)).thenReturn(Optional.of(config));
        List<EffectiveMetrologyConfigurationOnUsagePoint> configs = Collections.singletonList(effectiveMetrologyConfigurationOnUsagePoint);
        when(usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(configs);
        when(usagePoint.findEffectiveMetrologyConfigurationById(1L)).thenReturn(Optional.of(effectiveMetrologyConfigurationOnUsagePoint));
        when(usagePoint.getEffectiveMetrologyConfigurationByStart(Instant.EPOCH)).thenReturn(Optional.of(effectiveMetrologyConfigurationOnUsagePoint));

        MetrologyConfigurationInfo metrologyConfigurationInfo = new MetrologyConfigurationInfo();
        metrologyConfigurationInfo.id = 1L;
        metrologyConfigurationInfo.name = "MC-1";

        EffectiveMetrologyConfigurationOnUsagePointInfo effectiveMCInfo = new EffectiveMetrologyConfigurationOnUsagePointInfo();
        effectiveMCInfo.metrologyConfiguration = metrologyConfigurationInfo;
        effectiveMCInfo.start = Instant.EPOCH.toEpochMilli();
        effectiveMCInfo.end = Instant.EPOCH.toEpochMilli();

        UsagePointInfo info = new UsagePointInfo();
        info.id = 1L;
        info.name = "Name";
        info.installationTime = Instant.EPOCH.toEpochMilli();
        info.version = 1L;
        info.metrologyConfigurationVersion = effectiveMCInfo;

        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/metrologyconfigurationversion").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);
        verify(usagePoint, times(1)).apply(config, Instant.EPOCH, Instant.EPOCH);

        effectiveMCInfo.editable = true;

        response = target("usagepoints/" + USAGE_POINT_NAME + "/metrologyconfigurationversion/" + Instant.EPOCH.toEpochMilli()).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);
        verify(usagePoint, times(1)).updateWithInterval(effectiveMetrologyConfigurationOnUsagePoint, config, Instant.EPOCH, Instant.EPOCH);

        response = target("usagepoints/" + USAGE_POINT_NAME + "/metrologyconfigurationversion/1").request().build("DELETE", Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(200);
        verify(usagePoint, times(1)).removeMetrologyConfigurationVersion(effectiveMetrologyConfigurationOnUsagePoint);
    }
}
