package com.energyict.mdc.protocol.api;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import java.time.Clock;
import java.time.Instant;
import java.time.Period;
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
     * The LogBookIdentifier, which unique defines the LogBook to read.
     */
    private final LogBookIdentifier logBookIdentifier;

    /**
     * @param logBookObisCode   Holds the ObisCode from the LogBook to read
     * @param lastLogBook       Holds the Date from where to start fetching data from the LogBook
     * @param logBookIdentifier The LogBookIdentifier, which unique defines the LogBook to read.
     * @param deviceIdentifier The serial number of the meter of the logbook
     */
    public LogBookReader(Clock clock, ObisCode logBookObisCode, Optional<Instant> lastLogBook, LogBookIdentifier logBookIdentifier, DeviceIdentifier<?> deviceIdentifier) {
        if (!lastLogBook.isPresent()) {
            this.lastLogBook = clock.instant().minus(Period.ofMonths(1));
        } else {
            this.lastLogBook = lastLogBook.get();
        }
        this.logBookObisCode = logBookObisCode;
        this.logBookIdentifier = logBookIdentifier;
        this.deviceIdentifier = deviceIdentifier;
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

}