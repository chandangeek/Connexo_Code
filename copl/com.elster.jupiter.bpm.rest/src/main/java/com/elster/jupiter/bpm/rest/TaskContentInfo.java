package com.elster.jupiter.bpm.rest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TaskContentInfo {

    public String key;
    public String name;
    public boolean required;
    public PropertyTypeInfo propertyTypeInfo;
    public PropertyValueInfo propertyValueInfo;

    public TaskContentInfo(){

    }

    public TaskContentInfo(JSONObject field){
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


}
