package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.nls.Thesaurus;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
public class EstimationTaskInfos {

    public int total;
    public List<EstimationTaskInfo> estimationTasks = new ArrayList<>();

    public EstimationTaskInfos() {
    }

    public EstimationTaskInfos(Iterable<? extends EstimationTask> sets, Thesaurus thesaurus) {
        addAll(sets, thesaurus);
    }

    public EstimationTaskInfo add(EstimationTask ruleSet, Thesaurus thesaurus) {
        EstimationTaskInfo result = new EstimationTaskInfo(ruleSet, thesaurus);
        estimationTasks.add(result);
        total++;
        return result;
    }

    public void addAll(Iterable<? extends EstimationTask> sets, Thesaurus thesaurus) {
        for (EstimationTask each : sets) {
            add(each, thesaurus);
        }
    }
}


