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
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.data.DeviceService;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.energyict.mdc.device.config.cps.LoadProfileTypeOneCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class LoadProfileTypeOneCustomPropertySet implements CustomPropertySet<ChannelSpec, LoadProfileTypeOneDomainExtension> {

    public static final String TABLE_NAME = "RVK_CPS_CHANNEL_ONE";
    public static final String FK_CPS_CHANNEL_ONE = "FK_CPS_CHANNEL_ONE";

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

    public LoadProfileTypeOneCustomPropertySet() {
        super();
    }

    @Inject
    public LoadProfileTypeOneCustomPropertySet(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Activate
    public void activate() {
        System.err.println(TABLE_NAME);
    }

    @Override
    public String getName() {
        return LoadProfileTypeOneCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<ChannelSpec> getDomainClass() {
        return ChannelSpec.class;
    }

    @Override
    public PersistenceSupport<ChannelSpec, LoadProfileTypeOneDomainExtension> getPersistenceSupport() {
        return new LoadProfileTypeOnePeristenceSupport();
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
        PropertySpec testNumberEnumPropertySpec = this.propertySpecService
                .newPropertySpecBuilder(new BigDecimalFactory())
                .name(LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName(), LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                .description("A")
                .addValues(7, 77, 777)
                .setDefaultValue(77)
                .finish();
        PropertySpec testStringEnumPropertySpec = this.propertySpecService
                .newPropertySpecBuilder(new BigDecimalFactory())
                .name(LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName(), LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName())
                .description("infoEnumString")
                .addValues("alfa", "beta", "gamma")
                .setDefaultValue("gamma")
                .finish();
        PropertySpec testBooleanPropertySpec = this.propertySpecService
                .newPropertySpecBuilder(new BooleanFactory())
                .name(LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName(), LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                .description("flag")
                .setDefaultValue(false)
                .finish();
        return Arrays.asList(testNumberEnumPropertySpec, testStringEnumPropertySpec, testBooleanPropertySpec);
    }

    private static class LoadProfileTypeOnePeristenceSupport implements PersistenceSupport<ChannelSpec, LoadProfileTypeOneDomainExtension> {
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
            return LoadProfileTypeOneDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_CHANNEL_ONE;
        }

        @Override
        public Class<LoadProfileTypeOneDomainExtension> persistenceClass() {
            return LoadProfileTypeOneDomainExtension.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.empty();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table) {
            table
                    .column(LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.databaseName())
                    .number()
                    .map(LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_NUMBER.javaName())
                    .add();
            table
                    .column(LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.databaseName())
                    .varChar()
                    .map(LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_ENUM_STRING.javaName())
                    .add();
            table
                    .column(LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.databaseName())
                    .bool()
                    .map(LoadProfileTypeOneDomainExtension.FieldNames.TEST_ATTRIBUTE_BOOLEAN.javaName())
                    .add();
        }
    }
}