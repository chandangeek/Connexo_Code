package com.elster.jupiter.yellowfin.groups.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.yellowfin.groups.AdHocDeviceGroup;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AdHocDeviceGroupImpl implements AdHocDeviceGroup {

    public String ADHOC_GROUP_NAME_PREFIX = "__##SEARCH_RESULTS##__";

    long id;
    String name;
    private Instant createTime;

    protected List<AdHocEntryImpl> entries = new ArrayList<AdHocEntryImpl>();
    protected final DataModel dataModel;

    @Inject
    private AdHocDeviceGroupImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    AdHocDeviceGroupImpl init(long id, List<Long> devices) {
        this.id = id;
        this.name = String.format(ADHOC_GROUP_NAME_PREFIX + "%d", id);
        entries.addAll(devices.stream().map(deviceId -> AdHocEntryImpl.from(dataModel, id, deviceId)).collect(Collectors.toList()));

        return this;
    }

    static AdHocDeviceGroupImpl from(DataModel dataModel, long id, List<Long> devices) {
        return dataModel.getInstance(AdHocDeviceGroupImpl.class).init(id, devices);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    static class AdHocEntryImpl {

        private long groupId;
        private long deviceId;

        private final DataModel dataModel;

        @Inject
        AdHocEntryImpl(DataModel dataModel) {
            this.dataModel = dataModel;
        }

        AdHocEntryImpl init(long groupId, long deviceId) {
            this.groupId = groupId;
            this.deviceId = deviceId;
            return this;
        }

        static AdHocEntryImpl from(DataModel dataModel, long groupId, long deviceId) {
            return dataModel.getInstance(AdHocEntryImpl.class).init(groupId, deviceId);
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

            AdHocEntryImpl entry = (AdHocEntryImpl) o;

            return groupId == entry.getGroupId() && deviceId == entry.getDeviceId();

        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId, deviceId);
        }
    }

    public void save() {
        factory().persist(this);
        this.name = String.format(ADHOC_GROUP_NAME_PREFIX + "%d", id);
        factory().update(this);
        for (AdHocEntryImpl entry : entries) {
            entry.setGroupId(id);
        }

        List<AdHocEntryImpl> result = entries.stream().collect(Collectors.toList());
        entryFactory().persist(result);
    }

    private DataMapper<AdHocDeviceGroupImpl> factory() {
         return dataModel.mapper(AdHocDeviceGroupImpl.class);
    }

    private DataMapper<AdHocEntryImpl> entryFactory() {
        return dataModel.mapper(AdHocEntryImpl.class);
    }

    public List<AdHocEntryImpl> getEntries(){
        return this.entries;
    }
}
