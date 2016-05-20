package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.hypermedia.LinkInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;

import java.util.List;

public class ConfigurationSecurityPropertySetInfo extends LinkInfo<Long> {

    public String name;
    public LinkInfo authenticationAccessLevel;
    public LinkInfo encryptionAccessLevel;

    public List<PropertyInfo> properties;
}
