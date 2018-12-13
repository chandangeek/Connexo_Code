/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PagedInfoListTest {

    @Test
    public void testObjectMapperSerializesTypeInformation() throws Exception {
        BogusInfo deviceTypeInfo1 = new BogusInfo();
        deviceTypeInfo1.name = "new 1";
        deviceTypeInfo1.canBeDirectlyAddressed = true;
        deviceTypeInfo1.canBeGateway = false;
        deviceTypeInfo1.deviceConfigurationCount = 4;
        deviceTypeInfo1.loadProfileCount = 5;
        deviceTypeInfo1.logBookCount = 6;
        deviceTypeInfo1.registerCount = 7;
        BogusInfo deviceTypeInfo2 = new BogusInfo();
        deviceTypeInfo2.name = "new 2";
        deviceTypeInfo2.canBeDirectlyAddressed = true;
        deviceTypeInfo2.canBeGateway = false;
        deviceTypeInfo2.deviceConfigurationCount = 4;
        deviceTypeInfo2.loadProfileCount = 5;
        deviceTypeInfo2.logBookCount = 6;
        deviceTypeInfo2.registerCount = 7;
        BogusInfo deviceTypeInfo3 = new BogusInfo();
        deviceTypeInfo3.name = "new 3";
        deviceTypeInfo3.canBeDirectlyAddressed = true;
        deviceTypeInfo3.canBeGateway = false;
        deviceTypeInfo3.deviceConfigurationCount = 4;
        deviceTypeInfo3.loadProfileCount = 5;
        deviceTypeInfo3.logBookCount = 6;
        deviceTypeInfo3.registerCount = 7;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonQueryParameters queryParameters = mock(JsonQueryParameters.class);
        when(queryParameters.getLimit()).thenReturn(Optional.of(2));
        when(queryParameters.getStart()).thenReturn(Optional.empty());
        String response = objectMapper.writeValueAsString(PagedInfoList.fromPagedList("deviceTypes", Arrays.asList(deviceTypeInfo1, deviceTypeInfo2, deviceTypeInfo3), queryParameters));
        assertThat(response).contains("\"deviceTypes\":[{");
        assertThat(response).contains("\"total\":3");
        assertThat(response).doesNotContain("\"new 3\"");
    }

    @Test
    public void testHasNextPage() throws Exception {
        JsonQueryParameters queryParameters = mock(JsonQueryParameters.class);
        when(queryParameters.getLimit()).thenReturn(Optional.of(5));
        when(queryParameters.getStart()).thenReturn(Optional.of(80));
        List<Object> infos = Arrays.asList(new Object(), new Object(), new Object(), new Object(), new Object(), new Object()); // 6 objects

        PagedInfoList list = PagedInfoList.fromPagedList("list", infos, queryParameters);
        assertThat(list.getTotal()).isEqualTo(86);
        assertThat(list.getInfos()).hasSize(5);
    }

    @Test
    public void testGetTotalImmutability() throws Exception {
        JsonQueryParameters queryParameters = mock(JsonQueryParameters.class);
        when(queryParameters.getLimit()).thenReturn(Optional.of(5));
        when(queryParameters.getStart()).thenReturn(Optional.of(80));
        List<Object> infos = Arrays.asList(new Object(), new Object(), new Object(), new Object(), new Object(), new Object()); // 6 objects

        PagedInfoList list = PagedInfoList.fromPagedList("list", infos, queryParameters);
        assertThat(list.getTotal()).isEqualTo(86);
        assertThat(list.getInfos()).hasSize(5);
        assertThat(list.getTotal()).isEqualTo(86);
        assertThat(list.getInfos()).hasSize(5);
    }

    @Test
    public void testHasNoNextPage() throws Exception {
        JsonQueryParameters queryParameters = mock(JsonQueryParameters.class);
        when(queryParameters.getLimit()).thenReturn(Optional.of(5));
        when(queryParameters.getStart()).thenReturn(Optional.of(80));
        List<Object> infos = Arrays.asList(new Object(), new Object(), new Object(), new Object(), new Object()); // 5 objects

        PagedInfoList list = PagedInfoList.fromPagedList("list", infos, queryParameters);
        assertThat(list.getTotal()).isEqualTo(85);
        assertThat(list.getInfos()).hasSize(5);
    }

    @Test
    public void testNextPage() throws Exception {
        JsonQueryParameters queryParameters = mock(JsonQueryParameters.class);
        when(queryParameters.getLimit()).thenReturn(Optional.of(10));
        when(queryParameters.getStart()).thenReturn(Optional.of(10));

        List<Object> infos = Arrays.asList(new Object(), new Object(), new Object(), new Object(), new Object()
                , new Object(), new Object(), new Object(), new Object(), new Object(), new Object()); // 11 objects

        PagedInfoList list = PagedInfoList.fromPagedList("list", infos, queryParameters);
        assertThat(list.getTotal()).isEqualTo(21);
        assertThat(list.getInfos()).hasSize(10);

    }
}
