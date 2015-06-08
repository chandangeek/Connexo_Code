package com.energyict.protocolimplv2.identifiers;

import com.energyict.cbo.NotFoundException;
import com.energyict.util.Collections;
import com.energyict.mdc.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.meterdata.identifiers.LogBookIdentifierType;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdw.core.LogBook;
import com.energyict.mdw.core.LogBookFactory;
import com.energyict.mdw.core.LogBookFactoryProvider;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.meterdata.identifiers.LogBookIdentifier} interface
 * that uses a device's {@link com.energyict.mdc.protocol.inbound.DeviceIdentifier} and the {@link com.energyict.obis.ObisCode} of the logbook to identify it
 * <p/>
 * Copyrights EnergyICT
 * Date: 13/05/13
 * Time: 16:12
 */
@XmlRootElement
public class LogBookIdentifierByObisCodeAndDevice implements LogBookIdentifier {

    private DeviceIdentifier deviceIdentifier;
    private ObisCode logBookObisCode;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private LogBookIdentifierByObisCodeAndDevice() {
    }

    public LogBookIdentifierByObisCodeAndDevice(DeviceIdentifier deviceIdentifier, ObisCode logBookObisCode) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.logBookObisCode = logBookObisCode;
    }

    @Override
    public LogBook getLogBook() {
        LogBook logBook = getLogBookFactory().findByDeviceAndDeviceObisCode(deviceIdentifier.findDevice(), logBookObisCode);
        if (logBook == null) {
            throw new NotFoundException("No logbook found with obiscode '" + logBookObisCode.toString() + "'for device with serial number '" + deviceIdentifier.toString() + "'");
        } else {
            return logBook;
        }
    }

    @XmlAttribute
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @XmlAttribute
    public ObisCode getLogBookObisCode() {
        return logBookObisCode;
    }

    @Override
    public LogBookIdentifierType getLogBookIdentifierType() {
        return LogBookIdentifierType.DeviceIdentifierAndObisCode;
    }

    @Override
    public List<Object> getIdentifier() {
        return Collections.toList((Object) getDeviceIdentifier(), getLogBookObisCode());
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    /**
     * Check if the given {@link Object} is equal to this {@link LogBookIdentifierByObisCodeAndDevice}. <BR>
     * WARNING: if comparing with a {@link com.energyict.mdc.meterdata.identifiers.LogBookIdentifier} of another type (not of type {@link LogBookIdentifierByObisCodeAndDevice}),
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

    private LogBookFactory getLogBookFactory() {
        return LogBookFactoryProvider.instance.get().getLogBookFactory();
    }
}