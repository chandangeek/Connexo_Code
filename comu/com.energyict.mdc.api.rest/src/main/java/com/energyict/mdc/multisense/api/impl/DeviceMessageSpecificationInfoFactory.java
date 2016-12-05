package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class DeviceMessageSpecificationInfoFactory extends SelectableFieldFactory<DeviceMessageSpecificationInfo, DeviceMessageSpec> {

    private final MdcPropertyUtils mdcPropertyUtil;
    private final Provider<DeviceMessageCategoryInfoFactory> deviceMessageCategoryInfoFactoryProvider;

    @Inject
    public DeviceMessageSpecificationInfoFactory(MdcPropertyUtils mdcPropertyUtil,
                                                 Provider<DeviceMessageCategoryInfoFactory> deviceMessageCategoryInfoFactory) {
        this.mdcPropertyUtil = mdcPropertyUtil;
        this.deviceMessageCategoryInfoFactoryProvider = deviceMessageCategoryInfoFactory;
    }

    public LinkInfo asLink(DeviceMessageSpec deviceMessageSpecification, Relation relation, UriInfo uriInfo) {
        DeviceMessageSpecificationInfo info = new DeviceMessageSpecificationInfo();
        copySelectedFields(info,deviceMessageSpecification,uriInfo, Arrays.asList("id"));
        info.link = link(deviceMessageSpecification,relation,uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<DeviceMessageSpec> deviceMessageSpecifications, Relation relation, UriInfo uriInfo) {
        return deviceMessageSpecifications.stream().map(i-> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(DeviceMessageSpec deviceMessageSpecification, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Device message specification")
                .build(deviceMessageSpecification.getCategory().getId(), deviceMessageSpecification.getId().dbValue());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                    .path(DeviceMessageSpecificationResource.class)
                    .path(DeviceMessageSpecificationResource.class, "getDeviceMessageSpecification");
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
        map.put("link", ((deviceMessageSpecificationInfo, deviceMessageSpecification, uriInfo) ->
                deviceMessageSpecificationInfo.link = link(deviceMessageSpecification, Relation.REF_SELF, uriInfo)));
        map.put("name", (deviceMessageSpecificationInfo, deviceMessageSpecification, uriInfo) -> deviceMessageSpecificationInfo.name = deviceMessageSpecification.getName());
        map.put("deviceMessageId", (deviceMessageSpecificationInfo, deviceMessageSpecification, uriInfo) -> deviceMessageSpecificationInfo.deviceMessageId = deviceMessageSpecification.getId().name());
        map.put("propertySpecs", (deviceMessageSpecificationInfo, deviceMessageSpecification, uriInfo) -> {
            deviceMessageSpecificationInfo.propertySpecs = mdcPropertyUtil.convertPropertySpecsToPropertyInfos(deviceMessageSpecification.getPropertySpecs(), TypedProperties.empty());
        });
        map.put("category", ((deviceMessageSpecificationInfo, deviceMessageSpecification, uriInfo) ->
                deviceMessageSpecificationInfo.category = deviceMessageCategoryInfoFactoryProvider.get().asLink(deviceMessageSpecification.getCategory(), Relation.REF_PARENT, uriInfo)));

        return map;
    }
}
