/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.security;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.protocols.mdc.services.impl.TranslationKeys;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class InheritedAuthenticationDeviceAccessLevel implements AuthenticationDeviceAccessLevel {

    private final Thesaurus thesaurus;

    @Inject
    public InheritedAuthenticationDeviceAccessLevel(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public int getId() {
        return DeviceAccessLevel.CAN_INHERIT_PROPERTIES_FROM_MASTER_ID;
    }

    @Override
    public String getTranslation() {
        return thesaurus.getFormat(TranslationKeys.INHERITED_ACCESSLEVEL).format();
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return Collections.emptyList();
    }
}