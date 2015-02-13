package com.energyict.mdc.issue.datacollection.impl.templates.params;

import com.elster.jupiter.issue.share.cep.NumberParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterControl;
import com.elster.jupiter.issue.share.cep.ParameterViolation;
import com.elster.jupiter.issue.share.cep.controls.SimpleControl;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;

import java.util.ArrayList;
import java.util.List;

public class ThresholdParameter extends TranslatedParameter {
    private static final ParameterConstraint CONSTRAINT = new NumberParameterConstraint(false, 0, 100) {
        @Override
        public List<ParameterViolation> validate(String value, String paramKey) {
            List<ParameterViolation> errors = new ArrayList<>();
            double decimalValue = 0;
            try {
                decimalValue = Double.parseDouble(value);
            } catch (NumberFormatException ex) {
                errors.add(new ParameterViolation(paramKey, MessageSeeds.ISSUE_CREATION_RULE_THRESHOLD_PARAMETER_INCORRECT.getKey(), IssueService.COMPONENT_NAME, getMin(), getMax()));
                return errors;
            }
            if ((decimalValue != 0 || !isOptional()) && (decimalValue > getMax() || decimalValue < getMin())) {
                errors.add(new ParameterViolation(paramKey, MessageSeeds.ISSUE_CREATION_RULE_THRESHOLD_PARAMETER_INCORRECT.getKey(), IssueService.COMPONENT_NAME, getMin(), getMax()));
            }
            return errors;
        }
    };

    public ThresholdParameter(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public String getKey() {
        return "threshold";
    }

    @Override
    public ParameterControl getControl() {
        return SimpleControl.NUMBER_FIELD;
    }

    @Override
    public ParameterConstraint getConstraint() {
        return CONSTRAINT;
    }

    @Override
    public String getLabel() {
        return getString(MessageSeeds.PARAMETER_NAME_THRESHOLD);
    }

    @Override
    public String getSuffix() {
        return getString(MessageSeeds.PARAMETER_NAME_THRESHOLD_SUFFIX);
    }
}