package com.energyict.mdc.device.data.api.impl;

import com.energyict.mdc.device.data.Device;
import java.net.URI;
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

    public DeviceInfo plain(Device device) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.id = device.getId();
        deviceInfo.mRID = device.getmRID();
        deviceInfo.serialNumber = device.getSerialNumber();
        deviceInfo.deviceTypeId = device.getDeviceType().getId();
        deviceInfo.deviceTypeName = device.getDeviceType().getName();
        deviceInfo.deviceConfigurationId = device.getDeviceConfiguration().getId();
        deviceInfo.deviceConfigurationName = device.getDeviceConfiguration().getName();
        return deviceInfo;
    }

    public DeviceInfo asHypermedia(Device device, UriInfo uriInfo) {
        DeviceInfo deviceInfo = plain(device);
        deviceInfo.self = Link.fromUri(getUri(device.getmRID(), uriInfo)).rel("self").build();
        return deviceInfo;
    }

    private URI getUri(String mrid, UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder().path(DeviceResource.class).path("{mrid}").build(mrid);
    }

    public HalInfo asHal(Device device, UriInfo uriInfo) {
        DeviceInfo deviceInfo = plain(device);
        URI uri = getUri(deviceInfo.mRID, uriInfo);
        HalInfo wrap = HalInfo.wrap(deviceInfo, uri);
        return wrap;
    }

}
