package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.events.EventType;
import com.elster.jupiter.validation.ValidationRuleSet;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ValidationRuleSetInfo {

	public String name;
	public String description;

	public ValidationRuleSetInfo(ValidationRuleSet validationRuleSet) {
        name = validationRuleSet.getName();
        description = validationRuleSet.getDescription();
    }

    public ValidationRuleSetInfo() {
    }

    
    
    

}
