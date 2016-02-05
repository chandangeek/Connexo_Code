package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.data.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsagePointCustomPropertySetResourceTest extends UsagePointDataRestApplicationJerseyTest {

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

    @Before
    public void before() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(CPS_PROPERTY);
        ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.getValueType()).thenReturn(String.class);
        when(valueFactory.fromStringValue(anyString())).thenAnswer(invocation -> invocation.getArguments()[0]);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);

        when(customPropertySet.getId()).thenReturn(CPS_ID);
        when(customPropertySet.getDomainClass()).thenReturn(UsagePoint.class);
        when(customPropertySet.getPropertySpecs()).thenReturn(Arrays.asList(propertySpec));

        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(registeredCustomPropertySet.isViewableByCurrentUser()).thenReturn(true);
        when(registeredCustomPropertySet.isEditableByCurrentUser()).thenReturn(true);
    }

    @Test
    public void testGetCustomPropertySetValuesForUnexistedUsagePoint() throws Exception {
        when(usagePointDataService.findUsagePointExtensionByMrid(USAGE_POINT_MRID)).thenReturn(Optional.empty());

        Response response = target("usagepoints/" + USAGE_POINT_MRID + "/properties/metrology").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.message")).contains("No usage point with MRID UsagePoint");
    }

    @Test
    public void testGetEmptyCustomPropertySetValues() throws Exception {
        when(usagePointDataService.findUsagePointExtensionByMrid(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePointExtension));
        when(usagePointExtension.getMetrologyConfigurationCustomPropertySetValues()).thenReturn(Collections.emptyMap());

        String json = target("usagepoints/" + USAGE_POINT_MRID + "/properties/metrology").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List>get("$.customPropertySets")).hasSize(0);
    }

    @Test
    public void testGetNonVersionedCustomPropertySetValues() {
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty(CPS_PROPERTY, "test value");
        Map<RegisteredCustomPropertySet, CustomPropertySetValues> valuesMap = new HashMap<>();
        valuesMap.put(registeredCustomPropertySet, values);
        when(usagePointExtension.getMetrologyConfigurationCustomPropertySetValues()).thenReturn(valuesMap);
        when(usagePointDataService.findUsagePointExtensionByMrid(USAGE_POINT_MRID)).thenReturn(Optional.of(usagePointExtension));

        String json = target("usagepoints/" + USAGE_POINT_MRID + "/properties/metrology").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.customPropertySets")).hasSize(1);
        assertThat(jsonModel.<String>get("$.customPropertySets[0].customPropertySetId")).isEqualTo(CPS_ID);
        assertThat(jsonModel.<List>get("$.customPropertySets[0].properties")).hasSize(1);
        assertThat(jsonModel.<String>get("$.customPropertySets[0].properties[0].key")).isEqualTo(CPS_PROPERTY);
        assertThat(jsonModel.<String>get("$.customPropertySets[0].properties[0].propertyValueInfo.value")).isEqualTo("test value");
    }
}
