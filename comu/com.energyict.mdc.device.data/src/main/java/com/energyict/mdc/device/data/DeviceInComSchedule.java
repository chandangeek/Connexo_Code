package com.energyict.mdc.device.data;

import com.energyict.mdc.scheduling.model.ComSchedule;

public interface DeviceInComSchedule {

    Device getDevice();

    void setDevice(Device device);

    ComSchedule getComSchedule();

    void setComSchedule(ComSchedule comSchedule);
}
