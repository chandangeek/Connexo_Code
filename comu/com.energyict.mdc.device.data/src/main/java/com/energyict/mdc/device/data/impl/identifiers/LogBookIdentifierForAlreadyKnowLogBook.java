/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.MessageFormat;

/**
 * Represents a LogBookIdentifier for a LogBook that is already know (should not be fetched anymore)
 */
@XmlRootElement
public class LogBookIdentifierForAlreadyKnowLogBook implements LogBookIdentifier<LogBook> {

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
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {

    }

    @Override
    public DeviceIdentifier<?> getDeviceIdentifier() {
        return new DeviceIdentifierForAlreadyKnownDeviceByMrID(getLogBook().getDevice());
    }

    @Override
    public String toString() {
        return MessageFormat.format("logbook with name ''{0}'' on device with name ''{1}''",
                logBook.getLogBookType().getName(), logBook.getDevice().getName());
    }
}
