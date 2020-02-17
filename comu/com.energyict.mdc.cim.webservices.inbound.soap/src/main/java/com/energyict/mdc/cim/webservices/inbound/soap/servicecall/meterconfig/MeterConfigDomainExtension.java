/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig;

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
        METER_MRID("meterMrid", "METER_MRID"),
        METER_NAME("meterName", "METER_NAME"),
        PARENT_SERVICE_CALL("parentServiceCallId", "parentServiceCallId"),
        ERROR_MESSAGE("errorMessage", "errorMessage"),
        ERROR_CODE("errorCode", "errorCode"),
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

    private String meter;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String meterMrid;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String meterName;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private BigDecimal parentServiceCallId;
    private String errorMessage;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String errorCode;
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

    public String getMeterMrid() {
        return meterMrid;
    }

    public void setMeterMrid(String meterMrid) {
        this.meterMrid = meterMrid;
    }

    public String getMeterName() {
        return meterName;
    }

    public void setMeterName(String meterName) {
        this.meterName = meterName;
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

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
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
        this.setMeterMrid((String) propertyValues.getProperty(FieldNames.METER_MRID.javaName()));
        this.setMeterName((String) propertyValues.getProperty(FieldNames.METER_NAME.javaName()));
        this.setParentServiceCallId(new BigDecimal(Optional.ofNullable(propertyValues.getProperty(FieldNames.PARENT_SERVICE_CALL.javaName()))
                .orElse(BigDecimal.ZERO).toString()));
        this.setErrorMessage((String) propertyValues.getProperty(FieldNames.ERROR_MESSAGE.javaName()));
        this.setErrorCode((String) propertyValues.getProperty(FieldNames.ERROR_CODE.javaName()));
        this.setOperation((String) propertyValues.getProperty(FieldNames.OPERATION.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.METER.javaName(), this.getMeter());
        propertySetValues.setProperty(FieldNames.METER_MRID.javaName(), this.getMeterMrid());
        propertySetValues.setProperty(FieldNames.METER_NAME.javaName(), this.getMeterName());
        propertySetValues.setProperty(FieldNames.PARENT_SERVICE_CALL.javaName(), this.getParentServiceCallId());
        propertySetValues.setProperty(FieldNames.ERROR_MESSAGE.javaName(), this.getErrorMessage());
        propertySetValues.setProperty(FieldNames.ERROR_CODE.javaName(), this.getErrorCode());
        propertySetValues.setProperty(FieldNames.OPERATION.javaName(), this.getOperation());
    }

    @Override
    public void validateDelete() {
    }
}
