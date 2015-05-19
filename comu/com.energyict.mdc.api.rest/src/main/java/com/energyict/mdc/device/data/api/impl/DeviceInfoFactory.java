package com.energyict.mdc.device.data.api.impl;

import com.energyict.mdc.device.data.Device;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 4/30/15.
 */
public class DeviceInfoFactory {

    private final DeviceConfigurationInfoFactory deviceConfigurationInfoFactory;

    @Inject
    public DeviceInfoFactory(DeviceConfigurationInfoFactory deviceConfigurationInfoFactory) {
        this.deviceConfigurationInfoFactory = deviceConfigurationInfoFactory;
    }

    public DeviceInfo plain(Device Device, Collection<String> fields) {
        DeviceInfo DeviceInfo = new DeviceInfo();
        getSelectedFields(fields).stream().forEach(copier -> copier.copy(DeviceInfo, Device, Optional.empty()));
        return DeviceInfo;
    }

    public DeviceInfo asHypermedia(Device Device, UriInfo uriInfo, List<String> fields) {
        DeviceInfo DeviceInfo = new DeviceInfo();
        getSelectedFields(fields).stream().forEach(copier -> copier.copy(DeviceInfo, Device, Optional.of(uriInfo)));
        DeviceInfo.link = Link.fromUriBuilder(getUriTemplate(uriInfo)).rel("self").title("self reference").build(Device.getDeviceType().getId(), Device.getId());
        return DeviceInfo;
    }

    private List<PropertyCopier<DeviceInfo, Device>> getSelectedFields(Collection<String> fields) {
        Map<String, PropertyCopier<DeviceInfo, Device>> fieldSelectionMap = buildFieldSelectionMap();
        if (fields==null || fields.isEmpty()) {
            fields = fieldSelectionMap.keySet();
        }
        return fields.stream().filter(fieldSelectionMap::containsKey).map(fieldSelectionMap::get).collect(toList());
    }

    public HalInfo asHal(Device device, UriInfo uriInfo, List<String> fields) {
        DeviceInfo deviceInfo = plain(device, fields);
        URI uri = getUriTemplate(uriInfo).build(deviceInfo.mIRD);
        HalInfo wrap = HalInfo.wrap(deviceInfo, uri);
        return wrap;
    }

    private UriBuilder getUriTemplate(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder().path(DeviceResource.class).path("{mrid}");
    }

    private Map<String, PropertyCopier<DeviceInfo,Device>> buildFieldSelectionMap() {
        Map<String, PropertyCopier<DeviceInfo, Device>> map = new HashMap<>();
        map.put("id", (deviceInfo, device, uriInfo) -> {
            deviceInfo.id = device.getId();
        });
        map.put("name", (deviceInfo, device, uriInfo) -> {
            deviceInfo.name = device.getName();
        });
        map.put("serialNumber", (deviceInfo, device, uriInfo) -> {
            deviceInfo.serialNumber = device.getSerialNumber();
        });
        map.put("deviceConfiguration", (deviceInfo, device, uriInfo) -> {
            deviceInfo.deviceConfiguration = new DeviceConfigurationInfo();
            deviceInfo.deviceConfiguration.id = device.getDeviceConfiguration().getId();
            deviceInfo.deviceConfiguration.id = device.getDeviceConfiguration().getId();
            if (uriInfo.isPresent()) {
                deviceInfo.deviceConfiguration.link = Link.fromUriBuilder(uriInfo.get().getBaseUriBuilder().path(DeviceConfigurationResource.class).path("{id}")).rel("parent").title("Device configuration").build(device.getDeviceType().getId(), device.getDeviceConfiguration().getId());
            }
            deviceInfo.deviceConfiguration.deviceType = new DeviceTypeInfo();
            deviceInfo.deviceConfiguration.deviceType.id = device.getDeviceType().getId();
            if (uriInfo.isPresent()) {
                deviceInfo.deviceConfiguration.deviceType.link = Link.fromUriBuilder(uriInfo.get().getBaseUriBuilder().path(DeviceTypeResource.class).path("{id}")).rel("parent").title("Device type").build(device.getDeviceType().getId());
            }

        });
        return map;
    }

}
