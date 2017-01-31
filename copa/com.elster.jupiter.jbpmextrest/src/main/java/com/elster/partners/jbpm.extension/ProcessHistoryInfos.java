/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;


import java.util.ArrayList;
import java.util.List;

public class ProcessHistoryInfos {

    public int total;

    public List<ProcessHistoryInfo> processHistories = new ArrayList<ProcessHistoryInfo>();

    public ProcessHistoryInfos(){

    }

    public ProcessHistoryInfos(List<Object[]> list){
        addAll(list);
    }

    private void addAll(List<Object[]> list){
        for(Object[] obj: list){
            add(obj);
        }
    }

    public ProcessHistoryInfo add(Object[] object){
        ProcessHistoryInfo processHistoryInfo = new ProcessHistoryInfo(object);
        processHistories.add(processHistoryInfo);
        total++;
        return processHistoryInfo;
    }

    public void removeLast(int total){
        if(processHistories.size() > 0) {
            processHistories.remove(processHistories.get(processHistories.size() - 1));
            this.total = total;
        }
    }

    public void setTotal(int total){
        this.total = total;
    }
}
