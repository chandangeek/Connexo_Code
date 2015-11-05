package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.protocol.api.ConnectionType;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.google.inject.Module;

import java.util.Optional;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-05 (13:24)
 */
public class IpConnectionPersistenceSupport implements PersistenceSupport<ConnectionType, IpConnectionPropertyValues> {

    @Override
    public String componentName() {
        return "TST";
    }

    @Override
    public String tableName() {
        return "TST_IP_PROPS";
    }

    @Override
    public String domainFieldName() {
        return "connectionType";
    }

    @Override
    public String domainForeignKeyName() {
        return "FK_TST_IP_PROPS_CT";
    }

    @Override
    public Class<IpConnectionPropertyValues> persistenceClass() {
        return IpConnectionPropertyValues.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table) {
        table
            .column(IpConnectionProperties.IP_ADDRESS.propertyName())
            .notNull()
            .varChar()
            .add();
        table
            .column(IpConnectionProperties.PORT.propertyName())
            .number()
            .conversion(ColumnConversion.NUMBER2INT)
            .add();
    }

}