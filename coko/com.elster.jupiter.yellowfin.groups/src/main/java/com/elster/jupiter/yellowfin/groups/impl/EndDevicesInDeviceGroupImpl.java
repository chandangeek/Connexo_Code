package com.elster.jupiter.yellowfin.groups.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.google.common.collect.FluentIterable;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EndDevicesInDeviceGroupImpl implements EndDevicesInDeviceGroup {
    private long id;
    private List<EntryImpl> entries = new ArrayList<EntryImpl>();
    private final DataModel dataModel;

    @Inject
    private EndDevicesInDeviceGroupImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    EndDevicesInDeviceGroupImpl init(long id, List<EndDevice> endDevices) {
        this.id = id;
        for(EndDevice endDevice : endDevices){
            entries.add(EntryImpl.from(dataModel, id, endDevice.getId()));
        }

        return this;
    }

    static EndDevicesInDeviceGroupImpl from(DataModel dataModel, long id, List<EndDevice> endDevices) {
        return dataModel.getInstance(EndDevicesInDeviceGroupImpl.class).init(id, endDevices);
    }

    static class EntryImpl implements EndDevicesInDeviceGroup.Entry {

        private transient EndDeviceGroup endDeviceGroup;
        private long groupId;
        private transient EndDevice endDevice;
        private long endDeviceId;

        private final DataModel dataModel;
        //private final MeteringService meteringService;

        @Inject
        EntryImpl(DataModel dataModel/*, MeteringService meteringService*/) {
            this.dataModel = dataModel;
            //this.meteringService = meteringService;
        }

        EntryImpl init(long endDeviceGroupId, long endDeviceId) {
            this.groupId = endDeviceGroupId;
            this.endDeviceId = endDeviceId;
            return this;
        }

        static EntryImpl from(DataModel dataModel, long endDeviceGroupId, long endDeviceId) {
            return dataModel.getInstance(EntryImpl.class).init(endDeviceGroupId, endDeviceId);
        }

        @Override
        public long getEndDeviceId() {
            return endDeviceId;
        }

        @Override
        public long getEndDeviceGroupId() {
            return groupId;
        }

        /*@Override
        public EndDevice getEndDevice() {
            if (endDevice == null) {
                endDevice = meteringService.findEndDevice(endDeviceId).get();
            }
            return endDevice;
        }

        @Override
        public EndDeviceGroup getEndDeviceGroup() {
            if (endDeviceGroup == null) {
                endDeviceGroup = dataModel.mapper(QueryEndDeviceGroup.class).getOptional(groupId).get();
            }
            return endDeviceGroup;
        }*/

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            EntryImpl entry = (EntryImpl) o;

            return groupId == entry.groupId && endDeviceId == entry.endDeviceId;

        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId, endDeviceId);
        }
    }

    @Override
    public void save() {
        List<Entry> existingEntries = entryFactory().find("groupId", id);
        DiffList<Entry> entryDiff = ArrayDiffList.fromOriginal(existingEntries);

        entryDiff.clear();
        for(Entry entry : entries){
            entryDiff.add(EntryImpl.from(dataModel, id, entry.getEndDeviceId()));
        }

        entryFactory().remove(FluentIterable.from(entryDiff.getRemovals()).toList());
        entryFactory().update(FluentIterable.from(entryDiff.getRemaining()).toList());
        entryFactory().persist(FluentIterable.from(entryDiff.getAdditions()).toList());
    }

   // private DataMapper<QueryEndDeviceGroup> factory() {
   //     return dataModel.mapper(QueryEndDeviceGroup.class);
   // }

    private DataMapper<Entry> entryFactory() {
        return dataModel.mapper(Entry.class);
    }
}
