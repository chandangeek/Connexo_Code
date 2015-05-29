package com.energyict.mdc.issue.datacollection.impl.templates.params;

import com.elster.jupiter.issue.share.cep.AbstractParameterDefinition;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.issue.datacollection.impl.i18n.MessageSeeds;

public abstract class TranslatedParameter extends AbstractParameterDefinition {
    private final Thesaurus thesaurus;

    protected TranslatedParameter(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected String getString(MessageSeeds seed) {
        if (seed != null) {
            return getThesaurus().getString(seed.getKey(), seed.getDefaultFormat());
        }
        return "";
    }
}
