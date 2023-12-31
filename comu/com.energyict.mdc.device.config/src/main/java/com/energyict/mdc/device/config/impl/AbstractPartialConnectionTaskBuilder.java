/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.device.config.PartialConnectionTaskBuilder;
import com.energyict.mdc.common.device.config.ServerPartialConnectionTask;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

abstract class AbstractPartialConnectionTaskBuilder<S, T extends ComPortPool, U extends ServerPartialConnectionTask> implements PartialConnectionTaskBuilder<S, T, U> {

    final S myself;
    final DataModel dataModel;
    final DeviceConfigurationImpl configuration;
    String name;
    T comPortPool;
    ConnectionTypePluggableClass connectionTypePluggableClass;
    boolean asDefault;
    Map<String, Object> properties = new HashMap<>();
    private final EventService eventService;
    private ProtocolDialectConfigurationProperties protocolDialectProperties;
    private ConnectionFunction connectionFunction;

    @SuppressWarnings("unchecked")
    AbstractPartialConnectionTaskBuilder(EventService eventService, Class<?> selfType, DataModel dataModel, DeviceConfigurationImpl configuration) {
        this.eventService = eventService;
        this.dataModel = dataModel;
        this.configuration = configuration;
        myself = (S) selfType.cast(this);
    }

    @Override
    public S pluggableClass(ConnectionTypePluggableClass connectionTypePluggableClass) {
        this.connectionTypePluggableClass = connectionTypePluggableClass;
        return myself;
    }

    @Override
    public S comPortPool(T comPortPool) {
        this.comPortPool = comPortPool;
        return myself;
    }

    @Override
    public S addProperty(String key, Object value) {
        properties.put(key, value);
        return myself;
    }

    @Override
    public S name(String name) {
        this.name = name;
        return myself;
    }

    @Override
    public S setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties properties) {
        this.protocolDialectProperties = properties;
        return myself;
    }

    @Override
    public S connectionFunction(ConnectionFunction connectionFunction) {
        this.connectionFunction = connectionFunction;
        return myself;
    }

    @Override
    public U build() {
        U instance = newInstance();
        instance.setName(name);
        instance.setConnectionTypePluggableClass(connectionTypePluggableClass);
        instance.setConnectionFunction(connectionFunction);
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            instance.setProperty(entry.getKey(), entry.getValue());
        }
        populate(instance);
        configuration.addPartialConnectionTask(instance);
        if (configuration.getId() > 0 && instance instanceof PartialConnectionTaskImpl) {
            eventService.postEvent(((PartialConnectionTaskImpl)instance).createEventType().topic(), instance);
        }
        return instance;
    }

    DeviceConfigurationImpl getConfiguration() {
        return configuration;
    }

    void populate(U instance){
       instance.setProtocolDialectConfigurationProperties(protocolDialectProperties);
    }

    abstract U newInstance();

}
