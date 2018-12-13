/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.servicecall.examples;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;

import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public class DisconnectRequestDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {
    public enum FieldNames {
        DOMAIN("serviceCall", "serviceCall"),
        REASON("reason", "disconnect_reason"),
        ATTEMPTS("attempts", "disconnect_attempts"),
        ENDDATE("endDate", "disconnect_enddate");

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

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "FieldTooLong")
    private String reason;
    private BigDecimal attempts;
    private Instant endDate;

    public DisconnectRequestDomainExtension() {
        super();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return super.getRegisteredCustomPropertySet();
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getAttempts() {
        return attempts;
    }

    public void setAttempts(BigDecimal attempts) {
        this.attempts = attempts;
    }

    public Instant getEnddate() {
        return endDate;
    }

    public void setEnddate(Instant enddate) {
        this.endDate = enddate;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setReason((String) propertyValues.getProperty(FieldNames.REASON.javaName()));
        this.setAttempts(new BigDecimal(propertyValues.getProperty(FieldNames.ATTEMPTS.javaName()).toString()));
        this.setEnddate((Instant) propertyValues.getProperty(FieldNames.ENDDATE.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.REASON.javaName(), this.getReason());
        propertySetValues.setProperty(FieldNames.ATTEMPTS.javaName(), this.getAttempts());
        propertySetValues.setProperty(FieldNames.ENDDATE.javaName(), this.getEnddate());
    }

    @Override
    public void validateDelete() {
    }
}
