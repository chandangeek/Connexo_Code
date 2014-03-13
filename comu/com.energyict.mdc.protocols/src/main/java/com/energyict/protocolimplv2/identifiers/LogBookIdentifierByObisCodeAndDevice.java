package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.LogBook;
import com.energyict.mdc.protocol.api.device.LogBookFactory;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;

import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier} interface
 * that uses a device's {@link DeviceIdentifier} and the {@link ObisCode} of the logbook to identify it
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 16:12
 */
public class LogBookIdentifierByObisCodeAndDevice implements LogBookIdentifier {

    private DeviceIdentifier deviceIdentifier;
    private ObisCode logBookObisCode;

    public LogBookIdentifierByObisCodeAndDevice(DeviceIdentifier deviceIdentifier, ObisCode logBookObisCode) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.logBookObisCode = logBookObisCode;
    }

    @Override
    public LogBook getLogBook() {
        List<LogBookFactory> logBookFactories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(LogBookFactory.class);
        BaseDevice device = deviceIdentifier.findDevice();
        for (LogBookFactory factory : logBookFactories) {
            LogBook logBook = factory.findLogBooksByDeviceAndDeviceObisCode(device, logBookObisCode);
            if (logBook != null) {
                return logBook;
            }
        }
        throw new NotFoundException("No logbook found with obiscode '" + logBookObisCode.toString() + "'for device with serial number '" + deviceIdentifier.toString() + "'");
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public ObisCode getLogBookObisCode() {
        return logBookObisCode;
    }

    /**
     * Check if the given {@link Object} is equal to this {@link LogBookIdentifierByObisCodeAndDevice}. <BR>
     * WARNING: if comparing with a {@link LogBookIdentifier} of another type (not of type {@link LogBookIdentifierByObisCodeAndDevice}),
     * this check will always return false, regardless of the fact they can both point to the same {@link LogBook}!
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