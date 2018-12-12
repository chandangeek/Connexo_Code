/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeMap;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test to verify support for all register types defined at {@link RegisterDataInfoFactory.RegisterType}
 */
public class RegisterDataTypeTest extends UsagePointApplicationJerseyTest {

    private Map<RegisterTypeAttribute, RegisterDataInfoFactory.RegisterType> registerTypeMap = new HashMap<>();
    private List<Aggregate> aggregatesWithEventDate = Arrays.asList(Aggregate.MAXIMUM, Aggregate.FIFTHMAXIMIMUM,
            Aggregate.FOURTHMAXIMUM, Aggregate.MINIMUM, Aggregate.SECONDMAXIMUM, Aggregate.SECONDMINIMUM, Aggregate.THIRDMAXIMUM);

    private Instant previousReadingTime = Instant.ofEpochMilli(1459634400000L);
    private Instant actualReadingTime = Instant.ofEpochMilli(1459717200000L);

    private Instant billingTimePeriodStart = Instant.ofEpochMilli(1559634400000L);
    private Instant billingTimePeriodEnd = Instant.ofEpochMilli(1559717200000L);
    private Range<Instant> billingTimePeriod = Range.openClosed(billingTimePeriodStart, billingTimePeriodEnd);

    private BigDecimal readingValue = BigDecimal.valueOf(1000L);
    private BigDecimal previousReadingValue = BigDecimal.valueOf(500L);
    private BigDecimal deltaValue = readingValue.subtract(previousReadingValue);

    @Mock
    private ReadingQualityInfoFactory readingQualityInfoFactory;
    @InjectMocks
    private RegisterDataInfoFactory registerDataInfoFactory;

    @Before
    public void before() {
        putAllTypes();
    }

    /**
     * Verify correct {@link RegisterDataInfoFactory.RegisterType} instance creation based on reading types
     */
    @Test
    public void registerTypeDeterminationTest() {
        for (Map.Entry<RegisterTypeAttribute, RegisterDataInfoFactory.RegisterType> entry : registerTypeMap.entrySet()) {
            RegisterTypeAttribute registerTypeAttribute = entry.getKey();
            RegisterDataInfoFactory.RegisterType referencedRegisterType = entry.getValue();
            RegisterDataInfoFactory.RegisterType determinedRegisterType = RegisterDataInfoFactory.RegisterType.determineRegisterType(mockRegisterReadingWithValidationStatus(registerTypeAttribute));
            assertEquals("Determined register type " + determinedRegisterType + " does not match reference type " + referencedRegisterType + " for predefined attribute " + registerTypeAttribute,
                    determinedRegisterType, referencedRegisterType);
        }
    }

