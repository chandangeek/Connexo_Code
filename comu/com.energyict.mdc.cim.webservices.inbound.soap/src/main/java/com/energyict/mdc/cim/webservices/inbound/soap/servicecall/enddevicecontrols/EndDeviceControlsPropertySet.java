/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.enddevicecontrols;

import com.elster.jupiter.cps.CustomPropertySet;
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
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
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

@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.EndDeviceControlsPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + EndDeviceControlsPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class EndDeviceControlsPropertySet implements CustomPropertySet<ServiceCall, EndDeviceControlsDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_NAME = "EndDeviceControlsPropertySet";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    // For OSGi framework
    public EndDeviceControlsPropertySet() {
    }

    @Inject
    public EndDeviceControlsPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
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
    public String getName() {
        return thesaurus.getFormat(TranslationKeys.CEDC_CPS).format();
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
    public PersistenceSupport<ServiceCall, EndDeviceControlsDomainExtension> getPersistenceSupport() {
        return new CustomPropertyPersistenceSupport();
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
                        .named(EndDeviceControlsDomainExtension.FieldNames.DEVICE_NAME.javaName(), TranslationKeys.DEVICE_NAME)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(EndDeviceControlsDomainExtension.FieldNames.DEVICE_MRID.javaName(), TranslationKeys.DEVICE_MRID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(EndDeviceControlsDomainExtension.FieldNames.DEVICE_SERIAL_NUMBER.javaName(), TranslationKeys.DEVICE_SERIAL_NUMBER)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(EndDeviceControlsDomainExtension.FieldNames.TRIGGER_DATE.javaName(), TranslationKeys.TRIGGER_DATE)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(EndDeviceControlsDomainExtension.FieldNames.ERROR.javaName(), TranslationKeys.ERROR)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(EndDeviceControlsDomainExtension.FieldNames.CANCELLATION_REASON.javaName(), TranslationKeys.CANCELLATION_REASON)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private static class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, EndDeviceControlsDomainExtension> {
        private static final String TABLE_NAME = "DC3_EDC_CHILD_CPS";
        private static final String FK = "FK_DC3_EDC_CHILD_CPS";

        @Override
        public String componentName() {
            return "DC3";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return EndDeviceControlsDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<EndDeviceControlsDomainExtension> persistenceClass() {
            return EndDeviceControlsDomainExtension.class;
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
            table.column(EndDeviceControlsDomainExtension.FieldNames.DEVICE_NAME.databaseName())
                    .varChar()
                    .map(EndDeviceControlsDomainExtension.FieldNames.DEVICE_NAME.javaName())
                    .add();
            table.column(EndDeviceControlsDomainExtension.FieldNames.DEVICE_MRID.databaseName())
                    .varChar()
                    .map(EndDeviceControlsDomainExtension.FieldNames.DEVICE_MRID.javaName())
                    .add();
            table.column(EndDeviceControlsDomainExtension.FieldNames.DEVICE_SERIAL_NUMBER.databaseName())
                    .varChar()
                    .map(EndDeviceControlsDomainExtension.FieldNames.DEVICE_SERIAL_NUMBER.javaName())
                    .since(version(10, 9, 24))
                    .add();
            table.column(EndDeviceControlsDomainExtension.FieldNames.TRIGGER_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(EndDeviceControlsDomainExtension.FieldNames.TRIGGER_DATE.javaName())
                    .notNull()
                    .add();
            table.column(EndDeviceControlsDomainExtension.FieldNames.ERROR.databaseName())
                    .varChar()
                    .map(EndDeviceControlsDomainExtension.FieldNames.ERROR.javaName())
                    .add();
            table.column(EndDeviceControlsDomainExtension.FieldNames.CANCELLATION_REASON.databaseName())
                    .varChar()
                    .map(EndDeviceControlsDomainExtension.FieldNames.CANCELLATION_REASON.javaName())
                    .add();
        }

        @Override
        public String application() {
            return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
        }
    }
}
