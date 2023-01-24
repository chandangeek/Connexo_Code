/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;

import javax.inject.Inject;

import java.util.List;

public class DeviceFinder {

    private final DeviceService deviceService;
    private final MeterConfigFaultMessageFactory faultMessageFactory;

    @Inject
    public DeviceFinder(DeviceService deviceService,
                        MeterConfigFaultMessageFactory faultMessageFactory) {
        this.deviceService = deviceService;
        this.faultMessageFactory = faultMessageFactory;
    }

    public Device findDevice(String mrid, String serialNumber, String deviceName) throws FaultMessage {
        Device device = (mrid != null && !mrid.isEmpty()) ? findDeviceByMRID(mrid, deviceName) :
                (serialNumber != null && !serialNumber.isEmpty()) ? findFirstDeviceBySerialNumber(serialNumber, deviceName) :
                        deviceService.findDeviceByName(deviceName)
                                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(deviceName, MessageSeeds.NO_DEVICE_WITH_NAME, deviceName));
        return device;
    }

    private Device findDeviceByMRID(String mrid, String deviceName) throws FaultMessage {
        return deviceService.findDeviceByMrid(mrid)
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(deviceName, MessageSeeds.NO_DEVICE_WITH_MRID, mrid));
    }

    private Device findFirstDeviceBySerialNumber(String serialNumber, String deviceName) throws FaultMessage {
        return deviceService.findDevicesBySerialNumber(serialNumber).stream().findFirst()
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(deviceName, MessageSeeds.NO_DEVICE_WITH_SERIAL_NUMBER, serialNumber));
    }

    public List<Device> findDevicesBySerialNumber(String serialNumber) {
        return deviceService.findDevicesBySerialNumber(serialNumber);
    }
}