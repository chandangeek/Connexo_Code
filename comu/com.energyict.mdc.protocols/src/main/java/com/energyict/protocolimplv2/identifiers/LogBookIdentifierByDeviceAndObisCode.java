package com.energyict.protocolimplv2.identifiers;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.protocol.api.device.BaseLogBook;
import com.energyict.mdc.protocol.api.device.LogBookFactory;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.common.ObisCode;

import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import java.util.List;

/**
 * Provides an implementation for the {@link LogBookIdentifier} interface
 * that uses a device's {@link DeviceIdentifier identifier} and the {@link ObisCode}
 * of the logbook to identify it.
 *
 * @author khe
 * @since 16/01/13 - 11:49
 */

public class LogBookIdentifierByDeviceAndObisCode implements LogBookIdentifier {

    private DeviceIdentifier deviceIdentifier;
    private ObisCode logBookObisCode;

    public LogBookIdentifierByDeviceAndObisCode(DeviceIdentifier deviceIdentifier, ObisCode logBookObisCode) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.logBookObisCode = logBookObisCode;
    }

    @Override
    public BaseLogBook getLogBook() {
        BaseLogBook logBook = this.getLogBookFactory().findLogBooksByDeviceAndDeviceObisCode(deviceIdentifier.findDevice(), logBookObisCode);
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
     * Check if the given {@link Object} is equal to this {@link LogBookIdentifierByDeviceAndObisCode}. <BR>
     * WARNING: if comparing with a {@link LogBookIdentifier} of another type (not of type {@link LogBookIdentifierByDeviceAndObisCode}),
     * this check will always return false, regardless of the fact they can both point to the same {@link com.energyict.mdc.protocol.api.device.BaseLogBook}!
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogBookIdentifierByDeviceAndObisCode otherIdentifier = (LogBookIdentifierByDeviceAndObisCode) o;
        return (this.deviceIdentifier.toString().equals(otherIdentifier.getDeviceIdentifier().toString()) && logBookObisCode.equals(otherIdentifier.getLogBookObisCode()));
    }

    @Override
    public int hashCode () {
        int result = deviceIdentifier.hashCode();
        result = 31 * result + logBookObisCode.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Identifier for logbook with obiscode '" + logBookObisCode.toString() + "' on " + deviceIdentifier.toString();
    }

    private LogBookFactory getLogBookFactory () {
        List<LogBookFactory> factories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(LogBookFactory.class);
        if (factories.isEmpty()) {
            throw new CommunicationException(MessageSeeds.MISSING_MODULE, LogBookFactory.class);
        }
        else {
            return factories.get(0);
        }
    }

}