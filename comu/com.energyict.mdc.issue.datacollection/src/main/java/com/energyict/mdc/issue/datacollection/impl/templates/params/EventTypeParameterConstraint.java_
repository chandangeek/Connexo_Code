package com.energyict.mdc.issue.datacollection.impl.templates.params;

import com.elster.jupiter.issue.share.cep.ParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterViolation;
import com.elster.jupiter.metering.MeteringService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;

import java.util.ArrayList;
import java.util.List;

public class EventTypeParameterConstraint implements ParameterConstraint {
    @Override
    public boolean isOptional() {
        return false;
    }

    @Override
    public String getRegexp() {
        return null;
    }

    @Override
    public Integer getMin() {
        return null;
    }

    @Override
    public Integer getMax() {
        return null;
    }

    @Override
    public List<ParameterViolation> validate(String value, String paramKey) {
        throw new IllegalAccessError("This method shouldn't be called!");
    }

    public List<ParameterViolation> validate(String value, String paramKey, MeteringService meteringService, boolean isAggreagtion) {
        List<ParameterViolation> errors = new ArrayList<ParameterViolation>();
        if (value == null) {
            errors.add(new ParameterViolation(paramKey, MessageSeeds.ISSUE_CREATION_RULE_PARAMETER_ABSENT.getKey(), IssueDataCollectionService.COMPONENT_NAME));
            return errors;
        }
        if (!validateValueInEventDescriptions(value, isAggreagtion)) {
            errors.add(new ParameterViolation(paramKey, MessageSeeds.ISSUE_CREATION_RULE_PARAMETER_INCORRECT.getKey(), IssueDataCollectionService.COMPONENT_NAME, value));
            return errors;
        }
        return errors;
    }

    private boolean validateValueInEventDescriptions(String value, boolean isAggregation) {
        for (DataCollectionEventDescription eventDescription : DataCollectionEventDescription.values()) {
            if ((!isAggregation || eventDescription.canBeAggregated())
                    && eventDescription.getUniqueKey().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
