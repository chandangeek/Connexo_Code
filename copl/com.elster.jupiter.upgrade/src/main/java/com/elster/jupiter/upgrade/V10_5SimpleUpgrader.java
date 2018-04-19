/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.upgrade;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.util.Map;

public class V10_5SimpleUpgrader implements Upgrader {
    public static final Version VERSION = Version.version(10, 5);
    public static final Map<Version, Class<? extends Upgrader>> V10_5_UPGRADER = ImmutableMap.of(VERSION, V10_5SimpleUpgrader.class);
    private final DataModel dataModel;

    @Inject
    public V10_5SimpleUpgrader(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        dataModelUpgrader.upgrade(dataModel, VERSION);
    }
}
