///*
// * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
// */
//
//package com.energyict.mdc.multisense.api.impl;
//
//import com.elster.jupiter.domain.util.Finder;
//import com.elster.jupiter.util.conditions.Condition;
//import com.energyict.mdc.common.device.config.DeviceConfiguration;
//import com.energyict.mdc.common.device.config.DeviceType;
//import com.energyict.mdc.common.device.data.Device;
//
//import com.jayway.jsonpath.JsonModel;
//
//import javax.ws.rs.core.Response;
//import java.io.ByteArrayInputStream;
//import java.io.InputStream;
//import java.util.Arrays;
//import java.util.List;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.when;
//
///**
// * Created by bvn on 7/16/15.
// */
//public class DeviceTypeResourceTest extends MultisensePublicApiJerseyTest {
//
//    @Override
//    @Before
//    public void setUp() throws Exception {
//        super.setUp();
//
//        DeviceType water = mockDeviceType(10, "water", 3333L);
//        DeviceType gas = mockDeviceType(11, "gas", 3333L);
//        DeviceType elec1 = mockDeviceType(101, "Electricity 1", 3333L);
//        DeviceType elec2 = mockDeviceType(101, "Electricity 2", 3333L);
//        DeviceType elec3 = mockDeviceType(101, "Electricity 3", 3333L);
//        DeviceType elec4 = mockDeviceType(101, "Electricity 4", 3333L);
//        DeviceType elec5 = mockDeviceType(101, "Electricity 5", 3333L);
//        Finder<DeviceType> deviceTypeFinder = mockFinder(Arrays.asList(water, gas, elec1, elec2, elec3, elec4, elec5));
//        when(this.deviceConfigurationService.findAllDeviceTypes()).thenReturn(deviceTypeFinder);
//
//        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(13L, "Default configuration", elec1, 3333L);
//        Device device = mockDevice("DAV", "65749846514", deviceConfiguration, 3333L);
//        Device deviceXas = mockDevice("XAS", "5544657642", deviceConfiguration, 3333L);
//        DeviceConfiguration deviceConfiguration2 = mockDeviceConfiguration(23L, "Default configuration", elec2, 3333L);
//        Device device3 = mockDevice("PIO", "54687651356", deviceConfiguration2, 3333L);
//        Finder<Device> deviceFinder = mockFinder(Arrays.asList(device, deviceXas, device3));
//        when(this.deviceService.findAllDevices(any(Condition.class))).thenReturn(deviceFinder);
//    }
//
//    @Test
//    public void testJsonCallSinglePage() throws Exception {
//
//        Response response = target("/devicetypes").queryParam("start",0).queryParam("limit",10).request("application/json").get();
//        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
//        assertThat(model.<List>get("$.data")).hasSize(7);
//        assertThat(model.<List>get("$.link")).hasSize(1);
//
//    }
//
//    @Test
//    public void testJsonCallMultiPage() throws Exception {
//
//        Response response = target("/devicetypes").queryParam("start",2).queryParam("limit", 2).request("application/json").get();
//        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
//        assertThat(model.<List>get("$.data")).hasSize(2);
//        assertThat(model.<List>get("$.link")).hasSize(3);
//
//    }
//
//    @Test
//    public void testJsonCallSingle() throws Exception {
//        Response response = target("/devicetypes/10").request("application/json").get();
//        JsonModel model = JsonModel.model((InputStream) response.getEntity());
//    }
//
//
//}
