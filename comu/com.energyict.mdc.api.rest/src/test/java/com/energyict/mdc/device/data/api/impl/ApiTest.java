package com.energyict.mdc.device.data.api.impl;

import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.rest.JsonQueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.jayway.jsonpath.JsonModel;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.Before;
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

    @Test
    public void testJsonCall() throws Exception {

        Response response = target("/devices").request(MediaType.APPLICATION_JSON_TYPE).get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.total")).isEqualTo(3);
        assertThat(model.<List>get("$.devices")).hasSize(3);

    }

    @Test
    public void testHalJsonCall() throws Exception {

        Response response = target("/devices").request("application/hal+json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());

    }

    @Test
    public void testHalJsonCallSingle() throws Exception {

        Response response = target("/devices/XAS").request("application/hal+json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());

    }

    @Test
    public void testLinkJsonCallSingle() throws Exception {

        Response response = target("/devices/XAS").request("application/h+json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());

    }

    @Test
    public void testLinkJsonCallSingleRegister() throws Exception {

        Response response = target("/devices/XAS/registers/0").request("application/h+json").get();
        JsonModel model = JsonModel.model((InputStream) response.getEntity());

    }

    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(JsonQueryParameters.class))).thenReturn(finder);
        when(finder.defaultSortColumn(anyString())).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenReturn(list.stream());
        return finder;
    }

}
