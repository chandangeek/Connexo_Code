package com.energyict.mdc.issue.datacollection.impl.templates.params;

import com.elster.jupiter.issue.share.cep.NoParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterControl;
import com.elster.jupiter.issue.share.cep.controls.SimpleControl;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;

public class AutoResolutionParameter extends TranslatedParameter {

    public static final String AUTO_RESOLUTION_PARAMETER_KEY = "autoResolution";
    private static final ParameterConstraint CONSTRAINT = new NoParameterConstraint();

    public AutoResolutionParameter(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public String getKey() {
        return AUTO_RESOLUTION_PARAMETER_KEY;
    }

    @Override
    public ParameterControl getControl() {
        return SimpleControl.CHECKBOX_FIELD;
    }

    @Override
    public ParameterConstraint getConstraint() {
        return CONSTRAINT;
    }

    @Override
    public Object getDefaultValue() {
        return Boolean.TRUE;
    }

    @Override
    public String getLabel() {
        return getString(MessageSeeds.PARAMETER_AUTO_RESOLUTION);
    }
}