    @Test
    public void registerDataInfoCreationTest() {

        when(readingQualityInfoFactory.asInfos(anyList())).thenReturn(null);

        for (Map.Entry<RegisterTypeAttribute, RegisterDataInfoFactory.RegisterType> entry : registerTypeMap.entrySet()) {
            RegisterDataInfo registerDataInfo = registerDataInfoFactory.asInfo(mockRegisterReadingWithValidationStatus(entry
                    .getKey()), TreeRangeMap.create());

            RegisterDataInfoFactory.RegisterType referencedRegisterType = entry.getValue();

            RegisterTypeAttribute registerTypeAttribute = entry.getKey();
            assertEquals(registerDataInfo.isCumulative, registerTypeAttribute.isCumulative());
            assertEquals(registerDataInfo.hasEvent, registerTypeAttribute.hasEvent());
            assertEquals(registerDataInfo.isBilling, registerTypeAttribute.isBilling());

            // simply mocked - simply checked
            assertEquals(ValidationStatus.NOT_VALIDATED, registerDataInfo.validationResult);
            assertNull(registerDataInfo.readingQualities);

            switch (referencedRegisterType) {
                case CUMULATIVE_VALUE:
                    // measurementPeriod
                    assertNotNull(registerDataInfo.measurementPeriod);
                    assertEquals(registerDataInfo.measurementPeriod.start, previousReadingTime);
                    assertEquals(registerDataInfo.measurementPeriod.end, actualReadingTime);

                    // collected value
                    assertEquals(registerDataInfo.collectedValue, readingValue);

                    // delta value
                    assertEquals(deltaValue, registerDataInfo.deltaValue);

                    // measurement time
                    assertNull(registerDataInfo.measurementTime);

                    // event date
                    assertNull(registerDataInfo.eventDate);
                    break;
                case NOT_CUMULATIVE_VALUE:
                    // measurementPeriod
                    assertNull(registerDataInfo.measurementPeriod);

                    // collected value
                    assertEquals(readingValue, registerDataInfo.collectedValue);

                    // delta value
                    assertNull(registerDataInfo.deltaValue);

                    // measurement time
                    assertEquals(actualReadingTime, registerDataInfo.measurementTime);

                    // event date
                    assertNull(registerDataInfo.eventDate);
                    break;
                case CUMULATIVE_BILLING_VALUE:
                    // measurementPeriod
                    assertNotNull(registerDataInfo.measurementPeriod);
                    assertEquals(registerDataInfo.measurementPeriod.start, previousReadingTime);
                    assertEquals(registerDataInfo.measurementPeriod.end, billingTimePeriodEnd);

                    // collected value
                    assertEquals(registerDataInfo.collectedValue, readingValue);

                    // delta value
                    assertEquals(registerDataInfo.deltaValue, deltaValue);

                    // measurement time
                    assertNull(registerDataInfo.measurementTime);

                    // event date
                    assertNull(registerDataInfo.eventDate);
                    break;
                case NOT_CUMULATIVE_BILLING_VALUE:
                    // measurementPeriod
                    assertNotNull(registerDataInfo.measurementPeriod);
                    assertEquals(registerDataInfo.measurementPeriod.start, billingTimePeriodStart);
                    assertEquals(registerDataInfo.measurementPeriod.end, billingTimePeriodEnd);

                    // collected value
                    assertEquals(registerDataInfo.collectedValue, readingValue);

                    // delta value
                    assertNull(registerDataInfo.deltaValue);

                    // measurement time
                    assertNull(registerDataInfo.measurementTime);

                    // event date
                    assertNull(registerDataInfo.eventDate);
                    break;
                case EVENT_VALUE:
                    // measurementPeriod
                    assertNull(registerDataInfo.measurementPeriod);

                    // collected value
                    assertEquals(registerDataInfo.collectedValue, readingValue);

                    // delta value
                    assertNull(registerDataInfo.deltaValue);

                    // measurement time
                    assertEquals(actualReadingTime, registerDataInfo.measurementTime);

                    // event date
                    assertEquals(actualReadingTime, registerDataInfo.eventDate);
                    break;
                case EVENT_BILLING_VALUE:
                    // measurementPeriod
                    assertNotNull(registerDataInfo.measurementPeriod);
                    assertEquals(registerDataInfo.measurementPeriod.start, billingTimePeriodStart);
                    assertEquals(registerDataInfo.measurementPeriod.end, billingTimePeriodEnd);

                    // collected value
                    assertEquals(registerDataInfo.collectedValue, readingValue);

                    // delta value
                    assertNull(registerDataInfo.deltaValue);

                    // measurement time
                    assertNull(registerDataInfo.measurementTime);

                    // event date
                    assertEquals(actualReadingTime, registerDataInfo.eventDate);
                    break;
                case CUMULATIVE_EVENT_BILLING_VALUE:
                    // measurementPeriod
                    assertNotNull(registerDataInfo.measurementPeriod);
                    assertEquals(registerDataInfo.measurementPeriod.start, previousReadingTime);
                    assertEquals(registerDataInfo.measurementPeriod.end, billingTimePeriodEnd);

                    // collected value
                    assertEquals(registerDataInfo.collectedValue, readingValue);

                    // delta value
                    assertEquals(registerDataInfo.deltaValue, deltaValue);

                    // measurement time
                    assertNull(registerDataInfo.measurementTime);

                    // event date
                    assertNull(registerDataInfo.eventDate);
                    break;
                default:
                    Assert.fail("Unknown Register Type: " + referencedRegisterType);

            }

        }
    }

