/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.cps.AbstractVersionedPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.energyict.mdc.device.config.cps.DeviceSAPInfoCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class DeviceSAPInfoCustomPropertySet implements CustomPropertySet<Device, DeviceSAPInfoCustomPropertySet.DeviceSAPInfoDomainExtension> {

    public static final String PREFIX = "SDI";

    private enum Fields {
        DOMAIN("device"),
        USAGE_TYPE("usageType"),
        IN_USE("inUse"),;

        Fields(String javaName) {
            this.javaName = javaName;
        }

        private final String javaName;

        public String javaName() {
            return javaName;
        }
    }

    public static class DeviceSAPInfoDomainExtension extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<Device> {
        @NotNull(message = "{" + MessageSeeds.Keys.REQUIRED_FIELD + "}")
        private String usageType;
        @NotNull(message = "{" + MessageSeeds.Keys.REQUIRED_FIELD + "}")
        private Boolean inUse;

        private Reference<Device> device = ValueReference.absent();

        @Override
        public void copyFrom(Device domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
            this.device.set(domainInstance);
            this.usageType = (String) propertyValues.getProperty(Fields.USAGE_TYPE.javaName());
            this.inUse = (Boolean) propertyValues.getProperty(Fields.IN_USE.javaName());
        }

        @Override
        public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
            propertySetValues.setProperty(Fields.USAGE_TYPE.javaName(), this.usageType);
            propertySetValues.setProperty(Fields.IN_USE.javaName(), this.inUse);
        }

        @Override
        public void validateDelete() {
            // allow delete
        }
    }

    public static class DeviceSAPInfoPersistentSupport implements PersistenceSupport<Device, DeviceSAPInfoDomainExtension> {
        private final Thesaurus thesaurus;

        public DeviceSAPInfoPersistentSupport(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }

        @Override
        public String componentName() {
            return PREFIX;
        }

        @Override
        public String tableName() {
            return PREFIX + "_SAP_INFO";
        }

        @Override
        public String domainFieldName() {
            return Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return PREFIX + "_SAP_INFO_FK";
        }

        @Override
        public Class<DeviceSAPInfoDomainExtension> persistenceClass() {
            return DeviceSAPInfoDomainExtension.class;
        }

        @Override
        public Optional<Module> module() {
            return Optional.of(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Thesaurus.class).toInstance(thesaurus);
                    bind(MessageInterpolator.class).toInstance(thesaurus);
                }
            });
        }

        @Override
        public List<Column> addCustomPropertyPrimaryKeyColumnsTo(Table table) {
            return Collections.emptyList();
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table.column(Fields.USAGE_TYPE.name()).varChar(Table.NAME_LENGTH).map(Fields.USAGE_TYPE.javaName()).notNull().add();
            table.column(Fields.IN_USE.name()).bool().map(Fields.IN_USE.javaName()).notNull().add();
        }

        @Override
        public String application() {
            return "Demo";
        }

        @Override
        public String columnNameFor(PropertySpec propertySpec) {
            return EnumSet
                    .complementOf(EnumSet.of(Fields.DOMAIN))
                    .stream()
                    .filter(each -> each.javaName().equals(propertySpec.getName()))
                    .findFirst()
                    .map(Fields::name)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown property spec: " + propertySpec.getName()));
        }
    }

    private Thesaurus thesaurus;
    private PropertySpecService propertySpecService;
    private DeviceService deviceService; // table ordering

    @SuppressWarnings("unused") // OSGI
    public DeviceSAPInfoCustomPropertySet() {
    }

    @SuppressWarnings("unused") // TESTS
    @Inject
    public DeviceSAPInfoCustomPropertySet(NlsService nlsService, PropertySpecService propertySpecService, DeviceService deviceService) {
        setNlsService(nlsService);
        setPropertySpecService(propertySpecService);
        setDeviceService(deviceService);
    }

    @SuppressWarnings("unused") // OSGI
    @org.osgi.service.component.annotations.Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(CustomPropertySetsDemoInstaller.COMPONENT_NAME, Layer.DOMAIN);
    }

    @SuppressWarnings("unused") // OSGI
    @org.osgi.service.component.annotations.Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @SuppressWarnings("unused") // OSGI, table ordering
    @org.osgi.service.component.annotations.Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(TranslationKeys.SDI_NAME).format();
    }

    @Override
    public Class<Device> getDomainClass() {
        return Device.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.DOMAIN_NAME_DEVICE).format();
    }

    @Override
    public PersistenceSupport<Device, DeviceSAPInfoDomainExtension> getPersistenceSupport() {
        return new DeviceSAPInfoPersistentSupport(this.thesaurus);
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
        List<PropertySpec> properties = new ArrayList<>();
        properties.add(this.propertySpecService.stringSpec().named(Fields.USAGE_TYPE.javaName(), TranslationKeys.SDI_PROPERTY_USAGE_TYPE)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.booleanSpec().named(Fields.IN_USE.javaName(), TranslationKeys.SDI_PROPERTY_IN_USE)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .setDefaultValue(Boolean.TRUE)
                .finish());
        return properties;
    }
}