/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.tasks.RegistersTask;

public interface RegisterGroupUsage {

    RegistersTask getRegistersTask();

    void setRegistersTask(RegistersTask registersTaskReference);

    RegisterGroup getRegistersGroup();

    void setRegistersGroup(RegisterGroup registersGroupReference);
}
