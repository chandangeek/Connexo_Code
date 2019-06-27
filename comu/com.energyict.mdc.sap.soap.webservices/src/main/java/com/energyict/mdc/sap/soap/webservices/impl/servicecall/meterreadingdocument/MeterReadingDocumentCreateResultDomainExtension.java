/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public class MeterReadingDocumentCreateResultDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {
    public enum FieldNames {
        DOMAIN("serviceCall", "serviceCall"),
        PARENT_SERVICE_CALL("parentServiceCallId", "parentServiceCallId"),
        METER_READING_DOCUMENT_ID("meterReadingDocumentId", "meterReadingDocumentId"),
        DEVICE_ID("deviceId", "deviceId"),
        DEVICE_NAME("deviceName", "deviceName"),
        LRN("lrn", "lrn"),
        READING_REASON_CODE("readingReasonCode", "readingReasonCode"),
        SCHEDULED_READING_DATE("scheduledReadingDate", "scheduledReadingDate"),
        CHANNEL_ID("channelId", "channelId"),
        DATA_SOURCE("dataSource", "dataSource"),
        FUTURE_CASE("futureCase", "futureCase"),
        PROCESSING_DATE("processingDate", "processingDate"),
        ACTUAL_READING_DATE("actualReadingDate", "actualReadingDate"),
        READING("reading", "reading");

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
    private BigDecimal parentServiceCallId;

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String meterReadingDocumentId;

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String deviceId;

    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String deviceName;

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String lrn;

    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String readingReasonCode;

    private Instant scheduledReadingDate;
    private BigDecimal channelId;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String dataSource;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private boolean futureCase;
    private Instant processingDate;
    private Instant actualReadingDate;
    private BigDecimal reading;

    public MeterReadingDocumentCreateResultDomainExtension() {
        super();
    }

    public BigDecimal getParentServiceCallId() {
        return parentServiceCallId;
    }

    public void setParentServiceCallId(BigDecimal parentServiceCallId) {
        this.parentServiceCallId = parentServiceCallId;
    }

    public String getMeterReadingDocumentId() {
        return meterReadingDocumentId;
    }

    public void setMeterReadingDocumentId(String meterReadingDocumentId) {
        this.meterReadingDocumentId = meterReadingDocumentId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
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

    public Instant getScheduledReadingDate() {
        return scheduledReadingDate;
    }

    public void setScheduledReadingDate(Instant scheduledReadingDate) {
        this.scheduledReadingDate = scheduledReadingDate;
    }

    public BigDecimal getChannelId() {
        return channelId;
    }

    public void setChannelId(BigDecimal channelId) {
        this.channelId = channelId;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public boolean isFutureCase() {
        return futureCase;
    }

    public void setFutureCase(boolean futureCase) {
        this.futureCase = futureCase;
    }

    public Instant getProcessingDate() {
        return processingDate;
    }

    public void setProcessingDate(Instant processingDate) {
        this.processingDate = processingDate;
    }

    public Instant getActualReadingDate() {
        return actualReadingDate;
    }

    public void setActualReadingDate(Instant actualReadingDate) {
        this.actualReadingDate = actualReadingDate;
    }

    public BigDecimal getReading() {
        return reading;
    }

    public void setReading(BigDecimal reading) {
        this.reading = reading;
    }

    @Override
    public void copyFrom(ServiceCall serviceCall, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(serviceCall);
        this.setParentServiceCallId(new BigDecimal(Optional.ofNullable(propertyValues.getProperty(FieldNames.PARENT_SERVICE_CALL.javaName()))
                .orElse(BigDecimal.ZERO).toString()));
        this.setMeterReadingDocumentId((String) propertyValues.getProperty(FieldNames.METER_READING_DOCUMENT_ID.javaName()));
        this.setDeviceId((String) propertyValues.getProperty(FieldNames.DEVICE_ID.javaName()));
        this.setDeviceName((String) propertyValues.getProperty(FieldNames.DEVICE_NAME.javaName()));
        this.setLrn((String) propertyValues.getProperty(FieldNames.LRN.javaName()));
        this.setReadingReasonCode((String) propertyValues.getProperty(FieldNames.READING_REASON_CODE.javaName()));
        this.setScheduledReadingDate((Instant) propertyValues.getProperty(FieldNames.SCHEDULED_READING_DATE.javaName()));
        this.setProcessingDate((Instant) propertyValues.getProperty(FieldNames.PROCESSING_DATE.javaName()));
        this.setChannelId((BigDecimal) propertyValues.getProperty(FieldNames.CHANNEL_ID.javaName()));
        this.setDataSource((String) propertyValues.getProperty(FieldNames.DATA_SOURCE.javaName()));
        this.setFutureCase((Boolean) propertyValues.getProperty(FieldNames.FUTURE_CASE.javaName()));
        this.setActualReadingDate((Instant) propertyValues.getProperty(FieldNames.ACTUAL_READING_DATE.javaName()));
        this.setReading((BigDecimal) propertyValues.getProperty(FieldNames.READING.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.PARENT_SERVICE_CALL.javaName(), this.getParentServiceCallId());
        propertySetValues.setProperty(FieldNames.METER_READING_DOCUMENT_ID.javaName(), this.getMeterReadingDocumentId());
        propertySetValues.setProperty(FieldNames.DEVICE_ID.javaName(), this.getDeviceId());
        propertySetValues.setProperty(FieldNames.DEVICE_NAME.javaName(), this.getDeviceName());
        propertySetValues.setProperty(FieldNames.LRN.javaName(), this.getLrn());
        propertySetValues.setProperty(FieldNames.READING_REASON_CODE.javaName(), this.getReadingReasonCode());
        propertySetValues.setProperty(FieldNames.SCHEDULED_READING_DATE.javaName(), this.getScheduledReadingDate());
        propertySetValues.setProperty(FieldNames.PROCESSING_DATE.javaName(), this.getProcessingDate());
        propertySetValues.setProperty(FieldNames.CHANNEL_ID.javaName(), this.getChannelId());
        propertySetValues.setProperty(FieldNames.DATA_SOURCE.javaName(), this.getDataSource());
        propertySetValues.setProperty(FieldNames.FUTURE_CASE.javaName(), this.isFutureCase());
        propertySetValues.setProperty(FieldNames.ACTUAL_READING_DATE.javaName(), this.getActualReadingDate());
        propertySetValues.setProperty(FieldNames.READING.javaName(), this.getReading());
    }

    @Override
    public void validateDelete() {
    }
}