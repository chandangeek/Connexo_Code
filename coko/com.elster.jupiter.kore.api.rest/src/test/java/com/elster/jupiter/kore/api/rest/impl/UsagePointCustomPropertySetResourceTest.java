package com.elster.jupiter.kore.api.rest.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.rest.CustomPropertySetAttributeInfo;
import com.elster.jupiter.cps.rest.impl.PropertyType;
import com.elster.jupiter.kore.api.impl.UsagePointCustomPropertySetInfo;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointPropertySet;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.hypermedia.Relation;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsagePointCustomPropertySetResourceTest extends PlatformPublicApiJerseyTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        UsagePoint usagePoint = mockUsagePoint(123L, "up001", 5L, ServiceKind.ELECTRICITY);
        UsagePointCustomPropertySetExtension extension = mock(UsagePointCustomPropertySetExtension.class);
        CustomPropertySet cps = mockCustomPropertySet("PersonPropertySet", "Person");
        when(usagePoint.forCustomProperties()).thenReturn(extension);
        UsagePointPropertySet usagePointPropertySet = mockUsagePointPropertySet(31, cps, usagePoint, extension);
        UsagePointPropertySet usagePointPropertySet2 = mockUsagePointPropertySet(32, cps, usagePoint, extension);
        when(extension.getAllPropertySets()).thenReturn(Arrays.asList(usagePointPropertySet, usagePointPropertySet2));
    }

    @Test
    public void testAllGetUsagePointCustomPropertySetsPaged() throws Exception {
        Response response = target("/usagepoints/123/custompropertysets").queryParam("start", 0)
                .queryParam("limit", 10)
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<List>get("link")).hasSize(1);
        Assertions.assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        Assertions.assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        Assertions.assertThat(model.<String>get("link[0].href"))
                .isEqualTo("http://localhost:9998/usagepoints/123/custompropertysets?start=0&limit=10");
        Assertions.assertThat(model.<List>get("data")).hasSize(2);
        Assertions.assertThat(model.<Integer>get("data[0].id")).isEqualTo(31);
        Assertions.assertThat(model.<String>get("data[0].name")).isEqualTo("Person");
        Assertions.assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        Assertions.assertThat(model.<String>get("data[0].link.href"))
                .isEqualTo("http://localhost:9998/usagepoints/123/custompropertysets/31");
    }

    @Test
    public void testGetSingleUsagePointCustomPropertySetWithFields() throws Exception {
        Response response = target("/usagepoints/123/custompropertysets/31").queryParam("fields", "id,name")
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        Assertions.assertThat(model.<Integer>get("$.version")).isNull();
        Assertions.assertThat(model.<String>get("$.name")).isEqualTo("Person");
        Assertions.assertThat(model.<String>get("$.link")).isNull();
    }

    /**
     * Mocks CPS for string (name) and age (bigdecimal) specs
     */
    private CustomPropertySet mockCustomPropertySet(String id, String name) {
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getId()).thenReturn(id);
        when(customPropertySet.getName()).thenReturn(name);
        when(customPropertySet.getDomainClass()).thenReturn(String.class);
        PropertySpec bigDecimalPropertySpec = mockBigDecimalPropertySpec();
        PropertySpec stringPropertySpec = mockStringPropertySpec();
        when(customPropertySet.getPropertySpecs()).thenReturn(Arrays.asList(stringPropertySpec, bigDecimalPropertySpec));
        return customPropertySet;
    }

    @Test
    public void testGetSingleUsagePointCustomPropertySetAllFields() throws Exception {
        Response response = target("/usagepoints/123/custompropertysets/31").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        Assertions.assertThat(model.<String>get("$.name")).isEqualTo("Person");
        Assertions.assertThat(model.<String>get("$.domainClass")).isEqualTo("java.lang.String");
        Assertions.assertThat(model.<Boolean>get("$.isRequired")).isEqualTo(false);
        Assertions.assertThat(model.<Boolean>get("$.isVersioned")).isEqualTo(false);
        Assertions.assertThat(model.<List>get("$.properties")).hasSize(2);
        Assertions.assertThat(model.<String>get("$.properties[0].key")).isEqualTo("string.property");
        Assertions.assertThat(model.<String>get("$.properties[0].propertyValueInfo.defaultValue")).isEqualTo("default");
        Assertions.assertThat(model.<Boolean>get("$.properties[0].propertyValueInfo.propertyHasValue"))
                .isEqualTo(false);
        Assertions.assertThat(model.<String>get("$.properties[0].propertyTypeInfo.simplePropertyType"))
                .isEqualTo("TEXT");
        Assertions.assertThat(model.<String>get("$.properties[0].propertyTypeInfo.type")).isEqualTo("java.lang.String");
        Assertions.assertThat(model.<String>get("$.properties[0].propertyTypeInfo.typeSimpleName"))
                .isEqualTo("java.lang.String");
        Assertions.assertThat(model.<Boolean>get("$.properties[0].required")).isEqualTo(true);
        Assertions.assertThat(model.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        Assertions.assertThat(model.<String>get("$.link.href"))
                .isEqualTo("http://localhost:9998/usagepoints/123/custompropertysets/31");
    }

    @Test
    public void testUpdateCustomPropertySet() throws Exception {
        UsagePointCustomPropertySetInfo info = new UsagePointCustomPropertySetInfo();
        PropertyValueInfo<String> propertyValueInfo = new PropertyValueInfo<>("NewName", "name", true);
        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo(PropertyType.TEXT, null, null, null);
        PropertyValueInfo<BigDecimal> propertyValueInfo2 = new PropertyValueInfo<>(BigDecimal.valueOf(99), BigDecimal.valueOf(18), true);
        PropertyTypeInfo propertyTypeInfo2 = new PropertyTypeInfo(PropertyType.NUMBER, null, null, null);
        CustomPropertySetAttributeInfo info1 = new CustomPropertySetAttributeInfo();
        CustomPropertySetAttributeInfo info2 = new CustomPropertySetAttributeInfo();
        info.properties = Arrays.asList(info1, info2);
        Response response = target("/usagepoints/123/custompropertysets/31").request().put(Entity.json(info));

    }

    @Test
    public void testUsagePointCustomPropertySetFields() throws Exception {
        Response response = target("/usagepoints/123/custompropertysets").request("application/json")
                .method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<List>get("$")).hasSize(12);
        Assertions.assertThat(model.<List<String>>get("$"))
                .containsOnly("version", "domainDomainName", "endTime", "id", "isActive", "isRequired", "isVersioned", "link", "name", "properties", "startTime", "versionId");
    }


}
