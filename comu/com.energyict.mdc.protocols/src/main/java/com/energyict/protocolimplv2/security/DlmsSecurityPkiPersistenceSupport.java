/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.protocolimplv2.security;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecurityProperties;
import com.energyict.mdc.protocol.api.security.CommonBaseDeviceSecuritySupport;
import com.energyict.protocols.naming.CustomPropertySetComponentName;

import com.google.inject.Module;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class DlmsSecurityPkiPersistenceSupport extends CommonBaseDeviceSecuritySupport<DlmsSecurityPkiProperties> {
    private final String COMPONENT_NAME = "PKI";

    @Override
    public String application() {
        return "MultiSense";
    }

    @Override
    public String componentName() {
        return CustomPropertySetComponentName.P35.name();
    }

    @Override
    public String tableName() {
        return COMPONENT_NAME + "_DLMS_SECURITY";
    }

    @Override
    public String domainFieldName() {
        return CommonBaseDeviceSecurityProperties.Fields.DEVICE.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return COMPONENT_NAME + "_FK_DLMSSEC_DEV";
    }

    @Override
    protected String propertySpecProviderForeignKeyName() {
        return COMPONENT_NAME + "_FK_DLMSSEC_SECPROV";
    }

    @Override
    public Class<DlmsSecurityPkiProperties> persistenceClass() {
        return DlmsSecurityPkiProperties.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table, Column completeColumn, List<Column> customPrimaryKeyColumns) {
        Stream
            .of(DlmsSecurityPkiProperties.ActualFields.values())
            .forEach(field -> field.addTo(table));
    }

}