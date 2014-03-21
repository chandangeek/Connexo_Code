package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTaskBuilder;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 13/03/14
 * Time: 15:03
 */
public abstract class AbstractPartialConnectionTaskBuilder<S, T extends ComPortPool, U extends PartialConnectionTask> implements PartialConnectionTaskBuilder<S, T, U> {

    final S myself;
    final DataModel dataModel;
    final DeviceCommunicationConfiguration configuration;
    String name;
    T comPortPool;
    ConnectionTypePluggableClass connectionTypePluggableClass;
    boolean asDefault;
    Map<String, Object> properties = new HashMap<>();

    @SuppressWarnings("unchecked")
    AbstractPartialConnectionTaskBuilder(Class<?> selfType, DataModel dataModel, DeviceCommunicationConfiguration configuration) {
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
    public U build() {
        U instance = newInstance();
        instance.setName(name);
        instance.setConnectionTypePluggableClass(connectionTypePluggableClass);
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            instance.setProperty(entry.getKey(), entry.getValue());
            instance.getProperties().add(PartialConnectionTaskPropertyImpl.from(instance, entry.getKey(), entry.getValue()));
        }

        populate(instance);

        configuration.addPartialConnectionTask(instance);

        return instance;
    }

    abstract void populate(U instance);

    abstract U newInstance();
}
