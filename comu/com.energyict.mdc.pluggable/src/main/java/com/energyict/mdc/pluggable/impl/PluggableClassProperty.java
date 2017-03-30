/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.pluggable.PluggableClass;

import javax.inject.Inject;
import java.time.Instant;

/**
 * Models a key/value pair to hold the properties of a {@link PluggableClass}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-20 (17:39)
 */
public class PluggableClassProperty {

    private final DataModel dataModel;
    private final Reference<PluggableClass> pluggableClass = ValueReference.absent();
    public String name;
    public String value;
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    @Inject
    PluggableClassProperty(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    void init (PluggableClass pluggableClass, String name, String value) {
        this.pluggableClass.set(pluggableClass);
        this.name = name;
        this.value = value;
    }

    public void save() {
        Save.UPDATE.save(this.dataModel, this);
    }
}