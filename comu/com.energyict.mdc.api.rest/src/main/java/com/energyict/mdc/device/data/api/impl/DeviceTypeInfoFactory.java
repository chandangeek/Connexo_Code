package com.energyict.mdc.device.data.api.impl;

import com.energyict.mdc.device.config.DeviceType;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Created by bvn on 4/30/15.
 */
public class DeviceTypeInfoFactory {

    @Inject
    public DeviceTypeInfoFactory() {
    }

    public DeviceTypeInfo asId(DeviceType deviceType) {
        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        deviceTypeInfo.id=deviceType.getId();
        return deviceTypeInfo;
    }

    public DeviceTypeInfo asHypermediaId(DeviceType deviceType, UriInfo uriInfo) {
        DeviceTypeInfo deviceTypeInfo = asId(deviceType);
        deviceTypeInfo.link = Link.fromUri(getUriTemplate(uriInfo).resolveTemplate("id", deviceType.getId()).build()).rel("parent").title("Device type").build();
        return deviceTypeInfo;
    }

    public DeviceTypeInfo plain(DeviceType deviceType, Collection<String> fields) {
        DeviceTypeInfo deviceTypeInfo = new DeviceTypeInfo();
        Map<String, Consumer<DeviceType>> consumerMap = buildConsumerMap(deviceTypeInfo);
        if (fields==null || fields.isEmpty()) {
            fields = consumerMap.keySet();
        }
        fields.stream().forEach(f->consumerMap.getOrDefault(f, d->d=d).accept(deviceType));
        return deviceTypeInfo;
    }

    private Map<String, Consumer<DeviceType>> buildConsumerMap(DeviceTypeInfo deviceTypeInfo) {
        Map<String, Consumer<DeviceType>> consumerMap = new HashMap<>();
        consumerMap.put("id", d -> deviceTypeInfo.id = d.getId());
        consumerMap.put("name", d -> deviceTypeInfo.name = d.getName());
        return consumerMap;
    }

    public DeviceTypeInfo plain(DeviceType device) {
        DeviceTypeInfo deviceInfo = new DeviceTypeInfo();
        Map<String, Consumer<DeviceType>> consumerMap = buildConsumerMap(deviceInfo);
        return plain(device, consumerMap.keySet());
    }

    public DeviceTypeInfo asHypermedia(DeviceType deviceType, UriInfo uriInfo, List<String> fields) {
        DeviceTypeInfo deviceTypeInfo = plain(deviceType, fields);
        deviceTypeInfo.link = Link.fromUri(getUriTemplate(uriInfo).resolveTemplate("id", deviceType.getId()).build()).rel("self").title("self reference").build();
        return deviceTypeInfo;
    }

    public HalInfo asHal(DeviceType deviceType, UriInfo uriInfo) {
        DeviceTypeInfo deviceTypeInfo = plain(deviceType);
        URI uri = getUriTemplate(uriInfo).build(deviceTypeInfo.id);
        HalInfo wrap = HalInfo.wrap(deviceTypeInfo, uri);
        return wrap;
    }

    private UriBuilder getUriTemplate(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder().path(DeviceTypeResource.class).path("{id}");
    }

}
