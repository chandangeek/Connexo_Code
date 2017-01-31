/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;

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

/**
 * Created by bvn on 7/20/15.
 */
public class DeviceMessageCategoryInfoFactory extends SelectableFieldFactory<DeviceMessageCategoryInfo, DeviceMessageCategory> {

    private final Provider<DeviceMessageSpecificationInfoFactory> deviceMessageSpecificationInfoFactoryProvider;

    @Inject
    public DeviceMessageCategoryInfoFactory(Provider<DeviceMessageSpecificationInfoFactory> deviceMessageSpecificationInfoFactoryProvider) {
        this.deviceMessageSpecificationInfoFactoryProvider = deviceMessageSpecificationInfoFactoryProvider;
    }

    public LinkInfo asLink(DeviceMessageCategory deviceMessageCategory, Relation relation, UriInfo uriInfo) {
        DeviceMessageCategoryInfo info = new DeviceMessageCategoryInfo();
        copySelectedFields(info,deviceMessageCategory,uriInfo, Arrays.asList("id"));
        info.link = link(deviceMessageCategory,relation,uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<DeviceMessageCategory> deviceMessageCategorys, Relation relation, UriInfo uriInfo) {
        return deviceMessageCategorys.stream().map(i-> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(DeviceMessageCategory deviceMessageCategory, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Device message category")
                .build(deviceMessageCategory.getId());
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
            deviceMessageCategoryInfo.link = asLink(deviceMessageCategory, Relation.REF_SELF, uriInfo).link));
        map.put("deviceMessageSpecs", ((deviceMessageCategoryInfo, deviceMessageCategory, uriInfo) ->
            deviceMessageCategoryInfo.deviceMessageSpecs = deviceMessageSpecificationInfoFactoryProvider.get().asLink(deviceMessageCategory.getMessageSpecifications(), Relation.REF_RELATION, uriInfo)));
        return map;
    }
}
