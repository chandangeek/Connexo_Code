package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.services.DefaultFinder;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.ComTaskExecutionFields;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.DeviceTopology;
import com.energyict.mdc.device.data.TopologyTimeline;
import com.energyict.mdc.device.data.TopologyTimeslice;
import com.energyict.mdc.device.data.impl.finders.DeviceFinder;
import com.energyict.mdc.device.data.impl.finders.ProtocolDialectPropertiesFinder;
import com.energyict.mdc.device.data.impl.finders.SecuritySetFinder;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.scheduling.model.ComSchedule;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.google.common.collect.Range;
import com.google.inject.Inject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link DeviceService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-10 (16:27)
 */
public class DeviceServiceImpl implements ServerDeviceService {

    private final DeviceDataModelService deviceDataModelService;

    @Inject
    public DeviceServiceImpl(DeviceDataModelService deviceDataModelService) {
        super();
        this.deviceDataModelService = deviceDataModelService;
    }

    @Override
    public List<CanFindByLongPrimaryKey<? extends HasId>> finders() {
        List<CanFindByLongPrimaryKey<? extends HasId>> finders = new ArrayList<>();
        finders.add(new DeviceFinder(this.deviceDataModelService.dataModel()));
        finders.add(new ProtocolDialectPropertiesFinder(this.deviceDataModelService.dataModel()));
        finders.add(new SecuritySetFinder(this.deviceDataModelService.deviceConfigurationService()));
        return finders;
    }

    @Override
    public boolean hasDevices(DeviceConfiguration deviceConfiguration) {
        Condition condition = where(DeviceFields.DEVICECONFIGURATION.fieldName()).isEqualTo(deviceConfiguration);
        Finder<Device> page =
                DefaultFinder.
                        of(Device.class, condition, this.deviceDataModelService.dataModel()).
                        paged(0, 1);
        List<Device> allDevices = page.find();
        return !allDevices.isEmpty();
    }

    @Override
    public Device newDevice(DeviceConfiguration deviceConfiguration, String name, String mRID) {
        return this.deviceDataModelService.dataModel().getInstance(DeviceImpl.class).initialize(deviceConfiguration, name, mRID);
    }

    @Override
    public Device findDeviceById(long id) {
        return this.deviceDataModelService.dataModel().mapper(Device.class).getUnique("id", id).orElse(null);
    }

    @Override
    public Device findByUniqueMrid(String mrId) {
        return this.deviceDataModelService.dataModel().mapper(Device.class).getUnique(DeviceFields.MRID.fieldName(), mrId).orElse(null);
    }

    @Override
    public Device getPrototypeDeviceFor(DeviceConfiguration deviceConfiguration) {
        return null;
    }

    @Override
    public List<Device> findPhysicalConnectedDevicesFor(Device device) {
        Condition condition = this.getDevicesInTopologyCondition(device);
        List<PhysicalGatewayReference> physicalGatewayReferences = this.deviceDataModelService.dataModel().mapper(PhysicalGatewayReference.class).select(condition);
        return this.findUniqueReferencingDevices(physicalGatewayReferences);
    }

    @Override
    public List<Device> findCommunicationReferencingDevicesFor(Device device) {
        Condition condition = this.getDevicesInTopologyCondition(device);
        List<CommunicationGatewayReference> communicationGatewayReferences = this.deviceDataModelService.dataModel().mapper(CommunicationGatewayReference.class).select(condition);
        return this.findUniqueReferencingDevices(communicationGatewayReferences);
    }

    private Condition getDevicesInTopologyCondition(Device device) {
        return where("gateway").isEqualTo(device).and(where("interval").isEffective());
    }

    private List<Device> findUniqueReferencingDevices (List<? extends GatewayReference> gatewayReferences) {
        Map<Long, Device> devicesById = new HashMap<>();
        for (GatewayReference reference : gatewayReferences) {
            devicesById.put(reference.getOrigin().getId(), reference.getOrigin());
        }
        return new ArrayList<>(devicesById.values());
    }

    @Override
    public List<Device> findCommunicationReferencingDevicesFor(Device device, Instant timestamp) {
        Condition condition = where("gateway").isEqualTo(device).and(where("interval").isEffective(timestamp));
        List<CommunicationGatewayReference> communicationGatewayReferences = this.deviceDataModelService.dataModel().mapper(CommunicationGatewayReference.class).select(condition);
        return this.findUniqueReferencingDevices(communicationGatewayReferences);
    }

