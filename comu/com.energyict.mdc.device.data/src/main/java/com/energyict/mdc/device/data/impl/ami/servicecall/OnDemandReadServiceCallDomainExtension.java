/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami.servicecall;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Optional;

public class OnDemandReadServiceCallDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {

    private Reference<ServiceCall> serviceCall = Reference.empty();
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private BigDecimal expectedTasks;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private BigDecimal successfulTasks;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private BigDecimal completedTasks;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private BigDecimal triggerDate;

    public OnDemandReadServiceCallDomainExtension() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    public Reference<ServiceCall> getServiceCall() {
        return serviceCall;
    }

    public BigDecimal getExpectedTasks() {
        return expectedTasks;
    }

    public void setExpectedTasks(BigDecimal expectedTasks) {
        this.expectedTasks = expectedTasks;
    }

    public BigDecimal getSuccessfulTasks() {
        return successfulTasks;
    }

    public void setSuccessfulTasks(BigDecimal successfulTasks) {
        this.successfulTasks = successfulTasks;
    }

    public BigDecimal getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(BigDecimal completedTasks) {
        this.completedTasks = completedTasks;
    }

    public BigDecimal getTriggerDate() {
        return triggerDate;
    }

    public void setTriggerDate(BigDecimal triggerDate) {
        this.triggerDate = triggerDate;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setExpectedTasks(new BigDecimal(Optional.ofNullable(propertyValues.getProperty(FieldNames.EXPECTED_TASKS.javaName()))
                .orElse(BigDecimal.ZERO)
                .toString()));
        this.setSuccessfulTasks(new BigDecimal(Optional.ofNullable(propertyValues.getProperty(FieldNames.SUCCESSFUL_TASKS
                .javaName())).orElse(BigDecimal.ZERO).toString()));
        this.setCompletedTasks(new BigDecimal(Optional.ofNullable(propertyValues.getProperty(FieldNames.COMPLETED_TASKS.javaName()))
                .orElse(BigDecimal.ZERO)
                .toString()));
        this.setTriggerDate(new BigDecimal(Optional.ofNullable(propertyValues.getProperty(FieldNames.TRIGGERDATE.javaName()))
                .orElse(BigDecimal.ZERO)
                .toString()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.EXPECTED_TASKS.javaName(), this.getExpectedTasks());
        propertySetValues.setProperty(FieldNames.SUCCESSFUL_TASKS.javaName(), this.getSuccessfulTasks());
        propertySetValues.setProperty(FieldNames.COMPLETED_TASKS.javaName(), this.getCompletedTasks());
        propertySetValues.setProperty(FieldNames.TRIGGERDATE.javaName(), this.getTriggerDate());
    }

    @Override
    public void validateDelete() {
    }

    public enum FieldNames {
        DOMAIN("serviceCall", "service_call"),
        EXPECTED_TASKS("expectedTasks", "expected_tasks"),
        SUCCESSFUL_TASKS("successfulTasks", "succesful_tasks"),
        COMPLETED_TASKS("completedTasks", "completed_tasks"),
        TRIGGERDATE("triggerDate", "trigger_date");

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
}