/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.doa.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.slp.SyntheticLoadProfile;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.elster.jupiter.metering.cps.doa.ConsumptionAllocation",
        service = CustomPropertySet.class,
        property = "name=ConsumptionAllocation",
        immediate = true)
@SuppressWarnings("unused")
public class ConsumptionAllocationCustomPropertySet implements CustomPropertySet<UsagePoint, ConsumptionAllocationDomainExtension> {

    static final String ID = "com.elster.jupiter.metering.cps.doa.ConsumptionAllocation";
    private static final String TABLE_NAME = "DOA_UP_CONSO_ALLOCATION";
    private static final String FK_CPS_USAGEPOINT = "FK_DOA_UP_CONSO_ALLOCATION";
    private static final String COMPONENT_NAME = "DOA";
    private volatile Thesaurus thesaurus;  //lori

    private PropertySpecService propertySpecService;
    private MeteringService meteringService;

    @Reference
    @SuppressWarnings("unused") // Called by OSGi framework
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    @SuppressWarnings("unused") // Called by OSGi framework
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return this.getThesaurus().getFormat(TranslationKeys.DOA_CPS_COMSUMPTION_ALLOCATION_NAME).format();
    }

    public ConsumptionAllocationCustomPropertySet(){}

    @Inject
    public ConsumptionAllocationCustomPropertySet(Thesaurus thesaurus, NlsService nlsService ){
        this.thesaurus = thesaurus;
        this.setNlsService(nlsService);
    }

    public Thesaurus getThesaurus(){
        return this.thesaurus;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(TranslationInstaller.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }


    @Override
    public String getDomainClassDisplayName(){
        return this.thesaurus.getFormat(TranslationKeys.DOMAIN_NAME_USAGEPOINT).format();
    }

    @Override
    public PersistenceSupport<UsagePoint, ConsumptionAllocationDomainExtension> getPersistenceSupport() {
        return new UsagePointGeneralPersistenceSupport();
    }

    @Override
    public boolean isRequired() {
        return false;
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
        PropertySpec clientType = this.propertySpecService
                .stringSpec()
                .named(ConsumptionAllocationDomainExtension.Fields.CLIENT_TYPE.javaName(), "Client type")
                .describedAs("Type of client")
                .markRequired()
                .finish();
        PropertySpec estimatedAnnualConsumption = propertySpecService
                .bigDecimalSpec()
                .named(ConsumptionAllocationDomainExtension.Fields.ESTIMATED_ANNUAL_CONSUMPTION.javaName(), "Estimated annual consumption")
                .describedAs("Estimated annul consumption (kWh)")
                .markRequired()
                .finish();
        PropertySpec syntheticLoadProfile = propertySpecService
                .referenceSpec(SyntheticLoadProfile.class)
                .named(ConsumptionAllocationDomainExtension.Fields.SYNTHETIC_LOAD_PROFILE.javaName(), "SLP")
                .describedAs("Synthetic load profile that matched the type of client")
                .markRequired()
                .finish();

        return Arrays.asList(
                clientType,
                estimatedAnnualConsumption,
                syntheticLoadProfile);
    }

    private class UsagePointGeneralPersistenceSupport implements PersistenceSupport<UsagePoint, ConsumptionAllocationDomainExtension> {

        @Override
        public String application() {
            return COMPONENT_NAME;
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
            return ConsumptionAllocationDomainExtension.Fields.USAGEPOINT.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_USAGEPOINT;
        }

        @Override
        public Class<ConsumptionAllocationDomainExtension> persistenceClass() {
            return ConsumptionAllocationDomainExtension.class;
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
            table.column(ConsumptionAllocationDomainExtension.Fields.CLIENT_TYPE.databaseName())
                    .varChar()
                    .map(ConsumptionAllocationDomainExtension.Fields.CLIENT_TYPE.javaName())
                    .notNull()
                    .add();
            table.column(ConsumptionAllocationDomainExtension.Fields.ESTIMATED_ANNUAL_CONSUMPTION.databaseName())
                    .number()
                    .map(ConsumptionAllocationDomainExtension.Fields.ESTIMATED_ANNUAL_CONSUMPTION.javaName())
                    .notNull()
                    .add();
            Column slp = table.column(ConsumptionAllocationDomainExtension.Fields.SYNTHETIC_LOAD_PROFILE.databaseName())
                    .number()
                    .notNull()
                    .add();
            table
                .foreignKey("FK_DOA_CONSO_ALLOC_SLP")
                .on(slp)
                .references(SyntheticLoadProfile.class)
                .map(ConsumptionAllocationDomainExtension.Fields.SYNTHETIC_LOAD_PROFILE.javaName())
                .add();
        }

        @Override
        public String columnNameFor(PropertySpec propertySpec) {
            return EnumSet
                    .complementOf(EnumSet.of(ConsumptionAllocationDomainExtension.Fields.USAGEPOINT))
                    .stream()
                    .filter(each -> each.javaName().equals(propertySpec.getName()))
                    .findFirst()
                    .map(ConsumptionAllocationDomainExtension.Fields::databaseName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown property spec: " + propertySpec.getName()));
        }

    }

}