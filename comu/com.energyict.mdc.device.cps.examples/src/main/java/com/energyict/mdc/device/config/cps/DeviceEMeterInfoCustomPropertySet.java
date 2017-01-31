/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
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
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.QuantityValueFactory;
import com.elster.jupiter.util.units.Quantity;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.energyict.mdc.device.config.cps.DeviceEMeterInfoCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class DeviceEMeterInfoCustomPropertySet implements CustomPropertySet<Device, DeviceEMeterInfoCustomPropertySet.DeviceEMeterInfoDomainExtension> {

    public static final String PREFIX = "EMI";

    private enum Fields {
        DOMAIN("device"),
        MANUFACTURER("manufacturer"),
        MODEL_NUMBER("modelNumber"),
        CONFIG_SCHEME("configScheme"),
        SERVICE_COMPANY("serviceCompany"),
        TECHNICIAN("technician"),
        REPLACE_BY("replaceBy"),
        MAX_CURRENT_RATING("maxCurrentRating"),
        MAX_VOLTAGE("maxVoltage"),;

        Fields(String javaName) {
            this.javaName = javaName;
        }

        private final String javaName;

        public String javaName() {
            return javaName;
        }
    }

    public static class DeviceEMeterInfoDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<Device> {
        @NotNull(message = "{" + MessageSeeds.Keys.REQUIRED_FIELD + "}")
        private String manufacturer;
        @NotNull(message = "{" + MessageSeeds.Keys.REQUIRED_FIELD + "}")
        private String modelNumber;
        @NotNull(message = "{" + MessageSeeds.Keys.REQUIRED_FIELD + "}")
        private Long configScheme;
        private String serviceCompany;
        private String technician;
        private Instant replaceBy;
        @NotNull(message = "{" + MessageSeeds.Keys.REQUIRED_FIELD + "}")
        private Quantity maxCurrentRating;
        @NotNull(message = "{" + MessageSeeds.Keys.REQUIRED_FIELD + "}")
        private Quantity maxVoltage;

        private Reference<Device> device = ValueReference.absent();

        @Override
        public void copyFrom(Device domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
            this.device.set(domainInstance);
            this.manufacturer = (String) propertyValues.getProperty(Fields.MANUFACTURER.javaName());
            this.modelNumber = (String) propertyValues.getProperty(Fields.MODEL_NUMBER.javaName());
            this.configScheme = (Long) propertyValues.getProperty(Fields.CONFIG_SCHEME.javaName());
            this.serviceCompany = (String) propertyValues.getProperty(Fields.SERVICE_COMPANY.javaName());
            this.technician = (String) propertyValues.getProperty(Fields.TECHNICIAN.javaName());
            this.replaceBy = (Instant) propertyValues.getProperty(Fields.REPLACE_BY.javaName());
            this.maxCurrentRating = (Quantity) propertyValues.getProperty(Fields.MAX_CURRENT_RATING.javaName());
            this.maxVoltage = (Quantity) propertyValues.getProperty(Fields.MAX_VOLTAGE.javaName());
        }

        @Override
        public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
            propertySetValues.setProperty(Fields.MANUFACTURER.javaName(), this.manufacturer);
            propertySetValues.setProperty(Fields.MODEL_NUMBER.javaName(), this.modelNumber);
            propertySetValues.setProperty(Fields.CONFIG_SCHEME.javaName(), this.configScheme);
            propertySetValues.setProperty(Fields.SERVICE_COMPANY.javaName(), this.serviceCompany);
            propertySetValues.setProperty(Fields.TECHNICIAN.javaName(), this.technician);
            propertySetValues.setProperty(Fields.REPLACE_BY.javaName(), this.replaceBy);
            propertySetValues.setProperty(Fields.MAX_CURRENT_RATING.javaName(), this.maxCurrentRating);
            propertySetValues.setProperty(Fields.MAX_VOLTAGE.javaName(), this.maxVoltage);
        }

        @Override
        public void validateDelete() {
            // allow delete
        }
    }

    public static class DeviceEMeterInfoPersistentSupport implements PersistenceSupport<Device, DeviceEMeterInfoDomainExtension> {
        private final Thesaurus thesaurus;

        public DeviceEMeterInfoPersistentSupport(Thesaurus thesaurus) {
            this.thesaurus = thesaurus;
        }

        @Override
        public String componentName() {
            return PREFIX;
        }

        @Override
        public String tableName() {
            return PREFIX + "_METER_INFO";
        }

        @Override
        public String domainFieldName() {
            return Fields.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return PREFIX + "_METER_INFO_FK";
        }

        @Override
        public Class<DeviceEMeterInfoDomainExtension> persistenceClass() {
            return DeviceEMeterInfoDomainExtension.class;
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
            table.column(Fields.MANUFACTURER.name()).map(Fields.MANUFACTURER.javaName()).varChar(Table.NAME_LENGTH).notNull().add();
            table.column(Fields.MODEL_NUMBER.name()).map(Fields.MODEL_NUMBER.javaName()).varChar(Table.NAME_LENGTH).notNull().add();
            table.column(Fields.CONFIG_SCHEME.name()).map(Fields.CONFIG_SCHEME.javaName()).number().conversion(ColumnConversion.NUMBER2LONGWRAPPER).notNull().add();
            table.column(Fields.SERVICE_COMPANY.name()).map(Fields.SERVICE_COMPANY.javaName()).varChar(Table.NAME_LENGTH).add();
            table.column(Fields.TECHNICIAN.name()).map(Fields.TECHNICIAN.javaName()).varChar(Table.NAME_LENGTH).add();
            table.column(Fields.REPLACE_BY.name()).map(Fields.REPLACE_BY.javaName()).number().conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.addQuantityColumns(Fields.MAX_CURRENT_RATING.name(), true, Fields.MAX_CURRENT_RATING.javaName());
            table.addQuantityColumns(Fields.MAX_VOLTAGE.name(), true, Fields.MAX_VOLTAGE.javaName());
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
    public DeviceEMeterInfoCustomPropertySet() {
    }

    @SuppressWarnings("unused") // TESTS
    @Inject
    public DeviceEMeterInfoCustomPropertySet(NlsService nlsService, PropertySpecService propertySpecService, DeviceService deviceService) {
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
        return this.thesaurus.getFormat(TranslationKeys.EMI_NAME).format();
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
    public PersistenceSupport<Device, DeviceEMeterInfoDomainExtension> getPersistenceSupport() {
        return new DeviceEMeterInfoPersistentSupport(this.thesaurus);
    }

    @Override
    public boolean isRequired() {
        return true;
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
        List<PropertySpec> properties = new ArrayList<>();
        properties.add(this.propertySpecService.stringSpec().named(Fields.MANUFACTURER.javaName(), TranslationKeys.EMI_PROPERTY_MANUFACTURER)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.stringSpec().named(Fields.MODEL_NUMBER.javaName(), TranslationKeys.EMI_PROPERTY_MODEL_NUMBER)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.longSpec().named(Fields.CONFIG_SCHEME.javaName(), TranslationKeys.EMI_PROPERTY_CONFIG_SCHEME)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .addValues(1L, 2L, 3L, 4L)
                .setDefaultValue(1L)
                .markExhaustive()
                .finish());
        properties.add(this.propertySpecService.stringSpec().named(Fields.SERVICE_COMPANY.javaName(), TranslationKeys.EMI_PROPERTY_SERVICE_COMPANY)
                .fromThesaurus(this.thesaurus)
                .addValues("SERV1", "SERV2", "SERV3", "SERV4")
                .markExhaustive()
                .finish());
        properties.add(this.propertySpecService.stringSpec().named(Fields.TECHNICIAN.javaName(), TranslationKeys.EMI_PROPERTY_TECHNICIAN)
                .fromThesaurus(this.thesaurus)
                .finish());
        properties.add(this.propertySpecService.specForValuesOf(new InstantFactory()).named(Fields.REPLACE_BY.javaName(), TranslationKeys.EMI_PROPERTY_REPLACE_BY)
                .fromThesaurus(this.thesaurus)
                .finish());
        properties.add(this.propertySpecService.specForValuesOf(new QuantityValueFactory()).named(Fields.MAX_CURRENT_RATING.javaName(), TranslationKeys.EMI_PROPERTY_MAX_CURRENT_RATING)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .addValues(Quantity.create(BigDecimal.ZERO, 0, "A"))
                .setDefaultValue(Quantity.create(BigDecimal.valueOf(100L), 0, "A"))
                .finish());
        properties.add(this.propertySpecService.specForValuesOf(new QuantityValueFactory()).named(Fields.MAX_VOLTAGE.javaName(), TranslationKeys.EMI_PROPERTY_MAX_VOLTAGE)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .addValues(Quantity.create(BigDecimal.ZERO, 0, "V"))
                .setDefaultValue(Quantity.create(BigDecimal.valueOf(400L), 0, "V"))
                .finish());
        return properties;
    }
}