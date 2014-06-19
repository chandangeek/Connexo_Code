package com.elster.jupiter.issue.datacollection.templates.params;

import com.elster.jupiter.issue.datacollection.impl.TrendPeriodUnit;
import com.elster.jupiter.issue.datacollection.impl.i18n.MessageSeeds;
import com.elster.jupiter.issue.share.cep.NumberParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterControl;
import com.elster.jupiter.issue.share.cep.controls.ComboBoxControl;
import com.elster.jupiter.nls.Thesaurus;

import java.util.ArrayList;
import java.util.List;

public class TrendPeriodUnitParameter extends TranslatedParameter{
    private static final ParameterConstraint CONSTRAINT = new NumberParameterConstraint();

    private List<Object> values;
    public TrendPeriodUnitParameter(Thesaurus thesaurus) {
        super(thesaurus);
        values = new ArrayList<>(TrendPeriodUnit.values().length);
        for (TrendPeriodUnit trendPeriodUnit : TrendPeriodUnit.values()) {
            ComboBoxControl.Values info = new ComboBoxControl.Values();
            info.id = trendPeriodUnit.getId();
            info.title = trendPeriodUnit.getTitle(getThesaurus());
            values.add(info);
        }
    }

    @Override
    public String getKey() {
        return "trendPeriodUnit";
    }

    @Override
    public ParameterControl getControl() {
        return ComboBoxControl.COMBOBOX;
    }

    @Override
    public ParameterConstraint getConstraint() {
        return CONSTRAINT;
    }

    @Override
    public String getLabel() {
        return getString(MessageSeeds.PARAMETER_NAME_TREND_PERIOD_UNIT);
    }

    @Override
    public Object getDefaultValue() {
        return values.get(0);
    }

    @Override
    public List<Object> getDefaultValues() {
        return values;
    }
}
