/*
 * Phenomen.java
 *
 * Created on 12 mei 2003, 15:05
 */

package com.energyict.mdc.common.interval;

import com.energyict.mdc.common.Unit;

/**
 * Class Phenomenon represents a physical magnitude,
 * combined with the unit in which it will be measured:
 * e.g.
 * Active Energy Import in kWh.
 * Peak Demand Import in kW.
 *
 * @author Karel
 */
public interface Phenomenon {

    /**
     * Returns the object's unique id
     *
     * @return the id
     */
    public long getId();

    /**
     * Returns the object's name
     *
     * @return the name
     */
    public String getName();

    public void setName (String newName);

    /**
     * Returns the receiver's unit.
     *
     * @return the unit.
     */
    public Unit getUnit();

    /**
     * Returns a description of the receiver.
     *
     * @return the description.
     */
    public String getDescription();

    /**
     * Returns the code used to identify the receiver in a measurement context.
     *
     * @return the measurement code.
     */
    public String getMeasurementCode();

    /**
     * Returns a code to represent the receiver in an
     * Electronic Data Interchange context.
     *
     * @return the EDI code.
     */
    public String getEdiCode();

    /**
     * Tests if the receiver represents the undefined Phenomenon.
     *
     * @return true if the receiver represents an undefined Phenomenon.
     */
    public boolean isUndefined();

    /**
     * Tests if the receiver represents a phenomenon without unit
     * Equivalent to getUnit().isUndefined()
     *
     * @return true if the receiver represents an undefined Phenomenon.
     */
    public boolean isUnitless();

    boolean isInUse();

    public void save();

    public void delete();

    void setUnit(Unit unit);

    void setMeasurementCode(String measurementCode);

    void setEdiCode(String ediCode);
}

