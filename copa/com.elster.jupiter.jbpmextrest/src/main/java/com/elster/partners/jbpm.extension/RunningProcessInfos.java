/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;


import java.util.ArrayList;
import java.util.List;

public class RunningProcessInfos {

    public int total;

    public List<RunningProcessInfo> processInstances = new ArrayList<RunningProcessInfo>();

    public RunningProcessInfos(){

    }

    public RunningProcessInfos(List<Object[]> list){
        addAll(list);
    }

    private void addAll(List<Object[]> list){
        for(Object[] obj: list){
            add(obj);
        }
    }

    public RunningProcessInfo add(Object[] object){
        RunningProcessInfo runningProcessInfo = new RunningProcessInfo(object);
        processInstances.add(runningProcessInfo);
        total++;
        return runningProcessInfo;
    }

    public void removeLast(int total){
        if(processInstances.size() > 0) {
            processInstances.remove(processInstances.get(processInstances.size() - 1));
            this.total = total;
        }
    }

    public void setTotal(int total){
        this.total = total;
    }
}
