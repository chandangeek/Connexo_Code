package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifierType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier} interface
 * that uses the unique MRID of the device
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (15:10)
 */
@XmlRootElement
public final class DeviceIdentifierByMRID implements DeviceIdentifier<Device> {

    private String mrid;
    private DeviceService deviceService;
    private Device device;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public DeviceIdentifierByMRID() {
    }

    public DeviceIdentifierByMRID(String mrid, DeviceService deviceService) {
        super();
        this.mrid = mrid;
        this.deviceService = deviceService;
    }

    @Override
    public Device findDevice() {
        // lazyload the device
        if (this.device == null) {
            this.device = this.deviceService.findByUniqueMrid(this.mrid).orElseThrow(() -> CanNotFindForIdentifier.device(this, MessageSeeds.CAN_NOT_FIND_FOR_DEVICE_IDENTIFIER));
        }
        return this.device;
    }

    @Override
    public String toString() {
        return "mrid " + this.mrid;
    }

    @Override
    public String getIdentifier() {
        return this.mrid;
    }

    @Override
    public DeviceIdentifierType getDeviceIdentifierType() {
        return DeviceIdentifierType.Other;
    }

    @Override
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeviceIdentifierByMRID)) {
            return false;
        }

        DeviceIdentifierByMRID that = (DeviceIdentifierByMRID) o;

        return !(mrid != null ? !mrid.equals(that.mrid) : that.mrid != null);

    }

    @Override
    public int hashCode() {
        return mrid != null ? mrid.hashCode() : 0;
    }
}