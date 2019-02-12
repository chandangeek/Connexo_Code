package com.elster.partners.jbpm.extension;

import java.util.ArrayList;
import java.util.List;

public class VariableValueInfos {
    public int total;

    public List<VariableValueInfo> variableValues = new ArrayList<VariableValueInfo>();

    public VariableValueInfos(){

    }

    public VariableValueInfos(List<Object[]> list){
        addAll(list);
    }

    private void addAll(List<Object[]> list){
        for(Object[] obj: list){
            add(obj);
        }
    }

    public VariableValueInfo add(Object[] object){
        VariableValueInfo variableValueInfo = new VariableValueInfo(object);
        variableValues.add(variableValueInfo);
        total++;
        return variableValueInfo;
    }

    public void removeLast(int total){
        if(variableValues.size() > 0) {
            variableValues.remove(variableValues.get(variableValues.size() - 1));
            this.total = total;
        }
    }

    public void setTotal(int total){
        this.total = total;
    }
}
