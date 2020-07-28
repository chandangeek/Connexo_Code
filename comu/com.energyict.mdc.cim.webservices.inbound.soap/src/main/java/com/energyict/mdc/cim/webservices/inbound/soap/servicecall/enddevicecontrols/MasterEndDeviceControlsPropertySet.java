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

@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.MasterEndDeviceControlsPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + MasterEndDeviceControlsPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class MasterEndDeviceControlsPropertySet implements CustomPropertySet<ServiceCall, MasterEndDeviceControlsDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_NAME = "MasterEndDeviceControlsPropertySet";
    public static final String MODEL_NAME = "DC1";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    // For OSGi framework
    public MasterEndDeviceControlsPropertySet() {
    }

    @Inject
    public MasterEndDeviceControlsPropertySet(PropertySpecService propertySpecService, Thesaurus thesaurus) {
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
        return thesaurus.getFormat(TranslationKeys.MEDC_CPS).format();
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
    public PersistenceSupport<ServiceCall, MasterEndDeviceControlsDomainExtension> getPersistenceSupport() {
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
                        .named(MasterEndDeviceControlsDomainExtension.FieldNames.CALLBACK_URL.javaName(), TranslationKeys.CALL_BACK_URL)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MasterEndDeviceControlsDomainExtension.FieldNames.CORRELATION_ID.javaName(), TranslationKeys.CORRELATION_ID)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish(),
                this.propertySpecService
                        .longSpec()
                        .named(MasterEndDeviceControlsDomainExtension.FieldNames.MAX_EXEC_TIME.javaName(), TranslationKeys.MAX_EXEC_TIME)
                        .fromThesaurus(thesaurus)
                        .markRequired()
                        .finish()
        );
    }

    private static class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, MasterEndDeviceControlsDomainExtension> {
        private static final String TABLE_NAME = "DC1_EDC_MASTER_CPS";
        private static final String FK = "FK_DC1_EDC_MASTER_CPS";
        private final String IDX = "IDX_DC1_EDC_MASTER_CPS_CORID";

        @Override
        public String componentName() {
            return MODEL_NAME;
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return MasterEndDeviceControlsDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<MasterEndDeviceControlsDomainExtension> persistenceClass() {
            return MasterEndDeviceControlsDomainExtension.class;
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
            table.column(MasterEndDeviceControlsDomainExtension.FieldNames.CALLBACK_URL.databaseName())
                    .varChar()
                    .map(MasterEndDeviceControlsDomainExtension.FieldNames.CALLBACK_URL.javaName())
                    .notNull()
                    .add();
            Column correlationIdColumnString = table.column(MasterEndDeviceControlsDomainExtension.FieldNames.CORRELATION_ID.databaseName())
                    .varChar()
                    .map(MasterEndDeviceControlsDomainExtension.FieldNames.CORRELATION_ID.javaName())
                    .notNull()
                    .add();
            table.index(IDX).on(correlationIdColumnString).add();
            table.column(MasterEndDeviceControlsDomainExtension.FieldNames.MAX_EXEC_TIME.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .map(MasterEndDeviceControlsDomainExtension.FieldNames.MAX_EXEC_TIME.javaName())
                    .notNull()
                    .add();
        }

        @Override
        public String application() {
            return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
        }
    }
}
