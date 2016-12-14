package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifierType;

import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlRootElement;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a LogBookIdentifier for a LogBook that is already know (should not be fetched anymore)
 */
@XmlRootElement
public class LogBookIdentifierForAlreadyKnowLogBook implements LogBookIdentifier {

    private final LogBook logBook;

    public LogBookIdentifierForAlreadyKnowLogBook(LogBook logBook) {
        this.logBook = logBook;
    }

    @Override
    public ObisCode getLogBookObisCode() {
        return logBook.getDeviceObisCode();
    }

    @Override
    public LogBook getLogBook() {
        return this.logBook;
    }

    @Override
    public LogBookIdentifierType getLogBookIdentifierType() {
        return LogBookIdentifierType.ActualLogBook;
    }

    @Override
    public List<Object> getParts() {
        return Arrays.asList(logBook);
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return new DeviceIdentifierForAlreadyKnownDeviceByMrID(getLogBook().getDevice());
    }

    @Override
    public String toString() {
        return MessageFormat.format("logbook with name ''{0}'' on device having MRID {1}", logBook.getLogBookType().getName(), logBook.getId());
    }
}