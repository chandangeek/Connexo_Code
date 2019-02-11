/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.tou.campaign.impl.servicecall;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TimeOfUseCampaignPersistenceSupport implements PersistenceSupport<ServiceCall, TimeOfUseCampaignDomainExtension> {

    private static final String TABLE_NAME = TimeOfUseCampaignService.COMPONENT_NAME + "_" + "TU1_CAMPAIGN";
    private static final String FK_NAME = "FK_" + TABLE_NAME;
    static final String COMPONENT_NAME = "TU1";
    private final TimeOfUseCampaignService timeOfUseCampaignService;

    public TimeOfUseCampaignPersistenceSupport(TimeOfUseCampaignService timeOfUseCampaignService) {
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
        return TimeOfUseCampaignDomainExtension.FieldNames.DOMAIN.javaName();
    }

    @Override
    public String domainForeignKeyName() {
        return FK_NAME;
    }

    @Override
    public Class<TimeOfUseCampaignDomainExtension> persistenceClass() {
        return TimeOfUseCampaignDomainExtension.class;
    }

    @Override
    public Optional<Module> module() {
        return Optional.of(new AbstractModule() {
            @Override
            public void configure() {
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
        Column name = table.column(TimeOfUseCampaignDomainExtension.FieldNames.NAME_OF_CAMPAIGN.databaseName())
                .varChar()
                .map(TimeOfUseCampaignDomainExtension.FieldNames.NAME_OF_CAMPAIGN.javaName())
                .notNull()
                .add();
        table.column(TimeOfUseCampaignDomainExtension.FieldNames.DEVICE_GROUP.databaseName())
                .varChar()
                .map(TimeOfUseCampaignDomainExtension.FieldNames.DEVICE_GROUP.javaName())
                .notNull()
                .add();
        table.column(TimeOfUseCampaignDomainExtension.FieldNames.ACTIVATION_START.databaseName())
                .number()
                .conversion(ColumnConversion.NUMBER2INSTANT)
                .map(TimeOfUseCampaignDomainExtension.FieldNames.ACTIVATION_START.javaName())
                .add();
        table.column(TimeOfUseCampaignDomainExtension.FieldNames.ACTIVATION_END.databaseName())
                .number()
                .conversion(ColumnConversion.NUMBER2INSTANT)
                .map(TimeOfUseCampaignDomainExtension.FieldNames.ACTIVATION_END.javaName())
                .add();
        Column calendar = table.column(TimeOfUseCampaignDomainExtension.FieldNames.CALENDAR.databaseName())
                .number()
                .notNull()
                .add();
        Column deviceType = table.column(TimeOfUseCampaignDomainExtension.FieldNames.DEVICE_TYPE.databaseName())
                .number()
                .notNull()
                .add();
        table.column(TimeOfUseCampaignDomainExtension.FieldNames.ACTIVATION_OPTION.databaseName())
                .varChar()
                .map(TimeOfUseCampaignDomainExtension.FieldNames.ACTIVATION_OPTION.javaName())
                .add();
        table.column(TimeOfUseCampaignDomainExtension.FieldNames.ACTIVATION_DATE.databaseName())
                .number()
                .conversion(ColumnConversion.NUMBER2INSTANT)
                .map(TimeOfUseCampaignDomainExtension.FieldNames.ACTIVATION_DATE.javaName())
                .add();
        table.column(TimeOfUseCampaignDomainExtension.FieldNames.UPDATE_TYPE.databaseName())
                .varChar()
                .map(TimeOfUseCampaignDomainExtension.FieldNames.UPDATE_TYPE.javaName())
                .add();
        table.column(TimeOfUseCampaignDomainExtension.FieldNames.VALIDATION_TIMEOUT.databaseName())
                .number()
                .conversion(ColumnConversion.NUMBER2LONG)
                .map(TimeOfUseCampaignDomainExtension.FieldNames.VALIDATION_TIMEOUT.javaName())
                .add();
        table.foreignKey(FK_NAME + "_CAL")
                .on(calendar)
                .references(Calendar.class)
                .map(TimeOfUseCampaignDomainExtension.FieldNames.CALENDAR.javaName())
                .add();
        table.foreignKey(FK_NAME + "_DT")
                .on(deviceType)
                .references(DeviceType.class)
                .map(TimeOfUseCampaignDomainExtension.FieldNames.DEVICE_TYPE.javaName())
                .add();
        table.unique("UK_"+TABLE_NAME+"_NAME").on(name).add();
    }

    @Override
    public String application() {
        return "MultiSense";
    }
}