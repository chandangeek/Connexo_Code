/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands.devices;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.commands.ActivateDevicesCommand;
import com.elster.jupiter.demo.impl.templates.DeviceConfigurationTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;

import javax.inject.Provider;
import java.util.Collections;


public class CreateDataLoggerSlaveCommand extends CreateDeviceCommand {

    private Provider<ActivateDevicesCommand> activeLifeCyclestatePostBuilder;

    public CreateDataLoggerSlaveCommand() {
        super();
        setConfigurationTpl(DeviceConfigurationTpl.DATA_LOGGER_SLAVE);
    }

    public void setActiveLifeCyclestatePostBuilder(Provider<ActivateDevicesCommand> activeLifeCyclestatePostBuilder) {
        this.activeLifeCyclestatePostBuilder = activeLifeCyclestatePostBuilder;
    }

    public void run() {
        this.setDeviceTypeTpl(DeviceTypeTpl.EIMETER_FLEX);
        super.run();
        if (this.activeLifeCyclestatePostBuilder != null) {
            this.activeLifeCyclestatePostBuilder.get()
                    .setDevices(Collections.singletonList(Builders.from(DeviceBuilder.class).withName(getDeviceName()).get()))
                    .run();
        }
    }
}
