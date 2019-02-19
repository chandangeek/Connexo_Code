/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentCollectionData;
import com.energyict.mdc.sap.soap.webservices.impl.AdditionalProperties;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateResultDomainExtension;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SAPMeterReadingDocumentCollectionDataBuilder implements SAPMeterReadingDocumentCollectionData {

    private final MeteringService meteringService;

    private Integer readindCollectionInterval;
    private Integer readingDateWindow;
    private Instant scheduledReadingDate;
    private Optional<Channel> meterChannel;
    private Optional<ReadingType> meterReadingType;
    private ServiceCall serviceCall;
    private String deviceName;
    private boolean pastCase;

    private SAPMeterReadingDocumentCollectionDataBuilder(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public static SAPMeterReadingDocumentCollectionDataBuilder.Builder builder(MeteringService meteringService) {
        return new SAPMeterReadingDocumentCollectionDataBuilder(meteringService).new Builder();
    }

    public Integer getReadindCollectionInterval() {
        return readindCollectionInterval;
    }

    public Integer getReadingDateWindow() {
        return readingDateWindow;
    }

    public Instant getScheduledReadingDate() {
        return scheduledReadingDate;
    }

    public Optional<Channel> getMeterChannel() {
        return meterChannel;
    }

    public Optional<ReadingType> getMeterReadingType() {
        return meterReadingType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public boolean isPastCase() {
        return pastCase;
    }

    public boolean isRegular() {
        return getMeterChannel()
                .map(Channel::isRegular)
                .orElse(Boolean.FALSE);
    }

    public void calculate() {
        Optional<BaseReadingRecord> closestReadingRecord = getBaseReadingRecord(getReadings());
        serviceCall.getExtension(MeterReadingDocumentCreateResultDomainExtension.class)
                .ifPresent(domainExtension -> {
                    closestReadingRecord.ifPresent(record -> {
                        domainExtension.setReading(record.getValue());
                        domainExtension.setActualReadingDate(record.getTimeStamp());
                        serviceCall.update(domainExtension);
                        serviceCall.requestTransition(DefaultState.SUCCESSFUL);
                    });
                    if (!closestReadingRecord.isPresent()) {
                        serviceCall.requestTransition(DefaultState.FAILED);
                    }
                });
    }

    public ServiceCall getServiceCall() {
        return serviceCall;
    }

    private List<BaseReadingRecord> getReadings() {
        return getMeterReadingType()
                .map(this::getReadings)
                .orElse(new ArrayList<>());
    }

    private List<BaseReadingRecord> getReadings(ReadingType readingType) {
        return getMeterChannel()
                .flatMap(ch -> ch.getCimChannel(readingType))
                .map(this::getReadings)
                .orElse(new ArrayList<>());
    }

    private List<BaseReadingRecord> getReadings(CimChannel cimChannel) {
        return cimChannel.getReadings(Range
                .open(scheduledReadingDate.minusSeconds(getReadingDateWindow() * 60),
                        scheduledReadingDate.plusSeconds(getReadingDateWindow() * 60)));
    }

    private Optional<BaseReadingRecord> getBaseReadingRecord(List<BaseReadingRecord> baseReadingRecords) {
        return baseReadingRecords.isEmpty()
                ? Optional.empty()
                : Optional.ofNullable(Collections.min(baseReadingRecords, (r1, r2) -> {
            long diff1 = Math.abs(r1.getTimeStamp().getEpochSecond() - scheduledReadingDate.getEpochSecond());
            long diff2 = Math.abs(r2.getTimeStamp().getEpochSecond() - scheduledReadingDate.getEpochSecond());
            return Long.compare(diff1, diff2);
        }));
    }

    public class Builder {

        private Builder() {
        }

        public SAPMeterReadingDocumentCollectionDataBuilder.Builder from(ServiceCall serviceCall,
                                                                         Map<AdditionalProperties, Integer> properties) {
            setServiceCall(serviceCall);
            serviceCall.getExtension(MeterReadingDocumentCreateResultDomainExtension.class)
                    .ifPresent(domainExtension -> {
                        setDeviceName(domainExtension.getDeviceName());
                        setMeterChannel(domainExtension.getChannelId().longValue());
                        setMeterReadingType(domainExtension.getDataSource());
                        setScheduledReadingDate(domainExtension.getScheduledReadingDate());
                        setReadindCollectionInterval(properties.get(AdditionalProperties.READING_COLLECTION_INTERVAL));
                        setReadingDateWindow(properties.get(AdditionalProperties.READING_DATE_WINDOW));
                        setPastCase(domainExtension.isFutureCase());
                    });
            return this;
        }

        private SAPMeterReadingDocumentCollectionDataBuilder.Builder setDeviceName(String deviceName) {
            SAPMeterReadingDocumentCollectionDataBuilder.this.deviceName = deviceName;
            return this;
        }

        private SAPMeterReadingDocumentCollectionDataBuilder.Builder setServiceCall(ServiceCall serviceCall) {
            SAPMeterReadingDocumentCollectionDataBuilder.this.serviceCall = serviceCall;
            return this;
        }

        private SAPMeterReadingDocumentCollectionDataBuilder.Builder setMeterChannel(long id) {
            SAPMeterReadingDocumentCollectionDataBuilder.this.meterChannel = meteringService.findChannel(id);
            return this;
        }

        private SAPMeterReadingDocumentCollectionDataBuilder.Builder setMeterReadingType(String dataSource) {
            SAPMeterReadingDocumentCollectionDataBuilder.this.meterReadingType = meteringService
                    .findReadingTypes(Collections.singletonList(dataSource))
                    .stream()
                    .findFirst();
            return this;
        }

        private SAPMeterReadingDocumentCollectionDataBuilder.Builder setScheduledReadingDate(Instant scheduledReadingDate) {
            SAPMeterReadingDocumentCollectionDataBuilder.this.scheduledReadingDate = scheduledReadingDate;
            return this;
        }

        private SAPMeterReadingDocumentCollectionDataBuilder.Builder setReadindCollectionInterval(Integer readindCollectionInterval) {
            SAPMeterReadingDocumentCollectionDataBuilder.this.readindCollectionInterval = readindCollectionInterval;
            return this;
        }

        private SAPMeterReadingDocumentCollectionDataBuilder.Builder setReadingDateWindow(Integer readingDateWindow) {
            SAPMeterReadingDocumentCollectionDataBuilder.this.readingDateWindow = readingDateWindow;
            return this;
        }

        private SAPMeterReadingDocumentCollectionDataBuilder.Builder setPastCase(boolean isFutureCase) {
            SAPMeterReadingDocumentCollectionDataBuilder.this.pastCase = !isFutureCase;
            return this;
        }

        public SAPMeterReadingDocumentCollectionDataBuilder build() {
            return SAPMeterReadingDocumentCollectionDataBuilder.this;
        }
    }
}