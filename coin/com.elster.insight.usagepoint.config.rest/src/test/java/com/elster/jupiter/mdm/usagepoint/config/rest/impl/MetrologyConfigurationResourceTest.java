package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationStatus;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfos;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MetrologyConfigurationResourceTest extends UsagePointConfigurationRestApplicationJerseyTest {

    @Mock
    private MetrologyConfiguration config1, config2;
    @Mock
    private ValidationRuleSet vrs, vrs2;

    @Before
    public void setUpStubs() {
        config1 = mockMetrologyConfiguration(1L, "config1", ServiceKind.ELECTRICITY, MetrologyConfigurationStatus.INACTIVE);
        config2 = mockMetrologyConfiguration(2L, "config2", ServiceKind.WATER, MetrologyConfigurationStatus.ACTIVE);

        when(metrologyConfigurationService.findAllMetrologyConfigurations()).thenReturn(Arrays.asList(config1, config2));
        when(metrologyConfigurationService.findMetrologyConfiguration(1)).thenReturn(Optional.of(config1));
        when(metrologyConfigurationService.findMetrologyConfiguration(2)).thenReturn(Optional.of(config2));

        List<ValidationRuleSet> ruleSets = new ArrayList<>();
        ruleSets.add(vrs);
        when(vrs.getName()).thenReturn("ValidationRuleSet");
        when(vrs.getId()).thenReturn(1L);
        when(usagePointConfigurationService.getValidationRuleSets(config1)).thenReturn(ruleSets);

        List<ValidationRuleSet> assignableRuleSets = new ArrayList<>();
        assignableRuleSets.add(vrs2);
        when(vrs2.getName()).thenReturn("AssignableValidationRuleSet");
        when(vrs2.getId()).thenReturn(31L);
        when(validationService.getValidationRuleSets()).thenReturn(assignableRuleSets);
    }

    private MetrologyConfiguration mockMetrologyConfiguration(long id, String name, ServiceKind serviceKind, MetrologyConfigurationStatus status) {
        MetrologyConfiguration mock = mock(MetrologyConfiguration.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(mock.getServiceCategory()).thenReturn(serviceCategory);
        when(serviceCategory.getKind()).thenReturn(serviceKind);
        when(serviceCategory.getName()).thenReturn(serviceKind.getDefaultFormat());
        when(mock.getStatus()).thenReturn(status);
        when(mock.getVersion()).thenReturn(1L);
        when(mock.getDescription()).thenReturn("some description");
        return mock;
    }

    @Test
    public void testGetMetrologyConfigurations() {
        //Business method
        String json = target("/metrologyconfigurations").request().get(String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<Number>get("$.metrologyconfigurations[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.metrologyconfigurations[0].name")).isEqualTo("config1");
        assertThat(jsonModel.<Number>get("$.metrologyconfigurations[1].id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.metrologyconfigurations[1].name")).isEqualTo("config2");
    }

    @Test
    public void testGetMetrologyConfiguration() {
        MetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(13L, "Residential", ServiceKind.GAS, MetrologyConfigurationStatus.INACTIVE);
        when(metrologyConfigurationService.findMetrologyConfiguration(13L)).thenReturn(Optional.of(metrologyConfiguration));

        //Business method
        String json = target("metrologyconfigurations/13").request().get(String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(13);
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("Residential");
        assertThat(jsonModel.<String>get("$.description")).isEqualTo("some description");
        assertThat(jsonModel.<String>get("$.status.id")).isEqualTo("inactive");
        assertThat(jsonModel.<String>get("$.status.name")).isEqualTo("Inactive");
        assertThat(jsonModel.<String>get("$.serviceCategory.id")).isEqualTo(ServiceKind.GAS.name());
        assertThat(jsonModel.<String>get("$.serviceCategory.name")).isEqualTo(ServiceKind.GAS.getDefaultFormat());
        assertThat(jsonModel.<List<?>>get("$.meterRoles")).isEmpty();
        assertThat(jsonModel.<List<?>>get("$.purposes")).isEmpty();
        assertThat(jsonModel.<Number>get("$.version")).isEqualTo(1);
    }

    @Test
    public void testUpdateMetrologyConfiguration() {
        MetrologyConfigurationInfo metrologyConfigurationInfo = new MetrologyConfigurationInfo();
        metrologyConfigurationInfo.name = "newName";
        Entity<MetrologyConfigurationInfo> json = Entity.json(metrologyConfigurationInfo);
        MetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(2L, metrologyConfigurationInfo.name, ServiceKind.GAS, MetrologyConfigurationStatus.INACTIVE);
        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(anyLong(), anyLong())).thenReturn(Optional.of(metrologyConfiguration));

        Response response = target("/metrologyconfigurations/2").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testAssignedValidationRuleSets() {
        String json = target("metrologyconfigurations/1/assignedvalidationrulesets").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("$.assignedvalidationrulesets[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.assignedvalidationrulesets[0].name")).isEqualTo("ValidationRuleSet");
    }

    @Test
    public void testAssignableValidationRuleSets() {
        String json = target("metrologyconfigurations/1/assignablevalidationrulesets").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("$.assignablevalidationrulesets[0].id")).isEqualTo(31);
        assertThat(jsonModel.<String>get("$.assignablevalidationrulesets[0].name")).isEqualTo("AssignableValidationRuleSet");
    }

    @Test
    public void testAssignValidationRuleSets() {
        ValidationRuleSetInfos validationRuleSetInfos = new ValidationRuleSetInfos();
        ValidationRuleSetInfo info = new ValidationRuleSetInfo();
        info.id = 1;
        validationRuleSetInfos.ruleSets.add(info);
        doReturn(Optional.of(vrs)).when(validationService).getValidationRuleSet(info.id);

        Entity<ValidationRuleSetInfos> json = Entity.json(validationRuleSetInfos);

        Response response = target("/metrologyconfigurations/1/assignedvalidationrulesets").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testGetListOfAssignedCPS() throws Exception {
        long mConfigId = 123L;
        long rcpsId = 456L;
        String cpsId = "TestCPSId";

        CustomPropertySet cps = mock(CustomPropertySet.class);
        when(cps.getId()).thenReturn(cpsId);
        when(cps.getDomainClass()).thenReturn(MetrologyConfiguration.class);
        RegisteredCustomPropertySet rcps = mock(RegisteredCustomPropertySet.class);
        when(rcps.getId()).thenReturn(rcpsId);
        when(rcps.getCustomPropertySet()).thenReturn(cps);
        when(rcps.isViewableByCurrentUser()).thenReturn(true);
        MetrologyConfiguration mConfig = mock(MetrologyConfiguration.class);
        when(mConfig.getId()).thenReturn(mConfigId);
        when(mConfig.isActive()).thenReturn(false);
        when(mConfig.getVersion()).thenReturn(1L);
        when(mConfig.getCustomPropertySets()).thenReturn(Arrays.asList(rcps));
        when(metrologyConfigurationService.findMetrologyConfiguration(mConfigId)).thenReturn(Optional.of(mConfig));

        Response response = target("/metrologyconfigurations/" + mConfigId + "/custompropertysets").request().get();
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.customPropertySets")).hasSize(1);
        assertThat(model.<String>get("$.customPropertySets[0].customPropertySetId")).isEqualTo(cpsId);
        verify(mConfig).getCustomPropertySets();
        verify(rcps, atLeastOnce()).isViewableByCurrentUser();
    }

    @Test
    public void testGetListOfUnassignedCPS() throws Exception {
        long mConfigId = 123L;

        CustomPropertySet cps1 = mock(CustomPropertySet.class);
        when(cps1.getId()).thenReturn("TestCPSId");
        when(cps1.getDomainClass()).thenReturn(MetrologyConfiguration.class);

        RegisteredCustomPropertySet rcps1 = mock(RegisteredCustomPropertySet.class);
        when(rcps1.getId()).thenReturn(456L);
        when(rcps1.getCustomPropertySet()).thenReturn(cps1);
        when(rcps1.isViewableByCurrentUser()).thenReturn(true);

        CustomPropertySet cps2 = mock(CustomPropertySet.class);
        when(cps2.getId()).thenReturn("AnotherCPS");
        when(cps2.getDomainClass()).thenReturn(UsagePoint.class);

        RegisteredCustomPropertySet rcps2 = mock(RegisteredCustomPropertySet.class);
        when(rcps2.getId()).thenReturn(789L);
        when(rcps2.getCustomPropertySet()).thenReturn(cps2);
        when(rcps2.isViewableByCurrentUser()).thenReturn(true);

        MetrologyConfiguration mConfig = mock(MetrologyConfiguration.class);
        when(mConfig.getId()).thenReturn(mConfigId);
        when(mConfig.isActive()).thenReturn(false);
        when(mConfig.getVersion()).thenReturn(1L);
        when(mConfig.getCustomPropertySets()).thenReturn(Arrays.asList(rcps1));

        when(metrologyConfigurationService.findMetrologyConfiguration(mConfigId)).thenReturn(Optional.of(mConfig));
        when(customPropertySetService.findActiveCustomPropertySets(UsagePoint.class)).thenReturn(Arrays.asList(rcps1, rcps2));

        Response response = target("/metrologyconfigurations/" + mConfigId + "/custompropertysets").queryParam("linked", false).request().get();
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.customPropertySets")).hasSize(1);
        assertThat(model.<Number>get("$.customPropertySets[0].id")).isEqualTo(789);
        assertThat(model.<String>get("$.customPropertySets[0].customPropertySetId")).isEqualTo("AnotherCPS");
        verify(mConfig).getCustomPropertySets();
        verify(rcps2, atLeastOnce()).isViewableByCurrentUser();
    }

    @Test
    public void testAddCustomPropertySetToMetrologyConfiguration() throws Exception {
        long mConfigId = 123L;

        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        info.id = mConfigId;
        info.version = 1;
        info.customPropertySets = Collections.singletonList(new CustomPropertySetInfo());
        info.customPropertySets.get(0).customPropertySetId = "TestSPCId";

        CustomPropertySet cps = mock(CustomPropertySet.class);
        when(cps.getId()).thenReturn("TestSPCId");
        when(cps.getDomainClass()).thenReturn(MetrologyConfiguration.class);

        RegisteredCustomPropertySet rcps = mock(RegisteredCustomPropertySet.class);
        when(rcps.getId()).thenReturn(456L);
        when(rcps.getCustomPropertySet()).thenReturn(cps);
        when(rcps.isViewableByCurrentUser()).thenReturn(true);

        MetrologyConfiguration mConfig = mockMetrologyConfiguration(1L, "name", ServiceKind.ELECTRICITY, MetrologyConfigurationStatus.INACTIVE);
        when(mConfig.isActive()).thenReturn(false);
        when(mConfig.getVersion()).thenReturn(1L);
        when(mConfig.getCustomPropertySets()).thenReturn(Arrays.asList(rcps));

        when(customPropertySetService.findActiveCustomPropertySet("TestSPCId")).thenReturn(Optional.of(rcps));
        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(mConfigId, info.version)).thenReturn(Optional.of(mConfig));

        Response response = target("/metrologyconfigurations/" + mConfigId + "/custompropertysets").request().put(Entity.json(info));
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(model.<List>get("$.customPropertySets")).hasSize(1);
        assertThat(model.<String>get("$.customPropertySets[0].customPropertySetId")).isEqualTo("TestSPCId");
        verify(mConfig).addCustomPropertySet(rcps);
        verify(mConfig).getCustomPropertySets();
        verify(rcps, atLeastOnce()).isViewableByCurrentUser();
    }

    @Test
    public void testAddCustomPropertySetConcurrentCheck() throws Exception {
        long mConfigId = 123L;

        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        info.id = mConfigId;
        info.version = 1;
        info.customPropertySets = Collections.singletonList(new CustomPropertySetInfo());
        info.customPropertySets.get(0).customPropertySetId = "TestSPCId";

        when(metrologyConfigurationService.findMetrologyConfiguration(mConfigId)).thenReturn(Optional.empty());
        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(mConfigId, info.version)).thenReturn(Optional.empty());

        Response response = target("/metrologyconfigurations/" + mConfigId + "/custompropertysets").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testRemoveCustomPropertySetFromMetrologyConfiguration() throws Exception {
        long mConfigId = 123L;

        MetrologyConfigurationInfo parent = new MetrologyConfigurationInfo();
        parent.id = mConfigId;
        parent.version = 1;

        CustomPropertySetInfo<MetrologyConfigurationInfo> info = new CustomPropertySetInfo<>();
        info.parent = parent;
        info.customPropertySetId = "TestSPCId";

        CustomPropertySet cps = mock(CustomPropertySet.class);
        when(cps.getId()).thenReturn("TestSPCId");
        when(cps.getDomainClass()).thenReturn(MetrologyConfiguration.class);

        RegisteredCustomPropertySet rcps = mock(RegisteredCustomPropertySet.class);
        when(rcps.getId()).thenReturn(456L);
        when(rcps.getCustomPropertySet()).thenReturn(cps);
        when(rcps.isViewableByCurrentUser()).thenReturn(true);

        MetrologyConfiguration mConfig = mockMetrologyConfiguration(1L, "name", ServiceKind.ELECTRICITY, MetrologyConfigurationStatus.INACTIVE);
        when(mConfig.isActive()).thenReturn(false);
        when(mConfig.getVersion()).thenReturn(1L);
        when(mConfig.getCustomPropertySets()).thenReturn(Collections.singletonList(rcps));

        when(customPropertySetService.findActiveCustomPropertySet("TestSPCId")).thenReturn(Optional.of(rcps));
        when(metrologyConfigurationService.findMetrologyConfiguration(mConfigId)).thenReturn(Optional.of(mConfig));
        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(mConfigId, parent.version)).thenReturn(Optional.of(mConfig));

        Response response = target("/metrologyconfigurations/" + mConfigId + "/custompropertysets/TestSPCId").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        verify(mConfig).removeCustomPropertySet(rcps);
        verify(mConfig, atLeastOnce()).getCustomPropertySets();
    }

    @Test
    public void testRemoveCustomPropertySetConcurrentCheck() throws Exception {
        long mConfigId = 123L;

        CustomPropertySet cps = mock(CustomPropertySet.class);
        when(cps.getId()).thenReturn("TestSPCId");
        when(cps.getDomainClass()).thenReturn(MetrologyConfiguration.class);

        RegisteredCustomPropertySet rcps = mock(RegisteredCustomPropertySet.class);
        when(rcps.getId()).thenReturn(456L);
        when(rcps.getCustomPropertySet()).thenReturn(cps);
        when(rcps.isViewableByCurrentUser()).thenReturn(true);

        MetrologyConfiguration mConfig = mock(MetrologyConfiguration.class);
        when(mConfig.getId()).thenReturn(mConfigId);
        when(mConfig.isActive()).thenReturn(false);
        when(mConfig.getVersion()).thenReturn(1L);
        when(mConfig.getCustomPropertySets()).thenReturn(Collections.singletonList(rcps));
        MetrologyConfigurationInfo parent = new MetrologyConfigurationInfo();
        parent.id = mConfigId;
        parent.version = 1;

        CustomPropertySetInfo<MetrologyConfigurationInfo> info = new CustomPropertySetInfo<>();
        info.parent = parent;
        info.customPropertySetId = "TestSPCId";
        when(metrologyConfigurationService.findMetrologyConfiguration(mConfigId)).thenReturn(Optional.of(mConfig));
        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(mConfigId, parent.version)).thenReturn(Optional.empty());
        when(customPropertySetService.findActiveCustomPropertySet("TestSPCId")).thenReturn(Optional.of(rcps));

        Response response = target("/metrologyconfigurations/" + mConfigId + "/custompropertysets/TestSPCId").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }
}