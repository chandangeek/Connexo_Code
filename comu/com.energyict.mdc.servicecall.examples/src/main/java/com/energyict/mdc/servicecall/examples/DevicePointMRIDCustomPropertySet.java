package com.energyict.mdc.servicecall.examples;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.energyict.servicecall.DeviceMRIDCustomPropertySet",
        service = CustomPropertySet.class,
        immediate = true)
public class DevicePointMRIDCustomPropertySet implements CustomPropertySet<ServiceCall, DeviceMRIDDomainExtension> {

    public DevicePointMRIDCustomPropertySet() {
    }

    @Inject
    public DevicePointMRIDCustomPropertySet(PropertySpecService propertySpecService, CustomPropertySetService customPropertySetService) {
        this.propertySpecService = propertySpecService;
        customPropertySetService.addCustomPropertySet(this);
    }

    public volatile PropertySpecService propertySpecService;

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        // PATCH; required for proper startup; do not delete
    }

    @Activate
    public void activate() {
        System.out.println("DeviceMRIDCustomPropertySet activating");
    }

    @Override
    public String getName() {
        return DevicePointMRIDCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<ServiceCall> getDomainClass() {
        return ServiceCall.class;
    }

    @Override
    public PersistenceSupport<ServiceCall, DeviceMRIDDomainExtension> getPersistenceSupport() {
        return new MyPersistenceSupport();
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
        return Collections.emptySet();
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return Collections.emptySet();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.singletonList(
                this.propertySpecService
                        .stringSpec()
                        .named(DeviceMRIDDomainExtension.FieldNames.MRID.javaName(), DeviceMRIDDomainExtension.FieldNames.MRID
                                .javaName())
                        .describedAs("Device mRID")
                        .setDefaultValue("no mRID")
                        .finish());
    }

    private class MyPersistenceSupport implements PersistenceSupport<ServiceCall, DeviceMRIDDomainExtension> {
        private final String TABLE_NAME = "TVN_SCS_CPS_DV";
        private final String FK = "FK_SCS_CPS_DV";

        @Override
        public String componentName() {
            return "DeviceMRID";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return DeviceMRIDDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<DeviceMRIDDomainExtension> persistenceClass() {
            return DeviceMRIDDomainExtension.class;
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
                    .column(DeviceMRIDDomainExtension.FieldNames.MRID.databaseName())
                    .varChar()
                    .map(DeviceMRIDDomainExtension.FieldNames.MRID.javaName())
                    .notNull()
                    .add();
        }
    }
}
