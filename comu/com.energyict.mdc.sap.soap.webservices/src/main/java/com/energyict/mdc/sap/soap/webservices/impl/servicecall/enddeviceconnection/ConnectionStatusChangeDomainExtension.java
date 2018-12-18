/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;

import javax.validation.constraints.Size;
import java.time.Instant;

public class ConnectionStatusChangeDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {

        DOMAIN("serviceCall", "service_call"),
        ID("id", "id"),
        CATEGORY_CODE("categoryCode", "category_code"),
        CONFIRMATION_URL("confirmationURL", "confirmation_url"),
        REASON_CODE("reasonCode", "reason_code"),
        PROCESS_DATE("processDate", "process_date"),

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
    private String id;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String categoryCode;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String confirmationURL;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String reasonCode;
    private Instant processDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getConfirmationURL() {
        return confirmationURL;
    }

    public void setConfirmationURL(String confirmationURL) {
        this.confirmationURL = confirmationURL;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public Instant getProcessDate() {
        return processDate;
    }

    public void setProcessDate(Instant processDate) {
        this.processDate = processDate;
    }

    public ConnectionStatusChangeDomainExtension() {
        super();
    }

    @Override
    public void copyFrom(ServiceCall domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(domainInstance);
        this.setId((String) propertyValues.getProperty(FieldNames.ID.javaName()));
        this.setCategoryCode((String) propertyValues.getProperty(FieldNames.CATEGORY_CODE.javaName()));
        this.setConfirmationURL((String) propertyValues.getProperty(FieldNames.CONFIRMATION_URL.javaName()));
        this.setReasonCode((String) propertyValues.getProperty(FieldNames.REASON_CODE.javaName()));
        this.setProcessDate((Instant) propertyValues.getProperty(FieldNames.PROCESS_DATE.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.ID.javaName(), this.getId());
        propertySetValues.setProperty(FieldNames.CATEGORY_CODE.javaName(), this.getCategoryCode());
        propertySetValues.setProperty(FieldNames.CONFIRMATION_URL.javaName(), this.getConfirmationURL());
        propertySetValues.setProperty(FieldNames.REASON_CODE.javaName(), this.getReasonCode());
        propertySetValues.setProperty(FieldNames.PROCESS_DATE.javaName(), this.getProcessDate());
    }

    @Override
    public void validateDelete() {
        // nothing to validate
    }
}