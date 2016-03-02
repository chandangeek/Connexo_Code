package com.elster.partners.jbpm.extension;

import org.codehaus.jackson.annotate.JsonProperty;
import org.jbpm.kie.services.api.RuntimeDataService;
import org.jbpm.kie.services.impl.model.ProcessAssetDesc;

import java.util.List;


public class TaskSummaryList {

    private int total = 0;
    private List<TaskSummary> tasks;

    public TaskSummaryList(RuntimeDataService runtimeDataService, List<TaskSummary> tasks) {
        for (TaskSummary summary : tasks) {
            ProcessAssetDesc process = runtimeDataService.getProcessById(summary.getProcessName());
            if (process != null) {
                summary.setProcessName(process.getName());
            }
        }
        this.tasks = tasks;
        this.total = tasks == null ? 0 : tasks.size();
    }

    public List<TaskSummary> getTasks() {
        return tasks;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total){
        this.total = total;
    }

    public void removeLast(int total){
        if(tasks.size() > 0) {
            tasks.remove(tasks.get(tasks.size() - 1));
            this.total = total;
        }
    }
}
