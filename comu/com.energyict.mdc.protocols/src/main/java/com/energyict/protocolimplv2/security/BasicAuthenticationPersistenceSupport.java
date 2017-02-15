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
 * Provides an implementation for the {@link PersistenceSupport} interface for {@link BasicAuthenticationSecurityProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (13:35)
 */
public class BasicAuthenticationPersistenceSupport extends CommonBaseDeviceSecuritySupport<BasicAuthenticationSecurityProperties> {
    @Override
    public String application() {
        return "MultiSense";
    }

    @Override
    public String componentName() {
        return CustomPropertySetComponentName.P28.name();
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_BASIC_AUTHENTICATION";
    }

    @Override
    public String domainFieldName() {
        return CommonBaseDeviceSecurityProperties.Fields.DEVICE.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return DeviceProtocolService.COMPONENT_NAME + "_FK_BASIC_AUTH_DEV";
    }

    @Override
    protected String propertySpecProviderForeignKeyName() {
        return DeviceProtocolService.COMPONENT_NAME + "_FK_BASIC_AUTH_SECPROV";
    }

    @Override
    public Class<BasicAuthenticationSecurityProperties> persistenceClass() {
        return BasicAuthenticationSecurityProperties.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table, Column completeColumn, List<Column> customPrimaryKeyColumns) {
        Stream
            .of(BasicAuthenticationSecurityProperties.ActualFields.values())
            .forEach(field -> field.addTo(table));
    }

}