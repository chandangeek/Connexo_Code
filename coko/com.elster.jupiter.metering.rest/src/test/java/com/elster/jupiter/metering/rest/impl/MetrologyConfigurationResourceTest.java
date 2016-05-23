package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationStatus;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfigurationBuilder;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
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

    String name;
    String description;
    ServiceKind serviceKind;
    MetrologyConfigurationStatus status;
    String readingTypeMrid;
    ReadingType readingType;

    @Before
    public void setUpStubs() {
        name = "config1";
        description = "description";
        serviceKind = ServiceKind.ELECTRICITY;
        status = MetrologyConfigurationStatus.INACTIVE;
        readingTypeMrid = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0";
        readingType = mockReadingType(readingTypeMrid);
        when(readingType.getName()).thenReturn("Reading type");

        UsagePointMetrologyConfigurationBuilder builder = mock(UsagePointMetrologyConfigurationBuilder.class);
        when(builder.withDescription(description)).thenReturn(builder);
        UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        when(metrologyConfiguration.getStatus()).thenReturn(status);
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(serviceCategory.getKind()).thenReturn(serviceKind);
        when(serviceCategory.getName()).thenReturn(serviceKind.getDisplayName());
        when(metrologyConfiguration.getServiceCategory()).thenReturn(serviceCategory);
        when(builder.create()).thenReturn(metrologyConfiguration);
        when(meteringService.getServiceCategory(serviceKind)).thenReturn(Optional.of(serviceCategory));
        when(metrologyConfigurationService.newUsagePointMetrologyConfiguration(name, serviceCategory)).thenReturn(builder);
        MetrologyPurpose metrologyPurpose = mock(MetrologyPurpose.class);
        when(metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION)).thenReturn(Optional.of(metrologyPurpose));
        MeterRole meterRole = mock(MeterRole.class);
        when(metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())).thenReturn(Optional.of(meterRole));
        when(meteringService.findReadingTypes(Collections.singletonList(readingTypeMrid))).thenReturn(Collections.singletonList(readingType));
        UsagePointMetrologyConfiguration.MetrologyConfigurationReadingTypeRequirementBuilder requirementBuilder = mock(UsagePointMetrologyConfiguration.MetrologyConfigurationReadingTypeRequirementBuilder.class);
        when(metrologyConfiguration.newReadingTypeRequirement(readingType.getFullAliasName())).thenReturn(requirementBuilder);
        when(requirementBuilder.withMeterRole(meterRole)).thenReturn(requirementBuilder);
        FullySpecifiedReadingTypeRequirement fullySpecifiedReadingTypeRequirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(requirementBuilder.withReadingType(readingType)).thenReturn(fullySpecifiedReadingTypeRequirement);
        ReadingTypeDeliverableBuilder deliverableBuilder = mock(ReadingTypeDeliverableBuilder.class);
        when(metrologyConfiguration.newReadingTypeDeliverable(readingType.getFullAliasName(), readingType, Formula.Mode.AUTO)).thenReturn(deliverableBuilder);
        ReadingTypeDeliverable readingTypeDeliverable = mock(ReadingTypeDeliverable.class);
        when(deliverableBuilder.build(deliverableBuilder.requirement(fullySpecifiedReadingTypeRequirement))).thenReturn(readingTypeDeliverable);
        MetrologyContract metrologyContract = mock(MetrologyContract.class);
        when(metrologyConfiguration.addMetrologyContract(metrologyPurpose)).thenReturn(metrologyContract);
        when(metrologyContract.addDeliverable(readingTypeDeliverable)).thenReturn(metrologyContract);
    }

    @Test
    public void createMetrologyConfigurationTest() {
        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        info.name = name;
        info.description = description;
        info.status = new IdWithNameInfo(status, status.getTranslationKey().getDefaultFormat());
        info.serviceCategory = new IdWithNameInfo(serviceKind, serviceKind.getDefaultFormat());
        info.readingTypes = Collections.singletonList(new ReadingTypeInfo(readingType));
        Entity<MetrologyConfigurationInfo> json = Entity.json(info);

        Response response = target("/metrologyconfigurations").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void tryCreateMetrologyConfigurationWithoutNameTest() {
        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        info.description = description;
        info.status = new IdWithNameInfo(status, status.getTranslationKey().getDefaultFormat());
        info.serviceCategory = new IdWithNameInfo(serviceKind, serviceKind.getDefaultFormat());
        info.readingTypes = Collections.singletonList(new ReadingTypeInfo(readingType));
        Entity<MetrologyConfigurationInfo> json = Entity.json(info);

        Response response = target("/metrologyconfigurations").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void tryCreateMetrologyConfigurationWithoutServiceCategoryTest() {
        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        info.name = name;
        info.description = description;
        info.status = new IdWithNameInfo(status, status.getTranslationKey().getDefaultFormat());
        info.readingTypes = Collections.singletonList(new ReadingTypeInfo(readingType));
        Entity<MetrologyConfigurationInfo> json = Entity.json(info);

        Response response = target("/metrologyconfigurations").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void tryCreateMetrologyConfigurationWithoutReadingTypesTest() {
        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        info.name = name;
        info.description = description;
        info.status = new IdWithNameInfo(status, status.getTranslationKey().getDefaultFormat());
        info.serviceCategory = new IdWithNameInfo(serviceKind, serviceKind.getDefaultFormat());
        Entity<MetrologyConfigurationInfo> json = Entity.json(info);

        Response response = target("/metrologyconfigurations").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testMetrologyConfigurationRemoving() {
        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        info.id = 1;
        info.name = name;
        info.description = description;
        info.status = new IdWithNameInfo(status, status.getTranslationKey().getDefaultFormat());
        info.serviceCategory = new IdWithNameInfo(serviceKind, serviceKind.getDefaultFormat());
        info.readingTypes = Collections.singletonList(new ReadingTypeInfo(readingType));

        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(info.id, info.version)).thenReturn(Optional.of(mock(UsagePointMetrologyConfiguration.class)));

        Response response = target("metrologyconfigurations/1").request().method("DELETE", Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);
    }
}