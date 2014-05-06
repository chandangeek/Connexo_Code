package com.energyict.mdc.scheduling.rest.impl;

import com.energyict.mdc.tasks.ComTask;
import java.util.ArrayList;
import java.util.List;

public class ComTaskInfo {
    public long id;
    public String name;

    public static ComTaskInfo from(ComTask comTask) {
        ComTaskInfo info = new ComTaskInfo();
        info.id=comTask.getId();
        info.name=comTask.getName();
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
