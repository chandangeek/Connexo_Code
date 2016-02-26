package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.cps.impl.metrology.TranslationKeys;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "c.e.j.metering.cps.impl.UsagePointTechnicalElectricityCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class UsagePointTechnicalElectricityCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointTechnicalElectricityDomainExtension> {

    private volatile PropertySpecService propertySpecService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;
    private static final String ID = "c.e.j.mtr.cps.impl.UsagePointTechnicalElectricityCustomPropertySet";

    public UsagePointTechnicalElectricityCustomPropertySet() {
    }

    public UsagePointTechnicalElectricityCustomPropertySet(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
        activate();
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(TranslationInstaller.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Activate
    public void activate() {
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.CPS_TECHNICAL_ELECTRICITY_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointTechnicalElectricityDomainExtension> getPersistenceSupport() {
        return new UsagePointTechnicalElectricityPersistenceSupport(thesaurus);
    }

    @Override
    public boolean isRequired() {
        return false;
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
        PropertySpec crossSectionalAreaSpec = propertySpecService
                .stringSpec()
                .named(UsagePointTechnicalElectricityDomainExtension.Fields.CROSS_SECTIONAL_AREA.javaName(), TranslationKeys.CPS_TECHNICAL_PROPERTIES_CROSS_SECTIONAL_AREA)
                .describedAs(TranslationKeys.CPS_TECHNICAL_PROPERTIES_CROSS_SECTIONAL_AREA_DESCRIPTION)
                .fromThesaurus(this.thesaurus)
                .addValues("EU", "NA")
                .finish();
        //unused until QuantityFactory is created
//        PropertySpec cableLocationSpec = propertySpecService
//                .stringSpec()
//                .named(UsagePointTechnicalElectricityDomainExtension.Fields.CABLE_LOCATION.javaName(), TranslationKeys.CPS_TECHNICAL_PROPERTIES_CABLE_LOCATION)
//                .describedAs(TranslationKeys.CPS_TECHNICAL_PROPERTIES_CABLE_LOCATION_DESCRIPTION)
//                .fromThesaurus(this.thesaurus)
//                .finish();
        PropertySpec volatageLevelSpec = propertySpecService
                .stringSpec()
                .named(UsagePointTechnicalElectricityDomainExtension.Fields.VOLTAGE_LEVEL.javaName(), TranslationKeys.CPS_TECHNICAL_PROPERTIES_VOLTAGE_LEVEL)
                .fromThesaurus(this.thesaurus)
                .addValues("low", "medium", "high")
                .finish();
        return Arrays.asList(crossSectionalAreaSpec,
                //  cableLocationSpec,
                volatageLevelSpec);
    }

    private class UsagePointTechnicalElectricityPersistenceSupport implements PersistenceSupport<UsagePoint, UsagePointTechnicalElectricityDomainExtension> {

        private Thesaurus thesaurus;

        public UsagePointTechnicalElectricityPersistenceSupport(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }

        @Override
        public String componentName() {
            return UsagePointTechnicalSeeds.COMPONENT_NAME.get();
        }

        @Override
        public String tableName() {
            return UsagePointTechnicalSeeds.TABLE_NAME.get();
        }

        @Override
        public String domainFieldName() {
            return UsagePointTechnicalElectricityDomainExtension.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return UsagePointTechnicalSeeds.FK_CPS_DEVICE_TECHNICAL.get();
        }

        @Override
        public Class<UsagePointTechnicalElectricityDomainExtension> persistenceClass() {
            return UsagePointTechnicalElectricityDomainExtension.class;
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
            table.column(UsagePointTechnicalElectricityDomainExtension.Fields.CROSS_SECTIONAL_AREA.databaseName())
                    .varChar(255)
                    .map(UsagePointTechnicalElectricityDomainExtension.Fields.CROSS_SECTIONAL_AREA.javaName())
                    .add();
            //unused until QuantityFactory is created
//            table.column(UsagePointTechnicalElectricityDomainExtension.Fields.CABLE_LOCATION.databaseName())
//                    .varChar(255)
//                    .map(UsagePointTechnicalElectricityDomainExtension.Fields.CABLE_LOCATION.javaName())
//                    .add();
            table.column(UsagePointTechnicalElectricityDomainExtension.Fields.VOLTAGE_LEVEL.databaseName())
                    .varChar(255)
                    .map(UsagePointTechnicalElectricityDomainExtension.Fields.VOLTAGE_LEVEL.javaName())
                    .add();
        }
    }
}

