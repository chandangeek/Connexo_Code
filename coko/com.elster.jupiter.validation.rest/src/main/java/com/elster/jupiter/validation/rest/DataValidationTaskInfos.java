package com.elster.jupiter.validation.rest;



import com.elster.jupiter.validation.DataValidationTask;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
public class DataValidationTaskInfos {
    public int total;
    public List<DataValidationTaskInfo> dataValidationTasks = new ArrayList<>();

    public DataValidationTaskInfos(Iterable<? extends DataValidationTask> sets){
        addAll(sets);
    }

    public DataValidationTaskInfo add(DataValidationTask ruleSet) {
        DataValidationTaskInfo result = new DataValidationTaskInfo(ruleSet);
        dataValidationTasks.add(result);
        total++;
        return result;
    }


    public void addAll(Iterable<? extends DataValidationTask> sets) {
        for (DataValidationTask each : sets) {
            add(each);
        }
    }
}
