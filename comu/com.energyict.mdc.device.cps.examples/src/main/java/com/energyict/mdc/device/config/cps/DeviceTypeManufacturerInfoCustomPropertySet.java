/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.energyict.mdc.device.config.cps.DeviceTypeTypeCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class DeviceTypeManufacturerInfoCustomPropertySet implements CustomPropertySet<DeviceType, DeviceTypeManufacturerInfoDomainExtension> {

    public static final String TABLE_NAME = "DMI_MANUFACTURER_INFO_CPS";
    public static final String FK_DMI_MANUFACT_INFO_DT = "FK_DMI_MANUFACT_INFO_DT";
    public static final String PREFIX = "DMI";

    private volatile PropertySpecService propertySpecService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile Thesaurus thesaurus;

    @Reference
    public void setDeviceConfiguratyionService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setCustomPropertySetsDemoInstaller(CustomPropertySetsDemoInstaller customPropertySetsDemoInstaller) {
        /* For translations. To wait untill CustomPropertySetsDemoInstaller is up. */
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(CustomPropertySetsDemoInstaller.COMPONENT_NAME, Layer.DOMAIN);
    }

    /* OSGI */
    public DeviceTypeManufacturerInfoCustomPropertySet() {
        super();
    }

    @Inject
    public DeviceTypeManufacturerInfoCustomPropertySet(PropertySpecService propertySpecService, DeviceConfigurationService deviceConfigurationService, NlsService nlsService)  {
        this();
        this.setNlsService(nlsService);
        this.setPropertySpecService(propertySpecService);
        this.setDeviceConfiguratyionService(deviceConfigurationService);
    }

    private static class DeviceTypePeristenceSupport implements PersistenceSupport<DeviceType, DeviceTypeManufacturerInfoDomainExtension> {
        @Override
        public String application() {
            return "Example";
        }

        @Override
        public String componentName() {
            return PREFIX;
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return DeviceTypeManufacturerInfoDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_DMI_MANUFACT_INFO_DT;
        }

        @Override
        public Class<DeviceTypeManufacturerInfoDomainExtension> persistenceClass() {
            return DeviceTypeManufacturerInfoDomainExtension.class;
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
                    .column(DeviceTypeManufacturerInfoDomainExtension.FieldNames.MANUFACT_ID_NUMBER.databaseName())
                    .number()
                    .map(DeviceTypeManufacturerInfoDomainExtension.FieldNames.MANUFACT_ID_NUMBER.javaName())
                    .notNull()
                    .add();
            table
                    .column(DeviceTypeManufacturerInfoDomainExtension.FieldNames.MANUFACT_NAME_STRING.databaseName())
                    .varChar()
                    .map(DeviceTypeManufacturerInfoDomainExtension.FieldNames.MANUFACT_NAME_STRING.javaName())
                    .notNull()
                    .add();
        }
    }


    @Override
    public PersistenceSupport<DeviceType, DeviceTypeManufacturerInfoDomainExtension> getPersistenceSupport() {
        return new DeviceTypePeristenceSupport();
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
    public Class<DeviceType> getDomainClass() {
        return DeviceType.class;
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
    public String getName() {
        return this.thesaurus.getFormat(TranslationKeys.DMI_NAME).format();
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.DMI_DOMAIN_NAME).format();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(DeviceTypeManufacturerInfoDomainExtension.FieldNames.MANUFACT_ID_NUMBER.javaName(), TranslationKeys.DMI_MANUFACTURER_ID)
                        .fromThesaurus(this.thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(DeviceTypeManufacturerInfoDomainExtension.FieldNames.MANUFACT_NAME_STRING.javaName(), TranslationKeys.DMI_MANUFACTURER_NAME)
                        .fromThesaurus(this.thesaurus)
                        .markRequired()
                        .finish());
    }

}
