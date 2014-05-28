package com.elster.jupiter.issue.datacollection.templates.params;

import com.elster.jupiter.issue.datacollection.impl.i18n.MessageSeeds;
import com.elster.jupiter.issue.share.cep.ParameterConstraint;
import com.elster.jupiter.issue.share.cep.ParameterControl;
import com.elster.jupiter.issue.share.cep.StringParameterConstraint;
import com.elster.jupiter.issue.share.cep.controls.SimpleControl;
import com.elster.jupiter.nls.Thesaurus;

public class ReadingTypeParameter extends TranslatedParameter{
    private static final ParameterConstraint CONSTRAINT = new StringParameterConstraint(false, 2, 80);

    private ParameterControl control;
    public ReadingTypeParameter(Thesaurus thesaurus) {
        super(thesaurus);
        ((StringParameterConstraint)CONSTRAINT).setRegexp("(\\d+\\.){17}\\d+");
    }

    @Override
    public String getKey() {
        return "readingType";
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
