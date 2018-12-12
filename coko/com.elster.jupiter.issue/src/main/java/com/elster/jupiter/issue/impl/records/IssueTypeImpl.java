/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

public final class IssueTypeImpl extends EntityImpl implements IssueType {
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String key;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 80, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String translationKey;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 3, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String prefix;

    private final Thesaurus thesaurus;

    @Inject
    public IssueTypeImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel);
        this.thesaurus = thesaurus;
    }

    public IssueTypeImpl init(String key, TranslationKey translationKey, String prefix) {
        this.key = key;
        this.prefix = prefix;
        if (translationKey != null) {
            this.translationKey = translationKey.getKey();
        }
        return this;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(new SimpleTranslationKey(this.translationKey, this.translationKey)).format();
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IssueTypeImpl that = (IssueTypeImpl) o;

        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
