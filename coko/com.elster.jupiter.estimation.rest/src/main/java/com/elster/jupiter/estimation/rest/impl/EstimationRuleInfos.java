package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.rest.PropertyUtils;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class EstimationRuleInfos {

    private final PropertyUtils propertyUtils;
    public int total;
    public List<EstimationRuleInfo> rules = new ArrayList<EstimationRuleInfo>();

    // required for serialization
    public EstimationRuleInfos() {
        this.propertyUtils = null;
    }

    EstimationRuleInfos(PropertyUtils propertyUtils) {
        this.propertyUtils = propertyUtils;
    }

    EstimationRuleInfo add(EstimationRule estimationRule) {
        EstimationRuleInfo result = new EstimationRuleInfo(estimationRule, propertyUtils);
        rules.add(result);
        total++;
        return result;
    }

    public void add(EstimationRuleInfo ruleInfo) {
        rules.add(ruleInfo);
        total++;
    }
}
