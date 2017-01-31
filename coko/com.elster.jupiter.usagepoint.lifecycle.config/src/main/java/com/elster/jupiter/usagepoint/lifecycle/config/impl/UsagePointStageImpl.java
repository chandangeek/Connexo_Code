/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;

public class UsagePointStageImpl implements UsagePointStage {

    private final Key stage;
    private final Thesaurus thesaurus;

    public UsagePointStageImpl(Key stage, Thesaurus thesaurus) {
        this.stage = stage;
        this.thesaurus = thesaurus;
    }

    @Override
    public Key getKey() {
        return this.stage;
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getString(TranslationKeys.Keys.STAGE_PREFIX + getKey(), getKey().name());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UsagePointStageImpl that = (UsagePointStageImpl) o;

        return stage == that.stage;

    }

    @Override
    public int hashCode() {
        return stage != null ? stage.hashCode() : 0;
    }
}