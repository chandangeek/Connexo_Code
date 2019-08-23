/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.share.service.IssueService;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * This validator will check:
 * <ul>
 *     <li>Rule is not null in case of issue is not manual</li>
 * </ul>
 *
 */
public class NotManualIssueRuleValidator implements ConstraintValidator<NotManualIssueRuleIsPresent, IssueImpl> {

    @Override
    public void initialize(NotManualIssueRuleIsPresent annotation) {
        // No need to initialize from the annotation element
    }

    @Override
    public boolean isValid(IssueImpl issue, ConstraintValidatorContext context) {
        if (ruleIsValid(issue)) {
            return true;
        } else {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).addPropertyNode("rule").addConstraintViolation();
            return false;
        }
    }

    private boolean ruleIsValid(IssueImpl issue) {
        if (!issue.getRule().isPresent()) {
            if (issue.getType() != null && IssueService.MANUAL_ISSUE_TYPE.equals(issue.getType().getKey())) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
