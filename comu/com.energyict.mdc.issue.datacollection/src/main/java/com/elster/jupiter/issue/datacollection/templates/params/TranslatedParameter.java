package com.elster.jupiter.issue.datacollection.templates.params;

import com.elster.jupiter.issue.datacollection.impl.i18n.MessageSeeds;
import com.elster.jupiter.issue.share.cep.AbstractParameterDefenition;
import com.elster.jupiter.nls.Thesaurus;

public abstract class TranslatedParameter extends AbstractParameterDefenition {
    private final Thesaurus thesaurus;

    protected TranslatedParameter(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected String getString(MessageSeeds seed){
        if (seed != null) {
            return getThesaurus().getString(seed.getKey(), seed.getDefaultFormat());
        }
        return "";
    }
}
