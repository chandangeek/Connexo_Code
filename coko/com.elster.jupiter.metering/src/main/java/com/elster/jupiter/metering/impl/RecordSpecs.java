/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.RecordSpecBuilder;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.nls.LocalizedFieldValidationException;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

import static com.elster.jupiter.ids.FieldType.INSTANT;
import static com.elster.jupiter.ids.FieldType.LONGINTEGER;
import static com.elster.jupiter.ids.FieldType.NUMBER;
import static com.elster.jupiter.ids.FieldType.TEXT;

public enum RecordSpecs {
    /*
        0 : process status
        1 : profile status
        2 : reading value
     */
    SINGLEINTERVAL("Single Interval Data", true) {
        @Override
        void addFieldSpecs(RecordSpecBuilder recordSpec) {
            recordSpec.addFieldSpec(VALUE, NUMBER);
        }

		private Object[] toArray(BaseReading reading, ProcessStatus status) {
			Object[] result = new Object[3];
			result[0] = status.getBits();
            result[1] = 0L;            ////The 'profile status' is no longer used. Its usage has been replaced by reading qualities.
            result[2] = reading.getValue();
            return result;
		}

        @Override
        Object[] toArray(BaseReading reading, int slotIndex, ProcessStatus status) {
            if (slotIndex != 0) {
                throw new IllegalArgumentException();
            }
            return toArray(reading, status);
        }

        @Override
        int slotOffset() {
            return 2;
        }
    },
    /*
        0 : process status
        1 : profile status
        2 : delta reading value
        3 : bulk reading value
     */
    BULKQUANTITYINTERVAL("Bulk Quantity Interval Data", true) {
        @Override
        void addFieldSpecs(RecordSpecBuilder recordSpec) {
            recordSpec.addFieldSpec(VALUE, NUMBER);
            recordSpec.addFieldSpec(BULK, NUMBER);
        }

		@Override
		Object[] toArray(BaseReading reading, int slotIndex, ProcessStatus status) {
			if (slotIndex != 0 && slotIndex != 1) {
				throw new IllegalArgumentException();
			}
			Object[] result = new Object[4];
			result[0] = status.getBits();
            result[1] = 0L;            //The 'profile status' is no longer used. Its usage has been replaced by reading qualities.
            result[2 + slotIndex] = reading.getValue();
            return result;
		}

        @Override
        int slotOffset() {
            return 2;
        }
    },
    /*
        0 : process status
        1 : profile status
        2 - 7 : reading values
     */
    MULTIINTERVAL("Multi Interval Data", true) {
        @Override
        void addFieldSpecs(RecordSpecBuilder recordSpec) {
            recordSpec.addFieldSpec("Value1", NUMBER);
            recordSpec.addFieldSpec("Value2", NUMBER);
            recordSpec.addFieldSpec("Value3", NUMBER);
            recordSpec.addFieldSpec("Value4", NUMBER);
            recordSpec.addFieldSpec("Value5", NUMBER);
            recordSpec.addFieldSpec("Value6", NUMBER);
        }

		@Override
		Object[] toArray(BaseReading reading, int slotIndex, ProcessStatus status) {
			if (slotIndex < 0 || slotIndex > 5) {
				throw new IllegalArgumentException();
			}
			Object[] result = new Object[8];
			result[0] = status.getBits();
            result[1] = 0L;            //The 'profile status' is no longer used. Its usage has been replaced by reading qualities.
            result[2 + slotIndex] = reading.getValue();
            return result;
		}

        @Override
        int slotOffset() {
            return 2;
        }
    },
    /*
        0 : process status
        1 : reading value
        2 : text
     */
    BASEREGISTER("Base Register", false) {
        @Override
        void addFieldSpecs(RecordSpecBuilder recordSpec) {
            recordSpec.addFieldSpec(VALUE, NUMBER);
            recordSpec.addFieldSpec("Text", TEXT);
        }

        private Object[] toArray(BaseReading reading, ProcessStatus status) {
            Object[] result = new Object[3];
            result[0] = status.getBits();
            result[1] = reading.getValue();
            result[2] = ((Reading) reading).getText();
            return result;
        }

        @Override
        Object[] toArray(BaseReading reading, int slotIndex, ProcessStatus status) {
            if (slotIndex != 0) {
                throw new IllegalArgumentException();
            }
            return toArray(reading, status);
        }

        @Override
        int slotOffset() {
            return 1;
        }
    },
    /*
        0 : process status
        1 : reading value
        2 : text
        3 : time period start
        4 : time period end
     */
    BILLINGPERIOD("Billing Period Register", false) {
        @Override
        void addFieldSpecs(RecordSpecBuilder recordSpec) {
            recordSpec.addFieldSpec(VALUE, NUMBER);
            recordSpec.addFieldSpec("Text", TEXT);
            recordSpec.addFieldSpec("From Time", INSTANT);
            recordSpec.addFieldSpec("To Time", INSTANT);
        }

        private Object[] toArray(BaseReading reading, ProcessStatus status) {
            Object[] result = new Object[5];
            result[0] = status.getBits();
            result[1] = reading.getValue();
            result[2] = ((Reading) reading).getText();
            reading.getTimePeriod().ifPresent(range -> {
                if (range.hasLowerBound()) {
                    result[3] = range.lowerEndpoint();
                }
                if (range.hasUpperBound()) {
                    result[4] = range.upperEndpoint();
                }
            });
            return result;
        }

        @Override
        Object[] toArray(BaseReading reading, int slotIndex, ProcessStatus status) {
            if (slotIndex != 0) {
                throw new IllegalArgumentException();
            }
            return toArray(reading, status);
        }

        @Override
        void validateValues(BaseReading reading, Object[] values) {
            reading.getTimePeriod().ifPresent(range -> {
                // We can't set error to the timestamp field because it is inactive for edit register
                if (range.lowerEndpoint().isAfter(reading.getTimeStamp())) {
                    throw new LocalizedFieldValidationException(PrivateMessageSeeds.READING_TIMESTAMP_NOT_IN_MEASUREMENT_PERIOD, "interval.start");
                }
                if (range.upperEndpoint().isBefore(reading.getTimeStamp())) {
                    throw new LocalizedFieldValidationException(PrivateMessageSeeds.READING_TIMESTAMP_NOT_IN_MEASUREMENT_PERIOD, "interval.end");
                }
            });
        }

        @Override
        public Optional<Range<Instant>> getTimePeriod(BaseReading reading, Object[] values) {
            if (values != null && values.length == 5) {
                Instant start = (Instant) values[3];
                Instant end = (Instant) values[4];
                if (start != null && end != null) {
                    return Optional.of(Range.openClosed(start, end));
                }
            }
            return Optional.empty();
        }

        @Override
        int slotOffset() {
            return 1;
        }
    },
    /*
        0 : process status
        1 : multiplied value
        2 : reading value
        3 : text
     */
    BASEREGISTER_WITH_MULTIPLIED_REGISTER("Base Multiplied Register", false) {
        @Override
        void addFieldSpecs(RecordSpecBuilder recordSpec) {
            recordSpec.addFieldSpec("MultipliedValue", NUMBER);
            recordSpec.addFieldSpec(VALUE, NUMBER);
            recordSpec.addFieldSpec("Text", TEXT);
        }

        @Override
        Object[] toArray(BaseReading reading, int slotIndex, ProcessStatus status) {
            if (!Range.closed(0, 1).contains(slotIndex)) {
                throw new IllegalArgumentException();
            }
            Object[] result = new Object[4];
            result[0] = status.getBits();
            result[slotOffset() + slotIndex] = reading.getValue();
            result[3] = ((Reading) reading).getText();
            return result;
        }

        @Override
        int slotOffset() {
            return 1;
        }
    },
    /*
        0 : process status
        1 : profile status
        2 : multiplied reading value
        3 : measured reading value
     */
    VALUE_MULTIPLIED_INTERVAL("Multiplied Interval Data", true) {
        @Override
        void addFieldSpecs(RecordSpecBuilder recordSpec) {
            recordSpec.addFieldSpec("Multiplied Value", NUMBER);
            recordSpec.addFieldSpec(VALUE, NUMBER);
        }

        @Override
        Object[] toArray(BaseReading reading, int slotIndex, ProcessStatus status) {
            if (slotIndex != 0 && slotIndex != 1) {
                throw new IllegalArgumentException();
            }
            Object[] result = new Object[4];
            result[0] = status.getBits();
            result[1] = 0L;            //The 'profile status' is no longer used. Its usage has been replaced by reading qualities.
            result[2 + slotIndex] = reading.getValue();
            return result;
        }

        @Override
        int slotOffset() {
            return 2;
        }
    },;

