package com.energyict.mdc.issue.datacollection.impl.templates.params;

import com.elster.jupiter.issue.share.cep.NumberParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterControl;
import com.elster.jupiter.issue.share.cep.ParameterDefinition;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;

public class TrendPeriodParameter extends TranslatedParameter {
    private static final ParameterConstraint CONSTRAINT = new NumberParameterConstraint(false, 1, 7);
    private final ParameterControl control = new ParameterControl() {
        public ParameterDefinition getUnitParameter() {
            return new TrendPeriodUnitParameter(TrendPeriodParameter.this.getThesaurus());
        }

        @Override
        public String getXtype() {
            return "trendPeriodControl";
        }
    };

    public TrendPeriodParameter(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public String getKey() {
        return "trendPeriod";
    }

    @Override
    public ParameterControl getControl() {
        return control;
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
