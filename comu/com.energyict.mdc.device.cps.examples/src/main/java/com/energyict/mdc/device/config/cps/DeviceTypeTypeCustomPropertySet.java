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
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.energyict.mdc.device.config.cps.DeviceTypeTypeCustomPropertySet", service = CustomPropertySet.class, immediate = true)
public class DeviceTypeTypeCustomPropertySet  implements CustomPropertySet<DeviceType, DeviceTypeTypeDomainExtension> {

/*XROMVYU */
    public static final String TABLE_NAME = "RVK_CPS_DEVICE_TYPE";
    public static final String FK_CPS_DEVICE_TYPE = "FK_CPS_DEVICE_TYPE";

    public volatile PropertySpecService propertySpecService;
    public volatile DeviceConfigurationService deviceConfigurationService;
    private volatile Thesaurus thesaurus;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setDeviceConfiguratyionService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }


    @SuppressWarnings("unused") // OSGI
    @org.osgi.service.component.annotations.Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(CustomPropertySetsDemoInstaller.COMPONENT_NAME, Layer.DOMAIN);
    }

    public DeviceTypeTypeCustomPropertySet() {
        super();
        System.out.println("CREATE DeviceTypeTypeCustomPropertySet!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    @Inject
    public DeviceTypeTypeCustomPropertySet(PropertySpecService propertySpecService, DeviceConfigurationService deviceConfigurationService){
        this();
        this.setPropertySpecService(propertySpecService);
        this.setDeviceConfiguratyionService(deviceConfigurationService);
    }

    @Inject
    public DeviceTypeTypeCustomPropertySet(PropertySpecService propertySpecService, DeviceConfigurationService deviceConfigurationService, NlsService nlsService)  {
        this();
        this.setNlsService(nlsService);
        this.setPropertySpecService(propertySpecService);
        this.setDeviceConfiguratyionService(deviceConfigurationService);
    }




    private static class DeviceTypePeristenceSupport implements PersistenceSupport<DeviceType, DeviceTypeTypeDomainExtension> {
        @Override
        public String application() {
            return "Example";
        }

        @Override
        public String componentName() {
            return "DTP";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return DeviceTypeTypeDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_CPS_DEVICE_TYPE;
        }

        @Override
        public Class<DeviceTypeTypeDomainExtension> persistenceClass() {
            return DeviceTypeTypeDomainExtension.class;
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
                    .column(DeviceTypeTypeDomainExtension.FieldNames.MANUFACT_ID_NUMBER.databaseName())
                    .number()
                    .map(DeviceTypeTypeDomainExtension.FieldNames.MANUFACT_ID_NUMBER.javaName())
                    .notNull()
                    .add();
            table
                    .column(DeviceTypeTypeDomainExtension.FieldNames.MANUFACT_NAME_STRING.databaseName())
                    .varChar()
                    .map(DeviceTypeTypeDomainExtension.FieldNames.MANUFACT_NAME_STRING.javaName())
                    .notNull()
                    .add();
        }
    }


    @Override
    public PersistenceSupport<DeviceType, DeviceTypeTypeDomainExtension> getPersistenceSupport() {
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
        return DeviceTypeTypeCustomPropertySet.class.getSimpleName();
    }

    @Override
    public String getDomainClassDisplayName() {

        return "Device type";//this.thesaurus.getFormat(TranslationKeys.DOMAIN_NAME_DEVICE).format();  // CONM-332
        //return "Device";
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(DeviceTypeTypeDomainExtension.FieldNames.MANUFACT_ID_NUMBER.javaName(), DeviceTypeTypeDomainExtension.FieldNames.MANUFACT_ID_NUMBER.javaName())
                        .describedAs("manufacturerId")
                        .setDefaultValue(BigDecimal.ZERO)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(DeviceTypeTypeDomainExtension.FieldNames.MANUFACT_NAME_STRING.javaName(), DeviceTypeTypeDomainExtension.FieldNames.MANUFACT_NAME_STRING.javaName())
                        .describedAs("manufacturerName")
                        .markRequired()
                        .finish());
    }

}
