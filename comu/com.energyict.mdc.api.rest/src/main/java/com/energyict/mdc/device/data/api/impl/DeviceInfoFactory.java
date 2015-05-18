package com.energyict.mdc.device.data.api.impl;

import com.energyict.mdc.device.data.Device;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Created by bvn on 4/30/15.
 */
public class DeviceInfoFactory {

    private final DeviceTypeInfoFactory deviceTypeInfoFactory;

    @Inject
    public DeviceInfoFactory(DeviceTypeInfoFactory deviceTypeInfoFactory) {
        this.deviceTypeInfoFactory = deviceTypeInfoFactory;
    }

    public DeviceInfo plain(Device device, Collection<String> fields) {
        Map<String, BiFunction<DeviceInfo, Device, Object>> consumerMap = buildConsumerMap();
        consumerMap.put("deviceType", (i,d) -> {
            i.deviceType = deviceTypeInfoFactory.asPlainId(d.getDeviceType());
            return null;
        });

        return buildInfo(device, fields, consumerMap);
    }

    public DeviceInfo plain(Device device) {
        return plain(device, Collections.emptyList());
    }

    public DeviceInfo asHypermedia(Device device, UriInfo uriInfo, List<String> fields) {
        Map<String, BiFunction<DeviceInfo, Device, Object>> consumerMap = buildConsumerMap();
        consumerMap.put("deviceType", (i,d) -> {
            i.deviceType = deviceTypeInfoFactory.asPlainId(d.getDeviceType());
            i.deviceType.link = Link.fromUri(getUriTemplate(uriInfo).resolveTemplate("id", d.getDeviceType().getId()).build()).rel("parent").title("Device type").build();

            return null;
        });
        DeviceInfo deviceInfo = buildInfo(device, fields, consumerMap);
        deviceInfo.link = Link.fromUri(getUriTemplate(uriInfo).resolveTemplate("mrid", device.getmRID()).build()).rel("self").title("self reference").build();
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

    private Map<String, BiFunction<DeviceInfo,Device,Object>> buildConsumerMap() {
        Map<String, BiFunction<DeviceInfo, Device, Object>> consumerMap = new HashMap<>();
        consumerMap.put("id", (deviceInfo, device) -> {
            deviceInfo.id = device.getId();
            return null;
        });
        consumerMap.put("serialNumber", (deviceInfo, device) -> {
            deviceInfo.serialNumber = device.getSerialNumber();
            return null;
        });
        consumerMap.put("mIRD", (deviceInfo, device) -> {
            deviceInfo.mIRD = device.getmRID();
            return null;
        });
        return consumerMap;
    }

    private DeviceInfo buildInfo(Device device, Collection<String> fields, Map<String, BiFunction<DeviceInfo, Device, Object>> consumerMap) {
        DeviceInfo deviceInfo = new DeviceInfo();
        if (fields == null || fields.isEmpty()) {
            fields = consumerMap.keySet();
        }
        fields.stream().forEach(f -> consumerMap.getOrDefault(f, (x1, x2) -> {return null;}).apply(deviceInfo, device));
        return deviceInfo;
    }

}
