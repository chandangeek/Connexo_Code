/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.security;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.device.BaseDevice;

import java.util.Collections;
import java.util.List;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-20 (12:28)
 */
public abstract class CommonBaseDeviceSecuritySupport<T extends PersistentDomainExtension<BaseDevice>> implements PersistenceSupport<BaseDevice, T> {

    @Override
    public String domainFieldName() {
        return CommonBaseDeviceSecurityProperties.Fields.DEVICE.javaName();
    }

    @Override
    public String domainColumnName() {
        return CommonBaseDeviceSecurityProperties.Fields.DEVICE.databaseName();
    }

    @Override
    public final List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
        return Collections.singletonList(
                table
                    .column(CommonBaseDeviceSecurityProperties.Fields.PROPERTY_SPEC_PROVIDER.databaseName())
                    .number()
                    .notNull()
                    .add());
    }

    @Override
    public final void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
        Column complete =
            table
                .column(CommonBaseDeviceSecurityProperties.Fields.COMPLETE.databaseName())
                .map(CommonBaseDeviceSecurityProperties.Fields.COMPLETE.javaName())
                .number()
                .conversion(ColumnConversion.NUMBER2BOOLEAN)
                .notNull()
                .add();
        this.addCustomPropertyColumnsTo(table, complete, customPrimaryKeyColumns);
        table
            .foreignKey(this.propertySpecProviderForeignKeyName())
            .on(customPrimaryKeyColumns.get(0))
            .map(CommonBaseDeviceSecurityProperties.Fields.PROPERTY_SPEC_PROVIDER.javaName())
            .references(SecurityPropertySpecProvider.class)
            .add();
    }

    protected abstract String propertySpecProviderForeignKeyName();

    public abstract void addCustomPropertyColumnsTo(Table table, Column completeColumn, List<Column> customPrimaryKeyColumns);

}