    private RegisterReadingWithValidationStatus mockRegisterReadingWithValidationStatus(RegisterTypeAttribute registerTypeAttribute) {
        RegisterReadingWithValidationStatus registerReadingWithValidationStatus = mock(RegisterReadingWithValidationStatus.class);
        ReadingRecord readingRecord = mock(ReadingRecord.class);

        ReadingType readingType = mock(ReadingType.class);
        when(readingType.isCumulative()).thenReturn(registerTypeAttribute.isCumulative);
        when(readingType.getAggregate()).thenReturn(registerTypeAttribute.aggregate);
        when(readingType.getMacroPeriod()).thenReturn(registerTypeAttribute.macroPeriod);

        when(readingRecord.getReadingType()).thenReturn(readingType);
        when(readingRecord.getReadingQualities()).thenReturn(new ArrayList<>());
        when(readingRecord.getTimeStamp()).thenReturn(actualReadingTime);
        if (registerTypeAttribute.isBilling()) {
            when(readingRecord.getTimePeriod()).thenReturn(Optional.of(billingTimePeriod));
        } else {
            when(readingRecord.getTimePeriod()).thenReturn(Optional.empty());
        }


        ReadingRecord previousReadingRecord = mock(ReadingRecord.class);
        when(previousReadingRecord.getTimeStamp()).thenReturn(previousReadingTime);
        when(previousReadingRecord.getValue()).thenReturn(previousReadingValue);

        when(registerReadingWithValidationStatus.getReadingRecord()).thenReturn(readingRecord);
        when(registerReadingWithValidationStatus.getPreviousReadingRecord()).thenReturn(Optional.of(previousReadingRecord));
        when(registerReadingWithValidationStatus.getValue()).thenReturn(readingValue);
        when(registerReadingWithValidationStatus.getTimeStamp()).thenReturn(actualReadingTime);
        when(registerReadingWithValidationStatus.getValidationStatus(any(Instant.class))).thenReturn(ValidationStatus.NOT_VALIDATED);
        return registerReadingWithValidationStatus;
    }

    private class RegisterTypeAttribute {

        boolean isCumulative;
        Aggregate aggregate;
        MacroPeriod macroPeriod;

        boolean isCumulative() {
            return isCumulative;
        }

        boolean hasEvent() {
            return aggregatesWithEventDate.contains(aggregate);
        }

        boolean isBilling() {
            return macroPeriod.equals(MacroPeriod.BILLINGPERIOD);
        }

        @Override
        public String toString() {
            return (isCumulative ? "Cumulative" : "Not cumulative") + ", " +
                    (isCumulative() ? "event" : "not event") + " and " +
                    (isBilling() ? "billing" : "not billing") + " register type attribute";
        }

        public RegisterTypeAttribute(boolean isCumulative, Aggregate aggregate, MacroPeriod macroPeriod) {
            this.isCumulative = isCumulative;
            this.aggregate = aggregate;
            this.macroPeriod = macroPeriod;
        }
    }

    private void putAllTypes() {
        putCumulativeEventBillingValues();
        putEventBillingValues();
        putEventValues();
        putNotCumulativeBillingValues();
        putCumulativeBillingValues();
        putCumulativeValues();
        putNotCumulativeValues();
    }

    private void putCumulativeEventBillingValues() {
        registerTypeMap.putAll(
                aggregatesWithEventDate.stream()
                        .map(eventAggregate -> new RegisterTypeAttribute(true, eventAggregate, MacroPeriod.BILLINGPERIOD))
                        .collect(Collectors.toMap(Function.identity(), c -> RegisterDataInfoFactory.RegisterType.CUMULATIVE_EVENT_BILLING_VALUE)));
    }

