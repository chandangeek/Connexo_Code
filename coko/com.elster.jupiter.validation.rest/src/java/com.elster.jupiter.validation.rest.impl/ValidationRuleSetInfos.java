package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.validation.ValidationRuleSet;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
public class ValidationRuleSetInfos {

	public int total;
	public List<ValidationRuleSetInfo> ruleSets = new ArrayList<>();

	ValidationRuleSetInfos() {
	}

	ValidationRuleSetInfos(ValidationRuleSet ruleSet) {
	    add(ruleSet);
	}

	ValidationRuleSetInfos(Iterable<? extends ValidationRuleSet> sets) {
	    addAll(sets);
	}

    ValidationRuleSetInfo add(ValidationRuleSet ruleSet) {
        ValidationRuleSetInfo result = new ValidationRuleSetInfo(ruleSet);
        ruleSets.add(result);
	    total++;
	    return result;
	}

	void addAll(Iterable<? extends ValidationRuleSet> sets) {
	    for (ValidationRuleSet each : sets) {
	        add(each);
	    }
	}


}


