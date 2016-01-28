package com.elster.jupiter.bpm.rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class TasksWithMandatoryFieldsInfo {

    public String result = "";
//    public TaskInfos taskListWithMandatoryFields;
    public Map<String, Map<String, List<TaskInfo>>> taskListWithMandatoryFields;
    public TaskContentInfos taskForm;

    public TasksWithMandatoryFieldsInfo(){

    }
    public TasksWithMandatoryFieldsInfo(JSONObject jsonObject){
        try {
            result = jsonObject.getString("result");
            if(result.equals("WITHMANDATORY")) {
                JSONArray arr = jsonObject.getJSONObject("taskSummaryList").getJSONArray("tasks");
                TaskInfos x= new TaskInfos(arr);
                taskListWithMandatoryFields = x.getTasks().stream()
                        .collect(groupingBy(TaskInfo::getName, groupingBy(TaskInfo::getDeploymentId)));
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
