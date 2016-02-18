package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.cps.rest.CustomPropertySetAttributeInfo;
import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsagePointCustomPropertySetResourceTest extends UsagePointDataRestApplicationJerseyTest {

    private static final long USAGE_POINT_ID = 128L;
    private static final long RCPS_ID = 6L;
    private static final String USAGE_POINT_MRID = "UsagePoint";
    private static final String CPS_ID = "cpsid";
    private static final String CPS_PROPERTY = "testProperty";

    @Mock
    MetrologyConfiguration metrologyConfiguration;
    @Mock
    UsagePoint usagePoint;
    @Mock
    CustomPropertySet customPropertySet;
    @Mock
    RegisteredCustomPropertySet registeredCustomPropertySet;
    @Mock
    UsagePointCustomPropertySetExtension usagePointExtension;
    @Mock
    ServiceCategory serviceCategory;

    @Before
    public void before() {
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(usagePoint.forCustomProperties()).thenReturn(usagePointExtension);
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

        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(registeredCustomPropertySet.isViewableByCurrentUser()).thenReturn(true);
        when(registeredCustomPropertySet.isEditableByCurrentUser()).thenReturn(true);
        when(registeredCustomPropertySet.getViewPrivileges()).thenReturn(EnumSet.of(ViewPrivilege.LEVEL_1));
        when(registeredCustomPropertySet.getEditPrivileges()).thenReturn(EnumSet.of(EditPrivilege.LEVEL_1));
        when(registeredCustomPropertySet.getId()).thenReturn(RCPS_ID);
    }

    private void testAnyCustomPropertySetsResponse(String url) {
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty(CPS_PROPERTY, "test value");
        when(meteringService.findUsagePoint(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(usagePointExtension.getCustomPropertySetValue(eq(registeredCustomPropertySet))).thenReturn(values);

        String json = target("usagepoints/" + USAGE_POINT_MRID + url).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.customPropertySets")).hasSize(1);
        assertThat(jsonModel.<String>get("$.customPropertySets[0].customPropertySetId")).isEqualTo(CPS_ID);
        assertThat(jsonModel.<List>get("$.customPropertySets[0].properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.customPropertySets[0].properties[0].key")).isEqualTo(CPS_PROPERTY);
        assertThat(jsonModel.<String>get("$.customPropertySets[0].properties[0].propertyValueInfo.value")).isEqualTo("test value");
    }

    private void testAnyCustomPropertySetsNoUsagePoint(String url) throws IOException {
        when(meteringService.findUsagePoint(USAGE_POINT_MRID)).thenReturn(Optional.empty());

        Response response = target("usagepoints/" + USAGE_POINT_MRID + url).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).contains("No usage point with MRID UsagePoint");
    }

    private void testGetAnyCustomPropertySetsNoCustomPropertySets(String url) {
        when(meteringService.findUsagePoint(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));

        String json = target("usagepoints/" + USAGE_POINT_MRID + url).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List>get("$.customPropertySets")).hasSize(0);
    }

    @Test
    public void testGetAllCustomPropertySetsOkCase() throws Exception {
        when(usagePointExtension.getAllCustomPropertySets()).thenReturn(Collections.singletonList(registeredCustomPropertySet));
        testAnyCustomPropertySetsResponse("/customproperties");
    }

    @Test
    public void testGetAllCustomPropertySetsNoUsagePoint() throws Exception {
        testAnyCustomPropertySetsNoUsagePoint("/customproperties");
    }

    @Test
    public void testGetAllCustomPropertySetsNoCustomPropertySets() throws Exception {
        when(usagePointExtension.getAllCustomPropertySets()).thenReturn(Collections.emptyList());
        testGetAnyCustomPropertySetsNoCustomPropertySets("/customproperties");
    }

    @Test
    public void testGetCustomPropertySetsOnMetrologyConfigurationOkCase() throws Exception {
        when(usagePointExtension.getCustomPropertySetsOnMetrologyConfiguration()).thenReturn(Collections.singletonList(registeredCustomPropertySet));
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
        when(usagePointExtension.getCustomPropertySetsOnServiceCategory()).thenReturn(Collections.singletonList(registeredCustomPropertySet));
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
        values.setProperty(CPS_PROPERTY, "test value");
        when(meteringService.findUsagePoint(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(usagePointExtension.getAllCustomPropertySets()).thenReturn(Collections.singletonList(registeredCustomPropertySet));
        when(usagePointExtension.getCustomPropertySetValue(eq(registeredCustomPropertySet))).thenReturn(values);

        String json = target("usagepoints/" + USAGE_POINT_MRID + "/customproperties/" + RCPS_ID).request().get(String.class);
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
        assertThat(jsonModel.<String>get("$.properties[0].propertyValueInfo.value")).isEqualTo("test value");
        assertThat(jsonModel.<String>get("$.properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(jsonModel.<Boolean>get("$.properties[0].required")).isEqualTo(false);
    }

    @Test
    public void testSetCustomPropertySetValuesByRegisteredIdOkCase() throws Exception {
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty(CPS_PROPERTY, "test value");
        when(meteringService.findUsagePoint(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByIdAndVersion(USAGE_POINT_ID, 1L)).thenReturn(Optional.of(usagePoint));
        when(usagePointExtension.getAllCustomPropertySets()).thenReturn(Collections.singletonList(registeredCustomPropertySet));
        when(usagePointExtension.getCustomPropertySetValue(eq(registeredCustomPropertySet))).thenReturn(values);

        CustomPropertySetInfo<UsagePointInfo> info = new CustomPropertySetInfo<>();
        info.customPropertySetId = CPS_ID;
        CustomPropertySetAttributeInfo propertiesInfo = new CustomPropertySetAttributeInfo();
        propertiesInfo.propertyValueInfo = new PropertyValueInfo<>("test value", null, null);
        propertiesInfo.key = CPS_PROPERTY;
        info.properties = Arrays.asList(propertiesInfo);
        info.parent = new UsagePointInfo();
        info.parent.id = USAGE_POINT_ID;
        info.parent.mRID = USAGE_POINT_MRID;
        info.parent.version = 1L;

        Response response = target("usagepoints/" + USAGE_POINT_MRID + "/customproperties/" + RCPS_ID).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testSetCustomPropertySetValuesByRegisteredIdNoCustomPropertySets() throws Exception {
        when(meteringService.findUsagePoint(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByIdAndVersion(USAGE_POINT_ID, 1L)).thenReturn(Optional.of(usagePoint));
        when(usagePointExtension.getAllCustomPropertySets()).thenReturn(Collections.emptyList());

        CustomPropertySetInfo<UsagePointInfo> info = new CustomPropertySetInfo<>();
        info.customPropertySetId = CPS_ID;
        CustomPropertySetAttributeInfo propertiesInfo = new CustomPropertySetAttributeInfo();
        propertiesInfo.propertyValueInfo = new PropertyValueInfo<>("test value", null, null);
        propertiesInfo.key = CPS_PROPERTY;
        info.properties = Arrays.asList(propertiesInfo);
        info.parent = new UsagePointInfo();
        info.parent.id = USAGE_POINT_ID;
        info.parent.mRID = USAGE_POINT_MRID;
        info.parent.version = 1L;

        Response response = target("usagepoints/" + USAGE_POINT_MRID + "/customproperties/" + RCPS_ID).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).contains("Custom property set with id");
    }

    @Test
    public void testSetCustomPropertySetValuesByRegisteredIdConflict() throws Exception {
        when(meteringService.findUsagePoint(USAGE_POINT_MRID)).thenReturn(Optional.empty());
        when(meteringService.findUsagePoint(USAGE_POINT_ID)).thenReturn(Optional.empty());
        when(meteringService.findAndLockUsagePointByIdAndVersion(USAGE_POINT_ID, 1L)).thenReturn(Optional.empty());

        CustomPropertySetInfo<UsagePointInfo> info = new CustomPropertySetInfo<>();
        info.customPropertySetId = CPS_ID;
        CustomPropertySetAttributeInfo propertiesInfo = new CustomPropertySetAttributeInfo();
        propertiesInfo.propertyValueInfo = new PropertyValueInfo<>("test value", null, null);
        propertiesInfo.key = CPS_PROPERTY;
        info.properties = Arrays.asList(propertiesInfo);
        info.parent = new UsagePointInfo();
        info.parent.id = USAGE_POINT_ID;
        info.parent.mRID = USAGE_POINT_MRID;
        info.parent.version = 1L;

        Response response = target("usagepoints/" + USAGE_POINT_MRID + "/customproperties/" + RCPS_ID).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }
}
