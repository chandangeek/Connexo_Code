/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface EventType {

    String getTopic();

    String getComponent();

    String getScope();

    String getCategory();

    String getName();

    boolean shouldPublish();

    void setPublish(boolean publish);

    boolean isEnabledForUseInStateMachines();

    void enableForUseInStateMachines();

    void disableForUseInStateMachines();

    LocalEvent create(Object source);

    EventPropertyType addProperty(String name, ValueType valueType, String accessPath);

    List<EventPropertyType> getPropertyTypes();

    void removePropertyType(EventPropertyType eventPropertyType);

    void update();

    long getVersion();
}