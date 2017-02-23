/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.security;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public enum Privileges implements TranslationKey {
    //Resources
    RESOURCE_BPM_PROCESSES("bpm.businessProcesses", "Business processes"),
    RESOURCE_BPM_PROCESSES_DESCRIPTION("bpm.businessProcesses.description", "Manage business processes"),
    RESOURCE_BPM_TASKS("bpm.userTasks", "User tasks"),
    RESOURCE_BPM_TASKS_DESCRIPTION("bpm.userTasks.description", "Manage user tasks"),
    PROCESS_EXECUTION_LEVELS("bpm.process.execution.levels", "Process security"),
    PROCESS_EXECUTION_LEVELS_DESCRIPTION("bpm.process.execution.levels.description", "Manage process security"),


    //Privileges
    VIEW_BPM(Constants.VIEW_BPM, "View"),
    DESIGN_BPM(Constants.DESIGN_BPM, "Design"),
    EXECUTE_TASK(Constants.EXECUTE_TASK, "Execute"),
    ASSIGN_TASK(Constants.ASSIGN_TASK, "Assign"),
    VIEW_TASK(Constants.VIEW_TASK, "View"),
    ADMINISTRATE_BPM(Constants.ADMINISTRATE_BPM, "Administrate"),
    EXECUTE_PROCESSES_LVL_1(Constants.EXECUTE_PROCESSES_LVL_1, "Execute level 1"),
    EXECUTE_PROCESSES_LVL_2(Constants.EXECUTE_PROCESSES_LVL_2, "Execute level 2"),
    EXECUTE_PROCESSES_LVL_3(Constants.EXECUTE_PROCESSES_LVL_3, "Execute level 3"),
    EXECUTE_PROCESSES_LVL_4(Constants.EXECUTE_PROCESSES_LVL_4, "Execute level 4");

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

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getString(this.getKey(), this.getDefaultFormat());
    }

    public static String getDescriptionForKey(String key, Thesaurus thesaurus){
        Optional<String> description = Arrays.stream(Privileges.values())
                .filter(s -> s.getKey().equals(key))
                .map(p -> p.getDisplayName(thesaurus))
                .findFirst();
        if(description.isPresent()){
            return description.get();
        }
        return "";
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
        String ADMINISTRATE_BPM = "privilege.administrate.bpm";
        String EXECUTE_PROCESSES_LVL_1 = "privilege.execute.processes.lvl.1";
        String EXECUTE_PROCESSES_LVL_2 = "privilege.execute.processes.lvl.2";
        String EXECUTE_PROCESSES_LVL_3 = "privilege.execute.processes.lvl.3";
        String EXECUTE_PROCESSES_LVL_4 = "privilege.execute.processes.lvl.4";
    }
}
