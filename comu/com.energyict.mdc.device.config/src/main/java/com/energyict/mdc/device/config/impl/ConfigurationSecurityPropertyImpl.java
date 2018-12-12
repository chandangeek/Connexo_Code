/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.energyict.mdc.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.device.config.SecurityPropertySet;

import javax.inject.Inject;
import javax.validation.constraints.Size;

/**
 * Implementation of the {@link ConfigurationSecurityProperty} interface
 *
 * @author stijn
 * @since 21.04.17 - 10:42
 */
public class ConfigurationSecurityPropertyImpl implements ConfigurationSecurityProperty {

    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<SecurityPropertySet> securityPropertySet = Reference.empty();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<SecurityAccessorType> keyAccessorType = Reference.empty();

    @Inject
    public ConfigurationSecurityPropertyImpl() {
    }

    public ConfigurationSecurityPropertyImpl(SecurityPropertySet securityPropertySet, String name, SecurityAccessorType securityAccessorType) {
        super();
        this.securityPropertySet.set(securityPropertySet);
        this.name = name;
        this.keyAccessorType.set(securityAccessorType);
    }

    @Override
    public String getName() {
        return name;
    }

    public SecurityPropertySet getSecurityPropertySet() {
        return securityPropertySet.get();
    }

    public SecurityAccessorType getSecurityAccessorType() {
        return keyAccessorType.orNull();
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void setKeyAccessorType(SecurityAccessorType securityAccessorType) {
        this.keyAccessorType.set(securityAccessorType);
    }

    protected void setSecurityPropertySet(SecurityPropertySet securityPropertySet) {
        this.securityPropertySet.set(securityPropertySet);
    }
}