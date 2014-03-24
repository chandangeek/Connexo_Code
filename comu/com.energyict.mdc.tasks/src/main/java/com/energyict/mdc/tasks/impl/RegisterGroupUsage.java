package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.device.config.RegisterGroup;
import com.energyict.mdc.tasks.RegistersTask;

/**
 * Created with IntelliJ IDEA.
 * User: bvn
 * Date: 3/21/14
 * Time: 3:32 PM
 * To change this template use File | Settings | File Templates.
 */
public interface RegisterGroupUsage {

    RegistersTask getRegistersTask();

    void setRegistersTask(RegistersTask registersTaskReference);

    RegisterGroup getRegistersGroup();

    void setRegistersGroup(RegisterGroup registersGroupReference);
}
