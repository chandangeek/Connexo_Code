/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.RetrySearchDataSourceDomainExtension;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Optional;

public class MasterPodNotificationDomainExtension extends AbstractPersistentDomainExtension implements RetrySearchDataSourceDomainExtension {
    public enum FieldNames {
        // general
        DOMAIN("serviceCall", "SERVICE_CALL"),

        // provided
        REQUEST_ID("requestID", "REQUEST_ID"),
        UUID("uuid", "UUID"),
        BULK("bulk", "BULK"),
        ATTEMPT_NUMBER("attemptNumber", "ATTEMPT_NUMBER"),
        ;

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

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String requestID;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String uuid;
    private boolean bulk;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private BigDecimal attemptNumber;

    public BigDecimal getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(BigDecimal attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isBulk() {
        return bulk;
    }

    public void setBulk(boolean bulk) {
        this.bulk = bulk;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setRequestID((String) propertyValues.getProperty(FieldNames.REQUEST_ID.javaName()));
        this.setUuid((String) propertyValues.getProperty(FieldNames.UUID.javaName()));
        this.setBulk((boolean) propertyValues.getProperty(FieldNames.BULK.javaName()));
        this.setAttemptNumber(new BigDecimal(Optional.ofNullable(propertyValues.getProperty(FieldNames.ATTEMPT_NUMBER.javaName()))
                .orElse(BigDecimal.ZERO).toString()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.REQUEST_ID.javaName(), this.getRequestID());
        propertySetValues.setProperty(FieldNames.UUID.javaName(), this.getUuid());
        propertySetValues.setProperty(FieldNames.BULK.javaName(), this.isBulk());
        propertySetValues.setProperty(FieldNames.ATTEMPT_NUMBER.javaName(), this.getAttemptNumber());
    }

    @Override
    public void validateDelete() {
    }

    @Override
    public ServiceCall getServiceCall() {
        return serviceCall.get();
    }
}
