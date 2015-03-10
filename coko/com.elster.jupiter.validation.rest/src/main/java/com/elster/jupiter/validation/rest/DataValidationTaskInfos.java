package com.elster.jupiter.validation.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.rest.DataValidationTaskInfo;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
public class DataValidationTaskInfos {
    public int total;
    public List<DataValidationTaskInfo> dataValidationTasks = new ArrayList<>();
/*
    public DataValidationTaskInfos() {
    }
*/
    public DataValidationTaskInfos(Iterable<? extends DataValidationTask> sets){//), Thesaurus thesaurus, TimeService timeService) {
        addAll(sets);//, thesaurus, timeService);
    }

    public DataValidationTaskInfo add(DataValidationTask ruleSet) {//, Thesaurus thesaurus, TimeService timService) {
        DataValidationTaskInfo result = new DataValidationTaskInfo(ruleSet);//, thesaurus, timService);
        dataValidationTasks.add(result);
        total++;
        return result;
    }


    public void addAll(Iterable<? extends DataValidationTask> sets) {//, Thesaurus thesaurus, TimeService timeService) {
        for (DataValidationTask each : sets) {
            add(each);//, thesaurus, timeService);
        }
    }
}
