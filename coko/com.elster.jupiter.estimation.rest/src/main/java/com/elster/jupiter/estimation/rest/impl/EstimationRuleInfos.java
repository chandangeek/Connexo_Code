/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class EstimationRuleInfos {

    public int total;
    public List<EstimationRuleInfo> rules = new ArrayList<EstimationRuleInfo>();

    // required for serialization
    public EstimationRuleInfos() {
    }

    public void add(EstimationRuleInfo ruleInfo) {
        rules.add(ruleInfo);
        total++;
    }
}
