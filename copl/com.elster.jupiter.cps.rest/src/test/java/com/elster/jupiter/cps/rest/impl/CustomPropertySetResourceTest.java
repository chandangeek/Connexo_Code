package com.elster.jupiter.cps.rest.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CustomPropertySetResourceTest extends CustomPropertySetApplicationJerseyTest {

    @Mock
    CustomPropertySetInfoFactory customPropertySetInfoFactory;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        mockRegisteredCustomPropertySets();
    }

    @Test
    public void testGetCustomAttributeSets() throws Exception {
        Map<String, Object> map = target("/custompropertysets").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(1);
        assertThat((List) map.get("customAttributeSets")).hasSize(1);
        Map jsonCustomAttributeSets = (Map) ((List) map.get("customAttributeSets")).get(0);
        assertThat(jsonCustomAttributeSets.get("id")).isEqualTo(100500);
        assertThat(jsonCustomAttributeSets.get("name")).isEqualTo("domainExtensionName");
        assertThat(jsonCustomAttributeSets.get("domainName")).isEqualTo("com.elster.jupiter.properties.BigDecimalFactory");
        assertThat(jsonCustomAttributeSets.get("isActive")).isEqualTo(true);
        assertThat(jsonCustomAttributeSets.get("isRequired")).isEqualTo(true);
        assertThat(jsonCustomAttributeSets.get("isVersioned")).isEqualTo(false);
        List jsonViewPrivileges = (List) jsonCustomAttributeSets.get("viewPrivileges");
        assertThat(jsonViewPrivileges.get(0)).isEqualTo("LEVEL_1");
        List jsonEditPrivileges = (List) jsonCustomAttributeSets.get("editPrivileges");
        assertThat(jsonEditPrivileges.get(0)).isEqualTo("LEVEL_2");
        List jsonDefaultViewPrivileges = (List) jsonCustomAttributeSets.get("defaultViewPrivileges");
        assertThat(jsonDefaultViewPrivileges.get(0)).isEqualTo("LEVEL_3");
        List jsonDefaultEditPrivileges = (List) jsonCustomAttributeSets.get("defaultEditPrivileges");
        assertThat(jsonDefaultEditPrivileges.get(0)).isEqualTo("LEVEL_4");
        Map jsonCustomAttributes = (Map) ((List) jsonCustomAttributeSets.get("attributes")).get(0);
        assertThat(jsonCustomAttributes.get("name")).isEqualTo("customAttribute");
        assertThat(jsonCustomAttributes.get("type")).isEqualTo("com.elster.jupiter.properties.BigDecimalFactory");
        assertThat(jsonCustomAttributes.get("type")).isEqualTo("com.elster.jupiter.properties.BigDecimalFactory");
        assertThat(jsonCustomAttributes.get("typeSimpleName")).isEqualTo("BigDecimalFactory");
        assertThat(jsonCustomAttributes.get("required")).isEqualTo(true);
    }

    @Test
    public void testGetDomains() throws Exception {
        Map<String, Object> map = target("/custompropertysets/domains").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(1);
        Map jsonDomains = (Map) ((List) map.get("domainExtensions")).get(0);
        assertThat(jsonDomains.get("value")).isEqualTo("com.elster.jupiter.properties.BigDecimalFactory");
    }

    @Test
    public void testUpdatePrivileges() throws Exception {
        Entity<CustomPropertySetInfo> json = Entity.json(getCustomPropertySetInfo());
        Response response = target("/custompropertysets/100500").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @SuppressWarnings("unchecked")
    private PropertySpec getPropertySpec() {
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
    private CustomPropertySet getCustomPropertySet() {
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
    private CustomPropertySetInfo getCustomPropertySetInfo() {
        CustomPropertySetInfo customPropertySetInfo = new CustomPropertySetInfo();
        customPropertySetInfo.id = 100500;
        customPropertySetInfo.domainName = "domainName";
        customPropertySetInfo.isActive = true;
        customPropertySetInfo.isRequired = true;
        customPropertySetInfo.isVersioned = true;
        return customPropertySetInfo;
    }

    @SuppressWarnings("unchecked")
    private RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        PropertySpec propertySpec = getPropertySpec();
        CustomPropertySet customPropertySet = getCustomPropertySet();
        RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);
        when(registeredCustomPropertySet.getId()).thenReturn(100500L);
        when(registeredCustomPropertySet.getViewPrivileges()).thenReturn(Sets.newHashSet(ViewPrivilege.LEVEL_1));
        when(registeredCustomPropertySet.getEditPrivileges()).thenReturn(Sets.newHashSet(EditPrivilege.LEVEL_2));
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs()).thenReturn(Arrays.asList(propertySpec));
        return registeredCustomPropertySet;
    }

    @SuppressWarnings("unchecked")
    private void mockRegisteredCustomPropertySets() {
        RegisteredCustomPropertySet registeredCustomPropertySet = getRegisteredCustomPropertySet();
        when(customPropertySetService.findActiveCustomPropertySets()).thenReturn(Arrays.asList(registeredCustomPropertySet));
    }
}