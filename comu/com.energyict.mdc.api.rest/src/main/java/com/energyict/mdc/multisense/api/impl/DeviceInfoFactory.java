/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.TopologyTimeline;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 4/30/15.
 */
public class DeviceInfoFactory extends SelectableFieldFactory<DeviceInfo, Device> {

    private static final int RECENTLY_ADDED_COUNT = 5;

    private final TopologyService topologyService;
    private final DeviceLifeCycleService deviceLifeCycleService;

    @Inject
    public DeviceInfoFactory(TopologyService topologyService,
                             DeviceLifeCycleService deviceLifeCycleService) {
        this.topologyService = topologyService;
        this.deviceLifeCycleService = deviceLifeCycleService;
    }

    public LinkInfo asLink(Device device, Relation relation, UriInfo uriInfo) {
        DeviceInfo info = new DeviceInfo();
        copySelectedFields(info, device, uriInfo, Arrays.asList("id", "version"));
        info.link = link(device, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<Device> devices, Relation relation, UriInfo uriInfo) {
        return devices.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(Device device, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Device")
                .build(device.getmRID());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(DeviceResource.class)
                .path(DeviceResource.class, "getDevice");
    }

    public DeviceInfo asHypermedia(Device device, UriInfo uriInfo, Collection<String> fields) {
        DeviceInfo deviceInfo = new DeviceInfo();
        copySelectedFields(deviceInfo, device, uriInfo, fields);
        return deviceInfo;
    }

    private UriBuilder getUriTemplate(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder().path(DeviceResource.class).path("{mrid}");
    }

    protected Map<String, PropertyCopier<DeviceInfo, Device>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceInfo, Device>> map = new HashMap<>();
        map.put("id", (deviceInfo, device, uriInfo) -> deviceInfo.id = device.getId());
        map.put("link", (deviceInfo, device, uriInfo) -> deviceInfo.link = link(device, Relation.REF_SELF, uriInfo));
        map.put("name", (deviceInfo, device, uriInfo) -> deviceInfo.name = device.getName());
        map.put("mRID", (deviceInfo, device, uriInfo) -> deviceInfo.mRID = device.getmRID());
        map.put("serialNumber", (deviceInfo, device, uriInfo) -> deviceInfo.serialNumber = device.getSerialNumber());
        map.put("manufacturer", (deviceInfo, device, uriInfo) -> deviceInfo.manufacturer = device.getManufacturer());
        map.put("modelNumber", (deviceInfo, device, uriInfo) -> deviceInfo.modelNbr = device.getModelNumber());
        map.put("modelVersion", (deviceInfo, device, uriInfo) -> deviceInfo.modelVersion = device.getModelVersion());
        map.put("deviceProtocolPluggeableClassId", (deviceInfo, device, uriInfo) -> deviceInfo.deviceProtocolPluggeableClassId = device.getDeviceType()
                .getDeviceProtocolPluggableClass()
                .map(HasId::getId)
                .orElse(-1L));
        map.put("yearOfCertification", (deviceInfo, device, uriInfo) -> deviceInfo.yearOfCertification = device.getYearOfCertification());
        map.put("batch", (deviceInfo, device, uriInfo) -> device.getBatch().ifPresent(batch -> deviceInfo.batch = batch.getName()));
        map.put("gatewayType", (deviceInfo, device, uriInfo) -> deviceInfo.gatewayType = device.getConfigurationGatewayType());
        map.put("isDirectlyAddressable", (deviceInfo, device, uriInfo) -> deviceInfo.isDirectlyAddressable = device.getDeviceConfiguration().isDirectlyAddressable());
        map.put("isGateway", (deviceInfo, device, uriInfo) -> deviceInfo.isGateway = device.getDeviceConfiguration().canActAsGateway());
        map.put("version", (deviceInfo, device, uriInfo) -> deviceInfo.version = device.getVersion());
        map.put("lifecycleState", (deviceInfo, device, uriInfo) -> deviceInfo.lifecycleState = device.getState().getName());
        map.put("actions", (deviceInfo, device, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().
                    path(DeviceLifecycleActionResource.class).
                    path(DeviceLifecycleActionResource.class, "getAction").
                    resolveTemplate("mrid", device.getmRID());
            deviceInfo.actions = deviceLifeCycleService.getExecutableActions(device).stream().
                    map(ExecutableAction::getAction).
                    filter(aa -> aa instanceof AuthorizedTransitionAction).
                    map(AuthorizedTransitionAction.class::cast).
                    map(action -> {
                        LinkInfo linkInfo = new LinkInfo();
                        linkInfo.id = action.getId();
                        linkInfo.link = Link.fromUriBuilder(uriBuilder).build(action.getId());
                        return linkInfo;
                    }).
                    collect(toList());
        });
        map.put("physicalGateway", (deviceInfo, device, uriInfo) -> {
            Optional<Device> physicalGateway = topologyService.getPhysicalGateway(device);
            if (physicalGateway.isPresent()) {
                deviceInfo.masterDevice = new DeviceInfo();
                deviceInfo.masterDevice.mRID = physicalGateway.get().getmRID();
                UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(DeviceResource.class).path("{mrid}").resolveTemplate("mrid", physicalGateway.get().getmRID());
                deviceInfo.masterDevice.link = Link.fromUriBuilder(uriBuilder).rel(Relation.REF_RELATION.rel()).title("gateway").build();
            }
        });
        map.put("slaveDevices", (deviceInfo, device, uriInfo) -> {
            if (GatewayType.LOCAL_AREA_NETWORK.equals(device.getConfigurationGatewayType())) {
                TopologyTimeline timeline = topologyService.getPhysicalTopologyTimelineAdditions(device, RECENTLY_ADDED_COUNT);
                deviceInfo.slaveDevices = timeline.getAllDevices().stream()
                        .sorted(new DeviceRecentlyAddedComparator(timeline))
                        .map(slave -> newSlaveDeviceLinkInfo(slave, uriInfo))
                        .collect(Collectors.toList());
            } else {
                deviceInfo.slaveDevices = topologyService.findPhysicalConnectedDevices(device).stream()
                        .map(slave -> newSlaveDeviceLinkInfo(slave, uriInfo))
                        .collect(Collectors.toList());
            }
        });
        map.put("connectionMethods", (deviceInfo, device, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().
                    path(ConnectionTaskResource.class).
                    path(ConnectionTaskResource.class, "getConnectionTask").
                    resolveTemplate("mrid", device.getmRID());
            deviceInfo.connectionMethods = device.getConnectionTasks().stream().
                    map(connectionTask -> {
                        LinkInfo linkInfo = new LinkInfo();
                        linkInfo.id = connectionTask.getId();
                        linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(Relation.REF_RELATION.rel()).title("Connection method").build(connectionTask.getId());
                        return linkInfo;
                    }).
                    collect(toList());
        });
        map.put("deviceConfiguration", (deviceInfo, device, uriInfo) -> {
            deviceInfo.deviceConfiguration = new DeviceConfigurationInfo();
            deviceInfo.deviceConfiguration.id = device.getDeviceConfiguration().getId();
            deviceInfo.deviceConfiguration.link = Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(DeviceConfigurationResource.class).path("{id}"))
                    .rel(Relation.REF_PARENT.rel())
                    .title("Device configuration")
                    .build(device.getDeviceType().getId(), device.getDeviceConfiguration().getId());
            deviceInfo.deviceConfiguration.deviceType = new LinkInfo();
            deviceInfo.deviceConfiguration.deviceType.id = device.getDeviceType().getId();
            deviceInfo.deviceConfiguration.deviceType.link = Link.fromUriBuilder(
                    uriInfo.getBaseUriBuilder().path(DeviceTypeResource.class).path("{id}"))
                    .rel(Relation.REF_PARENT.rel())
                    .title("Device type")
                    .build(device.getDeviceType().getId());
        });
        map.put("deviceMessages", (deviceInfo, device, uriInfo) -> {
            deviceInfo.deviceMessages = device.getMessages()
                    .stream()
                    .map(msg -> {
                        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                                .path(DeviceMessageResource.class)
                                .path(DeviceMessageResource.class, "getDeviceMessage")
                                .resolveTemplate("mrid", msg.getDevice().getmRID());
                        LinkInfo linkInfo = new LinkInfo();
                        linkInfo.id = msg.getId();
                        linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(Relation.REF_RELATION.rel()).title("Device message").build(msg.getId());
                        return linkInfo;
                    }).collect(toList());
        });
        map.put("communicationTaskExecutions", (deviceInfo, device, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().
                    path(ComTaskExecutionResource.class).
                    path(ComTaskExecutionResource.class, "getComTaskExecution").
                    resolveTemplate("mrid", device.getmRID());
            deviceInfo.communicationsTaskExecutions = device.getComTaskExecutions().stream()
                    .map(cte -> {
                        LinkInfo linkInfo = new LinkInfo();
                        linkInfo.id = cte.getId();
                        linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(Relation.REF_RELATION.rel()).title("Communication task execution").build(cte.getId());
                        return linkInfo;
                    })
                    .collect(toList());

            deviceInfo.deviceConfiguration = new DeviceConfigurationInfo();
            deviceInfo.deviceConfiguration.id = device.getDeviceConfiguration().getId();
            deviceInfo.deviceConfiguration.link = Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(DeviceConfigurationResource.class).path("{id}"))
                    .rel(Relation.REF_PARENT.rel())
                    .title("Device configuration")
                    .build(device.getDeviceType().getId(), device.getDeviceConfiguration().getId());
            deviceInfo.deviceConfiguration.deviceType = new LinkInfo();
            deviceInfo.deviceConfiguration.deviceType.id = device.getDeviceType().getId();
            deviceInfo.deviceConfiguration.deviceType.link = Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(DeviceTypeResource.class).path("{id}"))
                    .rel(Relation.REF_PARENT.rel())
                    .title("Device type")
                    .build(device.getDeviceType().getId());
        });
        return map;
    }

    private LinkInfo newSlaveDeviceLinkInfo(Device device, UriInfo uriInfo) {
        LinkInfo linkInfo = new LinkInfo();
        linkInfo.id = device.getId();
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().
                path(DeviceResource.class).
                path(DeviceResource.class, "getDevice").
                resolveTemplate("mrid", device.getmRID());
        linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(Relation.REF_RELATION.rel()).title("slave device").build();
        return linkInfo;
    }

    private class DeviceRecentlyAddedComparator implements Comparator<Device> {
        private TopologyTimeline timeline;

        public DeviceRecentlyAddedComparator(TopologyTimeline timeline) {
            this.timeline = timeline;
        }

        @Override
        public int compare(Device d1, Device d2) {
            Instant d1AddTime = this.timeline.mostRecentlyAddedOn(d1).orElse(Instant.MIN);
            Instant d2AddTime = this.timeline.mostRecentlyAddedOn(d2).orElse(Instant.MIN);
            return d2AddTime.compareTo(d1AddTime);
        }
    }
}
