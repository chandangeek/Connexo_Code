package com.energyict.protocols.impl.channels.ip;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.protocols.naming.CustomPropertySetComponentName;

import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PersistenceSupport} interface
 * to support the persistence of {@link OutboundIpConnectionProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (13:04)
 */
public class OutboundIpConnectionPropertiesPersistenceSupport implements PersistenceSupport<ConnectionProvider, OutboundIpConnectionProperties> {

    @Override
    public String componentName() {
        return CustomPropertySetComponentName.P02.name();
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_IP_OUT_CONNECTION_TASK";
    }

    @Override
    public String domainFieldName() {
        return OutboundIpConnectionProperties.Fields.CONNECTION_PROVIDER.javaName();
    }

    @Override
    public String domainColumnName() {
        return OutboundIpConnectionProperties.Fields.CONNECTION_PROVIDER.databaseName();
    }

    @Override
    public String domainForeignKeyName() {
        return "FK_PR1_IP_OUT_CT";
    }

    @Override
    public Class<OutboundIpConnectionProperties> persistenceClass() {
        return OutboundIpConnectionProperties.class;
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
        table
            .column(OutboundIpConnectionProperties.Fields.HOST.databaseName())
            .varChar()
            .notNull()
            .map(OutboundIpConnectionProperties.Fields.HOST.javaName())
            .add();
        table
            .column(OutboundIpConnectionProperties.Fields.PORT_NUMBER.databaseName())
            .number()
            .notNull()
            .map(OutboundIpConnectionProperties.Fields.PORT_NUMBER.javaName())
            .add();
        table
            .column("CONNTIMEOUTVALUE")
            .number()
            .conversion(ColumnConversion.NUMBER2INT)
            .map(OutboundIpConnectionProperties.Fields.CONNECTION_TIMEOUT.javaName() + ".count")
            .add();
        table
            .column("CONNTIMEOUTUNIT")
            .number()
            .conversion(ColumnConversion.NUMBER2INT)
            .map(OutboundIpConnectionProperties.Fields.CONNECTION_TIMEOUT.javaName() + ".timeUnitCode")
            .add();
        Stream
            .of(
                OutboundIpConnectionProperties.Fields.BUFFER_SIZE,
                OutboundIpConnectionProperties.Fields.POST_DIAL_DELAY_MILLIS,
                OutboundIpConnectionProperties.Fields.POST_DIAL_COMMAND_ATTEMPTS)
            .forEach(field -> this.addNullableNumberColumnTo(table, field));
        this.addOptionalStringColumnTo(table, OutboundIpConnectionProperties.Fields.POST_DIAL_COMMAND);
    }

    private void addNullableNumberColumnTo(Table table, OutboundIpConnectionProperties.Fields field) {
        table
            .column(field.databaseName())
            .number()
            .map(field.javaName())
            .add();
    }

    private void addOptionalStringColumnTo(Table table, OutboundIpConnectionProperties.Fields fieldName) {
        table
            .column(fieldName.databaseName())
            .varChar()
            .map(fieldName.javaName())
            .add();
    }

}