package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.ConnectionProvider;

import com.google.inject.Module;

import java.util.Optional;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-05 (13:24)
 */
public class ModemConnectionPersistenceSupport implements PersistenceSupport<ConnectionProvider, ModemConnectionPropertyValues> {

    @Override
    public String componentName() {
        return "TST";
    }

    @Override
    public String tableName() {
        return "TST_MODEM_PROPS";
    }

    @Override
    public String domainFieldName() {
        return "connectionType";
    }

    @Override
    public String domainForeignKeyName() {
        return "FK_TST_MODEM_PROPS_CT";
    }

    @Override
    public Class<ModemConnectionPropertyValues> persistenceClass() {
        return ModemConnectionPropertyValues.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table) {
        table
            .column(ModemConnectionProperties.PHONE_NUMBER.propertyName())
            .notNull()
            .varChar()
            .add();
    }

}