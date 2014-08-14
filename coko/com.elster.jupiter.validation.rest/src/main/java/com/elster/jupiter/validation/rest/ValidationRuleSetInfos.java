package com.elster.jupiter.validation.rest;

import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
public class ValidationRuleSetInfos {

	public int total;
	public List<ValidationRuleSetInfo> ruleSets = new ArrayList<>();
	
	public ValidationRuleSetInfos() {
    }

    public ValidationRuleSetInfos(Iterable<? extends ValidationRuleSet> sets) {
	    addAll(sets);
	}

    public ValidationRuleSetInfo add(ValidationRuleSet ruleSet) {
        ValidationRuleSetInfo result = new ValidationRuleSetInfo(ruleSet);
        ruleSets.add(result);
	    total++;
	    return result;
	}

    public void addAll(Iterable<? extends ValidationRuleSet> sets) {
	    for (ValidationRuleSet each : sets) {
	        add(each);
	    }
	}
}


