package com.elster.jupiter.yellowfin.groups.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;
import com.elster.jupiter.yellowfin.groups.CachedDeviceGroup;
import com.google.common.collect.FluentIterable;

import javax.inject.Inject;
import java.util.List;

public class DynamicDeviceGroupImpl extends AbstractDeviceGroupImpl implements CachedDeviceGroup {

    @Inject
    protected DynamicDeviceGroupImpl(DataModel dataModel) {
        super(dataModel);
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
}
