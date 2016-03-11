package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.cps.impl.QuantityValueFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.InstantFactory;
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

//@Component(name = "c.e.j.m.cps.impl.mtr.UsagePointDecentProdCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class UsagePointDecentProdCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointDecentProdDomExt> {

    public PropertySpecService propertySpecService;
    public Thesaurus thesaurus;

    public static final String TABLE_NAME = "RVK_CPS_MTR_USAGEPOINT_DEC";
    public static final String FK_CPS_DEVICE_DECENTRALIZED_PRODUCTION = "FK_CPS_MTR_USAGEPOINT_DEC";
    public static final String COMPONENT_NAME = "DEC_PROD";

    public UsagePointDecentProdCustomPropertySet() {
        super();
    }

    public UsagePointDecentProdCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    @Activate
    public void activate() {
    }

    @Override
    public String getName() {
        return this.getThesaurus().getFormat(TranslationKeys.CPS_DECENTRALIZED_PRODUCTION_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointDecentProdDomExt> getPersistenceSupport() {
        return new UsagePointDecentrProdPS(this.getThesaurus());
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
        PropertySpec installedPowerSpec = propertySpecService
                .specForValuesOf(new QuantityValueFactory())
                .named(UsagePointDecentProdDomExt.Fields.INS_POWER.javaName(), TranslationKeys.CPS_DECENTRALIZED_PRODUCTION_INSTALLED_POWER)
                .fromThesaurus(this.getThesaurus())
                .markEditable()
                .markRequired()
                .finish();
        PropertySpec convertorPowerSpec = propertySpecService
                .specForValuesOf(new QuantityValueFactory())
                .named(UsagePointDecentProdDomExt.Fields.CONVERTOR_POWER.javaName(), TranslationKeys.CPS_DECENTRALIZED_PRODUCTION_CONVERTER_POWER)
                .fromThesaurus(this.getThesaurus())
                .markEditable()
                .markRequired()
                .finish();
        PropertySpec typeOfDecentralizedProductionSpec = propertySpecService
                .stringSpec()
                .named(UsagePointDecentProdDomExt.Fields.DEC_PROD.javaName(), TranslationKeys.CPS_DECENTRALIZED_PRODUCTION_TYPE_OF_DECENTRALIZED_PRODUCTION)
                .fromThesaurus(this.getThesaurus())
                .addValues("solar", "wind", "other")
                .markRequired()
                .finish();
        PropertySpec commissioningDateSpec = propertySpecService
                .specForValuesOf(new InstantFactory())
                .named(UsagePointDecentProdDomExt.Fields.COMMISSIONING_DATE.javaName(), TranslationKeys.CPS_DECENTRALIZED_PRODUCTION_COMMISSIONING_DATE)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .finish();

        return Arrays.asList(
                installedPowerSpec,
                convertorPowerSpec,
                typeOfDecentralizedProductionSpec,
                commissioningDateSpec);
    }

    private static class UsagePointDecentrProdPS implements PersistenceSupport<UsagePoint, UsagePointDecentProdDomExt> {
        private Thesaurus thesaurus;

        public UsagePointDecentrProdPS(Thesaurus thesaurus) {
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
            return UsagePointDecentProdDomExt.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_DECENTRALIZED_PRODUCTION;
        }

        @Override
        public Class<UsagePointDecentProdDomExt> persistenceClass() {
            return UsagePointDecentProdDomExt.class;
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
            table.addQuantityColumns(UsagePointDecentProdDomExt.Fields.INS_POWER.databaseName(), true,
                    UsagePointDecentProdDomExt.Fields.INS_POWER.javaName());
            table.addQuantityColumns(UsagePointDecentProdDomExt.Fields.CONVERTOR_POWER.databaseName(), true,
                    UsagePointDecentProdDomExt.Fields.CONVERTOR_POWER.javaName());
            table.column(UsagePointDecentProdDomExt.Fields.DEC_PROD.databaseName())
                    .varChar(255)
                    .map(UsagePointDecentProdDomExt.Fields.DEC_PROD.javaName())
                    .notNull()
                    .add();
            table.column(UsagePointDecentProdDomExt.Fields.COMMISSIONING_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(UsagePointDecentProdDomExt.Fields.COMMISSIONING_DATE.javaName())
                    .notNull()
                    .add();
        }
    }
}
