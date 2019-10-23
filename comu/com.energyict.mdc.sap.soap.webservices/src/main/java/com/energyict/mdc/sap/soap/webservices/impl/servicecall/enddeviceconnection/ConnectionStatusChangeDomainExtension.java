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
        UUID("uuid", "UUID"),
        CATEGORY_CODE("categoryCode", "category_code"),
        CONFIRMATION_URL("confirmationURL", "confirmation_url"), //up to 10.7
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
    private String uuid;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String categoryCode;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String reasonCode;
    private Instant processDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
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
        this.setUuid((String) propertyValues.getProperty(FieldNames.UUID.javaName()));
        this.setCategoryCode((String) propertyValues.getProperty(FieldNames.CATEGORY_CODE.javaName()));
        this.setReasonCode((String) propertyValues.getProperty(FieldNames.REASON_CODE.javaName()));
        this.setProcessDate((Instant) propertyValues.getProperty(FieldNames.PROCESS_DATE.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.ID.javaName(), this.getId());
        propertySetValues.setProperty(FieldNames.UUID.javaName(), this.getUuid());
        propertySetValues.setProperty(FieldNames.CATEGORY_CODE.javaName(), this.getCategoryCode());
        propertySetValues.setProperty(FieldNames.REASON_CODE.javaName(), this.getReasonCode());
        propertySetValues.setProperty(FieldNames.PROCESS_DATE.javaName(), this.getProcessDate());
    }

    @Override
    public void validateDelete() {
        // nothing to validate
    }
}