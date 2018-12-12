package com.energyict.mdc.device.data.impl.ami.servicecall;

import com.elster.jupiter.cps.*;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.*;

@Component(name = "com.energyict.mdc.device.data.CommunicationTestServiceCallCustomPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + CommunicationTestServiceCallCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class CommunicationTestServiceCallCustomPropertySet implements CustomPropertySet<ServiceCall, CommunicationTestServiceCallDomainExtension> {

    public static final String CUSTOM_PROPERTY_SET_NAME = "CommunicationTestServiceCallCustomPropertySet";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;
    private volatile CustomPropertySetService customPropertySetService;

    public CommunicationTestServiceCallCustomPropertySet() {
    }

    @Inject
    public CommunicationTestServiceCallCustomPropertySet(PropertySpecService propertySpecService, CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setCustomPropertySetService(customPropertySetService);
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        // PATCH; required for proper startup; do not delete
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
        this.customPropertySetService.addCustomPropertySet(this);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Activate
    public void activate() {
    }

    @Override
    public String getName() {
        return CommunicationTestServiceCallCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<ServiceCall> getDomainClass() {
        return ServiceCall.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(CustomPropertySetsTranslationKeys.DOMAIN_NAME).format();
    }

    @Override
    public PersistenceSupport<ServiceCall, CommunicationTestServiceCallDomainExtension> getPersistenceSupport() {
        return new CommunicationTestServiceCallPersistenceSupport();
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
        return Collections.emptySet();
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return Collections.emptySet();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(CommunicationTestServiceCallDomainExtension.FieldNames.EXPECTED_TASKS.javaName(), CommunicationTestServiceCallDomainExtension.FieldNames.EXPECTED_TASKS
                                .javaName())
                        .describedAs(CommunicationTestServiceCallDomainExtension.FieldNames.EXPECTED_TASKS.javaName())
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(CommunicationTestServiceCallDomainExtension.FieldNames.SUCCESSFUL_TASKS.javaName(), CommunicationTestServiceCallDomainExtension.FieldNames.SUCCESSFUL_TASKS
                                .javaName())
                        .describedAs(CommunicationTestServiceCallDomainExtension.FieldNames.SUCCESSFUL_TASKS.javaName())
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(CommunicationTestServiceCallDomainExtension.FieldNames.COMPLETED_TASKS.javaName(), CommunicationTestServiceCallDomainExtension.FieldNames.COMPLETED_TASKS
                                .javaName())
                        .describedAs(CommunicationTestServiceCallDomainExtension.FieldNames.COMPLETED_TASKS.javaName())
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(CommunicationTestServiceCallDomainExtension.FieldNames.TRIGGERDATE.javaName(), CommunicationTestServiceCallDomainExtension.FieldNames.TRIGGERDATE
                                .javaName())
                        .describedAs(CommunicationTestServiceCallDomainExtension.FieldNames.TRIGGERDATE.javaName())
                        .finish()
        );
    }

    private class CommunicationTestServiceCallPersistenceSupport implements PersistenceSupport<ServiceCall, CommunicationTestServiceCallDomainExtension> {
        private final String TABLE_NAME = "DDC_CPS_SERVICECALL_COM";
        private final String FK_DDC_CPS_SC_COM = "FK_DDC_CPS_SC_COM";

        @Override
        public String componentName() {
            return "STC";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return CommunicationTestServiceCallDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK_DDC_CPS_SC_COM;
        }

        @Override
        public Class<CommunicationTestServiceCallDomainExtension> persistenceClass() {
            return CommunicationTestServiceCallDomainExtension.class;
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
                    .column(CommunicationTestServiceCallDomainExtension.FieldNames.EXPECTED_TASKS.databaseName())
                    .number()
                    .map(CommunicationTestServiceCallDomainExtension.FieldNames.EXPECTED_TASKS.javaName())
                    .notNull()
                    .add();
            table
                    .column(CommunicationTestServiceCallDomainExtension.FieldNames.SUCCESSFUL_TASKS.databaseName())
                    .number()
                    .map(CommunicationTestServiceCallDomainExtension.FieldNames.SUCCESSFUL_TASKS.javaName())
                    .notNull()
                    .add();
            table
                    .column(CommunicationTestServiceCallDomainExtension.FieldNames.COMPLETED_TASKS.databaseName())
                    .number()
                    .map(CommunicationTestServiceCallDomainExtension.FieldNames.COMPLETED_TASKS.javaName())
                    .notNull()
                    .add();
            table
                    .column(CommunicationTestServiceCallDomainExtension.FieldNames.TRIGGERDATE.databaseName())
                    .number()
                    .map(CommunicationTestServiceCallDomainExtension.FieldNames.TRIGGERDATE.javaName())
                    .notNull()
                    .add();
        }

        @Override
        public String application() {
            return "MultiSense";
        }
    }
}