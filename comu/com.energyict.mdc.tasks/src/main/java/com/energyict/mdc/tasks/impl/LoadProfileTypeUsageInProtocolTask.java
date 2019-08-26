/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.common.masterdata.LoadProfileType;
import com.energyict.mdc.common.tasks.LoadProfilesTask;

public interface LoadProfileTypeUsageInProtocolTask {

    LoadProfilesTask getLoadProfilesTask();

    void setLoadProfilesTask(LoadProfilesTask loadProfilesTaskReference);

    LoadProfileType getLoadProfileType();

    void setLoadProfileType(LoadProfileType loadProfileType);

}
