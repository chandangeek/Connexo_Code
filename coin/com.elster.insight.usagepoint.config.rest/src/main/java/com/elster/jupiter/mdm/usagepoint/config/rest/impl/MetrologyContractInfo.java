package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.validation.rest.DataValidationTaskInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetrologyContractInfo {
    public long id;
    public String name;
    public boolean mandatory;
    public Long version;
    public List<ReadingTypeDeliverablesInfo> readingTypeDeliverables;
    public List<ValidationRuleSetInfo> validationRuleSets;
    public List<DataValidationTaskInfo> validationTasks;

    public MetrologyContractInfo() {
    }

    public MetrologyContractInfo(MetrologyContract metrologyContract) {
        this.id = metrologyContract.getId();
        this.name = metrologyContract.getMetrologyPurpose().getName();
        this.version = metrologyContract.getVersion();
        this.mandatory = metrologyContract.isMandatory();
    }

    public MetrologyContractInfo(MetrologyContract metrologyContract, List<DataValidationTaskInfo> validationTaskInfos) {
        this(metrologyContract);
        this.validationTasks = validationTaskInfos;
    }

    public MetrologyContractInfo(MetrologyContract metrologyContract, List<ValidationRuleSetInfo> validationRuleSets) {
        this(metrologyContract);
        this.validationRuleSets = validationRuleSets;
    }
}


