package com.energyict.protocols.impl.channels.serial.optical.dlms;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.protocols.impl.channels.serial.SioSerialConnectionProperties;

import com.google.inject.Module;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PersistenceSupport} interface
 * to support the persistence of {@link LegacyOpticalDlmsConnectionProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-10 (13:37)
 */
public class LegacyOpticalDlmsConnectionPropertiesPersistenceSupport implements PersistenceSupport<ConnectionType, LegacyOpticalDlmsConnectionProperties> {

    @Override
    public String componentName() {
        return DeviceProtocolService.COMPONENT_NAME;
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_LEGACYOPTICALDLMS_CT";
    }

    @Override
    public String domainFieldName() {
        return LegacyOpticalDlmsConnectionProperties.FieldNames.CONNECTION_TYPE.javaName();
    }

    @Override
    public String domainColumnName() {
        return LegacyOpticalDlmsConnectionProperties.FieldNames.CONNECTION_TYPE.databaseName();
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
    public void addCustomPropertyColumnsTo(Table table) {
        Stream
            .of(SioSerialConnectionProperties.FieldNames.values())
            .forEach(fieldName -> this.addCustomPropertyColumnTo(table, fieldName));
        Stream
            .of(LegacyOpticalDlmsConnectionProperties.Field.values())
            .forEach(fieldName -> this.addCustomPropertyColumnTo(table, fieldName));
    }

    private void addCustomPropertyColumnTo(Table table, SioSerialConnectionProperties.FieldNames fieldName) {
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