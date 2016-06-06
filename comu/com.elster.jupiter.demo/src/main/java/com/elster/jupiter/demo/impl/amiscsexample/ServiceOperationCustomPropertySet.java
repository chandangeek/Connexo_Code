package com.elster.jupiter.demo.impl.amiscsexample;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.units.Quantity;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.elster.jupiter.demo.impl.amiscsexample.service.operation.customPropertySet",
        service = CustomPropertySet.class,
        immediate = true)
public class ServiceOperationCustomPropertySet implements CustomPropertySet<ServiceCall, ServiceOperationDomainExtension> {

    public ServiceOperationCustomPropertySet() {
    }

    @Inject
    public ServiceOperationCustomPropertySet(CustomPropertySetService customPropertySetService) {
        customPropertySetService.addCustomPropertySet(this);
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
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
        System.out.println("Activating Contactor Operation Custom Property Set");
    }

    @Deactivate
    public void deactivate() {
    }

    @Override
    public String getName() {
        return ContactorOperationCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<ServiceCall> getDomainClass() {
        return ServiceCall.class;
    }

    @Override
    public PersistenceSupport<ServiceCall, ServiceOperationDomainExtension> getPersistenceSupport() {
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
        return Arrays.asList(
                this.propertySpecService
                        .stringSpec()
                        .named(ServiceOperationDomainExtension.FieldNames.MRID_DEVICE.javaName(), ServiceOperationDomainExtension.FieldNames.MRID_DEVICE
                                .javaName())
                        .describedAs("MRID device")
                        .finish(),
                this.propertySpecService
                        .longSpec()
                        .named(ServiceOperationDomainExtension.FieldNames.ACTIVATION_DATE.javaName(), ServiceOperationDomainExtension.FieldNames.ACTIVATION_DATE
                                .javaName())
                        .describedAs("Activation date")
                        .finish(),
                this.propertySpecService
                        .longSpec()
                        .named(ServiceOperationDomainExtension.FieldNames.BREAKER_STATUS.javaName(), ServiceOperationDomainExtension.FieldNames.BREAKER_STATUS
                                .javaName())
                        .describedAs("Desired breaker status")
                        .finish(),
                this.propertySpecService
                        .longSpec()
                        .named(ServiceOperationDomainExtension.FieldNames.NR_OF_UNCONFIRMED_DEVICE_COMMANDS.javaName(), ServiceOperationDomainExtension.FieldNames.NR_OF_UNCONFIRMED_DEVICE_COMMANDS
                                .javaName())
                        .describedAs("Nr of unconfirmed device commands")
                        .setDefaultValue(0L)
                        .finish(),
                this.propertySpecService
                        .longSpec()
                        .named(ServiceOperationDomainExtension.FieldNames.STATUS_INFORMATION_TASK_TRY_COUNT.javaName(), ServiceOperationDomainExtension.FieldNames.STATUS_INFORMATION_TASK_TRY_COUNT
                                .javaName())
                        .describedAs("Status information try count")
                        .setDefaultValue(0L)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(ServiceOperationDomainExtension.FieldNames.CALLBACK.javaName(), ServiceOperationDomainExtension.FieldNames.CALLBACK.javaName())
                        .describedAs("Callback URL")
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(ContactorOperationDomainExtension.FieldNames.LOAD_LIMIT_ENABLED.javaName(), ContactorOperationDomainExtension.FieldNames.LOAD_LIMIT_ENABLED.javaName())
                        .describedAs("Load Limit Enabled")
                        .setDefaultValue(false)
                        .finish(),
                this.propertySpecService
                        .referenceSpec(Quantity.class)
                        .named(ContactorOperationDomainExtension.FieldNames.LOAD_LIMIT.javaName(), ContactorOperationDomainExtension.FieldNames.LOAD_LIMIT.javaName())
                        .describedAs("Load Limit")
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(ServiceOperationDomainExtension.FieldNames.DESTINATION_SPEC_NAME.javaName(), ServiceOperationDomainExtension.FieldNames.DESTINATION_SPEC_NAME.javaName())
                        .describedAs("Destination Spec Name")
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(ServiceOperationDomainExtension.FieldNames.COMPLETION_MESSAGE.javaName(), ServiceOperationDomainExtension.FieldNames.COMPLETION_MESSAGE.javaName())
                        .describedAs("Completion Message")
                        .finish());

    }

    private class MyPersistenceSupport implements PersistenceSupport<ServiceCall, ServiceOperationDomainExtension> {
        private final String TABLE_NAME = "DDC_CPS_MSSRVOP_HEITF";
        private final String FK = "FK_DDC_CPS_MSSRVOP_HEITF";

        @Override
        public String componentName() {
            return "ServiceOperation";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return ServiceOperationDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<ServiceOperationDomainExtension> persistenceClass() {
            return ServiceOperationDomainExtension.class;
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
                    .column(ContactorOperationDomainExtension.FieldNames.MRID_DEVICE.databaseName())
                    .varChar()
                    .map(ContactorOperationDomainExtension.FieldNames.MRID_DEVICE.javaName())
                    .notNull()
                    .add();
            table
                    .column(ContactorOperationDomainExtension.FieldNames.ACTIVATION_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(ContactorOperationDomainExtension.FieldNames.ACTIVATION_DATE.javaName())
                    .notNull()
                    .add();
            table
                    .column(ContactorOperationDomainExtension.FieldNames.BREAKER_STATUS.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2ENUM)
                    .map(ContactorOperationDomainExtension.FieldNames.BREAKER_STATUS.javaName())
                    .notNull()
                    .add();
            table
                    .column(ContactorOperationDomainExtension.FieldNames.NR_OF_UNCONFIRMED_DEVICE_COMMANDS.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INT)
                    .map(ContactorOperationDomainExtension.FieldNames.NR_OF_UNCONFIRMED_DEVICE_COMMANDS.javaName())
                    //.notNull()
                    .add();
            table
                    .column(ContactorOperationDomainExtension.FieldNames.STATUS_INFORMATION_TASK_TRY_COUNT.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INT)
                    .map(ContactorOperationDomainExtension.FieldNames.STATUS_INFORMATION_TASK_TRY_COUNT.javaName())
                    //.notNull()
                    .add();
            table
                    .column(ContactorOperationDomainExtension.FieldNames.CALLBACK.databaseName())
                    .varChar()
                    .map(ContactorOperationDomainExtension.FieldNames.CALLBACK.javaName())
                    //.notNull()
                    .add();
            table
                    .column(ContactorOperationDomainExtension.FieldNames.LOAD_LIMIT_ENABLED.databaseName())
                    .varChar()
                    .map(ContactorOperationDomainExtension.FieldNames.LOAD_LIMIT_ENABLED.javaName())
                    //.notNull()
                    .add();
            table
                    .column(ContactorOperationDomainExtension.FieldNames.LOAD_LIMIT.databaseName())
                    .varChar()
                    .map(ContactorOperationDomainExtension.FieldNames.LOAD_LIMIT.javaName())
                    //.notNull()
                    .add();
            table
                    .column(ContactorOperationDomainExtension.FieldNames.DESTINATION_SPEC_NAME.databaseName())
                    .varChar()
                    .map(ContactorOperationDomainExtension.FieldNames.DESTINATION_SPEC_NAME.javaName())
                    .add();
            table
                    .column(ContactorOperationDomainExtension.FieldNames.COMPLETION_MESSAGE.databaseName())
                    .varChar()
                    .map(ContactorOperationDomainExtension.FieldNames.COMPLETION_MESSAGE.javaName())
                    .add();
        }
    }
}
