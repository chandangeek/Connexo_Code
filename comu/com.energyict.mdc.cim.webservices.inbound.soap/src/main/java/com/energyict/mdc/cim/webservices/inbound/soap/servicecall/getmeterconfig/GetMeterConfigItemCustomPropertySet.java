/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterconfig;

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

@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.GetMeterConfigItemCustomPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + GetMeterConfigItemCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class GetMeterConfigItemCustomPropertySet implements CustomPropertySet<ServiceCall, GetMeterConfigItemDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_NAME = "GetMeterConfigItemCustomPropertySet";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public GetMeterConfigItemCustomPropertySet() {
        // for test purposes
    }

    @Inject
    public GetMeterConfigItemCustomPropertySet(PropertySpecService propertySpecService, CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
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
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(InboundSoapEndpointsActivator.COMPONENT_NAME, Layer.SOAP);
    }

    @Override
    public String getName() {
        return GetMeterConfigItemCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<ServiceCall, GetMeterConfigItemDomainExtension> getPersistenceSupport() {
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
                        .named(GetMeterConfigItemDomainExtension.FieldNames.METER_MRID.javaName(), TranslationKeys.METER_MRID)
                        .describedAs(TranslationKeys.METER_MRID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(GetMeterConfigItemDomainExtension.FieldNames.METER_NAME.javaName(), TranslationKeys.METER_NAME)
                        .describedAs(TranslationKeys.METER_NAME)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(GetMeterConfigItemDomainExtension.FieldNames.ERROR_CODE.javaName(), TranslationKeys.ERROR_CODE)
                        .describedAs(TranslationKeys.ERROR_CODE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(GetMeterConfigItemDomainExtension.FieldNames.ERROR_MESSAGE.javaName(), TranslationKeys.ERROR_MESSAGE)
                        .describedAs(TranslationKeys.ERROR_MESSAGE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(GetMeterConfigItemDomainExtension.FieldNames.FROM_DATE.javaName(), TranslationKeys.FROM_DATE)
                        .describedAs(TranslationKeys.FROM_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .specForValuesOf(new InstantFactory())
                        .named(GetMeterConfigItemDomainExtension.FieldNames.TO_DATE.javaName(), TranslationKeys.TO_DATE)
                        .describedAs(TranslationKeys.TO_DATE)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class CustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, GetMeterConfigItemDomainExtension> {
        private final String TABLE_NAME = "MCI_WS_METER_SC_CPS";
        private final String FK = "FK_MCI_WS_METER_SC_CPS_SC";

        @Override
        public String componentName() {
            return "MCI";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return GetMeterConfigItemDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<GetMeterConfigItemDomainExtension> persistenceClass() {
            return GetMeterConfigItemDomainExtension.class;
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
            table.column(GetMeterConfigItemDomainExtension.FieldNames.METER_MRID.databaseName())
                    .varChar()
                    .map(GetMeterConfigItemDomainExtension.FieldNames.METER_MRID.javaName())
                    .add();
            table.column(GetMeterConfigItemDomainExtension.FieldNames.METER_NAME.databaseName())
                    .varChar()
                    .map(GetMeterConfigItemDomainExtension.FieldNames.METER_NAME.javaName())
                    .add();
            table.column(GetMeterConfigItemDomainExtension.FieldNames.ERROR_CODE.databaseName())
                    .varChar()
                    .map(GetMeterConfigItemDomainExtension.FieldNames.ERROR_CODE.javaName())
                    .add();
            table.column(GetMeterConfigItemDomainExtension.FieldNames.ERROR_MESSAGE.databaseName())
                    .varChar()
                    .map(GetMeterConfigItemDomainExtension.FieldNames.ERROR_MESSAGE.javaName())
                    .add();
            table.column(GetMeterConfigItemDomainExtension.FieldNames.FROM_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(GetMeterConfigItemDomainExtension.FieldNames.FROM_DATE.javaName())
                    .add();
            table.column(GetMeterConfigItemDomainExtension.FieldNames.TO_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(GetMeterConfigItemDomainExtension.FieldNames.TO_DATE.javaName())
                    .add();
        }

        @Override
        public String application() {
            return MeterConfigChecklist.APPLICATION_NAME;
        }
    }
}
