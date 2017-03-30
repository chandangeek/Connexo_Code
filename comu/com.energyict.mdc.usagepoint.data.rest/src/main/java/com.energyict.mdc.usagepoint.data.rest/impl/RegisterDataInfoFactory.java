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
     * @return {@link RegisterDataInfo} info object
     */
    public RegisterDataInfo asInfo(RegisterReadingWithValidationStatus readingRecord, RangeMap<Instant, Instant> lastChecked) {
        return RegisterType.determineRegisterType(readingRecord)
                .getRegisterDataInfo(readingQualityInfoFactory, lastChecked);
    }

    private enum RegisterType {
        CUMULATIVE_VALUE {
            @Override
            void createRegisterDataInfo(RegisterType registerType) {
                registerType.setMeasurementPeriod();
                registerType.setCollectedValue();
                registerType.setDeltaValue();
            }
        },
        NOT_CUMULATIVE_VALUE {
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

        private void setMeasurementTime() {
            registerDataInfo.measurementTime = reading.getTimeStamp();
        }

        private void setCollectedValue() {
            registerDataInfo.collectedValue = reading.getValue();
        }

        private void setDeltaValue() {
            BigDecimal delta = null;
            BigDecimal value = reading.getValue();
            BigDecimal previousValue = reading.getPreviousReadingRecord().map(ReadingRecord::getValue).orElse(null);
            if (value != null && previousValue != null) {
                delta = value.subtract(previousValue);
            }
            registerDataInfo.deltaValue = delta;
        }

        private void setEventDate() {
            if (registerDataInfo.hasEvent) {
                registerDataInfo.eventDate = reading.getReadingRecord().getTimeStamp();
            }
        }

        private void setReadingQualities(ReadingQualityInfoFactory readingQualityInfoFactory) {
            registerDataInfo.readingQualities = readingQualityInfoFactory.asInfos(reading.getReadingQualities());
        }

        private void setValidationResult(RangeMap<Instant, Instant> lastCheckedMap) {
            Optional<Instant> lastChecked = Optional.ofNullable(lastCheckedMap.get(reading.getReadingRecord()
                    .getTimeStamp()));
            registerDataInfo.validationResult = reading.getValidationStatus(lastChecked.orElse(Instant.MIN));
        }

        private RegisterDataInfo registerDataInfo;
        private RegisterReadingWithValidationStatus reading;

        private static final List<Aggregate> aggregatesWithEventDate = Arrays.asList(Aggregate.MAXIMUM, Aggregate.FIFTHMAXIMIMUM,
                Aggregate.FOURTHMAXIMUM, Aggregate.MINIMUM, Aggregate.SECONDMAXIMUM, Aggregate.SECONDMINIMUM, Aggregate.THIRDMAXIMUM);


        public static RegisterType determineRegisterType(RegisterReadingWithValidationStatus reading) {

            ReadingType readingType = reading.getReadingRecord().getReadingType();

            boolean isCumulative = readingType.isCumulative();
            boolean hasEvent = aggregatesWithEventDate.contains(readingType.getAggregate());
            boolean isBilling = readingType.getMacroPeriod().equals(MacroPeriod.BILLINGPERIOD);

            RegisterType registerType;

            // event register block
            if (isCumulative && hasEvent && isBilling) {
                registerType = CUMULATIVE_EVENT_BILLING_VALUE;
            } else if (hasEvent && isBilling) {
                registerType = EVENT_BILLING_VALUE;
            } else if (hasEvent) {
                registerType = EVENT_VALUE;
            }

            // billing register block
            if (!isCumulative && isBilling) {
                registerType = NOT_CUMULATIVE_BILLING_VALUE;
            } else if (isCumulative && isBilling) {
                registerType = CUMULATIVE_BILLING_VALUE;
            }

            // other
            if (isCumulative) {
                registerType = CUMULATIVE_VALUE;
            } else {
                registerType = NOT_CUMULATIVE_VALUE;
            }

            return registerType.withReading(reading)
                    .withRegisterDataInfo(isCumulative, hasEvent, isBilling);
        }

        public RegisterDataInfo getRegisterDataInfo(ReadingQualityInfoFactory readingQualityInfoFactory, RangeMap<Instant, Instant> lastChecked) {
            createRegisterDataInfo(this);
            setReadingQualities(readingQualityInfoFactory);
            setValidationResult(lastChecked);
            return registerDataInfo;
        }

        abstract void createRegisterDataInfo(RegisterType registerType);
    }
}
