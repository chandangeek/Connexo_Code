package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.groups.SearchCriteria;

import java.util.ArrayList;
import java.util.List;

public class SearchCriteriaInfo {

    public String criteriaName;
    public List<Object> criteriaValues = new ArrayList<Object>();

    public SearchCriteriaInfo() {}

    public SearchCriteriaInfo(SearchCriteria searchCriteria) {
        this.criteriaName = searchCriteria.getCriteriaName();
        this.criteriaValues = searchCriteria.getCriteriaValues();
    }

}