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
        DEVICE_ID("deviceId", "deviceIdentifier"),
        DEVICE_NAME("deviceName", "deviceName"),
        LRN("lrn", "lrnId"),
        READING_REASON_CODE("readingReasonCode", "readingReasonCode"),
        SCHEDULED_READING_DATE("scheduledReadingDate", "scheduledReadingDate"),
        DATA_SOURCE_TYPE_CODE("dataSourceTypeCode", "dataSourceTypeCode"),
        CHANNEL_ID("channelId", "channelId"),
        DATA_SOURCE("dataSource", "dataSource"),
        EXTRA_DATA_SOURCE("extraDataSource", "EXTRA_DATA_SOURCE"),
        FUTURE_CASE("futureCase", "futureCase"),
        PROCESSING_DATE("processingDate", "processingDate"),
        NEXT_READING_ATTEMPT_DATE("nextReadingAttemptDate", "nextReadingAttemptDate"),
        READING_ATTEMPT("readingAttempt", "readingAttempt"),
        ACTUAL_READING_DATE("actualReadingDate", "actualReadingDate"),
        READING("reading", "reading"),
        CANCELLED_BY_SAP("cancelledBySap", "cancelledBySap"),
        COM_TASK_EXECUTION_ID("comTaskExecutionId", "COM_TASK_EXECUTION_ID"),
        REFERENCE_ID("referenceID", "REFERENCE_ID"),
        REFERENCE_UUID("referenceUuid", "REFERENCE_UUID"),
        REQUESTED_SCHEDULED_READING_DATE("requestedScheduledReadingDate", "REQUESTED_READING_DATE");

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

    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String dataSourceTypeCode;

    private BigDecimal channelId;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String dataSource;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String extraDataSource;
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
    private boolean futureCase;
    private Instant processingDate;
    private Instant nextReadingAttemptDate;
    private long readingAttempt;
    private Instant actualReadingDate;
    private BigDecimal reading;
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String cancelledBySap;
    private Long comTaskExecutionId;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String referenceID;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String referenceUuid;

    private Instant requestedScheduledReadingDate;

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

    public String getDataSourceTypeCode(){
        return dataSourceTypeCode;
    }

    public void setDataSourceTypeCode(String dataSourceTypeCode) {
        this.dataSourceTypeCode = dataSourceTypeCode;
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

    public String getExtraDataSource() {
        return extraDataSource;
    }

    public void setExtraDataSource(String extraDataSource) {
        this.extraDataSource = extraDataSource;
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

    public Instant getNextReadingAttemptDate() {
        return nextReadingAttemptDate;
    }

    public void setNextReadingAttemptDate(Instant nextReadingAttemptDate) {
        this.nextReadingAttemptDate = nextReadingAttemptDate;
    }

    public String getCancelledBySap() {
        return cancelledBySap;
    }

    public void setCancelledBySap(String cancelledBySap) {
        this.cancelledBySap = cancelledBySap;
    }

    public boolean isCancelledBySap() {
        return cancelledBySap != null && cancelledBySap.toLowerCase().equals("yes");
    }

    public void setCancelledBySap(boolean isCancelledBySap) {
        if (isCancelledBySap) {
            setCancelledBySap("Yes");
        } else {
            setCancelledBySap("No");
        }
    }

    public long getReadingAttempt() {
        return readingAttempt;
    }

    public void setReadingAttempt(long readingAttempt) {
        this.readingAttempt = readingAttempt;
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

    public Long getComTaskExecutionId() {
        return comTaskExecutionId;
    }

    public void setComTaskExecutionId(Long comTaskExecutionId) {
        this.comTaskExecutionId = comTaskExecutionId;
    }

    public String getReferenceID() {
        return referenceID;
    }

    public void setReferenceID(String referenceID) {
        this.referenceID = referenceID;
    }

    public String getReferenceUuid() {
        return referenceUuid;
    }

    public void setReferenceUuid(String referenceUuid) {
        this.referenceUuid = referenceUuid;
    }

    public Instant getRequestedScheduledReadingDate() {
        return requestedScheduledReadingDate;
    }

    public void setRequestedScheduledReadingDate(Instant requestedScheduledReadingDate) {
        this.requestedScheduledReadingDate = requestedScheduledReadingDate;
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
        this.setDataSourceTypeCode((String) propertyValues.getProperty(FieldNames.DATA_SOURCE_TYPE_CODE.javaName()));
        this.setProcessingDate((Instant) propertyValues.getProperty(FieldNames.PROCESSING_DATE.javaName()));
        this.setNextReadingAttemptDate((Instant) propertyValues.getProperty(FieldNames.NEXT_READING_ATTEMPT_DATE.javaName()));
        this.setReadingAttempt((long) propertyValues.getProperty(FieldNames.READING_ATTEMPT.javaName()));
        this.setChannelId((BigDecimal) propertyValues.getProperty(FieldNames.CHANNEL_ID.javaName()));
        this.setDataSource((String) propertyValues.getProperty(FieldNames.DATA_SOURCE.javaName()));
        this.setExtraDataSource((String) propertyValues.getProperty(FieldNames.EXTRA_DATA_SOURCE.javaName()));
        this.setFutureCase((Boolean) propertyValues.getProperty(FieldNames.FUTURE_CASE.javaName()));
        this.setActualReadingDate((Instant) propertyValues.getProperty(FieldNames.ACTUAL_READING_DATE.javaName()));
        this.setReading((BigDecimal) propertyValues.getProperty(FieldNames.READING.javaName()));
        this.setCancelledBySap((String) propertyValues.getProperty(FieldNames.CANCELLED_BY_SAP.javaName()));
        this.setComTaskExecutionId((Long) propertyValues.getProperty(FieldNames.COM_TASK_EXECUTION_ID.javaName()));
        this.setReferenceID((String) propertyValues.getProperty(FieldNames.REFERENCE_ID.javaName()));
        this.setReferenceUuid((String) propertyValues.getProperty(FieldNames.REFERENCE_UUID.javaName()));
        this.setRequestedScheduledReadingDate((Instant) propertyValues.getProperty(FieldNames.REQUESTED_SCHEDULED_READING_DATE.javaName()));
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
        propertySetValues.setProperty(FieldNames.DATA_SOURCE_TYPE_CODE.javaName(), this.getDataSourceTypeCode());
        propertySetValues.setProperty(FieldNames.PROCESSING_DATE.javaName(), this.getProcessingDate());
        propertySetValues.setProperty(FieldNames.NEXT_READING_ATTEMPT_DATE.javaName(), this.getNextReadingAttemptDate());
        propertySetValues.setProperty(FieldNames.READING_ATTEMPT.javaName(), this.getReadingAttempt());
        propertySetValues.setProperty(FieldNames.CHANNEL_ID.javaName(), this.getChannelId());
        propertySetValues.setProperty(FieldNames.DATA_SOURCE.javaName(), this.getDataSource());
        propertySetValues.setProperty(FieldNames.EXTRA_DATA_SOURCE.javaName(), this.getExtraDataSource());
        propertySetValues.setProperty(FieldNames.FUTURE_CASE.javaName(), this.isFutureCase());
        propertySetValues.setProperty(FieldNames.ACTUAL_READING_DATE.javaName(), this.getActualReadingDate());
        propertySetValues.setProperty(FieldNames.READING.javaName(), this.getReading());
        propertySetValues.setProperty(FieldNames.CANCELLED_BY_SAP.javaName(), this.getCancelledBySap());
        propertySetValues.setProperty(FieldNames.COM_TASK_EXECUTION_ID.javaName(), this.getComTaskExecutionId());
        propertySetValues.setProperty(FieldNames.REFERENCE_ID.javaName(), this.getReferenceID());
        propertySetValues.setProperty(FieldNames.REFERENCE_UUID.javaName(), this.getReferenceUuid());
        propertySetValues.setProperty(FieldNames.REQUESTED_SCHEDULED_READING_DATE.javaName(), this.getRequestedScheduledReadingDate());
    }

    @Override
    public void validateDelete() {
    }

    public ServiceCall getServiceCall() {
        return serviceCall.get();
    }
}