package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.device.config.RegisterGroup;
import com.energyict.mdc.tasks.RegistersTask;

public interface RegisterGroupUsage {

    RegistersTask getRegistersTask();

    void setRegistersTask(RegistersTask registersTaskReference);

    RegisterGroup getRegistersGroup();

    void setRegistersGroup(RegisterGroup registersGroupReference);
}
