package com.elster.jupiter.yellowfin.groups.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.elster.jupiter.yellowfin.groups.CachedDeviceGroup;
import com.google.common.collect.FluentIterable;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DynamicDeviceGroupImpl implements CachedDeviceGroup {
    private long id;
    private List<Entry> entries = new ArrayList<Entry>();
    private final DataModel dataModel;

    @Inject
    private DynamicDeviceGroupImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    DynamicDeviceGroupImpl init(long id, List<EndDevice> endDevices) {
        this.id = id;
        for(EndDevice endDevice : endDevices){
            entries.add(EntryImpl.from(dataModel, id, endDevice.getId()));
        }

        return this;
    }

    static DynamicDeviceGroupImpl from(DataModel dataModel, long id, List<EndDevice> endDevices) {
        return dataModel.getInstance(DynamicDeviceGroupImpl.class).init(id, endDevices);
    }

    static class EntryImpl implements CachedDeviceGroup.Entry {

        private long groupId;
        private long deviceId;

        private final DataModel dataModel;

        @Inject
        EntryImpl(DataModel dataModel) {
            this.dataModel = dataModel;
        }

        EntryImpl init(long groupId, long deviceId) {
            this.groupId = groupId;
            this.deviceId = deviceId;
            return this;
        }

        static EntryImpl from(DataModel dataModel, long groupId, long deviceId) {
            return dataModel.getInstance(EntryImpl.class).init(groupId, deviceId);
        }

        @Override
        public long getDeviceId() {
            return deviceId;
        }

        @Override
        public long getGroupId() {
            return groupId;
        }

        @Override
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

            Entry entry = (Entry) o;

            return groupId == entry.getGroupId() && deviceId == entry.getDeviceId();

        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId, deviceId);
        }
    }

    @Override
    public void save() {
        List<Entry> existingEntries = entryFactory().find("groupId", id);
        DiffList<Entry> entryDiff = ArrayDiffList.fromOriginal(existingEntries);

        entryDiff.clear();
        for(Entry entry : entries){
            entryDiff.add(EntryImpl.from(dataModel, id, entry.getDeviceId()));
        }

        entryFactory().remove(FluentIterable.from(entryDiff.getRemovals()).toList());
        entryFactory().update(FluentIterable.from(entryDiff.getRemaining()).toList());
        entryFactory().persist(FluentIterable.from(entryDiff.getAdditions()).toList());
    }

    private DataMapper<Entry> entryFactory() {
        return dataModel.mapper(Entry.class);
    }

    @Override
    public List<Entry> getEntries(){
        return this.entries;
    }
}
