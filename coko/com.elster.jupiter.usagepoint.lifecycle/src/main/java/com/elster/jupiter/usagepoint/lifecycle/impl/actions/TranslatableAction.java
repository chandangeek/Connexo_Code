package com.elster.jupiter.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroAction;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroActionException;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategoryTranslationKeys;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Map;

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
    public String getKey() {
        return this.getClass().getSimpleName();
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
    public void execute(UsagePoint usagePoint, Instant transitionTime, Map<String, Object> properties) throws ExecutableMicroActionException {
        try {
            doExecute(usagePoint, transitionTime, properties);
        } catch (Exception ex) {
            throw new ExecutableMicroActionException(this, ex.getLocalizedMessage());
        }
    }

    protected abstract void doExecute(UsagePoint usagePoint, Instant transitionTime, Map<String, Object> properties);

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
