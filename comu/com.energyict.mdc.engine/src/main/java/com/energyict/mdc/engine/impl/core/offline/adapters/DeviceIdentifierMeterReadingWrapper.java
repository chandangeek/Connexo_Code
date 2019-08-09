package com.energyict.mdc.engine.impl.core.offline.adapters;

import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.identifiers.DeviceIdentifierForAlreadyKnownDevice;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

public class DeviceIdentifierMeterReadingWrapper {

    private MeterReading meterReading;
    private DeviceIdentifier deviceIdentifier;

    @XmlElements(
            {@XmlElement(type = DeviceIdentifierById.class), @XmlElement(type = DeviceIdentifierBySerialNumber.class), @XmlElement(type = DeviceIdentifierForAlreadyKnownDevice.class)}
    )
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @XmlElement(type = MeterReadingImpl.class)
    public MeterReading getMeterReading() {
        return meterReading;
    }

    public void setMeterReading(MeterReading meterReading) {
        this.meterReading = meterReading;
    }

    public void setDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public boolean equals(Object obj) {
        return getDeviceIdentifier().equals(((DeviceIdentifierMeterReadingWrapper)obj).getDeviceIdentifier());
    }

    @Override
    public int hashCode() {
        return getDeviceIdentifier().hashCode();
    }
}
