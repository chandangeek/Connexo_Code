package com.energyict.protocols.impl.channels.ip;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;

import com.google.inject.Module;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PersistenceSupport} interface
 * to support the persistence of {@link OutboundIpConnectionProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (13:04)
 */
public class OutboundIpConnectionPropertiesPersistenceSupport implements PersistenceSupport<ConnectionType, OutboundIpConnectionProperties> {

    @Override
    public String componentName() {
        return DeviceProtocolService.COMPONENT_NAME;
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_IP_OUT_CONNECTION_TASK";
    }

    @Override
    public String domainFieldName() {
        return OutboundIpConnectionProperties.Fields.CONNECTION_TYPE.javaName();
    }

    @Override
    public String domainColumnName() {
        return OutboundIpConnectionProperties.Fields.CONNECTION_TYPE.databaseName();
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
    public void addCustomPropertyColumnsTo(Table table) {
        this.addRquiredStringColumnTo(table, OutboundIpConnectionProperties.Fields.HOST);
        this.addRquiredStringColumnTo(table, OutboundIpConnectionProperties.Fields.PORT);
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

    private void addRquiredStringColumnTo(Table table, OutboundIpConnectionProperties.Fields fieldName) {
        table
            .column(fieldName.databaseName())
            .varChar()
            .notNull()
            .map(fieldName.javaName())
            .add();
    }

    private void addOptionalStringColumnTo(Table table, OutboundIpConnectionProperties.Fields fieldName) {
        table
            .column(fieldName.databaseName())
            .varChar()
            .notNull()
            .map(fieldName.javaName())
            .add();
    }

}