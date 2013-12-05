package com.energyict.protocolimplv2.identifiers;

import com.energyict.cbo.NotFoundException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.meterdata.identifiers.CanFindDevice;
import com.energyict.mdc.meterdata.identifiers.CanFindLogBook;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdw.core.LogBook;
import com.energyict.mdw.core.LogBookFactoryProvider;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier} interface
 * that uses a device's {@link DeviceIdentifier} and the {@link ObisCode} of the logbook to identify it
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 16:12
 */
public class LogBookIdentifierByObisCodeAndDevice implements CanFindLogBook {

    private CanFindDevice deviceIdentifier;
    private ObisCode logBookObisCode;

    public LogBookIdentifierByObisCodeAndDevice(CanFindDevice deviceIdentifier, ObisCode logBookObisCode) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.logBookObisCode = logBookObisCode;
    }

    @Override
    public LogBook getLogBook() {
        LogBook logBook = LogBookFactoryProvider.instance.get().getLogBookFactory().findByDeviceAndDeviceObisCode(deviceIdentifier.findDevice(), logBookObisCode);
        if (logBook == null) {
            throw new NotFoundException("No logbook found with obiscode '" + logBookObisCode.toString() + "'for device with serial number '" + deviceIdentifier.toString() + "'");
        } else {
            return logBook;
        }
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public ObisCode getLogBookObisCode() {
        return logBookObisCode;
    }

    /**
     * Check if the given {@link Object} is equal to this {@link LogBookIdentifierByObisCodeAndDevice}. <BR>
     * WARNING: if comparing with a {@link com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier} of another type (not of type {@link LogBookIdentifierByObisCodeAndDevice}),
     * this check will always return false, regardless of the fact they can both point to the same {@link com.energyict.mdw.core.LogBook}!
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogBookIdentifierByObisCodeAndDevice otherIdentifier = (LogBookIdentifierByObisCodeAndDevice) o;
        return (this.deviceIdentifier.toString().equals(otherIdentifier.getDeviceIdentifier().toString()) && logBookObisCode.equals(otherIdentifier.getLogBookObisCode()));
    }

    @Override
    public int hashCode() {
        int result = deviceIdentifier.hashCode();
        result = 31 * result + logBookObisCode.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Identifier for logbook with obiscode '" + logBookObisCode.toString() + "' on " + deviceIdentifier.toString();
    }

}