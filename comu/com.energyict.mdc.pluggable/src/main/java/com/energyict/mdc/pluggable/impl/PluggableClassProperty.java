package com.energyict.mdc.pluggable.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.pluggable.PluggableClass;

/**
 * Models a key/value pair to hold the properties of a {@link PluggableClass}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-20 (17:39)
 */
public class PluggableClassProperty {

    private final Reference<PluggableClass> pluggableClass = ValueReference.absent();
    public String name;
    public String value;

    PluggableClassProperty() {
        super();
    }

    public PluggableClassProperty(PluggableClass pluggableClass, String name, String value) {
        this();
        this.pluggableClass.set(pluggableClass);
        this.name = name;
        this.value = value;
    }

}