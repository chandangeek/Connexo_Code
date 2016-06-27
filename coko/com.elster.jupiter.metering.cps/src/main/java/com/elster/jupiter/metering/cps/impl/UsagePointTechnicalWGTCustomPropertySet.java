package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.cps.impl.metrology.TranslationKeys;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

//@Component(name = "c.e.j.mtr.cps.impl.UsagePointTechnicalWGTCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class UsagePointTechnicalWGTCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointTechnicalWGTDomExt> {

    public PropertySpecService propertySpecService;
    public Thesaurus thesaurus;

    public UsagePointTechnicalWGTCustomPropertySet() {
        super();
    }

    public UsagePointTechnicalWGTCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public Thesaurus getThesaurus() {
        return this.thesaurus;
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
    public PersistenceSupport<UsagePoint, UsagePointTechnicalWGTDomExt> getPersistenceSupport() {
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
                .named(UsagePointTechnicalWGTDomExt.Fields.PIPE_SIZE.javaName(), TranslationKeys.CPS_TECHNICAL_PROPERTIES_PIPE_SIZE)
                .describedAs(TranslationKeys.CPS_TECHNICAL_PROPERTIES_PIPE_SIZE_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .addValues("Flens - Dn40", "Flens - Dn50", "Flens - Dn80", "Flens - Dn100", "Flens - Dn150", "Flens - Dn200", "Schroefdraad - G 1/2\"", "Schroefdraad - G 3/4\"", "Schroefdraad - G 1\"", "Schroefdraad - G 1\" 1/4", "Schroefdraad - G 1\" 1/2", "Schroefdraad - G 1\" 3/4", "Schroefdraad - G 2\"")
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();
        PropertySpec pipeTypeSpec = propertySpecService
                .stringSpec()
                .named(UsagePointTechnicalWGTDomExt.Fields.PIPE_TYPE.javaName(), TranslationKeys.CPS_TECHNICAL_PROPERTIES_PIPE_TYPE)
                .describedAs(TranslationKeys.CPS_TECHNICAL_PROPERTIES_PIPE_TYPE_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .addValues("Asbestos", "Lead", "Iron", "Steel", "Bronze", "Copper", "Non metallic")
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();
        PropertySpec pressureLevelSpec = propertySpecService
                .stringSpec()
                .named(UsagePointTechnicalWGTDomExt.Fields.PRESSURE_LEVEL.javaName(), TranslationKeys.CPS_TECHNICAL_PROPERTIES_PRESSURE_LEVEL)
                .fromThesaurus(this.getThesaurus())
                .addValues("Low", "Medium", "High")
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();
        return Arrays.asList(pipeSizeSpec,
                pipeTypeSpec,
                pressureLevelSpec);
    }

    private class UsagePointTechnicalWGTPersistenceSupport implements PersistenceSupport<UsagePoint, UsagePointTechnicalWGTDomExt> {
        private Thesaurus thesaurus;

        @Override
        public String application() {
            return "Example";
        }

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
            return UsagePointTechnicalWGTDomExt.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return UsagePointTechnicalSeeds.FK_CPS_DEVICE_TECHNICAL.get();
        }

        @Override
        public Class<UsagePointTechnicalWGTDomExt> persistenceClass() {
            return UsagePointTechnicalWGTDomExt.class;
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
            table.column(UsagePointTechnicalWGTDomExt.Fields.PIPE_SIZE.databaseName())
                    .varChar(255)
                    .map(UsagePointTechnicalWGTDomExt.Fields.PIPE_SIZE.javaName())
                    .add();
            table.column(UsagePointTechnicalWGTDomExt.Fields.PIPE_TYPE.databaseName())
                    .varChar(255)
                    .map(UsagePointTechnicalWGTDomExt.Fields.PIPE_TYPE.javaName())
                    .add();
            table.column(UsagePointTechnicalWGTDomExt.Fields.PRESSURE_LEVEL.databaseName())
                    .varChar(255)
                    .map(UsagePointTechnicalWGTDomExt.Fields.PRESSURE_LEVEL.javaName())
                    .add();
        }
    }
}