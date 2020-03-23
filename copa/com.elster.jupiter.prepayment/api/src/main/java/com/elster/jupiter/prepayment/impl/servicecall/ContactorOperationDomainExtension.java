/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.prepayment.impl.servicecall;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.prepayment.impl.BreakerStatus;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.constraints.Size;

/**
 * @author sva
 * @since 30/03/2016 - 15:39
 */
public class ContactorOperationDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {
    public enum FieldNames {
        DOMAIN("serviceCall", "serviceCall"),
        BREAKER_STATUS("status", "status"),
        CALLBACK("callback", "callback"),
        PROVIDED_RESPONSE("providedResponse", "response");

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

    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String status;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String callback;
    private boolean providedResponse;

    public ContactorOperationDomainExtension() {
        super();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BreakerStatus getBreakerStatus() {
        return status != null ? BreakerStatus.valueOf(status) : null;
    }

    public void setBreakerStatus(BreakerStatus breakerStatus) {
        if (breakerStatus == null) {
            this.status = null;
        } else {
            this.status = breakerStatus.name();
        }
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public boolean providedResponse() {
        return providedResponse;
    }

    public void setProvidedResponse(boolean providedResponse) {
        this.providedResponse = providedResponse;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setStatus((String) propertyValues.getProperty(FieldNames.BREAKER_STATUS.javaName()));
        this.setCallback((String) propertyValues.getProperty(FieldNames.CALLBACK.javaName()));
        this.setProvidedResponse((Boolean) propertyValues.getProperty(FieldNames.PROVIDED_RESPONSE.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.BREAKER_STATUS.javaName(), this.getStatus());
        propertySetValues.setProperty(FieldNames.CALLBACK.javaName(), this.getCallback());
        propertySetValues.setProperty(FieldNames.PROVIDED_RESPONSE.javaName(), this.providedResponse());
    }

    @Override
    public void validateDelete() {
        //TODO: maybe check if the ServiceCall is completed - open servicecalls should not be deletable?
    }
}