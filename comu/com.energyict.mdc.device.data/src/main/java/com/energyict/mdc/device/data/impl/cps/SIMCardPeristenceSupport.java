/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl.cps;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import java.util.EnumSet;
import java.util.List;

public class SIMCardPeristenceSupport extends AbstractDeviceTypePersistenceSupport<SIMCardDomainExtension> {

    public static final String PREFIX = "D01";
    public static final String TABLE_NAME = PREFIX + "SIM_CARD";
    public static final String FOREIGN_KEY = "FK_CPS_" + TABLE_NAME;

    public SIMCardPeristenceSupport(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @Override
    public String componentName() {
        return PREFIX;
    }

    @Override
    public String tableName() {
        return TABLE_NAME;
    }

    @Override
    public String domainFieldName() {
        return SIMCardDomainExtension.FieldNames.DOMAIN.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return FOREIGN_KEY;
    }

    @Override
    public Class<SIMCardDomainExtension> persistenceClass() {
        return SIMCardDomainExtension.class;
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
        table.column(SIMCardDomainExtension.FieldNames.ICCID.databaseName())
                .varChar()
                .map(SIMCardDomainExtension.FieldNames.ICCID.javaName())
                .notNull()
                .add();
        table.column(SIMCardDomainExtension.FieldNames.PROVIDER.databaseName())
                .varChar()
                .map(SIMCardDomainExtension.FieldNames.PROVIDER.javaName())
                .notNull()
                .add();
        table.column(SIMCardDomainExtension.FieldNames.ACTIVE_IMSI.databaseName())
                .varChar()
                .map(SIMCardDomainExtension.FieldNames.ACTIVE_IMSI.javaName())
                .add();
        table.column(SIMCardDomainExtension.FieldNames.INACTIVE_IMSI_FIRST.databaseName())
                .varChar()
                .map(SIMCardDomainExtension.FieldNames.INACTIVE_IMSI_FIRST.javaName())
                .add();
        table.column(SIMCardDomainExtension.FieldNames.INACTIVE_IMSI_SECOND.databaseName())
                .varChar()
                .map(SIMCardDomainExtension.FieldNames.INACTIVE_IMSI_SECOND.javaName())
                .add();
        table.column(SIMCardDomainExtension.FieldNames.INACTIVE_IMSI_THIRD.databaseName())
                .varChar()
                .map(SIMCardDomainExtension.FieldNames.INACTIVE_IMSI_THIRD.javaName())
                .add();
        table.column(SIMCardDomainExtension.FieldNames.BATCH_ID.databaseName())
                .varChar()
                .map(SIMCardDomainExtension.FieldNames.BATCH_ID.javaName())
                .add();
        table.column(SIMCardDomainExtension.FieldNames.CARD_FORMAT.databaseName())
                .varChar()
                .map(SIMCardDomainExtension.FieldNames.CARD_FORMAT.javaName())
                .add();
        table.column(SIMCardDomainExtension.FieldNames.STATUS.databaseName())
                .varChar()
                .map(SIMCardDomainExtension.FieldNames.STATUS.javaName())
                .add();
    }

    @Override
    public String columnNameFor(PropertySpec propertySpec) {
        return EnumSet.complementOf(EnumSet.of(SIMCardDomainExtension.FieldNames.DOMAIN)).stream()
                .filter(each -> each.javaName().equals(propertySpec.getName())).findFirst()
                .map(SIMCardDomainExtension.FieldNames::databaseName)
                .orElseThrow(() -> new IllegalArgumentException(thesaurus.getFormat(MessageSeeds.UNKNOWN_PROPERTY)
                        .format(propertySpec.getName())));
    }
}