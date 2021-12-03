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
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Optional;

public class MeterReadingDocumentCreateRequestDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall> {
    public enum FieldNames {
        // general
        DOMAIN("serviceCall", "serviceCall"),
        PARENT_SERVICE_CALL("parentServiceCallId", "parentServiceCallId"),

        // provided
        METER_READING_DOCUMENT_ID("meterReadingDocumentId", "meterReadingDocumentId"),
        DEVICE_ID("deviceId", "deviceIdentifier"),
        LRN("lrn", "lrnId"),
        READING_REASON_CODE("readingReasonCode", "readingReasonCode"),
        SCHEDULED_READING_DATE("scheduledReadingDate", "scheduledReadingDate"),
        DATA_SOURCE_TYPE_CODE("dataSourceTypeCode", "dataSourceTypeCode"),

        // calculated
        DEVICE_NAME("deviceName", "deviceName"),
        CHANNEL_ID("channelId", "channelId"),
        DATA_SOURCE("dataSource", "dataSource"),
        EXTRA_DATA_SOURCE("extraDataSource", "EXTRA_DATA_SOURCE"),
        FUTURE_CASE("futureCase", "futureCase"),
        PROCESSING_DATE("processingDate", "processingDate"),
        CANCELLED_BY_SAP("cancelledBySap", "cancelledBySap"),

        REFERENCE_ID("referenceID", "REFERENCE_ID"),
        REFERENCE_UUID("referenceUuid", "REFERENCE_UUID"),

        REQUESTED_SCHEDULED_READING_DATE("requestedScheduledReadingDate", "REQUESTED_READING_DATE"),

        ERROR_MESSAGE("errorMessage", "ERROR_MESSAGE");

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
    @NotNull(message = "{" + MessageSeeds.Keys.THIS_FIELD_IS_REQUIRED + "}")
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
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String cancelledBySap;

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String referenceID;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String referenceUuid;

    private Instant requestedScheduledReadingDate;

    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String errorMessage;


    public MeterReadingDocumentCreateRequestDomainExtension() {
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setErrorMessage(MessageSeed messageSeed, Object... args) {
        this.errorMessage = MessageFormat.format(messageSeed.getDefaultFormat(), args);
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
        this.setFutureCase((Boolean) propertyValues.getProperty(FieldNames.FUTURE_CASE.javaName()));
        this.setProcessingDate((Instant) propertyValues.getProperty(FieldNames.PROCESSING_DATE.javaName()));
        this.setDataSource((String) propertyValues.getProperty(FieldNames.DATA_SOURCE.javaName()));
        this.setExtraDataSource((String) propertyValues.getProperty(FieldNames.EXTRA_DATA_SOURCE.javaName()));
        this.setChannelId((BigDecimal) propertyValues.getProperty(FieldNames.CHANNEL_ID.javaName()));
        this.setCancelledBySap((String) propertyValues.getProperty(FieldNames.CANCELLED_BY_SAP.javaName()));
        this.setReferenceID((String) propertyValues.getProperty(FieldNames.REFERENCE_ID.javaName()));
        this.setReferenceUuid((String) propertyValues.getProperty(FieldNames.REFERENCE_UUID.javaName()));
        this.setRequestedScheduledReadingDate((Instant) propertyValues.getProperty(FieldNames.REQUESTED_SCHEDULED_READING_DATE.javaName()));
        this.setErrorMessage((String) propertyValues.getProperty(FieldNames.ERROR_MESSAGE.javaName()));
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
        propertySetValues.setProperty(FieldNames.FUTURE_CASE.javaName(), this.isFutureCase());
        propertySetValues.setProperty(FieldNames.PROCESSING_DATE.javaName(), this.getProcessingDate());
        propertySetValues.setProperty(FieldNames.DATA_SOURCE.javaName(), this.getDataSource());
        propertySetValues.setProperty(FieldNames.EXTRA_DATA_SOURCE.javaName(), this.getExtraDataSource());
        propertySetValues.setProperty(FieldNames.CHANNEL_ID.javaName(), this.getChannelId());
        propertySetValues.setProperty(FieldNames.CANCELLED_BY_SAP.javaName(), this.getCancelledBySap());
        propertySetValues.setProperty(FieldNames.REFERENCE_ID.javaName(), this.getReferenceID());
        propertySetValues.setProperty(FieldNames.REFERENCE_UUID.javaName(), this.getReferenceUuid());
        propertySetValues.setProperty(FieldNames.REQUESTED_SCHEDULED_READING_DATE.javaName(), this.getRequestedScheduledReadingDate());
        propertySetValues.setProperty(FieldNames.ERROR_MESSAGE.javaName(), this.getErrorMessage());
    }

    @Override
    public void validateDelete() {
    }

    public ServiceCall getServiceCall() {
        return serviceCall.get();
    }
}
