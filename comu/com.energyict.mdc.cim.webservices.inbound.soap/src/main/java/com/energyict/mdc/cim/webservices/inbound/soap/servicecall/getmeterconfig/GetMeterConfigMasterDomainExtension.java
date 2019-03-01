/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterconfig;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Optional;

public class GetMeterConfigMasterDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {
    public enum FieldNames {
        DOMAIN("SERVICE_CALL", "SERVICE_CALL"),
        CALLS_EXPECTED("EXPECTED_NUMBER_OF_CALLS", "EXPECTED_CALLS"),
        CALLS_SUCCESS("ACTUAL_NUMBER_OF_SUCCESSFUL_CALLS", "SUCCESS_CALLS"),
        CALLS_FAILED("ACTUAL_NUMBER_OF_FAILED_CALLS", "FAILED_CALLS"),
        CALLBACK_URL("CALLBACK_URL", "CALLBACK_URL");

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
    private Long expectedNumberOfCalls;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private Long actualNumberOfSuccessfulCalls;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private Long actualNumberOfFailedCalls;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String callbackURL;

    public GetMeterConfigMasterDomainExtension() {
        super();
    }

    public Long getExpectedNumberOfCalls() {
        return expectedNumberOfCalls;
    }

    public void setExpectedNumberOfCalls(Long expectedNumberOfCalls) {
        this.expectedNumberOfCalls = expectedNumberOfCalls;
    }

    public Long getActualNumberOfSuccessfulCalls() {
        return actualNumberOfSuccessfulCalls;
    }

    public void setActualNumberOfSuccessfulCalls(Long actualNumberOfSuccessfulCalls) {
        this.actualNumberOfSuccessfulCalls = actualNumberOfSuccessfulCalls;
    }

    public Long getActualNumberOfFailedCalls() {
        return actualNumberOfFailedCalls;
    }

    public void setActualNumberOfFailedCalls(Long actualNumberOfFailedCalls) {
        this.actualNumberOfFailedCalls = actualNumberOfFailedCalls;
    }

    public String getCallbackURL() {
        return callbackURL;
    }

    public void setCallbackURL(String callbackURL) {
        this.callbackURL = callbackURL;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setExpectedNumberOfCalls((Long)Optional.ofNullable(propertyValues.getProperty(FieldNames.CALLS_EXPECTED.javaName())).orElse(0l));
        this.setActualNumberOfSuccessfulCalls((Long)Optional.ofNullable(propertyValues.getProperty(FieldNames.CALLS_SUCCESS.javaName())).orElse(0l));
        this.setActualNumberOfFailedCalls((Long)Optional.ofNullable(propertyValues.getProperty(FieldNames.CALLS_FAILED.javaName())).orElse(0l));
        this.setCallbackURL((String) propertyValues.getProperty(FieldNames.CALLBACK_URL.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.CALLS_EXPECTED.javaName(), this.getExpectedNumberOfCalls());
        propertySetValues.setProperty(FieldNames.CALLS_SUCCESS.javaName(), this.getActualNumberOfSuccessfulCalls());
        propertySetValues.setProperty(FieldNames.CALLS_FAILED.javaName(), this.getActualNumberOfFailedCalls());
        propertySetValues.setProperty(FieldNames.CALLBACK_URL.javaName(), this.getCallbackURL());
    }

    @Override
    public void validateDelete() {
    }
}

