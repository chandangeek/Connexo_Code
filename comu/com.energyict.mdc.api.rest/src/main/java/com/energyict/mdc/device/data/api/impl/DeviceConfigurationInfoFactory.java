package com.energyict.mdc.device.data.api.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
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
public class DeviceConfigurationInfoFactory {

    @Inject
    public DeviceConfigurationInfoFactory() {
    }

    public DeviceConfigurationInfo asId(DeviceConfiguration DeviceConfiguration) {
        DeviceConfigurationInfo DeviceConfigurationInfo = new DeviceConfigurationInfo();
        DeviceConfigurationInfo.id=DeviceConfiguration.getId();
        return DeviceConfigurationInfo;
    }

    public DeviceConfigurationInfo asHypermediaId(DeviceConfiguration DeviceConfiguration, UriInfo uriInfo) {
        DeviceConfigurationInfo DeviceConfigurationInfo = asId(DeviceConfiguration);
        DeviceConfigurationInfo.link = Link.fromUri(getUriTemplate(uriInfo).resolveTemplate("id", DeviceConfiguration.getId()).build()).rel("parent").title("Device type").build();
        return DeviceConfigurationInfo;
    }

    public DeviceConfigurationInfo plain(DeviceConfiguration DeviceConfiguration, Collection<String> fields) {
        DeviceConfigurationInfo DeviceConfigurationInfo = new DeviceConfigurationInfo();
        Map<String, Consumer<DeviceConfiguration>> consumerMap = buildConsumerMap(DeviceConfigurationInfo);
        if (fields==null || fields.isEmpty()) {
            fields = consumerMap.keySet();
        }
        fields.stream().forEach(f->consumerMap.getOrDefault(f, d->d=d).accept(DeviceConfiguration));
        return DeviceConfigurationInfo;
    }

    private Map<String, Consumer<DeviceConfiguration>> buildConsumerMap(DeviceConfigurationInfo DeviceConfigurationInfo) {
        Map<String, Consumer<DeviceConfiguration>> consumerMap = new HashMap<>();
        consumerMap.put("id", d -> DeviceConfigurationInfo.id = d.getId());
        consumerMap.put("name", d -> DeviceConfigurationInfo.name = d.getName());
        return consumerMap;
    }

    public DeviceConfigurationInfo plain(DeviceConfiguration device) {
        DeviceConfigurationInfo deviceInfo = new DeviceConfigurationInfo();
        Map<String, Consumer<DeviceConfiguration>> consumerMap = buildConsumerMap(deviceInfo);
        return plain(device, consumerMap.keySet());
    }

    public DeviceConfigurationInfo asHypermedia(DeviceConfiguration DeviceConfiguration, UriInfo uriInfo, List<String> fields) {
        DeviceConfigurationInfo DeviceConfigurationInfo = plain(DeviceConfiguration, fields);
        DeviceConfigurationInfo.link = Link.fromUri(getUriTemplate(uriInfo).resolveTemplate("id", DeviceConfiguration.getId()).build()).rel("self").title("self reference").build();
        return DeviceConfigurationInfo;
    }

    public HalInfo asHal(DeviceConfiguration DeviceConfiguration, UriInfo uriInfo) {
        DeviceConfigurationInfo DeviceConfigurationInfo = plain(DeviceConfiguration);
        URI uri = getUriTemplate(uriInfo).build(DeviceConfigurationInfo.id);
        HalInfo wrap = HalInfo.wrap(DeviceConfigurationInfo, uri);
        return wrap;
    }

    private UriBuilder getUriTemplate(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder().path(DeviceConfigurationResource.class).path("{id}");
    }

}
