package com.elster.jupiter.issue.share.cep;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.service.IssueService;

import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.util.Checks.is;

public class StringParameterConstraint implements ParameterConstraint {

    private boolean optional = true;
    private Integer minLength = 5;
    private Integer maxLength = 100;
    private String regexp;

    public StringParameterConstraint(boolean optional, Integer minLength, Integer maxLength) {
        this.optional = optional;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }

    @Override
    public String getRegexp() {
        return regexp;
    }

    @Override
    public Integer getMin() {
        return minLength;
    }

    @Override
    public Integer getMax() {
        return maxLength;
    }

    @Override
    public List<ParameterViolation> validate(String value, String paramKey) {
        List<ParameterViolation> errors = new ArrayList<>();
        boolean empty = is(value).emptyOrOnlyWhiteSpace();
        boolean outsideLimits = value.length() < minLength || value.length() > maxLength;
        if( !optional && (empty || outsideLimits) || optional && !empty && outsideLimits) {
            Object[] args = new Object[] {minLength, maxLength};
            errors.add(new ParameterViolation(paramKey, MessageSeeds.ISSUE_CREATION_RULE_INVALID_SRTING_PARAMETER.getKey(), IssueService.COMPONENT_NAME, args));
        }
        return errors;
    }
}
