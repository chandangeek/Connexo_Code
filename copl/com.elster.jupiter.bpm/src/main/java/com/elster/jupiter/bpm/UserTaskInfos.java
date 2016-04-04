package com.elster.jupiter.bpm;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserTaskInfos {

    public int total;

    public List<UserTaskInfo> tasks = new ArrayList<>();

    public UserTaskInfos() {
    }

    public UserTaskInfos(JSONArray tasks, String currentUser) {
        addAll(tasks, currentUser);
    }

    void addAll(JSONArray taskList, String currentUser) {
        if (taskList != null) {
            for (int i = 0; i < taskList.length(); i++) {
                try {
                    JSONObject task = taskList.getJSONObject(i);
                    UserTaskInfo result = new UserTaskInfo(task, currentUser);
                    tasks.add(result);
                    total++;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public List<UserTaskInfo> getTasks() {
        return tasks;
    }

    public void removeLast() {
        tasks.remove(tasks.size() - 1);
        total++;
    }
}
