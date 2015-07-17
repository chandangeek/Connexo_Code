package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 4/30/15.
 */
public class DeviceTypeInfoFactory {

    @Inject
    public DeviceTypeInfoFactory() {
    }

    private List<PropertyCopier<DeviceTypeInfo, DeviceType>> getSelectedFields(Collection<String> fields) {
        Map<String, PropertyCopier<DeviceTypeInfo, DeviceType>> fieldSelectionMap = buildFieldSelectionMap();
        if (fields==null || fields.isEmpty()) {
            fields = fieldSelectionMap.keySet();
        }
        return fields.stream().filter(fieldSelectionMap::containsKey).map(fieldSelectionMap::get).collect(toList());
    }

    public DeviceTypeInfo asHypermedia(DeviceType deviceType, UriInfo uriInfo, List<String> fields) {
        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        getSelectedFields(fields).stream().forEach(copier -> copier.copy(deviceTypeInfo, deviceType, uriInfo));
        return deviceTypeInfo;
    }

    private Map<String, PropertyCopier<DeviceTypeInfo,DeviceType>> buildFieldSelectionMap() {
        Map<String, PropertyCopier<DeviceTypeInfo, DeviceType>> map = new HashMap<>();
        map.put("id", (deviceTypeInfo, deviceType, uriInfo) -> {
            deviceTypeInfo.id = deviceType.getId();
        });
        map.put("name", (deviceTypeInfo, deviceType, uriInfo) -> {
            deviceTypeInfo.name = deviceType.getName();
        });
        map.put("link", (deviceTypeInfo, deviceType, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(DeviceTypeResource.class).path("{id}");
            deviceTypeInfo.link = Link.fromUriBuilder(uriBuilder).rel(LinkInfo.REF_SELF).title("self reference").build(deviceType.getId());
        });
        map.put("description", (deviceTypeInfo, deviceType, uriInfo) -> {
            deviceTypeInfo.description = deviceType.getDescription();
        });
        map.put("deviceConfigurations", (deviceTypeInfo, deviceType, uriInfo) -> {
            deviceTypeInfo.deviceConfigurations = new ArrayList<>();
            for (DeviceConfiguration deviceConfiguration : deviceType.getConfigurations()) {
                LinkInfo deviceConfigurationInfo = new LinkInfo();
                deviceConfigurationInfo.id = deviceConfiguration.getId();
                UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                        .path(DeviceConfigurationResource.class)
                        .path(DeviceConfigurationResource.class, "getDeviceConfiguration");
                deviceConfigurationInfo.link = Link.fromUriBuilder(uriBuilder).rel("child").title("Device configuration").build(deviceType.getId(), deviceConfiguration.getId());
                deviceTypeInfo.deviceConfigurations.add(deviceConfigurationInfo);
            }
        });
        return map;
    }


}
