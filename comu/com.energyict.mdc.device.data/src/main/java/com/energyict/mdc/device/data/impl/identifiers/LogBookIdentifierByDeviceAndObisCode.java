package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifierType;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link LogBookIdentifier} interface
 * that uses a device's {@link DeviceIdentifier identifier} and the {@link ObisCode}
 * of the logbook to identify it.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-05 (13:24)
 */
@XmlRootElement
public class LogBookIdentifierByDeviceAndObisCode implements LogBookIdentifier {

    private final DeviceIdentifier deviceIdentifier;
    private final ObisCode logBookObisCode;

    public LogBookIdentifierByDeviceAndObisCode(DeviceIdentifier deviceIdentifier, ObisCode logBookObisCode) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.logBookObisCode = logBookObisCode;
    }

    @Override
    public List<Object> getParts() {
        return Arrays.asList((Object) getDeviceIdentifier(), getLogBookObisCode());
    }

    @Override
    public LogBook getLogBook() {
        Device device = (Device) this.deviceIdentifier.findDevice(); //Downcast to the Connexo Device
        return device.getLogBooks()
                .stream()
                .filter(lb -> lb.getDeviceObisCode().equals(this.logBookObisCode))
                .findFirst()
                .orElseThrow(() -> CanNotFindForIdentifier.logBook(this, MessageSeeds.CAN_NOT_FIND_FOR_LOGBOOK_IDENTIFIER));
    }

    @Override
    public ObisCode getLogBookObisCode() {
        return logBookObisCode;
    }

    @Override
    public LogBookIdentifierType getLogBookIdentifierType() {
        return LogBookIdentifierType.DeviceIdentifierAndObisCode;
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
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
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
    public int hashCode() {
        int result = this.deviceIdentifier.hashCode();
        result = 31 * result + this.logBookObisCode.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return MessageFormat.format("logbook having OBIS code {0} on device with deviceIdentifier ''{1}''", logBookObisCode, deviceIdentifier);
    }

}