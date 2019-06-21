/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.firmware.impl.campaign;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.FirmwareCampaignService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FirmwareCampaignItemPersistenceSupport implements PersistenceSupport<ServiceCall, FirmwareCampaignItemDomainExtension> {

    private static final String TABLE_NAME = FirmwareCampaignService.COMPONENT_NAME + "_" + "FC2_ITEMS";
    private static final String FK_NAME = "FK_" + TABLE_NAME;
    public static final String COMPONENT_NAME = "FC2";
    private final FirmwareServiceImpl firmwareService;

    public FirmwareCampaignItemPersistenceSupport(FirmwareServiceImpl firmwareService) {
        this.firmwareService = firmwareService;
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
        return FirmwareCampaignItemDomainExtension.FieldNames.DOMAIN.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return FK_NAME;
    }

    @Override
    public Class<FirmwareCampaignItemDomainExtension> persistenceClass() {
        return FirmwareCampaignItemDomainExtension.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.of(new AbstractModule() {
            @Override
            public void configure() {
                bind(FirmwareServiceImpl.class).toInstance(firmwareService);
                bind(FirmwareService.class).toInstance(firmwareService);
            }
        });
    }

    @Override
    public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
        return Collections.emptyList();
    }

    @Override
    public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
        Column parent = table.column(FirmwareCampaignItemDomainExtension.FieldNames.PARENT.databaseName())
                .number()
                .notNull()
                .add();
        table.foreignKey(FK_NAME + "_PARENT")
                .on(parent)
                .references(ServiceCall.class)
                .map(FirmwareCampaignItemDomainExtension.FieldNames.PARENT.javaName())
                .add();
        Column device = table.column(FirmwareCampaignItemDomainExtension.FieldNames.DEVICE.databaseName())
                .number()
                .notNull()
                .add();
        table.foreignKey(FK_NAME + "_DEV")
                .on(device)
                .references(Device.class)
                .map(FirmwareCampaignItemDomainExtension.FieldNames.DEVICE.javaName())
                .add();
        Column deviceMessage = table.column(FirmwareCampaignItemDomainExtension.FieldNames.DEVICE_MESSAGE.databaseName())
                .number()
                .add();
        table.foreignKey(FK_NAME + "_DEV_MES")
                .on(deviceMessage)
                .map(FirmwareCampaignItemDomainExtension.FieldNames.DEVICE_MESSAGE.javaName())
                .references(DeviceMessage.class)
                .add();
    }

    @Override
    public String application() {
        return "MultiSense";
    }
}