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
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.data.Device;

import com.google.inject.Module;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class DeviceSAPInfoCustomPropertySet implements CustomPropertySet<Device, DeviceSAPInfoDomainExtension> {
    public static final String CPS_ID = DeviceSAPInfoCustomPropertySet.class.getName();
    static final String MODEL_NAME = "DI1";

    // Common for all domain objects
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    DeviceSAPInfoCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public String getId() {
        return CPS_ID;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.CPS_DEVICE_SAP_INFO).format();
    }

    @Override
    public Class<Device> getDomainClass() {
        return Device.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return thesaurus.getFormat(TranslationKeys.DOMAIN_NAME_DEVICE).format();
    }

    @Override
    public PersistenceSupport<Device, DeviceSAPInfoDomainExtension> getPersistenceSupport() {
        return new CustomPropertyPersistenceSupport();
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public boolean isVersioned() {
        return false;
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
                        .named(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_IDENTIFIER.javaName(), TranslationKeys.CPS_DEVICE_IDENTIFIER)
                        .describedAs(TranslationKeys.CPS_DEVICE_IDENTIFIER_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .finish());
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<Device, DeviceSAPInfoDomainExtension> {
        private final String TABLE_NAME = "SAP_CAS_DI1";
        private final String FK = "FK_SAP_CAS_DI1";
        private final String IDX = "IDX_SAP_CAS_DI1_DEVID";

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
            return DeviceSAPInfoDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<DeviceSAPInfoDomainExtension> persistenceClass() {
            return DeviceSAPInfoDomainExtension.class;
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
            Column deviceIdColumn = table.column(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_IDENTIFIER.databaseName())
                    .number()
                    .map(DeviceSAPInfoDomainExtension.FieldNames.DEVICE_IDENTIFIER.javaName())
                    .add();
            table.index(IDX).on(deviceIdColumn).add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
