/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.getmeterreadings;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

public class ParentGetMeterReadingsDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {

    public enum FieldNames {
        DOMAIN("serviceCall", "SERVICE_CALL"),
        SOURCE("source", "SOURCE"),
        CALLBACK_URL("callbackUrl", "CALLBACK_URL"),
        CORRELATION_ID("correlationId", "CORRELATION_ID"),
        TIME_PERIOD_START("timePeriodStart", "TIME_PERIOD_START"),
        TIME_PERIOD_END("timePeriodEnd", "TIME_PERIOD_END"),
        READING_TYPES("readingTypes", "READING_TYPES"),
        LOAD_PROFILES("loadProfiles", "LOAD_PROFILES"),
        REGISTER_GROUPS("registerGroups", "REGISTER_GROUPS"),
        SCHEDULE_STRATEGY("scheduleStrategy", "SCHEDULE_STRATEGY"),
        CONNECTION_METHOD("connectionMethod", "CONNECTION_METHOD"),
        RESPONSE_STATUS("responseStatus", "RESPONSE_STATUS")
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

    public enum ResponseStatus {
        NOT_SENT("Not sent"),
        NOT_CONFIRMED("Sent / not confirmed"),
        CONFIRMED("Confirmed")
        ;

        ResponseStatus(String name) {
            this.name = name;
        }

        private final String name;

        public String getName() {
            return name;
        }
    }

    @IsPresent
    private Reference<ServiceCall> serviceCall = Reference.empty();
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String source;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String callbackUrl;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String correlationId;
    private Instant timePeriodStart;
    private Instant timePeriodEnd;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String readingTypes;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String loadProfiles;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String registerGroups;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String scheduleStrategy;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String connectionMethod;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String responseStatus;

    public ParentGetMeterReadingsDomainExtension() {
        super();
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Instant getTimePeriodStart() {
        return timePeriodStart;
    }

    public void setTimePeriodStart(Instant timePeriodStart) {
        this.timePeriodStart = timePeriodStart;
    }

    public Instant getTimePeriodEnd() {
        return timePeriodEnd;
    }

    public void setTimePeriodEnd(Instant timePeriodEnd) {
        this.timePeriodEnd = timePeriodEnd;
    }

    public String getReadingTypes() {
        return readingTypes;
    }

    public void setReadingTypes(String readingTypes) {
        this.readingTypes = readingTypes;
    }

    public String getLoadProfiles() {
        return loadProfiles;
    }

    public void setLoadProfiles(String loadProfiles) {
        this.loadProfiles = loadProfiles;
    }

    public String getRegisterGroups() {
        return registerGroups;
    }

    public void setRegisterGroups(String registerGroups) {
        this.registerGroups = registerGroups;
    }

    public String getScheduleStrategy() {
        return scheduleStrategy;
    }

    public void setScheduleStrategy(String scheduleStrategy) {
        this.scheduleStrategy = scheduleStrategy;
    }

    public String getConnectionMethod() {
        return connectionMethod;
    }

    public void setConnectionMethod(String connectionMethod) {
        this.connectionMethod = connectionMethod;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setSource((String) propertyValues.getProperty(FieldNames.SOURCE.javaName()));
        this.setCallbackUrl((String) propertyValues.getProperty(FieldNames.CALLBACK_URL.javaName()));
        this.setCorrelationId((String) propertyValues.getProperty(FieldNames.CORRELATION_ID.javaName()));
        this.setTimePeriodStart((Instant) propertyValues.getProperty(FieldNames.TIME_PERIOD_START.javaName()));
        this.setTimePeriodEnd((Instant) propertyValues.getProperty(FieldNames.TIME_PERIOD_END.javaName()));
        this.setReadingTypes((String) propertyValues.getProperty(FieldNames.READING_TYPES.javaName()));
        this.setLoadProfiles((String) propertyValues.getProperty(FieldNames.LOAD_PROFILES.javaName()));
        this.setRegisterGroups((String) propertyValues.getProperty(FieldNames.REGISTER_GROUPS.javaName()));
        this.setScheduleStrategy((String) propertyValues.getProperty(FieldNames.SCHEDULE_STRATEGY.javaName()));
        this.setConnectionMethod((String) propertyValues.getProperty(FieldNames.CONNECTION_METHOD.javaName()));
        this.setResponseStatus((String) propertyValues.getProperty(FieldNames.RESPONSE_STATUS.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.SOURCE.javaName(), this.getSource());
        propertySetValues.setProperty(FieldNames.CALLBACK_URL.javaName(), this.getCallbackUrl());
        propertySetValues.setProperty(FieldNames.CORRELATION_ID.javaName(), this.getCorrelationId());
        propertySetValues.setProperty(FieldNames.TIME_PERIOD_START.javaName(), this.getTimePeriodStart());
        propertySetValues.setProperty(FieldNames.TIME_PERIOD_END.javaName(), this.getTimePeriodEnd());
        propertySetValues.setProperty(FieldNames.READING_TYPES.javaName(), this.getReadingTypes());
        propertySetValues.setProperty(FieldNames.LOAD_PROFILES.javaName(), this.getLoadProfiles());
        propertySetValues.setProperty(FieldNames.REGISTER_GROUPS.javaName(), this.getRegisterGroups());
        propertySetValues.setProperty(FieldNames.SCHEDULE_STRATEGY.javaName(), this.getScheduleStrategy());
        propertySetValues.setProperty(FieldNames.CONNECTION_METHOD.javaName(), this.getConnectionMethod());
        propertySetValues.setProperty(FieldNames.RESPONSE_STATUS.javaName(), this.getResponseStatus());
    }

    @Override
    public void validateDelete() {
        // do nothing
    }
}
