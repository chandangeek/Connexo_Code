/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class DeviceInfoFactory extends SelectableFieldFactory<DeviceInfo, Meter> {

    public final LocationShortInfoFactory locationShortInfoFactory;
    public final UsagePointShortInfoFactory usagePointShortInfoFactory;


    @Inject
    public DeviceInfoFactory(LocationShortInfoFactory locationShortInfoFactory, UsagePointShortInfoFactory usagePointShortInfoFactory) {
        this.locationShortInfoFactory = locationShortInfoFactory;
        this.usagePointShortInfoFactory = usagePointShortInfoFactory;
    }

    public LinkInfo asLink(Meter endDevice, Relation relation, UriInfo uriInfo) {
        DeviceInfo info = new DeviceInfo();
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
                .title("Device")
                .build(endDevice.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder();
    }

    public DeviceInfo from(Meter endDevice, UriInfo uriInfo, Collection<String> fields) {
        DeviceInfo info = new DeviceInfo();
        copySelectedFields(info, endDevice, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<DeviceInfo, Meter>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceInfo, Meter>> map = new HashMap<>();
        map.put("id", (deviceInfo, endDevice, uriInfo) -> deviceInfo.id = endDevice.getId());
        map.put("mRID", (deviceInfo, endDevice, uriInfo) -> deviceInfo.mRID = endDevice
                .getMRID());
        map.put("name", (deviceInfo, endDevice, uriInfo) -> deviceInfo.name = endDevice.getName());
        map.put("location", (deviceInfo, endDevice, uriInfo) -> deviceInfo.location = locationShortInfoFactory.asInfo(endDevice.getLocation().orElse(null)));
        //support for inheriting usage point location
        map.put("usagePoint", (deviceInfo, endDevice, uriInfo) -> {
            if (endDevice != null && Meter.class.isInstance(endDevice)) {
                Meter meter = Meter.class.cast(endDevice);
                Optional<? extends MeterActivation> meterActivation = meter.getCurrentMeterActivation();
                if (meterActivation.isPresent()) {
                    deviceInfo.usagePoint = usagePointShortInfoFactory.asInfo(meterActivation.get().getUsagePoint().orElse(null));
                }}});
        return map;
    }
}
