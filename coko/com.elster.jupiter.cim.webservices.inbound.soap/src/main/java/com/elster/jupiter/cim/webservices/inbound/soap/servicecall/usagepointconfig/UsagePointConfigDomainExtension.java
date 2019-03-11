package com.elster.jupiter.cim.webservices.inbound.soap.servicecall.usagepointconfig;

import com.elster.jupiter.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public class UsagePointConfigDomainExtension extends AbstractPersistentDomainExtension
        implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {
        DOMAIN("serviceCall", "serviceCall"),
        USAGE_POINT("usagePoint", "usagePoint"),
        REQUEST_TIMESTAMP("requestTimestamp", "requestTimestamp"),
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

    @Size(max = Table.MAX_STRING_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"
            + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String usagePoint;
    private Instant requestTimestamp;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private BigDecimal parentServiceCallId;
    @Size(max = Table.MAX_STRING_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"
            + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String errorMessage;
    @Size(max = Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"
            + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String errorCode;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"
            + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String operation;

    public String getUsagePoint() {
        return usagePoint;
    }

    public void setUsagePoint(String usagePoint) {
        this.usagePoint = usagePoint;
    }

    public Instant getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(Instant requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
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
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues,
            Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        setUsagePoint((String) propertyValues.getProperty(FieldNames.USAGE_POINT.javaName));
        setRequestTimestamp((Instant) propertyValues.getProperty(FieldNames.REQUEST_TIMESTAMP.javaName));
        setParentServiceCallId(new BigDecimal(
                Optional.ofNullable(propertyValues.getProperty(FieldNames.PARENT_SERVICE_CALL.javaName()))
                        .orElse(BigDecimal.ZERO).toString()));
        setErrorMessage((String) propertyValues.getProperty(FieldNames.ERROR_MESSAGE.javaName()));
        setErrorCode((String) propertyValues.getProperty(FieldNames.ERROR_CODE.javaName()));
        setOperation((String) propertyValues.getProperty(FieldNames.OPERATION.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.USAGE_POINT.javaName(), getUsagePoint());
        propertySetValues.setProperty(FieldNames.REQUEST_TIMESTAMP.javaName(), getRequestTimestamp());
        propertySetValues.setProperty(FieldNames.PARENT_SERVICE_CALL.javaName(), getParentServiceCallId());
        propertySetValues.setProperty(FieldNames.ERROR_MESSAGE.javaName(), getErrorMessage());
        propertySetValues.setProperty(FieldNames.ERROR_CODE.javaName(), getErrorCode());
        propertySetValues.setProperty(FieldNames.OPERATION.javaName(), getOperation());
    }

    @Override
    public void validateDelete() {
    }

}
