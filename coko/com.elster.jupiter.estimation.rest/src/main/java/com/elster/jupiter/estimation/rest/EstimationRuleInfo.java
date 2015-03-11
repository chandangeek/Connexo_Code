package com.elster.jupiter.estimation.rest;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;

import java.util.*;

public class EstimationRuleInfo {

    public long id;
    public boolean active;
    public boolean deleted;
    public String implementation; //validator classname
    public String displayName; // readable name
    public String name;
    public int position;
    public List<PropertyInfo> properties = new ArrayList<PropertyInfo>();
    public List<ReadingTypeInfo> readingTypes = new ArrayList<ReadingTypeInfo>();
    public EstimationRuleSetInfo ruleSet;

    public EstimationRuleInfo(EstimationRule estimationRule) {
        id = estimationRule.getId();
        active = estimationRule.isActive();
        implementation = estimationRule.getImplementation();
        displayName = estimationRule.getDisplayName();
        name = estimationRule.getName();
        deleted = estimationRule.isObsolete();
        ruleSet = new EstimationRuleSetInfo(estimationRule.getRuleSet());
        properties = new PropertyUtils().convertPropertySpecsToPropertyInfos(estimationRule.getPropertySpecs(), estimationRule.getProps());
        for (ReadingType readingType : estimationRule.getReadingTypes()) {
            readingTypes.add(new ReadingTypeInfo(readingType));
        }
    }

    public static List<EstimationRuleInfo> from(List<EstimationRule> estimationRules) {
        List<EstimationRuleInfo> infos = new ArrayList<>(estimationRules.size());
        for (EstimationRule validationRule : estimationRules) {
            infos.add(new EstimationRuleInfo(validationRule));
        }
        return infos;
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
