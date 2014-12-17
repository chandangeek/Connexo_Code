package com.elster.jupiter.yellowfin.groups;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.associations.Effectivity;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

public interface CachedDeviceGroup {
    void save();
    List<Entry> getEntries();

    interface Entry {
        long getDeviceId();
        long getGroupId();
        void setGroupId(long id);
    }
}
