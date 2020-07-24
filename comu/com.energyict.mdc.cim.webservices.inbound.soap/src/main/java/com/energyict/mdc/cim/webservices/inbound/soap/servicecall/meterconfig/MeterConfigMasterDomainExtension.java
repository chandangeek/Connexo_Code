/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.meterconfig;

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

public class MeterConfigMasterDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {
    public enum FieldNames {
        DOMAIN("serviceCall", "SERVICE_CALL"),
        CALLS_EXPECTED("expectedNumberOfCalls", "EXPECTED_CALLS"),
        CALLS_SUCCESS("actualNumberOfSuccessfulCalls", "SUCCESS_CALLS"),
        CALLS_FAILED("actualNumberOfFailedCalls", "FAILED_CALLS"),
        CALLBACK_URL("callbackURL", "CALLBACK_URL"),
        METER_STATUS_SOURCE("meterStatusSource", "METER_STATUS_SOURCE"),
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
    private Long expectedNumberOfCalls;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private Long actualNumberOfSuccessfulCalls;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private Long actualNumberOfFailedCalls;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String callbackURL;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String meterStatusSource;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String correlationId;

    public MeterConfigMasterDomainExtension() {
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

    public String getMeterStatusSource() {
        return meterStatusSource;
    }

    public void setMeterStatusSource(String meterStatusSource) {
        this.meterStatusSource = meterStatusSource;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setExpectedNumberOfCalls((Long)Optional.ofNullable(propertyValues.getProperty(FieldNames.CALLS_EXPECTED.javaName())).orElse(0l));
        this.setActualNumberOfSuccessfulCalls((Long)Optional.ofNullable(propertyValues.getProperty(FieldNames.CALLS_SUCCESS.javaName())).orElse(0l));
        this.setActualNumberOfFailedCalls((Long)Optional.ofNullable(propertyValues.getProperty(FieldNames.CALLS_FAILED.javaName())).orElse(0l));
        this.setCallbackURL((String) propertyValues.getProperty(FieldNames.CALLBACK_URL.javaName()));
        this.setMeterStatusSource((String) propertyValues.getProperty(FieldNames.METER_STATUS_SOURCE.javaName()));
        this.setCorrelationId((String) propertyValues.getProperty(FieldNames.CORRELATION_ID.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.CALLS_EXPECTED.javaName(), this.getExpectedNumberOfCalls());
        propertySetValues.setProperty(FieldNames.CALLS_SUCCESS.javaName(), this.getActualNumberOfSuccessfulCalls());
        propertySetValues.setProperty(FieldNames.CALLS_FAILED.javaName(), this.getActualNumberOfFailedCalls());
        propertySetValues.setProperty(FieldNames.CALLBACK_URL.javaName(), this.getCallbackURL());
        propertySetValues.setProperty(FieldNames.METER_STATUS_SOURCE.javaName(), this.getMeterStatusSource());
        propertySetValues.setProperty(FieldNames.CORRELATION_ID.javaName(), this.getCorrelationId());
    }

    @Override
    public void validateDelete() {
    }
}
