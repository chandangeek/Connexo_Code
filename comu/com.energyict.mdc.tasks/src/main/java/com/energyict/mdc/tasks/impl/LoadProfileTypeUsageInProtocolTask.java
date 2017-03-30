/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.tasks.LoadProfilesTask;

public interface LoadProfileTypeUsageInProtocolTask {

    LoadProfilesTask getLoadProfilesTask();

    void setLoadProfilesTask(LoadProfilesTask loadProfilesTaskReference);

    LoadProfileType getLoadProfileType();

    void setLoadProfileType(LoadProfileType loadProfileType);

}
