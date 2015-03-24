package com.elster.jupiter.validation.rest;



import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.DataValidationTask;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
public class DataValidationTaskInfos {
    public int total;
    public List<DataValidationTaskInfo> dataValidationTasks = new ArrayList<>();

    public DataValidationTaskInfos(){}
    public DataValidationTaskInfos(Iterable<? extends DataValidationTask> sets, Thesaurus thesaurus){
        addAll(sets, thesaurus);
    }

    public DataValidationTaskInfo add(DataValidationTask ruleSet, Thesaurus thesaurus) {
        DataValidationTaskInfo result = new DataValidationTaskInfo(ruleSet, thesaurus);
        dataValidationTasks.add(result);
        total++;
        return result;
    }


    public void addAll(Iterable<? extends DataValidationTask> sets, Thesaurus thesaurus) {
        for (DataValidationTask each : sets) {
            add(each, thesaurus);
        }
    }
}
