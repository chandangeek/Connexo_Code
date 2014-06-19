package com.elster.jupiter.issue.datacollection.templates.params;

import com.elster.jupiter.issue.datacollection.impl.i18n.MessageSeeds;
import com.elster.jupiter.issue.share.cep.NumberParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterControl;
import com.elster.jupiter.issue.share.cep.controls.SimpleControl;
import com.elster.jupiter.nls.Thesaurus;

public class ThresholdParameter extends TranslatedParameter {
    private static final ParameterConstraint CONSTRAINT = new NumberParameterConstraint(false, 1, 100);

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