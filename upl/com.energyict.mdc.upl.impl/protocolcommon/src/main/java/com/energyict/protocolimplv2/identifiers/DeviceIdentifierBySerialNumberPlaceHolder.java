package com.energyict.protocolimplv2.identifiers;

import com.energyict.cbo.NotFoundException;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.MeteringWarehouse;

import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.inbound.DeviceIdentifier} interface
 * that uses a PlaceHolder for a {@link com.energyict.mdw.core.Device}'s serial number to uniquely identify it.
 * <b>Be aware that the serialNumber is NOT a unique field in the database.
 * It is possible that multiple devices are found based on the provided SerialNumber.
 * In that case, a {@link com.energyict.cbo.NotFoundException} is throw</b>
 *
 * Copyrights EnergyICT
 * Date: 9/3/13
 * Time: 11:45 AM
 */
public class DeviceIdentifierBySerialNumberPlaceHolder implements DeviceIdentifier {

    private final SerialNumberPlaceHolder serialNumberPlaceHolder;
    private Device device;

    public DeviceIdentifierBySerialNumberPlaceHolder(SerialNumberPlaceHolder serialNumberPlaceHolder) {
        this.serialNumberPlaceHolder = serialNumberPlaceHolder;
    }

    @Override
    public Device findDevice() {
        // lazy load the device
        if(this.device == null){
            List<Device> devicesBySerialNumber = MeteringWarehouse.getCurrent().getDeviceFactory().findBySerialNumber(serialNumberPlaceHolder.getSerialNumber());
            if (devicesBySerialNumber.isEmpty()) {
                return null;
            }
            else {
                if (devicesBySerialNumber.size() > 1) {
                    throw new NotFoundException("More than one device found with serialnumber " + serialNumberPlaceHolder.getSerialNumber());
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
        DeviceIdentifierBySerialNumberPlaceHolder that = (DeviceIdentifierBySerialNumberPlaceHolder) o;
        return serialNumberPlaceHolder.equals(that.serialNumberPlaceHolder);
    }

    @Override
    public int hashCode () {
     return serialNumberPlaceHolder.getSerialNumber().hashCode();
    }

    @Override
    public String toString () {
        return "device with serial number " + serialNumberPlaceHolder.getSerialNumber();
    }


        @Override
    public String getIdentifier() {
        return serialNumberPlaceHolder.getSerialNumber();
    }
}
