package com.energyict.mdc.device.config;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.Unit;

/**
 * Represent a utility product, combining a Phenomenon with a time of use.
 * In CIM both Phenomenon and time of user are represented by {@link ReadingType}
 * so currently, a ProductSpec is actually a wrapper around a ReadingType
 * to keep the API and the database schema compatible.
 *
 * @author Karel
 */
@Deprecated // There is not really a replacement class, try Phenomenon or ReadingType on MeasurementType
public interface ProductSpec {

    /**
     * Returns number that uniquely identifies this ProductSpec.
     *
     * @return the id
     */
    public long getId();

    /**
     * Returns the product spec's measurement unit
     *
     * @return the unit
     */
    public Unit getUnit();

    /**
     * Returns a text description of this ProductSpec.
     *
     * @return the text description
     */
    public String getDescription();

    public ReadingType getReadingType();

    public void setReadingType(ReadingType readingType);

    public void save();

    public void delete();

}