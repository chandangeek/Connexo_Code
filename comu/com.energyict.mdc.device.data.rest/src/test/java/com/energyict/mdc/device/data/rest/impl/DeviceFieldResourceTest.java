package com.energyict.mdc.device.data.rest.impl;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by bvn on 6/21/17.
 */
public class DeviceFieldResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Test
    public void getDeviceCommandStatuses() throws Exception {
        String response = target("/field/devicemessagestatuses").request().get(String.class);
        assertThat(response).contains("deviceMessageStatuses");
    }
}
