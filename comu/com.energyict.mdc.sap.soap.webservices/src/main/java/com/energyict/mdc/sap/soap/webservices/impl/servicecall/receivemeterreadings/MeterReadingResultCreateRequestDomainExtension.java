/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.receivemeterreadings;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateRequestDomainExtension;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Optional;

public class MeterReadingResultCreateRequestDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {
    public enum FieldNames {
        // general
        DOMAIN("serviceCall", "SERVICE_CALL"),

        // provided
        DEVICE_ID("deviceId", "DEVICE_ID"),
        METER_READING_DOCUMENT_ID("meterReadingDocumentId", "METER_READING_DOCUMENT_ID"),
        READING_REASON_CODE("readingReasonCode", "READING_REASON_CODE"),
        LRN("lrn", "LRN"),
        METER_READING_DATE_TIME("meterReadingDateTime", "METER_READING_DATE_TIME"),
        METER_READING_TYPE_CODE("meterReadingTypeCode", "METER_READING_TYPE_CODE"),
        METER_READING_VALUE("meterReadingValue", "METER_READING_VALUE"),


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

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String meterReadingDocumentId;

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String lrn;

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String readingReasonCode;

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private Instant meterReadingDateTime;

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String meterReadingTypeCode;

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private BigDecimal meterReadingValue;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String errorCode;

    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String errorMessage;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getMeterReadingDocumentId() {
        return meterReadingDocumentId;
    }

    public void setMeterReadingDocumentId(String meterReadingDocumentId) {
        this.meterReadingDocumentId = meterReadingDocumentId;
    }

    public String getLrn() {
        return lrn;
    }

    public void setLrn(String lrn) {
        this.lrn = lrn;
    }

    public String getReadingReasonCode() {
        return readingReasonCode;
    }

    public void setReadingReasonCode(String readingReasonCode) {
        this.readingReasonCode = readingReasonCode;
    }

    public Instant getMeterReadingDateTime() {
        return meterReadingDateTime;
    }

    public void setMeterReadingDateTime(Instant meterReadingDateTime) {
        this.meterReadingDateTime = meterReadingDateTime;
    }

    public String getMeterReadingTypeCode() {
        return meterReadingTypeCode;
    }

    public void setMeterReadingTypeCode(String meterReadingTypeCode) {
        this.meterReadingTypeCode = meterReadingTypeCode;
    }

    public BigDecimal getMeterReadingValue() {
        return meterReadingValue;
    }

    public void setMeterReadingValue(BigDecimal meterReadingValue) {
        this.meterReadingValue = meterReadingValue;
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

    public ServiceCall getServiceCall() {
        return serviceCall.get();
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setDeviceId((String) propertyValues.getProperty(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.DEVICE_ID.javaName()));
        this.setLrn((String) propertyValues.getProperty(FieldNames.LRN.javaName()));
        this.setReadingReasonCode((String) propertyValues.getProperty(MeterReadingDocumentCreateRequestDomainExtension.FieldNames.READING_REASON_CODE.javaName()));
        this.setMeterReadingDocumentId((String) propertyValues.getProperty(FieldNames.METER_READING_DOCUMENT_ID.javaName()));
        this.setMeterReadingDateTime((Instant) propertyValues.getProperty(FieldNames.METER_READING_DATE_TIME.javaName()));
        this.setMeterReadingTypeCode((String) propertyValues.getProperty(FieldNames.METER_READING_TYPE_CODE.javaName()));
        this.setMeterReadingValue(new BigDecimal(Optional.ofNullable(propertyValues.getProperty(FieldNames.METER_READING_VALUE.javaName()))
                .orElse(BigDecimal.ZERO).toString()));
        this.setErrorCode((String) propertyValues.getProperty(FieldNames.ERROR_CODE.javaName()));
        this.setErrorMessage((String) propertyValues.getProperty(FieldNames.ERROR_MESSAGE.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.DEVICE_ID.javaName(), this.getDeviceId());
        propertySetValues.setProperty(FieldNames.READING_REASON_CODE.javaName(), this.getReadingReasonCode());
        propertySetValues.setProperty(FieldNames.METER_READING_DOCUMENT_ID.javaName(), this.getMeterReadingDocumentId());
        propertySetValues.setProperty(FieldNames.LRN.javaName(), this.getLrn());
        propertySetValues.setProperty(FieldNames.METER_READING_DATE_TIME.javaName(), this.getMeterReadingDateTime());
        propertySetValues.setProperty(FieldNames.METER_READING_TYPE_CODE.javaName(), this.getMeterReadingTypeCode());
        propertySetValues.setProperty(FieldNames.METER_READING_VALUE.javaName(), this.getMeterReadingValue());
        propertySetValues.setProperty(FieldNames.ERROR_CODE.javaName(), this.getErrorCode());
        propertySetValues.setProperty(FieldNames.ERROR_MESSAGE.javaName(), this.getErrorMessage());
    }

    @Override
    public void validateDelete() {

    }

}
