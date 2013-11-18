package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.util.time.UtcInstant;
import com.elster.jupiter.validation.ValidationRuleSet;

import java.util.List;

class MeterActivationValidation {

    private long id;
    private transient MeterActivation meterActivation;
    private long ruleSetId;
    private transient ValidationRuleSet ruleSet;
    private UtcInstant lastRun;
    private List<ChannelValidation> channelValidations;

    private MeterActivationValidation() {

    }

    public MeterActivationValidation(MeterActivation meterActivation) {
        id = meterActivation.getId();
        this.meterActivation = meterActivation;
    }

    MeterActivation getMeterActivation() {
        return meterActivation;
    }

    long getId() {
        return id;
    }
}
