package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.cps.impl.TranslationInstaller;
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

@Component(name = "c.e.j.mtr.cps.impl.mtr.UsagePointTechnicalInstallationElectricityCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class UsagePointTechnicalInstallationElectricityCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointTechnicalInstallationElectricityDomainExtension> {


    private volatile PropertySpecService propertySpecService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;
    private static final String ID = "c.e.j.m.cps.impl.mtr.UsagePointTechnicalInstallationElectricityCAS";

    public UsagePointTechnicalInstallationElectricityCustomPropertySet() {

    }

    public UsagePointTechnicalInstallationElectricityCustomPropertySet(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
        activate();
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(TranslationInstaller.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
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
        return thesaurus.getFormat(TranslationKeys.CPS_TECHNICAL_INSTALLATION_ELECTRICITY_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointTechnicalInstallationElectricityDomainExtension> getPersistenceSupport() {
        return new UsagePointTechnicalInstallationElectricityPersistenceSupport(thesaurus);
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
        //unused until QuantityFactory is created
//        PropertySpec distanceFromTheSubstationSpec = propertySpecService
//                .stringSpec()
//                .named(UsagePointTechnicalInstallationDomainExtension.Fields.DISTANCE_FROM_THE_SUBSTATION.javaName(), TranslationKeys.CPS_TECHNICAL_INSTALLATION_DISTANCE_FROM_THE_SUBSTATION)
//                .describedAs(TranslationKeys.CPS_TECHNICAL_INSTALLATION_DISTANCE_FROM_THE_SUBSTATION_DESCRIPTION)
//                .fromThesaurus(thesaurus)
//                .markRequired()
//                .finish();
        PropertySpec feederSpec = propertySpecService
                .stringSpec()
                .named(UsagePointTechnicalInstallationElectricityDomainExtension.Fields.FEEDER.javaName(), TranslationKeys.CPS_TECHNICAL_INSTALLATION_FEEDER)
                .fromThesaurus(thesaurus)
                .finish();
        //unused until QuantityFactory is created
//        PropertySpec utilizationCategorySpec = propertySpecService
//                .stringSpec()
//                .named(UsagePointTechnicalInstallationDomainExtension.Fields.UTILIZATION_CATEGORY.javaName(), TranslationKeys.CPS_TECHNICAL_INSTALLATION_UTILIZATION_CATEGORY)
//                .describedAs(TranslationKeys.CPS_TECHNICAL_INSTALLATION_UTILIZATION_CATEGORY_DESCRIPTION)
//                .fromThesaurus(thesaurus)
//                .finish();

        return Arrays.asList(
                //distanceFromTheSubstationSpec,
                feederSpec);
        // utilizationCategorySpec);
    }

    private static class UsagePointTechnicalInstallationElectricityPersistenceSupport implements PersistenceSupport<UsagePoint, UsagePointTechnicalInstallationElectricityDomainExtension> {

        public static final String TABLE_NAME = "RVK_CPS_MTR_USAGEPOINT_TECH";
        public static final String FK_CPS_DEVICE_LICENSE = "FK_CPS_MTR_USAGEPOINT_TECH";
        public static final String COMPONENT_NAME = "MTR_INST";
        private Thesaurus thesaurus;

        public UsagePointTechnicalInstallationElectricityPersistenceSupport(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
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
            return UsagePointTechnicalInstallationElectricityDomainExtension.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_LICENSE;
        }

        @Override
        public Class<UsagePointTechnicalInstallationElectricityDomainExtension> persistenceClass() {
            return UsagePointTechnicalInstallationElectricityDomainExtension.class;
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
            //unused until QuantityFactory is created
//            table.column(UsagePointTechnicalInstallationDomainExtension.Fields.DISTANCE_FROM_THE_SUBSTATION.databaseName())
//                    .varChar(255)
//                    .map(UsagePointTechnicalInstallationDomainExtension.Fields.DISTANCE_FROM_THE_SUBSTATION.javaName())
//                    .notNull()
//                    .add();
            table.column(UsagePointTechnicalInstallationElectricityDomainExtension.Fields.FEEDER.databaseName())
                    .varChar(255)
                    .map(UsagePointTechnicalInstallationElectricityDomainExtension.Fields.FEEDER.javaName())
                    .add();
            //unused until QuantityFactory is created
//            table.column(UsagePointTechnicalInstallationDomainExtension.Fields.UTILIZATION_CATEGORY.databaseName())
//                    .varChar(255)
//                    .map(UsagePointTechnicalInstallationDomainExtension.Fields.UTILIZATION_CATEGORY.javaName())
//                    .add();
        }
    }
}
