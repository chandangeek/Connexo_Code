/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.search.*;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

import static com.elster.jupiter.search.SearchCriteriaService.SearchCriteriaBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class DynamicSearchResourceTest extends SearchApplicationTest {

    private SearchDomain devicesDomain;
    private SearchBuilder searchBuilder;
    private Finder finder;
    private SearchCriteriaBuilder searchCriteriaBuilder;
    private static final String PRIVILEGE = "PRIVILEGE";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        SearchDomain devicesDomain = mockDeviceSearchDomain();

        SearchDomain deviceTypeDomain = mock(SearchDomain.class);
        Query<SearchCriteria> searchCriteriaQuery= mock(Query.class);
        when(deviceTypeDomain.getId()).thenReturn("com.deviceTypes");
        when(this.searchCriteriaService.getCreationRuleQuery()).thenReturn(searchCriteriaQuery);
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
        when(devicesDomain.getPropertiesWithConstrictions(Matchers.anyList())).thenReturn(Arrays.asList(mRID, deviceType, deviceConfig));
        when(devicesDomain.displayName()).thenReturn("devices");
        return devicesDomain;
    }

    private SearchableProperty mockMRIDProperty(SearchDomain searchDomain) {
        SearchableProperty mRID = mock(SearchableProperty.class);
        when(mRID.getGroup()).thenReturn(Optional.empty());
        when(mRID.getDomain()).thenReturn(searchDomain);
        when(mRID.getSelectionMode()).thenReturn(SearchableProperty.SelectionMode.MULTI);
        when(mRID.getVisibility()).thenReturn(SearchableProperty.Visibility.STICKY);
        when(mRID.affectsAvailableDomainProperties()).thenReturn(false);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getValueFactory()).thenReturn(new StringFactory());
        when(mRID.getName()).thenReturn("mRID");
        when(mRID.getDisplayName()).thenReturn("mRID");
        when(mRID.getSpecification()).thenReturn(propertySpec);
        SearchablePropertyGroup group = mock(SearchablePropertyGroup.class);
        when(group.getId()).thenReturn("Abc");
        when(group.getDisplayName()).thenReturn("Group abc");
        when(mRID.getGroup()).thenReturn(Optional.of(group));
        return mRID;
    }

    private SearchableProperty mockDeviceTypeProperty(SearchDomain searchDomain) {
        SearchableProperty deviceType = mock(SearchableProperty.class);
        when(deviceType.getGroup()).thenReturn(Optional.empty());
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
        when(deviceConfig.getGroup()).thenReturn(Optional.empty());
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
        when(possibleValue.getAllValues()).thenReturn(Arrays.asList(deviceConfig2, deviceConfig1));
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
        Form input = new Form();
        input.param("start", null);
        input.param("limit", null);
        input.param("filter", null);
        Entity<Form> entity = Entity.entity(input, MediaType.APPLICATION_FORM_URLENCODED);
        Response response = target("/search/com.devices/searchcriteria").request(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON).post(entity);
        JsonModel model = JsonModel.model((ByteArrayInputStream)response.getEntity());
        assertThat(model.<Integer>get("$.total")).isEqualTo(3);
        assertThat(model.<List>get("$.properties")).hasSize(3);
        assertThat(model.<String>get("$.properties[0].name")).isEqualTo("mRID");
        assertThat(model.<Boolean>get("$.properties[0].affectsAvailableDomainProperties")).isFalse();
        assertThat(model.<String>get("$.properties[0].group.id")).isEqualTo("Abc");
        assertThat(model.<String>get("$.properties[0].group.displayValue")).isEqualTo("Group abc");
        assertThat(model.<String>get("$.properties[0].selectionMode")).isEqualTo("multiple");
        assertThat(model.<Boolean>get("$.properties[0].allowsIsDefined")).isFalse();
        assertThat(model.<Boolean>get("$.properties[0].allowsIsUndefined")).isFalse();
        assertThat(model.<String>get("$.properties[0].visibility")).isEqualTo("sticky");
        assertThat(model.<List>get("$.properties[0].constraints")).hasSize(0);
        assertThat(model.<Object>get("$.properties[0].link")).isNotNull();
        assertThat(model.<Boolean>get("$.properties[1].affectsAvailableDomainProperties")).isEqualTo(true);
        assertThat(model.<String>get("$.properties[2].name")).isEqualTo("deviceConfig");
        assertThat(model.<String>get("$.properties[2].displayValue")).isEqualTo("Device configuration");
        assertThat(model.<String>get("$.properties[2].link.href")).endsWith("/search/com.devices/searchcriteria/deviceConfig");
        assertThat(model.<List>get("$.properties[2].constraints")).hasSize(1);
    }

    @Test
    public void testRestictedDomainProperties() throws Exception {
        Form input = new Form();
        input.param("start", "0");
        input.param("limit", null);
        input.param("filter", "[{\"property\":\"deviceType\",\"value\":[{\"operator\":\"==\",\"criteria\":\"13\"}]}]");
        Entity<Form> entity = Entity.entity(input, MediaType.APPLICATION_FORM_URLENCODED);

        Response response = target("/search/com.devices/searchcriteria").request(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON).post(entity);
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
    public void testGetDomainPropertyValuesSortedByName() throws Exception {
        String response = target("/search/com.devices/searchcriteria/deviceConfig").request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(2);
        assertThat(model.<String>get("$.values[0].displayValue")).isEqualTo("device config 1");
        assertThat(model.<String>get("$.values[1].displayValue")).isEqualTo("device config 2");
    }

    @Test
    public void testGetPreselectedDomainPropertyValuesFilterByConstraint() throws Exception {
        Response response = target("/search/com.devices/searchcriteria/deviceConfig").queryParam("filter", ExtjsFilter.filter().property("deviceType", Collections.singletonList("13")).create()).request().accept("application/json").get();
//        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        // Expect not to throw exceptions
    }

    @Test
    public void testPostSearchWithOutPrivileges() throws Exception {
        User user = mock(User.class);
        Set<Privilege> privileges = new HashSet<>();

        when(user.getPrivileges()).thenReturn(privileges);
        when(threadPrincipalService.getPrincipal()).thenReturn(user);

        Form input = new Form();
        input.param("page", "1");
        input.param("start", "0");
        input.param("limit", "100");
        input.param("filter", "[{\"property\":\"name\",\"value\":[{\"operator\":\"IN\",\"criteria\":[\"SPE01000001\",\"SPE01000002\",\"SPE01000003\"],\"filter\":\"\"}]}]");
        Entity<Form> entity = Entity.entity(input, MediaType.APPLICATION_FORM_URLENCODED);

        JsonQueryParameters jsonQueryParameters = new JsonQueryParameters(0,100);

        List<SearchDeviceObject> resultList = new ArrayList<>();

        SearchDeviceObject  device1 = new SearchDeviceObject(1,"SPE01000001");
        resultList.add(device1);

        InfoFactory infoFactory = mock(InfoFactory.class);
        when(infoFactory.from(any())).thenReturn(resultList);

        when(infoFactoryService.getInfoFactoryFor(any())).thenReturn(infoFactory);

        finder = mock(Finder.class);
        when(finder.from(any(JsonQueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(resultList);

        searchBuilder = mock(SearchBuilder.class);
        when(searchBuilder.toFinder()).thenReturn(finder);

        when(searchService.search(any(SearchDomain.class))).thenReturn(searchBuilder);

        Response response = target("/search/com.devices").request(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON).post(entity);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(model.<Integer>get("$.total")).isEqualTo(0);
    }
    @Test
    public void testPostSearch() throws Exception {

        User user = mock(User.class);
        Privilege privilege1 = mock(Privilege.class);
        when(privilege1.getName()).thenReturn("privilege.administrate.deviceData");
        Privilege privilege2 = mock(Privilege.class);
        when(privilege2.getName()).thenReturn("privilege.view.device");

        Set<Privilege> privileges = new HashSet<>();
        privileges.add(privilege1);
        privileges.add(privilege2);
        when(user.getPrivileges()).thenReturn(privileges);
        when(threadPrincipalService.getPrincipal()).thenReturn(user);

        Form input = new Form();
        input.param("page", "1");
        input.param("start", "0");
        input.param("limit", "100");
        input.param("filter", "[{\"property\":\"name\",\"value\":[{\"operator\":\"IN\",\"criteria\":[\"SPE01000001\",\"SPE01000002\",\"SPE01000003\"],\"filter\":\"\"}]}]");
        Entity<Form> entity = Entity.entity(input, MediaType.APPLICATION_FORM_URLENCODED);

        JsonQueryParameters jsonQueryParameters = new JsonQueryParameters(0,100);

        List<SearchDeviceObject> resultList = new ArrayList<>();

        SearchDeviceObject  device1 = new SearchDeviceObject(1,"SPE01000001");
        resultList.add(device1);
        SearchDeviceObject  device2 = new SearchDeviceObject(2,"SPE01000002");
        resultList.add(device2);
        SearchDeviceObject  device3 = new SearchDeviceObject(3,"SPE01000003");
        resultList.add(device3);

        InfoFactory infoFactory = mock(InfoFactory.class);
        when(infoFactory.from(any())).thenReturn(resultList);

        when(infoFactoryService.getInfoFactoryFor(any())).thenReturn(infoFactory);

        finder = mock(Finder.class);
        when(finder.from(any(JsonQueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(resultList);

        searchBuilder = mock(SearchBuilder.class);
        when(searchBuilder.toFinder()).thenReturn(finder);

        when(searchService.search(any(SearchDomain.class))).thenReturn(searchBuilder);

        Response response = target("/search/com.devices").request(MediaType.APPLICATION_FORM_URLENCODED).accept(MediaType.APPLICATION_JSON).post(entity);

        /* Check arguments passed to Finder.from() method */
        ArgumentCaptor<Finder> argument = ArgumentCaptor.forClass(Finder.class);
        verify(finder).from((QueryParameters) argument.capture());
        assertThat(((QueryParameters)argument.getValue()).getStart().get()).isEqualTo(0);
        assertThat(((QueryParameters)argument.getValue()).getLimit().get()).isEqualTo(100);

        /* Check response */
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(model.<Integer>get("$.total")).isEqualTo(3);
        assertThat(model.<String>get("$.searchResults[0].name")).isEqualTo("SPE01000001");
        assertThat(model.<String>get("$.searchResults[1].name")).isEqualTo("SPE01000002");
        assertThat(model.<String>get("$.searchResults[2].name")).isEqualTo("SPE01000003");

    }

    @Test
    public void testGetSearchCriteria() throws IOException {
        Query<SearchCriteria> searchCriteriaQuery = mock(Query.class);
        when(searchCriteriaService.getCreationRuleQuery()).thenReturn(searchCriteriaQuery);
        when(threadPrincipalService.getPrincipal()).thenReturn(user);
        Response response = target("/search/saveSearchCriteria")
                                    .request("application/json").accept("application/json").get();
        JsonModel model = JsonModel.model((ByteArrayInputStream)response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetSearchCriteriaWithNonExisting() throws IOException {
        Query<SearchCriteria> searchCriteriaQuery = mock(Query.class);
        when(searchCriteriaService.getCreationRuleQuery()).thenReturn(searchCriteriaQuery);
        when(threadPrincipalService.getPrincipal()).thenReturn(user);
        when(threadPrincipalService.getPrincipal().getName()).thenReturn("Dummy");
        Response response = target("/search/saveSearchCriteria")
                .request("application/json").accept("application/json").get();
        JsonModel model = JsonModel.model((ByteArrayInputStream)response.getEntity());

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(model.<net.minidev.json.JSONArray>get("$.numberOfSearchResults").size()).isEqualTo(0);
    }

    @Test
    public void testPostSearchCriteria() throws IOException {
        searchCriteriaBuilder = mock(SearchCriteriaBuilder.class);
        when(threadPrincipalService.getPrincipal()).thenReturn(user);
        when( searchCriteriaService.newSearchCriteria()).thenReturn(searchCriteriaBuilder);
        when(threadPrincipalService.getPrincipal().getName()).thenReturn("root");
        Form input = new Form();
        input.param("domain", "device");
        input.param("filter", "[{\"initialConfig\":{\"property\":\"deviceType\",\"value\":[{\"operator\":\"==\",\"criteria\":[\"10\"],\"filter\":\"\"}],\"id\":\"deviceType\"},\"property\":\"deviceType\",\"value\":[{\"operator\":\"==\",\"criteria\":[\"10\"],\"filter\":\"\"}],\"id\":\"deviceType\"}]");
        Entity<Form> entity = Entity.entity(input, MediaType.APPLICATION_FORM_URLENCODED);
        Response response = target("/search/saveCriteria/device_type")
                .request("application/json").accept("application/json").post(entity);
        JsonModel model = JsonModel.model((ByteArrayInputStream)response.getEntity());

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(model.<String>get("$.status")).isEqualTo("Save");
    }

    @Test
    public void testDeleteSearchCriteria() throws IOException {
        searchCriteriaBuilder = mock(SearchCriteriaBuilder.class);
        when(threadPrincipalService.getPrincipal()).thenReturn(user);
        when( searchCriteriaService.newSearchCriteria()).thenReturn(searchCriteriaBuilder);
        when(threadPrincipalService.getPrincipal().getName()).thenReturn("root");
        Form input = new Form();
        input.param("domain", "device");
        input.param("filter", "[{\"initialConfig\":{\"property\":\"deviceType\",\"value\":[{\"operator\":\"==\",\"criteria\":[\"10\"],\"filter\":\"\"}],\"id\":\"deviceType\"},\"property\":\"deviceType\",\"value\":[{\"operator\":\"==\",\"criteria\":[\"10\"],\"filter\":\"\"}],\"id\":\"deviceType\"}]");
        Entity<Form> entity = Entity.entity(input, MediaType.APPLICATION_FORM_URLENCODED);
        target("/search/saveCriteria/device_type")
                .request("application/json").accept("application/json").post(entity);

        Response response1 = target("/search/searchCriteria/device_type")
                .request("application/json").accept("application/json").delete();

        assertThat(response1.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

    }


    interface DeviceType extends HasId, HasName { }
    interface DeviceConfig extends HasId, HasName {  }

    private class SearchDeviceObject{
        public long id;
        public String name;
        SearchDeviceObject(long id, String name){
            this.id = id;
            this.name = name;
        }
        String getName(){
            return name;
        }
        long getId(){
            return id;
        }

    }
}
