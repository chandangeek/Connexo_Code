package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/20/15.
 */
public class DeviceMessageCategoryInfoFactory extends SelectableFieldFactory<DeviceMessageCategoryInfo, DeviceMessageCategory> {

    private final DeviceMessageSpecificationInfoFactory deviceMessageSpecificationInfoFactory;

    @Inject
    public DeviceMessageCategoryInfoFactory(DeviceMessageSpecificationInfoFactory deviceMessageSpecificationInfoFactory) {
        this.deviceMessageSpecificationInfoFactory = deviceMessageSpecificationInfoFactory;
    }

    public DeviceMessageCategoryInfo from(DeviceMessageCategory deviceMessageCategory, UriInfo uriInfo, Collection<String> fields) {
        DeviceMessageCategoryInfo info = new DeviceMessageCategoryInfo();
        copySelectedFields(info, deviceMessageCategory, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<DeviceMessageCategoryInfo, DeviceMessageCategory>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceMessageCategoryInfo, DeviceMessageCategory>> map = new HashMap<>();
        map.put("id", (deviceMessageCategoryInfo, deviceMessageCategory, uriInfo) -> deviceMessageCategoryInfo.id = (long) deviceMessageCategory.getId());
        map.put("name", (deviceMessageCategoryInfo, deviceMessageCategory, uriInfo) -> deviceMessageCategoryInfo.name = deviceMessageCategory.getName());
        map.put("description", (deviceMessageCategoryInfo, deviceMessageCategory, uriInfo) -> deviceMessageCategoryInfo.description = deviceMessageCategory.getDescription());
        map.put("link", ((deviceMessageCategoryInfo, deviceMessageCategory, uriInfo) ->
            deviceMessageCategoryInfo.link = Link.fromUriBuilder(uriInfo.
                    getBaseUriBuilder().
                    path(DeviceMessageCategoryResource.class).
                    path(DeviceMessageCategoryResource.class, "getDeviceMessageCategory")).
                    rel(LinkInfo.REF_SELF).
                    title("Device message category").
                    build(deviceMessageCategory.getId())
        ));
        map.put("deviceMessageSpecs", ((deviceMessageCategoryInfo, deviceMessageCategory, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                    .path(DeviceMessageSpecificationResource.class)
                    .path(DeviceMessageSpecificationResource.class, "getDeviceMessageSpecification")
                    .resolveTemplate("categoryId", deviceMessageCategory.getId());
            deviceMessageCategoryInfo.deviceMessageSpecs = deviceMessageCategory
                    .getMessageSpecifications().stream()
                    .map(spec -> {
                        LinkInfo linkInfo = new LinkInfo();
                        linkInfo.id = spec.getId().dbValue();
                        linkInfo.link = Link.fromUriBuilder(uriBuilder)
                                .rel(LinkInfo.REF_RELATION)
                                .title("Device message specification")
                                .build(spec.getId().dbValue());
                        return linkInfo;
                    })
                    .collect(toList());
        }));
        return map;
    }
}
