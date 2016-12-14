package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides an implementation for the {@link com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier} interface
 * that uses the unique MRID of the device
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (15:10)
 */
@XmlRootElement
public final class DeviceIdentifierByMRID implements DeviceIdentifier {

    private String mrid;
    private DeviceService deviceService;
    private Device device;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public DeviceIdentifierByMRID() {
    }

    public DeviceIdentifierByMRID(String mrid, DeviceService deviceService) {
        this();
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
        return "device having MRID " + this.mrid;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
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

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "mRID";
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "databaseValue": {
                    return mrid;
                }
                default: {
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
                }
            }
        }

    }

}