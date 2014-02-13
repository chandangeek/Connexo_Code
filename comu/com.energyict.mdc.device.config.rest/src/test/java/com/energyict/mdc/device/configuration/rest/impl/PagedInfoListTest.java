package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.QueryParameters;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PagedInfoListTest {

    @Test
    public void testObjectMapperSerializesTypeInformation() throws Exception {
        DeviceTypeInfo deviceTypeInfo1 = new DeviceTypeInfo();
        deviceTypeInfo1.name="new 1";
        deviceTypeInfo1.canBeDirectlyAddressed =true;
        deviceTypeInfo1.canBeGateway=false;
        deviceTypeInfo1.deviceConfigurationCount=4;
        deviceTypeInfo1.loadProfileCount=5;
        deviceTypeInfo1.logBookCount=6;
        deviceTypeInfo1.registerCount=7;
        deviceTypeInfo1.deviceProtocolInfo=new DeviceProtocolInfo();
        deviceTypeInfo1.deviceProtocolInfo.name="protocol name";
        DeviceTypeInfo deviceTypeInfo2 = new DeviceTypeInfo();
        deviceTypeInfo2.name="new 2";
        deviceTypeInfo2.canBeDirectlyAddressed =true;
        deviceTypeInfo2.canBeGateway=false;
        deviceTypeInfo2.deviceConfigurationCount=4;
        deviceTypeInfo2.loadProfileCount=5;
        deviceTypeInfo2.logBookCount=6;
        deviceTypeInfo2.registerCount=7;
        deviceTypeInfo2.deviceProtocolInfo=new DeviceProtocolInfo();
        deviceTypeInfo2.deviceProtocolInfo.name="protocol name";
        ObjectMapper objectMapper = new ObjectMapper();
        QueryParameters queryParameters = mock(QueryParameters.class);
        when(queryParameters.getLimit()).thenReturn(2);
        String response = objectMapper.writeValueAsString(PagedInfoList.asJson("deviceTypes", Arrays.asList(deviceTypeInfo1, deviceTypeInfo2), queryParameters));
        assertThat(response).contains("\"deviceTypes\":[{");
        assertThat(response).contains("\"total\":3");
    }

}
