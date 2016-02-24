package com.elster.partners.jbpm.extension;


public class TaskBulkRaportInfo {

    public long total;
    public long failed;

    public TaskBulkRaportInfo(){

    }

    public TaskBulkRaportInfo(long total, long failed){
        this.failed = failed;
        this.total = total;
    }

}
