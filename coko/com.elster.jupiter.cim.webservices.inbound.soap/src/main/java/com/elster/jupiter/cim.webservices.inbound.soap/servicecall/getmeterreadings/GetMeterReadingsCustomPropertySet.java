package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.getmeterreadings;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.CIMInboundSoapEndpointsActivator;
import com.elster.jupiter.cim.webservices.inbound.soap.impl.TranslationKeys;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component(name = "com.elster.jupiter.cim.webservices.inbound.soap.GetMeterReadingsCustomPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + GetMeterReadingsCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class GetMeterReadingsCustomPropertySet implements CustomPropertySet<ServiceCall, GetMeterReadingsDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_NAME = "GetMeterReadingsCustomPropertySet";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public GetMeterReadingsCustomPropertySet() {
        // for test purposes
    }

    @Inject
    public GetMeterReadingsCustomPropertySet(PropertySpecService propertySpecService, CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
        this();
        this.setPropertySpecService(propertySpecService);
        this.setCustomPropertySetService(customPropertySetService);
        this.thesaurus = thesaurus;
    }

    @Reference
    @SuppressWarnings("unused") // For OSGi framework
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    @SuppressWarnings("unused") // For OSGi framework
    public void setServiceCallService(ServiceCallService serviceCallService) {
        // PATCH; required for proper startup; do not delete
    }

    @Reference
    @SuppressWarnings("unused") // For OSGi framework
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        customPropertySetService.addCustomPropertySet(this);
    }

    @Reference
    @SuppressWarnings("unused") // For OSGi framework
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(CIMInboundSoapEndpointsActivator.COMPONENT_NAME, Layer.SOAP);
    }

    @Override
    public String getName() {
        return GetMeterReadingsCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<ServiceCall> getDomainClass() {
        return ServiceCall.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.DOMAIN_NAME).format();
    }

    @Override
    public PersistenceSupport<ServiceCall, GetMeterReadingsDomainExtension> getPersistenceSupport() {
        return new GetMeterReadingsCustomPropertyPersistenceSupport();
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
                        .bigDecimalSpec()
                        .named(GetMeterReadingsDomainExtension.FieldNames.PARENT_SERVICE_CALL.javaName(), TranslationKeys.PARENT_SERVICE_CALL)
                        .describedAs(TranslationKeys.PARENT_SERVICE_CALL)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(GetMeterReadingsDomainExtension.FieldNames.END_DEVICE_MRID.javaName(), TranslationKeys.END_DEVICE_MRID)
                        .describedAs(TranslationKeys.END_DEVICE_MRID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(GetMeterReadingsDomainExtension.FieldNames.END_DEVICE_NAME.javaName(), TranslationKeys.END_DEVICE_NAME)
                        .describedAs(TranslationKeys.END_DEVICE_NAME)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(GetMeterReadingsDomainExtension.FieldNames.REGISTERS.javaName(), TranslationKeys.REGISTERS)
                        .describedAs(TranslationKeys.REGISTERS)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(GetMeterReadingsDomainExtension.FieldNames.CHANNELS.javaName(), TranslationKeys.CHANNELS)
                        .describedAs(TranslationKeys.CHANNELS)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class GetMeterReadingsCustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, GetMeterReadingsDomainExtension> {
        /// TODO check table name and FK
        private final String TABLE_NAME = "GMR_SCS_CNT";
        private final String FK = "FK_GMR_SCS_CNT";

        /// TODO check component name!
        @Override
        public String componentName() {
            return "HZC";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return GetMeterReadingsDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<GetMeterReadingsDomainExtension> persistenceClass() {
            return GetMeterReadingsDomainExtension.class;
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
            table.column(GetMeterReadingsDomainExtension.FieldNames.PARENT_SERVICE_CALL.databaseName())
                    .number()
                    .map(GetMeterReadingsDomainExtension.FieldNames.PARENT_SERVICE_CALL.javaName())
                    .notNull()
                    .add();
            table.column(GetMeterReadingsDomainExtension.FieldNames.END_DEVICE_MRID.databaseName())
                    .varChar()
                    .map(GetMeterReadingsDomainExtension.FieldNames.END_DEVICE_MRID.javaName())
                    .notNull()
                    .add();
            table.column(GetMeterReadingsDomainExtension.FieldNames.END_DEVICE_NAME.databaseName())
                    .varChar()
                    .map(GetMeterReadingsDomainExtension.FieldNames.END_DEVICE_NAME.javaName())
                    .notNull()
                    .add();
            table.column(GetMeterReadingsDomainExtension.FieldNames.REGISTERS.databaseName())
                    .varChar()
                    .map(GetMeterReadingsDomainExtension.FieldNames.REGISTERS.javaName())
                    .add();
            table.column(GetMeterReadingsDomainExtension.FieldNames.CHANNELS.databaseName())
                    .varChar()
                    .map(GetMeterReadingsDomainExtension.FieldNames.CHANNELS.javaName())
                    .add();
        }

        @Override
        public String application() {
            return "MultiSense";
        }
    }
}
