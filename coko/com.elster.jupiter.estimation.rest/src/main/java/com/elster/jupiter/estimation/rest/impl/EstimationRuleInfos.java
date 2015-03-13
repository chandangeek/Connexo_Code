package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationRule;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class EstimationRuleInfos {

    public int total;
    public List<EstimationRuleInfo> rules = new ArrayList<EstimationRuleInfo>();

    EstimationRuleInfos() {
    }

    EstimationRuleInfos(EstimationRule estimationRule) {
        add(estimationRule);
    }

    EstimationRuleInfo add(EstimationRule estimationRule) {
        EstimationRuleInfo result = new EstimationRuleInfo(estimationRule);
        rules.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends EstimationRule> estimationRules) {
        for (EstimationRule each : estimationRules) {
            add(each);
        }
    }
}
