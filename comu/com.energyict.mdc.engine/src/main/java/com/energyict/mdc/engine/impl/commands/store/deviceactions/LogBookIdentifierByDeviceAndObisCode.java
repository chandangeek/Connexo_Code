package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.BaseLogBook;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

/**
 * Provides an implementation for the {@link LogBookIdentifier} interface
 * that uses a device's {@link DeviceIdentifier identifier} and the {@link ObisCode}
 * of the logbook to identify it.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-05 (13:24)
 */
public class LogBookIdentifierByDeviceAndObisCode implements LogBookIdentifier {

    private final DeviceIdentifier deviceIdentifier;
    private final ObisCode logBookObisCode;

    public LogBookIdentifierByDeviceAndObisCode(DeviceIdentifier deviceIdentifier, ObisCode logBookObisCode) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.logBookObisCode = logBookObisCode;
    }

    @Override
    public BaseLogBook getLogBook() {
        Device device = (Device) this.deviceIdentifier.findDevice();
        return device.getLogBooks()
                .stream()
                .filter(lb -> lb.getDeviceObisCode().equals(this.logBookObisCode))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No logbook found with obiscode '" + this.logBookObisCode.toString() + "'for device identified by '" + this.deviceIdentifier.toString() + "'"));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogBookIdentifierByDeviceAndObisCode otherIdentifier = (LogBookIdentifierByDeviceAndObisCode) o;
        return (this.deviceIdentifier.toString().equals(otherIdentifier.deviceIdentifier.toString())
            && this.logBookObisCode.equals(otherIdentifier.logBookObisCode));
    }

    @Override
    public int hashCode () {
        int result = this.deviceIdentifier.hashCode();
        result = 31 * result + this.logBookObisCode.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Identifier for logbook with obiscode '" + logBookObisCode.toString() + "' on " + deviceIdentifier.toString();
    }

}