package com.elster.partners.jbpm.extension;


public class TaskBulkReportInfo {

    public long total;
    public long failed;

    public TaskBulkReportInfo(){

    }

    public TaskBulkReportInfo(long total, long failed){
        this.failed = failed;
        this.total = total;
    }

}
