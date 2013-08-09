package com.elster.jupiter.events;

import java.util.List;

public interface EventType {

    String getTopic();

    String getComponent();

    String getScope();

    String getCategory();

    String getName();

    boolean shouldPublish();

    List<EventPropertyType> getPropertyTypes();

    LocalEvent create(Object source);

    void setPublish(boolean publish);

    EventPropertyType addProperty(String name, ValueType valueType, String accessPath);

    void save();
}
