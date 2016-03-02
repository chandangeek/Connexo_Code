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
import org.osgi.service.component.annotations.ReferenceCardinality;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "c.e.j.metering.cps.impl.UsagePointTechElectricityCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class UsagePointTechElectricityCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointTechElectricityDomainExtension> {

    public static final String TABLE_NAME = "RVK_CPS_TECH_EL";
    public static final String FK_CPS_DEVICE_ONE = "FK_CPS_TECH_EL";

    public volatile PropertySpecService propertySpecService;
    public volatile MeteringService meteringService;
    public volatile NlsService nlsService;

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public UsagePointTechElectricityCustomPropertySet() {
        super();
    }

    @Inject
    public UsagePointTechElectricityCustomPropertySet(PropertySpecService propertySpecService, MeteringService meteringService) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setMeteringService(meteringService);
    }

    @Activate
    public void activate() {
        System.out.println(TABLE_NAME);
    }

    @Override
    public String getName() {
        return this.getThesaurus().
                getFormat(TranslationKeys.CPS_TECHNICAL_ELECTRICITY_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointTechElectricityDomainExtension> getPersistenceSupport() {
        return new UsagePointTechnicalElectricityPersistenceSupport(this.getThesaurus());
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
                .named(UsagePointTechElectricityDomainExtension.FieldNames.CROSS_SECTIONAL_AREA.javaName(), TranslationKeys.CPS_TECHNICAL_PROPERTIES_CROSS_SECTIONAL_AREA)
                .describedAs(TranslationKeys.CPS_TECHNICAL_PROPERTIES_CROSS_SECTIONAL_AREA_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .addValues("EU", "NA")
                .finish();

        PropertySpec volatageLevelSpec = propertySpecService
                .stringSpec()
                .named(UsagePointTechElectricityDomainExtension.FieldNames.VOLTAGE_LEVEL.javaName(), TranslationKeys.CPS_TECHNICAL_PROPERTIES_VOLTAGE_LEVEL)
                .fromThesaurus(this.getThesaurus())
                .addValues("low", "medium", "high")
                .finish();

        return Arrays.asList(
                crossSectionalAreaSpec,
                volatageLevelSpec);
    }

    private Thesaurus getThesaurus() {
        return nlsService.getThesaurus(TranslationInstaller.COMPONENT_NAME, Layer.DOMAIN);
    }

    private static class UsagePointTechnicalElectricityPersistenceSupport implements PersistenceSupport<UsagePoint, UsagePointTechElectricityDomainExtension> {
        private Thesaurus thesaurus;

        public UsagePointTechnicalElectricityPersistenceSupport(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }


        @Override
        public String componentName() {
            return "TE1";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return UsagePointTechElectricityDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_ONE;
        }

        @Override
        public Class<UsagePointTechElectricityDomainExtension> persistenceClass() {
            return UsagePointTechElectricityDomainExtension.class;
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
            table.column(UsagePointTechElectricityDomainExtension.FieldNames.CROSS_SECTIONAL_AREA.databaseName())
                    .varChar(255)
                    .map(UsagePointTechElectricityDomainExtension.FieldNames.CROSS_SECTIONAL_AREA.javaName())
                    .add();
            table.column(UsagePointTechElectricityDomainExtension.FieldNames.VOLTAGE_LEVEL.databaseName())
                    .varChar(255)
                    .map(UsagePointTechElectricityDomainExtension.FieldNames.VOLTAGE_LEVEL.javaName())
                    .add();
        }
    }
}