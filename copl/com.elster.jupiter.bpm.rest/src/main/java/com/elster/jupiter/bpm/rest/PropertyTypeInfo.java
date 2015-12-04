package com.elster.jupiter.bpm.rest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PropertyTypeInfo {

    public String simplePropertyType;
    public PredefinedPropertyValuesInfo predefinedPropertyValuesInfo;
    public PropertyValueInfo propertyValueInfo;
    private TaskContentInfo taskContentInfo;


    public PropertyTypeInfo(){

    }

    public PropertyTypeInfo(JSONObject field, TaskContentInfo taskContentInfo){
        this.taskContentInfo = taskContentInfo;
        try {
            if(field.getString("type").equals("InputText")){
                simplePropertyType = passwordOrSimple(field);
                if(!checkAndCreateComboBox(field)){
                    checkDefaultValues(field);
                }
            }
            if(field.getString("type").equals("InputTextCharacter")){
                simplePropertyType = "TEXT";
                if(!checkAndCreateComboBox(field)){
                    checkDefaultValues(field);
                }
            }
            if(field.getString("type").equals("InputTextArea")){
                simplePropertyType = "TEXTAREA";
                if(!checkAndCreateComboBox(field)){
                    checkDefaultValues(field);
                }
            }
            if(field.getString("type").equals("InputTextFloat")){
                simplePropertyType = "NUMBER";
                if(!checkAndCreateComboBox(field)){
                    checkDefaultValues(field);
                }
            }
            if(field.getString("type").equals("InputTextDouble")){
                simplePropertyType = "NUMBER";
                if(!checkAndCreateComboBox(field)){
                    checkDefaultValues(field);
                }
            }
            if(field.getString("type").equals("InputTextBigDecimal")){
                simplePropertyType = "LONG";
                if(!checkAndCreateComboBox(field)){
                    checkDefaultValues(field);
                }
            }
            if(field.getString("type").equals("InputTextBigInteger")){
                simplePropertyType = "LONG";
                if(!checkAndCreateComboBox(field)){
                    checkDefaultValues(field);
                }
            }
            if(field.getString("type").equals("InputTextInteger")){
                simplePropertyType = "LONG";
                if(!checkAndCreateComboBox(field)){
                    checkDefaultValues(field);
                }
            }
            if(field.getString("type").equals("InputTextLong")){
                simplePropertyType = "LONG";
                if(!checkAndCreateComboBox(field)){
                    checkDefaultValues(field);
                }
            }
            if(field.getString("type").equals("InputDate")){
                simplePropertyType = "TIMESTAMP";
            }
            if(field.getString("type").equals("InputShortDate")){
                simplePropertyType = "DATE";
            }
            if(field.getString("type").equals("CheckBox")){
                simplePropertyType = "BOOLEAN";
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String getComboBoxValues(JSONObject field, String property){
        JSONArray arr = null;
        try {
            arr = field.getJSONArray("properties");
            if (arr != null){
                for(int i = 0; i < arr.length(); i++) {
                    JSONObject prop = arr.getJSONObject(i);
                    if(prop.getString("name").equals(property)){
                        if(!prop.getString("value").equals("")){
                            return prop.getString("value");
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean checkAndCreateComboBox(JSONObject field){
        String combo = getComboBoxValues(field, "rangeFormula");
        if(combo !=null){
            String[] comboArray = combo.split(";");
            Map<String, Object> comboKeys = new HashMap<>();
            for(int i=0; i < comboArray.length; i++){
                comboKeys.put(comboArray[i].split(",")[0].replace("{",""), comboArray[i].substring(comboArray[i].indexOf(",") + 1).trim().replace("}",""));
                comboArray[i] = comboArray[i].substring(comboArray[i].indexOf(",") + 1).trim().replace("}","");
            }
            if(taskContentInfo.propertyValueInfo == null) {
                String comboDefault = getComboBoxValues(field, "defaultValueFormula");
                taskContentInfo.propertyValueInfo = new PropertyValueInfo(combo, comboDefault);
            }else if(taskContentInfo.propertyValueInfo.defaultValue.isEmpty()){
                String comboDefault = getComboBoxValues(field, "defaultValueFormula");
                taskContentInfo.propertyValueInfo = new PropertyValueInfo(combo, comboDefault);
            }else{
                Iterator<String> it = comboKeys.keySet().iterator();
                while(it.hasNext()){
                    String theKey = (String)it.next();
                    if(theKey.equals(taskContentInfo.propertyValueInfo.defaultValue)){
                        taskContentInfo.propertyValueInfo = new PropertyValueInfo(comboKeys.get(theKey).toString());
                    }
                }
            }
            predefinedPropertyValuesInfo = new PredefinedPropertyValuesInfo(comboArray, comboKeys);
            return true;
        }
        return false;
    }

    private void checkDefaultValues(JSONObject field){
        String defaultValue = getComboBoxValues(field, "defaultValueFormula");
        if(defaultValue != null){
            taskContentInfo.propertyValueInfo = new PropertyValueInfo(defaultValue.replaceAll("\"","").replaceAll("=",""));
        }
    }

    private String passwordOrSimple(JSONObject field){
        JSONArray arr = null;
        try {
            arr = field.getJSONArray("properties");
            if (arr != null){
                for(int i = 0; i < arr.length(); i++) {
                    JSONObject prop = arr.getJSONObject(i);
                    if(prop.getString("name").equals("hideContent")){
                        if(prop.getString("value").equals("true")){
                            return "PASSWORD";
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "TEXT";
    }

}
