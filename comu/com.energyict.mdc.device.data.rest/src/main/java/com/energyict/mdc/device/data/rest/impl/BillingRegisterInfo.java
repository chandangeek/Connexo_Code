package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.energyict.mdc.device.data.BillingReading;
import com.energyict.mdc.device.data.BillingRegister;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public class BillingRegisterInfo extends RegisterInfo<BillingRegister, BillingReading> {
    @JsonProperty("validationStatus")
    public Boolean validationStatus;
    @JsonProperty("dataValidated")
    public Boolean dataValidated;
    @JsonProperty("suspectReason")
    public Set<Map.Entry<ValidationRuleInfo, Long>> suspectReason;
    @JsonProperty("lastChecked")
    public Date lastChecked;

    public BillingRegisterInfo() {
    }

    public BillingRegisterInfo(BillingRegister register, RegisterValidationInfo registerValidationInfo, ValidationEvaluator evaluator) {
        super(register, evaluator);
        this.validationStatus = registerValidationInfo.validationStatus;
        if(this.validationStatus) {
            this.dataValidated = registerValidationInfo.dataValidated;
            this.suspectReason = registerValidationInfo.suspectReason != null ?  registerValidationInfo.suspectReason.entrySet() : null;
            this.lastChecked = registerValidationInfo.lastChecked;
        }
    }
}