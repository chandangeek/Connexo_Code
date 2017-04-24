package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.legacy.DeviceExtractor;
import com.energyict.obis.ObisCode;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.Optional;

/**
 * Provides an implementation for the {@link DeviceExtractor} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-17 (14:40)
 */
@Component(name = "com.energyict.mdc.device.data.upl.extractor", service = {DeviceExtractor.class}, immediate = true)
@SuppressWarnings("unused")
public class DeviceExtractorImpl implements DeviceExtractor {

    @Activate
    public void activate() {
        Services.deviceExtractor(this);
    }

    @Deactivate
    public void deactivate() {
        Services.deviceExtractor(null);
    }

    @Override
    public String serialNumber(com.energyict.mdc.upl.meterdata.Device device) {
        return ((Device) device).getSerialNumber();
    }

    @Override
    public String getDeviceProtocolPluggableClass(com.energyict.mdc.upl.meterdata.Device device) {
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = ((Device) device).getDeviceProtocolPluggableClass();
        if (deviceProtocolPluggableClass.isPresent()) {
            return deviceProtocolPluggableClass.get().getJavaClassName();
        } else {
            return null;
        }
    }

    @Override
    public <T> T protocolProperty(com.energyict.mdc.upl.meterdata.Device device, String propertyName, T defaultValue) {
        return this.protocolProperty((Device) defaultValue, propertyName, defaultValue);
    }

    private <T> T protocolProperty(Device device, String propertyName, T defaultValue) {
        return device.getDeviceProtocolProperties().getTypedProperty(propertyName, defaultValue);
    }

    @Override
    public Optional<com.energyict.mdc.upl.meterdata.Register> register(com.energyict.mdc.upl.meterdata.Device uplDevice, ObisCode obisCode) {
        Device device = (Device) uplDevice;
        return device
                .getRegisterWithDeviceObisCode(obisCode)
                .flatMap(Optional::of);
    }

}