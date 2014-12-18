package com.elster.jupiter.yellowfin.groups.impl;


import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.yellowfin.groups.CachedDeviceGroup;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class AdHocDeviceGroupImpl extends AbstractDeviceGroupImpl implements CachedDeviceGroup {

    @Inject
    private AdHocDeviceGroupImpl(DataModel dataModel) {
        super(dataModel);
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
}
