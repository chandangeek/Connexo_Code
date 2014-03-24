package com.elster.jupiter.issue.rest.response.rules;

import java.util.ArrayList;
import java.util.List;

public class AssignmentRuleListInfo {

    private List<AssignmentRuleInfo> data;
    private int total;

    public AssignmentRuleListInfo() {
        data = new ArrayList<>();
    }

    public AssignmentRuleListInfo(List<AssignmentRuleInfo> allRules, int start, int limit){
        this();
        if (allRules != null && allRules.size() > 0){
            data.addAll(allRules);
            total = start + allRules.size();
            if (allRules.size() == limit) {
                total++;
            }
        }
    }

    public List<AssignmentRuleInfo> getData() {
        return data;
    }

    public void setData(List<AssignmentRuleInfo> data) {
        this.data = data;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}