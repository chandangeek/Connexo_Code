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
import com.elster.jupiter.metering.config.MetrologyConfigurationUpdater;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfigurationBuilder;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MetrologyConfigurationResourceTest extends MeteringApplicationJerseyTest {

    private static final ServiceKind SERVICE_KIND_ELECTRICITY = ServiceKind.ELECTRICITY;
    private static final MetrologyConfigurationStatus STATUS_INACTIVE = MetrologyConfigurationStatus.INACTIVE;
    private static final String READING_TYPE_MRID = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0";

    @Before
    public void setup() {
        readingTypeInfoFactory = new ReadingTypeInfoFactory(thesaurus);
    }

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

    private UsagePointMetrologyConfiguration mockMetrologyConfiguration(MetrologyConfigurationInfo info) {
        ReadingType readingType = mockReadingType(READING_TYPE_MRID);

        UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(serviceCategory.getKind()).thenReturn(SERVICE_KIND_ELECTRICITY);
        when(serviceCategory.getName()).thenReturn(SERVICE_KIND_ELECTRICITY.getDisplayName());
        when(metrologyConfiguration.getStatus()).thenReturn(STATUS_INACTIVE);
        when(metrologyConfiguration.getServiceCategory()).thenReturn(serviceCategory);
        UsagePointMetrologyConfigurationBuilder builder = mock(UsagePointMetrologyConfigurationBuilder.class);
        when(builder.withDescription(info.description)).thenReturn(builder);
        when(builder.create()).thenReturn(metrologyConfiguration);
        when(meteringService.getServiceCategory(serviceCategory.getKind())).thenReturn(Optional.of(serviceCategory));
        when(metrologyConfigurationService.newUsagePointMetrologyConfiguration(info.name, serviceCategory)).thenReturn(builder);
        MetrologyPurpose metrologyPurpose = mock(MetrologyPurpose.class);
        when(metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.INFORMATION)).thenReturn(Optional.of(metrologyPurpose));
        MeterRole meterRole = mock(MeterRole.class);
        when(metrologyConfigurationService.findMeterRole(DefaultMeterRole.DEFAULT.getKey())).thenReturn(Optional.of(meterRole));
        when(meteringService.findReadingTypes(Collections.singletonList(READING_TYPE_MRID))).thenReturn(Collections.singletonList(readingType));
        UsagePointMetrologyConfiguration.MetrologyConfigurationReadingTypeRequirementBuilder requirementBuilder = mock(UsagePointMetrologyConfiguration.MetrologyConfigurationReadingTypeRequirementBuilder.class);
        when(metrologyConfiguration.newReadingTypeRequirement(readingType.getFullAliasName(), meterRole)).thenReturn(requirementBuilder);
        FullySpecifiedReadingTypeRequirement fullySpecifiedReadingTypeRequirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(requirementBuilder.withReadingType(readingType)).thenReturn(fullySpecifiedReadingTypeRequirement);
        ReadingTypeDeliverableBuilder deliverableBuilder = mock(ReadingTypeDeliverableBuilder.class);
        when(metrologyConfiguration.newReadingTypeDeliverable(readingType.getFullAliasName(), readingType, Formula.Mode.EXPERT)).thenReturn(deliverableBuilder);
        ReadingTypeDeliverable readingTypeDeliverable = mock(ReadingTypeDeliverable.class);
        when(deliverableBuilder.build(deliverableBuilder.requirement(fullySpecifiedReadingTypeRequirement))).thenReturn(readingTypeDeliverable);
        MetrologyContract metrologyContract = mock(MetrologyContract.class);
        when(metrologyConfiguration.addMandatoryMetrologyContract(metrologyPurpose)).thenReturn(metrologyContract);
        when(metrologyContract.addDeliverable(readingTypeDeliverable)).thenReturn(metrologyContract);
        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(info.id, info.version)).thenReturn(Optional.of(metrologyConfiguration));
        ResourceHelper resourceHelper = mock(ResourceHelper.class);
        when(resourceHelper.findAndLockMetrologyConfiguration(info)).thenReturn(null);

        return metrologyConfiguration;
    }

    private MetrologyConfigurationInfo mockMetrologyConfigurationInfo(long id, String name, String description, long version) {
        ReadingType readingType = mockReadingType(READING_TYPE_MRID);

        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        info.id = id;
        info.name = name;
        info.description = description;
        info.status = new IdWithNameInfo(STATUS_INACTIVE, STATUS_INACTIVE.getTranslationKey().getDefaultFormat());
        info.serviceCategory = new IdWithNameInfo(SERVICE_KIND_ELECTRICITY, SERVICE_KIND_ELECTRICITY.getDefaultFormat());
        info.readingTypes = Collections.singletonList(readingTypeInfoFactory.from(readingType));
        info.version = version;

        return info;
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

    @Test
    public void createMetrologyConfigurationTest() {
        MetrologyConfigurationInfo info = mockMetrologyConfigurationInfo(0L, "config1", "some description", 0L);
        Entity<MetrologyConfigurationInfo> json = Entity.json(info);
        mockMetrologyConfiguration(info);

        //Business method
        Response response = target("/metrologyconfigurations").request().post(json);

        //Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void tryCreateMetrologyConfigurationWithoutNameTest() {
        MetrologyConfigurationInfo info = mockMetrologyConfigurationInfo(0L, null, "some description", 0L);
        Entity<MetrologyConfigurationInfo> json = Entity.json(info);
        mockMetrologyConfiguration(info);

        //Business method
        Response response = target("/metrologyconfigurations").request().post(json);

        //Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void tryCreateMetrologyConfigurationWithoutServiceCategoryTest() {
        MetrologyConfigurationInfo info = mockMetrologyConfigurationInfo(0L, "config1", "some description", 0L);
        info.serviceCategory = null;
        Entity<MetrologyConfigurationInfo> json = Entity.json(info);
        mockMetrologyConfiguration(info);

        //Business method
        Response response = target("/metrologyconfigurations").request().post(json);

        //Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void tryCreateMetrologyConfigurationWithoutReadingTypesTest() {
        MetrologyConfigurationInfo info = mockMetrologyConfigurationInfo(0L, "config1", "some description", 0L);
        info.readingTypes = null;
        Entity<MetrologyConfigurationInfo> json = Entity.json(info);
        mockMetrologyConfiguration(info);

        //Business method
        Response response = target("/metrologyconfigurations").request().post(json);

        //Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testMetrologyConfigurationRemoving() {
        MetrologyConfigurationInfo info = mockMetrologyConfigurationInfo(1L, "config1", "some description", 1L);
        Entity<MetrologyConfigurationInfo> json = Entity.json(info);
        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(info);

        //Business method
        Response response = target("metrologyconfigurations/" + info.id).request().method("DELETE", json);

        //Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(metrologyConfiguration).makeObsolete();
    }

    @Test
    public void testMetrologyConfigurationRemovingConcurrentModification() {
        MetrologyConfigurationInfo info = mockMetrologyConfigurationInfo(1L, "config1", "some description", 1L);
        Entity<MetrologyConfigurationInfo> json = Entity.json(info);
        mockMetrologyConfiguration(mockMetrologyConfigurationInfo(1L, "config1", "some description", 2L));
        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(1L, 1L)).thenReturn(Optional.empty());
        when(metrologyConfigurationService.findMetrologyConfiguration(1L)).thenReturn(Optional.empty());

        //Business method
        Response response = target("metrologyconfigurations/" + info.id).request().method("DELETE", json);

        //Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testGetMetrologyConfiguration() {
        MetrologyConfigurationInfo info = mockMetrologyConfigurationInfo(1L, "config1", "some description", 1L);
        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(1L, "config1", ServiceKind.ELECTRICITY, MetrologyConfigurationStatus.INACTIVE, "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        when(metrologyConfigurationService.findMetrologyConfiguration(1L)).thenReturn(Optional.of(metrologyConfiguration));

        //Business method
        String json = target("metrologyconfigurations/" + info.id).request().get(String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("config1");
        assertThat(jsonModel.<Number>get("$.description")).isEqualTo("some description");
        assertThat(jsonModel.<String>get("$.status.id")).isEqualTo("inactive");
        assertThat(jsonModel.<String>get("$.serviceCategory.id")).isEqualTo("ELECTRICITY");
        assertThat(jsonModel.<List>get("$.readingTypes")).hasSize(1);
        assertThat(jsonModel.<String>get("$.readingTypes[0].mRID")).isEqualTo("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        assertThat(jsonModel.<Number>get("$.version")).isEqualTo(1);
    }

    @Test
    public void testGetMetrologyConfigurationNotFound() {
        when(metrologyConfigurationService.findMetrologyConfiguration(1L)).thenReturn(Optional.empty());

        //Business method
        Response response = target("metrologyconfigurations/1").request().get();

        //Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testUpdateMetrologyConfiguration() throws Exception {
        MetrologyConfigurationInfo info = mockMetrologyConfigurationInfo(1L, "config1", "some description", 1L);
        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(mockMetrologyConfigurationInfo(1L, "config1", "some description", 2L));
        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(1L, 1L)).thenReturn(Optional.of(metrologyConfiguration));
        when(metrologyConfigurationService.findMetrologyConfiguration(1L)).thenReturn(Optional.of(metrologyConfiguration));
        MetrologyConfigurationUpdater metrologyConfigurationUpdater = mock(MetrologyConfigurationUpdater.class);
        when(metrologyConfiguration.startUpdate()).thenReturn(metrologyConfigurationUpdater);
        when(metrologyConfigurationUpdater.setName(info.name)).thenReturn(metrologyConfigurationUpdater);
        when(metrologyConfigurationUpdater.setDescription(info.description)).thenReturn(metrologyConfigurationUpdater);

        //Business method
        Response response = target("metrologyconfigurations/" + info.id).request().put(Entity.json(info));

        //Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testUpdateMetrologyConfigurationConcurrentModification() throws Exception {
        MetrologyConfigurationInfo info = mockMetrologyConfigurationInfo(1L, "config1", "some description", 1L);
        mockMetrologyConfiguration(mockMetrologyConfigurationInfo(1L, "config1", "some description", 2L));
        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(1L, 1L)).thenReturn(Optional.empty());
        when(metrologyConfigurationService.findMetrologyConfiguration(1L)).thenReturn(Optional.empty());

        //Business method
        Response response = target("metrologyconfigurations/" + info.id).request().put(Entity.json(info));

        //Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testActivateMetrologyConfiguration() throws Exception {
        MetrologyConfigurationInfo info = mockMetrologyConfigurationInfo(1L, "config1", "some description", 1L);
        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(mockMetrologyConfigurationInfo(1L, "config1", "some description", 1L));
        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(1L, 1L)).thenReturn(Optional.of(metrologyConfiguration));
        when(metrologyConfigurationService.findMetrologyConfiguration(1L)).thenReturn(Optional.of(metrologyConfiguration));
        MetrologyConfigurationUpdater metrologyConfigurationUpdater = mock(MetrologyConfigurationUpdater.class);
        when(metrologyConfiguration.startUpdate()).thenReturn(metrologyConfigurationUpdater);
        when(metrologyConfigurationUpdater.setName(info.name)).thenReturn(metrologyConfigurationUpdater);
        when(metrologyConfigurationUpdater.setDescription(info.description)).thenReturn(metrologyConfigurationUpdater);

        //Business method
        MetrologyConfigurationStatus status = MetrologyConfigurationStatus.ACTIVE;
        info.status = new IdWithNameInfo(status.getId(), thesaurus.getFormat(status.getTranslationKey()).format());
        Response response = target("metrologyconfigurations/" + info.id).request().put(Entity.json(info));

        //Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testActivateMetrologyConfigurationConcurrentModification() throws Exception {
        MetrologyConfigurationInfo info = mockMetrologyConfigurationInfo(1L, "config1", "some description", 1L);
        mockMetrologyConfiguration(mockMetrologyConfigurationInfo(1L, "config1", "some description", 2L));
        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(1L, 1L)).thenReturn(Optional.empty());
        when(metrologyConfigurationService.findMetrologyConfiguration(1L)).thenReturn(Optional.empty());

        //Business method
        MetrologyConfigurationStatus status = MetrologyConfigurationStatus.ACTIVE;
        info.status = new IdWithNameInfo(status.getId(), thesaurus.getFormat(status.getTranslationKey()).format());
        Response response = target("metrologyconfigurations/" + info.id).request().put(Entity.json(info));

        //Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }
}