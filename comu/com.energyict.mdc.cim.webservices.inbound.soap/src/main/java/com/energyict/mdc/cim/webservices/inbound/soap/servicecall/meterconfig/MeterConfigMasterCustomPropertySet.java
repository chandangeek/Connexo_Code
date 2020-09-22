/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig;

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
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.cim.webservices.inbound.soap.MeterConfigChecklist;
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

@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.MeterConfigMasterCustomPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + MeterConfigMasterCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class MeterConfigMasterCustomPropertySet implements CustomPropertySet<ServiceCall, MeterConfigMasterDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_NAME = "MeterConfigMasterCustomPropertySet";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public MeterConfigMasterCustomPropertySet() {
        // for OSGi
    }

    @Inject
    public MeterConfigMasterCustomPropertySet(PropertySpecService propertySpecService,
                                              CustomPropertySetService customPropertySetService,
                                              Thesaurus thesaurus,
                                              ServiceCallService serviceCallService) {
        this();
        this.setPropertySpecService(propertySpecService);
        setServiceCallService(serviceCallService);
        this.thesaurus = thesaurus;
        customPropertySetService.addCustomPropertySet(this);
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
        return MeterConfigMasterCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<ServiceCall, MeterConfigMasterDomainExtension> getPersistenceSupport() {
        return new MeterConfigMasterCustomPropertyPersistenceSupport();
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
                        .named(MeterConfigMasterDomainExtension.FieldNames.CALLBACK_URL.javaName(), TranslationKeys.CALL_BACK_URL)
                        .describedAs(TranslationKeys.CALL_BACK_URL)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterConfigMasterDomainExtension.FieldNames.METER_STATUS_SOURCE.javaName(), TranslationKeys.METER_STATUS_SOURCE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .booleanSpec()
                        .named(MeterConfigMasterDomainExtension.FieldNames.PING.javaName(), TranslationKeys.PING)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterConfigMasterDomainExtension.FieldNames.CORRELATION_ID.javaName(), TranslationKeys.CORRELATION_ID)
                        .describedAs(TranslationKeys.CORRELATION_ID)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class MeterConfigMasterCustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, MeterConfigMasterDomainExtension> {
        private final String TABLE_NAME = "MCP_SCS_CNT";
        private final String FK = "FK_MCP_SCS_CNT";

        @Override
        public String componentName() {
            return "PKT";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return MeterConfigMasterDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<MeterConfigMasterDomainExtension> persistenceClass() {
            return MeterConfigMasterDomainExtension.class;
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
            table.column(MeterConfigMasterDomainExtension.FieldNames.CALLS_SUCCESS.databaseName())
                    .number()
                    .map(MeterConfigMasterDomainExtension.FieldNames.CALLS_SUCCESS.javaName())
                    .notNull()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .upTo(Version.version(10, 9))
                    .add();
            table.column(MeterConfigMasterDomainExtension.FieldNames.CALLS_FAILED.databaseName())
                    .number()
                    .map(MeterConfigMasterDomainExtension.FieldNames.CALLS_FAILED.javaName())
                    .notNull()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .upTo(Version.version(10, 9))
                    .add();
            table.column(MeterConfigMasterDomainExtension.FieldNames.CALLS_EXPECTED.databaseName())
                    .number()
                    .map(MeterConfigMasterDomainExtension.FieldNames.CALLS_EXPECTED.javaName())
                    .notNull()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .upTo(Version.version(10, 9))
                    .add();
            Column oldColumn = table.column(MeterConfigMasterDomainExtension.FieldNames.CALLBACK_URL.databaseName())
                    .varChar()
                    .map(MeterConfigMasterDomainExtension.FieldNames.CALLBACK_URL.javaName())
                    .notNull()
                    .upTo(Version.version(10, 6))
                    .add();
            table.column(MeterConfigMasterDomainExtension.FieldNames.CALLBACK_URL.databaseName())
                    .varChar()
                    .map(MeterConfigMasterDomainExtension.FieldNames.CALLBACK_URL.javaName())
                    .since(Version.version(10, 6))
                    .previously(oldColumn)
                    .add();
            table.column(MeterConfigMasterDomainExtension.FieldNames.METER_STATUS_SOURCE.databaseName())
                    .varChar()
                    .map(MeterConfigMasterDomainExtension.FieldNames.METER_STATUS_SOURCE.javaName())
                    .since(Version.version(10, 9))
                    .add();
            table.column(MeterConfigMasterDomainExtension.FieldNames.PING.databaseName())
                    .bool()
                    .map(MeterConfigMasterDomainExtension.FieldNames.PING.javaName())
                    .since(Version.version(10, 9))
                    .installValue("'N'")
                    .add();
            table.column(MeterConfigMasterDomainExtension.FieldNames.CORRELATION_ID.databaseName())
                    .varChar()
                    .since(Version.version(10, 7))
                    .map(MeterConfigMasterDomainExtension.FieldNames.CORRELATION_ID.javaName())
                    .notNull(false)
                    .add();
        }

        @Override
        public String application() {
            return MeterConfigChecklist.APPLICATION_NAME;
        }
    }
}
