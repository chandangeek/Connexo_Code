package com.energyict.protocols.impl.channels.sms;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;

import com.google.inject.Module;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link PersistenceSupport} interface
 * to support the persistence of {@link OutboundProximusConnectionProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (12:01)
 */
public class OutboundProximusConnectionPropertiesPersistenceSupport implements PersistenceSupport<ConnectionType, OutboundProximusConnectionProperties> {

    @Override
    public String componentName() {
        return DeviceProtocolService.COMPONENT_NAME;
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_PROXIMUS_SMS_OUT_CT";
    }

    @Override
    public String domainFieldName() {
        return OutboundProximusConnectionProperties.Fields.CONNECTION_TYPE.javaName();
    }

    @Override
    public String domainColumnName() {
        return OutboundProximusConnectionProperties.Fields.CONNECTION_TYPE.databaseName();
    }

    @Override
    public String domainForeignKeyName() {
        return "FK_PR1_PROXIMUS_SMS_OUT_CT";
    }

    @Override
    public Class<OutboundProximusConnectionProperties> persistenceClass() {
        return OutboundProximusConnectionProperties.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table) {
        Stream
            .of(OutboundProximusConnectionProperties.Fields.values())
            .forEach(fieldName -> this.addCustomPropertyColumnTo(table, fieldName));
    }

    private void addCustomPropertyColumnTo(Table table, OutboundProximusConnectionProperties.Fields fieldName) {
        table
            .column(fieldName.databaseName())
            .varChar()
            .notNull()
            .map(fieldName.javaName())
            .add();
    }

}