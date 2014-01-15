/*
 * Phenomen.java
 *
 * Created on 12 mei 2003, 15:05
 */

package com.energyict.mdc.common;

import java.sql.SQLException;

/**
 * Class Phenomenon represents a physical magnitude,
 * combined with the unit in which it will be measured:
 * e.g.
 * Active Energy Import in kWh.
 * Peak Demand Import in kW.
 *
 * @author Karel
 */
public interface Phenomenon extends NamedBusinessObject {

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
     * Returns a shadow object, initialized with the receiver.
     *
     * @return the shadow.
     */
    public PhenomenonShadow getShadow();

    /**
     * Updates the receiver with the information in the argument.
     *
     * @param shadow contains the updated information.
     * @throws BusinessException if a business error occured.
     * @throws SQLException      if a database error occured.
     */
    public void update(final PhenomenonShadow shadow) throws BusinessException, SQLException;

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
}

