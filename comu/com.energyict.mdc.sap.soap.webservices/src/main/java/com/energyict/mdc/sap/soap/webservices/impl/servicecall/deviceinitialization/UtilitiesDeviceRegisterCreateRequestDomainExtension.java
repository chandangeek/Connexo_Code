/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.Instant;

public class UtilitiesDeviceRegisterCreateRequestDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {
    public enum FieldNames {
        // general
        DOMAIN("serviceCall", "SERVICE_CALL"),

        // provided
        DEVICE_ID("deviceId", "DEVICE_ID"),
        OBIS("obis", "OBIS"),
        RECURRENCE_CODE("recurrenceCode", "INTERVAL"),
        LRN("lrn", "LRN"),
        START_DATE("startDate", "START_DATE"),
        END_DATE("endDate", "END_DATE"),
        TIME_ZONE("timeZone", "TIME_ZONE"),
        DIVISION_CATEGORY("divisionCategory", "DIVISION_CATEGORY"),
        REGISTER_ID("registerId", "REGISTER_ID"),

        TOTAL_DIGIT_NUMBER_VALUE("totalDigitNumberValue", "TOTAL_DIGIT_NUMBER_VALUE"),
        FRACTION_DIGIT_NUMBER_VALUE("fractionDigitNumberValue", "FRACTION_DIGIT_NUMBER_VALUE"),

        //returned
        ERROR_CODE("errorCode", "ERROR_CODE"),
        ERROR_MESSAGE("errorMessage", "ERROR_MESSAGE"),
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

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String deviceId;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String obis;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String recurrenceCode;

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String lrn;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String divisionCategory;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String registerId;

    private Instant startDate;
    private Instant endDate;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String errorCode;

    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String errorMessage;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String timeZone;

    private BigDecimal totalDigitNumberValue;
    private BigDecimal fractionDigitNumberValue;

    public ServiceCall getServiceCall() {
        return serviceCall.get();
    }

    public String getObis() {
        return obis;
    }

    public void setObis(String obis) {
        this.obis = obis;
    }

    public String getRecurrenceCode() {
        return recurrenceCode;
    }

    public void setRecurrenceCode(String recurrenceCode) {
        this.recurrenceCode = recurrenceCode;
    }

    public String getLrn() {
        return lrn;
    }

    public void setLrn(String lrn) {
        this.lrn = lrn;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getDivisionCategory() {
        return divisionCategory;
    }

    public void setDivisionCategory(String divisionCategory) {
        this.divisionCategory = divisionCategory;
    }

    public String getRegisterId() {
        return registerId;
    }

    public void setRegisterId(String registerId) {
        this.registerId = registerId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setError(MessageSeed messageSeed, Object... args) {
        setErrorCode(String.valueOf(messageSeed.getNumber()));
        setErrorMessage(MessageFormat.format(messageSeed.getDefaultFormat(), args));
    }

    public BigDecimal getTotalDigitNumberValue() {
        return totalDigitNumberValue;
    }

    public void setTotalDigitNumberValue(BigDecimal totalDigitNumberValue) {
        this.totalDigitNumberValue = totalDigitNumberValue;
    }

    public BigDecimal getFractionDigitNumberValue() {
        return fractionDigitNumberValue;
    }

    public void setFractionDigitNumberValue(BigDecimal fractionDigitNumberValue) {
        this.fractionDigitNumberValue = fractionDigitNumberValue;
    }


    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setDeviceId((String) propertyValues.getProperty(FieldNames.DEVICE_ID.javaName()));
        this.setObis((String) propertyValues.getProperty(FieldNames.OBIS.javaName()));
        this.setRecurrenceCode((String) propertyValues.getProperty(FieldNames.RECURRENCE_CODE.javaName()));
        this.setLrn((String) propertyValues.getProperty(FieldNames.LRN.javaName()));
        this.setStartDate((Instant) propertyValues.getProperty(FieldNames.START_DATE.javaName()));
        this.setEndDate((Instant) propertyValues.getProperty(FieldNames.END_DATE.javaName()));
        this.setTimeZone((String) propertyValues.getProperty(FieldNames.TIME_ZONE.javaName()));
        this.setDivisionCategory((String) propertyValues.getProperty(FieldNames.DIVISION_CATEGORY.javaName()));
        this.setRegisterId((String) propertyValues.getProperty(FieldNames.REGISTER_ID.javaName()));
        this.setErrorCode((String) propertyValues.getProperty(FieldNames.ERROR_CODE.javaName()));
        this.setErrorMessage((String) propertyValues.getProperty(FieldNames.ERROR_MESSAGE.javaName()));
        this.setTotalDigitNumberValue(propertyValues.getProperty(FieldNames.TOTAL_DIGIT_NUMBER_VALUE.javaName()) == null ? null : (BigDecimal) propertyValues.getProperty(FieldNames.TOTAL_DIGIT_NUMBER_VALUE
                .javaName()));
        this.setFractionDigitNumberValue((propertyValues.getProperty(FieldNames.FRACTION_DIGIT_NUMBER_VALUE.javaName()) == null) ? null : (BigDecimal) propertyValues.getProperty(FieldNames.FRACTION_DIGIT_NUMBER_VALUE
                .javaName()));

    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.DEVICE_ID.javaName(), this.getDeviceId());
        propertySetValues.setProperty(FieldNames.OBIS.javaName(), this.getObis());
        propertySetValues.setProperty(FieldNames.RECURRENCE_CODE.javaName(), this.getRecurrenceCode());
        propertySetValues.setProperty(FieldNames.LRN.javaName(), this.getLrn());
        propertySetValues.setProperty(FieldNames.START_DATE.javaName(), this.getStartDate());
        propertySetValues.setProperty(FieldNames.END_DATE.javaName(), this.getEndDate());
        propertySetValues.setProperty(FieldNames.TIME_ZONE.javaName(), this.getTimeZone());
        propertySetValues.setProperty(FieldNames.DIVISION_CATEGORY.javaName(), this.getDivisionCategory());
        propertySetValues.setProperty(FieldNames.REGISTER_ID.javaName(), this.getRegisterId());
        propertySetValues.setProperty(FieldNames.ERROR_CODE.javaName(), this.getErrorCode());
        propertySetValues.setProperty(FieldNames.ERROR_MESSAGE.javaName(), this.getErrorMessage());
        propertySetValues.setProperty(FieldNames.TOTAL_DIGIT_NUMBER_VALUE.javaName(), this.getTotalDigitNumberValue());
        propertySetValues.setProperty(FieldNames.FRACTION_DIGIT_NUMBER_VALUE.javaName(), this.getFractionDigitNumberValue());
    }

    @Override
    public void validateDelete() {

    }
}
