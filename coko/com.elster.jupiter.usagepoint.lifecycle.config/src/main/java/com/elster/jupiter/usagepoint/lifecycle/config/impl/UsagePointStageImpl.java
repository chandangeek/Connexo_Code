package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointStage;

public class UsagePointStageImpl implements UsagePointStage {

    private final UsagePointStage.Stage stage;
    private final Thesaurus thesaurus;

    public UsagePointStageImpl(Stage stage, Thesaurus thesaurus) {
        this.stage = stage;
        this.thesaurus = thesaurus;
    }

    @Override
    public Stage getKey() {
        return this.stage;
    }

    @Override
    public String getDisplayName() {
        return this.thesaurus.getString(TranslationKeys.Keys.STAGE_PREFIX + getKey(), getKey().name());
    }
}