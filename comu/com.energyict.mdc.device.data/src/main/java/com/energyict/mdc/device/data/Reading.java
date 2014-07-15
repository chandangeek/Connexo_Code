package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.ReadingType;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Models data that was read from a Device and stored in a {@link Register}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-14 (11:58)
 */
public interface Reading {

    public ReadingType getType();

    public Date getTimeStamp();

    public Date getReportedDateTime();

    public BigDecimal getSensorAccuracy();

    public String getSource();

    /**
     * Tests if this Reading has already been validated
     * by the validation mechanism or not.
     * If that is the case then it would make sense
     * to get the reading qualities for this Reading.
     *
     * @return A flag that indicates if this Reading has been validated
     */
    public boolean isValidated ();

    /**
     * Gets the results of the validation mechanism
     * after having validated this Reading.
     * Note that when the validation mechanism has not
     * validated this Reading than an empty List will be returned.
     *
     * @return The List of ReadingQuality
     */
    public List<ReadingQuality> getReadingQualities();

}