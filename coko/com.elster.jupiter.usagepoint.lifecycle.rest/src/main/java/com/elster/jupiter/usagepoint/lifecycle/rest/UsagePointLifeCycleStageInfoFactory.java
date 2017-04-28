/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import javax.inject.Inject;

public class UsagePointLifeCycleStageInfoFactory {
    private final Thesaurus thesaurus;

    @Inject
    public UsagePointLifeCycleStageInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public IdWithNameInfo from(Stage stage) {
        IdWithNameInfo info = new IdWithNameInfo();
        info.id = stage.getName();
        info.name = thesaurus.getString(stage.getName(), stage.getName());

        return info;
    }
}
