package com.elster.jupiter.prepayment.impl.servicecall;

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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author sva
 * @since 30/03/2016 - 15:43
 */
@Component(name = "com.energyict.servicecall.redknee.contactoroperation.customPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + ContactorOperationCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class ContactorOperationCustomPropertySet implements CustomPropertySet<ServiceCall, ContactorOperationDomainExtension> {

    public static final String CUSTOM_PROPERTY_SET_NAME = "contactoroperation";

    public volatile PropertySpecService propertySpecService;

    public ContactorOperationCustomPropertySet() {
    }

    @Inject
    public ContactorOperationCustomPropertySet(CustomPropertySetService customPropertySetService) {
        customPropertySetService.addCustomPropertySet(this);
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
        customPropertySetService.addCustomPropertySet(this);
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
    public PersistenceSupport<ServiceCall, ContactorOperationDomainExtension> getPersistenceSupport() {
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
                        .named(ContactorOperationDomainExtension.FieldNames.CALLBACK.javaName(), ContactorOperationDomainExtension.FieldNames.CALLBACK.javaName())
                        .describedAs("Callback URL")
                        .finish());
    }

    private class MyPersistenceSupport implements PersistenceSupport<ServiceCall, ContactorOperationDomainExtension> {
        private final String TABLE_NAME = "RKN_SCS_CNT";
        private final String FK = "FK_RKN_SCS_CNT";

        @Override
        public String componentName() {
            return "RKN_CNT";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return ContactorOperationDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<ContactorOperationDomainExtension> persistenceClass() {
            return ContactorOperationDomainExtension.class;
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
                    .column(ContactorOperationDomainExtension.FieldNames.CALLBACK.databaseName())
                    .varChar()
                    .map(ContactorOperationDomainExtension.FieldNames.CALLBACK.javaName())
                    .notNull()
                    .add();
        }
    }
}