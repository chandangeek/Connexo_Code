/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperty;
import com.energyict.mdc.device.config.events.EventType;

import javax.validation.constraints.Size;
import java.time.Instant;

@SizeForDynamicAttributeName(max = 4000, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
class ProtocolDialectConfigurationPropertyImpl implements ProtocolDialectConfigurationProperty {

    private Reference<ProtocolDialectConfigurationPropertiesImpl> properties = ValueReference.absent();
    @Size(max = 255, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    private String value;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;

    static ProtocolDialectConfigurationPropertyImpl forKey(ProtocolDialectConfigurationPropertiesImpl props, String name) {
        ProtocolDialectConfigurationPropertyImpl property = new ProtocolDialectConfigurationPropertyImpl();
        property.name = name;
        property.properties.set(props);
        return property;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    public ProtocolDialectConfigurationPropertyImpl setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties() {
        return this.properties.get();
    }

    void validateDelete() {
        this.properties.get().getEventService().postEvent(EventType.PROTOCOLCONFIGURATIONPROPS_VALIDATEREMOVE_ONE.topic(), this);
    }

}