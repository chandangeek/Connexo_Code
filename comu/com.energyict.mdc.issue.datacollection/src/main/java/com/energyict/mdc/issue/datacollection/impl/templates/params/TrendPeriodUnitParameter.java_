package com.energyict.mdc.issue.datacollection.impl.templates.params;

import com.elster.jupiter.issue.share.cep.NumberParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterControl;
import com.elster.jupiter.issue.share.cep.controls.ComboBoxControl;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.issue.datacollection.impl.TrendPeriodUnit;

import java.util.ArrayList;
import java.util.List;

public class TrendPeriodUnitParameter extends TranslatedParameter {
    private static final ParameterConstraint CONSTRAINT = new NumberParameterConstraint();

    private List<Object> values;

    public TrendPeriodUnitParameter(Thesaurus thesaurus) {
        super(thesaurus);
        values = new ArrayList<>(TrendPeriodUnit.values().length);
        for (TrendPeriodUnit trendPeriodUnit : TrendPeriodUnit.values()) {
            ComboBoxControl.Values info = new ComboBoxControl.Values();
            info.id = String.valueOf(trendPeriodUnit.getId());
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
        return "";
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
