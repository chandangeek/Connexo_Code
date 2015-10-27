package com.elster.jupiter.tasks.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.time.TimeService;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
public class TaskInfos {

    public int total;
    public List<TaskInfo> tasks = new ArrayList<>();

    public TaskInfos() {
    }

    public TaskInfos(Iterable<? extends RecurrentTask> allTasks) {
        for (RecurrentTask each : allTasks) {
            tasks.add(new TaskInfo(each));
            total++;
        }
    }

}


