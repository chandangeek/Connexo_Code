/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

public final class IssueStatusImpl extends EntityImpl implements IssueStatus{
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String key;

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String translationKey;

    private boolean isHistorical;

    private final Thesaurus thesaurus;

    @Inject
    public IssueStatusImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel);
        this.thesaurus = thesaurus;
    }

    public IssueStatusImpl init(String key, boolean isHistorical, TranslationKey translationKey){
        this.key = key;
        this.isHistorical = isHistorical;
        if (translationKey != null) {
            this.translationKey = translationKey.getKey();
        }
        return this;
    }

    @Override
    @Deprecated
    public long getId() {
        return getKey().hashCode();
    }

    @Override
    public String getKey() {
        return key;
    }

    public String getName() {
        return thesaurus.getFormat(new SimpleTranslationKey(this.translationKey, this.translationKey)).format();
    }

    public boolean isHistorical() {
        return isHistorical;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IssueStatusImpl that = (IssueStatusImpl) o;

        return this.key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
