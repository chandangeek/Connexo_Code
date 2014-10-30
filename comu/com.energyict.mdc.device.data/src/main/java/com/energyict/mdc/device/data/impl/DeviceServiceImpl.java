package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.services.DefaultFinder;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.*;
import com.energyict.mdc.device.data.impl.finders.DeviceFinder;
import com.energyict.mdc.device.data.impl.finders.ProtocolDialectPropertiesFinder;
import com.energyict.mdc.device.data.impl.finders.SecuritySetFinder;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.scheduling.model.ComSchedule;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.Interval;
import com.google.inject.Inject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

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
        finders.add(new SecuritySetFinder(this.deviceDataModelService.dataModel()));
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
        Condition condition = where("gateway").isEqualTo(device).and(where("interval").isEffective());
        List<PhysicalGatewayReference> physicalGatewayReferences = this.deviceDataModelService.dataModel().mapper(PhysicalGatewayReference.class).select(condition);
        if (!physicalGatewayReferences.isEmpty()) {
            List<Device> devices = new ArrayList<>();
            for (PhysicalGatewayReference physicalGatewayReference : physicalGatewayReferences) {
                devices.add(physicalGatewayReference.getOrigin());
            }
            return devices;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Device> findCommunicationReferencingDevicesFor(Device device) {
        Condition condition = where("gateway").isEqualTo(device).and(where("interval").isEffective());
        return this.findCommunicationReferencingDevicesFor(condition);
    }

    private List<Device> findCommunicationReferencingDevicesFor(Condition condition) {
        List<CommunicationGatewayReference> communicationGatewayReferences = this.deviceDataModelService.dataModel().mapper(CommunicationGatewayReference.class).select(condition);
        if (!communicationGatewayReferences.isEmpty()) {
            List<Device> devices = new ArrayList<>();
            for (CommunicationGatewayReference communicationGatewayReference : communicationGatewayReferences) {
                devices.add(communicationGatewayReference.getOrigin());
            }
            return devices;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Device> findCommunicationReferencingDevicesFor(Device device, Instant timestamp) {
        Condition condition = where("gateway").isEqualTo(device).and(where("interval").isEffective(timestamp));
        return this.findCommunicationReferencingDevicesFor(condition);
    }

    @Override
    public List<CommunicationTopologyEntry> findCommunicationReferencingDevicesFor(Device device, Interval interval) {
        Condition condition = where("gateway").isEqualTo(device).and(where("interval").isEffective(interval.toClosedRange()));
        List<CommunicationGatewayReference> communicationGatewayReferences = this.deviceDataModelService.dataModel().mapper(CommunicationGatewayReference.class).select(condition);
        if (!communicationGatewayReferences.isEmpty()) {
            List<CommunicationTopologyEntry> entries = new ArrayList<>(communicationGatewayReferences.size());
            for (CommunicationGatewayReference communicationGatewayReference : communicationGatewayReferences) {
                entries.add(
                        new SimpleCommunicationTopologyEntryImpl(
                                communicationGatewayReference.getOrigin(),
                                communicationGatewayReference.getInterval()));
            }
            return entries;
        } else {
            return Collections.emptyList();
        }
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

    @Override
    public List<CommunicationGatewayReference> getRecentlyAddedCommunicationReferencingDevices(Device device, int count) {
        List<CommunicationGatewayReference> references = DefaultFinder.of(CommunicationGatewayReference.class, where(CommunicationGatewayReferenceImpl.Field.GATEWAY.fieldName()).isEqualTo(device), this.deviceDataModelService.dataModel())
                .sorted(CommunicationGatewayReferenceImpl.Field.CREATION_TIME.fieldName(), false)
                .paged(0, count).find();
        if (count < references.size()){
            references = references.subList(0, count);
        }
        return references;
    }

    @Override
    public List<PhysicalGatewayReference> getRecentlyAddedPhysicalConnectedDevices(Device device, int count) {
        List<PhysicalGatewayReference> references = DefaultFinder.of(PhysicalGatewayReference.class, where(PhysicalGatewayReferenceImpl.Field.GATEWAY.fieldName()).isEqualTo(device), this.deviceDataModelService.dataModel())
                .sorted(PhysicalGatewayReferenceImpl.Field.CREATION_TIME.fieldName(), false)
                .paged(0, count).find();
        if (count < references.size()){
            references = references.subList(0, count);
        }
        return references;
    }

}