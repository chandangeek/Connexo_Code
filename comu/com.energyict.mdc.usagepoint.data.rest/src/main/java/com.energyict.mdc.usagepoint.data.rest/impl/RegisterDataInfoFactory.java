/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Factory to create {@link RegisterDataInfo} objects
 */
public class RegisterDataInfoFactory {

    private final ReadingQualityInfoFactory readingQualityInfoFactory;

    @Inject
    public RegisterDataInfoFactory(ReadingQualityInfoFactory readingQualityInfoFactory) {
        this.readingQualityInfoFactory = readingQualityInfoFactory;
    }

    /**
     * Represents {@link RegisterReadingWithValidationStatus} as {@link RegisterDataInfo}
     *
     * @param readingRecord {@link RegisterReadingWithValidationStatus} object to be represented as {@link RegisterDataInfo}
     * @param lastChecked {@link RangeMap} representing last checks for register
     * @return {@link RegisterDataInfo} info object
     */
    RegisterDataInfo asInfo(RegisterReadingWithValidationStatus readingRecord, RangeMap<Instant, Instant> lastChecked) {
        return RegisterType.determineRegisterType(readingRecord)
                .getRegisterDataInfo(readingQualityInfoFactory, lastChecked);
    }

    /*
     * RegisterType enum defines all possible (supported) register types
     * Type of register depends on register reading type and defined by the following attributes
     *    Is reading type:
     *       - cumulative
     *       - having event
     *       - billing
     *
     * Each enum value have to override createRegisterDataInfo method to fill internal registerDataInfo object
     * by required data. It uses internal functions, that can fill any registerDataInfo field.
     */
    enum RegisterType {
        CUMULATIVE_VALUE {
            /*
            this type will produce registerDataInfo containing measurement period, collected value and delta value
             */
            @Override
            void createRegisterDataInfo(RegisterType registerType) {
                registerType.setMeasurementPeriod();
                registerType.setCollectedValue();
                registerType.setDeltaValue();
            }
        },
        NOT_CUMULATIVE_VALUE {
            /*
            this type will produce registerDataInfo containing measurement period and collected value
             */
            @Override
            void createRegisterDataInfo(RegisterType registerType) {
                registerType.setMeasurementTime();
                registerType.setCollectedValue();
            }
        },
        CUMULATIVE_BILLING_VALUE {
            @Override
            void createRegisterDataInfo(RegisterType registerType) {
                registerType.setMeasurementPeriod();
                registerType.setCollectedValue();
                registerType.setDeltaValue();
            }
        },
        NOT_CUMULATIVE_BILLING_VALUE {
            @Override
            void createRegisterDataInfo(RegisterType registerType) {
                registerType.setMeasurementPeriod();
                registerType.setCollectedValue();
            }
        },
        EVENT_VALUE {
            @Override
            void createRegisterDataInfo(RegisterType registerType) {
                registerType.setEventDate();
                registerType.setMeasurementTime();
                registerType.setCollectedValue();
            }
        },
        EVENT_BILLING_VALUE {
            @Override
            void createRegisterDataInfo(RegisterType registerType) {
                registerType.setMeasurementPeriod();
                registerType.setEventDate();
                registerType.setCollectedValue();
            }
        },
        CUMULATIVE_EVENT_BILLING_VALUE {
            @Override
            void createRegisterDataInfo(RegisterType registerType) {
                registerType.setMeasurementPeriod();
                registerType.setCollectedValue();
                registerType.setDeltaValue();
            }
        };



        // internal RegisterDataInfo that will be created by enum instance
        private RegisterDataInfo registerDataInfo;

        private RegisterReadingWithValidationStatus reading;
        private static final List<Aggregate> aggregatesWithEventDate = Arrays.asList(Aggregate.MAXIMUM, Aggregate.FIFTHMAXIMIMUM,
                Aggregate.FOURTHMAXIMUM, Aggregate.MINIMUM, Aggregate.SECONDMAXIMUM, Aggregate.SECONDMINIMUM, Aggregate.THIRDMAXIMUM);

        /*
            each type will put required data into registerDataInfo object
         */
        abstract void createRegisterDataInfo(RegisterType registerType);

        private RegisterType withReading(RegisterReadingWithValidationStatus reading) {
            this.reading = reading;
            return this;
        }

        private RegisterType withRegisterDataInfo(boolean isCumulative, boolean hasEvent, boolean isBilling) {
            RegisterDataInfo registerDataInfo = new RegisterDataInfo();
            registerDataInfo.isCumulative = isCumulative;
            registerDataInfo.hasEvent = hasEvent;
            registerDataInfo.isBilling = isBilling;
            this.registerDataInfo = registerDataInfo;
            return this;
        }

