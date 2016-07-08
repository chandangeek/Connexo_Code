package com.elster.jupiter.demo.impl.commands.devices;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.builders.device.SetDeviceInActiveLifeCycleStatePostBuilder;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;

import javax.inject.Provider;


public class CreateDataLoggerSlaveCommand extends CreateDeviceCommand {

    private Provider<SetDeviceInActiveLifeCycleStatePostBuilder> activeLifeCyclestatePostBuilder;

    public void setActiveLifeCyclestatePostBuilder(Provider<SetDeviceInActiveLifeCycleStatePostBuilder> activeLifeCyclestatePostBuilder) {
        this.activeLifeCyclestatePostBuilder = activeLifeCyclestatePostBuilder;
    }

    public void run(){
        this.setDeviceTypeTpl(DeviceTypeTpl.EIMETER_FLEX);
        super.run();
        if (this.activeLifeCyclestatePostBuilder != null) {
            this.activeLifeCyclestatePostBuilder.get().accept(Builders.from(DeviceBuilder.class).withMrid(getMrid()).get());
        }
    }
}
