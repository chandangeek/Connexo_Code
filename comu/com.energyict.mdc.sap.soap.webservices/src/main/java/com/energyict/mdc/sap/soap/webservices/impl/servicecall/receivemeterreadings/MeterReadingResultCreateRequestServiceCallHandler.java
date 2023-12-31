/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.receivemeterreadings;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Component(name = MeterReadingResultCreateRequestServiceCallHandler.NAME, service = ServiceCallHandler.class,
        property = "name=" + MeterReadingResultCreateRequestServiceCallHandler.NAME, immediate = true)
public class MeterReadingResultCreateRequestServiceCallHandler implements ServiceCallHandler {

    public static final String NAME = "MeterReadingResultCreateRequest";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";
    public static final String COLLECTED_VALUE = "01";
    public static final String CALCULATED_VALUE = "02";

    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile Clock clock;

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            case ONGOING:
                processServiceCall(serviceCall);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }


    private void processServiceCall(ServiceCall serviceCall) {
        MeterReadingResultCreateRequestDomainExtension extension = serviceCall.getExtensionFor(new MeterReadingResultCreateRequestCustomPropertySet())
                .orElseThrow(() -> new IllegalStateException("Can not find domain extension for parent service call"));

        Optional<com.elster.jupiter.metering.Channel> channel;
        Optional<Device> device = sapCustomPropertySets.getDevice(extension.getDeviceId());
        if (device.isPresent()) {
            if (device.get().getCreateTime().getEpochSecond() > extension.getMeterReadingDateTime().getEpochSecond()) {
                failServiceCall(extension, MessageSeeds.READING_DATE_AFTER_DEVICE_CREATION_DATETIME);
            } else {
                if (Arrays.stream(ReasonCodes.values()).noneMatch(e -> e.getValue().equals(extension.getReadingReasonCode()))) {
                    failServiceCall(extension, MessageSeeds.UNSUPPORTED_REASON_CODE);
                } else {
                    channel = sapCustomPropertySets.getChannel(extension.getLrn(), clock.instant());
                    if (channel.isPresent()) {
                        List<? extends ReadingType> readingTypeList = channel.get().getReadingTypes();
                        String readingTypeMrid = "";
                        switch (extension.getMeterReadingTypeCode()) {
                            case CALCULATED_VALUE:
                                if (readingTypeList.size() == 1) {
                                    failServiceCall(extension, MessageSeeds.READING_TYPE_FOR_REGISTER_INCORRECT);
                                } else {
                                    readingTypeMrid = readingTypeList.get(0).getMRID();
                                }
                                break;
                            case COLLECTED_VALUE:
                                readingTypeMrid = readingTypeList.get(readingTypeList.size() - 1).getMRID();
                                break;
                            default:
                                failServiceCall(extension, MessageSeeds.READING_TYPE_INCORRECT);
                                break;
                        }
                        if (!readingTypeMrid.isEmpty()) {
                            ReadingImpl reading = ReadingImpl.of(readingTypeMrid, extension.getMeterReadingValue(), extension.getMeterReadingDateTime());
                            reading.addQuality(ReadingQualityType.of(QualityCodeSystem.EXTERNAL, QualityCodeIndex.CUSTOMERREAD).getCode());
                            MeterReadingImpl meterReading = MeterReadingImpl.of(reading);
                            try {
                                device.get().store(meterReading, null);
                                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
                            } catch (IllegalArgumentException e) {
                                failServiceCall(extension, MessageSeeds.INVALID_READING_TIMESTAMP_FOR_CHANNEL, extension.getMeterReadingDateTime());
                            } catch (Exception e) {
                                failServiceCall(extension, MessageSeeds.ERROR_PROCESSING_RESULT_CREATE_REQUEST, e.getLocalizedMessage());
                            }
                        }
                    } else {
                        failServiceCall(extension, MessageSeeds.NO_DATA_SOURCE_FOUND_BY_LRN, extension.getLrn());
                    }
                }
            }
        } else {
            failServiceCall(extension, MessageSeeds.NO_DEVICE_FOUND_BY_SAP_ID, extension.getDeviceId());
        }
    }


    private void failServiceCall(MeterReadingResultCreateRequestDomainExtension extension, MessageSeed messageSeed, Object... args) {
        ServiceCall serviceCall = extension.getServiceCall();

        extension.setError(messageSeed, args);
        serviceCall.update(extension);
        serviceCall.requestTransition(DefaultState.FAILED);
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }


    enum ReasonCodes {
        FULL_INSTALLATION1("08"),
        FULL_INSTALLATION2("21"),
        FULL_REMOVAL1("12"),
        FULL_REMOVAL2("22"),
        ;

        ReasonCodes(String value) {
            this.value = value;

        }

        private final String value;

        public String getValue() {
            return value;
        }

    }
}
