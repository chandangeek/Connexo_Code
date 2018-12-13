/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.tasks.LoadProfilesTask;

import java.time.Instant;

public class LoadProfileTypeUsageInProtocolTaskImpl implements LoadProfileTypeUsageInProtocolTask {

    enum Fields {
        LOADPROFILE_TASK_REFERENCE("loadProfilesTaskReference"),
        LOADPROFILE_TYPE_REFERENCE("loadProfileTypeReference");
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    private Reference<LoadProfilesTask> loadProfilesTaskReference = ValueReference.absent();
    private Reference<LoadProfileType> loadProfileTypeReference = ValueReference.absent();
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    @Override
    public LoadProfilesTask getLoadProfilesTask() {
        return loadProfilesTaskReference.get();
    }

    @Override
    public void setLoadProfilesTask(LoadProfilesTask loadProfilesTaskReference) {
        this.loadProfilesTaskReference.set(loadProfilesTaskReference);
    }

    @Override
    public LoadProfileType getLoadProfileType() {
        return loadProfileTypeReference.get();
    }

    @Override
    public void setLoadProfileType(LoadProfileType loadProfileType) {
        this.loadProfileTypeReference.set(loadProfileType);
    }
}