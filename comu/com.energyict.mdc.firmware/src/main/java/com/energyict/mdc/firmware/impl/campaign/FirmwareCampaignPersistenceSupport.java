/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.firmware.impl.campaign;

import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareCampaignService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.orm.Version.version;

public class FirmwareCampaignPersistenceSupport implements PersistenceSupport<ServiceCall, FirmwareCampaignDomainExtension> {

    public static final String TABLE_NAME = FirmwareCampaignService.COMPONENT_NAME + "_" + "FC1_CAMPAIGN";
    private static final String FK_NAME = "FK_" + TABLE_NAME;
    static final String COMPONENT_NAME = "FC1";
    private final FirmwareServiceImpl firmwareService;

    public FirmwareCampaignPersistenceSupport(FirmwareServiceImpl firmwareService) {
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
        return FirmwareCampaignDomainExtension.FieldNames.DOMAIN.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return FK_NAME;
    }

    @Override
    public Class<FirmwareCampaignDomainExtension> persistenceClass() {
        return FirmwareCampaignDomainExtension.class;
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
        Column name = table.column(FirmwareCampaignDomainExtension.FieldNames.NAME.databaseName())
                .varChar()
                .map(FirmwareCampaignDomainExtension.FieldNames.NAME.javaName())
                .notNull()
                .add();
        table.column(FirmwareCampaignDomainExtension.FieldNames.DEVICE_GROUP.databaseName())
                .varChar()
                .map(FirmwareCampaignDomainExtension.FieldNames.DEVICE_GROUP.javaName())
                .notNull()
                .add();
        table.column(FirmwareCampaignDomainExtension.FieldNames.MANAGEMENT_OPTION.databaseName())
                .number()
                .conversion(ColumnConversion.NUMBER2ENUM)
                .map(FirmwareCampaignDomainExtension.FieldNames.MANAGEMENT_OPTION.javaName())
                .notNull()
                .add();
        table.column(FirmwareCampaignDomainExtension.FieldNames.FIRMWARE_TYPE.databaseName())
                .number()
                .conversion(ColumnConversion.NUMBER2ENUM)
                .map(FirmwareCampaignDomainExtension.FieldNames.FIRMWARE_TYPE.javaName())
                .notNull()
                .add();
        table.column(FirmwareCampaignDomainExtension.FieldNames.UPLOAD_PERIOD_START.databaseName())
                .number()
                .conversion(ColumnConversion.NUMBER2INSTANT)
                .map(FirmwareCampaignDomainExtension.FieldNames.UPLOAD_PERIOD_START.javaName())
                .add();
        table.column(FirmwareCampaignDomainExtension.FieldNames.UPLOAD_PERIOD_END.databaseName())
                .number()
                .conversion(ColumnConversion.NUMBER2INSTANT)
                .map(FirmwareCampaignDomainExtension.FieldNames.UPLOAD_PERIOD_END.javaName())
                .add();
        Column deviceType = table.column(FirmwareCampaignDomainExtension.FieldNames.DEVICE_TYPE.databaseName())
                .number()
                .notNull()
                .add();
        table.column(FirmwareCampaignDomainExtension.FieldNames.ACTIVATION_DATE.databaseName())
                .number()
                .conversion(ColumnConversion.NUMBER2INSTANT)
                .map(FirmwareCampaignDomainExtension.FieldNames.ACTIVATION_DATE.javaName())
                .add();
        table.column("VALIDATION_TIMEOUT_VALUE")
                .number()
                .conversion(ColumnConversion.NUMBER2INT)
                .map(FirmwareCampaignDomainExtension.FieldNames.VALIDATION_TIMEOUT.javaName() + ".count")
                .notNull()
                .since(version(10, 4, 1))
                .installValue("1")
                .add();
        table.column("VALIDATION_TIMEOUT_UNIT")
                .number()
                .conversion(ColumnConversion.NUMBER2INT)
                .map(FirmwareCampaignDomainExtension.FieldNames.VALIDATION_TIMEOUT.javaName() + ".timeUnitCode")
                .notNull()
                .since(version(10, 4, 1))
                .installValue(Integer.toString(TimeDuration.TimeUnit.HOURS.getCode()))
                .add();
        table.foreignKey(FK_NAME + "_DT")
                .on(deviceType)
                .references(DeviceType.class)
                .map(FirmwareCampaignDomainExtension.FieldNames.DEVICE_TYPE.javaName())
                .add();
        table.unique("UK_" + TABLE_NAME + "_NAME").on(name).add();
    }

    @Override
    public String application() {
        return "MultiSense";
    }
}