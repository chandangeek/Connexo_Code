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
import com.energyict.mdc.device.data.DeviceProtocolProperty;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.finders.DeviceFinder;
import com.energyict.mdc.device.data.impl.finders.ProtocolDialectPropertiesFinder;
import com.energyict.mdc.device.data.impl.finders.SecuritySetFinder;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.scheduling.model.ComSchedule;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

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
    public List<Device> findDevicesBySerialNumber(String serialNumber) {
        return this.deviceDataModelService.dataModel().mapper(Device.class).find("serialNumber", serialNumber);
    }

    @Override
    public Finder<Device> findAllDevices(Condition condition) {
        return DefaultFinder.of(Device.class, condition, this.deviceDataModelService.dataModel(), DeviceConfiguration.class, DeviceType.class);
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
    public Finder<Device> findDevicesByPropertySpecValue(String propertySpecName, String propertySpecValue) {
        Condition condition = where("propertySpec").isEqualTo(propertySpecName).and(where("propertyValue").isEqualTo(propertySpecValue));
        return DefaultFinder.of(Device.class, condition, this.deviceDataModelService.dataModel(), DeviceProtocolProperty.class);
    }
}