package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.jayway.jsonpath.JsonModel;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 6/1/15.
 */
public class SearchTest extends SearchApplicationTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        SearchDomain devicesDomain = mockDeviceSearchDomain();

        SearchDomain deviceTypeDomain = mock(SearchDomain.class);
        when(deviceTypeDomain.getId()).thenReturn("deviceTypes");
        when(this.searchService.getDomains()).thenReturn(Arrays.asList(devicesDomain, deviceTypeDomain));

    }

    private SearchDomain mockDeviceSearchDomain() {
        SearchDomain devicesDomain = mock(SearchDomain.class);
        when(devicesDomain.getId()).thenReturn("devices");
        SearchableProperty mRID = mockMRID();
        SearchableProperty deviceType = mockDeviceType();
        SearchableProperty deviceConfig = mockDeviceConfig();
        when(deviceConfig.getConstraints()).thenReturn(Arrays.asList(deviceType));

        when(devicesDomain.getProperties()).thenReturn(Arrays.asList(mRID, deviceType, deviceConfig));
        return devicesDomain;
    }

    private SearchableProperty mockMRID() {
        SearchableProperty mRID = mock(SearchableProperty.class);
        when(mRID.getGroup()).thenReturn(Optional.<SearchablePropertyGroup>empty());
        when(mRID.getSelectionMode()).thenReturn(SearchableProperty.SelectionMode.MULTI);
        when(mRID.getVisibility()).thenReturn(SearchableProperty.Visibility.STICKY);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        when(propertySpec.getName()).thenReturn("mRID");
        when(mRID.getSpecification()).thenReturn(propertySpec);
        return mRID;
    }

    private SearchableProperty mockDeviceType() {
        SearchableProperty deviceType = mock(SearchableProperty.class);
        when(deviceType.getGroup()).thenReturn(Optional.<SearchablePropertyGroup>empty());
        when(deviceType.getSelectionMode()).thenReturn(SearchableProperty.SelectionMode.MULTI);
        when(deviceType.getVisibility()).thenReturn(SearchableProperty.Visibility.STICKY);
        PropertySpec propertySpec = mock(PropertySpec.class);
        ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.getValueType()).thenReturn(DeviceType.class);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        when(propertySpec.getName()).thenReturn("deviceType");
        when(deviceType.getSpecification()).thenReturn(propertySpec);
        return deviceType;
    }

    private SearchableProperty mockDeviceConfig() {
        SearchableProperty deviceConfig = mock(SearchableProperty.class);
        when(deviceConfig.getGroup()).thenReturn(Optional.<SearchablePropertyGroup>empty());
        when(deviceConfig.getSelectionMode()).thenReturn(SearchableProperty.SelectionMode.MULTI);
        when(deviceConfig.getVisibility()).thenReturn(SearchableProperty.Visibility.STICKY);
        PropertySpec propertySpec = mock(PropertySpec.class);
        ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.getValueType()).thenReturn(DeviceConfig.class);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        when(deviceConfig.getSpecification()).thenReturn(propertySpec);
        return deviceConfig;
    }

    @Test
    public void testGetDomains() throws Exception {
        Response response = target("/search").request().accept("application/json").get();
        JsonModel model = JsonModel.model((ByteArrayInputStream)response.getEntity());
        assertThat(model.<Integer>get("$.total")).isEqualTo(2);
        assertThat(model.<List>get("$.domains")).hasSize(2);
        assertThat(model.<String>get("$.domains[0].name")).isEqualTo("devices");
        assertThat(model.<String>get("$.domains[0].link.href")).isEqualTo("http://localhost:9998/search/devices");
        assertThat(model.<String>get("$.domains[1].name")).isEqualTo("deviceTypes");
        assertThat(model.<String>get("$.domains[1].link.href")).isEqualTo("http://localhost:9998/search/deviceTypes");
    }

    @Test
    public void testGetDomainProperties() throws Exception {

        Response response = target("/search/devices").request().accept("application/json").get();
        JsonModel model = JsonModel.model((ByteArrayInputStream)response.getEntity());
        assertThat(model.<List>get("$.properties")).hasSize(3);
        assertThat(model.<String>get("$.properties[0].name")).isEqualTo("mRID");
        assertThat(model.<String>get("$.properties[0].type")).isEqualTo("String");
        assertThat(model.<String>get("$.properties[0].selectionMode")).isEqualTo("multiple");
        assertThat(model.<String>get("$.properties[0].visibility")).isEqualTo("sticky");
        assertThat(model.<List>get("$.properties[0].constraints")).hasSize(0);

        assertThat(model.<List>get("$.properties[2].constraints")).hasSize(1);
    }

    class DeviceType {

    }
    class DeviceConfig {

    }
}
