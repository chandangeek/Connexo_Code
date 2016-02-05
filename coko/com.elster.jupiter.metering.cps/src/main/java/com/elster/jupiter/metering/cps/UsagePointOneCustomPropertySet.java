package com.elster.jupiter.metering.cps;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
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
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.elster.jupiter.metering.cps.UsagePointOneCustomPropertySet", service = CustomPropertySet.class, immediate = true)
@SuppressWarnings("unused")
public class UsagePointOneCustomPropertySet implements CustomPropertySet<UsagePoint, UsagePointOneDomainExtension> {

    public static final String TABLE_NAME = "RVK_CPS_USAGEPOINT_ONE";
    public static final String FK_CPS_DEVICE_ONE = "FK_CPS_USAGEPOINT_ONE";

    public volatile PropertySpecService propertySpecService;
    public volatile MeteringService meteringService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public UsagePointOneCustomPropertySet() {
        super();
    }

    @Inject
    public UsagePointOneCustomPropertySet(PropertySpecService propertySpecService, MeteringService meteringService) {
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
        return UsagePointOneCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<UsagePoint> getDomainClass() {
        return UsagePoint.class;
    }

    @Override
    public PersistenceSupport<UsagePoint, UsagePointOneDomainExtension> getPersistenceSupport() {
        return new UsagePointOnePeristenceSupport();
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
        return Arrays.asList(
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(UsagePointOneDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName(), UsagePointOneDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName())
                        .describedAs("kw")
                        .setDefaultValue(BigDecimal.ZERO)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UsagePointOneDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName(), UsagePointOneDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName())
                        .describedAs("infoString")
                        .setDefaultValue("description")
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(UsagePointOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName(), UsagePointOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                        .describedAs("A")
                        .addValues(BigDecimal.valueOf(7L), BigDecimal.valueOf(77L), BigDecimal.valueOf(777L))
                        .setDefaultValue(BigDecimal.valueOf(77L))
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(UsagePointOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName(), UsagePointOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName())
                        .describedAs("infoEnumString")
                        .addValues("alfa", "beta", "gamma")
                        .setDefaultValue("gamma")
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(UsagePointOneDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName(), UsagePointOneDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                        .describedAs("flag")
                        .setDefaultValue(false)
                        .finish());
    }

    private static class UsagePointOnePeristenceSupport implements PersistenceSupport<UsagePoint, UsagePointOneDomainExtension> {
        @Override
        public String componentName() {
            return "CPM1";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return UsagePointOneDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_ONE;
        }

        @Override
        public Class<UsagePointOneDomainExtension> persistenceClass() {
            return UsagePointOneDomainExtension.class;
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
            table
                    .column(UsagePointOneDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.databaseName())
                    .number()
                    .map(UsagePointOneDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName())
                    .notNull()
                    .add();
            table
                    .column(UsagePointOneDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.databaseName())
                    .varChar()
                    .map(UsagePointOneDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName())
                    .notNull()
                    .add();
            table
                    .column(UsagePointOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.databaseName())
                    .number()
                    .map(UsagePointOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                    .add();
            table
                    .column(UsagePointOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.databaseName())
                    .varChar()
                    .map(UsagePointOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName())
                    .add();
            table
                    .column(UsagePointOneDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.databaseName())
                    .bool()
                    .map(UsagePointOneDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                    .add();
        }
    }
}