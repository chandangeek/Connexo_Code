package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.issue.share.service.IssueService;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.TopologyTimeline;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 4/30/15.
 */
public class DeviceInfoFactory extends SelectableFieldFactory<DeviceInfo,Device> {

    private static final int RECENTLY_ADDED_COUNT = 5;

    private final DeviceImportService deviceImportService;
    private final TopologyService topologyService;
    private final IssueService issueService;
    private final DeviceLifeCycleService deviceLifeCycleService;

    @Inject
    public DeviceInfoFactory(DeviceImportService deviceImportService, TopologyService topologyService, IssueService issueService, DeviceLifeCycleService deviceLifeCycleService) {
        this.deviceImportService = deviceImportService;
        this.topologyService = topologyService;
        this.issueService = issueService;
        this.deviceLifeCycleService = deviceLifeCycleService;
    }

    public DeviceInfo asHypermedia(Device device, UriInfo uriInfo, Collection<String> fields) {
        DeviceInfo deviceInfo = new DeviceInfo();
        copySelectedFields(deviceInfo, device, uriInfo, fields);
        return deviceInfo;
    }

    public Set<String> getAvailableFields() {
        return buildFieldMap().keySet();
    }

    private UriBuilder getUriTemplate(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder().path(DeviceResource.class).path("{mrid}");
    }

    protected Map<String, PropertyCopier<DeviceInfo,Device>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceInfo, Device>> map = new HashMap<>();
        map.put("id", (deviceInfo, device, uriInfo) -> deviceInfo.id = device.getId());
        map.put("link", (deviceInfo, device, uriInfo) -> deviceInfo.link = Link.fromUriBuilder(getUriTemplate(uriInfo)).rel("self").title("self reference").build(device.getmRID()));
        map.put("name", (deviceInfo, device, uriInfo) -> deviceInfo.name = device.getName());
        map.put("mRID", (deviceInfo, device, uriInfo) -> deviceInfo.mRID = device.getmRID());
        map.put("serialNumber", (deviceInfo, device, uriInfo) -> deviceInfo.serialNumber = device.getSerialNumber());
        map.put("deviceProtocolPluggeableClassId", (deviceInfo, device, uriInfo) -> deviceInfo.deviceProtocolPluggeableClassId = device.getDeviceType().getDeviceProtocolPluggableClass().getId());
        map.put("yearOfCertification", (deviceInfo, device, uriInfo) -> deviceInfo.yearOfCertification = device.getYearOfCertification());
        map.put("batch", (deviceInfo, device, uriInfo) -> deviceImportService.findBatch(device.getId()).ifPresent(batch -> deviceInfo.batch = batch.getName()));
        map.put("gatewayType", (deviceInfo, device, uriInfo) -> deviceInfo.gatewayType=device.getConfigurationGatewayType());
        map.put("nbrOfDataCollectionIssues", (deviceInfo, device, uriInfo) -> deviceInfo.nbrOfDataCollectionIssues = issueService.countOpenDataCollectionIssues(device.getmRID()));
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
                deviceInfo.masterDevice.link = Link.fromUriBuilder(uriBuilder).rel("related").title("gateway").build();
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
        map.put("logBooks", (deviceInfo, device, uriInfo) -> {
            deviceInfo.logBooks = new ArrayList<>();
            for (LogBook logBook : device.getLogBooks()) {
                LinkInfo linkInfo = new LinkInfo();
                deviceInfo.logBooks.add(linkInfo);
                linkInfo.id = logBook.getId();
                UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().
                        path(LogBookResource.class).
                        path(LogBookResource.class, "getLogBook").
                        resolveTemplate("deviceTypeId", device.getDeviceType().getId()).
                        resolveTemplate("deviceConfigurationId", device.getDeviceConfiguration().getId()).
                        resolveTemplate("deviceId", device.getmRID()).
                        resolveTemplate("id", logBook.getId());
                linkInfo.link = Link.fromUriBuilder(uriBuilder).rel("related").title("log book").build();
            }
        });
        map.put("loadProfiles", (deviceInfo, device, uriInfo) -> {
            deviceInfo.loadProfiles = new ArrayList<>();
            for (LoadProfile loadProfile: device.getLoadProfiles()) {
                LinkInfo linkInfo = new LinkInfo();
                deviceInfo.loadProfiles.add(linkInfo);
                linkInfo.id = loadProfile.getId();
                UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().
                        path(LoadProfileResource.class).
                        path(LoadProfileResource.class, "getLoadProfile").
                        resolveTemplate("deviceTypeId", device.getDeviceType().getId()).
                        resolveTemplate("deviceConfigurationId", device.getDeviceConfiguration().getId()).
                        resolveTemplate("deviceId", device.getmRID()).
                        resolveTemplate("id", loadProfile.getId());
                linkInfo.link = Link.fromUriBuilder(uriBuilder).rel("related").title("load profile").build();
            }
        });
        map.put("deviceConfiguration", (deviceInfo, device, uriInfo) -> {
            deviceInfo.deviceConfiguration = new DeviceConfigurationInfo();
            deviceInfo.deviceConfiguration.id = device.getDeviceConfiguration().getId();
            deviceInfo.deviceConfiguration.link = Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(DeviceConfigurationResource.class).path("{id}")).rel("up").title("Device configuration").build(device.getDeviceType().getId(), device.getDeviceConfiguration().getId());
            deviceInfo.deviceConfiguration.deviceType = new DeviceTypeInfo();
            deviceInfo.deviceConfiguration.deviceType.id = device.getDeviceType().getId();
            deviceInfo.deviceConfiguration.deviceType.link = Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(DeviceTypeResource.class).path("{id}")).rel("up").title("Device type").build(device.getDeviceType().getId());
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
        linkInfo.link = Link.fromUriBuilder(uriBuilder).rel("related").title("slave device").build();
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
