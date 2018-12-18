/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.partners.jbpm.extension;

import java.util.ArrayList;
import java.util.List;

public class ProcessHistoryGenInfos {

    public int total;

    public List<ProcessHistoryGenInfo> processHistories = new ArrayList<ProcessHistoryGenInfo>();

    public ProcessHistoryGenInfos(){

    }

    public ProcessHistoryGenInfos(List<Object[]> list){
        addAll(list);
    }

    private void addAll(List<Object[]> list){
        for(Object[] obj: list){
            add(obj);
        }
    }

    public ProcessHistoryGenInfo add(Object[] object){
        ProcessHistoryGenInfo processHistoryGenInfo = new ProcessHistoryGenInfo(object);
        processHistories.add(processHistoryGenInfo);
        total++;
        return processHistoryGenInfo;
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

