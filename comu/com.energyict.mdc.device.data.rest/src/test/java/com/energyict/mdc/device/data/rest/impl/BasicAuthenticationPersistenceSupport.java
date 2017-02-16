/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;
import com.energyict.mdc.protocol.api.security.SecurityPropertySpecProvider;

import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PersistenceSupport} interface for {@link BasicAuthenticationSecurityProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-20 (11:13)
 */
public class BasicAuthenticationPersistenceSupport implements PersistenceSupport<BaseDevice, BasicAuthenticationSecurityProperties> {
    @Override
    public String application() {
        return "MultiSense";
    }

    @Override
    public String componentName() {
        return "DDT";   // As in device.data.test
    }

    @Override
    public String tableName() {
        return "DDT_BASIC_AUTHENTICATION";
    }

    @Override
    public String domainFieldName() {
        return CommonBaseDeviceSecurityProperties.Fields.DEVICE.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return "DDT_FK_BASIC_AUTH_DEV";
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
    public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
        return Collections.singletonList(
                table
                        .column(CommonBaseDeviceSecurityProperties.Fields.PROPERTY_SPEC_PROVIDER.databaseName())
                        .number()
                        .notNull()
                        .add());
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
        Stream
                .of(BasicAuthenticationSecurityProperties.ActualFields.values())
                .forEach(field -> field.addTo(table));

        table
                .foreignKey("DDT_FK_BASIC_AUTH_SECPROV")
                .on(customPrimaryKeyColumns.get(0))
                .map(CommonBaseDeviceSecurityProperties.Fields.PROPERTY_SPEC_PROVIDER.javaName())
                .references(SecurityPropertySpecProvider.class)
                .add();
    }

}