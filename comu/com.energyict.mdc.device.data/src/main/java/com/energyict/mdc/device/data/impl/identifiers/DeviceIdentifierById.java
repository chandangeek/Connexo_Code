package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses a {@link com.energyict.mdc.upl.meterdata.Device}'s database identifier.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (15:10)
 */
@XmlRootElement
public final class DeviceIdentifierById implements DeviceIdentifier {

    private long id;
    private DeviceService deviceService;
    private Device device;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public DeviceIdentifierById() {
    }

    public DeviceIdentifierById(long id, DeviceService deviceService) {
        this();
        this.id = id;
        this.deviceService = deviceService;
    }

    // used for reflection
    public DeviceIdentifierById(String id, DeviceService deviceService) {
        super();
        this.deviceService = deviceService;
        this.id = Long.parseLong(id);
    }

    @Override
    public Device findDevice() {
        // lazyload the device
        if (this.device == null) {
            this.device = this.deviceService.findDeviceById(this.id).orElseThrow(() -> CanNotFindForIdentifier.device(this, MessageSeeds.CAN_NOT_FIND_FOR_DEVICE_IDENTIFIER));
        }
        return this.device;
    }

    @Override
    public String toString() {
        return "device having id " + this.id;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
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
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        DeviceIdentifierById that = (DeviceIdentifierById) other;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(this.id).hashCode();
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "DatabaseId";
        }

        @Override
        public Object getValue(String role) {
            if ("databaseValue".equals(role)) {
                return id;
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }
    }

}