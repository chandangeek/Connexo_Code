package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ValidationRuleSetInfo {

    public long id;
	public String name;
	public String description;
    public List<ValidationRuleInfo> rules = new ArrayList<ValidationRuleInfo>();

	public ValidationRuleSetInfo(ValidationRuleSet validationRuleSet) {
        id = validationRuleSet.getId();
        name = validationRuleSet.getName();
        description = validationRuleSet.getDescription();
        for (ValidationRule rule : validationRuleSet.getRules()) {
            rules.add(new ValidationRuleInfo(rule));
        }
    }

    public ValidationRuleSetInfo() {
    }

    
    
    

}
