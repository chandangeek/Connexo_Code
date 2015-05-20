package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.jayway.jsonpath.JsonModel;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 4/29/15.
 */
public class ApiTest extends DeviceDataPublicApiJerseyTest {

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

        Device device = mockDevice("DAV", "65749846514");
        Device device2 = mockDevice("XAS", "5544657642");
        Device device3 = mockDevice("PIO", "54687651356");
        when(this.deviceService.findByUniqueMrid("DAV")).thenReturn(Optional.of(device));
        when(this.deviceService.findByUniqueMrid("XAS")).thenReturn(Optional.of(device2));
        Finder<Device> deviceFinder = mockFinder(Arrays.asList(device, device2, device3));
        when(this.deviceService.findAllDevices(any(Condition.class))).thenReturn(deviceFinder);
    }

    private Device mockDevice(String mrid, String serial) {
        Device mock = mock(Device.class);
        when(mock.getmRID()).thenReturn(mrid);
        when(mock.getName()).thenReturn(mrid);
        when(mock.getId()).thenReturn((long) mrid.hashCode());
        when(mock.getSerialNumber()).thenReturn(serial);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getName()).thenReturn("device type X1");
        when(deviceType.getId()).thenReturn(31L);
        when(mock.getDeviceType()).thenReturn(deviceType);
        DeviceConfiguration deviceConfig = mock(DeviceConfiguration.class);
        when(deviceConfig.getName()).thenReturn("Default configuration");
        when(deviceConfig.getId()).thenReturn(34L);
        when(mock.getDeviceConfiguration()).thenReturn(deviceConfig);
        Register register = mock(Register.class);
        when(register.getRegisterSpecId()).thenReturn(666L);
        when(mock.getRegisters()).thenReturn(Collections.singletonList(register));
        return mock;
    }

    private DeviceType mockDeviceType(long id, String name) {
        DeviceType mock = mock(DeviceType.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(1000 + id, "Default");
        when(mock.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
        return mock;
    }

    private DeviceConfiguration mockDeviceConfiguration(long id, String name) {
        DeviceConfiguration mock = mock(DeviceConfiguration.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getName()).thenReturn(name);

        return mock;
    }

    @Test
    public void testJsonCallSinglePage() throws Exception {

        Response response = target("/devicetypes").queryParam("start",0).queryParam("limit",10).request("application/h+json").get();
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<List>get("$.data")).hasSize(7);
        assertThat(model.<List>get("$.links")).hasSize(1);

    }

    @Test
    public void testJsonCallMultiPage() throws Exception {

        Response response = target("/devicetypes").queryParam("start",2).queryParam("limit",2).request("application/h+json").get();
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<List>get("$.data")).hasSize(2);
        assertThat(model.<List>get("$.links")).hasSize(3);

    }

    @Test
    public void testHalJsonCallSingle() throws Exception {

        Response response = target("/devicetypes/10").request("application/hal+json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());

    }

    @Test
    public void testHypermediaLinkJsonCallSingle() throws Exception {

        Response response = target("/devices/XAS").request("application/h+json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());

    }

    @Test
    public void testHypermediaLinkWithFieldsCallSingle() throws Exception {

        Response response = target("/devices/XAS").queryParam("fields", "id,serialNumber").request("application/h+json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());

    }

    @Test
    @Ignore
    public void testLinkJsonCallSingleRegister() throws Exception {

        Response response = target("/devices/XAS/registers/0").request("application/h+json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());

    }

    @Test
    public void testDeviceTypeWithConfig() throws Exception {
        DeviceType serial = mockDeviceType(4, "Serial");
        DeviceType serial2 = mockDeviceType(6, "Serial 2");
        Finder<DeviceType> finder = mockFinder(Arrays.asList(serial, serial2));
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);

        target("/devicetypes").queryParam("fields", "deviceConfigurations").request("application/h+json").get();

    }

    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenReturn(list.stream());
        return finder;
    }

}
