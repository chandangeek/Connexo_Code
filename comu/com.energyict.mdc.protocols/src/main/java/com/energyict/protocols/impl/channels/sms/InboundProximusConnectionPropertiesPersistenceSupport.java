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
 * to support the persistence of {@link InboundProximusConnectionProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-05 (17:43)
 */
public class InboundProximusConnectionPropertiesPersistenceSupport implements PersistenceSupport<ConnectionType, InboundProximusConnectionProperties> {

    @Override
    public String componentName() {
        return DeviceProtocolService.COMPONENT_NAME;
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_PROXIMUS_SMS_IN_CT";
    }

    @Override
    public String domainFieldName() {
        return InboundProximusConnectionProperties.Fields.CONNECTION_TYPE.javaName();
    }

    @Override
    public String domainColumnName() {
        return InboundProximusConnectionProperties.Fields.CONNECTION_TYPE.databaseName();
    }

    @Override
    public String domainForeignKeyName() {
        return "FK_PR1_PROXIMUS_SMS_IN_CT";
    }

    @Override
    public Class<InboundProximusConnectionProperties> persistenceClass() {
        return InboundProximusConnectionProperties.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table) {
        Stream
            .of(InboundProximusConnectionProperties.Fields.values())
            .forEach(fieldName -> fieldName.addTo(table));
    }

}