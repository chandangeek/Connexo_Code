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
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.cim.webservices.inbound.soap.MeterConfigChecklist;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.InboundSoapEndpointsActivator;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.TranslationKeys;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import com.google.inject.Module;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.elster.jupiter.orm.ColumnConversion.CLOB2STRING;
import static com.elster.jupiter.orm.Version.version;

@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.MeterConfigCustomPropertySet",
        service = CustomPropertySet.class,
        property = "name=" + MeterConfigCustomPropertySet.CUSTOM_PROPERTY_SET_NAME,
        immediate = true)
public class MeterConfigCustomPropertySet implements CustomPropertySet<ServiceCall, MeterConfigDomainExtension> {
    public static final String CUSTOM_PROPERTY_SET_NAME = "MeterConfigCustomPropertySet";

    private volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    public MeterConfigCustomPropertySet() {
        // for OSGi
    }

    @Inject
    public MeterConfigCustomPropertySet(PropertySpecService propertySpecService,
                                        CustomPropertySetService customPropertySetService,
                                        Thesaurus thesaurus,
                                        ServiceCallService serviceCallService,
                                        TaskService taskService) {
        this.thesaurus = thesaurus;
        setPropertySpecService(propertySpecService);
        setServiceCallService(serviceCallService);
        setTaskService(taskService);
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
    public void setTaskService(TaskService taskService) {
        // to make sure ComTasks are available
    }

    @Reference
    @SuppressWarnings("unused") // For OSGi framework
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(InboundSoapEndpointsActivator.COMPONENT_NAME, Layer.SOAP);
    }

    @Override
    public String getName() {
        return MeterConfigCustomPropertySet.class.getSimpleName();
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
    public PersistenceSupport<ServiceCall, MeterConfigDomainExtension> getPersistenceSupport() {
        return new MeterConfigCustomPropertyPersistenceSupport();
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
                        .named(MeterConfigDomainExtension.FieldNames.METER.javaName(), TranslationKeys.METER_CONFIG)
                        .describedAs(TranslationKeys.METER_CONFIG)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterConfigDomainExtension.FieldNames.METER_MRID.javaName(), TranslationKeys.METER_MRID)
                        .describedAs(TranslationKeys.METER_MRID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterConfigDomainExtension.FieldNames.METER_SERIAL_NUMBER.javaName(), TranslationKeys.METER_MRID)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterConfigDomainExtension.FieldNames.METER_NAME.javaName(), TranslationKeys.METER_NAME)
                        .describedAs(TranslationKeys.METER_NAME)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .referenceSpec(ComTask.class)
                        .named(MeterConfigDomainExtension.FieldNames.COMMUNICATION_TASK.javaName(), TranslationKeys.COMMUNICATION_TASK)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .bigDecimalSpec()
                        .named(MeterConfigDomainExtension.FieldNames.PARENT_SERVICE_CALL.javaName(), TranslationKeys.PARENT_SERVICE_CALL)
                        .describedAs(TranslationKeys.PARENT_SERVICE_CALL)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterConfigDomainExtension.FieldNames.ERROR_MESSAGE.javaName(), TranslationKeys.ERROR_MESSAGE)
                        .describedAs(TranslationKeys.ERROR_MESSAGE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterConfigDomainExtension.FieldNames.ERROR_CODE.javaName(), TranslationKeys.ERROR_CODE)
                        .describedAs(TranslationKeys.ERROR_CODE)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterConfigDomainExtension.FieldNames.OPERATION.javaName(), TranslationKeys.OPERATION)
                        .describedAs(TranslationKeys.OPERATION)
                        .fromThesaurus(thesaurus)
                        .finish(),
                this.propertySpecService
                        .stringSpec()
                        .named(MeterConfigDomainExtension.FieldNames.PING_RESULT.javaName(), TranslationKeys.PING_RESULT)
                        .fromThesaurus(thesaurus)
                        .finish()
        );
    }

    private class MeterConfigCustomPropertyPersistenceSupport implements PersistenceSupport<ServiceCall, MeterConfigDomainExtension> {
        private final String TABLE_NAME = "MCM_SCS_CNT";
        private final String FK = "FK_MCM_SCS_CNT";

        @Override
        public String componentName() {
            return "PKM";
        }

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public String domainFieldName() {
            return MeterConfigDomainExtension.FieldNames.DOMAIN.javaName();
        }

        @Override
        public String domainForeignKeyName() {
            return FK;
        }

        @Override
        public Class<MeterConfigDomainExtension> persistenceClass() {
            return MeterConfigDomainExtension.class;
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
            table.column(MeterConfigDomainExtension.FieldNames.METER.databaseName())
                    .type("CLOB")
                    .conversion(CLOB2STRING)
                    .map(MeterConfigDomainExtension.FieldNames.METER.javaName())
                    .add();
            table.column(MeterConfigDomainExtension.FieldNames.METER_MRID.databaseName())
                    .varChar()
                    .map(MeterConfigDomainExtension.FieldNames.METER_MRID.javaName())
                    .add();
            table.column(MeterConfigDomainExtension.FieldNames.METER_SERIAL_NUMBER.databaseName())
                    .varChar()
                    .map(MeterConfigDomainExtension.FieldNames.METER_SERIAL_NUMBER.javaName())
                    .since(version(10, 9, 23))
                    .add();
            table.column(MeterConfigDomainExtension.FieldNames.METER_NAME.databaseName())
                    .varChar()
                    .map(MeterConfigDomainExtension.FieldNames.METER_NAME.javaName())
                    .add();
            Column comTask = table.column(MeterConfigDomainExtension.FieldNames.COMMUNICATION_TASK.databaseName())
                    .number()
                    .since(Version.version(10, 9))
                    .add();
            table.foreignKey(FK + "_COMTASK")
                    .on(comTask)
                    .references(ComTask.class)
                    .map(MeterConfigDomainExtension.FieldNames.COMMUNICATION_TASK.javaName())
                    .since(Version.version(10, 9))
                    .add();
            table.column(MeterConfigDomainExtension.FieldNames.PARENT_SERVICE_CALL.databaseName())
                    .number()
                    .map(MeterConfigDomainExtension.FieldNames.PARENT_SERVICE_CALL.javaName())
                    .notNull()
                    .add();
            table.column(MeterConfigDomainExtension.FieldNames.ERROR_MESSAGE.databaseName())
                    .type("CLOB")
                    .conversion(CLOB2STRING)
                    .map(MeterConfigDomainExtension.FieldNames.ERROR_MESSAGE.javaName())
                    .add();
            table.column(MeterConfigDomainExtension.FieldNames.ERROR_CODE.databaseName())
                    .varChar()
                    .map(MeterConfigDomainExtension.FieldNames.ERROR_CODE.javaName())
                    .add();
            table.column(MeterConfigDomainExtension.FieldNames.OPERATION.databaseName())
                    .varChar()
                    .map(MeterConfigDomainExtension.FieldNames.OPERATION.javaName())
                    .notNull()
                    .add();
            table.column(MeterConfigDomainExtension.FieldNames.PING_RESULT.databaseName())
                    .varChar()
                    .map(MeterConfigDomainExtension.FieldNames.PING_RESULT.javaName())
                    .since(Version.version(10, 9))
                    .add();
        }

        @Override
        public String application() {
            return MeterConfigChecklist.APPLICATION_NAME;
        }
    }
}
