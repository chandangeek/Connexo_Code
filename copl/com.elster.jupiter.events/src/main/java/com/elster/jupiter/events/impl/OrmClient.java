package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.cache.TypeCache;

public interface OrmClient {

    void install();

    TypeCache<EventType> getEventTypeFactory();

    DataModel getDataModel();

    DataMapper<EventPropertyType> getEventTypePropertyFactory();
}