    private List<ServerTopologyTimeslice> findCommunicationReferencingDevicesFor(Device device, Range<Instant> period) {
        Condition condition = this.getDevicesInTopologyInIntervalCondition(device, period);
        List<CommunicationGatewayReference> communicationGatewayReferences = this.deviceDataModelService.dataModel().mapper(CommunicationGatewayReference.class).select(condition);
        return this.toTopologyTimeslices(communicationGatewayReferences);
    }

    private List<ServerTopologyTimeslice> findRecentCommunicationReferencingDevicesFor(Device device, int maxRecentCount) {
        Condition condition = this.getDevicesInTopologyCondition(device);
        Order[] ordering = new Order[]{Order.descending("interval.start")};
        List<CommunicationGatewayReference> communicationGatewayReferences =
                this.deviceDataModelService.dataModel()
                    .query(CommunicationGatewayReference.class)
                    .select(condition, ordering, false, new String[0], 1, maxRecentCount);
        return this.toTopologyTimeslices(communicationGatewayReferences);
    }

    private List<ServerTopologyTimeslice> findPhysicallyReferencingDevicesFor(Device device, Range<Instant> period) {
        Condition condition = this.getDevicesInTopologyInIntervalCondition(device, period);
        List<PhysicalGatewayReference> gatewayReferences = this.deviceDataModelService.dataModel().mapper(PhysicalGatewayReference.class).select(condition);
        return this.toTopologyTimeslices(gatewayReferences);
    }

    private List<ServerTopologyTimeslice> findRecentPhysicallyReferencingDevicesFor(Device device, int maxRecentCount) {
        Condition condition = this.getDevicesInTopologyCondition(device);
        Order[] ordering = new Order[]{Order.descending("interval.start")};
        List<PhysicalGatewayReference> gatewayReferences =
                this.deviceDataModelService.dataModel()
                        .query(PhysicalGatewayReference.class)
                        .select(condition, ordering, false, new String[0], 1, maxRecentCount);
        return this.toTopologyTimeslices(gatewayReferences);
    }

    private Condition getDevicesInTopologyInIntervalCondition(Device device, Range<Instant> period) {
        return where("gateway").isEqualTo(device).and(where("interval").isEffective(period));
    }

    private List<ServerTopologyTimeslice> toTopologyTimeslices(List<? extends GatewayReference> gatewayReferences) {
        return gatewayReferences.stream().map(this::toTopologyTimeslice).collect(Collectors.toList());
    }

    private ServerTopologyTimeslice toTopologyTimeslice(GatewayReference r) {
        return new SimpleTopologyTimesliceImpl(r.getOrigin(), r.getInterval());
    }

    @Override
    public DeviceTopology buildCommunicationTopology(Device root, Range<Instant> period) {
        return this.buildTopology(root, period, this::findCommunicationReferencingDevicesFor);
    }

    @Override
    public DeviceTopology buildPhysicalTopology(Device root, Range<Instant> period) {
        return this.buildTopology(root, period, this::findPhysicallyReferencingDevicesFor);
    }

    private DeviceTopologyImpl buildTopology(Device root, Range<Instant> period, FirstLevelTopologyTimeslicer firstLevelTopologyTimeslicer) {
        List<ServerTopologyTimeslice> firstLevelTopologyEntries = firstLevelTopologyTimeslicer.firstLevelTopologyTimeslices(root, period);
        Range<Instant> spanningInterval = this.intervalSpanOf(firstLevelTopologyEntries);
        DeviceTopologyImpl topology = new DeviceTopologyImpl(root, period.intersection(spanningInterval));
        for (TopologyTimeslice firstLevelTopologyEntry : firstLevelTopologyEntries) {
            for (Device device : firstLevelTopologyEntry.getDevices()) {
                topology.addChild(this.buildTopology(device, period.intersection(firstLevelTopologyEntry.getPeriod()), firstLevelTopologyTimeslicer));
            }
        }
        return topology;
    }

