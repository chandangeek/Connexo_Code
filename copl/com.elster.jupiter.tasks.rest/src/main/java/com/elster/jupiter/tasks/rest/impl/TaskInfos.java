package com.elster.jupiter.tasks.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.time.TimeService;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@XmlRootElement
public class TaskInfos {

    public int total;
    public List<TaskInfo> tasks = new ArrayList<>();

    public TaskInfos() {
    }

    public TaskInfos(Iterable<? extends RecurrentTask> allTasks, Thesaurus thesaurus, TimeService timeService, Locale locale) {
        for (RecurrentTask each : allTasks) {
            if (each.getNextExecution() != null)  {
                tasks.add(new TaskInfo(each, thesaurus, timeService, locale));
                total++;
            }
        }
    }

}


