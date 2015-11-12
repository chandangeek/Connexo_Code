package com.elster.jupiter.bpm.rest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
                simplePropertyType = "TEXT";
                checkAndCreateComboBox(field);
            }
            if(field.getString("type").equals("InputTextArea")){
                simplePropertyType = "TEXTAREA";
                checkAndCreateComboBox(field);
            }
            if(field.getString("type").equals("InputTextFloat")){
                simplePropertyType = "NUMBER";
                checkAndCreateComboBox(field);
            }
            if(field.getString("type").equals("InputTextDouble")){
                simplePropertyType = "NUMBER";
                checkAndCreateComboBox(field);
            }
            if(field.getString("type").equals("InputTextBigDecimal")){
                simplePropertyType = "LONG";
                checkAndCreateComboBox(field);
            }
            if(field.getString("type").equals("InputTextBigInteger")){
                simplePropertyType = "LONG";
                checkAndCreateComboBox(field);
            }
            if(field.getString("type").equals("InputTextInteger")){
                simplePropertyType = "LONG";
                checkAndCreateComboBox(field);
            }
            if(field.getString("type").equals("InputDate")){
                simplePropertyType = "TIMESTAMP";
            }
            if(field.getString("type").equals("CheckBox")){
                simplePropertyType = "BOOLEAN";
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String getComboBoxValues(JSONObject field){
        JSONArray arr = null;
        try {
            arr = field.getJSONArray("properties");
            if (arr != null){
                for(int i = 0; i < arr.length(); i++) {
                    JSONObject prop = arr.getJSONObject(i);
                    if(prop.getString("name").equals("rangeFormula")){
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

    private String getComboBoxDefault(JSONObject field){
        JSONArray arr = null;
        try {
            arr = field.getJSONArray("properties");
            if (arr != null){
                for(int i = 0; i < arr.length(); i++) {
                    JSONObject prop = arr.getJSONObject(i);
                    if(prop.getString("name").equals("defaultValueFormula")){
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

    private void checkAndCreateComboBox(JSONObject field){
        String combo = getComboBoxValues(field);
        if(combo !=null){
            String comboDefault = getComboBoxDefault(field);
            if(comboDefault != null){
                taskContentInfo.propertyValueInfo = new PropertyValueInfo(combo, comboDefault);
            }
            predefinedPropertyValuesInfo = new PredefinedPropertyValuesInfo(combo);
        }
    }

}
