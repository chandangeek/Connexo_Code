package com.elster.jupiter.bpm.rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TasksWithMandatoryFieldsInfo {

    public String result = "";
    public TaskInfos taskListWithMandatoryFields;
    public TaskContentInfos taskForm;

    public TasksWithMandatoryFieldsInfo(){

    }
    public TasksWithMandatoryFieldsInfo(JSONObject jsonObject){
        try {
            result = jsonObject.getString("result");
            if(result.equals("WITHMANDATORY")) {
                JSONArray arr = jsonObject.getJSONObject("taskSummaryList").getJSONArray("tasks");
                taskListWithMandatoryFields = new TaskInfos(arr);
            }
            if(result.equals("ALLMANDATORY")) {
                JSONObject form = jsonObject.getJSONObject("connexoForm");
                taskForm = new TaskContentInfos(form);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
