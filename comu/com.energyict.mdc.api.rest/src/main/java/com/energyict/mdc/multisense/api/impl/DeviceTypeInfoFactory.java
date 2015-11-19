package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 4/30/15.
 */
public class DeviceTypeInfoFactory extends SelectableFieldFactory<DeviceTypeInfo, DeviceType> {

    private final Provider<DeviceConfigurationInfoFactory> deviceConfigurationInfoFactoryProvider;

    @Inject
    public DeviceTypeInfoFactory(Provider<DeviceConfigurationInfoFactory> deviceConfigurationInfoFactory) {
        this.deviceConfigurationInfoFactoryProvider = deviceConfigurationInfoFactory;
    }

    public LinkInfo asLink(DeviceType deviceType, Relation relation, UriInfo uriInfo) {
        return asLink(deviceType, relation, getUriBuilder(uriInfo));
    }

    public List<LinkInfo> asLink(Collection<DeviceType> deviceTypes, Relation relation, UriInfo uriInfo) {
        UriBuilder uriBuilder = getUriBuilder(uriInfo);
        return deviceTypes.stream().map(i-> asLink(i, relation, uriBuilder)).collect(toList());
    }

    private LinkInfo asLink(DeviceType deviceType, Relation relation, UriBuilder uriBuilder) {
        LinkInfo info = new LinkInfo();
        info.id = deviceType.getId();
        info.link = Link.fromUriBuilder(uriBuilder)
                .rel(relation.rel())
                .title("Device type")
                .build(deviceType.getId());
        return info;
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(DeviceTypeResource.class)
                .path(DeviceTypeResource.class, "getDeviceType");
    }

    public DeviceTypeInfo from(DeviceType deviceType, UriInfo uriInfo, List<String> fields) {
        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        copySelectedFields(deviceTypeInfo, deviceType, uriInfo, fields);
        return deviceTypeInfo;
    }

    protected Map<String, PropertyCopier<DeviceTypeInfo,DeviceType>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceTypeInfo, DeviceType>> map = new HashMap<>();
        map.put("id", (deviceTypeInfo, deviceType, uriInfo) -> {
            deviceTypeInfo.id = deviceType.getId();
        });
        map.put("name", (deviceTypeInfo, deviceType, uriInfo) -> {
            deviceTypeInfo.name = deviceType.getName();
        });
        map.put("link", (deviceTypeInfo, deviceType, uriInfo) -> {
            deviceTypeInfo.link = asLink(deviceType, Relation.REF_SELF, uriInfo).link;
        });
        map.put("description", (deviceTypeInfo, deviceType, uriInfo) -> {
            deviceTypeInfo.description = deviceType.getDescription();
        });
        map.put("deviceConfigurations", (deviceTypeInfo, deviceType, uriInfo) -> {
            deviceTypeInfo.deviceConfigurations = deviceConfigurationInfoFactoryProvider.get().asLink(deviceType.getConfigurations(), Relation.REF_RELATION, uriInfo);
        });
        return map;
    }


}
