/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PredefinedPropertyValuesInfo<T> {

    public T[] possibleValues;
    public Map<String, Object> comboKeys;
    public String selectionMode;
    public boolean exhaustive;

    public PredefinedPropertyValuesInfo(){

    }

    public PredefinedPropertyValuesInfo(T[] possibleValues, Map<String, Object> comboKeys){
        this.possibleValues = possibleValues;
        this.comboKeys = comboKeys;
        selectionMode = "COMBOBOX";
        exhaustive = true;
    }

}
