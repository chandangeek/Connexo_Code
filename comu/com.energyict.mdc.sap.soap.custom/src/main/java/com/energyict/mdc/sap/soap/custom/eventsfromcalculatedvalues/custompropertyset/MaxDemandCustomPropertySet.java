/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.EnumFactory;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.common.device.data.Device;
import com.google.inject.Module;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.CustomPropertySets.APPLICATION_NAME;

public class MaxDemandCustomPropertySet implements CustomPropertySet<Device, MaxDemandDomainExtension> {
    public static final String CPS_ID = MaxDemandCustomPropertySet.class.getName();
    static final String MODEL_NAME = "MD1";

    // Common for all domain objects
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    private static final BigDecimal defaultConnectedLoad = new BigDecimal(0.5);
    private static final Unit defaultUnit = Unit.kW;
    private static final boolean defaultCheckEnabled = false;

    MaxDemandCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public String getId() {
        return CPS_ID;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.CPS_DEVICE_MAX_DEMAND).format();
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
    public PersistenceSupport<Device, MaxDemandDomainExtension> getPersistenceSupport() {
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
        return Arrays.asList(
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(MaxDemandDomainExtension.FieldNames.CONNECTED_LOAD.javaName(), TranslationKeys.CPS_DEVICE_CONNECTED_LOAD)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .setDefaultValue(defaultConnectedLoad)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new EnumFactory(Unit.class))
                        .named(MaxDemandDomainExtension.FieldNames.UNIT.javaName(), TranslationKeys.CPS_DEVICE_UNIT)
                        .describedAs(TranslationKeys.CPS_DEVICE_UNIT_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .addValues(Unit.values())
                        .markExhaustive(PropertySelectionMode.COMBOBOX)
                        .markRequired()
                        .setDefaultValue(defaultUnit)
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(MaxDemandDomainExtension.FieldNames.CHECK_ENABLED.javaName(), TranslationKeys.CPS_DEVICE_CHECK_ENABLED)
                        .fromThesaurus(thesaurus)
                        .setDefaultValue(defaultCheckEnabled)
                        .markRequired()
                        .finish()
        );
    }

    protected class CustomPropertyPersistenceSupport implements PersistenceSupport<Device, MaxDemandDomainExtension> {
        private final String TABLE_NAME = "CSE_CAS_ECV_MD1";
        private final String FK = "FK_CSE_CAS_ECV_MD1";

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
            return MaxDemandDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<MaxDemandDomainExtension> persistenceClass() {
            return MaxDemandDomainExtension.class;
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
            table.column(MaxDemandDomainExtension.FieldNames.CONNECTED_LOAD.databaseName())
                    .number()
                    .map(MaxDemandDomainExtension.FieldNames.CONNECTED_LOAD.javaName())
                    .notNull()
                    .add();
            table.column(MaxDemandDomainExtension.FieldNames.UNIT.databaseName())
                    .varChar(Table.NAME_LENGTH)
                    .map(MaxDemandDomainExtension.FieldNames.UNIT.javaName())
                    .notNull()
                    .conversion(ColumnConversion.CHAR2ENUM)
                    .add();
            table.column(MaxDemandDomainExtension.FieldNames.CHECK_ENABLED.databaseName())
                    .bool()
                    .map(MaxDemandDomainExtension.FieldNames.CHECK_ENABLED.javaName())
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
