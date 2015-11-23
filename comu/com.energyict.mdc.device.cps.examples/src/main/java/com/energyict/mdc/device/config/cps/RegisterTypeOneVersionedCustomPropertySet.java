package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.*;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.DeviceService;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import javax.inject.Inject;
import java.util.*;

@Component(name = "com.energyict.mdc.device.config.cps.RegisterTypeOneVersionedCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class RegisterTypeOneVersionedCustomPropertySet implements CustomPropertySet<RegisterSpec, RegisterTypeOneVersionedDomainExtension> {

    public static final String TABLE_NAME = "RVK_CPS_REGISTER_VER";
    public static final String FK_CPS_DEVICE_VER = "FK_CPS_REGISTER_VER";

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

    public RegisterTypeOneVersionedCustomPropertySet() {
        super();
    }

    @Inject
    public RegisterTypeOneVersionedCustomPropertySet(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Activate
    public void activate() {
        System.err.println(TABLE_NAME);
    }

    @Override
    public String getName() {
        return RegisterTypeOneVersionedCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<RegisterSpec> getDomainClass() {
        return RegisterSpec.class;
    }

    @Override
    public PersistenceSupport<RegisterSpec, RegisterTypeOneVersionedDomainExtension> getPersistenceSupport() {
        return new RegisterTypeOneVersionedPeristenceSupport();
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
        PropertySpec testNumberPropertySpec = this.propertySpecService
                .newPropertySpecBuilder(new BigDecimalFactory())
                .name(RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName(), RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName())
                .description("kw")
                .setDefaultValue(0)
                .markRequired()
                .finish();
        PropertySpec testStringPropertySpec = this.propertySpecService
                .newPropertySpecBuilder(new StringFactory())
                .name(RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName(), RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName())
                .description("infoString")
                .setDefaultValue("description")
                .finish();
        PropertySpec testNumberEnumPropertySpec = this.propertySpecService
                .newPropertySpecBuilder(new BigDecimalFactory())
                .name(RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName(), RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                .description("A")
                .addValues(7, 77, 777)
                .setDefaultValue(77)
                .finish();
        PropertySpec testStringEnumPropertySpec = this.propertySpecService
                .newPropertySpecBuilder(new BigDecimalFactory())
                .name(RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName(), RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName())
                .description("infoEnumString")
                .addValues("alfa", "beta", "gamma")
                .setDefaultValue("gamma")
                .finish();
        PropertySpec testBooleanPropertySpec = this.propertySpecService
                .newPropertySpecBuilder(new BooleanFactory())
                .name(RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName(), RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                .description("flag")
                .setDefaultValue(false)
                .finish();
        return Arrays.asList(testNumberPropertySpec, testStringPropertySpec, testNumberEnumPropertySpec, testStringEnumPropertySpec, testBooleanPropertySpec);
    }

    private static class RegisterTypeOneVersionedPeristenceSupport implements PersistenceSupport<RegisterSpec, RegisterTypeOneVersionedDomainExtension> {
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
            return RegisterTypeOneVersionedDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_VER;
        }

        @Override
        public Class<RegisterTypeOneVersionedDomainExtension> persistenceClass() {
            return RegisterTypeOneVersionedDomainExtension.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.empty();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table) {
            table
                    .column(RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.databaseName())
                    .number()
                    .map(RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName())
                    .notNull()
                    .add();
            table
                    .column(RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.databaseName())
                    .varChar()
                    .map(RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_STRING.javaName())
                    .notNull()
                    .add();
            table
                    .column(RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.databaseName())
                    .number()
                    .map(RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                    .add();
            table
                    .column(RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.databaseName())
                    .varChar()
                    .map(RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName())
                    .add();
            table
                    .column(RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.databaseName())
                    .bool()
                    .map(RegisterTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                    .add();
        }
    }
}