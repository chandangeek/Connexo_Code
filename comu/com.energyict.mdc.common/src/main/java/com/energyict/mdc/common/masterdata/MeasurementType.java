/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.masterdata;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ConsumerType;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

import java.time.Instant;

/**
 * Represents the definition of a Measurement.
 */
@ConsumerType
public interface MeasurementType extends HasId {

    /**
     * Returns number that uniquely identifies this MeasurementType.
     *
     * @return the id
     */
    public long getId();

    /**
     * Returns the obis code for this MeasurementType.
     *
     * @return the obis code
     */
    public ObisCode getObisCode();

    public void setObisCode(ObisCode obisCode);

    /**
     * Test if this MeasurementType represents a cumulative reading.
     *
     * @return true if cumulative , false otherwise
     */
    public boolean isCumulative();

    public void setCumulative(boolean cumulative);

    /**
     * Returns the MeasurementType's unit.
     *
     * @return the mapping's unit
     */
    public Unit getUnit();

    public ReadingType getReadingType ();

    public void setReadingType(ReadingType readingType);

    /**
     * Returns the MeasurementType's description.
     *
     * @return the receiver's description
     */
    public String getDescription();

    public void setDescription(String newDescription);

    /**
     * Returns the MeasurementType's last modification date.
     *
     * @return the last modification date
     */
    public Instant getModificationDate();

    public int getTimeOfUse();

    public void save ();

    public void delete ();

    long getVersion();
}
