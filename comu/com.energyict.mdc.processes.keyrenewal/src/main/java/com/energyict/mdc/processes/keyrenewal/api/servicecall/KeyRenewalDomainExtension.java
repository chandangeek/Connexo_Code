/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.processes.keyrenewal.api.servicecall;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.processes.keyrenewal.api.MessageSeeds;

import javax.validation.constraints.Size;

public class KeyRenewalDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {
    public enum FieldNames {
        DOMAIN("serviceCall", "serviceCall"),
        CALLBACK_SUCCESS("callbackSuccess", "callbackSuccess"),
        CALLBACK_ERROR("callbackError", "callbackError"),
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
    private String callbackSuccess;
    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String callbackError;
    private boolean providedResponse;

    public KeyRenewalDomainExtension() {
        super();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCallbackSuccess() {
        return callbackSuccess;
    }

    public void setCallbackSuccess(String callbackSuccess) {
        this.callbackSuccess = callbackSuccess;
    }

    public String getCallbackError() {
        return callbackError;
    }

    public void setCallbackError(String callbackError) {
        this.callbackError = callbackError;
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
        this.setCallbackSuccess((String) propertyValues.getProperty(FieldNames.CALLBACK_SUCCESS.javaName()));
        this.setCallbackError((String) propertyValues.getProperty(FieldNames.CALLBACK_ERROR.javaName()));
        this.setProvidedResponse((Boolean) propertyValues.getProperty(FieldNames.PROVIDED_RESPONSE.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.CALLBACK_SUCCESS.javaName(), this.getCallbackSuccess());
        propertySetValues.setProperty(FieldNames.CALLBACK_ERROR.javaName(), this.getCallbackError());
        propertySetValues.setProperty(FieldNames.PROVIDED_RESPONSE.javaName(), this.providedResponse());
    }

    @Override
    public void validateDelete() {
        //TODO: maybe check if the ServiceCall is completed - open servicecalls should not be deletable?
    }
}