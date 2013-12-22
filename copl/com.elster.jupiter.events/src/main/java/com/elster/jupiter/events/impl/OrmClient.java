package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

public interface OrmClient {

    void install();

    DataMapper<EventType> getEventTypeFactory();

    DataModel getDataModel();

    DataMapper<EventPropertyType> getEventTypePropertyFactory();
}
