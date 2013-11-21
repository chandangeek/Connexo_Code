package com.energyict.mdc.rest.impl.properties;

/**
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 11:38
 */
public class PredefinedPropertyValuesInfo<T> {

    final T[] possibleValues;
    final PropertySelectionMode selectionMode;
    final boolean exhaustive;

    public PredefinedPropertyValuesInfo(T[] possibleValues, PropertySelectionMode selectionMode, boolean exhaustive) {
        this.possibleValues = possibleValues;
        this.selectionMode = selectionMode;
        this.exhaustive = exhaustive;
    }

    public T[] getPossibleValues() {
        return possibleValues;
    }

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
