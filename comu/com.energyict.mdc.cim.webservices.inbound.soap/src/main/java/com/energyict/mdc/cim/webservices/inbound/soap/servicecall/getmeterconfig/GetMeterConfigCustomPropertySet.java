/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterconfig;

import com.elster.jupiter.cps.*;
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
import com.energyict.mdc.cim.webservices.inbound.soap.MeterConfigChecklist;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.TranslationKeys;
import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getenddeviceevents.GetEndDeviceEventsDomainExtension;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.*;

@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.GetMeterConfigCustomPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + GetMeterConfigCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class GetMeterConfigCustomPropertySet implements CustomPropertySet<ServiceCall, GetMeterConfigDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_NAME = "GetMeterConfigCustomPropertySet";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public GetMeterConfigCustomPropertySet() {
    }

    @Inject
    public GetMeterConfigCustomPropertySet(PropertySpecService propertySpecService, CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
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
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        customPropertySetService.addCustomPropertySet(this);
    }

    @Reference
    @SuppressWarnings("unused") // For OSGi framework
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(InboundSoapEndpointsActivator.COMPONENT_NAME, Layer.SOAP);
    }

    @Override
    public String getName() {
        return GetMeterConfigCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<ServiceCall, GetMeterConfigDomainExtension> getPersistenceSupport() {
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
                        .named(GetMeterConfigDomainExtension.FieldNames.METER_MRID.javaName(), TranslationKeys.METER_CONFIG)
                        .describedAs(TranslationKeys.METER_CONFIG)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(GetMeterConfigDomainExtension.FieldNames.METER_NAME.javaName(), TranslationKeys.METER_CONFIG)
                        .describedAs(TranslationKeys.METER_CONFIG)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(GetMeterConfigDomainExtension.FieldNames.ERROR_CODE.javaName(), TranslationKeys.ERROR_CODE)
                        .describedAs(TranslationKeys.ERROR_CODE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(GetMeterConfigDomainExtension.FieldNames.ERROR_MESSAGE.javaName(), TranslationKeys.ERROR_MESSAGE)
                        .describedAs(TranslationKeys.ERROR_MESSAGE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(GetMeterConfigDomainExtension.FieldNames.FROM_DATE.javaName(), TranslationKeys.FROM_DATE)
                        .describedAs(TranslationKeys.FROM_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(GetMeterConfigDomainExtension.FieldNames.TO_DATE.javaName(), TranslationKeys.TO_DATE)
                        .describedAs(TranslationKeys.TO_DATE)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, GetMeterConfigDomainExtension> {
        private final String TABLE_NAME = "MCG_SCS_CNT";
        private final String FK = "FK_MCG_SCS_CNT";

        @Override
        public String componentName() {
            return "PKG";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return GetMeterConfigDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<GetMeterConfigDomainExtension> persistenceClass() {
            return GetMeterConfigDomainExtension.class;
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
            table.column(GetMeterConfigDomainExtension.FieldNames.METER_MRID.databaseName())
                    .varChar()
                    .map(GetMeterConfigDomainExtension.FieldNames.METER_MRID.javaName())
                    .add();
            table.column(GetMeterConfigDomainExtension.FieldNames.METER_NAME.databaseName())
                    .varChar()
                    .map(GetMeterConfigDomainExtension.FieldNames.METER_NAME.javaName())
                    .add();
            table.column(GetMeterConfigDomainExtension.FieldNames.ERROR_CODE.databaseName())
                    .varChar()
                    .map(GetMeterConfigDomainExtension.FieldNames.ERROR_CODE.javaName())
                    .add();
            table.column(GetMeterConfigDomainExtension.FieldNames.ERROR_MESSAGE.databaseName())
                    .varChar()
                    .map(GetMeterConfigDomainExtension.FieldNames.ERROR_MESSAGE.javaName())
                    .add();
            table.column(GetMeterConfigDomainExtension.FieldNames.FROM_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(GetMeterConfigDomainExtension.FieldNames.FROM_DATE.javaName())
                    .add();
            table.column(GetEndDeviceEventsDomainExtension.FieldNames.TO_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(GetMeterConfigDomainExtension.FieldNames.TO_DATE.javaName())
                    .add();
        }

        @Override
        public String application() {
            return MeterConfigChecklist.APPLICATION_NAME;
        }
    }
}
