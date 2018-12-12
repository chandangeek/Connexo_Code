/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.servicecall;

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
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.device.data.DeviceDataServices;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author sva
 * @since 30/03/2016 - 15:43
 */
@Component(name = "com.energyict.servicecall.ami.CommandCustomPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + CommandCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class CommandCustomPropertySet implements CustomPropertySet<ServiceCall, CommandServiceCallDomainExtension> {

    public static final String CUSTOM_PROPERTY_SET_NAME = "CommandCustomPropertySet";

    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;

    public CommandCustomPropertySet() {
    }

    @Inject
    public CommandCustomPropertySet(PropertySpecService propertySpecService, CustomPropertySetService customPropertySetService, Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
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

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Activate
    public void activate() {
    }

    @Override
    public String getName() {
        return CommandCustomPropertySet.class.getSimpleName();
    }

    @Override
    public Class<ServiceCall> getDomainClass() {
        return ServiceCall.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(CustomPropertySetsTranslationKeys.DOMAIN_NAME).format();
    }

    @Override
    public PersistenceSupport<ServiceCall, CommandServiceCallDomainExtension> getPersistenceSupport() {
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
        return Arrays.asList(
                this.propertySpecService
                        .stringSpec()
                        .named(CommandServiceCallDomainExtension.FieldNames.RELEASE_DATE.javaName(), CustomPropertySetsTranslationKeys.RELEASE_DATE)
                        .describedAs(CustomPropertySetsTranslationKeys.RELEASE_DATE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(CommandServiceCallDomainExtension.FieldNames.DEVICE_MSG.javaName(), CustomPropertySetsTranslationKeys.DEVICE_MSG)
                        .describedAs(CustomPropertySetsTranslationKeys.DEVICE_MSG)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .longSpec()
                        .named(CommandServiceCallDomainExtension.FieldNames.NR_OF_UNCONFIRMED_DEVICE_COMMANDS.javaName(), CustomPropertySetsTranslationKeys.NR_OF_UNCONFIRMED_DEVICE_COMMANDS)
                        .describedAs(CustomPropertySetsTranslationKeys.NR_OF_UNCONFIRMED_DEVICE_COMMANDS)
                        .fromThesaurus(thesaurus)
                        .setDefaultValue(0L)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(CommandServiceCallDomainExtension.FieldNames.STATUS.javaName(), CustomPropertySetsTranslationKeys.STATUS)
                        .describedAs(CustomPropertySetsTranslationKeys.STATUS)
                        .fromThesaurus(thesaurus)
                        .finish());
    }

    private class MyPersistenceSupport implements PersistenceSupport<ServiceCall, CommandServiceCallDomainExtension> {
        private final String TABLE_NAME = "DDC_SCS_COMMAND";
        private final String FK = "FK_DDC_SCS_COMMAND";

        @Override
        public String componentName() {
            return "CCP";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return CommandServiceCallDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<CommandServiceCallDomainExtension> persistenceClass() {
            return CommandServiceCallDomainExtension.class;
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
                    .column(CommandServiceCallDomainExtension.FieldNames.RELEASE_DATE.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(CommandServiceCallDomainExtension.FieldNames.RELEASE_DATE.javaName())
                    .notNull()
                    .add();
            table
                    .column(CommandServiceCallDomainExtension.FieldNames.DEVICE_MSG.databaseName())
                    .varChar()
                    .map(CommandServiceCallDomainExtension.FieldNames.DEVICE_MSG.javaName())
                    .add();
            table
                    .column(CommandServiceCallDomainExtension.FieldNames.NR_OF_UNCONFIRMED_DEVICE_COMMANDS.databaseName())
                    .number()
                    .conversion(ColumnConversion.NUMBER2INT)
                    .map(CommandServiceCallDomainExtension.FieldNames.NR_OF_UNCONFIRMED_DEVICE_COMMANDS.javaName())
                    .notNull()
                    .add();
            table
                    .column(CommandServiceCallDomainExtension.FieldNames.STATUS.databaseName())
                    .varChar()
                    .map(CommandServiceCallDomainExtension.FieldNames.STATUS.javaName())
                    .notNull()
                    .add();
        }

        @Override
        public String application() {
            return "MultiSense";
        }
    }
}