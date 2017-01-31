/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.serial.optical.dlms;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.protocols.impl.channels.serial.SioSerialConnectionProperties;
import com.energyict.protocols.naming.CustomPropertySetComponentName;

import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PersistenceSupport} interface
 * to support the persistence of {@link LegacyOpticalDlmsConnectionProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-10 (13:37)
 */
public class LegacyOpticalDlmsConnectionPropertiesPersistenceSupport implements PersistenceSupport<ConnectionProvider, LegacyOpticalDlmsConnectionProperties> {
    @Override
    public String application() {
        return "MultiSense";
    }

    @Override
    public String componentName() {
        return CustomPropertySetComponentName.P13.name();
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_LEGACYOPTICALDLMS_CT";
    }

    @Override
    public String domainFieldName() {
        return "connectionProvider";
    }

    @Override
    public String domainColumnName() {
        return "CONNECTIONPROVIDER";
    }

    @Override
    public String domainForeignKeyName() {
        return "FK_PR1_LEGACYOPTICALDLMS_CT";
    }

    @Override
    public Class<LegacyOpticalDlmsConnectionProperties> persistenceClass() {
        return LegacyOpticalDlmsConnectionProperties.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
        // None of the custom properties are part of the primary key
        return Collections.emptyList();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
        Stream
            .of(SioSerialConnectionProperties.Fields.values())
            .forEach(fieldName -> this.addCustomPropertyColumnTo(table, fieldName));
        Stream
            .of(LegacyOpticalDlmsConnectionProperties.Field.values())
            .forEach(fieldName -> this.addCustomPropertyColumnTo(table, fieldName));
    }

    private void addCustomPropertyColumnTo(Table table, SioSerialConnectionProperties.Fields fieldName) {
        table
            .column(fieldName.databaseName())
            .number()
            .conversion(ColumnConversion.NUMBER2ENUM)
            .map(fieldName.javaName())
            .add();
    }

    private void addCustomPropertyColumnTo(Table table, LegacyOpticalDlmsConnectionProperties.Field fieldName) {
        table
            .column(fieldName.databaseName())
            .number()
            .map(fieldName.javaName())
            .add();
    }

}