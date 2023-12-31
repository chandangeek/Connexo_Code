/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetAttributeInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfoFactory;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.mdm.usagepoint.config.rest.EstimationRuleSetInfo;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfigurationStatus;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirementNode;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointRequirement;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationVersionStatus;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MetrologyConfigurationResourceTest extends UsagePointConfigurationRestApplicationJerseyTest {

    @Mock
    private UsagePointMetrologyConfiguration config1, config2;
    @Mock
    private MetrologyContract metrologyContract1, metrologyContract2;
    private ReadingType readingType = mockReadingType();
    @Mock
    private ValidationRuleSet vrs, vrs2, vrs3;
    @Mock
    private EstimationRuleSet ers, ers2, ers3;
    @Mock
    private ServiceCategory serviceCategory;
    @Mock
    private ReadingTypeDeliverable readingTypeDeliverable;
    private MetrologyContractInfo metrologyContractInfo;

    @Before
    public void initializeMocks() {
        config1 = mockMetrologyConfiguration(1L, "config1", ServiceKind.ELECTRICITY, MetrologyConfigurationStatus.INACTIVE, metrologyContract1, readingType);
        config2 = mockMetrologyConfiguration(2L, "config2", ServiceKind.WATER, MetrologyConfigurationStatus.ACTIVE, metrologyContract2);
        when(metrologyConfigurationService.findAllMetrologyConfigurations()).thenReturn(Arrays.asList(config1, config2));
        when(metrologyConfigurationService.findMetrologyConfiguration(1)).thenReturn(Optional.of(config1));
        when(metrologyConfigurationService.findMetrologyConfiguration(2)).thenReturn(Optional.of(config2));

        ValidationRuleSetVersion validationRuleSetVersion = mockValidationRuleSetVersion(vrs, 1, readingType);
        ValidationRuleSetVersion validationRuleSetVersion2 = mockValidationRuleSetVersion(vrs2, 2);
        ValidationRuleSetVersion validationRuleSetVersion3 = mockValidationRuleSetVersion(vrs3, 3);
        EstimationRule estimationRule = mock(EstimationRule.class);
        doReturn(Collections.singleton(mockReadingType())).when(estimationRule).getReadingTypes();
        when(ers.getName()).thenReturn("EstimationRuleSet");
        when(ers.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDM);
        when(ers.getId()).thenReturn(51L);
        when(ers2.getName()).thenReturn("EstimationRuleSet2");
        when(ers2.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDM);
        when(ers2.getId()).thenReturn(52L);
        when(ers3.getName()).thenReturn("EstimationRuleSet3");
        when(ers3.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDM);
        when(ers3.getId()).thenReturn(53L);
        metrologyContractInfo = new MetrologyContractInfo(metrologyContract1);
        metrologyContractInfo.validationRuleSets = Collections.singletonList(new ValidationRuleSetInfoFactory().from(vrs3, Collections.emptySet()));
        metrologyContractInfo.estimationRuleSets = Collections.emptyList();
        when(vrs3.getId()).thenReturn(2L);
        when(vrs3.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDM);
        doReturn(Collections.singletonList(validationRuleSetVersion3)).when(vrs3).getRuleSetVersions();
        when(vrs.getName()).thenReturn("ValidationRuleSet");
        when(vrs.getId()).thenReturn(1L);
        when(vrs.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDM);
        doReturn(Collections.singletonList(validationRuleSetVersion)).when(vrs).getRuleSetVersions();
        when(metrologyConfigurationService.findAndLockMetrologyContract(metrologyContractInfo.id, metrologyContractInfo.version)).thenReturn(Optional.of(metrologyContract1));
        when(metrologyConfigurationService.findMetrologyContract(1L)).thenReturn(Optional.of(metrologyContract1));
        when(usagePointConfigurationService.getValidationRuleSets(metrologyContract1)).thenReturn(Collections.singletonList(vrs));
        when(usagePointConfigurationService.getMatchingDeliverablesOnValidationRuleSet(metrologyContract1, vrs2)).thenReturn(Collections.singletonList(readingTypeDeliverable));
        when(vrs2.getName()).thenReturn("LinkableValidationRuleSet");
        when(vrs2.getId()).thenReturn(31L);
        when(vrs2.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDM);
        doReturn(Collections.singletonList(validationRuleSetVersion2)).when(vrs2).getRuleSetVersions();
        doReturn(Optional.of(vrs3)).when(validationService).getValidationRuleSet(anyLong());
        when(validationService.getValidationRuleSets()).thenReturn(Arrays.asList(vrs, vrs2));
        when(usagePointConfigurationService.isLinkableValidationRuleSet(metrologyContract1, vrs2, Collections.singletonList(vrs))).thenReturn(true);
        doReturn(Optional.of(ers)).when(estimationService).getEstimationRuleSet(51L);
        doReturn(Optional.of(ers2)).when(estimationService).getEstimationRuleSet(52L);
        doReturn(Optional.of(ers3)).when(estimationService).getEstimationRuleSet(53L);
        doReturn(Arrays.asList(ers, ers2, ers3)).when(estimationService).getEstimationRuleSets();
        when(usagePointConfigurationService.getEstimationRuleSets(metrologyContract1)).thenReturn(Arrays.asList(ers2, ers3));
        when(usagePointConfigurationService.isLinkableEstimationRuleSet(metrologyContract1, ers, Arrays.asList(ers2, ers3))).thenReturn(true);
    }

    private ValidationRuleSetVersion mockValidationRuleSetVersion(ValidationRuleSet validationRuleSet, long id) {
        return mockValidationRuleSetVersion(validationRuleSet, id, mockReadingType());
    }

    private ValidationRuleSetVersion mockValidationRuleSetVersion(ValidationRuleSet validationRuleSet, long id,
                                                                  ReadingType readingType) {
        ValidationRuleSetVersion validationRuleSetVersion = mock(ValidationRuleSetVersion.class);
        ValidationRule validationRule = mock(ValidationRule.class);
        when(validationRuleSetVersion.getId()).thenReturn(id);
        when(validationRuleSetVersion.getStatus()).thenReturn(ValidationVersionStatus.CURRENT);
        when(validationRuleSetVersion.getRuleSet()).thenReturn(validationRuleSet);
        doReturn(Collections.singleton(readingType)).when(validationRule).getReadingTypes();
        when(validationRule.appliesTo(readingType)).thenReturn(true);
        when(validationRule.getRuleSetVersion()).thenReturn(validationRuleSetVersion);
        doReturn(Collections.singletonList(validationRule)).when(validationRuleSetVersion).getRules();
        return validationRuleSetVersion;
    }

    private UsagePointMetrologyConfiguration mockMetrologyConfiguration(long id, String name, ServiceKind serviceKind,
                                                                        MetrologyConfigurationStatus status,
                                                                        MetrologyContract metrologyContract) {
        return mockMetrologyConfiguration(id, name, serviceKind, status, metrologyContract, mockReadingType());
    }

    private UsagePointMetrologyConfiguration mockMetrologyConfiguration(long id, String name, ServiceKind serviceKind,
                                                                        MetrologyConfigurationStatus status,
                                                                        MetrologyContract metrologyContract,
                                                                        ReadingType readingType) {
        UsagePointMetrologyConfiguration mock = mock(UsagePointMetrologyConfiguration.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(mock.getServiceCategory()).thenReturn(serviceCategory);
        when(serviceCategory.getKind()).thenReturn(serviceKind);
        when(serviceCategory.getName()).thenReturn(serviceKind.getDefaultFormat());
        when(mock.getStatus()).thenReturn(status);
        when(mock.getVersion()).thenReturn(1L);
        when(mock.getDescription()).thenReturn("some description");
        when(mock.areGapsAllowed()).thenReturn(true);

        MeterRole role = mock(MeterRole.class);
        when(role.getKey()).thenReturn(DefaultMeterRole.DEFAULT.getKey());
        when(role.getDisplayName()).thenReturn(DefaultMeterRole.DEFAULT.getDefaultFormat());
        when(mock.getMeterRoles()).thenReturn(Collections.singletonList(role));

        MetrologyPurpose purpose = mock(MetrologyPurpose.class);
        when(purpose.getId()).thenReturn(1L);
        when(purpose.getDescription()).thenReturn(DefaultMetrologyPurpose.BILLING.getDescription().getDefaultMessage());
        when(purpose.getName()).thenReturn(DefaultMetrologyPurpose.BILLING.getName().getDefaultMessage());
        ReadingTypeDeliverable deliverable = mock(ReadingTypeDeliverable.class);
        when(deliverable.getMetrologyConfiguration()).thenReturn(mock);
        when(deliverable.getName()).thenReturn("testDeliverable");
        when(deliverable.getReadingType()).thenReturn(readingType);
        Formula formula = mock(Formula.class);
        ReadingTypeRequirementNode requirementNode = mock(ReadingTypeRequirementNode.class);
        FullySpecifiedReadingTypeRequirement requirement = mock(FullySpecifiedReadingTypeRequirement.class);
        when(requirement.getMetrologyConfiguration()).thenReturn(mock);
        when(requirement.getReadingType()).thenReturn(readingType);
        when(requirementNode.getReadingTypeRequirement()).thenReturn(requirement);
        when(formula.getExpressionNode()).thenReturn(requirementNode);
        when(deliverable.getFormula()).thenReturn(formula);
        when(metrologyContract.getId()).thenReturn(1L);
        when(metrologyContract.getVersion()).thenReturn(1L);
        when(metrologyContract.getMetrologyPurpose()).thenReturn(purpose);
        when(metrologyContract.getMetrologyConfiguration()).thenReturn(mock);
        when(metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(deliverable));

        when(mock.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
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
        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(13L, "Residential", ServiceKind.GAS, MetrologyConfigurationStatus.INACTIVE, this.metrologyContract2);
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
        assertThat(jsonModel.<List<?>>get("$.meterRoles").size()).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.meterRoles[0].id")).isEqualTo(DefaultMeterRole.DEFAULT.getKey());
        assertThat(jsonModel.<String>get("$.meterRoles[0].name")).isEqualTo(DefaultMeterRole.DEFAULT.getDefaultFormat());
        assertThat(jsonModel.<List<?>>get("$.purposes")).isNotEmpty();
        assertThat(jsonModel.<List<?>>get("$.purposes").size()).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("$.purposes[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.purposes[0].name")).isEqualTo(DefaultMetrologyPurpose.BILLING.getName().getDefaultMessage());
        assertThat(jsonModel.<List<?>>get("$.metrologyContracts")).isNotEmpty();
        assertThat(jsonModel.<Integer>get("$.metrologyContracts[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.metrologyContracts[0].name")).isEqualTo(DefaultMetrologyPurpose.BILLING.getName().getDefaultMessage());
        assertThat(jsonModel.<List<?>>get("$.metrologyContracts[0].readingTypeDeliverables")).isNotEmpty();
        assertThat(jsonModel.<Number>get("$.version")).isEqualTo(1);
    }

    @Test
    public void getVersionedCustomPropertySets() {
        MetrologyContract metrologyContract = mock(MetrologyContract.class);
        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(13L, "Residential", ServiceKind.GAS, MetrologyConfigurationStatus.INACTIVE, metrologyContract);
        UsagePoint usagePoint = mock(UsagePoint.class);
        RegisteredCustomPropertySet rcps = mock(RegisteredCustomPropertySet.class);
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        CustomPropertySetAttributeInfo attributeInfo = new CustomPropertySetAttributeInfo();
        PropertyValueInfo valueInfo = new PropertyValueInfo();
        PropertySpec spec = mock(PropertySpec.class);
        PropertyInfo propertyInfo = mock(PropertyInfo.class);
        PropertyTypeInfo typeInfo = mock(PropertyTypeInfo.class);
        List<PropertySpec> specList = Collections.singletonList(spec);
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        ValueFactory valueFactory = mock(ValueFactory.class);
        CustomPropertySetInfo info = new CustomPropertySetInfo();

        propertyInfo.key="antennaPower";
        propertyInfo.name="Antenna power";
        propertyInfo.propertyValueInfo = valueInfo;
        propertyInfo.propertyTypeInfo = typeInfo;
        values.setProperty("Antenna power",Quantity.create(BigDecimal.valueOf(55),-3,"Wh"));
        valueInfo.value = Quantity.create(BigDecimal.valueOf(55),-3,"Wh");
        info.isVersioned = true;
        info.name = "Antenna";
        info.id = 36;
        info.properties = Collections.singletonList(attributeInfo);
        attributeInfo.name = "Antenna power";
        attributeInfo.key = "antennaPower";
        attributeInfo.propertyValueInfo = valueInfo;

        when(propertyInfo.getPropertyValueInfo()).thenReturn(valueInfo);
        when(clock.instant()).thenReturn(Instant.now());
        when(metrologyConfigurationService.findMetrologyConfiguration(13L)).thenReturn(Optional.of(metrologyConfiguration));
        when(meteringService.findUsagePointByName("someName")).thenReturn(Optional.of(usagePoint));
        when(metrologyConfiguration.getCustomPropertySets()).thenReturn(Collections.singletonList(rcps));
        when(rcps.getCustomPropertySet()).thenReturn(customPropertySet);
        when(this.customPropertySetService.getUniqueValuesFor(any(CustomPropertySet.class), any(UsagePoint.class), any(Instant.class))).thenReturn(values);
        when(customPropertySet.getName()).thenReturn("Antenna");
        when(customPropertySet.getId()).thenReturn("13");
        when(customPropertySet.isVersioned()).thenReturn(true);
        when(customPropertySet.getPropertySpecs()).thenReturn(specList);
        when(customPropertySet.isVersioned()).thenReturn(true);
        when(customPropertySet.getDomainClass()).thenReturn(CustomPropertySetInfoFactory.class);
        when(this.propertyValueInfoService.getPropertyInfo(any(PropertySpec.class),any(Function.class))).thenReturn(propertyInfo);
        when(spec.getValueFactory()).thenReturn(valueFactory);
        when(valueFactory.getValueType()).thenReturn(Integer.class);

        //Business method
        String json = target("metrologyconfigurations/13/usagepoint/someName").request().get(String.class);

        //Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<String>get("$.customPropertySets[0].name")).isEqualTo("Antenna");
        assertThat(jsonModel.<Boolean>get("$.customPropertySets[0].isVersioned")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.customPropertySets[0].properties[0].propertyValueInfo.value.unit")).isEqualTo("Wh");
        assertThat(jsonModel.<Integer>get("$.customPropertySets[0].properties[0].propertyValueInfo.value.multiplier")).isEqualTo(-3);
        assertThat(jsonModel.<Integer>get("$.customPropertySets[0].properties[0].propertyValueInfo.value.value")).isEqualTo(55);
        assertThat(jsonModel.<String>get("$.customPropertySets[0].properties[0].key")).isEqualTo("antennaPower");
        assertThat(jsonModel.<String>get("$.customPropertySets[0].properties[0].name")).isEqualTo("Antenna power");
    }

    @Test
    public void testUpdateMetrologyConfiguration() {
        MetrologyConfigurationInfo metrologyConfigurationInfo = new MetrologyConfigurationInfo();
        metrologyConfigurationInfo.name = "newName";
        Entity<MetrologyConfigurationInfo> json = Entity.json(metrologyConfigurationInfo);
        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(2L, metrologyConfigurationInfo.name, ServiceKind.GAS, MetrologyConfigurationStatus.INACTIVE, this.metrologyContract2);
        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(anyLong(), anyLong())).thenReturn(Optional.of(metrologyConfiguration));

        Response response = target("/metrologyconfigurations/2").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testLinkedValidationRuleSetsOfMetrologyContract() {
        String json = target("/metrologyconfigurations/1/contracts").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<?>>get("$.contracts[*]")).hasSize(1);
        assertThat(jsonModel.<List<?>>get("$.contracts[0].validationRuleSets[*]")).hasSize(1);
        JsonModel ruleSet = jsonModel.getSubModel("$.contracts[0].validationRuleSets[0]");
        assertThat(ruleSet.<Integer>get("$.id")).isEqualTo(1);
        assertThat(ruleSet.<String>get("$.name")).isEqualTo("ValidationRuleSet");
        assertThat(ruleSet.<Boolean>get("$.hasCurrent")).isTrue();
        assertThat(ruleSet.<Integer>get("$.currentVersionId")).isEqualTo(1);
        assertThat(ruleSet.<Integer>get("$.currentVersion.id")).isEqualTo(1);
        assertThat(ruleSet.<String>get("$.currentVersion.status")).isEqualTo(ValidationVersionStatus.CURRENT.name());
        assertThat(ruleSet.<Integer>get("$.currentVersion.numberOfRules")).isEqualTo(1);
    }

    @Test
    public void testLinkableValidationRuleSetsOfMetrologyContract() {
        when(usagePointConfigurationService.getValidationRuleSets(this.metrologyContract1)).thenReturn(Collections.emptyList());
        String json = target("/metrologyconfigurations/1/contracts/1").request().header("X-CONNEXO-APPLICATION-NAME", "INS").get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Integer>get("$.validationRuleSets[0].id")).isEqualTo(31);
        assertThat(jsonModel.<String>get("$.validationRuleSets[0].name")).isEqualTo("LinkableValidationRuleSet");
    }

    @Test
    public void testAddValidationRuleSetsToMetrologyContract() {
        Entity<MetrologyContractInfo> json = Entity.json(metrologyContractInfo);
        Response response = target("/metrologyconfigurations/1/contracts/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(usagePointConfigurationService).addValidationRuleSet(metrologyContract1, vrs3);
    }

    @Test
    public void testAddValidationRuleSetsConcurrencyCheck() {
        when(metrologyConfigurationService.findAndLockMetrologyContract(metrologyContractInfo.id, metrologyContractInfo.version)).thenReturn(Optional.empty());
        Entity<MetrologyContractInfo> json = Entity.json(metrologyContractInfo);
        Response response = target("/metrologyconfigurations/1/contracts/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testRemoveValidationRuleSetFromMetrologyContract() {
        Entity<MetrologyContractInfo> json = Entity.json(metrologyContractInfo);
        Response response = target("/metrologyconfigurations/1/contracts/1").queryParam("action", "remove").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(usagePointConfigurationService).removeValidationRuleSet(metrologyContract1, vrs3);
    }

    @Test
    public void testRemoveValidationRuleSetConcurrencyCheck() {
        when(metrologyConfigurationService.findAndLockMetrologyContract(metrologyContractInfo.id, metrologyContractInfo.version)).thenReturn(Optional.empty());
        Entity<MetrologyContractInfo> json = Entity.json(metrologyContractInfo);
        Response response = target("/metrologyconfigurations/1/contracts/1").queryParam("action", "remove").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testLinkedEstimationRuleSetsOfMetrologyContract() {
        String json = target("/metrologyconfigurations/1/contracts").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("$.contracts[0].estimationRuleSets[0].id")).isEqualTo(52);
        assertThat(jsonModel.<String>get("$.contracts[0].estimationRuleSets[0].name")).isEqualTo("EstimationRuleSet2");
    }

    @Test
    public void testLinkableEstimationRuleSetsOfMetrologyContract() {
        String json = target("/metrologyconfigurations/1/contracts/1").request().header("X-CONNEXO-APPLICATION-NAME", "INS").get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Integer>get("$.estimationRuleSets[0].id")).isEqualTo(51);
        assertThat(jsonModel.<String>get("$.estimationRuleSets[0].name")).isEqualTo("EstimationRuleSet");
    }

    @Test
    public void testAddEstimationRuleSetsToMetrologyContract() {
        metrologyContractInfo.validationRuleSets = Collections.emptyList();
        metrologyContractInfo.estimationRuleSets = Collections.singletonList(new EstimationRuleSetInfo(ers));
        Entity<MetrologyContractInfo> json = Entity.json(metrologyContractInfo);
        Response response = target("/metrologyconfigurations/1/contracts/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(usagePointConfigurationService).addEstimationRuleSet(metrologyContract1, ers);
    }

    @Test
    public void testAddEstimationRuleSetsConcurrencyCheck() {
        metrologyContractInfo.validationRuleSets = Collections.emptyList();
        metrologyContractInfo.estimationRuleSets = Collections.singletonList(new EstimationRuleSetInfo(ers));
        when(metrologyConfigurationService.findAndLockMetrologyContract(metrologyContractInfo.id, metrologyContractInfo.version)).thenReturn(Optional.empty());
        Entity<MetrologyContractInfo> json = Entity.json(metrologyContractInfo);
        Response response = target("/metrologyconfigurations/1/contracts/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testRemoveEstimationRuleSetFromMetrologyContract() {
        metrologyContractInfo.validationRuleSets = Collections.emptyList();
        metrologyContractInfo.estimationRuleSets = Collections.singletonList(new EstimationRuleSetInfo(ers));
        Entity<MetrologyContractInfo> json = Entity.json(metrologyContractInfo);
        Response response = target("/metrologyconfigurations/1/contracts/1").queryParam("action", "remove").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(usagePointConfigurationService).removeEstimationRuleSet(metrologyContract1, ers);
    }

    @Test
    public void testRemoveEstimationRuleSetConcurrencyCheck() {
        metrologyContractInfo.validationRuleSets = Collections.emptyList();
        metrologyContractInfo.estimationRuleSets = Collections.singletonList(new EstimationRuleSetInfo(ers));
        when(metrologyConfigurationService.findAndLockMetrologyContract(metrologyContractInfo.id, metrologyContractInfo.version)).thenReturn(Optional.empty());
        Entity<MetrologyContractInfo> json = Entity.json(metrologyContractInfo);
        Response response = target("/metrologyconfigurations/1/contracts/1").queryParam("action", "remove").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testReorderEstimationRuleSetFromMetrologyContract() {
        MetrologyContractInfos infos = new MetrologyContractInfos();
        metrologyContractInfo.validationRuleSets = Collections.emptyList();
        metrologyContractInfo.estimationRuleSets = Arrays.asList(new EstimationRuleSetInfo(ers),new EstimationRuleSetInfo(ers2),new EstimationRuleSetInfo(ers3));
        infos.total = 1;
        infos.contracts = Collections.singletonList(metrologyContractInfo);
        Entity<MetrologyContractInfos> json = Entity.json(infos);
        Response response = target("/metrologyconfigurations/1/contracts").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(usagePointConfigurationService).reorderEstimationRuleSets(metrologyContract1, Arrays.asList(ers,ers2,ers3));
    }

    @Test
    public void testGetListOfAssignedCPS() throws Exception {
        long mConfigId = 123L;
        long rcpsId = 456L;
        String cpsId = "TestCPSId";

        CustomPropertySet cps = mock(CustomPropertySet.class);
        when(cps.getId()).thenReturn(cpsId);
        when(cps.getDomainClass()).thenReturn(UsagePointMetrologyConfiguration.class);
        RegisteredCustomPropertySet rcps = mock(RegisteredCustomPropertySet.class);
        when(rcps.getId()).thenReturn(rcpsId);
        when(rcps.getCustomPropertySet()).thenReturn(cps);
        when(rcps.isViewableByCurrentUser()).thenReturn(true);
        UsagePointMetrologyConfiguration mConfig = mock(UsagePointMetrologyConfiguration.class);
        when(mConfig.getId()).thenReturn(mConfigId);
        when(mConfig.isActive()).thenReturn(false);
        when(mConfig.getVersion()).thenReturn(1L);
        when(mConfig.getCustomPropertySets()).thenReturn(Collections.singletonList(rcps));
        when(metrologyConfigurationService.findMetrologyConfiguration(mConfigId)).thenReturn(Optional.of(mConfig));

        Response response = target("/metrologyconfigurations/" + mConfigId + "/custompropertysets").queryParam("start",0).queryParam("limit", 10).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
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
        when(cps1.getDomainClass()).thenReturn(UsagePointMetrologyConfiguration.class);

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

        CustomPropertySet cps3 = mock(CustomPropertySet.class);
        when(cps3.getId()).thenReturn("ServiceCatCPS");
        when(cps3.getDomainClass()).thenReturn(UsagePoint.class);

        RegisteredCustomPropertySet rcps3 = mock(RegisteredCustomPropertySet.class);
        when(rcps3.getId()).thenReturn(777L);
        when(rcps3.getCustomPropertySet()).thenReturn(cps3);
        when(rcps3.isViewableByCurrentUser()).thenReturn(true);

        UsagePointMetrologyConfiguration mConfig = mock(UsagePointMetrologyConfiguration.class);
        when(mConfig.getId()).thenReturn(mConfigId);
        when(mConfig.isActive()).thenReturn(false);
        when(mConfig.getVersion()).thenReturn(1L);
        when(mConfig.getCustomPropertySets()).thenReturn(Collections.singletonList(rcps1));

        when(meteringService.getServiceCategory(any(ServiceKind.class))).thenReturn(Optional.of(serviceCategory));
        when(serviceCategory.getCustomPropertySets()).thenReturn(Stream.of(rcps3).collect(Collectors.toList()));
        when(rcps3.getCustomPropertySet()).thenReturn(cps3);

        when(metrologyConfigurationService.findMetrologyConfiguration(mConfigId)).thenReturn(Optional.of(mConfig));
        when(customPropertySetService.findActiveCustomPropertySets(UsagePoint.class)).thenReturn(Arrays.asList(rcps1, rcps2));

        Response response = target("/metrologyconfigurations/" + mConfigId + "/custompropertysets").queryParam("linked", false).queryParam("start", 0).queryParam("limit", 10).request().get();
        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.customPropertySets")).hasSize(1);
        assertThat(model.<Number>get("$.customPropertySets[0].id")).isEqualTo(789);
        assertThat(model.<Number>get("$.customPropertySets[0].id")).isNotEqualTo(777);
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
        when(cps.getDomainClass()).thenReturn(UsagePointMetrologyConfiguration.class);

        RegisteredCustomPropertySet rcps = mock(RegisteredCustomPropertySet.class);
        when(rcps.getId()).thenReturn(456L);
        when(rcps.getCustomPropertySet()).thenReturn(cps);
        when(rcps.isViewableByCurrentUser()).thenReturn(true);

        UsagePointMetrologyConfiguration mConfig = mockMetrologyConfiguration(1L, "name", ServiceKind.ELECTRICITY, MetrologyConfigurationStatus.INACTIVE, this.metrologyContract2);
        when(mConfig.isActive()).thenReturn(false);
        when(mConfig.getVersion()).thenReturn(1L);
        when(mConfig.getCustomPropertySets()).thenReturn(Collections.singletonList(rcps));

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
        when(cps.getDomainClass()).thenReturn(UsagePointMetrologyConfiguration.class);

        RegisteredCustomPropertySet rcps = mock(RegisteredCustomPropertySet.class);
        when(rcps.getId()).thenReturn(456L);
        when(rcps.getCustomPropertySet()).thenReturn(cps);
        when(rcps.isViewableByCurrentUser()).thenReturn(true);

        UsagePointMetrologyConfiguration mConfig = mockMetrologyConfiguration(1L, "name", ServiceKind.ELECTRICITY, MetrologyConfigurationStatus.INACTIVE, this.metrologyContract2);
        when(mConfig.isActive()).thenReturn(false);
        when(mConfig.getVersion()).thenReturn(1L);
        when(mConfig.getCustomPropertySets()).thenReturn(Collections.singletonList(rcps));

        when(customPropertySetService.findActiveCustomPropertySet("TestSPCId")).thenReturn(Optional.of(rcps));
        when(metrologyConfigurationService.findMetrologyConfiguration(mConfigId)).thenReturn(Optional.of(mConfig));
        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(mConfigId, parent.version)).thenReturn(Optional.of(mConfig));

        target("/metrologyconfigurations/" + mConfigId + "/custompropertysets/TestSPCId").request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        verify(mConfig).removeCustomPropertySet(rcps);
        verify(mConfig, atLeastOnce()).getCustomPropertySets();
    }

    @Test
    public void testRemoveCustomPropertySetConcurrentCheck() throws Exception {
        long mConfigId = 123L;

        CustomPropertySet cps = mock(CustomPropertySet.class);
        when(cps.getId()).thenReturn("TestSPCId");
        when(cps.getDomainClass()).thenReturn(UsagePointMetrologyConfiguration.class);

        RegisteredCustomPropertySet rcps = mock(RegisteredCustomPropertySet.class);
        when(rcps.getId()).thenReturn(456L);
        when(rcps.getCustomPropertySet()).thenReturn(cps);
        when(rcps.isViewableByCurrentUser()).thenReturn(true);

        UsagePointMetrologyConfiguration mConfig = mock(UsagePointMetrologyConfiguration.class);
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

    @Test
    public void testMetrologyConfigurationHasUsagePointRequirement() throws Exception {
        SearchablePropertyValue.ValueBean stickyCriteriaBean = new SearchablePropertyValue.ValueBean();
        stickyCriteriaBean.propertyName = "stickyCriteria";
        stickyCriteriaBean.operator = SearchablePropertyOperator.EQUAL;
        stickyCriteriaBean.values = Arrays.asList("Value 1", "Value 2");

        PropertySpecPossibleValues possibleValues = mock(PropertySpecPossibleValues.class);
        when(possibleValues.getAllValues()).thenReturn(Arrays.asList("Value 1", "Value 2", "Value 3", "Value 4"));

        ValueFactory stickyValueFactory = mock(ValueFactory.class);
        when(stickyValueFactory.getValueType()).thenReturn(String.class);

        PropertySpec stickyPropertySpec = mock(PropertySpec.class);
        when(stickyPropertySpec.getPossibleValues()).thenReturn(possibleValues);
        when(stickyPropertySpec.getValueFactory()).thenReturn(stickyValueFactory);

        SearchablePropertyGroup searchablePropertyGroup = mock(SearchablePropertyGroup.class);
        when(searchablePropertyGroup.getId()).thenReturn("group.id");
        when(searchablePropertyGroup.getDisplayName()).thenReturn("Group name");

        SearchableProperty stickySearchableProperty = mock(SearchableProperty.class);
        when(stickySearchableProperty.getName()).thenReturn(stickyCriteriaBean.propertyName);
        when(stickySearchableProperty.getVisibility()).thenReturn(SearchableProperty.Visibility.STICKY);
        when(stickySearchableProperty.getSelectionMode()).thenReturn(SearchableProperty.SelectionMode.MULTI);
        when(stickySearchableProperty.getConstraints()).thenReturn(Collections.emptyList());
        when(stickySearchableProperty.getGroup()).thenReturn(Optional.empty());
        when(stickySearchableProperty.getSpecification()).thenReturn(stickyPropertySpec);
        when(stickySearchableProperty.toDisplay(any())).thenAnswer(invocation -> invocation.getArguments()[0].toString());

        UsagePointRequirement stickyRequirement = mock(UsagePointRequirement.class);
        when(stickyRequirement.toValueBean()).thenReturn(stickyCriteriaBean);
        when(stickyRequirement.getSearchableProperty()).thenReturn(stickySearchableProperty);

        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(serviceCategory.getKind()).thenReturn(ServiceKind.ELECTRICITY);

        UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
        when(metrologyConfiguration.getId()).thenReturn(1L);
        when(metrologyConfiguration.getUsagePointRequirements()).thenReturn(Collections.singletonList(stickyRequirement));
        when(metrologyConfiguration.getStatus()).thenReturn(MetrologyConfigurationStatus.INACTIVE);
        when(metrologyConfiguration.getServiceCategory()).thenReturn(serviceCategory);

        when(metrologyConfigurationService.findMetrologyConfiguration(1L)).thenReturn(Optional.of(metrologyConfiguration));

        Response response = target("/metrologyconfigurations/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<List<?>>get("$.usagePointRequirements")).hasSize(1);
        assertThat(model.<String>get("$.usagePointRequirements[0].name")).isEqualTo("stickyCriteria");
        assertThat(model.<String>get("$.usagePointRequirements[0].type")).isEqualTo("String");
        assertThat(model.<String>get("$.usagePointRequirements[0].factoryName")).isNotEmpty();
        assertThat(model.<String>get("$.usagePointRequirements[0].selectionMode")).isEqualTo("multiple");
        assertThat(model.<String>get("$.usagePointRequirements[0].visibility")).isEqualTo("sticky");
        assertThat(model.<List<String>>get("$.usagePointRequirements[0].values[*].id")).containsOnly("Value 1", "Value 2", "Value 3", "Value 4");
        assertThat(model.<String>get("$.usagePointRequirements[0].value[0].operator")).isEqualTo("==");
        assertThat(model.<List<String>>get("$.usagePointRequirements[0].value[0].criteria")).containsOnly("Value 1", "Value 2");
    }

    @Test
    public void testActivateMetrologyConfiguration() throws Exception {
        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(13L, "Residential", ServiceKind.ELECTRICITY, MetrologyConfigurationStatus.INACTIVE, this.metrologyContract2);
        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        info.id = 13L;
        info.version = 1;

        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(info.id, info.version)).thenReturn(Optional.of(metrologyConfiguration));

        Response response = target("metrologyconfigurations/13/activate").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testdeprecateMetrologyConfiguration() throws Exception {
        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(13L, "Residential", ServiceKind.ELECTRICITY, MetrologyConfigurationStatus.INACTIVE, this.metrologyContract2);
        MetrologyConfigurationInfo info = new MetrologyConfigurationInfo();
        info.id = 13L;
        info.version = 1;

        when(metrologyConfigurationService.findAndLockMetrologyConfiguration(info.id, info.version)).thenReturn(Optional.of(metrologyConfiguration));

        Response response = target("metrologyconfigurations/13/deprecate").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}
