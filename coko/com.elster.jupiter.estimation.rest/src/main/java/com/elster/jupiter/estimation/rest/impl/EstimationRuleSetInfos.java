/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
public class EstimationRuleSetInfos {

	public int total;
	public List<EstimationRuleSetInfo> ruleSets = new ArrayList<>();
	
	public EstimationRuleSetInfos() {
    }

    public EstimationRuleSetInfos(Iterable<? extends EstimationRuleSet> sets) {
	    addAll(sets);
	}

    public EstimationRuleSetInfo add(EstimationRuleSet ruleSet) {
        EstimationRuleSetInfo result = new EstimationRuleSetInfo(ruleSet);
        ruleSets.add(result);
	    total++;
	    return result;
	}

    public void addAll(Iterable<? extends EstimationRuleSet> sets) {
	    for (EstimationRuleSet each : sets) {
	        add(each);
	    }
	}
}


