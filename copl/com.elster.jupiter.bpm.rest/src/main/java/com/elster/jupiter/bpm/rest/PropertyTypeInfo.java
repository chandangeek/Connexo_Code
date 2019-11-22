/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;


import com.elster.jupiter.properties.QuantityValueFactory;
import com.elster.jupiter.util.units.Quantity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PropertyTypeInfo {

    public String simplePropertyType;
    public PredefinedPropertyValuesInfo predefinedPropertyValuesInfo;
    public PropertyValueInfo propertyValueInfo;
    public String valueProviderUrl;
    private TaskContentInfo taskContentInfo;
    public Map<String, Object> params;


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
            if (field.getString("type").equals("CustomInput")) {
                createCustomField(field);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void createCustomField(JSONObject field) {
        String param1 = getComboBoxValue(field, "param1");
        if ("quantity".equalsIgnoreCase(param1)) {
            createQuantityField(field);
        } else if ("DropDown".equalsIgnoreCase(param1)) {
            createDropDown(field);
        } else if ("readingType".equalsIgnoreCase(param1)) {
            simplePropertyType = "METROLOGYCONFIGOUTPUT";
        } else if ("certSecurityAccessors".equalsIgnoreCase(param1)) {
            simplePropertyType = "CERTSECURITYACCESSORSOUTPUT";
        } else if ("securityAccessors".equalsIgnoreCase(param1)) {
            simplePropertyType = "SECURITYACCESSORSOUTPUT";
        } else if ("serviceKeysSignatures".equalsIgnoreCase(param1)) {
            simplePropertyType = "SERVICEKEYSSIGNATURESOUTPUT";
        } else if ("meterActivationsOnUsagePoint".equalsIgnoreCase(param1)) {
            simplePropertyType = "UP_METERACTIVATION";
        } else if ("meterMrid".equalsIgnoreCase(param1)) {
            simplePropertyType = "METER_MRID";
        } else if ("metrologyConfiguration".equalsIgnoreCase(param1)) {
            simplePropertyType = "METROLOGYCONFIGURATION";
        } else if ("metrologyPurposes".equalsIgnoreCase(param1)) {
            simplePropertyType = "METROLOGYPURPOSES";
        } else if ("usagePointTransition".equalsIgnoreCase(param1)) {
            simplePropertyType = "UP_TRANSITION";
            if(getComboBoxValue(field, "param2")!=null) {
                params = new HashMap<>();
                params.put("toStage", getComboBoxValue(field, "param2"));
            }
        } else if ("meterInstallationDate".equalsIgnoreCase(param1)) {
            simplePropertyType = "METER_INSTALLATION_DATE";
        } else if ("meterRole".equalsIgnoreCase(param1)) {
            simplePropertyType = "METER_ROLE";
        }
    }

    private void createDropDown(JSONObject field) {
        simplePropertyType = "DYNAMIC_COMBOBOX";
        valueProviderUrl = getComboBoxValue(field, "param2");
    }

    private void createQuantityField(JSONObject field) {
        simplePropertyType = "QUANTITY";

        List<Integer> multipliers = Collections.singletonList(0);
        String param2 = getComboBoxValue(field, "param2");

        if (param2 != null) {
            multipliers = Arrays.stream(param2.split(","))
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());
        }

        String param3 = getComboBoxValue(field, "param3");
        List<String> units = Collections.emptyList();

        if (param3 != null) {
            units = Arrays.stream(param3.split(","))
                    .collect(Collectors.toList());
        }

        List<QuantityInfo> quantityInfos = new ArrayList<>();
        Map<String, Object> comboKeys = new HashMap<>();

        for (Integer multiplier : multipliers) {
            for (String unit : units) {
                QuantityInfo quantity = new QuantityInfo(Quantity.create(BigDecimal.ZERO, multiplier, unit));
                quantityInfos.add(quantity);
                comboKeys.put(quantity.displayValue, quantity);
            }
        }

        predefinedPropertyValuesInfo = new PredefinedPropertyValuesInfo<>(quantityInfos.toArray(), comboKeys);
        predefinedPropertyValuesInfo.selectionMode = "";
    }

    static class QuantityInfo {
        public String id;
        public String displayValue;

        public QuantityInfo(Quantity quantity) {
            this.id = new QuantityValueFactory().toStringValue(quantity);
            this.displayValue = quantity.toString().replace("0 ", "");
        }
    }

    private String getComboBoxValue(JSONObject field, String property){
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
        String combo = getComboBoxValue(field, "rangeFormula");
        if(combo !=null){
            String[] comboArray = combo.split(";");
            Map<String, Object> comboKeys = new HashMap<>();
            for(int i=0; i < comboArray.length; i++){
                comboKeys.put(comboArray[i].split(",")[0].replace("{",""), comboArray[i].substring(comboArray[i].indexOf(",") + 1).trim().replace("}",""));
                comboArray[i] = comboArray[i].substring(comboArray[i].indexOf(",") + 1).trim().replace("}","");
            }
            if(taskContentInfo.propertyValueInfo == null) {
                String comboDefault = getComboBoxValue(field, "defaultValueFormula");
                taskContentInfo.propertyValueInfo = new PropertyValueInfo(combo, comboDefault);
            }else if(taskContentInfo.propertyValueInfo.defaultValue == null || "".equals(taskContentInfo.propertyValueInfo.defaultValue)){
                String comboDefault = getComboBoxValue(field, "defaultValueFormula");
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
        String defaultValue = getComboBoxValue(field, "defaultValueFormula");
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
