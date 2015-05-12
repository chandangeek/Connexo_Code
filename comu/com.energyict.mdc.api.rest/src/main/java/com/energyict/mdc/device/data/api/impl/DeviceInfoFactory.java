package com.energyict.mdc.device.data.api.impl;

import com.energyict.mdc.device.data.Device;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Created by bvn on 4/30/15.
 */
public class DeviceInfoFactory {
    @Inject
    public DeviceInfoFactory() {
    }

    public DeviceInfo plain(Device device, Collection<String> fields) {
        DeviceInfo deviceInfo = new DeviceInfo();
        Map<String, Consumer<Device>> consumerMap = buildConsumerMap(deviceInfo);
        if (fields==null || fields.isEmpty()) {
            fields = consumerMap.keySet();
        }
        fields.stream().forEach(f->consumerMap.getOrDefault(f, d->d=d).accept(device));
        return deviceInfo;
    }

    private Map<String, Consumer<Device>> buildConsumerMap(DeviceInfo deviceInfo) {
        Map<String, Consumer<Device>> consumerMap = new HashMap<>();
        consumerMap.put("id", d -> deviceInfo.id = d.getId());
        consumerMap.put("serialNumber", d -> deviceInfo.serialNumber = d.getSerialNumber());
        consumerMap.put("mRID", d -> deviceInfo.mIRD = d.getmRID());
        return consumerMap;
    }

    public DeviceInfo plain(Device device) {
        DeviceInfo deviceInfo = new DeviceInfo();
        Map<String, Consumer<Device>> consumerMap = buildConsumerMap(deviceInfo);
        return plain(device, consumerMap.keySet());
    }
//    public DeviceInfo plain(Device device) {
//        DeviceInfo deviceInfo = new DeviceInfo();
//        deviceInfo.id = device.getId();
//        deviceInfo.mIRD = device.getmRID();
//        deviceInfo.serialNumber = device.getSerialNumber();
//        deviceInfo.deviceTypeId = device.getDeviceType().getId();
//        deviceInfo.deviceTypeName = device.getDeviceType().getName();
//        deviceInfo.deviceConfigurationId = device.getDeviceConfiguration().getId();
//        deviceInfo.deviceConfigurationName = device.getDeviceConfiguration().getName();
//        return deviceInfo;
//    }

    public DeviceInfo asHypermedia(Device device, UriInfo uriInfo, List<String> fields) {
        DeviceInfo deviceInfo = plain(device, fields);
        deviceInfo.self = Link.fromUri(getUriTemplate(uriInfo).resolveTemplate("mrid", device.getmRID()).build()).rel("self").title("self reference").build();
        return deviceInfo;
    }

    public HalInfo asHal(Device device, UriInfo uriInfo) {
        DeviceInfo deviceInfo = plain(device);
        URI uri = getUriTemplate(uriInfo).build(deviceInfo.mIRD);
        HalInfo wrap = HalInfo.wrap(deviceInfo, uri);
        return wrap;
    }

    private UriBuilder getUriTemplate(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder().path(DeviceResource.class).path("{mrid}");
    }

}
