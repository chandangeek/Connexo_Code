package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.issue.share.service.IssueService;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.TopologyTimeline;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 4/30/15.
 */
public class DeviceInfoFactory {

    private static final int RECENTLY_ADDED_COUNT = 5;

    private final DeviceImportService deviceImportService;
    private final TopologyService topologyService;
    private final IssueService issueService;

    @Inject
    public DeviceInfoFactory(DeviceImportService deviceImportService, TopologyService topologyService, IssueService issueService) {
        this.deviceImportService = deviceImportService;
        this.topologyService = topologyService;
        this.issueService = issueService;
    }

    public DeviceInfo asHypermedia(Device device, UriInfo uriInfo, List<String> fields) {
        DeviceInfo DeviceInfo = new DeviceInfo();
        getSelectedFields(fields).stream().forEach(copier -> copier.copy(DeviceInfo, device, Optional.of(uriInfo)));
        DeviceInfo.link = Link.fromUriBuilder(getUriTemplate(uriInfo)).rel("self").title("self reference").build(device.getmRID());
        return DeviceInfo;
    }

    private List<PropertyCopier<DeviceInfo, Device>> getSelectedFields(Collection<String> fields) {
        Map<String, PropertyCopier<DeviceInfo, Device>> fieldSelectionMap = buildFieldSelectionMap();
        if (fields==null || fields.isEmpty()) {
            fields = fieldSelectionMap.keySet();
        }
        return fields.stream().filter(fieldSelectionMap::containsKey).map(fieldSelectionMap::get).collect(toList());
    }

    private UriBuilder getUriTemplate(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder().path(DeviceResource.class).path("{mrid}");
    }

    private Map<String, PropertyCopier<DeviceInfo,Device>> buildFieldSelectionMap() {
        Map<String, PropertyCopier<DeviceInfo, Device>> map = new HashMap<>();
        map.put("id", (deviceInfo, device, uriInfo) -> deviceInfo.id = device.getId());
        map.put("name", (deviceInfo, device, uriInfo) -> deviceInfo.name = device.getName());
        map.put("serialNumber", (deviceInfo, device, uriInfo) -> deviceInfo.serialNumber = device.getSerialNumber());
        map.put("deviceProtocolPluggeableClassId", (deviceInfo, device, uriInfo) -> deviceInfo.deviceProtocolPluggeableClassId = device.getDeviceType().getDeviceProtocolPluggableClass().getId());
        map.put("yearOfCertification", (deviceInfo, device, uriInfo) -> deviceInfo.yearOfCertification = device.getYearOfCertification());
        map.put("batch", (deviceInfo, device, uriInfo) -> deviceImportService.findBatch(device.getId()).ifPresent(batch -> deviceInfo.batch = batch.getName()));
        map.put("gatewayType", (deviceInfo, device, uriInfo) -> deviceInfo.gatewayType=device.getConfigurationGatewayType());
        map.put("nbrOfDataCollectionIssues", (deviceInfo, device, uriInfo) -> deviceInfo.nbrOfDataCollectionIssues = issueService.countOpenDataCollectionIssues(device.getmRID()));
        map.put("isDirectlyAddressed", (deviceInfo, device, uriInfo) -> deviceInfo.isDirectlyAddressed = device.getDeviceConfiguration().isDirectlyAddressable());
        map.put("isGateway", (deviceInfo, device, uriInfo) -> deviceInfo.isGateway = device.getDeviceConfiguration().canActAsGateway());
        map.put("version", (deviceInfo, device, uriInfo) -> deviceInfo.version = device.getVersion());

        map.put("physicalGateway", (deviceInfo, device, uriInfo) -> {
            Optional<Device> physicalGateway = topologyService.getPhysicalGateway(device);
            if (physicalGateway.isPresent()) {
                deviceInfo.masterDevice = new DeviceInfo();
                deviceInfo.masterDevice.mIRD = physicalGateway.get().getmRID();
                if (uriInfo.isPresent()) {
                    UriBuilder uriBuilder = uriInfo.get().getBaseUriBuilder().path(DeviceResource.class).path("{mrid}").resolveTemplate("mrid", physicalGateway.get().getmRID());
                    deviceInfo.masterDevice.link = Link.fromUriBuilder(uriBuilder).rel("related").title("gateway").build();
                }
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
                if (uriInfo.isPresent()) {
                    UriBuilder uriBuilder = uriInfo.get().getBaseUriBuilder().
                            path(LogBookResource.class).
                            path(LogBookResource.class, "getLogBook").
                            resolveTemplate("deviceId", device.getmRID()).
                            resolveTemplate("id", logBook.getId());
                    linkInfo.link = Link.fromUriBuilder(uriBuilder).rel("related").title("log book").build();
                }
            }
        });
        map.put("loadProfiles", (deviceInfo, device, uriInfo) -> {
            deviceInfo.loadProfiles = new ArrayList<>();
            for (LoadProfile loadProfile: device.getLoadProfiles()) {
                LinkInfo linkInfo = new LinkInfo();
                deviceInfo.loadProfiles.add(linkInfo);
                linkInfo.id = loadProfile.getId();
                if (uriInfo.isPresent()) {
                    UriBuilder uriBuilder = uriInfo.get().getBaseUriBuilder().
                            path(LoadProfileResource.class).
                            path(LoadProfileResource.class, "getLoadProfile").
                            resolveTemplate("deviceId", device.getmRID()).
                            resolveTemplate("id", loadProfile.getId());
                    linkInfo.link = Link.fromUriBuilder(uriBuilder).rel("related").title("load profile").build();
                }
            }
        });
        map.put("deviceConfiguration", (deviceInfo, device, uriInfo) -> {
            deviceInfo.deviceConfiguration = new DeviceConfigurationInfo();
            deviceInfo.deviceConfiguration.id = device.getDeviceConfiguration().getId();
            deviceInfo.deviceConfiguration.id = device.getDeviceConfiguration().getId();
            if (uriInfo.isPresent()) {
                deviceInfo.deviceConfiguration.link = Link.fromUriBuilder(uriInfo.get().getBaseUriBuilder().path(DeviceConfigurationResource.class).path("{id}")).rel("up").title("Device configuration").build(device.getDeviceType().getId(), device.getDeviceConfiguration().getId());
            }
            deviceInfo.deviceConfiguration.deviceType = new DeviceTypeInfo();
            deviceInfo.deviceConfiguration.deviceType.id = device.getDeviceType().getId();
            if (uriInfo.isPresent()) {
                deviceInfo.deviceConfiguration.deviceType.link = Link.fromUriBuilder(uriInfo.get().getBaseUriBuilder().path(DeviceTypeResource.class).path("{id}")).rel("up").title("Device type").build(device.getDeviceType().getId());
            }
        });
        return map;
    }

    private LinkInfo newSlaveDeviceLinkInfo(Device device, Optional<UriInfo> uriInfo) {
        LinkInfo linkInfo = new LinkInfo();
        linkInfo.id = device.getId();
        UriBuilder uriBuilder = uriInfo.get().getBaseUriBuilder().
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
            Optional<Instant> d1AddTime = this.timeline.mostRecentlyAddedOn(d1);
            Optional<Instant> d2AddTime = this.timeline.mostRecentlyAddedOn(d2);
            if (d1AddTime.isPresent() && d2AddTime.isPresent()){
                return d2AddTime.get().compareTo(d1AddTime.get());
            } else if (d2AddTime.isPresent()){
                return 1;
            } else if (d1AddTime.isPresent()){
                return -1;
            }
            return 0;
        }
    }
}
