package com.elster.insight.usagepoint.config.rest.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.rest.MetrologyConfigurationInfo;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfos;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        List<MetrologyConfiguration> configs = new ArrayList<>();
        configs.add(config1);
        configs.add(config2);
        when(usagePointConfigurationService.findAllMetrologyConfigurations()).thenReturn(configs);
        when(usagePointConfigurationService.findMetrologyConfiguration(1)).thenReturn(Optional.of(config1));
        when(usagePointConfigurationService.findMetrologyConfiguration(2)).thenReturn(Optional.of(config2));


        when(config1.getName()).thenReturn("config1");
        when(config2.getName()).thenReturn("config2");

        when(config1.getId()).thenReturn(1L);
        when(config2.getId()).thenReturn(2L);

        List<ValidationRuleSet> ruleSets = new ArrayList<>();
        ruleSets.add(vrs);
        when(vrs.getName()).thenReturn("ValidationRuleSet");
        when(vrs.getId()).thenReturn(1L);
        when(config1.getValidationRuleSets()).thenReturn(ruleSets);

        List<ValidationRuleSet> assignableRuleSets = new ArrayList<>();
        assignableRuleSets.add(vrs2);
        when(vrs2.getName()).thenReturn("AssignableValidationRuleSet");
        when(vrs2.getId()).thenReturn(31L);
        when(validationService.getValidationRuleSets()).thenReturn(assignableRuleSets);
    }

    @Test
    public void testAllMetrologyConfigurationsInfo() {
        String json = target("metrologyconfigurations").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<Integer>get("$.metrologyconfigurations[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.metrologyconfigurations[0].name")).isEqualTo("config1");
        assertThat(jsonModel.<Integer>get("$.metrologyconfigurations[1].id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.metrologyconfigurations[1].name")).isEqualTo("config2");
    }

    @Test
    public void testMetrologyConfigurationsInfo() {
        String json = target("metrologyconfigurations/1").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("config1");

        json = target("metrologyconfigurations/2").request().get(String.class);
        jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("config2");
    }

    @Test
    public void testCreateMetrologyConfiguration() {
        MetrologyConfigurationInfo metrologyConfigurationInfo = new MetrologyConfigurationInfo();
        metrologyConfigurationInfo.name = "newName";
        Entity<MetrologyConfigurationInfo> json = Entity.json(metrologyConfigurationInfo);
        MetrologyConfiguration metrologyConfiguration = mock(MetrologyConfiguration.class);
        when(metrologyConfiguration.getId()).thenReturn(3L);
        when(metrologyConfiguration.getVersion()).thenReturn(1L);
        when(metrologyConfiguration.getName()).thenReturn("newName");
        when(usagePointConfigurationService.newMetrologyConfiguration(metrologyConfigurationInfo.name)).thenReturn(metrologyConfiguration);

        Response response = target("/metrologyconfigurations/").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testUpdateMetrologyConfiguration() {
        MetrologyConfigurationInfo metrologyConfigurationInfo = new MetrologyConfigurationInfo();
        metrologyConfigurationInfo.name = "newName";
        Entity<MetrologyConfigurationInfo> json = Entity.json(metrologyConfigurationInfo);
        MetrologyConfiguration metrologyConfiguration = mock(MetrologyConfiguration.class);
        when(metrologyConfiguration.getId()).thenReturn(2L);
        when(metrologyConfiguration.getVersion()).thenReturn(1L);
        when(metrologyConfiguration.getName()).thenReturn("newName");
        when(usagePointConfigurationService.findAndLockMetrologyConfiguration(anyLong(), anyLong())).thenReturn(Optional.of(metrologyConfiguration));

        Response response = target("/metrologyconfigurations/2").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
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
    public void testCanActivateMetrologyConfiguration() throws Exception {
        long mConfigId = 123L;

        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        info.id = mConfigId;
        info.version = 1;

        MetrologyConfiguration mConfig = mock(MetrologyConfiguration.class);
        when(mConfig.getId()).thenReturn(mConfigId);
        when(mConfig.isActive()).thenReturn(true);
        when(usagePointConfigurationService.findMetrologyConfiguration(mConfigId)).thenReturn(Optional.of(mConfig));
        when(usagePointConfigurationService.findAndLockMetrologyConfiguration(mConfigId, info.version)).thenReturn(Optional.of(mConfig));

        Response response = target("/metrologyconfigurations/" + mConfigId + "/activate").request().put(Entity.json(info));
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.active")).isTrue();
        verify(mConfig).activate();
    }

    @Test
    public void testCanDectivateMetrologyConfiguration() throws Exception {
        long mConfigId = 123L;

        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        info.id = mConfigId;
        info.version = 1;

        MetrologyConfiguration mConfig = mock(MetrologyConfiguration.class);
        when(mConfig.getId()).thenReturn(mConfigId);
        when(mConfig.isActive()).thenReturn(false);
        when(usagePointConfigurationService.findMetrologyConfiguration(mConfigId)).thenReturn(Optional.of(mConfig));
        when(usagePointConfigurationService.findAndLockMetrologyConfiguration(mConfigId, info.version)).thenReturn(Optional.of(mConfig));

        Response response = target("/metrologyconfigurations/" + mConfigId + "/deactivate").request().put(Entity.json(info));
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.active")).isFalse();
        verify(mConfig).deactivate();
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
        when(usagePointConfigurationService.findMetrologyConfiguration(mConfigId)).thenReturn(Optional.of(mConfig));

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
        when(cps2.getDomainClass()).thenReturn(MetrologyConfiguration.class);

        RegisteredCustomPropertySet rcps2 = mock(RegisteredCustomPropertySet.class);
        when(rcps2.getId()).thenReturn(789L);
        when(rcps2.getCustomPropertySet()).thenReturn(cps2);
        when(rcps2.isViewableByCurrentUser()).thenReturn(true);

        MetrologyConfiguration mConfig = mock(MetrologyConfiguration.class);
        when(mConfig.getId()).thenReturn(mConfigId);
        when(mConfig.isActive()).thenReturn(false);
        when(mConfig.getVersion()).thenReturn(1L);
        when(mConfig.getCustomPropertySets()).thenReturn(Arrays.asList(rcps1));

        when(usagePointConfigurationService.findMetrologyConfiguration(mConfigId)).thenReturn(Optional.of(mConfig));
        when(customPropertySetService.findActiveCustomPropertySets(MetrologyConfiguration.class)).thenReturn(Arrays.asList(rcps1, rcps2));

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

        MetrologyConfiguration mConfig = mock(MetrologyConfiguration.class);
        when(mConfig.getId()).thenReturn(mConfigId);
        when(mConfig.isActive()).thenReturn(false);
        when(mConfig.getVersion()).thenReturn(1L);
        when(mConfig.getCustomPropertySets()).thenReturn(Arrays.asList(rcps));

        when(customPropertySetService.findActiveCustomPropertySet("TestSPCId")).thenReturn(Optional.of(rcps));
        when(usagePointConfigurationService.findAndLockMetrologyConfiguration(mConfigId, info.version)).thenReturn(Optional.of(mConfig));

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

        when(usagePointConfigurationService.findMetrologyConfiguration(mConfigId)).thenReturn(Optional.empty());
        when(usagePointConfigurationService.findAndLockMetrologyConfiguration(mConfigId, info.version)).thenReturn(Optional.empty());

        Response response = target("/metrologyconfigurations/" + mConfigId + "/custompropertysets").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testRemoveCustomPropertySetFromMetrologyConfiguration() throws Exception {
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

        MetrologyConfiguration mConfig = mock(MetrologyConfiguration.class);
        when(mConfig.getId()).thenReturn(mConfigId);
        when(mConfig.isActive()).thenReturn(false);
        when(mConfig.getVersion()).thenReturn(1L);
        when(mConfig.getCustomPropertySets()).thenReturn(Collections.emptyList());

        when(customPropertySetService.findActiveCustomPropertySet("TestSPCId")).thenReturn(Optional.of(rcps));
        when(usagePointConfigurationService.findAndLockMetrologyConfiguration(mConfigId, info.version)).thenReturn(Optional.of(mConfig));

        Response response = target("/metrologyconfigurations/" + mConfigId + "/custompropertysets/TestSPCId").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(model.<List>get("$.customPropertySets")).isEmpty();
        verify(mConfig).removeCustomPropertySet(rcps);
        verify(mConfig).getCustomPropertySets();
    }

    @Test
    public void testRemoveCustomPropertySetConcurrentCheck() throws Exception {
        long mConfigId = 123L;

        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        info.id = mConfigId;
        info.version = 1;

        when(usagePointConfigurationService.findMetrologyConfiguration(mConfigId)).thenReturn(Optional.empty());
        when(usagePointConfigurationService.findAndLockMetrologyConfiguration(mConfigId, info.version)).thenReturn(Optional.empty());

        Response response = target("/metrologyconfigurations/" + mConfigId + "/custompropertysets/TestSPCId").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }
}