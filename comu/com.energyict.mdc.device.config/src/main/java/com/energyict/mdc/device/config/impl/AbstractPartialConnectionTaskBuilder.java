package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.DataModel;
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
public abstract class AbstractPartialConnectionTaskBuilder<S, T extends ComPortPool, U extends PartialConnectionTask<T>> implements PartialConnectionTaskBuilder<S, T, U> {

    final S myself;
    final DataModel dataModel;
    T comPortPool;
    ConnectionTypePluggableClass connectionTypePluggableClass;
    boolean asDefault;
    Map<String, Object> properties = new HashMap<>();

    @SuppressWarnings("unchecked")
    AbstractPartialConnectionTaskBuilder(Class<?> selfType, DataModel dataModel) {
        this.dataModel = dataModel;
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
    public S asDefault(boolean asDefault) {
        this.asDefault = asDefault;
        return myself;
    }

    public S addProperty(String key, Object value) {
        properties.put(key, value);
        return myself;
    }

    @Override
    public U build() {
        U instance = newInstance();
        instance.setComportPool(comPortPool);
        instance.setConnectionTypePluggableClass(connectionTypePluggableClass);
        instance.setDefault(asDefault);
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            instance.addProperty(entry.getKey(), entry.getValue());
            instance.getProperties().add(PartialConnectionTaskPropertyImpl.from(instance, entry.getKey(), entry.getValue()));
        }

        populate(instance);

        return instance;
    }

    abstract void populate(U instance);

    abstract U newInstance();
}
