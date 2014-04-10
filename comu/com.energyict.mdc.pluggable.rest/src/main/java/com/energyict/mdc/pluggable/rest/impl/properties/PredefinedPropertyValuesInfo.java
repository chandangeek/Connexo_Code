package com.energyict.mdc.pluggable.rest.impl.properties;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 11:38
 */
@XmlRootElement
public class PredefinedPropertyValuesInfo<T> {

    public T[] possibleValues;
    public PropertySelectionMode selectionMode;
    public boolean exhaustive;

    /**
     * Default constructor 4 JSON deserialization
     */
    public PredefinedPropertyValuesInfo() {
    }

    public PredefinedPropertyValuesInfo(T[] possibleValues, PropertySelectionMode selectionMode, boolean exhaustive) {
        this.possibleValues = possibleValues;
        this.selectionMode = selectionMode;
        this.exhaustive = exhaustive;
    }

    public T[] getPossibleValues() {
        return possibleValues;
    }

    /**
     * The selectionMode should help the FrontEnd to determine how to display these provided values
     *
     * @return the Mode in which the provided values should be displayed
     */
    public PropertySelectionMode getSelectionMode() {
        return selectionMode;
    }

    /**
     * Returns <code>true</code> if the possible values are an
     * exhaustive list, i.e. if no other values will be accepted.
     * Therefore, if this returns <code>false</code> then
     * the list of values returned by {@link #getPossibleValues()}
     * can be regarded as a example list of values.
     *
     * @return A flag that indicates if only values returned by the getAllValues method are accepted
     */
    public boolean isExhaustive() {
        return exhaustive;
    }
}
