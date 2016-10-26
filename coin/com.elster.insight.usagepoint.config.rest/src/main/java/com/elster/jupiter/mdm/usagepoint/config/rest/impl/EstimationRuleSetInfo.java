package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationVersionStatus;
import com.elster.jupiter.validation.rest.ValidationRuleSetVersionInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Optional;

@XmlRootElement
public class EstimationRuleSetInfo {
    public long id;
    public String name;
    public String description;

    public EstimationRuleSetInfo(EstimationRuleSet estimationRuleSet) {
        id = estimationRuleSet.getId();
        name = estimationRuleSet.getName();
        description = estimationRuleSet.getDescription();
    }

    public EstimationRuleSetInfo() {
    }
}
