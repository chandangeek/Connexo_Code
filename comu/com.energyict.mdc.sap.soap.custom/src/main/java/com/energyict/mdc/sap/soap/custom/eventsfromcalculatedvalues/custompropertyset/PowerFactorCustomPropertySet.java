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
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.TranslationKeys;
import com.google.inject.Module;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.APPLICATION_NAME;

public class PowerFactorCustomPropertySet implements CustomPropertySet<Device, PowerFactorDomainExtension> {
    public static final String CPS_ID = PowerFactorCustomPropertySet.class.getName();
    static final String MODEL_NAME = "PF1";

    // Common for all domain objects
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    private final BigDecimal defaultSetpointThreshold = new BigDecimal(0.9);
    private final BigDecimal defaultHysteresisPercentage = new BigDecimal(0.5);
    private final boolean defaultFlag = false;

    PowerFactorCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    @Override
    public String getId() {
        return CPS_ID;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.CPS_DEVICE_POWER_FACTOR).format();
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
    public PersistenceSupport<Device, PowerFactorDomainExtension> getPersistenceSupport() {
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
                        .named(PowerFactorDomainExtension.FieldNames.SETPOINT_THRESHOLD.javaName(), TranslationKeys.CPS_DEVICE_SETPOINT_THRESHOLD)
                        .describedAs(TranslationKeys.CPS_DEVICE_SETPOINT_THRESHOLD_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .setDefaultValue(defaultSetpointThreshold)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .boundedBigDecimalSpec(BigDecimal.ZERO, BigDecimal.valueOf(100))
                        .named(PowerFactorDomainExtension.FieldNames.HYSTERESIS_PERCENTAGE.javaName(), TranslationKeys.CPS_DEVICE_HYSTERESIS_PERCENTAGE)
                        .describedAs(TranslationKeys.CPS_DEVICE_HYSTERESIS_PERCENTAGE_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .setDefaultValue(defaultHysteresisPercentage)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(PowerFactorDomainExtension.FieldNames.FLAG.javaName(), TranslationKeys.CPS_DEVICE_FLAG)
                        .describedAs(TranslationKeys.CPS_DEVICE_FLAG_DESCRIPTION)
                        .fromThesaurus(thesaurus)
                        .setDefaultValue(defaultFlag)
                        .markRequired()
                        .finish()
        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<Device, PowerFactorDomainExtension> {
        private final String TABLE_NAME = "SAP_CAS_ECV_PF1";
        private final String FK = "FK_SAP_CAS_ECV_PF1";

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
            return PowerFactorDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<PowerFactorDomainExtension> persistenceClass() {
            return PowerFactorDomainExtension.class;
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
            table.column(PowerFactorDomainExtension.FieldNames.SETPOINT_THRESHOLD.databaseName()).number()
                    .map(PowerFactorDomainExtension.FieldNames.SETPOINT_THRESHOLD.javaName()).notNull()
                    .add();
            table.column(PowerFactorDomainExtension.FieldNames.HYSTERESIS_PERCENTAGE.databaseName()).number()
                    .map(PowerFactorDomainExtension.FieldNames.HYSTERESIS_PERCENTAGE.javaName()).notNull()
                    .add();
            table.column(PowerFactorDomainExtension.FieldNames.FLAG.databaseName()).bool()
                    .map(PowerFactorDomainExtension.FieldNames.FLAG.javaName()).notNull()
                    .add();
        }

        @Override
        public String application() {
            return APPLICATION_NAME;
        }
    }
}
