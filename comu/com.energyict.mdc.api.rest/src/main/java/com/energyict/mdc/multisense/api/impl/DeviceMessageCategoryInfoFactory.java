package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriInfo;

/**
 * Created by bvn on 7/20/15.
 */
public class DeviceMessageCategoryInfoFactory extends SelectableFieldFactory<DeviceMessageCategoryInfo, DeviceMessageCategory> {

    public DeviceMessageCategoryInfo asHypermedia(DeviceMessageCategory deviceMessageCategory, UriInfo uriInfo, Collection<String> fields) {
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
                    title("message category").
                    build(deviceMessageCategory.getId())
        ));
        return map;
    }
}
