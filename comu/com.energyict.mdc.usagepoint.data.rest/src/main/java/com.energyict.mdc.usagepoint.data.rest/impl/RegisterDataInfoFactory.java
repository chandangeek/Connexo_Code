/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.config.DeliverableType;
import com.elster.jupiter.validation.ValidationAction;

import com.energyict.mdc.device.data.NumericalRegister;

import com.google.common.collect.RangeMap;

import java.time.Instant;
import java.util.Optional;

/**
 * Factory to create {@link RegisterDataInfo} objects
 */
public class RegisterDataInfoFactory {

    /**
     * Represents {@link RegisterReadingWithValidationStatus} as {@link RegisterDataInfo}
     *
     * @param readingRecord {@link RegisterReadingWithValidationStatus} object to be represented as {@link RegisterDataInfo}
     * @param lastCheckedMap {@link RangeMap} map of last checked {@link Instant} entries
     * @return {@link RegisterDataInfo} info object
     */
    public RegisterDataInfo asInfo(RegisterReadingWithValidationStatus readingRecord,
                                   RangeMap<Instant, Instant> lastCheckedMap, DeliverableType deliverableType) {



        RegisterDataInfo registerDataInfo = null;
                /*new RegisterDataInfo();
        registerDataInfo.measurementTime = readingRecord.getTimeStamp();
        registerDataInfo.value = readingRecord.getValue();
        Optional<Instant> lastChecked = Optional.ofNullable(lastCheckedMap.get(readingRecord.getTimeStamp()));
        lastChecked.ifPresent(instant -> registerDataInfo.readingTime = readingRecord.getTimeStamp());
        registerDataInfo.validationResult = readingRecord.getValidationStatus(lastChecked.orElse(Instant.MIN));
        registerDataInfo.dataValidated = !readingRecord.getTimeStamp().isAfter(lastChecked.orElse(Instant.MIN));
        registerDataInfo.validationAction = ValidationAction.FAIL;*/




        return registerDataInfo;
    }

    private RegisterDataInfo createRegisterDataInfo(RegisterReadingWithValidationStatus registerReadingWithValidationStatus, DeliverableType deliverableType) {
        RegisterDataInfo registerDataInfo = null;
        switch (deliverableType) {
            case NUMERICAL:break;
            NumericalRegisterDataInfo numericalRegisterDataInfo = new NumericalRegisterDataInfo();
            numericalRegisterDataInfo.
            case TEXT: break;
            case BILLING:
                break;
            case FLAGS:
                break;
            default:

        }

        registerReadingWithValidationStatus.getReadingType();

    }
}