    public static final String PROCESS_STATUS = "ProcesStatus";
    public static final String PROFILE_STATUS = "ProfileStatus";
    public static final String VALUE = "Value";
    public static final String BULK = "Bulk";

    private final String specName;
    private final boolean interval;

    RecordSpecs(String specName, boolean interval) {
        this.specName = specName;
        this.interval = interval;
    }

    static void createAll(IdsService idsService) {
        for (RecordSpecs each : values()) {
            each.create(idsService);
        }
    }

    private RecordSpec create(IdsService idsService) {
        RecordSpecBuilder recordSpecBuilder = idsService.createRecordSpec(MeteringService.COMPONENTNAME, ordinal() + 1, specName)
                .addFieldSpec(PROCESS_STATUS, LONGINTEGER);
        if (interval) {
            recordSpecBuilder.addFieldSpec(PROFILE_STATUS, LONGINTEGER);
        }
        addFieldSpecs(recordSpecBuilder);
        return recordSpecBuilder.create();
    }

    void validateValues(BaseReading reading, Object[] values) {
    }

    abstract int slotOffset();

    abstract void addFieldSpecs(RecordSpecBuilder builder);

    abstract Object[] toArray(BaseReading reading, int slotIndex, ProcessStatus status);

    public Optional<Range<Instant>> getTimePeriod(BaseReading reading, Object[] values) {
        return Optional.empty();
    }

    final RecordSpec get(IdsService idsService) {
        return idsService.getRecordSpec(MeteringService.COMPONENTNAME, ordinal() + 1).get();
    }

}
