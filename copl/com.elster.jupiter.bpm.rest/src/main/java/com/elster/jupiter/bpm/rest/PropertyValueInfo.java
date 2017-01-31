/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;


public class PropertyValueInfo {

    public String defaultValue;
    public String value;
    public String inheritedValue;
    public boolean propertyHasValue;

    public PropertyValueInfo(){

    }

    public PropertyValueInfo(String combo, String comboDefault){
        String[] comboArray = combo.split(";");
        if(comboDefault != null) {
            comboDefault = comboDefault.replace("\"", "");
            comboDefault = comboDefault.replace("=", "");
            for (int i = 0; i < comboArray.length; i++) {
                String[] keys = comboArray[i].split(",");
                if (keys[0].replace("{", "").equals(comboDefault)) {
                    defaultValue = keys[1].replaceAll("}", "").replaceAll("\"", "").replaceAll("=", "");
                }
            }
        }else{
            String[] keys = comboArray[0].split(",");
            defaultValue = keys[1].replaceAll("}", "").replaceAll("\"", "").replaceAll("=", "").trim();
        }
    }

    public PropertyValueInfo(String defaultValue){
        this.defaultValue = defaultValue;
    }

}
