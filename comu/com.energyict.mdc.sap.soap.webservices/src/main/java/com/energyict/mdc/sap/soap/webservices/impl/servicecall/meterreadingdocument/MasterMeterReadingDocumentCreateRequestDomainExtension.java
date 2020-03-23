/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument;

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

public class MasterMeterReadingDocumentCreateRequestDomainExtension extends AbstractPersistentDomainExtension implements RetrySearchDataSourceDomainExtension {

    public enum FieldNames {

        DOMAIN("serviceCall", "serviceCall"),
        REQUEST_ID("requestID", "request_id"),
        UUID("uuid", "UUID"),
        ATTEMPT_NUMBER("attemptNumber", "attempt_number"),
        CONFIRMATION_URL("confirmationURL", "confirmation_url"), //up to 10.7
        RESULT_URL("resultURL", "result_url"), //up to 10.7
        BULK("bulk", "bulk");

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
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private BigDecimal attemptNumber;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private Boolean bulk;

    public MasterMeterReadingDocumentCreateRequestDomainExtension() {
        super();
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

    public BigDecimal getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(BigDecimal attemptNumber) {
        this.attemptNumber = attemptNumber;
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
        this.setAttemptNumber(new BigDecimal(Optional.ofNullable(propertyValues.getProperty(FieldNames.ATTEMPT_NUMBER.javaName()))
                .orElse(BigDecimal.ZERO).toString()));
        this.setBulk((Boolean) propertyValues.getProperty(FieldNames.BULK.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.REQUEST_ID.javaName(), this.getRequestID());
        propertySetValues.setProperty(FieldNames.UUID.javaName(), this.getUuid());
        propertySetValues.setProperty(FieldNames.ATTEMPT_NUMBER.javaName(), this.getAttemptNumber());
        propertySetValues.setProperty(FieldNames.BULK.javaName(), this.isBulk());
    }

    @Override
    public void validateDelete() {
    }

    @Override
    public ServiceCall getServiceCall() {
        return serviceCall.get();
    }
}