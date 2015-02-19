package com.energyict.mdc.common.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InfiniteScrollingInfoListTest {

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
        QueryParameters queryParameters = mock(QueryParameters.class);
        when(queryParameters.getLimit()).thenReturn(2);
        List<Object> infos = Arrays.asList(deviceTypeInfo1, deviceTypeInfo2, deviceTypeInfo3, deviceTypeInfo3,deviceTypeInfo3,deviceTypeInfo3);
        String response = objectMapper.writeValueAsString(InfiniteScrollingInfoList.asJson("deviceTypes", infos, queryParameters, infos.size()));
        assertThat(response).contains("\"deviceTypes\":[{");
        assertThat(response).contains("\"total\":6");
        assertThat(response).doesNotContain("\"new 3\"");
    }

    @Test
    public void testHasNextPage() throws Exception {
        QueryParameters queryParameters = mock(QueryParameters.class);
        when(queryParameters.getLimit()).thenReturn(5);
        List<Object> infos = new ArrayList<>();
        for(int i=0;i<20;i++){
            BogusInfo info = new BogusInfo();
            info.name = "ITEM " + String.valueOf(i+1);
            infos.add(info);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        InfiniteScrollingInfoList list = InfiniteScrollingInfoList.asJson("deviceTypes", infos, queryParameters, infos.size());
        String response = objectMapper.writeValueAsString(list);
        assertThat(response).contains("\"deviceTypes\":[{");
        assertThat(response).contains("\"total\":20");
        assertThat(response).doesNotContain("\"ITEM 6\"");
        assertThat(response).doesNotContain("\"ITEM 7\"");
        assertThat(response).doesNotContain("\"ITEM 8\"");
        assertThat(response).doesNotContain("\"ITEM 9\"");
        assertThat(response).doesNotContain("\"ITEM 10\"");
        assertThat(list.getTotal()).isEqualTo(20);
    }

    @Test
    public void testHasNoNextPage() throws Exception {
        QueryParameters queryParameters = mock(QueryParameters.class);
        when(queryParameters.getLimit()).thenReturn(5);
        List<Object> infos = Arrays.asList(new Object(), new Object(), new Object(), new Object(), new Object()); // 5 objects

        InfiniteScrollingInfoList list = InfiniteScrollingInfoList.asJson("list", infos, queryParameters, infos.size());
        assertThat(list.getTotal()).isEqualTo(5);
        assertThat(list.getInfos()).hasSize(5);
    }

    @Test
    public void testNextPage() throws Exception {
        QueryParameters queryParameters = mock(QueryParameters.class);
        when(queryParameters.getLimit()).thenReturn(5);
        List<Object> infos = new ArrayList<>();
        for(int i=0;i<20;i++){
            BogusInfo info = new BogusInfo();
            info.name = "ITEM " + String.valueOf(i+1);
            infos.add(info);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        InfiniteScrollingInfoList list = InfiniteScrollingInfoList.asJson("deviceTypes", infos.subList(5,10), queryParameters, infos.size());
        String response = objectMapper.writeValueAsString(list);
        assertThat(response).contains("\"deviceTypes\":[{");
        assertThat(response).contains("\"total\":20");
        assertThat(response).doesNotContain("\"ITEM 1\"");
        assertThat(response).doesNotContain("\"ITEM 2\"");
        assertThat(response).doesNotContain("\"ITEM 3\"");
        assertThat(response).doesNotContain("\"ITEM 4\"");
        assertThat(response).doesNotContain("\"ITEM 5\"");
        assertThat(response).contains("\"ITEM 6\"");
        assertThat(response).contains("\"ITEM 7\"");
        assertThat(response).contains("\"ITEM 8\"");
        assertThat(response).contains("\"ITEM 9\"");
        assertThat(response).contains("\"ITEM 10\"");
        assertThat(list.getTotal()).isEqualTo(20);
    }

    @Test
    public void testTotalCount() throws Exception {
        QueryParameters queryParameters = mock(QueryParameters.class);
        when(queryParameters.getLimit()).thenReturn(2);
        when(queryParameters.getStart()).thenReturn(0);
        List<Object> infos = Arrays.asList(new Object(), new Object(), new Object(), new Object(), new Object()
                , new Object(), new Object(), new Object(), new Object(), new Object(), new Object()); // 11 objects
        InfiniteScrollingInfoList list = InfiniteScrollingInfoList.asJson("list", infos, queryParameters, infos.size());
        assertThat(list.getTotal()).isEqualTo(infos.size());
        assertThat(list.getInfos()).hasSize(2);

    }
}
