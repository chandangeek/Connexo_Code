package com.elster.jupiter.metering.cps.impl.metrology;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.cps.impl.QuantityValueFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
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

//@Component(name = "c.e.j.m.cps.impl.mtr.UsagePointConvCPS", service = CustomPropertySet.class, immediate = true)
public class UsagePointConvCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointConvDomainExtension> {

    public PropertySpecService propertySpecService;
    public Thesaurus thesaurus;

    public static final String TABLE_NAME = "RVK_CPS_MTR_USAGEPOINT_CONV";
    public static final String FK_CPS_DEVICE_CONVERTOR = "FK_CPS_MTR_USAGEPOINT_CONV";
    public static final String COMPONENT_NAME = "CONVER";

    public UsagePointConvCustomPropertySet() {
        super();
    }

    public UsagePointConvCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
    }

    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    @Activate
    public void activate() {
        System.out.println(TABLE_NAME);
    }

    @Override
    public String getName() {
        return this.getThesaurus().getFormat(TranslationKeys.CPS_CONVERTOR_SIMPLE_NAME).format();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointConvDomainExtension> getPersistenceSupport() {
        return new UsagePointConvPersSupp(this.getThesaurus());
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
        PropertySpec serialNumberSpec = propertySpecService
                .stringSpec()
                .named(UsagePointConvDomainExtension.Fields.SERIAL_NUMBER.javaName(), TranslationKeys.CPS_CONVERTOR_SERIAL_NUMBER)
                .describedAs(TranslationKeys.CPS_CONVERTOR_SERIAL_NUMBER_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .finish();
        PropertySpec noOfCorrectedDialsSpec = propertySpecService
                .specForValuesOf(new QuantityValueFactory())
                .named(UsagePointConvDomainExtension.Fields.N_C_DI.javaName(), TranslationKeys.CPS_CONVERTOR_NO_OF_CORRECTED_DIALS)
                .describedAs(TranslationKeys.CPS_CONVERTOR_NO_OF_CORRECTED_DIALS_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .finish();
        PropertySpec noOfUncorrectedDialsSpec = propertySpecService
                .specForValuesOf(new QuantityValueFactory())
                .named(UsagePointConvDomainExtension.Fields.N_UNC_DI.javaName(), TranslationKeys.CPS_CONVERTOR_NO_OF_UNCORRECTED_DIALS)
                .describedAs(TranslationKeys.CPS_CONVERTOR_NO_OF_UNCORRECTED_DIALS_DESCRIPTION)
                .fromThesaurus(this.getThesaurus())
                .finish();

        return Arrays.asList(serialNumberSpec,
                noOfCorrectedDialsSpec,
                noOfUncorrectedDialsSpec);
    }

    private static class UsagePointConvPersSupp implements PersistenceSupport<UsagePoint, UsagePointConvDomainExtension> {

        private Thesaurus thesaurus;

        public UsagePointConvPersSupp(Thesaurus thesaurus) {
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
            return UsagePointConvDomainExtension.Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_CONVERTOR;
        }

        @Override
        public Class<UsagePointConvDomainExtension> persistenceClass() {
            return UsagePointConvDomainExtension.class;
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
            table.column(UsagePointConvDomainExtension.Fields.SERIAL_NUMBER.databaseName())
                    .varChar(255)
                    .map(UsagePointConvDomainExtension.Fields.SERIAL_NUMBER.javaName())
                    .add();
            table.addQuantityColumns(UsagePointConvDomainExtension.Fields.N_C_DI.databaseName(), false,
                    UsagePointConvDomainExtension.Fields.N_C_DI.javaName());
            table.addQuantityColumns(UsagePointConvDomainExtension.Fields.N_UNC_DI.databaseName(), false,
                    UsagePointConvDomainExtension.Fields.N_UNC_DI.javaName());
        }
    }
}
