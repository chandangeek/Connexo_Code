/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.groups.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DynamicDeviceGroupImpl  {
    protected long id;
    protected List<DynamicEntryImpl> entries = new ArrayList<DynamicEntryImpl>();
    protected final DataModel dataModel;

    @Inject
    protected DynamicDeviceGroupImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    DynamicDeviceGroupImpl init(long id, List<EndDevice> endDevices) {
        this.id = id;
        for(EndDevice endDevice : endDevices){
            entries.add(DynamicEntryImpl.from(dataModel, id, endDevice.getId()));
        }

        return this;
    }

    static DynamicDeviceGroupImpl from(DataModel dataModel, long id, List<EndDevice> endDevices) {
        return dataModel.getInstance(DynamicDeviceGroupImpl.class).init(id, endDevices);
    }

    static class DynamicEntryImpl /*implements CachedDeviceGroup.Entry */{

        private long groupId;
        private long deviceId;

        private final DataModel dataModel;

        @Inject
        DynamicEntryImpl(DataModel dataModel) {
            this.dataModel = dataModel;
        }

        DynamicEntryImpl init(long groupId, long deviceId) {
            this.groupId = groupId;
            this.deviceId = deviceId;
            return this;
        }

        static DynamicEntryImpl from(DataModel dataModel, long groupId, long deviceId) {
            return dataModel.getInstance(DynamicEntryImpl.class).init(groupId, deviceId);
        }


        public long getDeviceId() {
            return deviceId;
        }

        public long getGroupId() {
            return groupId;
        }

        public void setGroupId(long id){
            this.groupId = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            DynamicEntryImpl entry = (DynamicEntryImpl) o;

            return groupId == entry.getGroupId() && deviceId == entry.getDeviceId();

        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId, deviceId);
        }
    }

    public void save() {
        List<DynamicEntryImpl> existingEntries = entryFactory().find("groupId", id);

        List<DynamicEntryImpl> toRemoveEntries = existingEntries.stream().filter(entry -> !entries.contains(entry)).
                collect(Collectors.toList());
        entryFactory().remove(toRemoveEntries);

        List<DynamicEntryImpl> toAddEntries = entries.stream().filter(entry -> !existingEntries.contains(entry)).
                collect(Collectors.toList());
        entryFactory().persist(toAddEntries);
    }

    private DataMapper<DynamicEntryImpl> entryFactory() {
        return dataModel.mapper(DynamicEntryImpl.class);
    }


    public List<DynamicEntryImpl> getEntries(){
        return this.entries;
    }
}
