/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        this();
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

            tasks.stream().sorted((t1, t2) -> {
                if (t1.dueDate.isEmpty()) {
                    return (t2.dueDate.isEmpty()) ? ((Integer.parseInt(t1.priority) < Integer.parseInt(t2.priority)) ? -1 : 1) : -1;
                } else {
                    return (Long.parseLong(t1.dueDate) < Long.parseLong(t2.dueDate)) ? -1 : 1;
                }
            });
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