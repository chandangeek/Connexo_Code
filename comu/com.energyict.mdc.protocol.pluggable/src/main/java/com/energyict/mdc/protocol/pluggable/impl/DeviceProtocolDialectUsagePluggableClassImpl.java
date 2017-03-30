/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectUsagePluggableClass;

import java.time.Instant;
import java.util.List;

public class DeviceProtocolDialectUsagePluggableClassImpl implements DeviceProtocolDialectUsagePluggableClass {

    private final DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    private final DeviceProtocolDialect deviceProtocolDialect;

    public DeviceProtocolDialectUsagePluggableClassImpl(DeviceProtocolPluggableClass deviceProtocolPluggableClass, DeviceProtocolDialect deviceProtocolDialect) {
        this.deviceProtocolPluggableClass = deviceProtocolPluggableClass;
        this.deviceProtocolDialect = deviceProtocolDialect;
    }

    @Override
    public PluggableClassType getPluggableClassType () {
        return null;
    }

    @Override
    public long getId() {
        return this.deviceProtocolPluggableClass.getId();
    }

    @Override
    public String getName() {
        return deviceProtocolPluggableClass.getName();
    }

    @Override
    public void setName(String name)  {
        this.deviceProtocolPluggableClass.setName(name);
    }

    @Override
    public Instant getModificationDate() {
        return this.deviceProtocolPluggableClass.getModificationDate();
    }

    @Override
    public TypedProperties getProperties(List<PropertySpec> propertySpecs) {
        return this.deviceProtocolPluggableClass.getProperties(propertySpecs);
    }

    @Override
    public void setProperty(PropertySpec propertySpec, Object value) {
        this.deviceProtocolPluggableClass.setProperty(propertySpec, value);
    }

    @Override
    public void removeProperty(PropertySpec propertySpec) {
        this.deviceProtocolPluggableClass.removeProperty(propertySpec);
    }

    @Override
    public void save() {
        this.deviceProtocolPluggableClass.save();
    }

    @Override
    public String getJavaClassName() {
        return deviceProtocolPluggableClass.getJavaClassName();
    }

    @Override
    public void delete() {
        this.deviceProtocolPluggableClass.delete();
    }

    @Override
    public long getEntityVersion() {
        return this.deviceProtocolPluggableClass.getEntityVersion();
    }

    @Override
    public DeviceProtocolDialect getDeviceProtocolDialect() {
        return this.deviceProtocolDialect;
    }

}