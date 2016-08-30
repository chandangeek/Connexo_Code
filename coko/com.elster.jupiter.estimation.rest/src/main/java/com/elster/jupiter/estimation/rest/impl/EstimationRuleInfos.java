package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class EstimationRuleInfos {

    private final PropertyValueInfoService propertyValueInfoService;
    public int total;
    public List<EstimationRuleInfo> rules = new ArrayList<EstimationRuleInfo>();

    // required for serialization
    public EstimationRuleInfos() {
        this.propertyValueInfoService = null;
    }

    EstimationRuleInfos(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
    }

    EstimationRuleInfo add(EstimationRule estimationRule) {
        EstimationRuleInfo result = new EstimationRuleInfo(estimationRule, propertyValueInfoService);
        rules.add(result);
        total++;
        return result;
    }

    public void add(EstimationRuleInfo ruleInfo) {
        rules.add(ruleInfo);
        total++;
    }
}
