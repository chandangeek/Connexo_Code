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

@Component(name = "com.elster.jupiter.cim.webservices.inbound.soap.ParentGetMeterReadingsCustomPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + ParentGetMeterReadingsCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class ParentGetMeterReadingsCustomPropertySet implements CustomPropertySet<ServiceCall, ParentGetMeterReadingsDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_NAME = "ParentGetMeterReadingsCustomPropertySet";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public ParentGetMeterReadingsCustomPropertySet () {
        // for test purposes
    }

    @Inject
    public ParentGetMeterReadingsCustomPropertySet(PropertySpecService propertySpecService, CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
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
        return ParentGetMeterReadingsCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<ServiceCall, ParentGetMeterReadingsDomainExtension> getPersistenceSupport() {
        return new ParentGetMeterReadingsCustomPropertyPersistenceSupport();
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
                        .named(ParentGetMeterReadingsDomainExtension.FieldNames.SOURCE.javaName(), TranslationKeys.SOURCE)
                        .describedAs(TranslationKeys.SOURCE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(ParentGetMeterReadingsDomainExtension.FieldNames.CALLBACK_URL.javaName(), TranslationKeys.CALLBACK_URL)
                        .describedAs(TranslationKeys.CALLBACK_URL)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(ParentGetMeterReadingsDomainExtension.FieldNames.TIME_PERIOD_START.javaName(), TranslationKeys.TIME_PERIOD_START)
                        .describedAs(TranslationKeys.TIME_PERIOD_START)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(ParentGetMeterReadingsDomainExtension.FieldNames.TIME_PERIOD_END.javaName(), TranslationKeys.TIME_PERIOD_END)
                        .describedAs(TranslationKeys.TIME_PERIOD_END)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(ParentGetMeterReadingsDomainExtension.FieldNames.READING_TYPES.javaName(), TranslationKeys.READING_TYPES)
                        .describedAs(TranslationKeys.READING_TYPES)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class ParentGetMeterReadingsCustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, ParentGetMeterReadingsDomainExtension> {
        /// TODO check table name and FK
        private final String TABLE_NAME = "GMP_SCS_CNT";
        private final String FK = "FK_GMP_SCS_CNT";

        /// TODO check component name
        @Override
        public String componentName() {
            return "HZP";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return ParentGetMeterReadingsDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<ParentGetMeterReadingsDomainExtension> persistenceClass() {
            return ParentGetMeterReadingsDomainExtension.class;
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
            table.column(ParentGetMeterReadingsDomainExtension.FieldNames.SOURCE.databaseName())
                    .varChar()
                    .map(ParentGetMeterReadingsDomainExtension.FieldNames.SOURCE.javaName())
                    .notNull()
                    .add();
            table.column(ParentGetMeterReadingsDomainExtension.FieldNames.CALLBACK_URL.databaseName())
                    .varChar()
                    .map(ParentGetMeterReadingsDomainExtension.FieldNames.CALLBACK_URL.javaName())
                    .notNull()
                    .add();
            table.column(ParentGetMeterReadingsDomainExtension.FieldNames.TIME_PERIOD_START.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(ParentGetMeterReadingsDomainExtension.FieldNames.TIME_PERIOD_START.javaName())
                    .add();
            table.column(ParentGetMeterReadingsDomainExtension.FieldNames.TIME_PERIOD_END.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(ParentGetMeterReadingsDomainExtension.FieldNames.TIME_PERIOD_END.javaName())
                    .add();
            table.column(ParentGetMeterReadingsDomainExtension.FieldNames.READING_TYPES.databaseName())
                    .varChar()
                    .map(ParentGetMeterReadingsDomainExtension.FieldNames.READING_TYPES.javaName())
                    .notNull()
                    .add();
        }

        @Override
        public String application() {
            return "MultiSense";
        }
    }
}
