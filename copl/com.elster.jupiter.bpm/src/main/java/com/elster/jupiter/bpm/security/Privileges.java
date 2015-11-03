package com.elster.jupiter.bpm.security;

import com.elster.jupiter.bpm.BpmAppService;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.nls.TranslationKey;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_BPM_PROCESSES("bpm.businessProcesses", "Business processes"),
    RESOURCE_BPM_PROCESSES_DESCRIPTION("bpm.businessProcesses.description", "Manage business processes"),
    RESOURCE_BPM_TASKS("bpm.userTasks", "User tasks"),
    RESOURCE_BPM_TASKS_DESCRIPTION("bpm.userTasks.description", "Manage user tasks"),

    //Privileges
    VIEW_BPM(Constants.VIEW_BPM, "View"),
    DESIGN_BPM(Constants.DESIGN_BPM, "Design"),
    EXECUTE_TASK(Constants.EXECUTE_TASK, "Execute"),
    ASSIGN_TASK(Constants.ASSIGN_TASK, "Assign"),
    VIEW_TASK(Constants.VIEW_TASK, "View");

    private final String key;
    private final String description;

    Privileges(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return getDescription();
    }

    public String getDescription() {
        return description;
    }

    public static String[] keys() {
        return Arrays.stream(Privileges.values())
                .map(Privileges::getKey)
                .collect(Collectors.toList())
                .toArray(new String[Privileges.values().length]);
    }

    public interface Constants {
        String VIEW_BPM = "privilege.view.bpm";
        String DESIGN_BPM = "privilege.design.bpm";
        String EXECUTE_TASK = "privilege.execute.task";
        String ASSIGN_TASK = "privilege.assign.task";
        String VIEW_TASK = "privilege.view.task";
    }
}
