/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.getmeterconfig;

import ch.iec.tc57._2011.getmeterconfig.FaultMessage;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import java.util.Optional;

public class DeviceBuilder {

    private final DeviceService deviceService;
    private final GetMeterConfigFaultMessageFactory faultMessageFactory;

    @Inject
    public DeviceBuilder(DeviceService deviceService, GetMeterConfigFaultMessageFactory faultMessageFactory) {
        this.deviceService = deviceService;
        this.faultMessageFactory = faultMessageFactory;
    }

    public Device findDevice(Optional<String> mrid, String deviceName) throws FaultMessage {
        Device device = mrid.isPresent() ? findDeviceByMRID(mrid.get(), deviceName) :
                deviceService.findDeviceByName(deviceName)
                        .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(deviceName, MessageSeeds.NO_DEVICE_WITH_NAME, deviceName));
        return device;
    }

    private Device findDeviceByMRID(String mrid, String deviceName) throws FaultMessage {
        return deviceService.findDeviceByMrid(mrid)
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(deviceName, MessageSeeds.NO_DEVICE_WITH_MRID, mrid));
    }
}