package com.energyict.mdc.common.interval;

import java.util.Date;

/**
 * IntervalRecord represents an interval reading
 *
 * @author Karel
 */

public interface IntervalRecord extends IntervalStateBits {

    /**
     * Validation OK
     */
    public static final int VALIDATION_OK = 100;
    /**
     * Validation unknown
     */
    public static final int VALIDATION_UNKNOWN = 0;
    /**
     * validation suspect
     */
    public static final int VALIDATION_SUSPECT = 1;
    /**
     * validation modified
     */
    public static final int VALIDATION_MODIFIED = 2;
    /**
     * validation confirmed
     */
    public static final int VALIDATION_CONFIRMED = 3;
    /**
     * validation warning
     */
    public static final int VALIDATION_WARNING = 4;
    /**
     * user tagged validation
     */
    public static final int VALIDATION_USER = 5;


    /**
     * Returns the receiver's tariff code or zero if no tariff codes are used.
     *
     * @return the tariff code or zero
     */
    public int getCode();

    /**
     * Returns the receiver's date.
     *
     * @return an integer dstamp
     * @deprecated
     */
    public int getDstamp();

    /**
     * Return the receiver's time stamp.
     *
     * @return an integer timestamp
     * @deprecated
     */
    public int getTstamp();

    /**
     * Returns the value of the first or only field in this interval.
     *
     * @return the interval value
     */
    public Number getValue();

    /**
     * Returns the interval value of the field identified by the index.
     *
     * @param index the zero based index
     * @return the interval value
     */
    public Number getValue(int index);

    /**
     * Returns a Date representing the end time of the interval.
     *
     * @return the end of the interval
     */
    public Date getDate();

    /**
     * Returns a DateTime representing the end of the interval
     *
     * @return the end of the interval
     * @deprecated
     */
    public DateTime getDateTime();

    /**
     * Returns the number of milliseconds between the end of the interval.
     * and 1/1/1970
     *
     * @return the number of milliseconds
     */
    public long getTime();

    /**
     * Returns the number of fields in this IntervalRecord.
     *
     * @return the number of fields
     */
    public int getNumberOfFields();

    /**
     * Returns the interval state.
     *
     * @return an integer describing the interval state
     */
    public int getIntervalState();

    /**
     * Returns the validation state.
     *
     * @return an integer describing the validation state
     */
    public int getValidationState();

    /**
     * Returns the date when this IntervalRecord was entered into the system,
     * or null if this information is not available.
     *
     * @return the registration Date or null.
     */
    public java.util.Date getEntryDate();

    /**
     * Returns an IntervalFlags object representing the interval's state(s).
     *
     * @return an IntervalFlags object representing the interval's state(s)
     */
    public IntervalFlags getIntervalFlags();

    /**
     * Test if value is missing.
     *
     * @return true if value is missing
     */
    public boolean hasMissingValue();

    /**
     * Test if value is zero.
     *
     * @return true if value is zero
     */
    public boolean hasZeroValue();
}
