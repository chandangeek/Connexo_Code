package com.elster.jupiter.bpm.rest;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class TaskContentInfo {

    public String key;
    public String name;
    public String outputBinding;
    public boolean required;
    public boolean isReadOnly;
    public PropertyTypeInfo propertyTypeInfo;
    public PropertyValueInfo propertyValueInfo;

    public TaskContentInfo(){

    }

    public TaskContentInfo(JSONObject field,JSONObject content, JSONObject outputContent, String status){
        try {
            key = field.getString("type") + field.getString("id");
            JSONArray arr = field.getJSONArray("properties");
            if (arr != null){
                for(int i = 0; i < arr.length(); i++) {
                    JSONObject prop = arr.getJSONObject(i);
                    if(prop.getString("name").equals("label")){
                        name = extractFieldName(prop.getString("value"));
                    }
                    if(prop.getString("name").equals("fieldRequired")){
                        if(prop.getString("value").equals("true")){
                            required = true;
                        }else{
                            required = false;
                        }
                    }
                    if(!status.equals("InProgress")){
                        isReadOnly = true;
                    }else if(prop.getString("name").equals("readonly")){
                        if(prop.getString("value").equals("true")){
                            isReadOnly = true;
                        }else{
                            isReadOnly = false;
                        }
                    }
                    if(prop.getString("name").equals("inputBinding")){
                        if(!prop.getString("value").equals("")) {
                            setDefaultValueBinding(prop.getString("value"), content, field);
                        }
                    }
                    if(prop.getString("name").equals("outputBinding")){
                        if(!prop.getString("value").equals("")) {
                            if(outputContent != null) {
                                setDefaultValueBinding(prop.getString("value"), outputContent, field);
                            }
                            outputBinding = prop.getString("value");
                        }
                    }
                }
            }
            propertyTypeInfo = new PropertyTypeInfo(field, this);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String extractFieldName(String name){
        String[] stringArray = name.split("quot;");
        if(stringArray.length > 1){
            for(int i=0;i<stringArray.length;i++){
                if(stringArray[i].length() > 2){
                    if(!stringArray[i].equals("SSClass") && !stringArray[i].equals("SSStyle")){
                        return stringArray[i];
                    }
                }
            }
        }
        return name.replace("quot","");
    }

    private void setDefaultValueBinding(String value, JSONObject jsonObject, JSONObject field){
        Iterator<?> keys = jsonObject.keys();
        while( keys.hasNext() ) {
            String key = (String)keys.next();
            if(key.equals(value)){
                try {
                    if(field.getString("type").equals("InputDate") || field.getString("type").equals("InputShortDate")){
                        if(!jsonObject.getString(key).equals("")) {
                            Date date = new Date(jsonObject.getLong(key));
                            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
                            if(field.getString("type").equals("InputShortDate")){
                                dateFormat = new SimpleDateFormat("MM/dd/yy");
                            }
                            propertyValueInfo = new PropertyValueInfo(dateFormat.format(date));
                        }
                    }else {
                        try {
                            propertyValueInfo = new PropertyValueInfo(jsonObject.getString(key));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
