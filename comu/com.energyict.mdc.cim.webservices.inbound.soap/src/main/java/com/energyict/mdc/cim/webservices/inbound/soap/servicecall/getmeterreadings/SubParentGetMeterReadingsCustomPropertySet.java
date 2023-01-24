/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings;

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
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.TranslationKeys;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.elster.jupiter.orm.Version.version;

@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.SubParentGetMeterReadingsCustomPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + SubParentGetMeterReadingsCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class SubParentGetMeterReadingsCustomPropertySet implements CustomPropertySet<ServiceCall, SubParentGetMeterReadingsDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_NAME = "SubParentGetMeterReadingsCustomPropertySet";
    public static final String CUSTOM_PROPERTY_SET_ID = SubParentGetMeterReadingsDomainExtension.class.getName();

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    // For OSGi framework
    public SubParentGetMeterReadingsCustomPropertySet() {
    }

    @Inject
    public SubParentGetMeterReadingsCustomPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        this();
        this.setPropertySpecService(propertySpecService);
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
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(InboundSoapEndpointsActivator.COMPONENT_NAME, Layer.SOAP);
    }

    @Override
    public String getId() {
        return CUSTOM_PROPERTY_SET_ID;
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(TranslationKeys.SGMR_CPS).format();
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
    public PersistenceSupport<ServiceCall, SubParentGetMeterReadingsDomainExtension> getPersistenceSupport() {
        return new SubParentGetMeterReadingsCustomPropertyPersistenceSupport();
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
                        .named(SubParentGetMeterReadingsDomainExtension.FieldNames.END_DEVICE_NAME.javaName(), TranslationKeys.END_DEVICE_NAME)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(SubParentGetMeterReadingsDomainExtension.FieldNames.END_DEVICE_MRID.javaName(), TranslationKeys.END_DEVICE_MRID)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(SubParentGetMeterReadingsDomainExtension.FieldNames.END_DEVICE_SERIAL_NUMBER.javaName(), TranslationKeys.END_DEVICE_SERIAL_NUMBER)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish()
        );
    }

    private static class SubParentGetMeterReadingsCustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, SubParentGetMeterReadingsDomainExtension> {
        private static final String TABLE_NAME = "GMR_SUBPARENT_CPS";
        private static final String FK = "FK_GMR_SUBPARENT_CPS";

        @Override
        public String componentName() {
            return "GM2";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return SubParentGetMeterReadingsDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<SubParentGetMeterReadingsDomainExtension> persistenceClass() {
            return SubParentGetMeterReadingsDomainExtension.class;
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
            table.column(SubParentGetMeterReadingsDomainExtension.FieldNames.END_DEVICE_NAME.databaseName())
                    .varChar()
                    .map(SubParentGetMeterReadingsDomainExtension.FieldNames.END_DEVICE_NAME.javaName())
                    .notNull()
                    .add();
            table.column(SubParentGetMeterReadingsDomainExtension.FieldNames.END_DEVICE_MRID.databaseName())
                    .varChar()
                    .map(SubParentGetMeterReadingsDomainExtension.FieldNames.END_DEVICE_MRID.javaName())
                    .notNull()
                    .add();
            table.column(SubParentGetMeterReadingsDomainExtension.FieldNames.END_DEVICE_SERIAL_NUMBER.databaseName())
                    .varChar()
                    .map(SubParentGetMeterReadingsDomainExtension.FieldNames.END_DEVICE_SERIAL_NUMBER.javaName())
                    .notNull()
                    .installValue("'-'")
                    .since(version(10, 9, 23))
                    .add();
        }

        @Override
        public String application() {
            return "MultiSense";
        }
    }
}
