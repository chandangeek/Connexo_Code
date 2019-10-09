/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl.upgrade;

import com.elster.jupiter.orm.OrmService;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;

public class UpgraderV10_6 extends SqlExecuteUpgrader {

    @Inject
    UpgraderV10_6(OrmService ormService) {
        super(ormService);
    }

    @Override
    protected List<String> getSQLStatementsToExecute() {
        return new ArrayList<>();
    }
}