package com.elster.jupiter.metering.groups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchCriteria {

    private String criteriaName;
    private List<Object> criteriaValues = new ArrayList<>();

    public SearchCriteria(String criteriaName, List<Object> criteriaValues) {
        this.criteriaName = criteriaName;
        this.criteriaValues.addAll(criteriaValues);
    }

    public String getCriteriaName() {
        return criteriaName;
    }

    public List<Object> getCriteriaValues() {
        return Collections.unmodifiableList(criteriaValues);
    }

    public void setCriteriaName(String criteriaName) {
        this.criteriaName = criteriaName;
    }

    public void setCriteriaValues(List<Object> criteriaValues) {
        this.criteriaValues.clear();
        this.criteriaValues.addAll(criteriaValues);
    }

}