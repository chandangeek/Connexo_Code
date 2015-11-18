package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.TopologyTimeline;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 4/30/15.
 */
public class DeviceInfoFactory extends SelectableFieldFactory<DeviceInfo,Device> {

    private static final int RECENTLY_ADDED_COUNT = 5;

    private final BatchService batchService;
    private final TopologyService topologyService;
    private final DeviceLifeCycleService deviceLifeCycleService;

    @Inject
    public DeviceInfoFactory(BatchService batchService, TopologyService topologyService, DeviceLifeCycleService deviceLifeCycleService) {
        this.batchService = batchService;
        this.topologyService = topologyService;
        this.deviceLifeCycleService = deviceLifeCycleService;
    }

    public DeviceInfo asHypermedia(Device device, UriInfo uriInfo, Collection<String> fields) {
        DeviceInfo deviceInfo = new DeviceInfo();
        copySelectedFields(deviceInfo, device, uriInfo, fields);
        return deviceInfo;
    }

    private UriBuilder getUriTemplate(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder().path(DeviceResource.class).path("{mrid}");
    }

    protected Map<String, PropertyCopier<DeviceInfo,Device>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceInfo, Device>> map = new HashMap<>();
        map.put("id", (deviceInfo, device, uriInfo) -> deviceInfo.id = device.getId());
        map.put("link", (deviceInfo, device, uriInfo) -> deviceInfo.link = Link.fromUriBuilder(getUriTemplate(uriInfo)).rel(LinkInfo.REF_SELF).title("self reference").build(device.getmRID()));
        map.put("name", (deviceInfo, device, uriInfo) -> deviceInfo.name = device.getName());
        map.put("mRID", (deviceInfo, device, uriInfo) -> deviceInfo.mRID = device.getmRID());
        map.put("serialNumber", (deviceInfo, device, uriInfo) -> deviceInfo.serialNumber = device.getSerialNumber());
        map.put("deviceProtocolPluggeableClassId", (deviceInfo, device, uriInfo) -> deviceInfo.deviceProtocolPluggeableClassId = device.getDeviceType().getDeviceProtocolPluggableClass().getId());
        map.put("yearOfCertification", (deviceInfo, device, uriInfo) -> deviceInfo.yearOfCertification = device.getYearOfCertification());
        map.put("batch", (deviceInfo, device, uriInfo) -> batchService.findBatch(device).ifPresent(batch -> deviceInfo.batch = batch.getName()));
        map.put("gatewayType", (deviceInfo, device, uriInfo) -> deviceInfo.gatewayType=device.getConfigurationGatewayType());
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
        } );
        map.put("physicalGateway", (deviceInfo, device, uriInfo) -> {
            Optional<Device> physicalGateway = topologyService.getPhysicalGateway(device);
            if (physicalGateway.isPresent()) {
                deviceInfo.masterDevice = new DeviceInfo();
                deviceInfo.masterDevice.mRID = physicalGateway.get().getmRID();
                UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(DeviceResource.class).path("{mrid}").resolveTemplate("mrid", physicalGateway.get().getmRID());
                deviceInfo.masterDevice.link = Link.fromUriBuilder(uriBuilder).rel(LinkInfo.REF_RELATION).title("gateway").build();
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
                        linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(LinkInfo.REF_RELATION).title("Connection method").build(connectionTask.getId());
                        return linkInfo;
                    }).
                    collect(toList());
        });
        map.put("deviceConfiguration", (deviceInfo, device, uriInfo) -> {
            deviceInfo.deviceConfiguration = new DeviceConfigurationInfo();
            deviceInfo.deviceConfiguration.id = device.getDeviceConfiguration().getId();
            deviceInfo.deviceConfiguration.link = Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(DeviceConfigurationResource.class).path("{id}")).rel(LinkInfo.REF_PARENT).title("Device configuration").build(device.getDeviceType().getId(), device.getDeviceConfiguration().getId());
            deviceInfo.deviceConfiguration.deviceType = new LinkInfo();
            deviceInfo.deviceConfiguration.deviceType.id = device.getDeviceType().getId();
            deviceInfo.deviceConfiguration.deviceType.link = Link.fromUriBuilder(
                    uriInfo.getBaseUriBuilder().path(DeviceTypeResource.class).path("{id}"))
                    .rel(LinkInfo.REF_PARENT)
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
                        linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(LinkInfo.REF_RELATION).title("Device message").build(msg.getId());
                        return linkInfo;
                    }).collect(toList());
        });
        map.put("communicationTaskExecutions", (deviceInfo, device, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().
                    path(ComTaskExecutionResource.class).
                    path(ComTaskExecutionResource.class, "getComTaskExecution").
                    resolveTemplate("mrid", device.getmRID());
            deviceInfo.communicationsTaskExecutions = device.getComTaskExecutions().stream()
                    .map(cte->{
                        LinkInfo linkInfo = new LinkInfo();
                        linkInfo.id = cte.getId();
                        linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(LinkInfo.REF_RELATION).title("Communication task execution").build(cte.getId());
                        return linkInfo;
                    })
                    .collect(toList());

            deviceInfo.deviceConfiguration = new DeviceConfigurationInfo();
            deviceInfo.deviceConfiguration.id = device.getDeviceConfiguration().getId();
            deviceInfo.deviceConfiguration.link = Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(DeviceConfigurationResource.class).path("{id}")).rel(LinkInfo.REF_PARENT).title("Device configuration").build(device.getDeviceType().getId(), device.getDeviceConfiguration().getId());
            deviceInfo.deviceConfiguration.deviceType = new LinkInfo();
            deviceInfo.deviceConfiguration.deviceType.id = device.getDeviceType().getId();
            deviceInfo.deviceConfiguration.deviceType.link = Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(DeviceTypeResource.class).path("{id}")).rel(LinkInfo.REF_PARENT).title("Device type").build(device.getDeviceType().getId());
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
        linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(LinkInfo.REF_RELATION).title("slave device").build();
        return linkInfo;
    }

    private class DeviceRecentlyAddedComparator implements Comparator<Device>{
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
