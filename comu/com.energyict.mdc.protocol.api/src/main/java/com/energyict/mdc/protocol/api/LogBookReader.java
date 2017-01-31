/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;

import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Straightforward ValueObject representation for a LogBook to read.
 */
public class LogBookReader {

    /**
     * Holds the ObisCode from the LogBook to read
     */
    private final ObisCode logBookObisCode;

    /**
     * Holds the Date from where to start fetching data from the LogBook
     */
    private final Instant lastLogBook;

    /**
     * The Identifier of the holding Device
     */
    private final DeviceIdentifier<?> deviceIdentifier;

    /**
     * Holds the serialNumber of the meter for this LogBook
     */
    private final String meterSerialNumber;

    /**
     * The LogBookIdentifier, which unique defines the LogBook to read.
     */
    private final LogBookIdentifier logBookIdentifier;

    /**
     * @param logBookObisCode   Holds the ObisCode from the LogBook to read
     * @param lastLogBook       Holds the Date from where to start fetching data from the LogBook
     * @param logBookIdentifier The LogBookIdentifier, which unique defines the LogBook to read.
     * @param deviceIdentifier  the unique identifier of the LogBook
     * @param meterSerialNumber the serialNumber of the device which owns this LogBook
     */
    public LogBookReader(Clock clock, ObisCode logBookObisCode, Optional<Instant> lastLogBook, LogBookIdentifier logBookIdentifier, DeviceIdentifier<?> deviceIdentifier, String meterSerialNumber) {
        if (!lastLogBook.isPresent()) {
            this.lastLogBook = clock.instant().atOffset(ZoneOffset.UTC).minus(Period.ofMonths(1)).toInstant();
        } else {
            this.lastLogBook = lastLogBook.get();
        }
        this.logBookObisCode = logBookObisCode;
        this.logBookIdentifier = logBookIdentifier;
        this.deviceIdentifier = deviceIdentifier;
        this.meterSerialNumber = meterSerialNumber;
    }

    /**
     * Getter for the {@link #logBookObisCode}
     *
     * @return the {@link #logBookObisCode}
     */
    public ObisCode getLogBookObisCode() {
        return logBookObisCode;
    }

    /**
     * Getter for the deviceIdentifier
     *
     * @return the deviceIdentifier
     */
    public DeviceIdentifier<?> getDeviceIdentifier() {
        return deviceIdentifier;
    }

    /**
     * Getter for the {@link #lastLogBook}
     *
     * @return the {@link #lastLogBook}
     */
    public Instant getLastLogBook() {
        return lastLogBook;
    }

    /**
     * Getter for the {@link #logBookIdentifier}
     *
     * @return the {@link #logBookIdentifier}
     */
    public LogBookIdentifier getLogBookIdentifier() {
        return logBookIdentifier;
    }

    public String getMeterSerialNumber() {
        return meterSerialNumber;
    }

    @Override
    public String toString() {
        return LogBookReader.class.getSimpleName() + ("(ObisCode:" + this.getLogBookObisCode() + ", LogBookIdentifier:" + this.getLogBookIdentifier().toString() + ", DeviceIdentifier:" + this.getDeviceIdentifier().toString() + ")");
    }

}