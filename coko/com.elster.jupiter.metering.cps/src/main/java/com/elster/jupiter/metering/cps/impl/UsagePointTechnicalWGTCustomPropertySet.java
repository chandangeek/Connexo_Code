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

@Component(name = "c.e.j.mtr.cps.impl.UsagePointTechnicalWGTCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class UsagePointTechnicalWGTCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointTechnicalWGTDomainExtension> {

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

    public UsagePointTechnicalWGTCustomPropertySet() {
        super();
    }

    @Inject
    public UsagePointTechnicalWGTCustomPropertySet(PropertySpecService propertySpecService, MeteringService meteringService) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setMeteringService(meteringService);
    }

    @Activate
    public void activate() {
        System.out.println(UsagePointTechnicalSeeds.TABLE_NAME.get());
    }

    @Override
    public String getName() {
        return this.getThesaurus().getFormat(TranslationKeys.CPS_TECHNICAL_GWT_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointTechnicalWGTDomainExtension> getPersistenceSupport() {
        return new UsagePointTechnicalWGTPersistenceSupport(this.getThesaurus());
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
        PropertySpec pipeSizeSpec = propertySpecService
                .stringSpec()
                .named(UsagePointTechnicalWGTDomainExtension.Fields.PIPE_SIZE.javaName(), TranslationKeys.CPS_TECHNICAL_PROPERTIES_PIPE_SIZE)
                .describedAs(TranslationKeys.CPS_TECHNICAL_PROPERTIES_PIPE_SIZE_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .addValues("EU", "UK", "NA")
                .finish();
        PropertySpec pipeTypeSpec = propertySpecService
                .stringSpec()
                .named(UsagePointTechnicalWGTDomainExtension.Fields.PIPE_TYPE.javaName(), TranslationKeys.CPS_TECHNICAL_PROPERTIES_PIPE_TYPE)
                .describedAs(TranslationKeys.CPS_TECHNICAL_PROPERTIES_PIPE_TYPE_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .addValues("Asbestos", "Lead", "Iron", "Steel", "Bronze", "Copper", "Non metallic")
                .finish();
        PropertySpec pressureLevelSpec = propertySpecService
                .stringSpec()
                .named(UsagePointTechnicalWGTDomainExtension.Fields.PRESSURE_LEVEL.javaName(), TranslationKeys.CPS_TECHNICAL_PROPERTIES_PRESSURE_LEVEL)
                .fromThesaurus(this.getThesaurus())
                .addValues("low", "medium", "high")
                .finish();
        return Arrays.asList(pipeSizeSpec,
                pipeTypeSpec,
                pressureLevelSpec);
    }

    private Thesaurus getThesaurus() {
        return nlsService.getThesaurus(TranslationInstaller.COMPONENT_NAME, Layer.DOMAIN);
    }

    private class UsagePointTechnicalWGTPersistenceSupport implements PersistenceSupport<UsagePoint, UsagePointTechnicalWGTDomainExtension> {
        private Thesaurus thesaurus;

        public UsagePointTechnicalWGTPersistenceSupport(Thesaurus thesaurus) {
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
            return UsagePointTechnicalWGTDomainExtension.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return UsagePointTechnicalSeeds.FK_CPS_DEVICE_TECHNICAL.get();
        }

        @Override
        public Class<UsagePointTechnicalWGTDomainExtension> persistenceClass() {
            return UsagePointTechnicalWGTDomainExtension.class;
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
            table.column(UsagePointTechnicalWGTDomainExtension.Fields.PIPE_SIZE.databaseName())
                    .varChar(255)
                    .map(UsagePointTechnicalWGTDomainExtension.Fields.PIPE_SIZE.javaName())
                    .add();
            table.column(UsagePointTechnicalWGTDomainExtension.Fields.PIPE_TYPE.databaseName())
                    .varChar(255)
                    .map(UsagePointTechnicalWGTDomainExtension.Fields.PIPE_TYPE.javaName())
                    .add();
            table.column(UsagePointTechnicalWGTDomainExtension.Fields.PRESSURE_LEVEL.databaseName())
                    .varChar(255)
                    .map(UsagePointTechnicalWGTDomainExtension.Fields.PRESSURE_LEVEL.javaName())
                    .add();
        }
    }
}