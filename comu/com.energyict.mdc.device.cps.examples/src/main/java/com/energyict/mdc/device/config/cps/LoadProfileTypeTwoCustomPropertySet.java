package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.data.DeviceService;
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

@Component(name = "com.energyict.mdc.device.config.cps.LoadProfileTypeTwoCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class LoadProfileTypeTwoCustomPropertySet implements CustomPropertySet<ChannelSpec, LoadProfileTypeTwoDomainExtension> {

    public static final String TABLE_NAME = "RVK_CPS_CHANNEL_TWO";
    public static final String FK_CPS_CHANNEL_TWO = "FK_CPS_CHANNEL_TWO";

    public volatile PropertySpecService propertySpecService;
    public volatile DeviceService deviceService;

    @SuppressWarnings("unused")
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @SuppressWarnings("unused")
    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public LoadProfileTypeTwoCustomPropertySet() {
        super();
    }

    @Inject
    public LoadProfileTypeTwoCustomPropertySet(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Activate
    public void activate() {
        System.err.println(TABLE_NAME);
    }

    @Override
    public String getName() {
        return LoadProfileTypeTwoCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<ChannelSpec> getDomainClass() {
        return ChannelSpec.class;
    }

    @Override
    public PersistenceSupport<ChannelSpec, LoadProfileTypeTwoDomainExtension> getPersistenceSupport() {
        return new LoadProfileTypeTwoPeristenceSupport();
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
        PropertySpec testNumberPropertySpec = this.propertySpecService
                .bigDecimalSpec()
                .named(LoadProfileTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName(), LoadProfileTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName())
                .describedAs("kw")
                .setDefaultValue(BigDecimal.ZERO)
                .markRequired()
                .finish();
        PropertySpec testStringPropertySpec = this.propertySpecService
                .stringSpec()
                .named(LoadProfileTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName(), LoadProfileTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName())
                .describedAs("infoString")
                .setDefaultValue("description")
                .finish();
        PropertySpec testNumberEnumPropertySpec = this.propertySpecService
                .bigDecimalSpec()
                .named(LoadProfileTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName(), LoadProfileTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                .describedAs("A")
                .addValues(BigDecimal.valueOf(7), BigDecimal.valueOf(77), BigDecimal.valueOf(777))
                .setDefaultValue(BigDecimal.valueOf(77))
                .finish();
        PropertySpec testStringEnumPropertySpec = this.propertySpecService
                .stringSpec()
                .named(LoadProfileTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName(), LoadProfileTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName())
                .describedAs("infoEnumString")
                .addValues("alfa", "beta", "gamma")
                .setDefaultValue("gamma")
                .finish();
        PropertySpec testBooleanPropertySpec = this.propertySpecService
                .booleanSpec()
                .named(LoadProfileTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName(), LoadProfileTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                .describedAs("flag")
                .setDefaultValue(false)
                .finish();
        return Arrays.asList(testNumberPropertySpec, testStringPropertySpec, testNumberEnumPropertySpec, testStringEnumPropertySpec, testBooleanPropertySpec);
    }

    private static class LoadProfileTypeTwoPeristenceSupport implements PersistenceSupport<ChannelSpec, LoadProfileTypeTwoDomainExtension> {
        @Override
        public String componentName() {
            return "RVK9";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return LoadProfileTypeTwoDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_CHANNEL_TWO;
        }

        @Override
        public Class<LoadProfileTypeTwoDomainExtension> persistenceClass() {
            return LoadProfileTypeTwoDomainExtension.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.empty();
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.singletonList(
                    table
                            .column(LoadProfileTypeOneDomainExtension.FieldNames.DEVICE.databaseName())
                            .number()
                            .map(LoadProfileTypeOneDomainExtension.FieldNames.DEVICE.javaName())
                            .notNull()
                            .add());
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table
                    .column(LoadProfileTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.databaseName())
                    .number()
                    .map(LoadProfileTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName())
                    .notNull()
                    .add();
            table
                    .column(LoadProfileTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.databaseName())
                    .varChar()
                    .map(LoadProfileTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName())
                    .notNull()
                    .add();
            table
                    .column(LoadProfileTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.databaseName())
                    .number()
                    .map(LoadProfileTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                    .add();
            table
                    .column(LoadProfileTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.databaseName())
                    .varChar()
                    .map(LoadProfileTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName())
                    .add();
            table
                    .column(LoadProfileTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.databaseName())
                    .bool()
                    .map(LoadProfileTypeTwoDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                    .add();
        }
    }
}