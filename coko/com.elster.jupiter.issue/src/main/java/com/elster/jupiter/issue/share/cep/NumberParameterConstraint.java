package com.elster.jupiter.issue.share.cep;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.service.IssueService;

import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.util.Checks.is;

public class NumberParameterConstraint implements ParameterConstraint {

    private boolean optional = true;
    private int min = Integer.MIN_VALUE;
    private int max = Integer.MAX_VALUE;

    public NumberParameterConstraint() {
    }

    public NumberParameterConstraint(boolean optional, int min, int max) {
        this.optional = optional;
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    @Override
    public String getRegexp() {
        return null;
    }

    @Override
    public Integer getMin() {
        return min;
    }

    @Override
    public Integer getMax() {
        return max;
    }

    @Override
    public List<ParameterViolation> validate(String value, String paramKey) {
        List<ParameterViolation> errors = new ArrayList<>();
        int valueInt = 0;
        try {
            valueInt = Integer.parseInt(value);
        }catch (NumberFormatException ex){
            errors.add(new ParameterViolation(paramKey, MessageSeeds.ISSUE_CREATION_RULE_INVALID_NUMBER_PARAMETER.getKey(), IssueService.COMPONENT_NAME, value));
            return errors;
        }
        if((valueInt != 0 || !optional) && (valueInt > max || valueInt < min)) {
            errors.add(new ParameterViolation(paramKey, MessageSeeds.ISSUE_CREATION_RULE_INCORRECT_NUMBER_PARAMETER.getKey(), IssueService.COMPONENT_NAME, min, max));
        }
        return errors;
    }
}
