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

/**
 * Provides an implementation for the {@link PersistenceSupport} interface for {@link IEC1107SecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-18 (14:27)
 */
public class IEC1107PersistenceSupport extends CommonBaseDeviceSecuritySupport<IEC1107SecurityProperties> {
    @Override
    public String application() {
        return "MultiSense";
    }

    @Override
    public String componentName() {
        return CustomPropertySetComponentName.P22.name();
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_IEC1107_SECURITY";
    }

    @Override
    public String domainFieldName() {
        return CommonBaseDeviceSecurityProperties.Fields.DEVICE.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return DeviceProtocolService.COMPONENT_NAME + "_FK_IEC1107SEC_DEV";
    }

    @Override
    protected String propertySpecProviderForeignKeyName() {
        return DeviceProtocolService.COMPONENT_NAME + "_FK_IEC1107SEC_SECPROV";
    }

    @Override
    public Class<IEC1107SecurityProperties> persistenceClass() {
        return IEC1107SecurityProperties.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table, Column completeColumn, List<Column> customPrimaryKeyColumns) {
        table
            .column(IEC1107SecurityProperties.ActualFields.PASSWORD.databaseName())
            .varChar()
            .map(IEC1107SecurityProperties.ActualFields.PASSWORD.javaName())
            .add();
    }

}