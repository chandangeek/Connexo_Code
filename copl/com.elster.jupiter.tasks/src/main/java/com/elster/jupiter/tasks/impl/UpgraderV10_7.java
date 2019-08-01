/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.tasks.EventType;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;


public class UpgraderV10_7 implements Upgrader {
    private final DataModel dataModel;
    @Inject
    public UpgraderV10_7(DataModel dataModel, EventService eventService) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, Version.version(10, 7));
    }
}