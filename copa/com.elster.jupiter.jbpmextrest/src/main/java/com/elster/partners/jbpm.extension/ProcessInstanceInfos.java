/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;


import java.util.ArrayList;
import java.util.List;

public class ProcessInstanceInfos {

    public int total;

    public List<ProcessInstanceInfo> processInstances = new ArrayList<ProcessInstanceInfo>();

    public ProcessInstanceInfos(){

    }

    public ProcessInstanceInfos(List<Object[]> list){
        addAll(list);
    }

    private void addAll(List<Object[]> list){
        for(Object[] obj: list){
            add(obj);
        }
    }

    public ProcessInstanceInfo add(Object[] object){
        ProcessInstanceInfo processInstanceInfo = new ProcessInstanceInfo(object);
        processInstances.add(processInstanceInfo);
        total++;
        return processInstanceInfo;
    }
}
