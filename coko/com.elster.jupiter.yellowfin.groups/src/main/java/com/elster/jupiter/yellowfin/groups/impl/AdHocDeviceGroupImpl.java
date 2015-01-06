package com.elster.jupiter.yellowfin.groups.impl;


import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.yellowfin.groups.CachedDeviceGroup;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdHocDeviceGroupImpl implements CachedDeviceGroup {
    protected long id;
    protected List<Entry> entries = new ArrayList<Entry>();
    protected final DataModel dataModel;

    @Inject
    private AdHocDeviceGroupImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    AdHocDeviceGroupImpl init(long id, List<Device> devices) {
        this.id = id;
        for(Device device : devices){
            entries.add(EntryImpl.from(dataModel, id, device.getId()));
        }

        return this;
    }

    static AdHocDeviceGroupImpl from(DataModel dataModel, long id, List<Device> devices) {
        return dataModel.getInstance(AdHocDeviceGroupImpl.class).init(id, devices);
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
        factory().persist(this);
        for (Entry entry : entries) {
            entry.setGroupId(id);
        }

        ArrayList<Entry> result = new ArrayList<Entry>();
        for (Entry entry : entries) {
            result.add(entry);
        }
        entryFactory().persist(result);
    }

    private DataMapper<AdHocDeviceGroupImpl> factory() {
         return dataModel.mapper(AdHocDeviceGroupImpl.class);
    }

    private DataMapper<Entry> entryFactory() {
        return dataModel.mapper(Entry.class);
    }

    @Override
    public List<Entry> getEntries(){
        return this.entries;
    }
}
