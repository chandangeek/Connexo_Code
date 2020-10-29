/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;


import java.util.ArrayList;
import java.util.List;

public class ProcessInstanceLogInfos {

    public int total;

    public List<ProcessInstanceLogInfo> processInstanceLogs = new ArrayList<>();

    public ProcessInstanceLogInfos(){}

    public ProcessInstanceLogInfos(List<Object[]> list){
        addAll(list);
    }

    private void addAll(List<Object[]> list){
        for(Object[] obj: list){
            add(obj);
        }
    }

    public ProcessInstanceLogInfo add(Object[] object){
        ProcessInstanceLogInfo processInstanceLogInfo = new ProcessInstanceLogInfo(object);
        processInstanceLogs.add(processInstanceLogInfo);
        total++;
        return processInstanceLogInfo;
    }
}
