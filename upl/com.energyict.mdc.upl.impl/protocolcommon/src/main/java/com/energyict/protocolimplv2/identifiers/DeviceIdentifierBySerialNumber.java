package com.energyict.protocolimplv2.identifiers;

import com.energyict.cbo.NotFoundException;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.MeteringWarehouse;

import java.util.List;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses an {@link com.energyict.mdw.core.Device}'s serial number to uniquely identify it.
 * <b>Be aware that the serialNumber is NOT a unique field in the database.
 * It is possible that multiple devices are found based on the provided SerialNumber.
 * In that case, a {@link com.energyict.cbo.NotFoundException} is throw</b>
 *
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 13:06
 */
public class DeviceIdentifierBySerialNumber implements DeviceIdentifier {

    private String serialNumber;
    private Device device;

    public DeviceIdentifierBySerialNumber(String serialNumber) {
        super();
        this.serialNumber = serialNumber;
    }

    @Override
    public Device findDevice () {
        //lazyload the device
        if(this.device == null){
            List<Device> devicesBySerialNumber = MeteringWarehouse.getCurrent().getDeviceFactory().findBySerialNumber(this.serialNumber);
            if (devicesBySerialNumber.isEmpty()) {
                return null;
            }
            else {
                if (devicesBySerialNumber.size() > 1) {
                    throw new NotFoundException("More than one device found with serialnumber " + this.serialNumber);
                }
                else {
                    this.device = devicesBySerialNumber.get(0);
                }
            }
        }
        return this.device;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceIdentifierBySerialNumber that = (DeviceIdentifierBySerialNumber) o;
        return serialNumber.equals(that.serialNumber);
    }

    @Override
    public int hashCode () {
        return serialNumber.hashCode();
    }

    @Override
    public String toString () {
        return "device with serial number " + this.serialNumber;
    }

    @Override
    public String getIdentifier() {
        return serialNumber;
    }
}
