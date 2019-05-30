/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Optional;

public class MasterMeterReadingDocumentCreateRequestDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {

        DOMAIN("serviceCall", "serviceCall"),
        REQUEST_ID("requestID", "request_id"),
        ATTEMPT_NUMBER("attemptNumber", "attempt_number"),
        CONFIRMATION_URL("confirmationURL", "confirmation_url"),
        RESULT_URL("resultURL", "result_url"),
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

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String requestID;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private BigDecimal attemptNumber;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String confirmationURL;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String resultURL;
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

    public BigDecimal getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(BigDecimal attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public String getConfirmationURL() {
        return confirmationURL;
    }

    public void setConfirmationURL(String confirmationURL) {
        this.confirmationURL = confirmationURL;
    }

    public String getResultURL() {
        return resultURL;
    }

    public void setResultURL(String resultURL) {
        this.resultURL = resultURL;
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
        this.setAttemptNumber(new BigDecimal(Optional.ofNullable(propertyValues.getProperty(FieldNames.ATTEMPT_NUMBER.javaName()))
                .orElse(BigDecimal.ZERO).toString()));
        this.setConfirmationURL((String) propertyValues.getProperty(FieldNames.CONFIRMATION_URL.javaName()));
        this.setResultURL((String) propertyValues.getProperty(FieldNames.RESULT_URL.javaName()));
        this.setBulk((Boolean) propertyValues.getProperty(FieldNames.BULK.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.REQUEST_ID.javaName(), this.getRequestID());
        propertySetValues.setProperty(FieldNames.ATTEMPT_NUMBER.javaName(), this.getAttemptNumber());
        propertySetValues.setProperty(FieldNames.CONFIRMATION_URL.javaName(), this.getConfirmationURL());
        propertySetValues.setProperty(FieldNames.RESULT_URL.javaName(), this.getResultURL());
        propertySetValues.setProperty(FieldNames.BULK.javaName(), this.isBulk());
    }

    @Override
    public void validateDelete() {
    }
}