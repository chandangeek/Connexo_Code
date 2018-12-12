/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.MultivaluedHashMap;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KorePagedInfoListTest {

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
        QueryParameters queryParameters = createQueryParameters(0, 2);
        String response = objectMapper.writeValueAsString(KorePagedInfoList.asJson("deviceTypes", Arrays.asList(deviceTypeInfo1, deviceTypeInfo2, deviceTypeInfo3), queryParameters));
        assertThat(response).contains("\"deviceTypes\":[{");
        assertThat(response).contains("\"total\":3");
        assertThat(response).doesNotContain("\"new 3\"");
    }

    @Test
    public void testHasNextPage() throws Exception {
        QueryParameters queryParameters = createQueryParameters(80, 5);
        List<Object> infos = Arrays.asList(new Object(), new Object(), new Object(), new Object(), new Object(), new Object()); // 6 objects

        KorePagedInfoList list = KorePagedInfoList.asJson("list", infos, queryParameters);
        assertThat(list.getTotal()).isEqualTo(86);
        assertThat(list.getInfos()).hasSize(5);
    }

    @Test
    public void testHasNoNextPage() throws Exception {
        QueryParameters queryParameters = createQueryParameters(80, 5);
        List<Object> infos = Arrays.asList(new Object(), new Object(), new Object(), new Object(), new Object()); // 5 objects

        KorePagedInfoList list = KorePagedInfoList.asJson("list", infos, queryParameters);
        assertThat(list.getTotal()).isEqualTo(85);
        assertThat(list.getInfos()).hasSize(5);
    }

    @Test
    public void testNextPage() throws Exception {
        QueryParameters queryParameters = createQueryParameters(10,10);

        List<Object> infos = Arrays.asList(new Object(), new Object(), new Object(), new Object(), new Object()
                , new Object(), new Object(), new Object(), new Object(), new Object(), new Object()); // 11 objects

        KorePagedInfoList list = KorePagedInfoList.asJson("list", infos, queryParameters);
        assertThat(list.getTotal()).isEqualTo(21);
        assertThat(list.getInfos()).hasSize(10);

    }

    @Test
    public void testNextPageWithTotalCount() throws Exception {
        QueryParameters queryParameters = createQueryParameters(10,10);

        List<Object> infos = Arrays.asList(new Object(), new Object(), new Object(), new Object(), new Object()
                , new Object(), new Object(), new Object(), new Object(), new Object(), new Object()); // 11 objects

        int totalCount = infos.size();
        KorePagedInfoList list = KorePagedInfoList.asJson("list", infos, queryParameters, totalCount);
        assertThat(list.getTotal()).isEqualTo(totalCount);
        assertThat(list.getInfos()).hasSize(10);

    }

    @Test
    public void testTotalCount() throws Exception {
        QueryParameters queryParameters = createQueryParameters(0,2);

        List<Object> infos = Arrays.asList(new Object(), new Object(), new Object(), new Object(), new Object()
                , new Object(), new Object(), new Object(), new Object(), new Object(), new Object()); // 11 objects
        KorePagedInfoList list = KorePagedInfoList.asJson("list", infos, queryParameters, infos.size());
        assertThat(list.getTotal()).isEqualTo(infos.size());
        assertThat(list.getInfos()).hasSize(2);

    }

    private QueryParameters createQueryParameters(int start, int limit) {
        MultivaluedHashMap<String, String> multivaluedHashMap = new MultivaluedHashMap<>();
        multivaluedHashMap.put("start", Arrays.asList(""+start));
        multivaluedHashMap.put("limit", Arrays.asList(""+limit));
        return QueryParameters.wrap(multivaluedHashMap);
    }
}
