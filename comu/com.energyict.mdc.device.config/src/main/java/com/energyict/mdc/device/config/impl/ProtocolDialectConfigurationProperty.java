package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;

import javax.inject.Inject;
import java.time.Instant;

/**
 * Copyrights EnergyICT
 * Date: 6/03/14
 * Time: 16:43
 */
public class ProtocolDialectConfigurationProperty {

    private Reference<ProtocolDialectConfigurationProperties> properties = ValueReference.absent();
    private String name;
    private String value;
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    @Inject
    public ProtocolDialectConfigurationProperty() {
    }

    public static ProtocolDialectConfigurationProperty forKey(ProtocolDialectConfigurationProperties props, String name) {
        ProtocolDialectConfigurationProperty property = new ProtocolDialectConfigurationProperty();
        property.name = name;
        property.properties.set(props);
        return property;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public ProtocolDialectConfigurationProperty setValue(String value) {
        this.value = value;
        return this;
    }

}