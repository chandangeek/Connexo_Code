/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;


import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;

import com.google.common.collect.Sets;
import com.jayway.jsonpath.JsonModel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceCategoryResourceTest extends MeteringApplicationJerseyTest {

    @Test
    public void testGetServiceCategories() {
        ServiceCategory category = mock(ServiceCategory.class);
        when(category.getKind()).thenReturn(ServiceKind.ELECTRICITY);
        when(category.getName()).thenReturn(ServiceKind.ELECTRICITY.getDefaultFormat());
        when(category.isActive()).thenReturn(true);
        List<MeterRole> meterRoles = Arrays.asList(mockMeterRole(DefaultMeterRole.MAIN), mockMeterRole(DefaultMeterRole.CHECK));
        when(category.getMeterRoles()).thenReturn(meterRoles);
        when(meteringService.getServiceCategory(any(ServiceKind.class))).thenReturn(Optional.empty());
        when(meteringService.getServiceCategory(ServiceKind.ELECTRICITY)).thenReturn(Optional.of(category));

        String response = target("/servicecategory").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.categories")).hasSize(1);
        assertThat(model.<String>get("$.categories[0].name")).isEqualTo(ServiceKind.ELECTRICITY.name());
        assertThat(model.<String>get("$.categories[0].displayName")).isEqualTo(ServiceKind.ELECTRICITY.getDefaultFormat());
        assertThat(model.<List<String>>get("$.categories[0].meterRoles[*].id")).containsExactly(DefaultMeterRole.CHECK.getKey(), DefaultMeterRole.MAIN.getKey());
        assertThat(model.<List<String>>get("$.categories[0].meterRoles[*].name")).containsExactly(DefaultMeterRole.CHECK.getDefaultFormat(), DefaultMeterRole.MAIN.getDefaultFormat());
    }

    @Test
    public void testGetServiceCategoriesCustomPropertySets() {
        RegisteredCustomPropertySet registeredCustomPropertySet = mockRegisteredCustomPropertySet();
        ServiceCategory category = mock(ServiceCategory.class);
        when(category.getKind()).thenReturn(ServiceKind.ELECTRICITY);
        when(category.getName()).thenReturn(ServiceKind.ELECTRICITY.getDefaultFormat());
        when(category.getName()).thenReturn(ServiceKind.ELECTRICITY.getDefaultFormat());
        when(category.getCustomPropertySets()).thenReturn(Collections.singletonList(registeredCustomPropertySet));
        when(meteringService.getServiceCategory(any(ServiceKind.class))).thenReturn(Optional.empty());
        when(meteringService.getServiceCategory(ServiceKind.ELECTRICITY)).thenReturn(Optional.of(category));

        String response = target("/servicecategory/ELECTRICITY/custompropertysets").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.serviceCategoryCustomPropertySets")).hasSize(1);
        assertThat(model.<String>get("$.serviceCategoryCustomPropertySets[0].name")).isEqualTo(registeredCustomPropertySet.getCustomPropertySet().getName());
        assertThat(model.<Boolean>get("$.serviceCategoryCustomPropertySets[0].isRequired")).isEqualTo(registeredCustomPropertySet.getCustomPropertySet().isRequired());
        assertThat(model.<Boolean>get("$.serviceCategoryCustomPropertySets[0].isVersioned")).isEqualTo(registeredCustomPropertySet.getCustomPropertySet().isVersioned());
        assertThat(model.<List>get("$.serviceCategoryCustomPropertySets[0].defaultViewPrivileges")).hasSize(1);
        assertThat(model.<String>get("$.serviceCategoryCustomPropertySets[0].defaultViewPrivileges[0]")).isEqualTo(registeredCustomPropertySet.getCustomPropertySet().defaultViewPrivileges().toArray()[0].toString());
        assertThat(model.<List>get("$.serviceCategoryCustomPropertySets[0].defaultEditPrivileges")).hasSize(1);
        assertThat(model.<String>get("$.serviceCategoryCustomPropertySets[0].defaultEditPrivileges[0]")).isEqualTo(registeredCustomPropertySet.getCustomPropertySet().defaultEditPrivileges().toArray()[0].toString());
        assertThat(model.<List>get("$.serviceCategoryCustomPropertySets[0].viewPrivileges")).hasSize(1);
        assertThat(model.<String>get("$.serviceCategoryCustomPropertySets[0].viewPrivileges[0]")).isEqualTo(registeredCustomPropertySet.getViewPrivileges().toArray()[0].toString());
        assertThat(model.<List>get("$.serviceCategoryCustomPropertySets[0].editPrivileges")).hasSize(1);
        assertThat(model.<String>get("$.serviceCategoryCustomPropertySets[0].editPrivileges[0]")).isEqualTo(registeredCustomPropertySet.getEditPrivileges().toArray()[0].toString());
        assertThat(model.<List>get("$.serviceCategoryCustomPropertySets[0].properties")).hasSize(1);
    }

    @SuppressWarnings("unchecked")
    PropertySpec mockPropertySpec() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        ValueFactory valueFactory = mock(BigDecimalFactory.class);
        when(propertySpec.getName()).thenReturn("customAttribute");
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        when(propertySpec.getValueFactory().getValueType()).thenReturn(BigDecimalFactory.class);
        when(propertySpec.isRequired()).thenReturn(true);
        when(propertySpec.getDescription()).thenReturn("kw");
        return propertySpec;
    }

    @SuppressWarnings("unchecked")
    CustomPropertySet mockCustomPropertySet() {
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getName()).thenReturn("domainExtensionName");
        when(customPropertySet.isRequired()).thenReturn(true);
        when(customPropertySet.isVersioned()).thenReturn(false);
        when(customPropertySet.defaultViewPrivileges()).thenReturn(Sets.newHashSet(ViewPrivilege.LEVEL_3));
        when(customPropertySet.defaultEditPrivileges()).thenReturn(Sets.newHashSet(EditPrivilege.LEVEL_4));
        when(customPropertySet.getDomainClass()).thenReturn(BigDecimalFactory.class);
        return customPropertySet;
    }

    @SuppressWarnings("unchecked")
    protected RegisteredCustomPropertySet mockRegisteredCustomPropertySet() {
        PropertySpec propertySpec = mockPropertySpec();
        CustomPropertySet customPropertySet = mockCustomPropertySet();
        RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);
        when(registeredCustomPropertySet.getId()).thenReturn(100500L);
        when(registeredCustomPropertySet.getViewPrivileges()).thenReturn(Sets.newHashSet(ViewPrivilege.LEVEL_1));
        when(registeredCustomPropertySet.getEditPrivileges()).thenReturn(Sets.newHashSet(EditPrivilege.LEVEL_2));
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs()).thenReturn(Arrays.asList(propertySpec));
        PropertyInfo propertyInfo = mock(PropertyInfo.class);
        propertyInfo.key = "customAttribute";
        propertyInfo.required = true;
        propertyInfo.propertyTypeInfo = mock(PropertyTypeInfo.class);
        when(propertyValueInfoService.getPropertyInfo(any(PropertySpec.class), any(Function.class))).thenReturn(propertyInfo);
        return registeredCustomPropertySet;
    }

    private MeterRole mockMeterRole(DefaultMeterRole meterRole) {
        MeterRole mock = mock(MeterRole.class);
        when(mock.getKey()).thenReturn(meterRole.getKey());
        when(mock.getDisplayName()).thenReturn(meterRole.getDefaultFormat());
        return mock;
    }
}

