package com.energyict.protocols.impl.channels.inbound;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;

import com.google.inject.Module;

import java.util.Optional;

/**
 * Provides an implementation for the {@link PersistenceSupport} interface
 * to support the persistence of {@link EIWebConnectionProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (15:46)
 */
public class EIWebConnectionPropertiesPersistenceSupport implements PersistenceSupport<ConnectionProvider, EIWebConnectionProperties> {

    @Override
    public String componentName() {
        return DeviceProtocolService.COMPONENT_NAME;
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_EIWEB_CONNECTION_TYPE";
    }

    @Override
    public String domainFieldName() {
        return EIWebConnectionProperties.Fields.CONNECTION_PROVIDER.javaName();
    }

    @Override
    public String domainColumnName() {
        return EIWebConnectionProperties.Fields.CONNECTION_PROVIDER.databaseName();
    }

    @Override
    public String domainForeignKeyName() {
        return "FK_PR1_EIWEB_CT";
    }

    @Override
    public Class<EIWebConnectionProperties> persistenceClass() {
        return EIWebConnectionProperties.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table) {
        table
            .column(EIWebConnectionProperties.Fields.IP_ADDRESS.databaseName())
            .varChar()
            .map(EIWebConnectionProperties.Fields.IP_ADDRESS.javaName())
            .add();
    }

}