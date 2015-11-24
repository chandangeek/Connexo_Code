package com.elster.jupiter.bpm.rest;


import java.util.ArrayList;
import java.util.List;

public class PredefinedPropertyValuesInfo {

//    public List<PossibleValues> possibleValues = new ArrayList<>();
    public List<String> possibleValues = new ArrayList<>();
    public String selectionMode;
    public boolean exhaustive;

    public PredefinedPropertyValuesInfo(){

    }

    public PredefinedPropertyValuesInfo(String comboValues){
        selectionMode = "COMBOBOX";
        exhaustive = true;
        String[] comboArray = comboValues.split(";");
        for(int i=0; i < comboArray.length; i++){
//            possibleValues.add(new PossibleValues(comboArray[i].substring(comboArray[i].indexOf(",") + 1).trim().replace("}","")));
            possibleValues.add(comboArray[i].substring(comboArray[i].indexOf(",") + 1).trim().replace("}",""));
        }
    }


}
