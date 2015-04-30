package com.energyict.mdc.device.data.api.impl;

import com.energyict.mdc.device.data.Device;
import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriInfo;

/**
 * Created by bvn on 4/30/15.
 */
public class DeviceInfoFactory {

    @Inject
    public DeviceInfoFactory() {
    }

    public DeviceInfo from(Device device) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.id = device.getId();
        deviceInfo.mIRD = device.getmRID();
        deviceInfo.serialNumber = device.getSerialNumber();
        deviceInfo.deviceTypeId = device.getDeviceType().getId();
        deviceInfo.deviceTypeName = device.getDeviceType().getName();
        deviceInfo.deviceConfigurationId = device.getDeviceConfiguration().getId();
        deviceInfo.deviceConfigurationName = device.getDeviceConfiguration().getName();
        return deviceInfo;
    }

    public DeviceInfo from(Device device, UriInfo uriInfo) {
        DeviceInfo deviceInfo = from(device);
        deviceInfo.self = Link.fromUri(uriInfo.getBaseUriBuilder().path(DeviceResource.class).path("{mrid}").build(device.getmRID())).rel("self").build();
        return deviceInfo;
    }

}