        /**
         * Method to set measurement period value into registerDataInfo object
         */
        private void setMeasurementPeriod() {
            Range<Instant> range = null;
            if (registerDataInfo.isCumulative) {

                Optional<ReadingRecord> previousReadingRecord = reading.getPreviousReadingRecord();

                Instant actualReadingUpperInstant;
                if (reading.getReadingRecord().getTimePeriod().isPresent()) {
                    actualReadingUpperInstant = reading.getReadingRecord().getTimePeriod().get().upperEndpoint();
                } else {
                    actualReadingUpperInstant = reading.getReadingRecord().getTimeStamp();
                }

                range = previousReadingRecord.map(readingRecord -> Range.openClosed(previousReadingRecord.get()
                        .getTimeStamp(), actualReadingUpperInstant)).orElse(Range.atMost(actualReadingUpperInstant));

            } else if (registerDataInfo.isBilling) {
                range = reading.getReadingRecord().getTimePeriod().orElse(null);
            }

            registerDataInfo.measurementPeriod = MeasurementPeriod.from(range);
        }

        /**
         * Method to set measurement time value into registerDataInfo object
         */
        private void setMeasurementTime() {
            registerDataInfo.measurementTime = reading.getTimeStamp();
        }

        /**
         * Method to set collected value into registerDataInfo object
         */
        private void setCollectedValue() {
            registerDataInfo.collectedValue = reading.getValue();
        }

        /**
         * Method to set delta value into registerDataInfo object
         */
        private void setDeltaValue() {
            BigDecimal delta = null;
            BigDecimal value = reading.getValue();
            BigDecimal previousValue = reading.getPreviousReadingRecord().map(ReadingRecord::getValue).orElse(null);
            if (value != null && previousValue != null) {
                delta = value.subtract(previousValue);
            }
            registerDataInfo.deltaValue = delta;
        }

        /**
         * Method to set event date into registerDataInfo object
         */
        private void setEventDate() {
            if (registerDataInfo.hasEvent) {
                registerDataInfo.eventDate = reading.getReadingRecord().getTimeStamp();
            }
        }

        /**
         * Method to set reading qualities into registerDataInfo object
         */
        private void setReadingQualities(ReadingQualityInfoFactory readingQualityInfoFactory) {
            registerDataInfo.readingQualities = readingQualityInfoFactory.asInfos(reading.getReadingQualities());
        }

        /**
         * Method to set validation result into registerDataInfo object
         */
        private void setValidationResult(RangeMap<Instant, Instant> lastCheckedMap) {
            Optional<Instant> lastChecked = Optional.ofNullable(lastCheckedMap.get(reading.getReadingRecord()
                    .getTimeStamp()));
            registerDataInfo.validationResult = reading.getValidationStatus(lastChecked.orElse(Instant.MIN));
        }


        /**
         * Method to create {@link RegisterDataInfo}
         *
         * @param readingQualityInfoFactory {@link ReadingQualityInfoFactory} factory to create info objects for reading qualities
         * @param lastChecked {@link RangeMap} representing last checks for register
         * @return {@link RegisterDataInfo} info object
         */
        public RegisterDataInfo getRegisterDataInfo(ReadingQualityInfoFactory readingQualityInfoFactory, RangeMap<Instant, Instant> lastChecked) {
            createRegisterDataInfo(this);
            setReadingQualities(readingQualityInfoFactory);
            setValidationResult(lastChecked);
            return registerDataInfo;
        }

        /**
         * Method to create appropriate {@link RegisterType} instance by using data from {@link RegisterReadingWithValidationStatus}
         *
         * @param reading {@link RegisterReadingWithValidationStatus} object representing data about register reading
         * @return {@link RegisterType} instance
         */
        public static RegisterType determineRegisterType(RegisterReadingWithValidationStatus reading) {

            ReadingType readingType = reading.getReadingRecord().getReadingType();

            boolean isCumulative = readingType.isCumulative();
            boolean hasEvent = aggregatesWithEventDate.contains(readingType.getAggregate());
            boolean isBilling = readingType.getMacroPeriod().equals(MacroPeriod.BILLINGPERIOD);

            RegisterType registerType = null;

            // event register block
            if (isCumulative && hasEvent && isBilling) {
                registerType = CUMULATIVE_EVENT_BILLING_VALUE;
            }
            if (hasEvent && isBilling && registerType == null) {
                registerType = EVENT_BILLING_VALUE;
            }
            if (hasEvent && registerType == null) {
                registerType = EVENT_VALUE;
            }

            // billing register block
            if (!isCumulative && isBilling && registerType == null) {
                registerType = NOT_CUMULATIVE_BILLING_VALUE;
            }
            if (isCumulative && isBilling && registerType == null) {
                registerType = CUMULATIVE_BILLING_VALUE;
            }

            // other
            if (isCumulative && registerType == null) {
                registerType = CUMULATIVE_VALUE;
            }

            if (registerType == null){
                registerType = NOT_CUMULATIVE_VALUE;
            }

            return registerType.withReading(reading)
                    .withRegisterDataInfo(isCumulative, hasEvent, isBilling);
        }
    }
}
