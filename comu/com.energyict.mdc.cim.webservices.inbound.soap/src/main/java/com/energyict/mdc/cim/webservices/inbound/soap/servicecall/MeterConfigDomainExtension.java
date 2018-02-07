/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Optional;

public class MeterConfigDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {
    public enum FieldNames {
        DOMAIN("serviceCall", "serviceCall"),
        METER("meter", "meter"),
        PARENT_SERVICE_CALL("parentServiceCallId", "parentServiceCallId"),
        ERROR_MESSAGE("errorMessage", "errorMessage"),
        OPERATION("operation", "operation");

        FieldNames(String javaName, String databaseName) {
            this.javaName = javaName;
            this.databaseName = databaseName;
        }

        private final String javaName;
        private final String databaseName;

        public String javaName() {
            return javaName;
        }

        public String databaseName() {
            return databaseName;
        }
    }

    private Reference<ServiceCall> serviceCall = Reference.empty();

    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String meter;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private BigDecimal parentServiceCallId;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String errorMessage;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String operation;

    public MeterConfigDomainExtension() {
        super();
    }

    public String getMeter() {
        return meter;
    }

    public void setMeter(String meter) {
        this.meter = meter;
    }

    public BigDecimal getParentServiceCallId() {
        return parentServiceCallId;
    }

    public void setParentServiceCallId(BigDecimal parentServiceCallId) {
        this.parentServiceCallId = parentServiceCallId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setMeter((String) propertyValues.getProperty(FieldNames.METER.javaName()));
        this.setParentServiceCallId(new BigDecimal(Optional.ofNullable(propertyValues.getProperty(FieldNames.PARENT_SERVICE_CALL.javaName()))
                .orElse(BigDecimal.ZERO).toString()));
        this.setErrorMessage((String) propertyValues.getProperty(FieldNames.ERROR_MESSAGE.javaName()));
        this.setOperation((String) propertyValues.getProperty(FieldNames.OPERATION.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.METER.javaName(), this.getMeter());
        propertySetValues.setProperty(FieldNames.PARENT_SERVICE_CALL.javaName(), this.getParentServiceCallId());
        propertySetValues.setProperty(FieldNames.ERROR_MESSAGE.javaName(), this.getErrorMessage());
        propertySetValues.setProperty(FieldNames.OPERATION.javaName(), this.getOperation());
    }

    @Override
    public void validateDelete() {
    }
}
