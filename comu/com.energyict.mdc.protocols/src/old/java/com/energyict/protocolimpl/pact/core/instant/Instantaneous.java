/*
 * instantaneous.java
 *
 * Created on 12 mei 2004, 14:50
 */

package com.energyict.protocolimpl.pact.core.instant;

import com.energyict.cbo.Unit;
/**
 *
 * @author  Koen
 */
public class Instantaneous {

    private String description;
    private Unit unit;
    private int type;

    /** Creates a new instance of instantaneous */
    public Instantaneous(String description,Unit unit,int type) {
        this.description=description;
        this.unit=unit;
        this.type=type;
    }

    /**
     * Getter for property unit.
     * @return Value of property unit.
     */
    public Unit getUnit() {
        return unit;
    }

    /**
     * Setter for property unit.
     * @param unit New value of property unit.
     */
    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    /**
     * Getter for property type.
     * @return Value of property type.
     */
    public int getType() {
        return type;
    }

    /**
     * Setter for property type.
     * @param type New value of property type.
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Getter for property description.
     * @return Value of property description.
     */
    public java.lang.String getDescription() {
        return description;
    }

    /**
     * Setter for property description.
     * @param description New value of property description.
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }

}
