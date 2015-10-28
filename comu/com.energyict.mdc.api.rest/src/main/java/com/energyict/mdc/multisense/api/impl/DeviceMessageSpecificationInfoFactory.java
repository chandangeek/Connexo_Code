package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DeviceMessageSpecificationInfoFactory extends SelectableFieldFactory<DeviceMessageSpecificationInfo, DeviceMessageSpec> {

    private final MdcPropertyUtils mdcPropertyUtil;

    @Inject
    public DeviceMessageSpecificationInfoFactory(MdcPropertyUtils mdcPropertyUtil) {
        this.mdcPropertyUtil = mdcPropertyUtil;
    }

    public DeviceMessageSpecificationInfo from(DeviceMessageSpec deviceMessageSpecification, UriInfo uriInfo, Collection<String> fields) {
        DeviceMessageSpecificationInfo info = new DeviceMessageSpecificationInfo();
        copySelectedFields(info, deviceMessageSpecification, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<DeviceMessageSpecificationInfo, DeviceMessageSpec>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceMessageSpecificationInfo, DeviceMessageSpec>> map = new HashMap<>();
        map.put("id", (deviceMessageSpecificationInfo, deviceMessageSpecification, uriInfo) -> deviceMessageSpecificationInfo.id = deviceMessageSpecification.getId().dbValue());
        map.put("link", ((deviceMessageSpecificationInfo, deviceMessageSpecification, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo
                    .getBaseUriBuilder()
                    .path(DeviceMessageSpecificationResource.class)
                    .path(DeviceMessageSpecificationResource.class, "getDeviceMessageSpecification")
                    .resolveTemplate("categoryId", deviceMessageSpecification.getCategory().getId());
            deviceMessageSpecificationInfo.link = Link.fromUriBuilder(uriBuilder)
                    .rel(LinkInfo.REF_SELF)
                    .title("Device message specification")
                    .build(deviceMessageSpecification.getId().dbValue());
        }));

        map.put("name", (deviceMessageSpecificationInfo, deviceMessageSpecification, uriInfo) -> deviceMessageSpecificationInfo.name = deviceMessageSpecification.getName());
        map.put("deviceMessageId", (deviceMessageSpecificationInfo, deviceMessageSpecification, uriInfo) -> deviceMessageSpecificationInfo.deviceMessageId = deviceMessageSpecification.getId().name());
        map.put("propertySpecs", (deviceMessageSpecificationInfo, deviceMessageSpecification, uriInfo) -> {
            deviceMessageSpecificationInfo.propertySpecs = mdcPropertyUtil.convertPropertySpecsToPropertyInfos(deviceMessageSpecification.getPropertySpecs(), TypedProperties.empty());
        });
        map.put("category", ((deviceMessageSpecificationInfo, deviceMessageSpecification, uriInfo) -> {
            UriBuilder uirBuilder = uriInfo
                    .getBaseUriBuilder()
                    .path(DeviceMessageCategoryResource.class)
                    .path(DeviceMessageCategoryResource.class, "getDeviceMessageCategory");
            deviceMessageSpecificationInfo.category = new LinkInfo();
            deviceMessageSpecificationInfo.category.id = (long)deviceMessageSpecification.getCategory().getId();
            deviceMessageSpecificationInfo.category.link = Link.fromUriBuilder(uirBuilder)
                    .rel(LinkInfo.REF_PARENT)
                    .title("Device message category")
                    .build(deviceMessageSpecification.getCategory().getId());
        }));

        return map;
    }
}