    private Range<Instant> intervalSpanOf(List<ServerTopologyTimeslice> topologyEntries) {
        List<Instant> timeslicePeriodEndPoints =
                topologyEntries
                        .stream()
                        .map(TopologyTimeslice::getPeriod)
                        .flatMap(this::periodEndPoints)
                        .collect(Collectors.toList());
        if (timeslicePeriodEndPoints.isEmpty()) {
            return Range.all();
        }
        else {
            return Range.encloseAll(timeslicePeriodEndPoints);
        }
    }

    private Stream<Instant> periodEndPoints (Range<Instant> period) {
        return Stream.of(this.lowerEndpoint(period), this.upperEndpoint(period));
    }

    private Instant lowerEndpoint(Range<Instant> period) {
        if (period.hasLowerBound()) {
            return period.lowerEndpoint();
        }
        else {
            return Instant.MIN;
        }
    }

    private Instant upperEndpoint(Range<Instant> period) {
        if (period.hasUpperBound()) {
            return period.upperEndpoint();
        }
        else {
            return Instant.MAX;
        }
    }

    @Override
    public TopologyTimeline getCommunicationTopologyTimeline(Device device) {
        return TopologyTimelineImpl.merge(this.findCommunicationReferencingDevicesFor(device, Range.all()));
    }

    @Override
    public TopologyTimeline getCommunicationTopologyTimelineAdditions(Device device, int maximumNumberOfAdditions) {
        return TopologyTimelineImpl.merge(this.findRecentCommunicationReferencingDevicesFor(device, maximumNumberOfAdditions));
    }

    @Override
    public TopologyTimeline getPysicalTopologyTimeline(Device device) {
        return TopologyTimelineImpl.merge(this.findPhysicallyReferencingDevicesFor(device, Range.all()));
    }

    @Override
    public TopologyTimeline getPhysicalTopologyTimelineAdditions(Device device, int maximumNumberOfAdditions) {
        return TopologyTimelineImpl.merge(this.findRecentPhysicallyReferencingDevicesFor(device, maximumNumberOfAdditions));
    }

    @Override
    public List<Device> findDevicesBySerialNumber(String serialNumber) {
        return this.deviceDataModelService.dataModel().mapper(Device.class).find("serialNumber", serialNumber);
    }

    @Override
    public List<Device> findAllDevices() {
        return this.deviceDataModelService.dataModel().mapper(Device.class).find();
    }

    @Override
    public Finder<Device> findAllDevices(Condition condition) {
        return DefaultFinder.of(Device.class, condition, this.deviceDataModelService.dataModel(), DeviceConfiguration.class, DeviceType.class);
    }

    @Override
    public List<Device> findDevicesByTimeZone(TimeZone timeZone) {
        return this.deviceDataModelService.dataModel().mapper(Device.class).find("timeZoneId", timeZone.getID());
    }

    @Override
    public InfoType newInfoType(String name) {
        return this.deviceDataModelService.dataModel().getInstance(InfoTypeImpl.class).initialize(name);
    }

    @Override
    public InfoType findInfoType(String name) {
        return this.deviceDataModelService.dataModel().mapper(InfoType.class).getUnique("name", name).orElse(null);
    }

    @Override
    public InfoType findInfoTypeById(long infoTypeId) {
        return this.deviceDataModelService.dataModel().mapper(InfoType.class).getUnique("id", infoTypeId).orElse(null);
    }

    @Override
    public boolean isLinkedToDevices(ComSchedule comSchedule) {
        Condition condition = where(ComTaskExecutionFields.COM_SCHEDULE.fieldName()).isEqualTo(comSchedule).and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        List<ScheduledComTaskExecution> scheduledComTaskExecutions = this.deviceDataModelService.dataModel().query(ScheduledComTaskExecution.class).
                select(condition, new Order[0], false, new String[0], 1, 1);
        return !scheduledComTaskExecutions.isEmpty();
    }

    @Override
    public Finder<Device> findDevicesByDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        return DefaultFinder.of(Device.class, where("deviceConfiguration").isEqualTo(deviceConfiguration), this.deviceDataModelService.dataModel()).defaultSortColumn("lower(name)");
    }

    private interface FirstLevelTopologyTimeslicer {
        List<ServerTopologyTimeslice> firstLevelTopologyTimeslices(Device device, Range<Instant> period);
    }

}