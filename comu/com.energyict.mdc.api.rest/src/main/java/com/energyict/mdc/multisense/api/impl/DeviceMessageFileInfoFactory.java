package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.energyict.mdc.device.config.DeviceMessageFile;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Provides factory services for {@link DeviceMessageFileInfo}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-23 (11:21)
 */
public class DeviceMessageFileInfoFactory extends SelectableFieldFactory<DeviceMessageFileInfo, DeviceMessageFile> {

    private final Provider<DeviceTypeInfoFactory> deviceTypeInfoFactoryProvider;

    @Inject
    public DeviceMessageFileInfoFactory(Provider<DeviceTypeInfoFactory> deviceTypeInfoFactoryProvider) {
        this.deviceTypeInfoFactoryProvider = deviceTypeInfoFactoryProvider;
    }

    public DeviceMessageFileInfo asLink(DeviceMessageFile deviceMessageFile, Relation relation, UriInfo uriInfo) {
        DeviceMessageFileInfo info = new DeviceMessageFileInfo();
        this.copySelectedFields(info, deviceMessageFile, uriInfo, Collections.singletonList("id"));
        info.link = link(deviceMessageFile, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<DeviceMessageFile> deviceMessageFiles, Relation relation, UriInfo uriInfo) {
        return deviceMessageFiles
                    .stream()
                    .map(each -> asLink(each, relation, uriInfo))
                    .collect(toList());
    }

    private Link link(DeviceMessageFile deviceMessageFile, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(this.getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Device message")
                .build(deviceMessageFile.getDeviceType().getId(), deviceMessageFile.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(DeviceMessageFileResource.class)
                .path(DeviceMessageFileResource.class, "getDeviceMessageFile");
    }

    public DeviceMessageFileInfo from(DeviceMessageFile deviceMessageFile, UriInfo uriInfo, List<String> fields) {
        DeviceMessageFileInfo info = new DeviceMessageFileInfo();
        this.copySelectedFields(info, deviceMessageFile, uriInfo, fields);
        return info;
    }

    protected Map<String, PropertyCopier<DeviceMessageFileInfo, DeviceMessageFile>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceMessageFileInfo, DeviceMessageFile>> map = new HashMap<>();
        map.put("id", (info, deviceMessageFile, uriInfo) ->
            info.id = deviceMessageFile.getId());
        map.put("name", (info, deviceMessageFile, uriInfo) ->
            info.name = deviceMessageFile.getName());
        map.put("deviceType", (info, deviceMessageFile, uriInfo) ->
            info.deviceType = deviceTypeInfoFactoryProvider.get().asLink(deviceMessageFile.getDeviceType(), Relation.REF_PARENT, uriInfo));
        map.put("link", (info, deviceMessageFile, uriInfo) ->
            info.link = link(deviceMessageFile, Relation.REF_SELF, uriInfo));
        return map;
    }

}