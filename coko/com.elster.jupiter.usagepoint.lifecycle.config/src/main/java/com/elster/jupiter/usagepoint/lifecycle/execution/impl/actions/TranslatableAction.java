package com.elster.jupiter.usagepoint.lifecycle.execution.impl.actions;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.usagepoint.lifecycle.execution.ExecutableMicroAction;
import com.elster.jupiter.usagepoint.lifecycle.execution.impl.MicroCategoryTranslationKeys;

import javax.inject.Inject;

public abstract class TranslatableAction implements ExecutableMicroAction {
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
        return this.thesaurus.getString(MicroActionTranslationKeys.Keys.NAME_PREFIX + getKey(), getKey());
    }

    @Override
    public String getDescription() {
        return this.thesaurus.getString(MicroActionTranslationKeys.Keys.DESCRIPTION_PREFIX + getKey(), getKey());
    }

    @Override
    public String getCategoryName() {
        return this.thesaurus.getString(MicroCategoryTranslationKeys.Keys.NAME_PREFIX + getCategory(), getCategory());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TranslatableAction that = (TranslatableAction) o;
        return getKey() != null ? getKey().equals(that.getKey()) : that.getKey() == null;

    }

    @Override
    public int hashCode() {
        return getKey() != null ? getKey().hashCode() : 0;
    }
}
