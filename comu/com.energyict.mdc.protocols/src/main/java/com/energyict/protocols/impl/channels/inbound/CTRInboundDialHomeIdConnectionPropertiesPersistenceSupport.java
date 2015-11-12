package com.energyict.protocols.impl.channels.inbound;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;

import com.google.inject.Module;

import java.util.Optional;

/**
 * Provides an implementation for the {@link PersistenceSupport} interface
 * to support the persistence of {@link CTRInboundDialHomeIdConnectionProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (15:46)
 */
public class CTRInboundDialHomeIdConnectionPropertiesPersistenceSupport implements PersistenceSupport<ConnectionProvider, CTRInboundDialHomeIdConnectionProperties> {

    @Override
    public String componentName() {
        return DeviceProtocolService.COMPONENT_NAME;
    }

    @Override
    public String tableName() {
        return DeviceProtocolService.COMPONENT_NAME + "_CTR_DIALHOMEID_CT";
    }

    @Override
    public String domainFieldName() {
        return CTRInboundDialHomeIdConnectionProperties.Fields.CONNECTION_PROVIDER.javaName();
    }

    @Override
    public String domainColumnName() {
        return CTRInboundDialHomeIdConnectionProperties.Fields.CONNECTION_PROVIDER.databaseName();
    }

    @Override
    public String domainForeignKeyName() {
        return "FK_PR1_DIALHOMEID_CT";
    }

    @Override
    public Class<CTRInboundDialHomeIdConnectionProperties> persistenceClass() {
        return CTRInboundDialHomeIdConnectionProperties.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table) {
        this.addRequiredStringColumnTo(table, CTRInboundDialHomeIdConnectionProperties.Fields.DIAL_HOME_ID);
    }

    private void addRequiredStringColumnTo(Table table, CTRInboundDialHomeIdConnectionProperties.Fields fieldName) {
        table
            .column(fieldName.databaseName())
            .varChar()
            .notNull()
            .map(fieldName.javaName())
            .add();
    }

}