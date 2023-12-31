/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TimeOfUseItemPersistenceSupport implements PersistenceSupport<ServiceCall, TimeOfUseItemDomainExtension> {

    private static final String TABLE_NAME = TimeOfUseCampaignService.COMPONENT_NAME + "_" + "TU2_ITEMS";
    private static final String FK_NAME = "FK_" + TABLE_NAME;
    public static final String COMPONENT_NAME = "TU2";
    private final TimeOfUseCampaignServiceImpl timeOfUseCampaignService;

    public TimeOfUseItemPersistenceSupport(TimeOfUseCampaignServiceImpl timeOfUseCampaignService) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;
    }

    @Override
    public String componentName() {
        return COMPONENT_NAME;
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
        return Optional.of(new AbstractModule() {
            @Override
            public void configure() {
                bind(TimeOfUseCampaignServiceImpl.class).toInstance(timeOfUseCampaignService);
                bind(TimeOfUseCampaignService.class).toInstance(timeOfUseCampaignService);
            }
        });
    }

    @Override
    public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
        return Collections.emptyList();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
        table.column(TimeOfUseItemDomainExtension.FieldNames.PARENT_SERVICE_CALL.databaseName())
                .number()
                .conversion(ColumnConversion.NUMBER2LONG)
                .map(TimeOfUseItemDomainExtension.FieldNames.PARENT_SERVICE_CALL.javaName())
                .notNull()
                .add();
        Column device = table.column(TimeOfUseItemDomainExtension.FieldNames.DEVICE.databaseName())
                .number()
                .notNull()
                .add();
        table.foreignKey(FK_NAME + "_DEV")
                .on(device)
                .references(Device.class)
                .map(TimeOfUseItemDomainExtension.FieldNames.DEVICE.javaName())
                .add();
        Column deviceMessage = table.column(TimeOfUseItemDomainExtension.FieldNames.DEVICE_MESSAGE.databaseName())
                .number()
                .add();
        table.foreignKey(FK_NAME + "_DEV_MES")
                .on(deviceMessage)
                .map(TimeOfUseItemDomainExtension.FieldNames.DEVICE_MESSAGE.javaName())
                .references(DeviceMessage.class)
                .add();
        table.column(TimeOfUseItemDomainExtension.FieldNames.STEP_OF_UPDATE.databaseName())
                .installValue("0")
                .since(Version.version(10, 7))
                .number()
                .conversion(ColumnConversion.NUMBER2LONG)
                .map(TimeOfUseItemDomainExtension.FieldNames.STEP_OF_UPDATE.javaName())
                .add();
    }

    @Override
    public String application() {
        return "MultiSense";
    }
}