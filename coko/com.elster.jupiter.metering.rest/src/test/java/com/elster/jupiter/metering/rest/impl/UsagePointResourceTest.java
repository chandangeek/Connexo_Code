package com.elster.jupiter.metering.rest.impl;


import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.users.User;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsagePointResourceTest extends MeteringApplicationJerseyTest {

    public static final Instant NOW = ZonedDateTime.of(2015, 12, 10, 10, 43, 13, 0, ZoneId.systemDefault()).toInstant();


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
        when(meteringService.findUsagePoint(1L)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findUsagePoint(anyString())).thenReturn(Optional.of(usagePoint));
        when(meteringService.getServiceCategory(ServiceKind.ELECTRICITY)).thenReturn(Optional.of(serviceCategory));
        when(serviceCategory.newUsagePoint(anyString(), any(Instant.class))).thenReturn(usagePointBuilder);
        when(serviceCategory.getKind()).thenReturn(ServiceKind.ELECTRICITY);
        when(usagePointBuilder.withName(anyString())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.create()).thenReturn(usagePoint);
        when(clock.instant()).thenReturn(NOW);
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(usagePoint.getMRID()).thenReturn("MRID");
        when(usagePoint.getInstallationTime()).thenReturn(Instant.EPOCH);
        when(usagePoint.getId()).thenReturn(1L);
        when(usagePoint.getVersion()).thenReturn(1L);
        when(usagePoint.getCreateDate()).thenReturn(Instant.EPOCH);
        when(usagePoint.getModificationDate()).thenReturn(Instant.EPOCH);
        when(usagePoint.getLocation()).thenReturn(Optional.empty());
        when(usagePoint.getGeoCoordinates()).thenReturn(Optional.empty());
        when(usagePoint.getMetrologyConfiguration()).thenReturn(Optional.empty());
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint =  mockEffectiveMetrologyConfiguration();
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMetrologyConfigurationOnUsagePoint));
        when(usagePoint.getCurrentMeterActivation()).thenReturn(Optional.of(meterActivation));
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(meter.getAmrSystem()).thenReturn(amrSystem);
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        when(meterActivation.getUsagePoint()).thenReturn(Optional.of(usagePoint));
    }

    private EffectiveMetrologyConfigurationOnUsagePoint mockEffectiveMetrologyConfiguration() {
        MetrologyConfiguration config = mockMetrologyConfiguration(1L, "MC-1", "13.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
        EffectiveMetrologyConfigurationOnUsagePoint mock = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
        when(mock.getMetrologyConfiguration()).thenReturn(config);
        return mock;
    }

    private MetrologyConfiguration mockMetrologyConfiguration(long id, String name, String readingTypeMRID) {
        MetrologyConfiguration mock = mock(MetrologyConfiguration.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        when(mock.getDescription()).thenReturn("some description");
        ReadingType readingType = mockReadingType(readingTypeMRID);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        when(deliverable.getReadingType()).thenReturn(readingType);
        when(mock.getDeliverables()).thenReturn(Collections.singletonList(deliverable));
        return mock;
    }


    @Test
    public void testGetUsagePointInfo() {

        when(principal.hasPrivilege(any(String.class), any(String.class))).thenReturn(true);
        UsagePointInfo response = target("usagepoints/test").request().get(UsagePointInfo.class);
        assertThat(response.id).isEqualTo(1L);
    }

    @Test
    public void testUsagePointCreating() {

        UsagePointInfo info = new UsagePointInfo();
        info.mRID = "test";
        info.installationTime = Instant.EPOCH.toEpochMilli();
        info.serviceCategory = ServiceKind.ELECTRICITY;
        Response response = target("usagepoints").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testUsagePointUpdate() {
        when(meteringService.findUsagePoint(1L)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByIdAndVersion(1L, 1L)).thenReturn(Optional.of(usagePoint));
        UsagePointInfo info = new UsagePointInfo();
        info.id = 1L;
        info.mRID = "upd";
        info.name = "upd";
        info.installationTime = Instant.EPOCH.toEpochMilli();
        info.version = 1L;

        Response response = target("usagepoints/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);
        verify(usagePoint, never()).setMRID(anyString());
        verify(usagePoint, times(1)).setName("upd");
        verify(usagePoint, times(1)).setInstallationTime(any(Instant.class));
        verify(usagePoint, times(1)).update();
    }

    @Test
    public void testGetUsagePointMetrologyConfigurationHistory() {
        when(meteringService.findUsagePoint(1L)).thenReturn(Optional.of(usagePoint));
        List<EffectiveMetrologyConfigurationOnUsagePoint> configs = Collections.singletonList(mockEffectiveMetrologyConfiguration());

        when(usagePoint.getEffectiveMetrologyConfigurations()).thenReturn(configs);

        String json = target("usagepoints/1/history/metrologyconfiguration").request().get(String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.metrologyConfigurationVersions[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.metrologyConfigurationVersions[0].name")).isEqualTo("MC-1");
    }

}
