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

@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.SubMasterEndDeviceControlsPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + SubMasterEndDeviceControlsPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class SubMasterEndDeviceControlsPropertySet implements CustomPropertySet<ServiceCall, SubMasterEndDeviceControlsDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_NAME = "SubMasterEndDeviceControlsPropertySet";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    // For OSGi framework
    public SubMasterEndDeviceControlsPropertySet() {
    }

    @Inject
    public SubMasterEndDeviceControlsPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
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
        return thesaurus.getFormat(TranslationKeys.SEDC_CPS).format();
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
    public PersistenceSupport<ServiceCall, SubMasterEndDeviceControlsDomainExtension> getPersistenceSupport() {
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
                        .specForValuesOf(new InstantFactory())
                        .named(SubMasterEndDeviceControlsDomainExtension.FieldNames.TRIGGER_DATE.javaName(), TranslationKeys.TRIGGER_DATE)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(SubMasterEndDeviceControlsDomainExtension.FieldNames.COMMAND_CODE.javaName(), TranslationKeys.COMMAND_CODE)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(SubMasterEndDeviceControlsDomainExtension.FieldNames.COMMAND_ATTRIBUTES.javaName(), TranslationKeys.COMMAND_ATTRIBUTES)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private static class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, SubMasterEndDeviceControlsDomainExtension> {
        private static final String TABLE_NAME = "DC2_EDC_SUB_CPS";
        private static final String FK = "FK_DC2_EDC_SUB_CPS";

        @Override
        public String componentName() {
            return "DC2";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return SubMasterEndDeviceControlsDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<SubMasterEndDeviceControlsDomainExtension> persistenceClass() {
            return SubMasterEndDeviceControlsDomainExtension.class;
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
            table.column(SubMasterEndDeviceControlsDomainExtension.FieldNames.TRIGGER_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(SubMasterEndDeviceControlsDomainExtension.FieldNames.TRIGGER_DATE.javaName())
                    .notNull()
                    .add();
            table.column(SubMasterEndDeviceControlsDomainExtension.FieldNames.COMMAND_CODE.databaseName())
                    .varChar()
                    .map(SubMasterEndDeviceControlsDomainExtension.FieldNames.COMMAND_CODE.javaName())
                    .notNull()
                    .add();
            table.column(SubMasterEndDeviceControlsDomainExtension.FieldNames.COMMAND_ATTRIBUTES.databaseName())
                    .varChar()
                    .map(SubMasterEndDeviceControlsDomainExtension.FieldNames.COMMAND_ATTRIBUTES.javaName())
                    .add();
        }

        @Override
        public String application() {
            return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
        }
    }
}
