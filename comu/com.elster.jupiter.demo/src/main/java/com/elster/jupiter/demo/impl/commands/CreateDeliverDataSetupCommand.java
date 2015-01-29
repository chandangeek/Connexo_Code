package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.builders.DataExportTaskBuilder;

// TODO
public class CreateDeliverDataSetupCommand {

    public void run(){
        Builders.from(DataExportTaskBuilder.class).withName(Constants.DataExportTask.DEFAULT_PREFIX + Constants.DeviceGroup.NORTH_REGION)
                .withGroup(Constants.DeviceGroup.NORTH_REGION).get();
        Builders.from(DataExportTaskBuilder.class).withName(Constants.DataExportTask.DEFAULT_PREFIX + Constants.DeviceGroup.SOUTH_REGION)
                .withGroup(Constants.DeviceGroup.SOUTH_REGION).get();
    }
}
