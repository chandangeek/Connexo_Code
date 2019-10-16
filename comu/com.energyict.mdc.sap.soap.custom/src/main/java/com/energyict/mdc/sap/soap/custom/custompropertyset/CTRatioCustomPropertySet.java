/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.custompropertyset;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
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

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class CTRatioCustomPropertySet implements CustomPropertySet<Device, CTRatioDomainExtension> {
    public static final String CPS_ID = CTRatioCustomPropertySet.class.getName();
    static final String MODEL_NAME = "CR1";

    // Common for all domain objects
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    private final BigDecimal defaultCTRatio = new BigDecimal(100);
    private final boolean defaultFlag = false;

    CTRatioCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public String getId() {
        return CPS_ID;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.CPS_DEVICE_CT_RATIO).format();
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
    public PersistenceSupport<Device, CTRatioDomainExtension> getPersistenceSupport() {
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
                        .named(CTRatioDomainExtension.FieldNames.CT_RATIO.javaName(), TranslationKeys.CPS_DEVICE_CT_RATIO)
                        .describedAs(TranslationKeys.CPS_DEVICE_CT_RATIO_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .setDefaultValue(defaultCTRatio)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(CTRatioDomainExtension.FieldNames.FLAG.javaName(), TranslationKeys.CPS_DEVICE_FLAG)
                        .describedAs(TranslationKeys.CPS_DEVICE_FLAG_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .setDefaultValue(defaultFlag)
                        .markRequired()
                        .finish()
        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<Device, CTRatioDomainExtension> {
        private final String TABLE_NAME = "SAP_CAS_ECV_CR1";
        private final String FK = "FK_SAP_CAS_ECV_CR1";

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
            return CTRatioDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<CTRatioDomainExtension> persistenceClass() {
            return CTRatioDomainExtension.class;
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
            table.column(CTRatioDomainExtension.FieldNames.CT_RATIO.databaseName()).number()
                    .map(CTRatioDomainExtension.FieldNames.CT_RATIO.javaName()).notNull()
                    .add();
            table.column(CTRatioDomainExtension.FieldNames.FLAG.databaseName()).bool()
                    .map(CTRatioDomainExtension.FieldNames.FLAG.javaName()).notNull()
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
