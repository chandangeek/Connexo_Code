package com.elster.jupiter.issue.datacollection.templates.params;

import com.elster.jupiter.issue.datacollection.impl.i18n.MessageSeeds;
import com.elster.jupiter.issue.share.cep.NumberParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterControl;
import com.elster.jupiter.issue.share.cep.controls.SimpleControl;
import com.elster.jupiter.nls.Thesaurus;

public class TrendPeriodParameter extends TranslatedParameter{
    private static final ParameterConstraint CONSTRAINT = new NumberParameterConstraint(false, 1, 7);

    public TrendPeriodParameter(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public String getKey() {
        return "trendPeriod";
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
    public Object getDefaultValue() {
        return CONSTRAINT.getMin();
    }

    @Override
    public String getLabel() {
        return getString(MessageSeeds.PARAMETER_NAME_TREND_PERIOD);
    }
}
