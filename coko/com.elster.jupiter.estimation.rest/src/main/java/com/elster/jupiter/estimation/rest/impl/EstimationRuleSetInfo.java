package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.rest.PropertyUtils;

import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@XmlRootElement
public class EstimationRuleSetInfo {

    public long id;
	public String name;
	public String description;
    public int numberOfInactiveRules;
    public int numberOfRules;
    public List<EstimationRuleInfo> rules;

	public EstimationRuleSetInfo(EstimationRuleSet estimationRuleSet) {
        id = estimationRuleSet.getId();
        name = estimationRuleSet.getName();
        description = estimationRuleSet.getDescription();
        numberOfRules = estimationRuleSet.getRules().size();
        numberOfInactiveRules = 0;
        for (EstimationRule rule : estimationRuleSet.getRules()) {
            if (!rule.isActive()) {
                numberOfInactiveRules++;
            }
        }
    }

    public static EstimationRuleSetInfo withRules(EstimationRuleSet estimationRuleSet, PropertyUtils propertyUtils) {
        EstimationRuleSetInfo estimationRuleSetInfo = new EstimationRuleSetInfo(estimationRuleSet);
        estimationRuleSetInfo.rules = new ArrayList<>();
        estimationRuleSet.getRules().stream()
                .map(rule -> new EstimationRuleInfo(rule, propertyUtils))
                .forEach(estimationRuleSetInfo.rules::add);
        return estimationRuleSetInfo;
    }

    public EstimationRuleSetInfo() {
    }

    public static Comparator<EstimationRuleSetInfo> ESTIMATION_RULESET_NAME_COMPARATOR
            = new Comparator<EstimationRuleSetInfo>() {

        public int compare(EstimationRuleSetInfo ruleset1, EstimationRuleSetInfo ruleset2) {
            if(ruleset1 == null || ruleset1.name == null || ruleset2 == null || ruleset2.name == null) {
                throw new IllegalArgumentException("Ruleset information is missed");
            }
            return ruleset1.name.compareToIgnoreCase(ruleset2.name);
        }
    };
}
