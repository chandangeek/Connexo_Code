/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.NumericalRegister;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentCollectionData;
import com.energyict.mdc.sap.soap.webservices.impl.AdditionalProperties;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateResultDomainExtension;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SAPMeterReadingDocumentCollectionDataBuilder implements SAPMeterReadingDocumentCollectionData {

    private final MeteringService meteringService;
    private final Clock clock;
    private final Map<AdditionalProperties, Integer> properties;
    private final DeviceService deviceService;

    private Integer readindCollectionInterval;
    private int readingDateWindow;
    private int backwardReadingDateWindow;
    private Instant scheduledReadingDate;
    private Optional<Channel> meterChannel;
    private Optional<ReadingType> meterReadingType;
    private Optional<ReadingType> extraMeterReadingType;
    private ServiceCall serviceCall;
    private String deviceName;
    private boolean pastCase;
    private boolean isExtraDataSource = false;

    private SAPMeterReadingDocumentCollectionDataBuilder(MeteringService meteringService, Clock clock,
                                                         Map<AdditionalProperties, Integer> properties, DeviceService deviceService) {
        this.meteringService = meteringService;
        this.clock = clock;
        this.properties = properties;
        this.deviceService = deviceService;
    }

    public static SAPMeterReadingDocumentCollectionDataBuilder.Builder builder(MeteringService meteringService, Clock clock,
                                                                               Map<AdditionalProperties, Integer> properties, DeviceService deviceService) {
        return new SAPMeterReadingDocumentCollectionDataBuilder(meteringService, clock, properties, deviceService).new Builder();
    }

    public Integer getReadindCollectionInterval() {
        return readindCollectionInterval;
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

    public Optional<ReadingType> getExtraMeterReadingType() {
        return extraMeterReadingType;
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
        MeterReadingDocumentCreateResultDomainExtension domainExtension = serviceCall.getExtension(MeterReadingDocumentCreateResultDomainExtension.class).get();
        long currentAttempt = domainExtension.getReadingAttempt() + 1;
        domainExtension.setReadingAttempt(currentAttempt);

        if (closestReadingRecord.isPresent() && closestReadingRecord.get().getValue() != null) {
            domainExtension.setReading(getRoundedBigDecimal(closestReadingRecord.get().getValue()));
            domainExtension.setActualReadingDate(closestReadingRecord.get().getTimeStamp());
            serviceCall.update(domainExtension);
            serviceCall.transitionWithLockIfPossible(DefaultState.WAITING);
            if (isExtraDataSource) {
                serviceCall.log(LogLevel.INFO, "The reading is found on extra data source.");
            } else {
                serviceCall.log(LogLevel.INFO, "The reading is found on data source.");
            }
        } else {
            serviceCall.log(LogLevel.WARNING, "The reading isn't found.");
            long attempts = properties.get(AdditionalProperties.CHECK_SCHEDULED_READING_ATTEMPTS);

            if (currentAttempt != attempts) {
                domainExtension.setNextReadingAttemptDate(clock.instant()
                        .plusSeconds(properties.get(AdditionalProperties.CHECK_SCHEDULED_READING_INTERVAL) * 60));
                serviceCall.update(domainExtension);
                serviceCall.transitionWithLockIfPossible(DefaultState.PAUSED);
            } else {
                serviceCall.update(domainExtension);
                serviceCall.transitionWithLockIfPossible(DefaultState.WAITING);
            }
        }
    }

    private BigDecimal getRoundedBigDecimal(BigDecimal value) {
        Optional<Integer> numberOfFractionDigits = deviceService.findDeviceByName(getDeviceName())
                .map(device -> {
                    if (isRegular()) {
                        return device.getChannels().stream().filter(c -> c.getReadingType().equals(getMeterReadingType().get()))
                                .findFirst()
                                .map(com.energyict.mdc.common.device.data.Channel::getNrOfFractionDigits);
                    } else {
                        return device.getRegisters().stream().filter(r -> r.getReadingType().equals(getMeterReadingType().get()))
                                .findFirst()
                                .filter(r -> r instanceof NumericalRegister)
                                .map(NumericalRegister.class::cast)
                                .map(NumericalRegister::getNumberOfFractionDigits);
                    }
                }).orElse(Optional.empty());

        return numberOfFractionDigits.isPresent() ? value.setScale(numberOfFractionDigits.get(), BigDecimal.ROUND_UP) : value;
    }

    public ServiceCall getServiceCall() {
        return serviceCall;
    }

    private List<BaseReadingRecord> getReadings() {
        List<BaseReadingRecord> readings = getMeterReadingType()
                .map(this::getReadings)
                .orElseGet(ArrayList::new);

        if (!isRegular() && readings.isEmpty() && getExtraMeterReadingType().isPresent()) {
            isExtraDataSource = true;
            readings = getExtraMeterReadingType()
                    .map(this::getExtraReadings)
                    .orElseGet(ArrayList::new);
        }
        return readings;
    }

    private List<BaseReadingRecord> getExtraReadings(ReadingType readingType) {
        return getExtraMeterChannel(readingType)
                .flatMap(ch -> ch.getCimChannel(readingType))
                .map(this::getReadings)
                .orElse(new ArrayList<>());
    }

    private Optional<Channel> getExtraMeterChannel(ReadingType readingType) {
        return deviceService.findDeviceByName(getDeviceName())
                .flatMap(device -> device.getMeterActivation(scheduledReadingDate))
                .map(MeterActivation::getChannelsContainer)
                .flatMap(cc -> cc.getChannel(readingType));
    }

    private List<BaseReadingRecord> getReadings(ReadingType readingType) {
        return getMeterChannel()
                .flatMap(ch -> ch.getCimChannel(readingType))
                .map(this::getReadings)
                .orElse(new ArrayList<>());
    }

    private List<BaseReadingRecord> getReadings(CimChannel cimChannel) {
        return cimChannel.getReadings(Range
                .closed(scheduledReadingDate.minusSeconds(backwardReadingDateWindow * 60),
                        scheduledReadingDate.plusSeconds(readingDateWindow * 60)));
    }

    private Optional<BaseReadingRecord> getBaseReadingRecord(List<BaseReadingRecord> baseReadingRecords) {
        return baseReadingRecords.isEmpty()
                ? Optional.empty()
                : Optional.ofNullable(Collections.min(baseReadingRecords, (r1, r2) -> {
            long diff1 = Math.abs(r1.getTimeStamp().getEpochSecond() - scheduledReadingDate.getEpochSecond());
            long diff2 = Math.abs(r2.getTimeStamp().getEpochSecond() - scheduledReadingDate.getEpochSecond());
            int cmp = Long.compare(diff1, diff2);
            //if they are equidistant return the latest.
            if(cmp == 0){
                cmp = -1;
            }
            return cmp;
        }));
    }

    public class Builder {

        private Builder() {
        }

        public SAPMeterReadingDocumentCollectionDataBuilder.Builder from(ServiceCall serviceCall) {
            setServiceCall(serviceCall);
            serviceCall.getExtension(MeterReadingDocumentCreateResultDomainExtension.class)
                    .ifPresent(domainExtension -> {
                        setDeviceName(domainExtension.getDeviceName());
                        setMeterChannel(domainExtension.getChannelId().longValue());
                        setMeterReadingType(domainExtension.getDataSource());
                        setExtraMeterReadingType(domainExtension.getExtraDataSource());
                        setScheduledReadingDate(domainExtension.getScheduledReadingDate());
                        setReadindCollectionInterval(properties.get(AdditionalProperties.READING_COLLECTION_INTERVAL));
                        setReadingDateWindow(properties.get(AdditionalProperties.READING_DATE_WINDOW));
                        setBackwardReadingDateWindow(properties.get(AdditionalProperties.BACKWARD_READING_DATE_WINDOW));
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

        private SAPMeterReadingDocumentCollectionDataBuilder.Builder setExtraMeterReadingType(String extraDataSource) {
            if (extraDataSource != null) {
                SAPMeterReadingDocumentCollectionDataBuilder.this.extraMeterReadingType = meteringService
                        .findReadingTypes(Collections.singletonList(extraDataSource))
                        .stream()
                        .findFirst();
            } else {
                SAPMeterReadingDocumentCollectionDataBuilder.this.extraMeterReadingType = Optional.empty();
            }
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
            Optional.ofNullable(readingDateWindow).ifPresent(value -> SAPMeterReadingDocumentCollectionDataBuilder.this.readingDateWindow = value);
            return this;
        }

        private SAPMeterReadingDocumentCollectionDataBuilder.Builder setBackwardReadingDateWindow(Integer backwardReadingDateWindow) {
            Optional.ofNullable(backwardReadingDateWindow).ifPresent(value -> SAPMeterReadingDocumentCollectionDataBuilder.this.backwardReadingDateWindow = value);
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