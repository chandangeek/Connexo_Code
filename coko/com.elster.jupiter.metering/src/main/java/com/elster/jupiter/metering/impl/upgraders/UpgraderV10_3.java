/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.upgraders;

import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;

/**
 * Upgrades to release 10.3.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-17 (17:29)
 */
public class UpgraderV10_3 implements Upgrader {

    private final ServerMeteringService meteringService;
    private final TimeService timeService;

    @Inject
    public UpgraderV10_3(ServerMeteringService meteringService, TimeService timeService) {
        this.meteringService = meteringService;
        this.timeService = timeService;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        GasDayRelativePeriodCreator.createAll(this.meteringService, this.timeService);
    }

}