package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.templates.DataExportTaskTpl;

public class CreateDeliverDataSetupCommand {

    public void run(){
        Builders.from(DataExportTaskTpl.NORTH_REGION).get();
        Builders.from(DataExportTaskTpl.SOUTH_REGION).get();
    }
}
