/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig;

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
import java.math.BigDecimal;
import java.util.Optional;

public class MasterDataLinkageConfigMasterDomainExtension extends AbstractPersistentDomainExtension
        implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {
        DOMAIN("serviceCall", "SERVICECALL"),
        CALLS_EXPECTED("expectedNumberOfCalls", "EXPECTED_CALLS"),
        CALLS_SUCCESS("actualNumberOfSuccessfulCalls", "SUCCESS_CALLS"),
        CALLS_FAILED("actualNumberOfFailedCalls", "FAILED_CALLS"),
        CALLBACK_URL("callbackURL", "CALLBACK_URL"),
        CORRELATION_ID("correlationId", "CORRELATION_ID");

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
    private BigDecimal expectedNumberOfCalls;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private BigDecimal actualNumberOfSuccessfulCalls;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private BigDecimal actualNumberOfFailedCalls;
    @Size(max = Table.MAX_STRING_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"
            + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String callbackURL;

    @Size(max = Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"
            + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String correlationId;

    public BigDecimal getExpectedNumberOfCalls() {
        return expectedNumberOfCalls;
    }

    public void setExpectedNumberOfCalls(BigDecimal expectedNumberOfCalls) {
        this.expectedNumberOfCalls = expectedNumberOfCalls;
    }

    public BigDecimal getActualNumberOfSuccessfulCalls() {
        return actualNumberOfSuccessfulCalls;
    }

    public void setActualNumberOfSuccessfulCalls(BigDecimal actualNumberOfSuccessfulCalls) {
        this.actualNumberOfSuccessfulCalls = actualNumberOfSuccessfulCalls;
    }

    public BigDecimal getActualNumberOfFailedCalls() {
        return actualNumberOfFailedCalls;
    }

    public void setActualNumberOfFailedCalls(BigDecimal actualNumberOfFailedCalls) {
        this.actualNumberOfFailedCalls = actualNumberOfFailedCalls;
    }

    public String getCallbackURL() {
        return callbackURL;
    }

    public void setCallbackURL(String callbackURL) {
        this.callbackURL = callbackURL;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }


    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues,
                         Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        setExpectedNumberOfCalls(new BigDecimal(Optional
                .ofNullable(propertyValues.getProperty(FieldNames.CALLS_EXPECTED.javaName())).orElse(0).toString()));
        setActualNumberOfSuccessfulCalls(new BigDecimal(Optional
                .ofNullable(propertyValues.getProperty(FieldNames.CALLS_SUCCESS.javaName())).orElse(0).toString()));
        setActualNumberOfFailedCalls(new BigDecimal(Optional
                .ofNullable(propertyValues.getProperty(FieldNames.CALLS_FAILED.javaName())).orElse(0).toString()));
        setCallbackURL((String) propertyValues.getProperty(FieldNames.CALLBACK_URL.javaName()));
        setCorrelationId((String) propertyValues.getProperty(FieldNames.CORRELATION_ID.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.CALLS_EXPECTED.javaName(), getExpectedNumberOfCalls());
        propertySetValues.setProperty(FieldNames.CALLS_SUCCESS.javaName(), getActualNumberOfSuccessfulCalls());
        propertySetValues.setProperty(FieldNames.CALLS_FAILED.javaName(), getActualNumberOfFailedCalls());
        propertySetValues.setProperty(FieldNames.CALLBACK_URL.javaName(), getCallbackURL());
        propertySetValues.setProperty(FieldNames.CORRELATION_ID.javaName(), getCorrelationId());
    }

    @Override
    public void validateDelete() {
    }

}
