package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceInComSchedule;
import com.energyict.mdc.scheduling.model.ComSchedule;

public class DeviceInComScheduleImpl implements DeviceInComSchedule {

    enum Fields {
        DEVICE_REFERENCE("deviceReference"),
        COM_SCHEDULE_REFERENCE("comScheduleReference");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private Reference<Device> deviceReference = ValueReference.absent();
    private Reference<ComSchedule> comScheduleReference = ValueReference.absent();

    public DeviceInComScheduleImpl(Device device, ComSchedule comSchedule) {
        this.deviceReference.set(device);
        this.comScheduleReference.set(comSchedule);
    }

    @Override
    public Device getDevice() {
        return deviceReference.get();
    }

    @Override
    public void setDevice(Device device) {
        this.deviceReference.set(device);
    }

    @Override
    public ComSchedule getComSchedule() {
        return comScheduleReference.get();
    }

    @Override
    public void setComSchedule(ComSchedule comSchedule) {
        this.comScheduleReference.set(comSchedule);
    }
}
