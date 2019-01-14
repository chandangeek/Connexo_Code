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
import java.time.Instant;

public class MasterMeterReadingDocumentCreateResultDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {
        DOMAIN("serviceCall", "serviceCall"),
        REQUEST_UUID("requestUUID", "request_uuid"),
        REFERENCE_ID("referenceID", "reference_id"),
        CONFIRMATION_TIME("confirmationTime", "confirmation_time"),
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
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String requestUUID;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String referenceID;
    private Instant confirmationTime;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String resultURL;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private Boolean bulk;

    public MasterMeterReadingDocumentCreateResultDomainExtension() {
        super();
    }

    public String getRequestUUID() {
        return requestUUID;
    }

    public void setRequestUUID(String requestUUID) {
        this.requestUUID = requestUUID;
    }

    public String getReferenceID() {
        return referenceID;
    }

    public void setReferenceID(String referenceID) {
        this.referenceID = referenceID;
    }

    public Instant getConfirmationTime() {
        return confirmationTime;
    }

    public void setConfirmationTime(Instant confirmationTime) {
        this.confirmationTime = confirmationTime;
    }

    public String getResultURL() {
        return resultURL;
    }

    public void setResultURL(String resultURL) {
        this.resultURL = resultURL;
    }

    public Boolean isBulk() {
        return bulk;
    }

    public void setBulk(Boolean bulk) {
        this.bulk = bulk;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setRequestUUID((String) propertyValues.getProperty(FieldNames.REQUEST_UUID.javaName()));
        this.setReferenceID((String) propertyValues.getProperty(FieldNames.REFERENCE_ID.javaName()));
        this.setConfirmationTime((Instant) propertyValues.getProperty(FieldNames.CONFIRMATION_TIME.javaName()));
        this.setResultURL((String) propertyValues.getProperty(FieldNames.RESULT_URL.javaName()));
        this.setBulk((Boolean) propertyValues.getProperty(FieldNames.BULK.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.REQUEST_UUID.javaName(), this.getRequestUUID());
        propertySetValues.setProperty(FieldNames.REFERENCE_ID.javaName(), this.getReferenceID());
        propertySetValues.setProperty(FieldNames.CONFIRMATION_TIME.javaName(), this.getConfirmationTime());
        propertySetValues.setProperty(FieldNames.RESULT_URL.javaName(), this.getResultURL());
        propertySetValues.setProperty(FieldNames.BULK.javaName(), this.isBulk());
    }

    @Override
    public void validateDelete() {
    }
}
