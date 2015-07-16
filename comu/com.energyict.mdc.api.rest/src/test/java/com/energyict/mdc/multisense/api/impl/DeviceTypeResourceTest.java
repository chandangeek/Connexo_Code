package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.jayway.jsonpath.JsonModel;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 7/16/15.
 */
public class DeviceTypeResourceTest extends MultisensePublicApiJerseyTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        DeviceType water = mockDeviceType(10, "water");
        DeviceType gas = mockDeviceType(11, "gas");
        DeviceType elec1 = mockDeviceType(101, "Electricity 1");
        DeviceType elec2 = mockDeviceType(101, "Electricity 2");
        DeviceType elec3 = mockDeviceType(101, "Electricity 3");
        DeviceType elec4 = mockDeviceType(101, "Electricity 4");
        DeviceType elec5 = mockDeviceType(101, "Electricity 5");
        Finder<DeviceType> deviceTypeFinder = mockFinder(Arrays.asList(water, gas, elec1, elec2, elec3, elec4, elec5));
        when(this.deviceConfigurationService.findAllDeviceTypes()).thenReturn(deviceTypeFinder);

        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(13L, "Default configuration", elec1);
        Device device = mockDevice("DAV", "65749846514", deviceConfiguration);
        Device deviceXas = mockDevice("XAS", "5544657642", deviceConfiguration);
        DeviceConfiguration deviceConfiguration2 = mockDeviceConfiguration(23L, "Default configuration", elec2);
        Device device3 = mockDevice("PIO", "54687651356", deviceConfiguration2);
        Finder<Device> deviceFinder = mockFinder(Arrays.asList(device, deviceXas, device3));
        when(this.deviceService.findAllDevices(any(Condition.class))).thenReturn(deviceFinder);
    }

    @Test
    public void testJsonCallSinglePage() throws Exception {

        Response response = target("/devicetypes").queryParam("start",0).queryParam("limit",10).request("application/json").get();
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<List>get("$.data")).hasSize(7);
        assertThat(model.<List>get("$.link")).hasSize(1);

    }

    @Test
    public void testJsonCallMultiPage() throws Exception {

        Response response = target("/devicetypes").queryParam("start",2).queryParam("limit", 2).request("application/json").get();
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<List>get("$.data")).hasSize(2);
        assertThat(model.<List>get("$.link")).hasSize(3);

    }

    @Test
    public void testJsonCallSingle() throws Exception {
        Response response = target("/devicetypes/10").request("application/json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
    }


}
