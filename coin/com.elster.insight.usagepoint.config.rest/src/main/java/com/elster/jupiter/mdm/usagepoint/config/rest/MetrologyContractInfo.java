package com.elster.jupiter.mdm.usagepoint.config.rest;

import com.elster.jupiter.mdm.usagepoint.config.rest.impl.ReadingTypeDeliverablesInfo;
import com.elster.jupiter.mdm.usagepoint.config.rest.impl.ValidationRuleSetInfo;
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

    public void addValidationTasks(List<DataValidationTaskInfo> validationTaskInfos) {
        this.validationTasks = validationTaskInfos;
    }

    public void addValidationRuleSets(List<ValidationRuleSetInfo> validationRuleSets) {
        this.validationRuleSets = validationRuleSets;
    }
}


