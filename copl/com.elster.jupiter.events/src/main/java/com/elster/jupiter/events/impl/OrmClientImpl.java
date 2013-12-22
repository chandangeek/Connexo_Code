package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

public class OrmClientImpl implements OrmClient {

    private final DataModel dataModel;

    public OrmClientImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void install() {
        dataModel.install(true, true);
    }

    @Override
    public DataMapper<EventType> getEventTypeFactory() {
        return dataModel.mapper(EventType.class);
    }

    @Override
    public DataMapper<EventPropertyType> getEventTypePropertyFactory() {
        return dataModel.mapper(EventPropertyType.class);
    }

    @Override
    public DataModel getDataModel() {
        return dataModel;
    }
}
