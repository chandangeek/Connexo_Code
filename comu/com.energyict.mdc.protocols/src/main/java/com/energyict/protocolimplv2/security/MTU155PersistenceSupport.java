/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.security;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecuritySupport;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.protocols.naming.CustomPropertySetComponentName;

import com.google.inject.Module;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PersistenceSupport} interface for {@link Mtu155SecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (17:36)
 */
public class MTU155PersistenceSupport extends CommonBaseDeviceSecuritySupport<MTU155SecurityProperties> {
    @Override
    public String application() {
        return "MultiSense";
    }

    @Override
    public String componentName() {
        return CustomPropertySetComponentName.P24.name();
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_MTU155SEC_PER_CLIENT";
    }

    @Override
    public String domainFieldName() {
        return CommonBaseDeviceSecurityProperties.Fields.DEVICE.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return DeviceProtocolService.COMPONENT_NAME + "_FK_MTU155SEC_DEV";
    }

    @Override
    protected String propertySpecProviderForeignKeyName() {
        return DeviceProtocolService.COMPONENT_NAME + "_FK_MTU155SEC_SECPROV";
    }

    @Override
    public Class<MTU155SecurityProperties> persistenceClass() {
        return MTU155SecurityProperties.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table, Column completeColumn, List<Column> customPrimaryKeyColumns) {
        Stream
            .of(MTU155SecurityProperties.ActualFields.values())
            .forEach(field -> field.addTo(table));
    }

}