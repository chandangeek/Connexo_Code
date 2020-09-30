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
        CALLS_EXPECTED("expectedNumberOfCalls", "EXPECTED_CALLS"), // up to 10.9
        CALLS_SUCCESS("actualNumberOfSuccessfulCalls", "SUCCESS_CALLS"), // up to 10.9
        CALLS_FAILED("actualNumberOfFailedCalls", "FAILED_CALLS"), // up to 10.9
        CALLBACK_URL("callbackURL", "CALLBACK_URL"),
        METER_STATUS_SOURCE("meterStatusSource", "METER_STATUS_SOURCE"),
        PING("ping", "PING"),
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

    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String callbackURL;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String meterStatusSource;
    private boolean ping;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String correlationId;

    public MeterConfigMasterDomainExtension() {
        super();
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

    public boolean needsPing() {
        return ping;
    }

    public void setPing(boolean ping) {
        this.ping = ping;
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
        this.setCallbackURL((String) propertyValues.getProperty(FieldNames.CALLBACK_URL.javaName()));
        this.setMeterStatusSource((String) propertyValues.getProperty(FieldNames.METER_STATUS_SOURCE.javaName()));
        this.setPing((boolean) propertyValues.getProperty(FieldNames.PING.javaName()));
        this.setCorrelationId((String) propertyValues.getProperty(FieldNames.CORRELATION_ID.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.CALLBACK_URL.javaName(), this.getCallbackURL());
        propertySetValues.setProperty(FieldNames.METER_STATUS_SOURCE.javaName(), this.getMeterStatusSource());
        propertySetValues.setProperty(FieldNames.PING.javaName(), this.needsPing());
        propertySetValues.setProperty(FieldNames.CORRELATION_ID.javaName(), this.getCorrelationId());
    }

    @Override
    public void validateDelete() {
    }
}
