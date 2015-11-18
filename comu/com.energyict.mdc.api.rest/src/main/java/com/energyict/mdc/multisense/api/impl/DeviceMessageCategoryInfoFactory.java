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
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/20/15.
 */
public class DeviceMessageCategoryInfoFactory extends SelectableFieldFactory<DeviceMessageCategoryInfo, DeviceMessageCategory> {

    @Inject
    public DeviceMessageCategoryInfoFactory() {
    }

    public LinkInfo asLink(DeviceMessageCategory deviceMessageCategory, Relation relation, UriInfo uriInfo) {
        return asLink(deviceMessageCategory, relation, getUriBuilder(uriInfo));
    }

    public List<LinkInfo> asLink(Collection<DeviceMessageCategory> deviceMessageCategorys, Relation relation, UriInfo uriInfo) {
        UriBuilder uriBuilder = getUriBuilder(uriInfo);
        return deviceMessageCategorys.stream().map(i-> asLink(i, relation, uriBuilder)).collect(toList());
    }

    private LinkInfo asLink(DeviceMessageCategory deviceMessageCategory, Relation relation, UriBuilder uriBuilder) {
        LinkInfo info = new LinkInfo();
        info.id = (long)deviceMessageCategory.getId();
        info.link = Link.fromUriBuilder(uriBuilder)
                .rel(relation.rel())
                .title("Device message category")
                .build(deviceMessageCategory.getId());
        return info;
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(DeviceMessageCategoryResource.class)
                .path(DeviceMessageCategoryResource.class, "getDeviceMessageCategory");
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
                    rel(Relation.REF_SELF.rel()).
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
                                .rel(Relation.REF_RELATION.rel())
                                .title("Device message specification")
                                .build(spec.getId().dbValue());
                        return linkInfo;
                    })
                    .collect(toList());
        }));
        return map;
    }
}
