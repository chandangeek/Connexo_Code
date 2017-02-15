/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DevicesForConfigChangeSearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.assertj.core.api.Condition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DevicesForConfigChangeSearchFactoryTest {

    @Mock
    private SearchService searchService;

    @Test
    public void queryFilterTest() throws JsonProcessingException {

        SearchableProperty deviceTypeProperty = mock(SearchableProperty.class);
        when(deviceTypeProperty.getName()).thenReturn("deviceType");
        when(deviceTypeProperty.getSelectionMode()).thenReturn(SearchableProperty.SelectionMode.MULTI);
        SearchableProperty deviceConfigProperty = mock(SearchableProperty.class);
        when(deviceConfigProperty.getName()).thenReturn("deviceConfiguration");
        when(deviceConfigProperty.getSelectionMode()).thenReturn(SearchableProperty.SelectionMode.MULTI);
        SearchDomain searchDomain = mock(SearchDomain.class);
        doReturn(Device.class).when(searchDomain).getDomainClass();
        when(searchDomain.getId()).thenReturn("Device");
        when(searchDomain.getProperties()).thenReturn(Arrays.asList(deviceTypeProperty,  deviceConfigProperty));
        when(searchDomain.getPropertiesValues(Matchers.any())).thenAnswer(
                inv -> searchDomain.getProperties()
                        .stream()
                        .map(props -> ((Function<SearchableProperty, SearchablePropertyValue>)inv.getArguments()[0]).apply(props))
                        .collect(Collectors.toList())
        );
        when(searchService.findDomain(Device.class.getName())).thenReturn(Optional.of(searchDomain));

        Map<String, Object> deviceTypeValues = new HashMap<>();
        deviceTypeValues.put("operator", "==");
        deviceTypeValues.put("criteria", new String[]{"1001"});
        Map<String, Object> deviceConfigValues = new HashMap<>();
        deviceConfigValues.put("operator", "==");
        deviceConfigValues.put("criteria", new String[]{"1001","1002"});
        Map<String, Object> deviceType = new HashMap<>();
        deviceType.put("property", "deviceType");
        deviceType.put("value", new Object[]{deviceTypeValues});
        Map<String, Object> deviceConfig = new HashMap<>();
        deviceConfig.put("property", "deviceConfiguration");
        deviceConfig.put("value", new Object[]{deviceConfigValues});

        String string = new ObjectMapper().writer().writeValueAsString(new Object[]{deviceType, deviceConfig});
//        "[{\"property\":\"deviceType\",\"value\":[{\"operator\":\"==\",\"criteria\":[\"1001\"]}]},{\"property\":\"deviceConfiguration\",\"value\":[{\"operator\":\"==\",\"criteria\":[\"1001\",\"1002\"]}]}]"
        JsonQueryFilter jsonQueryFilter = new JsonQueryFilter(string);

        DevicesForConfigChangeSearchFactory devicesForConfigChangeSearchFactory = new DevicesForConfigChangeSearchFactory(searchService);

        DevicesForConfigChangeSearch devicesForConfigChangeSearch = devicesForConfigChangeSearchFactory.fromQueryFilter(jsonQueryFilter);

        assertThat(devicesForConfigChangeSearch.searchItems).hasSize(2);
        assertThat(devicesForConfigChangeSearch.searchItems.values()).haveExactly(1, new Condition<SearchablePropertyValue.ValueBean>(){
            @Override
            public boolean matches(SearchablePropertyValue.ValueBean value) {
                return value.values.containsAll(Arrays.asList("1001", "1002")) && value.propertyName.equals("deviceConfiguration");
            }
        });
        assertThat(devicesForConfigChangeSearch.searchItems.values()).haveExactly(1, new Condition<SearchablePropertyValue.ValueBean>(){
            @Override
            public boolean matches(SearchablePropertyValue.ValueBean value) {
                return value.values.containsAll(Arrays.asList("1001")) && value.propertyName.equals("deviceType");
            }
        });

    }

}