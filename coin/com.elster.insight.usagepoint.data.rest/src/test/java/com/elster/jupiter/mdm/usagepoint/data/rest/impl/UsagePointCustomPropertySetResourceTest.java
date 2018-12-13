/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.cps.rest.CustomPropertySetAttributeInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointCustomPropertySetValuesManageException;
import com.elster.jupiter.metering.UsagePointVersionedPropertySet;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.rest.util.StatusCode;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsagePointCustomPropertySetResourceTest extends UsagePointDataRestApplicationJerseyTest {

    private static final long USAGE_POINT_ID = 128L;
    private static final long RCPS_ID = 6L;
    private static final String USAGE_POINT_NAME = "UsagePoint";
    private static final String CPS_ID = "cpsid";
    private static final String CPS_PROPERTY = "testProperty";

    @Mock
    MetrologyConfiguration metrologyConfiguration;
    @Mock
    UsagePoint usagePoint;
    @Mock
    CustomPropertySet customPropertySet;
    @Mock
    UsagePointCustomPropertySetExtension usagePointExtension;
    @Mock
    ServiceCategory serviceCategory;
    @Mock
    UsagePointVersionedPropertySet usagePointPropertySet;

    @Before
    public void before() {
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(usagePoint.forCustomProperties()).thenReturn(usagePointExtension);
        when(usagePoint.getServiceLocation()).thenReturn(Optional.empty());
        when(usagePoint.getInstallationTime()).thenReturn(Instant.EPOCH);
        when(usagePoint.getCreateDate()).thenReturn(Instant.EPOCH);
        when(usagePoint.getModificationDate()).thenReturn(Instant.EPOCH);
        when(usagePoint.getDetail(any(Instant.class))).thenReturn(Optional.empty());
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());
        when(usagePointExtension.getVersionedPropertySet(RCPS_ID)).thenReturn(usagePointPropertySet);
        when(usagePointExtension.getPropertySet(RCPS_ID)).thenReturn(usagePointPropertySet);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(CPS_PROPERTY);
        ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.getValueType()).thenReturn(String.class);
        when(valueFactory.fromStringValue(anyString())).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(valueFactory.toStringValue(any())).thenAnswer(invocation -> invocation.getArguments()[0] != null ? invocation.getArguments()[0].toString() : null);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);

        when(customPropertySet.getId()).thenReturn(CPS_ID);
        when(customPropertySet.getDomainClass()).thenReturn(UsagePoint.class);
        when(customPropertySet.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(customPropertySet.defaultViewPrivileges()).thenReturn(EnumSet.of(ViewPrivilege.LEVEL_1));
        when(customPropertySet.defaultEditPrivileges()).thenReturn(EnumSet.of(EditPrivilege.LEVEL_1));

        when(usagePointPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(usagePointPropertySet.isViewableByCurrentUser()).thenReturn(true);
        when(usagePointPropertySet.isEditableByCurrentUser()).thenReturn(true);
        when(usagePointPropertySet.getViewPrivileges()).thenReturn(EnumSet.of(ViewPrivilege.LEVEL_1));
        when(usagePointPropertySet.getEditPrivileges()).thenReturn(EnumSet.of(EditPrivilege.LEVEL_1));
        when(usagePointPropertySet.getId()).thenReturn(RCPS_ID);
        when(usagePointPropertySet.getUsagePoint()).thenReturn(usagePoint);

        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo();
        propertyTypeInfo.simplePropertyType = SimplePropertyType.TEXT;
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.key = "testProperty";
        propertyInfo.required = false;
        propertyInfo.propertyTypeInfo = propertyTypeInfo;
        PropertyValueInfo propertyValueInfo = new PropertyValueInfo();
        propertyValueInfo.value = "version value";
        propertyInfo.propertyValueInfo = propertyValueInfo;
        when(propertyValueInfoService.getPropertyInfo(any(PropertySpec.class), any(Function.class))).thenReturn(propertyInfo);
    }

    private void testAnyCustomPropertySetsResponse(String url) {
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty(CPS_PROPERTY, "version value");
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(usagePointPropertySet.getValues()).thenReturn(values);

        String json = target("usagepoints/" + USAGE_POINT_NAME + url).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.customPropertySets")).hasSize(1);
        assertThat(jsonModel.<String>get("$.customPropertySets[0].customPropertySetId")).isEqualTo(CPS_ID);
        assertThat(jsonModel.<List>get("$.customPropertySets[0].properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.customPropertySets[0].properties[0].key")).isEqualTo(CPS_PROPERTY);
        assertThat(jsonModel.<String>get("$.customPropertySets[0].properties[0].propertyValueInfo.value")).isEqualTo("version value");
    }

    private void testAnyCustomPropertySetsNoUsagePoint(String url) throws IOException {
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.empty());

        Response response = target("usagepoints/" + USAGE_POINT_NAME + url).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).contains("No usage point with name UsagePoint");
    }

    private void testGetAnyCustomPropertySetsNoCustomPropertySets(String url) {
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));

        String json = target("usagepoints/" + USAGE_POINT_NAME + url).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List>get("$.customPropertySets")).hasSize(0);
    }

    @Test
    public void testGetAllCustomPropertySetsOkCase() throws Exception {
        when(usagePointExtension.getAllPropertySets()).thenReturn(Collections.singletonList(usagePointPropertySet));
        testAnyCustomPropertySetsResponse("/customproperties");
    }

    @Test
    public void testGetAllCustomPropertySetsNoUsagePoint() throws Exception {
        testAnyCustomPropertySetsNoUsagePoint("/customproperties");
    }

    @Test
    public void testGetAllCustomPropertySetsNoCustomPropertySets() throws Exception {
        when(usagePointExtension.getAllPropertySets()).thenReturn(Collections.emptyList());
        testGetAnyCustomPropertySetsNoCustomPropertySets("/customproperties");
    }

    @Test
    public void testGetCustomPropertySetsOnMetrologyConfigurationOkCase() throws Exception {
        when(usagePointExtension.getPropertySetsOnMetrologyConfiguration()).thenReturn(Collections.singletonList(usagePointPropertySet));
        testAnyCustomPropertySetsResponse("/customproperties/metrologyconfiguration");
    }

    @Test
    public void testGetCustomPropertySetsOnMetrologyConfigurationNoUsagePoint() throws Exception {
        testAnyCustomPropertySetsNoUsagePoint("/customproperties/metrologyconfiguration");
    }

    @Test
    public void testGetCustomPropertySetsOnMetrologyConfigurationNoCustomPropertySets() throws Exception {
        testAnyCustomPropertySetsNoUsagePoint("/customproperties/metrologyconfiguration");
    }

    @Test
    public void testGetCustomPropertySetsOnServiceCategoryOkCase() throws Exception {
        when(usagePointExtension.getPropertySetsOnServiceCategory()).thenReturn(Collections.singletonList(usagePointPropertySet));
        testAnyCustomPropertySetsResponse("/customproperties/servicecategory");
    }

    @Test
    public void testGetCustomPropertySetsOnServiceCategoryNoUsagePoint() throws Exception {
        testAnyCustomPropertySetsNoUsagePoint("/customproperties/servicecategory");
    }

    @Test
    public void testGetCustomPropertySetsOnServiceCategoryNoCustomPropertySets() throws Exception {
        testAnyCustomPropertySetsNoUsagePoint("/customproperties/servicecategory");
    }

    @Test
    public void testGetCustomPropertySetByRegisteredId() throws Exception {
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty(CPS_PROPERTY, "version value");
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(usagePointExtension.getAllPropertySets()).thenReturn(Collections.singletonList(usagePointPropertySet));
        when(usagePointPropertySet.getValues()).thenReturn(values);

        String json = target("usagepoints/" + USAGE_POINT_NAME + "/customproperties/" + RCPS_ID).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(6);
        assertThat(jsonModel.<String>get("$.customPropertySetId")).isEqualTo(CPS_ID);
        assertThat(jsonModel.<Boolean>get("$.isRequired")).isEqualTo(false);
        assertThat(jsonModel.<Boolean>get("$.isVersioned")).isEqualTo(false);
        assertThat(jsonModel.<Boolean>get("$.isEditable")).isEqualTo(true);
        assertThat(jsonModel.<List>get("$.viewPrivileges")).contains("LEVEL_1");
        assertThat(jsonModel.<List>get("$.editPrivileges")).contains("LEVEL_1");
        assertThat(jsonModel.<List>get("$.defaultViewPrivileges")).contains("LEVEL_1");
        assertThat(jsonModel.<List>get("$.defaultEditPrivileges")).contains("LEVEL_1");
        assertThat(jsonModel.<List>get("$.properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.properties[0].key")).isEqualTo(CPS_PROPERTY);
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.value")).isEqualTo("version value");
        assertThat(jsonModel.<String>get("$.properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(jsonModel.<Boolean>get("$.properties[0].required")).isEqualTo(false);
    }

    @Test
    public void testSetCustomPropertySetValuesByRegisteredIdOkCase() throws Exception {
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty(CPS_PROPERTY, "test value");
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByIdAndVersion(USAGE_POINT_ID, 1L)).thenReturn(Optional.of(usagePoint));
        when(usagePointExtension.getAllPropertySets()).thenReturn(Collections.singletonList(usagePointPropertySet));
        when(usagePointPropertySet.getValues()).thenReturn(values);

        CustomPropertySetInfo<UsagePointInfo> info = new CustomPropertySetInfo<>();
        info.customPropertySetId = CPS_ID;
        CustomPropertySetAttributeInfo propertiesInfo = new CustomPropertySetAttributeInfo();
        propertiesInfo.propertyValueInfo = new PropertyValueInfo<>("test value", null, null);
        propertiesInfo.key = CPS_PROPERTY;
        info.properties = Arrays.asList(propertiesInfo);
        info.parent = new UsagePointInfo();
        info.parent.id = USAGE_POINT_ID;
        info.parent.name = USAGE_POINT_NAME;
        info.parent.version = 1L;

        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/customproperties/" + RCPS_ID).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testSetCustomPropertySetValuesByRegisteredIdNoCustomPropertySets() throws Exception {
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByIdAndVersion(USAGE_POINT_ID, 1L)).thenReturn(Optional.of(usagePoint));
        doThrow(UsagePointCustomPropertySetValuesManageException.noLinkedCustomPropertySet(thesaurus, CPS_ID))
                .when(usagePointExtension).getPropertySet(RCPS_ID);

        CustomPropertySetInfo<UsagePointInfo> info = new CustomPropertySetInfo<>();
        info.customPropertySetId = CPS_ID;
        CustomPropertySetAttributeInfo propertiesInfo = new CustomPropertySetAttributeInfo();
        propertiesInfo.propertyValueInfo = new PropertyValueInfo<>("test value", null, null);
        propertiesInfo.key = CPS_PROPERTY;
        info.properties = Arrays.asList(propertiesInfo);
        info.parent = new UsagePointInfo();
        info.parent.id = USAGE_POINT_ID;
        info.parent.name = USAGE_POINT_NAME;
        info.parent.version = 1L;

        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/customproperties/" + RCPS_ID).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(StatusCode.UNPROCESSABLE_ENTITY.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).contains(" is not attached to the usage point.");
    }

    @Test
    public void testSetCustomPropertySetValuesByRegisteredIdConflict() throws Exception {
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.empty());
        when(meteringService.findUsagePointById(USAGE_POINT_ID)).thenReturn(Optional.empty());
        when(meteringService.findAndLockUsagePointByIdAndVersion(USAGE_POINT_ID, 1L)).thenReturn(Optional.empty());

        CustomPropertySetInfo<UsagePointInfo> info = new CustomPropertySetInfo<>();
        info.customPropertySetId = CPS_ID;
        CustomPropertySetAttributeInfo propertiesInfo = new CustomPropertySetAttributeInfo();
        propertiesInfo.propertyValueInfo = new PropertyValueInfo<>("test value", null, null);
        propertiesInfo.key = CPS_PROPERTY;
        info.properties = Arrays.asList(propertiesInfo);
        info.parent = new UsagePointInfo();
        info.parent.id = USAGE_POINT_ID;
        info.parent.name = USAGE_POINT_NAME;
        info.parent.version = 1L;

        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/customproperties/" + RCPS_ID).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testGetCurrentTimeSlicedCustomPropertySetInterval() throws Exception {
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        int start = 10000000;
        int end = 2 * start;
        when(usagePointPropertySet.getNewVersionInterval())
                .thenReturn(Range.closedOpen(Instant.ofEpochMilli(start), Instant.ofEpochMilli(end)));

        String json = target("usagepoints/" + USAGE_POINT_NAME + "/customproperties/" + RCPS_ID + "/currentinterval").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.start")).isEqualTo(start);
        assertThat(jsonModel.<Number>get("$.end")).isEqualTo(end);
    }

    @Test
    public void testGetAllTimeSlicedCustomPropertySetValues() throws Exception {
        Instant now = Instant.now();
        CustomPropertySetValues version1 = CustomPropertySetValues.emptyDuring(Interval.of(null, now));
        version1.setProperty(CPS_PROPERTY, "version value");
        CustomPropertySetValues version2 = CustomPropertySetValues.emptyDuring(Interval.of(now, null));
        version2.setProperty(CPS_PROPERTY, "version value");
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(usagePointPropertySet.getAllVersionValues()).thenReturn(Arrays.asList(version1, version2));

        String json = target("usagepoints/" + USAGE_POINT_NAME + "/customproperties/" + RCPS_ID + "/versions").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<List>get("$.versions")).hasSize(2);
        assertThat(jsonModel.<String>get("$.versions[0].properties[0].propertyValueInfo.value")).isEqualTo("version value");
        assertThat(jsonModel.<String>get("$.versions[1].properties[0].propertyValueInfo.value")).isEqualTo("version value");
    }

    @Test
    public void testGetTimeSlicedCustomPropertySetVersionValue() throws Exception {
        Instant now = Instant.now();
        CustomPropertySetValues version = CustomPropertySetValues.emptyDuring(Interval.of(now, null));
        version.setProperty(CPS_PROPERTY, "version value");
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(usagePointPropertySet.getVersionValues(now)).thenReturn(version);

        String json = target("usagepoints/" + USAGE_POINT_NAME + "/customproperties/" + RCPS_ID + "/versions/" + now.toEpochMilli()).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(6);
        assertThat(jsonModel.<String>get("$.customPropertySetId")).isEqualTo(CPS_ID);
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.value")).isEqualTo("version value");
    }
}
