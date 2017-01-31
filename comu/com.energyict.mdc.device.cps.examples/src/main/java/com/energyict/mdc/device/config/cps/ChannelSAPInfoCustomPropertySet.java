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
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.data.DeviceService;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.energyict.mdc.device.config.cps.ChannelSAPInfoCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class ChannelSAPInfoCustomPropertySet implements CustomPropertySet<ChannelSpec, ChannelSAPInfoCustomPropertySet.ChannelSAPInfoDomainExtension> {

    public static final String PREFIX = "SCI";

    private enum Fields {
        DOMAIN("channelSpec"),
        DEVICE("device"),
        LOGICAL_REGISTER_NUMBER("logicalRegisterNumber"),
        PROFILE_NUMBER("profileNumber"),
        IN_USE("inUse"),
        BILLING_FACTOR("billingFactor"),;

        Fields(String javaName) {
            this.javaName = javaName;
        }

        private final String javaName;

        public String javaName() {
            return javaName;
        }
    }

    public static class ChannelSAPInfoDomainExtension extends AbstractVersionedPersistentDomainExtension implements PersistentDomainExtension<ChannelSpec> {
        @NotNull(message = "{" + MessageSeeds.Keys.REQUIRED_FIELD + "}")
        private BigDecimal logicalRegisterNumber;
        @NotNull(message = "{" + MessageSeeds.Keys.REQUIRED_FIELD + "}")
        private BigDecimal profileNumber;
        @NotNull(message = "{" + MessageSeeds.Keys.REQUIRED_FIELD + "}")
        private Boolean inUse;
        @NotNull(message = "{" + MessageSeeds.Keys.REQUIRED_FIELD + "}")
        private BigDecimal billingFactor;

        private Reference<ChannelSpec> channelSpec = ValueReference.absent();
        private Long device;

        @Override
        public void copyFrom(ChannelSpec domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
            this.channelSpec.set(domainInstance);
            this.device = (Long) additionalPrimaryKeyValues[0];
            this.logicalRegisterNumber = (BigDecimal) propertyValues.getProperty(Fields.LOGICAL_REGISTER_NUMBER.javaName());
            this.profileNumber = (BigDecimal) propertyValues.getProperty(Fields.PROFILE_NUMBER.javaName());
            this.inUse = (Boolean) propertyValues.getProperty(Fields.IN_USE.javaName());
            this.billingFactor = (BigDecimal) propertyValues.getProperty(Fields.BILLING_FACTOR.javaName());
        }

        @Override
        public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
            additionalPrimaryKeyValues[0] = this.device;
            propertySetValues.setProperty(Fields.LOGICAL_REGISTER_NUMBER.javaName(), this.logicalRegisterNumber);
            propertySetValues.setProperty(Fields.PROFILE_NUMBER.javaName(), this.profileNumber);
            propertySetValues.setProperty(Fields.IN_USE.javaName(), this.inUse);
            propertySetValues.setProperty(Fields.BILLING_FACTOR.javaName(), this.billingFactor);
        }

        @Override
        public void validateDelete() {
            // allow delete
        }
    }

    public static class ChannelSAPInfoPersistentSupport implements PersistenceSupport<ChannelSpec, ChannelSAPInfoDomainExtension> {
        private final Thesaurus thesaurus;

        public ChannelSAPInfoPersistentSupport(Thesaurus thesaurus) {
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
        public Class<ChannelSAPInfoDomainExtension> persistenceClass() {
            return ChannelSAPInfoDomainExtension.class;
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
            return Collections.singletonList(table.column(Fields.DEVICE.name()).number().map(Fields.DEVICE.javaName()).conversion(ColumnConversion.NUMBER2LONG).notNull().add());
        }

        @Override
        public void addCustomPropertyColumnsTo(Table table, List<Column> customPrimaryKeyColumns) {
            table.column(Fields.LOGICAL_REGISTER_NUMBER.name()).map(Fields.LOGICAL_REGISTER_NUMBER.javaName()).number().notNull().add();
            table.column(Fields.PROFILE_NUMBER.name()).map(Fields.PROFILE_NUMBER.javaName()).number().notNull().add();
            table.column(Fields.IN_USE.name()).map(Fields.IN_USE.javaName()).bool().notNull().add();
            table.column(Fields.BILLING_FACTOR.name()).map(Fields.BILLING_FACTOR.javaName()).number().notNull().add();
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
    public ChannelSAPInfoCustomPropertySet() {
    }

    @SuppressWarnings("unused") // TESTS
    @Inject
    public ChannelSAPInfoCustomPropertySet(NlsService nlsService, PropertySpecService propertySpecService, DeviceService deviceService) {
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
        return this.thesaurus.getFormat(TranslationKeys.SCI_NAME).format();
    }

    @Override
    public Class<ChannelSpec> getDomainClass() {
        return ChannelSpec.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.DOMAIN_NAME_CHANNEL).format();
    }

    @Override
    public PersistenceSupport<ChannelSpec, ChannelSAPInfoDomainExtension> getPersistenceSupport() {
        return new ChannelSAPInfoPersistentSupport(this.thesaurus);
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
        properties.add(this.propertySpecService.bigDecimalSpec().named(Fields.LOGICAL_REGISTER_NUMBER.javaName(), TranslationKeys.SCI_PROPERTY_LOGICAL_REGISTER_NUMBER)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.bigDecimalSpec().named(Fields.PROFILE_NUMBER.javaName(), TranslationKeys.SCI_PROPERTY_PROFILE_NUMBER)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish());
        properties.add(this.propertySpecService.booleanSpec().named(Fields.IN_USE.javaName(), TranslationKeys.SCI_PROPERTY_IN_USE)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .setDefaultValue(Boolean.FALSE)
                .finish());
        properties.add(this.propertySpecService.bigDecimalSpec().named(Fields.BILLING_FACTOR.javaName(), TranslationKeys.SCI_PROPERTY_BILLING_FACTOR)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .setDefaultValue(BigDecimal.valueOf(1L))
                .finish());
        return properties;
    }
}