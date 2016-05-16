package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.MetrologyConfigurationStatus;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;

import com.jayway.jsonpath.JsonModel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetrologyConfigurationResourceTest extends MeteringApplicationJerseyTest {

    private UsagePointMetrologyConfiguration mockMetrologyConfiguration(long id, String name, ServiceKind serviceKind, MetrologyConfigurationStatus status, String readingTypeMRID) {
        UsagePointMetrologyConfiguration mock = mock(UsagePointMetrologyConfiguration.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        when(mock.getDescription()).thenReturn("some description");
        when(mock.getStatus()).thenReturn(status);
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(mock.getServiceCategory()).thenReturn(serviceCategory);
        when(serviceCategory.getKind()).thenReturn(serviceKind);
        when(serviceCategory.getName()).thenReturn(serviceKind.getDefaultFormat());
        when(mock.getVersion()).thenReturn(1L);
        ReadingType readingType = mockReadingType(readingTypeMRID);
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        when(deliverable.getReadingType()).thenReturn(readingType);
        when(mock.getDeliverables()).thenReturn(Collections.singletonList(deliverable));
        return mock;
    }

    @Test
    public void testGetMetrologyConfigurations() {
        UsagePointMetrologyConfiguration config1 = mockMetrologyConfiguration(1L, "config1", ServiceKind.ELECTRICITY, MetrologyConfigurationStatus.INACTIVE, "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        UsagePointMetrologyConfiguration config2 = mockMetrologyConfiguration(2L, "config2", ServiceKind.WATER, MetrologyConfigurationStatus.ACTIVE, "0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0");
        when(metrologyConfigurationService.findAllMetrologyConfigurations()).thenReturn(Arrays.asList(config1, config2));

        //Business method
        String json = target("/metrologyconfigurations").request().get(String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<Number>get("$.metrologyConfigurations[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.metrologyConfigurations[0].name")).isEqualTo("config1");
        assertThat(jsonModel.<String>get("$.metrologyConfigurations[0].status.id")).isEqualTo("inactive");
        assertThat(jsonModel.<String>get("$.metrologyConfigurations[0].serviceCategory.id")).isEqualTo("ELECTRICITY");
        assertThat(jsonModel.<List>get("$.metrologyConfigurations[0].readingTypes")).hasSize(1);
        assertThat(jsonModel.<String>get("$.metrologyConfigurations[0].readingTypes[0].mRID")).isEqualTo("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        assertThat(jsonModel.<Number>get("$.metrologyConfigurations[1].id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.metrologyConfigurations[1].name")).isEqualTo("config2");
        assertThat(jsonModel.<String>get("$.metrologyConfigurations[1].status.id")).isEqualTo("active");
        assertThat(jsonModel.<String>get("$.metrologyConfigurations[1].serviceCategory.id")).isEqualTo("WATER");
        assertThat(jsonModel.<List>get("$.metrologyConfigurations[1].readingTypes")).hasSize(1);
        assertThat(jsonModel.<String>get("$.metrologyConfigurations[1].readingTypes[0].mRID")).isEqualTo("0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.0.72.0");
        assertThat(jsonModel.<Number>get("$.metrologyConfigurations[1].version")).isEqualTo(1);
    }

    @Test
    public void testGetNoMetrologyConfigurations() {
        when(metrologyConfigurationService.findAllMetrologyConfigurations()).thenReturn(Collections.emptyList());

        //Business method
        String json = target("/metrologyconfigurations").request().get(String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List>get("$.metrologyConfigurations")).hasSize(0);
    }
}