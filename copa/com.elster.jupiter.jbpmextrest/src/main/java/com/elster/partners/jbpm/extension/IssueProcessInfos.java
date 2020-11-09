/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;


import java.util.ArrayList;
import java.util.List;

public class IssueProcessInfos {

    public int total;

    public List<IssueProcessInfo> processInstances = new ArrayList<IssueProcessInfo>();

    public IssueProcessInfos(){

    }

    public IssueProcessInfos(List<Object[]> list){
        addAll(list);
    }

    private void addAll(List<Object[]> list){
        for(Object[] obj: list){
            add(obj);
        }
    }

    public IssueProcessInfo add(Object[] object){
        IssueProcessInfo issueProcessInfo = new IssueProcessInfo(object);
        processInstances.add(issueProcessInfo);
        total++;
        return issueProcessInfo;
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
