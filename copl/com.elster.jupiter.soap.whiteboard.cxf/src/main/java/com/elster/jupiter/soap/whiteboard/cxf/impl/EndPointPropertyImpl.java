/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProperty;

import javax.inject.Inject;
import java.util.Objects;

public class EndPointPropertyImpl implements EndPointProperty {

    private String name;
    private String stringValue;
    private transient PropertySpec propertySpec;

    private Reference<EndPointConfiguration> endPointCfg = ValueReference.absent();

    @Inject
    EndPointPropertyImpl() {
    }

    EndPointPropertyImpl init(EndPointConfigurationImpl endPointConfiguration, String name, Object value) {
        return init(endPointConfiguration, endPointConfiguration.getPropertySpec(name).get(), value);
    }

    EndPointPropertyImpl init(EndPointConfiguration endPointConfiguration, PropertySpec propertySpec, Object value) {
        this.endPointCfg.set(endPointConfiguration);
        this.name = propertySpec.getName();
        this.propertySpec = propertySpec;
        setValue(value);
        return this;
    }

    PropertySpec getPropertySpec() {
        if (propertySpec == null) {
            propertySpec = endPointCfg.get().getPropertySpec(name).get();
        }
        return propertySpec;
    }

    @Override
    public EndPointConfiguration getEndPointConfiguration() {
        return endPointCfg.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getValue() {
        return getPropertySpec().getValueFactory().fromStringValue(stringValue);
    }

    @Override
    public void setValue(Object value) {
        this.stringValue = getPropertySpec().getValueFactory().toStringValue(value);
    }

    @Override
    public String toString() {
        return getName() + ": " + getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EndPointPropertyImpl that = (EndPointPropertyImpl) o;

        return getEndPointConfiguration().getId() == that.getEndPointConfiguration().getId() && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
