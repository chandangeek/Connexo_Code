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
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.data.DeviceService;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import javax.inject.Inject;
import java.util.*;

@Component(name = "com.energyict.mdc.device.config.cps.LoadProfileOneVersionedCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class LoadProfileOneVersionedCustomPropertySet implements CustomPropertySet<ChannelSpec, LoadProfileOneVersionedDomainExtension> {

    public static final String TABLE_NAME = "RVK_CPS_CHANNEL_VER";
    public static final String FK_CPS_DEVICE_VER = "FK_CPS_CHANNEL_VER";

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

    public LoadProfileOneVersionedCustomPropertySet() {
        super();
    }

    @Inject
    public LoadProfileOneVersionedCustomPropertySet(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Activate
    public void activate() {
        System.err.println(TABLE_NAME);
    }

    @Override
    public String getName() {
        return LoadProfileOneVersionedCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<ChannelSpec> getDomainClass() {
        return ChannelSpec.class;
    }

    @Override
    public PersistenceSupport<ChannelSpec, LoadProfileOneVersionedDomainExtension> getPersistenceSupport() {
        return new LoadProfileOneVersionedPeristenceSupport();
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
        PropertySpec testNumberEnumPropertySpec = this.propertySpecService
                .newPropertySpecBuilder(new BigDecimalFactory())
                .name(LoadProfileOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName(), LoadProfileOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                .description("A")
                .addValues(7, 77, 777)
                .setDefaultValue(77)
                .finish();
        PropertySpec testStringEnumPropertySpec = this.propertySpecService
                .newPropertySpecBuilder(new BigDecimalFactory())
                .name(LoadProfileOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName(), LoadProfileOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName())
                .description("infoEnumString")
                .addValues("alfa", "beta", "gamma")
                .setDefaultValue("gamma")
                .finish();
        PropertySpec testBooleanPropertySpec = this.propertySpecService
                .newPropertySpecBuilder(new BooleanFactory())
                .name(LoadProfileOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName(), LoadProfileOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                .description("flag")
                .setDefaultValue(false)
                .finish();
        return Arrays.asList(testNumberEnumPropertySpec, testStringEnumPropertySpec, testBooleanPropertySpec);
    }

    private static class LoadProfileOneVersionedPeristenceSupport implements PersistenceSupport<ChannelSpec, LoadProfileOneVersionedDomainExtension> {
        @Override
        public String componentName() {
            return "RVK6";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return LoadProfileOneVersionedDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_VER;
        }

        @Override
        public Class<LoadProfileOneVersionedDomainExtension> persistenceClass() {
            return LoadProfileOneVersionedDomainExtension.class;
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
                    .column(LoadProfileOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.databaseName())
                    .number()
                    .map(LoadProfileOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                    .add();
            table
                    .column(LoadProfileOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.databaseName())
                    .varChar()
                    .map(LoadProfileOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName())
                    .add();
            table
                    .column(LoadProfileOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.databaseName())
                    .bool()
                    .map(LoadProfileOneVersionedDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                    .add();
        }
    }
}