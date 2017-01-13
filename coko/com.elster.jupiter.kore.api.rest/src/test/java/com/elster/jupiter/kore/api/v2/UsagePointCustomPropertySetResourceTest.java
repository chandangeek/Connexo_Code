package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.UsagePointPropertySet;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.properties.PropertyInfo;
import com.elster.jupiter.rest.api.util.v1.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.api.util.v1.properties.PropertyValueInfo;
import com.elster.jupiter.rest.api.util.v1.properties.SimplePropertyType;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsagePointCustomPropertySetResourceTest extends PlatformPublicApiJerseyTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        UsagePoint usagePoint = mockUsagePoint(MRID, 5L, ServiceKind.ELECTRICITY);
        UsagePointCustomPropertySetExtension extension = mock(UsagePointCustomPropertySetExtension.class);
        CustomPropertySet cps = mockCustomPropertySet("PersonPropertySet", "Person");
        when(usagePoint.forCustomProperties()).thenReturn(extension);
        UsagePointPropertySet usagePointPropertySet = mockUsagePointPropertySet(31, cps, usagePoint, extension);
        UsagePointPropertySet usagePointPropertySet2 = mockUsagePointPropertySet(32, cps, usagePoint, extension);
        when(extension.getAllPropertySets()).thenReturn(Arrays.asList(usagePointPropertySet, usagePointPropertySet2));
        PropertyInfo propertyInfo = new PropertyInfo("string.property", "string.property", new PropertyValueInfo<>("Hello world 1", "default"), new PropertyTypeInfo(SimplePropertyType.TEXT, null, null, null), true);
        when(propertyValueInfoService.getPropertyInfo(any(PropertySpec.class), any(Function.class))).thenReturn(propertyInfo);
    }

    @Test
    public void testAllGetUsagePointCustomPropertySetsPaged() throws Exception {
        // Business method
        Response response = target("/usagepoints/" + MRID + "/custompropertysets")
                .queryParam("start", 0).queryParam("limit", 10).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("link")).hasSize(1);
        assertThat(model.<String>get("link[0].params.rel")).isEqualTo("current");
        assertThat(model.<String>get("link[0].params.title")).isEqualTo("current page");
        assertThat(model.<String>get("link[0].href")).isEqualTo("http://localhost:9998/usagepoints/" + MRID + "/custompropertysets?start=0&limit=10");
        assertThat(model.<List>get("data")).hasSize(2);
        assertThat(model.<Integer>get("data[0].id")).isEqualTo(31);
        assertThat(model.<String>get("data[0].name")).isEqualTo("Person");
        assertThat(model.<String>get("data[0].link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("data[0].link.href")).isEqualTo("http://localhost:9998/usagepoints/" + MRID + "/custompropertysets/31");
    }

    @Test
    public void testGetSingleUsagePointCustomPropertySetWithFields() throws Exception {
        // Business method
        Response response = target("/usagepoints/" + MRID + "/custompropertysets/31").queryParam("fields", "id,name").request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<Integer>get("$.version")).isNull();
        assertThat(model.<String>get("$.name")).isEqualTo("Person");
        assertThat(model.<String>get("$.link")).isNull();
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
        // Business method
        Response response = target("/usagepoints/" + MRID + "/custompropertysets/31").request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<String>get("$.name")).isEqualTo("Person");
        assertThat(model.<String>get("$.domainClass")).isEqualTo("java.lang.String");
        assertThat(model.<Boolean>get("$.isRequired")).isEqualTo(false);
        assertThat(model.<Boolean>get("$.isVersioned")).isEqualTo(false);
        assertThat(model.<List>get("$.properties")).hasSize(2);
        assertThat(model.<String>get("$.properties[0].key")).isEqualTo("string.property");
        assertThat(model.<String>get("$.properties[0].propertyValueInfo.defaultValue")).isEqualTo("default");
        assertThat(model.<Boolean>get("$.properties[0].propertyValueInfo.propertyHasValue")).isEqualTo(false);
        assertThat(model.<String>get("$.properties[0].propertyTypeInfo.simplePropertyType")).isEqualTo("TEXT");
        assertThat(model.<String>get("$.properties[0].propertyTypeInfo.type")).isEqualTo("java.lang.String");
        assertThat(model.<String>get("$.properties[0].propertyTypeInfo.typeSimpleName")).isEqualTo("java.lang.String");
        assertThat(model.<Boolean>get("$.properties[0].required")).isEqualTo(true);
        assertThat(model.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.link.href")).isEqualTo("http://localhost:9998/usagepoints/" + MRID + "/custompropertysets/31");
    }

    @Test
    public void testUpdateCustomPropertySet() throws Exception {
        UsagePointCustomPropertySetInfo info = new UsagePointCustomPropertySetInfo();
        PropertyValueInfo<String> propertyValueInfo = new PropertyValueInfo<>("NewName", "name", true);
        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo(SimplePropertyType.TEXT, null, null, null);
        PropertyValueInfo<BigDecimal> propertyValueInfo2 = new PropertyValueInfo<>(BigDecimal.valueOf(99), BigDecimal.valueOf(18), true);
        PropertyTypeInfo propertyTypeInfo2 = new PropertyTypeInfo(SimplePropertyType.NUMBER, null, null, null);
        CustomPropertySetAttributeInfo info1 = new CustomPropertySetAttributeInfo();
        CustomPropertySetAttributeInfo info2 = new CustomPropertySetAttributeInfo();
        info.properties = Arrays.asList(info1, info2);

        // Business method
        Response response = target("/usagepoints/" + MRID + "/custompropertysets/31").request().put(Entity.json(info));

        // Asserts?
    }

    @Test
    public void testUsagePointCustomPropertySetFields() throws Exception {
        // Business method
        Response response = target("/usagepoints/123/custompropertysets").request("application/json").method("PROPFIND", Response.class);

        // Asserts
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(12);
        assertThat(model.<List<String>>get("$")).containsOnly(
                "version",
                "domainDomainName",
                "endTime",
                "id",
                "isActive",
                "isRequired",
                "isVersioned",
                "link",
                "name",
                "properties",
                "startTime",
                "versionId"
        );
    }
}
