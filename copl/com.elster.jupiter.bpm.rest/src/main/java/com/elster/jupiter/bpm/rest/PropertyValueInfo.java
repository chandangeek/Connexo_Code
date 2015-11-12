package com.elster.jupiter.bpm.rest;


public class PropertyValueInfo {

    public String defaultValue;
    public String value;
    public boolean propertyHasValue;

    public PropertyValueInfo(){

    }

    public PropertyValueInfo(String combo, String comboDefault){
        String[] comboArray = combo.split(";");
        for(int i=0; i < comboArray.length; i++){
            String[] keys = comboArray[i].split(",");
            if(keys[0].replace("{","").equals(comboDefault)){
                defaultValue = keys[1].replace("}","");
//                value = keys[1].replace("}","");
            }
        }
    }

    public PropertyValueInfo(String defaultValue){
        this.defaultValue = defaultValue;
    }

}
