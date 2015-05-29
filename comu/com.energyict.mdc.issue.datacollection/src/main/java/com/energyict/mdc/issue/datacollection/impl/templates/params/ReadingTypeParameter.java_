package com.energyict.mdc.issue.datacollection.impl.templates.params;

import com.elster.jupiter.issue.share.cep.ParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterControl;
import com.elster.jupiter.issue.share.cep.StringParameterConstraint;
import com.elster.jupiter.issue.share.cep.controls.SimpleControl;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;

public class ReadingTypeParameter extends TranslatedParameter {
    public static final String READING_TYPE_PARAMETER_KEY = "readingType";
    private static final ParameterConstraint CONSTRAINT = new StringParameterConstraint(false, 2, 80);

    public ReadingTypeParameter(Thesaurus thesaurus) {
        super(thesaurus);
        ((StringParameterConstraint) CONSTRAINT).setRegexp("(\\d+\\.){17}\\d+");
    }

    @Override
    public String getKey() {
        return READING_TYPE_PARAMETER_KEY;
    }

    @Override
    public ParameterControl getControl() {
        return SimpleControl.TEXT_FIELD;
    }

    @Override
    public ParameterConstraint getConstraint() {
        return CONSTRAINT;
    }

    @Override
    public String getLabel() {
        return getString(MessageSeeds.PARAMETER_NAME_READING_TYPE);
    }

    @Override
    public String getHelp() {
        return getString(MessageSeeds.PARAMETER_NAME_READING_TYPE_DESCRIPTION);
    }
}
