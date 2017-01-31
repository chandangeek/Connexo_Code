/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.security;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.ViewPrivilege;
import com.energyict.mdc.protocol.api.device.BaseDevice;

import java.util.EnumSet;
import java.util.Set;

/**
 * Defines the minimal behavior for any component that will
 * be providing security properties as a {@link CustomPropertySet}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-18 (12:39)
 */
public abstract class SecurityCustomPropertySet<T extends PersistentDomainExtension<BaseDevice>> implements CustomPropertySet<BaseDevice, T> {

    @Override
    public abstract PersistenceSupport<BaseDevice, T> getPersistenceSupport();

    @Override
    public Class<BaseDevice> getDomainClass() {
        return BaseDevice.class;
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public boolean isVersioned() {
        return true;
    }

    @Override
    public Set<ViewPrivilege> defaultViewPrivileges() {
        return EnumSet.noneOf(ViewPrivilege.class);
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return EnumSet.noneOf(EditPrivilege.class);
    }

}