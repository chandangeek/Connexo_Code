package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.cache.TypeCache;

import static com.elster.jupiter.events.impl.TableSpecs.EVT_EVENTPROPERTYTYPE;
import static com.elster.jupiter.events.impl.TableSpecs.EVT_EVENTTYPE;

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
    public TypeCache<EventType> getEventTypeFactory() {
        return Bus.getComponentCache().getTypeCache(EventType.class, EventTypeImpl.class, EVT_EVENTTYPE.name());
    }

    @Override
    public DataMapper<EventPropertyType> getEventTypePropertyFactory() {
        return dataModel.getDataMapper(EventPropertyType.class, EventPropertyTypeImpl.class, EVT_EVENTPROPERTYTYPE.name());
    }

    @Override
    public DataModel getDataModel() {
        return dataModel;
    }
}
