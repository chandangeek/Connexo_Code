package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.cps.impl.metrology.TranslationKeys;
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


//@Component(name = "c.e.j.mtr.cps.impl.TestUsagePoint", service = CustomPropertySet.class, immediate = true)
public class TestUsagePoint implements CustomPropertySet<UsagePoint, TestDomainExtension> {

    public PropertySpecService propertySpecService;
    public Thesaurus thesaurus;

    public static final String TABLE_NAME = "RVK_CPS_USAGEPOINT_QUAN";
    public static final String FK_CPS_DEVICE_QUAN = "FK_CPS_USAGEPOINT_QUAN";
    public static final String COMPONENT_NAME = "QUAN";

    public TestUsagePoint() {
        super();
    }

    public TestUsagePoint(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    @Activate
    public void activate() {
        System.err.println("!!!");
    }

    @Override
    public String getName() {
        return this.getThesaurus().getFormat(TranslationKeys.INSTANT_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, TestDomainExtension> getPersistenceSupport() {
        return new QuantityPersistenceSupport(this.getThesaurus());
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
        PropertySpec quantity = propertySpecService
                .specForValuesOf(new QuantityValueFactory())
                .named(TestDomainExtension.Fields.QUANTITY.javaName(), TranslationKeys.QUANTITY_NAME)
                .fromThesaurus(this.getThesaurus())
                //  .setDefaultValue(Quantity.create(new BigDecimal(12), 1, "m"))
                .finish();
        PropertySpec instant = propertySpecService
                .specForValuesOf(new InstantFactory())
                .named(TestDomainExtension.Fields.INSTANT.javaName(), TranslationKeys.INSTANT_NAME)
                .describedAs(TranslationKeys.INSTANT_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                // .setDefaultValue(Instant.now())
                .finish();

        return Arrays.asList(
                instant,
                quantity);
    }

    private static class QuantityPersistenceSupport implements PersistenceSupport<UsagePoint, TestDomainExtension> {

        private Thesaurus thesaurus;

        public QuantityPersistenceSupport(Thesaurus thesaurus) {
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
            return TestDomainExtension.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_QUAN;
        }

        @Override
        public Class<TestDomainExtension> persistenceClass() {
            return TestDomainExtension.class;
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
            table.addQuantityColumns(TestDomainExtension.Fields.QUANTITY.databaseName(), false, TestDomainExtension.Fields.QUANTITY
                    .javaName());
            table.column(TestDomainExtension.Fields.INSTANT.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(TestDomainExtension.Fields.INSTANT.javaName())
                    .add();
        }
    }

}


