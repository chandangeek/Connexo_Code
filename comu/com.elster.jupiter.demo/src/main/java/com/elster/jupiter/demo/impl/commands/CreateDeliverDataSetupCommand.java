/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.templates.DataExportTaskTpl;

public class CreateDeliverDataSetupCommand extends CommandWithTransaction {

    public void run(){
        Builders.from(DataExportTaskTpl.NORTH_REGION).get();
        Builders.from(DataExportTaskTpl.SOUTH_REGION).get();
    }
}
