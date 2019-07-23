/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

public class ChildGetMeterReadingsDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {
        DOMAIN("serviceCall", "SERVICE_CALL"),
        COMMUNICATION_TASK("communicationTask", "COMMUNICATION_TASK"),
        TRIGGER_DATE("triggerDate", "TRIGGER_DATE"),
        ACTUAL_START_DATE("actualStartDate", "ACTUAL_START_DATE"),
        ACTUAL_END_DATE("actualEndDate", "ACTUAL_END_DATE")
        ;

        private final String javaName;
        private final String databaseName;

        FieldNames(String javaName, String databaseName) {
            this.javaName = javaName;
            this.databaseName = databaseName;
        }

        public String javaName() {
            return javaName;
        }

        public String databaseName() {
            return databaseName;
        }
    }

    private Reference<ServiceCall> serviceCall = Reference.empty();

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String communicationTask;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private Instant triggerDate;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private Instant actualStartDate;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private Instant actualEndDate;

    public ChildGetMeterReadingsDomainExtension() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    public Reference<ServiceCall> getServiceCall() {
        return serviceCall;
    }


    public String getCommunicationTask() {
        return communicationTask;
    }

    public void setCommunicationTask(String communicationTask) {
        this.communicationTask = communicationTask;
    }

    public Instant getTriggerDate() {
        return triggerDate;
    }

    public void setTriggerDate(Instant triggerDate) {
        this.triggerDate = triggerDate;
    }

    public Instant getActualStartDate() {
        return actualStartDate;
    }

    public void setActualStartDate(Instant actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    public Instant getActualEndDate() {
        return actualEndDate;
    }

    public void setActualEndDate(Instant actualEndDate) {
        this.actualEndDate = actualEndDate;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setCommunicationTask((String) propertyValues.getProperty(FieldNames.COMMUNICATION_TASK.javaName()));
        this.setTriggerDate((Instant) propertyValues.getProperty(FieldNames.TRIGGER_DATE.javaName()));
        this.setActualStartDate((Instant) propertyValues.getProperty(FieldNames.ACTUAL_START_DATE.javaName()));
        this.setActualEndDate((Instant) propertyValues.getProperty(FieldNames.ACTUAL_END_DATE.javaName()));

    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.COMMUNICATION_TASK.javaName(), this.getCommunicationTask());
        propertySetValues.setProperty(FieldNames.TRIGGER_DATE.javaName(), this.getTriggerDate());
        propertySetValues.setProperty(FieldNames.ACTUAL_START_DATE.javaName(), this.getActualStartDate());
        propertySetValues.setProperty(FieldNames.ACTUAL_END_DATE.javaName(), this.getActualEndDate());
    }

    @Override
    public void validateDelete() {
        // do nothing
    }
}