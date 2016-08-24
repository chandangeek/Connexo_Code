package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.properties.PropertyValueInfoService;
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
    public List<PropertyInfo> properties = new ArrayList<>();
    public List<ReadingTypeInfo> readingTypes = new ArrayList<>();
    public EstimationRuleSetInfo ruleSet;
    public long version;
    public EstimationRuleSetInfo parent = new EstimationRuleSetInfo();

    public EstimationRuleInfo(EstimationRule estimationRule, PropertyValueInfoService propertyValueInfoService) {
        id = estimationRule.getId();
        active = estimationRule.isActive();
        implementation = estimationRule.getImplementation();
        displayName = estimationRule.getDisplayName();
        name = estimationRule.getName();
        deleted = estimationRule.isObsolete();
        EstimationRuleSet ruleSet = estimationRule.getRuleSet();
        this.ruleSet = new EstimationRuleSetInfo(ruleSet);
        properties = propertyValueInfoService.getPropertyInfos(estimationRule.getPropertySpecs(), estimationRule.getProps());
        readingTypes.addAll(estimationRule.getReadingTypes().stream().map(ReadingTypeInfo::new).collect(Collectors.toList()));
        version = estimationRule.getVersion();
        parent.id = ruleSet.getId();
        parent.version = ruleSet.getVersion();
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
