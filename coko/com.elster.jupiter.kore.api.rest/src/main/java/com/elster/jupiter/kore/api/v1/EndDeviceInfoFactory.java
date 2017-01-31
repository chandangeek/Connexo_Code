/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v1;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class EndDeviceInfoFactory extends SelectableFieldFactory<EndDeviceInfo, Meter> {

    private final Provider<Clock> clock;

    @Inject
    public EndDeviceInfoFactory(Provider<Clock> clock) {
        this.clock = clock;
    }

    public LinkInfo asLink(Meter endDevice, Relation relation, UriInfo uriInfo) {
        EndDeviceInfo info = new EndDeviceInfo();
        copySelectedFields(info, endDevice, uriInfo, Arrays.asList("id", "version"));
        info.link = link(endDevice, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<? extends Meter> endDevices, Relation relation, UriInfo uriInfo) {
        return endDevices.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(Meter endDevice, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("EndDevice")
                .build(endDevice.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(EndDeviceResource.class)
                .path(EndDeviceResource.class, "getEndDevice");
    }

    public EndDeviceInfo from(Meter endDevice, UriInfo uriInfo, Collection<String> fields) {
        EndDeviceInfo info = new EndDeviceInfo();
        copySelectedFields(info, endDevice, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<EndDeviceInfo, Meter>> buildFieldMap() {
        Map<String, PropertyCopier<EndDeviceInfo, Meter>> map = new HashMap<>();
        map.put("id", (endDeviceInfo, endDevice, uriInfo) -> endDeviceInfo.id = endDevice.getId());
        map.put("version", (endDeviceInfo, endDevice, uriInfo) -> endDeviceInfo.version = endDevice
                .getVersion());
        map.put("link", ((endDeviceInfo, endDevice, uriInfo) ->
                endDeviceInfo.link = link(endDevice, Relation.REF_SELF, uriInfo)));
        map.put("mRID", (endDeviceInfo, endDevice, uriInfo) -> endDeviceInfo.mRID = endDevice.getMRID());
        map.put("name", (endDeviceInfo, endDevice, uriInfo) -> endDeviceInfo.name = endDevice.getName());
        map.put("serialNumber", (endDeviceInfo, endDevice, uriInfo) -> endDeviceInfo.serialNumber = endDevice.getSerialNumber());
        map.put("lifecycleState", (endDeviceInfo, endDevice, uriInfo) -> endDeviceInfo.lifecycleState
                = endDevice.getState(clock.get().instant()).map(State::getName).orElse(null));
        return map;
    }
}
