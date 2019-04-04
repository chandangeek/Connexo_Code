/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.rest;

import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ComTaskUserAction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ComTaskInfo {
    public long id;
    public String name;
    public boolean isSystemComTask;
    public List<String> privileges;

    public static ComTaskInfo from(ComTask comTask) {
        ComTaskInfo info = new ComTaskInfo();
        info.id=comTask.getId();
        info.name=comTask.getName();
        info.isSystemComTask = comTask.isSystemComTask();
        info.privileges = comTask.getUserActions().stream().map(ComTaskUserAction::getPrivilege).sorted()
                .collect(Collectors.toList());
        return info;
    }

    public static List<ComTaskInfo> from(List<ComTask> comTasks) {
        List<ComTaskInfo> infos = new ArrayList<>();
        for (ComTask comTask : comTasks) {
            infos.add(from(comTask));
        }
        return infos;
    }



}
