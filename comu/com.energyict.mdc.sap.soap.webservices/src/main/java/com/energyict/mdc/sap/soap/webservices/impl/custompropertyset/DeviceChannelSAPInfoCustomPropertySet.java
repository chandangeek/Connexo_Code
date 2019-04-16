/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.custompropertyset;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.config.ChannelSpec;

import com.google.inject.Module;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class DeviceChannelSAPInfoCustomPropertySet implements CustomPropertySet<ChannelSpec, DeviceChannelSAPInfoDomainExtension> {
    public static final String CPS_ID = DeviceChannelSAPInfoCustomPropertySet.class.getName();
    static final String MODEL_NAME = "DI2";

    // Common for all domain objects
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    DeviceChannelSAPInfoCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public String getId() {
        return CPS_ID;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.CPS_DEVICE_CHANNEL_SAP_INFO).format();
    }

    @Override
    public Class<ChannelSpec> getDomainClass() {
        return ChannelSpec.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return thesaurus.getFormat(TranslationKeys.DOMAIN_NAME_CHANNEL).format();
    }

    @Override
    public PersistenceSupport<ChannelSpec, DeviceChannelSAPInfoDomainExtension> getPersistenceSupport() {
        return new CustomPropertyPersistenceSupport();
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public boolean isVersioned() {
        return true;
    }

    @Override
    public Set<ViewPrivilege> defaultViewPrivileges() {
        return EnumSet.allOf(ViewPrivilege.class);
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return EnumSet.allOf(EditPrivilege.class);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.singletonList(
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(DeviceChannelSAPInfoDomainExtension.FieldNames.LOGICAL_REGISTER_NUMBER.javaName(), TranslationKeys.CPS_LOGICAL_REGISTER_NUMBER)
                        .describedAs(TranslationKeys.CPS_DEVICE_CHANNEL_IDENTIFIER_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish());
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ChannelSpec, DeviceChannelSAPInfoDomainExtension> {
        private final String TABLE_NAME = "SAP_CAS_DI2";
        private final String FK = "FK_SAP_CAS_DI2";
        private final String IDX = "IDX_SAP_CAS_DI2_LRN";

        @Override
        public String componentName() {
            return MODEL_NAME;
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return DeviceChannelSAPInfoDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<DeviceChannelSAPInfoDomainExtension> persistenceClass() {
            return DeviceChannelSAPInfoDomainExtension.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.empty();
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.singletonList(
                    table.column(DeviceChannelSAPInfoDomainExtension.FieldNames.DEVICE_ID.name())
                            .number()
                            .map(DeviceChannelSAPInfoDomainExtension.FieldNames.DEVICE_ID.javaName())
                            .conversion(ColumnConversion.NUMBER2LONG)
                            .notNull()
                            .add());
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            Column lrnColumn = table.column(DeviceChannelSAPInfoDomainExtension.FieldNames.LOGICAL_REGISTER_NUMBER.databaseName())
                    .number()
                    .map(DeviceChannelSAPInfoDomainExtension.FieldNames.LOGICAL_REGISTER_NUMBER.javaName())
                    .add();
            table.index(IDX).on(lrnColumn).add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}

