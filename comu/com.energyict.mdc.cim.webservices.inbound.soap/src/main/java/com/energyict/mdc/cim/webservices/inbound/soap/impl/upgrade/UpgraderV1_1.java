
/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl.upgrade;

import com.elster.jupiter.orm.OrmService;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.List;


public class UpgraderV1_1 extends SqlExecuteUpgrader {

    @Inject
    UpgraderV1_1(OrmService ormService) {
        super(ormService);
    }

    @Override
    protected List<String> getSQLStatementsToExecute() {
        return Arrays.asList(
                ignoreColumnExistsOrTableDoesNotExist("alter table MCP_SCS_CNT modify (CALLBACK_URL NULL)"));
    }

}
