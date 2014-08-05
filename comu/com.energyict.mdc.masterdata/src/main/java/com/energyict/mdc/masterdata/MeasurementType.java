package com.energyict.mdc.masterdata;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.interval.Phenomenon;
import java.util.Date;
import java.util.List;

/**
 * Represents the definition of a Measurement.
 */
public interface MeasurementType extends HasId {

    /**
     * Returns number that uniquely identifies this MeasurementType.
     *
     * @return the id
     */
    public long getId();

    /**
     * Returns the name that uniquely identifies this MeasurementType.
     *
     * @return the name
     */
    public String getName();

    public void setName (String newName);

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

    public void setUnit(Unit unit);

    public ReadingType getReadingType ();

    void setPhenomenon(Phenomenon phenomenon);

    public void setReadingType(ReadingType readingType);

    public Phenomenon getPhenomenon();

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
    public Date getModificationDate();

    public int getTimeOfUse();

    public void setTimeOfUse(int timeOfUse);

    public void save ();

    public void delete ();

}
