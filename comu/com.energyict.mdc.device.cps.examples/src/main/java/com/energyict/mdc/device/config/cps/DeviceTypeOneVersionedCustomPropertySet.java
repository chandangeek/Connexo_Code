package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import javax.inject.Inject;
import java.util.*;

@Component(name = "com.energyict.mdc.device.config.cps.DeviceTypeIntervalOneCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class DeviceTypeOneVersionedCustomPropertySet implements CustomPropertySet<Device, DeviceTypeOneVersionedDomainExtension> {

    public static final String TABLE_NAME = "RVK_CPS_DEVICE_VER";
    public static final String FK_CPS_DEVICE_VER = "FK_CPS_DEVICE_VER";

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

    public DeviceTypeOneVersionedCustomPropertySet() {
        super();
    }

    @Inject
    public DeviceTypeOneVersionedCustomPropertySet(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Activate
    public void activate() {
        System.err.println(TABLE_NAME);
    }

    @Override
    public String getName() {
        return DeviceTypeOneVersionedCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<Device> getDomainClass() {
        return Device.class;
    }

    @Override
    public PersistenceSupport<Device, DeviceTypeOneVersionedDomainExtension> getPersistenceSupport() {
        return new DeviceTypeOneVersionedPeristenceSupport();
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
                .name(DeviceTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName(), DeviceTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName())
                .description("aaaaaa")
                .setDefaultValue(0)
                .markRequired()
                .finish();
        PropertySpec testNumberEnumPropertySpec = this.propertySpecService
                .newPropertySpecBuilder(new BigDecimalFactory())
                .name(DeviceTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName(), DeviceTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                .description("bbbbbbbb")
                .addValues(8, 88, 888)
                .setDefaultValue(88)
                .finish();
        PropertySpec testBooleanPropertySpec = this.propertySpecService
                .newPropertySpecBuilder(new BooleanFactory())
                .name(DeviceTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName(), DeviceTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                .description("cccccccccc")
                .setDefaultValue(false)
                .finish();
        return Arrays.asList(testBooleanPropertySpec, testNumberPropertySpec, testNumberEnumPropertySpec);
    }

    private static class DeviceTypeOneVersionedPeristenceSupport implements PersistenceSupport<Device, DeviceTypeOneVersionedDomainExtension> {
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
            return DeviceTypeOneVersionedDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_VER;
        }

        @Override
        public Class<DeviceTypeOneVersionedDomainExtension> persistenceClass() {
            return DeviceTypeOneVersionedDomainExtension.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.empty();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table) {
            table
                    .column(DeviceTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.databaseName())
                    .number()
                    .map(DeviceTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_NUMBER.javaName())
                    .notNull()
                    .add();
            table
                    .column(DeviceTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.databaseName())
                    .number()
                    .map(DeviceTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                    .add();
            table
                    .column(DeviceTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.databaseName())
                    .bool()
                    .map(DeviceTypeOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                    .add();
        }
    }
}