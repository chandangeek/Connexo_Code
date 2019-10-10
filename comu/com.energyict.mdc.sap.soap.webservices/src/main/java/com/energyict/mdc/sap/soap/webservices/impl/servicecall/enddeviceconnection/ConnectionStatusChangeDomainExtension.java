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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

public class ConnectionStatusChangeDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {

        DOMAIN("serviceCall", "service_call"),
        ID("id", "id"),
        CATEGORY_CODE("categoryCode", "category_code"),
        CONFIRMATION_URL("confirmationURL", "confirmation_url"), //up to 10.7
        REASON_CODE("reasonCode", "reason_code"),
        PROCESS_DATE("processDate", "process_date"),

        BULK("bulk", "BULK"),
        CANCELLED_BY_SAP("cancelledBySap", "CANCELLED_BY_SAP"),
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
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String reasonCode;
    private Instant processDate;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private Boolean bulk;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private Boolean cancelledBySap;

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

    public Boolean isBulk() {
        return bulk;
    }

    public void setBulk(Boolean bulk) {
        this.bulk = bulk;
    }

    public Boolean isCancelledBySap() {
        return cancelledBySap;
    }

    public void setCancelledBySap(Boolean cancelledBySap) {
        this.cancelledBySap = cancelledBySap;
    }

    public ConnectionStatusChangeDomainExtension() {
        super();
    }

    @Override
    public void copyFrom(ServiceCall domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(domainInstance);
        this.setId((String) propertyValues.getProperty(FieldNames.ID.javaName()));
        this.setCategoryCode((String) propertyValues.getProperty(FieldNames.CATEGORY_CODE.javaName()));
        this.setReasonCode((String) propertyValues.getProperty(FieldNames.REASON_CODE.javaName()));
        this.setProcessDate((Instant) propertyValues.getProperty(FieldNames.PROCESS_DATE.javaName()));
        this.setBulk((Boolean) propertyValues.getProperty(FieldNames.BULK.javaName()));
        this.setCancelledBySap((Boolean) propertyValues.getProperty(FieldNames.CANCELLED_BY_SAP.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.ID.javaName(), this.getId());
        propertySetValues.setProperty(FieldNames.CATEGORY_CODE.javaName(), this.getCategoryCode());
        propertySetValues.setProperty(FieldNames.REASON_CODE.javaName(), this.getReasonCode());
        propertySetValues.setProperty(FieldNames.PROCESS_DATE.javaName(), this.getProcessDate());
        propertySetValues.setProperty(FieldNames.BULK.javaName(), this.isBulk());
        propertySetValues.setProperty(FieldNames.CANCELLED_BY_SAP.javaName(), this.isCancelledBySap());
    }

    @Override
    public void validateDelete() {
        // nothing to validate
    }

    public ServiceCall getServiceCall(){
        return serviceCall.get();
    }
}