    private void putEventBillingValues() {
        registerTypeMap.putAll(
                aggregatesWithEventDate.stream()
                        .map(eventAggregate -> new RegisterTypeAttribute(false, eventAggregate, MacroPeriod.BILLINGPERIOD))
                        .collect(Collectors.toMap(Function.identity(), c -> RegisterDataInfoFactory.RegisterType.EVENT_BILLING_VALUE)));
    }

    private void putEventValues() {
        registerTypeMap.putAll(
                Arrays.stream(MacroPeriod.values())
                        .filter(macroPeriod -> !macroPeriod.equals(MacroPeriod.BILLINGPERIOD))
                        .flatMap(notBillingMacroPeriod ->
                                Stream.concat(
                                        aggregatesWithEventDate.stream()
                                                .map(eventAggregate -> new RegisterTypeAttribute(false, eventAggregate, notBillingMacroPeriod)),
                                        aggregatesWithEventDate.stream()
                                                .map(eventAggregate -> new RegisterTypeAttribute(true, eventAggregate, notBillingMacroPeriod)))
                        )
                        .collect(Collectors.toMap(Function.identity(), c -> RegisterDataInfoFactory.RegisterType.EVENT_VALUE)));

    }

    private void putNotCumulativeBillingValues() {
        registerTypeMap.putAll(
                Arrays.stream(Aggregate.values())
                        .filter(aggregate -> !aggregatesWithEventDate.contains(aggregate))
                        .map(
                                notEventAggregate -> new RegisterTypeAttribute(false, notEventAggregate, MacroPeriod.BILLINGPERIOD)
                        )
                        .collect(Collectors.toMap(Function.identity(), c -> RegisterDataInfoFactory.RegisterType.NOT_CUMULATIVE_BILLING_VALUE)));
    }

    private void putCumulativeBillingValues() {
        registerTypeMap.putAll(
                Arrays.stream(Aggregate.values())
                        .filter(aggregate -> !aggregatesWithEventDate.contains(aggregate))
                        .map(
                                notEventAggregate -> new RegisterTypeAttribute(true, notEventAggregate, MacroPeriod.BILLINGPERIOD)
                        )
                        .collect(Collectors.toMap(Function.identity(), c -> RegisterDataInfoFactory.RegisterType.CUMULATIVE_BILLING_VALUE)));
    }

    private void putCumulativeValues() {
        registerTypeMap.putAll(
                Arrays.stream(Aggregate.values())
                        .filter(aggregate -> !aggregatesWithEventDate.contains(aggregate))
                        .flatMap(notEventAggregate ->
                                Arrays.stream(MacroPeriod.values())
                                        .filter(macroPeriod -> !macroPeriod.equals(MacroPeriod.BILLINGPERIOD))
                                        .flatMap(notBillingMacroPeriod ->
                                                aggregatesWithEventDate.stream()
                                                        .map(eventAggregate -> new RegisterTypeAttribute(true, notEventAggregate, notBillingMacroPeriod)))
                        )
                        .collect(Collectors.toMap(Function.identity(), c -> RegisterDataInfoFactory.RegisterType.CUMULATIVE_VALUE)));
    }

    private void putNotCumulativeValues() {
        registerTypeMap.putAll(
                Arrays.stream(Aggregate.values())
                        .filter(aggregate -> !aggregatesWithEventDate.contains(aggregate))
                        .flatMap(notEventAggregate ->
                                Arrays.stream(MacroPeriod.values())
                                        .filter(macroPeriod -> !macroPeriod.equals(MacroPeriod.BILLINGPERIOD))
                                        .flatMap(notBillingMacroPeriod ->
                                                aggregatesWithEventDate.stream()
                                                        .map(eventAggregate -> new RegisterTypeAttribute(false, notEventAggregate, notBillingMacroPeriod)))
                        )
                        .collect(Collectors.toMap(Function.identity(), c -> RegisterDataInfoFactory.RegisterType.NOT_CUMULATIVE_VALUE)));
    }

}
