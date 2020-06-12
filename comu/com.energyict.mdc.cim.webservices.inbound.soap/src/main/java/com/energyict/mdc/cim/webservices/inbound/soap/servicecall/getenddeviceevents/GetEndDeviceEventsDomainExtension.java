/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getenddeviceevents;

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
import java.time.Instant;

public class GetEndDeviceEventsDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {
    public enum FieldNames {
        DOMAIN("serviceCall", "serviceCall"),
        METERS("meters", "METER"),
        DEVICE_GROUPS("deviceGroups", "DEVICE_GROUPS"),
        EVENT_TYPES("eventTypes", "EVENT_TYPES"),
        FROM_DATE("fromDate", "fromDate"),
        TO_DATE("toDate", "toDate"),
        ERROR_MESSAGE("errorMessage", "errorMessage"),
        CALLBACK_URL("callbackURL", "callback_url"),
        CORRELATION_ID("correlationId", "correlation_id");

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
    private String meters;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String deviceGroups;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String eventTypes;
    private Instant fromDate;
    private Instant toDate;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String errorMessage;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String callbackURL;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String correlationId;


    public GetEndDeviceEventsDomainExtension() {
        super();
    }

    public String getMeters() {
        return meters;
    }

    public void setMeters(String meters) {
        this.meters = meters;
    }

    public String getDeviceGroups() {
        return deviceGroups;
    }

    public void setDeviceGroups(String deviceGroups) {
        this.deviceGroups = deviceGroups;
    }

    public String getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(String eventTypes) {
        this.eventTypes = eventTypes;
    }

    public Instant getFromDate() {
        return fromDate;
    }

    public void setFromDate(Instant fromDate) {
        this.fromDate = fromDate;
    }

    public Instant getToDate() {
        return toDate;
    }

    public void setToDate(Instant toDate) {
        this.toDate = toDate;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setMeters((String) propertyValues.getProperty(FieldNames.METERS.javaName()));
        this.setDeviceGroups((String) propertyValues.getProperty(FieldNames.DEVICE_GROUPS.javaName()));
        this.setEventTypes((String) propertyValues.getProperty(FieldNames.EVENT_TYPES.javaName()));
        this.setFromDate((Instant) propertyValues.getProperty(FieldNames.FROM_DATE.javaName()));
        this.setToDate((Instant) propertyValues.getProperty(FieldNames.TO_DATE.javaName()));
        this.setErrorMessage((String) propertyValues.getProperty(FieldNames.ERROR_MESSAGE.javaName()));
        this.setCallbackURL((String) propertyValues.getProperty(FieldNames.CALLBACK_URL.javaName()));
        this.setCorrelationId((String) propertyValues.getProperty(FieldNames.CORRELATION_ID.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.METERS.javaName(), this.getMeters());
        propertySetValues.setProperty(FieldNames.DEVICE_GROUPS.javaName(), this.getDeviceGroups());
        propertySetValues.setProperty(FieldNames.EVENT_TYPES.javaName(), this.getEventTypes());
        propertySetValues.setProperty(FieldNames.FROM_DATE.javaName(), this.getFromDate());
        propertySetValues.setProperty(FieldNames.TO_DATE.javaName(), this.getToDate());
        propertySetValues.setProperty(FieldNames.ERROR_MESSAGE.javaName(), this.getErrorMessage());
        propertySetValues.setProperty(FieldNames.CALLBACK_URL.javaName(), this.getCallbackURL());
        propertySetValues.setProperty(FieldNames.CORRELATION_ID.javaName(), this.getCorrelationId());
    }

    @Override
    public void validateDelete() {
    }
}
