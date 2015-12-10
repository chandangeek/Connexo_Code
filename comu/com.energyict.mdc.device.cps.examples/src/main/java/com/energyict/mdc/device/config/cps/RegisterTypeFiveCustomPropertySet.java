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
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.DeviceService;
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

@Component(name = "com.energyict.mdc.device.config.cps.RegisterTypeFiveCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class RegisterTypeFiveCustomPropertySet implements CustomPropertySet<RegisterSpec, RegisterTypeFiveDomainExtension> {

    public static final String TABLE_NAME = "RVK_CPS_REGISTER_FIVE";
    public static final String FK_CPS_REGISTER_FIVE = "FK_CPS_REGISTER_FIVE";

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

    public RegisterTypeFiveCustomPropertySet() {
        super();
    }

    @Inject
    public RegisterTypeFiveCustomPropertySet(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Activate
    public void activate() {
        System.err.println(TABLE_NAME);
    }

    @Override
    public String getName() {
        return RegisterTypeFiveCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<RegisterSpec> getDomainClass() {
        return RegisterSpec.class;
    }

    @Override
    public PersistenceSupport<RegisterSpec, RegisterTypeFiveDomainExtension> getPersistenceSupport() {
        return new RegisterTypeFivePeristenceSupport();
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
                .newPropertySpecBuilder(new BigDecimalFactory())
                .name(RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName(), RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName())
                .description("kw")
                .setDefaultValue(0)
                .markRequired()
                .finish();
        PropertySpec testStringPropertySpec = this.propertySpecService
                .newPropertySpecBuilder(new StringFactory())
                .name(RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName(), RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName())
                .description("infoString")
                .setDefaultValue("description")
                .finish();
        PropertySpec testBooleanPropertySpec = this.propertySpecService
                .newPropertySpecBuilder(new BooleanFactory())
                .name(RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName(), RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                .description("flag")
                .setDefaultValue(false)
                .finish();
        return Arrays.asList(testNumberPropertySpec, testStringPropertySpec, testBooleanPropertySpec);
    }

    private static class RegisterTypeFivePeristenceSupport implements PersistenceSupport<RegisterSpec, RegisterTypeFiveDomainExtension> {
        @Override
        public String componentName() {
            return "RVK";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return RegisterTypeFiveDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_REGISTER_FIVE;
        }

        @Override
        public Class<RegisterTypeFiveDomainExtension> persistenceClass() {
            return RegisterTypeFiveDomainExtension.class;
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
                    .column(RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.databaseName())
                    .number()
                    .map(RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName())
                    .notNull()
                    .add();
            table
                    .column(RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.databaseName())
                    .varChar()
                    .map(RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName())
                    .notNull()
                    .add();
            table
                    .column(RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.databaseName())
                    .bool()
                    .map(RegisterTypeFiveDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                    .add();
        }
    }
}