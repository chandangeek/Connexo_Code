package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.tasks.LoadProfilesTask;

/**
 * Copyrights EnergyICT
 * Date: 13/05/14
 * Time: 13:27
 */
public interface LoadProfileTypeUsageInProtocolTask {

    LoadProfilesTask getLoadProfilesTask();

    void setLoadProfilesTask(LoadProfilesTask loadProfilesTaskReference);

    LoadProfileType getLoadProfileType();

    void setLoadProfileType(LoadProfileType loadProfileType);

}
