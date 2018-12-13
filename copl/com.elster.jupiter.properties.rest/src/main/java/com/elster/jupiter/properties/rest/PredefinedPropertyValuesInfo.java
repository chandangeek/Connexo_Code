/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest;

import com.elster.jupiter.properties.PropertySelectionMode;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PredefinedPropertyValuesInfo<T> {

    public T[] possibleValues;
    public PropertySelectionMode selectionMode;
    public boolean exhaustive;
    public boolean editable;

    /**
     * Default constructor 4 JSON deserialization
     */
    public PredefinedPropertyValuesInfo() {
    }

    public PredefinedPropertyValuesInfo(T[] possibleValues, PropertySelectionMode selectionMode, boolean exhaustive, boolean editable) {
        this.possibleValues = possibleValues;
        this.selectionMode = selectionMode;
        this.exhaustive = exhaustive;
        this.editable = editable;
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

    /**
     * Returns <code>true</code> if the possible values should be editable,
     * i.e. the user should be allowed to type in text directly into the field,
     * after which matching possibilities are shown. This could be used in situations
     * where i.e. a ComboBox contains a lot of possible values. In this case,
     * it is not convenient for the user to select the value from the picker
     * (as he has to search his desired value in a big list of possible values).
     *
     * @return A flag that indicates if values should be editable or not
     */
    public boolean isEditable() {
        return editable;
    }
}
