/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TimeOfUseItemPersistenceSupport implements PersistenceSupport<ServiceCall, TimeOfUseItemDomainExtension> {

    private static final String TABLE_NAME = TimeOfUseCampaignService.COMPONENT_NAME + "_" + "TU2_ITEMS";
    private static final String FK_NAME = "FK_" + TABLE_NAME;

    @Override
    public String componentName() {
        return "TU2";
    }

    @Override
    public String tableName() {
        return TABLE_NAME;
    }

    @Override
    public String domainFieldName() {
        return TimeOfUseItemDomainExtension.FieldNames.DOMAIN.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return FK_NAME;
    }

    @Override
    public Class<TimeOfUseItemDomainExtension> persistenceClass() {
        return TimeOfUseItemDomainExtension.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.empty();
    }

    @Override
    public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
        return Collections.emptyList();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
        Column device = table.column(TimeOfUseItemDomainExtension.FieldNames.DEVICE.databaseName())
                .number()
                .notNull()
                .add();
        table.foreignKey(FK_NAME + "_DEV")
                .on(device)
                .references(Device.class)
                .map(TimeOfUseItemDomainExtension.FieldNames.DEVICE.javaName())
                .add();
    }

    @Override
    public String application() {
        return "MultiSense";
    }
}