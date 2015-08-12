package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.jayway.jsonpath.JsonModel;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 6/1/15.
 */
public class DynamicSearchResourceTest extends SearchApplicationTest {

    private SearchDomain devicesDomain;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        SearchDomain devicesDomain = mockDeviceSearchDomain();

        SearchDomain deviceTypeDomain = mock(SearchDomain.class);
        when(deviceTypeDomain.getId()).thenReturn("com.deviceTypes");
        when(this.searchService.getDomains()).thenReturn(Arrays.asList(devicesDomain, deviceTypeDomain));
        when(searchService.findDomain("com.deviceTypes")).thenReturn(Optional.of(deviceTypeDomain));
    }

    private SearchDomain mockDeviceSearchDomain() {
        devicesDomain = mock(SearchDomain.class);
        when(devicesDomain.getId()).thenReturn("com.devices");
        SearchableProperty mRID = mockMRIDProperty(devicesDomain);
        SearchableProperty deviceType = mockDeviceTypeProperty(devicesDomain);
        SearchableProperty deviceConfig = mockDeviceConfigProperty(devicesDomain);
        when(deviceConfig.getConstraints()).thenReturn(Arrays.asList(deviceType));
        when(searchService.findDomain("com.devices")).thenReturn(Optional.of(devicesDomain));

        when(devicesDomain.getProperties()).thenReturn(Arrays.asList(mRID, deviceType, deviceConfig));
        return devicesDomain;
    }

    private SearchableProperty mockMRIDProperty(SearchDomain searchDomain) {
        SearchableProperty mRID = mock(SearchableProperty.class);
        when(mRID.getGroup()).thenReturn(Optional.<SearchablePropertyGroup>empty());
        when(mRID.getDomain()).thenReturn(searchDomain);
        when(mRID.getSelectionMode()).thenReturn(SearchableProperty.SelectionMode.MULTI);
        when(mRID.getVisibility()).thenReturn(SearchableProperty.Visibility.STICKY);
        when(mRID.affectsAvailableDomainProperties()).thenReturn(false);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        when(mRID.getName()).thenReturn("mRID");
        when(mRID.getDisplayName()).thenReturn("mRID");
        when(mRID.getSpecification()).thenReturn(propertySpec);
        return mRID;
    }

    private SearchableProperty mockDeviceTypeProperty(SearchDomain searchDomain) {
        SearchableProperty deviceType = mock(SearchableProperty.class);
        when(deviceType.getGroup()).thenReturn(Optional.<SearchablePropertyGroup>empty());
        when(deviceType.getSelectionMode()).thenReturn(SearchableProperty.SelectionMode.MULTI);
        when(deviceType.getDomain()).thenReturn(searchDomain);
        when(deviceType.affectsAvailableDomainProperties()).thenReturn(true);
        when(deviceType.getVisibility()).thenReturn(SearchableProperty.Visibility.STICKY);
        PropertySpec propertySpec = mock(PropertySpec.class);
        ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.getValueType()).thenReturn(DeviceType.class);
        when(valueFactory.fromStringValue(anyString())).thenAnswer(invocationOnMock -> {
            DeviceType mock = mock(DeviceType.class);
            when(mock.getId()).thenReturn(Long.parseLong((String) invocationOnMock.getArguments()[0]));
            return mock;
        });
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        when(deviceType.getName()).thenReturn("deviceType");
        when(deviceType.getDisplayName()).thenReturn("Device type");
        when(deviceType.getSpecification()).thenReturn(propertySpec);
        return deviceType;
    }

    private SearchableProperty mockDeviceConfigProperty(SearchDomain searchDomain) {
        SearchableProperty deviceConfig = mock(SearchableProperty.class);
        when(deviceConfig.getGroup()).thenReturn(Optional.<SearchablePropertyGroup>empty());
        when(deviceConfig.getSelectionMode()).thenReturn(SearchableProperty.SelectionMode.MULTI);
        when(deviceConfig.getVisibility()).thenReturn(SearchableProperty.Visibility.STICKY);
        when(deviceConfig.affectsAvailableDomainProperties()).thenReturn(false);
        when(deviceConfig.getDomain()).thenReturn(searchDomain);
        when(deviceConfig.toDisplay(any())).thenAnswer(invocationOnMock1 -> ((DeviceConfig) invocationOnMock1.getArguments()[0]).getName());
        PropertySpec propertySpec = mock(PropertySpec.class);
        ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.getValueType()).thenReturn(DeviceConfig.class);
        when(valueFactory.fromStringValue(anyString())).thenAnswer(invocationOnMock -> {
            DeviceConfig mock = mock(DeviceConfig.class);
            when(mock.getId()).thenReturn(Long.parseLong((String) invocationOnMock.getArguments()[0]));
            return mock;
        } );
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        PropertySpecPossibleValues possibleValue = mock(PropertySpecPossibleValues.class);
        DeviceConfig deviceConfig1 = mock(DeviceConfig.class);
        when(deviceConfig1.getName()).thenReturn("device config 1");
        when(deviceConfig1.getId()).thenReturn(1L);
        DeviceConfig deviceConfig2 = mock(DeviceConfig.class);
        when(deviceConfig2.getName()).thenReturn("device config 2");
        when(deviceConfig2.getId()).thenReturn(2L);
        when(possibleValue.getAllValues()).thenReturn(Arrays.asList(deviceConfig1, deviceConfig2));
        when(possibleValue.isExhaustive()).thenReturn(true);
        when(propertySpec.getPossibleValues()).thenReturn(possibleValue);
        when(deviceConfig.getName()).thenReturn("deviceConfig");
        when(deviceConfig.getDisplayName()).thenReturn("Device configuration");
        when(deviceConfig.getSpecification()).thenReturn(propertySpec);
        return deviceConfig;
    }

    @Test
    public void testGetDomains() throws Exception {
        when(this.searchService.getDomains()).thenReturn(Arrays.asList(devicesDomain));
        Response response = target("/search").request().accept("application/json").get();
        JsonModel model = JsonModel.model((ByteArrayInputStream)response.getEntity());
        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.domains")).hasSize(1);
        assertThat(model.<String>get("$.domains[0].id")).isEqualTo("com.devices");
        assertThat(model.<String>get("$.domains[0].displayValue")).isEqualTo("devices");
        assertThat(model.<String>get("$.domains[0].link[0].href")).isEqualTo("http://localhost:9998/search/com.devices");
        assertThat(model.<String>get("$.domains[0].link[0].params.rel")).isEqualTo("self");
        assertThat(model.<String>get("$.domains[0].link[1].href")).isEqualTo("http://localhost:9998/search/com.devices/searchcriteria");
        assertThat(model.<String>get("$.domains[0].link[1].params.rel")).isEqualTo("glossary");
        assertThat(model.<String>get("$.domains[0].link[2].href")).isEqualTo("http://localhost:9998/search/com.devices/model");
        assertThat(model.<String>get("$.domains[0].link[2].params.rel")).isEqualTo("describedby");

    }

    @Test
    public void testGetDomainProperties() throws Exception {

        Response response = target("/search/com.devices/searchcriteria").request().accept("application/json").get();
        JsonModel model = JsonModel.model((ByteArrayInputStream)response.getEntity());
        assertThat(model.<Integer>get("$.total")).isEqualTo(3);
        assertThat(model.<List>get("$.properties")).hasSize(3);
        assertThat(model.<String>get("$.properties[0].name")).isEqualTo("mRID");
        assertThat(model.<String>get("$.properties[0].type")).isEqualTo("String");
        assertThat(model.<String>get("$.properties[0].selectionMode")).isEqualTo("multiple");
        assertThat(model.<String>get("$.properties[0].visibility")).isEqualTo("sticky");
        assertThat(model.<Boolean>get("$.properties[0].affectsAvailableDomainProperties")).isEqualTo(false);
        assertThat(model.<List>get("$.properties[0].constraints")).hasSize(0);
        assertThat(model.<Object>get("$.properties[0].link")).isNull();
        assertThat(model.<Boolean>get("$.properties[0].exhaustive")).isFalse();

        assertThat(model.<Boolean>get("$.properties[1].affectsAvailableDomainProperties")).isEqualTo(true);

        assertThat(model.<String>get("$.properties[2].name")).isEqualTo("deviceConfig");
        assertThat(model.<String>get("$.properties[2].displayValue")).isEqualTo("Device configuration");
        assertThat(model.<String>get("$.properties[2].link.href")).endsWith("/search/com.devices/searchcriteria/deviceConfig");
        assertThat(model.<Boolean>get("$.properties[2].exhaustive")).isTrue();
        assertThat(model.<List>get("$.properties[2].constraints")).hasSize(1);
    }

    @Test
    public void testRestictedDomainProperties() throws Exception {
        Response response = target("/search/com.devices/searchcriteria").queryParam("filter", ExtjsFilter.filter().property("deviceType", Collections.singletonList(13)).create()).request().accept("application/json").get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<List> constrictions = ArgumentCaptor.forClass(List.class);
        verify(devicesDomain).getPropertiesWithConstrictions(constrictions.capture());
        assertThat(((SearchablePropertyConstriction)constrictions.getValue().get(0)).getConstrainingProperty().getName()).isEqualTo("deviceType");
        assertThat(((DeviceType) ((SearchablePropertyConstriction) constrictions.getValue().get(0)).getConstrainingValues().get(0)).getId()).isEqualTo(13L);
    }

    @Test
    public void testGetDomainPropertyValues() throws Exception {
        Response response = target("/search/com.devices/searchcriteria/deviceConfig").request().accept("application/json").get();
        JsonModel model = JsonModel.model((ByteArrayInputStream)response.getEntity());
        assertThat(model.<Integer>get("$.total")).isEqualTo(2);
        assertThat(model.<List>get("$.values")).hasSize(2);
        assertThat(model.<Integer>get("$.values[0].id")).isEqualTo(1);
        assertThat(model.<String>get("$.values[0].displayValue")).isEqualTo("device config 1");
        assertThat(model.<Integer>get("$.values[1].id")).isEqualTo(2);
        assertThat(model.<String>get("$.values[1].displayValue")).isEqualTo("device config 2");
    }

    @Test
    public void testGetDomainPropertyValuesFilterByNameCaseInsensitive() throws Exception {
        Response response = target("/search/com.devices/searchcriteria/deviceConfig").queryParam("filter", ExtjsFilter.filter().property("name", "CONF").create()).request().accept("application/json").get();
        JsonModel model = JsonModel.model((ByteArrayInputStream)response.getEntity());
        assertThat(model.<Integer>get("$.total")).isEqualTo(2);
        assertThat(model.<List>get("$.values")).hasSize(2);
        assertThat(model.<Integer>get("$.values[0].id")).isEqualTo(1);
        assertThat(model.<String>get("$.values[0].displayValue")).isEqualTo("device config 1");
        assertThat(model.<Integer>get("$.values[1].id")).isEqualTo(2);
        assertThat(model.<String>get("$.values[1].displayValue")).isEqualTo("device config 2");
    }

    @Test
    public void testGetDomainPropertyValuesFilterByName() throws Exception {
        Response response = target("/search/com.devices/searchcriteria/deviceConfig").queryParam("filter", ExtjsFilter.filter().property("displayValue", "2").create()).request().accept("application/json").get();
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.values")).hasSize(1);
        assertThat(model.<Integer>get("$.values[0].id")).isEqualTo(2);
        assertThat(model.<String>get("$.values[0].displayValue")).isEqualTo("device config 2");
    }

    @Test
    public void testGetPreselectedDomainPropertyValuesFilterByConstraint() throws Exception {
        Response response = target("/search/com.devices/searchcriteria/deviceConfig").queryParam("filter", ExtjsFilter.filter().property("deviceType", Collections.singletonList(13)).create()).request().accept("application/json").get();
//        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        // Expect not to throw exceptions
    }

    interface DeviceType extends HasId, HasName { }
    interface DeviceConfig extends HasId, HasName {  }
}
