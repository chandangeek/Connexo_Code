package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.rest.PropertyUtils;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EstimationRuleInfo {

    public long id;
    public boolean active;
    public boolean deleted;
    public String implementation; //estimator classname
    public String displayName; // readable name
    public String name;
    public List<PropertyInfo> properties = new ArrayList<PropertyInfo>();
    public List<ReadingTypeInfo> readingTypes = new ArrayList<ReadingTypeInfo>();
    public EstimationRuleSetInfo ruleSet;

    public EstimationRuleInfo(EstimationRule estimationRule, PropertyUtils propertyUtils) {
        id = estimationRule.getId();
        active = estimationRule.isActive();
        implementation = estimationRule.getImplementation();
        displayName = estimationRule.getDisplayName();
        name = estimationRule.getName();
        deleted = estimationRule.isObsolete();
        ruleSet = new EstimationRuleSetInfo(estimationRule.getRuleSet());
        properties = propertyUtils.convertPropertySpecsToPropertyInfos(estimationRule.getPropertySpecs(), estimationRule.getProps());
        readingTypes.addAll(estimationRule.getReadingTypes().stream().map(ReadingTypeInfo::new).collect(Collectors.toList()));
    }

    public EstimationRuleInfo() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return id == ((EstimationRuleInfo) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
