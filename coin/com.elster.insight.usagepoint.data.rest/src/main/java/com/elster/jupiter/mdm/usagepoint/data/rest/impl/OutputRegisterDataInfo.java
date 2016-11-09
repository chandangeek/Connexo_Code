package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public class OutputRegisterDataInfo {

    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal value;

    public Instant timeStamp;

    public Instant reportedDateTime;

    public Boolean dataValidated;

    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationResult;

    public ValidationAction action;

    public Set<ValidationRuleInfo> validationRules;

}
