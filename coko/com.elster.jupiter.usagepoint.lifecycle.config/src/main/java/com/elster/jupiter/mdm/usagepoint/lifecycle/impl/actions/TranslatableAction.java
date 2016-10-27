package com.elster.jupiter.mdm.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroAction;
import com.elster.jupiter.mdm.usagepoint.lifecycle.impl.MicroCategoryTranslationKeys;
import com.elster.jupiter.nls.Thesaurus;

import javax.inject.Inject;

public abstract class TranslatableAction implements MicroAction {
    private Thesaurus thesaurus;

    protected final Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Inject
    public final void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public String getName() {
        return this.thesaurus.getString(MicroActionTranslationKeys.Keys.NAME_PREFIX + getKey().name(), getKey().name());
    }

    @Override
    public String getDescription() {
        return this.thesaurus.getString(MicroActionTranslationKeys.Keys.DESCRIPTION_PREFIX + getKey().name(), getKey().name());
    }

    @Override
    public String getCategoryName() {
        return this.thesaurus.getString(MicroCategoryTranslationKeys.Keys.NAME_PREFIX + getKey().getCategory().name(), getKey().getCategory().name());
    }
}
