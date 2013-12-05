package com.energyict.mdc.protocol.api;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;

import java.util.Date;

/**
 * Straightforward ValueObject representation for a LogBook to read.
 */
public class LogBookReader {

    private static final long SECONDS_IN_DAY = 86400L;
    private static final long MILLISECONDS_IN_SECOND = 1000L;
    private static final long DAYS_IN_MONTH = 30L;

    /**
     * Holds the ObisCode from the LogBook to read
     */
    private final ObisCode logBookObisCode;

    /**
     * Holds the Date from where to start fetching data from the LogBook
     */
    private final Date lastLogBook;

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
     * @param meterSerialNumber The serial number of the meter of the logbook
     */
    public LogBookReader(ObisCode logBookObisCode, Date lastLogBook, LogBookIdentifier logBookIdentifier, String meterSerialNumber) {
        if(lastLogBook == null){
            this.lastLogBook = new Date(new Date().getTime() - (DAYS_IN_MONTH * SECONDS_IN_DAY * MILLISECONDS_IN_SECOND));    //endTime - 1 month
        } else {
            this.lastLogBook = lastLogBook;
        }
        this.logBookObisCode = logBookObisCode;
        this.logBookIdentifier = logBookIdentifier;
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
     * Getter for the {@link #meterSerialNumber}
     *
     * @return the {@link #meterSerialNumber}
     */
    public String getMeterSerialNumber() {
        return meterSerialNumber;
    }

    /**
     * Getter for the {@link #lastLogBook}
     *
     * @return the {@link #lastLogBook}
     */
    public Date getLastLogBook() {
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