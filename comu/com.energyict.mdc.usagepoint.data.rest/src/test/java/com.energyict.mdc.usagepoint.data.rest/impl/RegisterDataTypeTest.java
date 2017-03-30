/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test to verify support for all register types defined at {@link RegisterDataInfoFactory.RegisterType}
 */
public class RegisterDataTypeTest extends UsagePointApplicationJerseyTest {

    /**
     * Verify correct {@link RegisterDataInfoFactory.RegisterType} instance creation based on reading type
     */
    @Test
    public void registerTypeDeterminationTest() {

        putAllTypes();

        for (Map.Entry<RegisterTypeAttribute, RegisterDataInfoFactory.RegisterType> entry : registerTypeMap.entrySet()) {
            RegisterTypeAttribute registerTypeAttribute = entry.getKey();
            RegisterDataInfoFactory.RegisterType referencedRegisterType = entry.getValue();
            RegisterDataInfoFactory.RegisterType determinedRegisterType = RegisterDataInfoFactory.RegisterType.determineRegisterType(mockRegisterReadingWithValidationStatus(registerTypeAttribute));
            Assert.assertEquals("Determined register type " + determinedRegisterType + " not match reference type " + referencedRegisterType + " for predefined attribute " + registerTypeAttribute,
                    determinedRegisterType, referencedRegisterType);
        }

    }


    private class RegisterTypeAttribute {

        boolean isCumulative;
        Aggregate aggregate;
        MacroPeriod macroPeriod;

        @Override
        public String toString() {
            return (isCumulative ? "Cumulative" : "Not cumulative") + ", " + (aggregatesWithEventDate.contains(aggregate) ? "event" : "not event") + " and " + (macroPeriod
                    .equals(MacroPeriod.BILLINGPERIOD) ? "billing" : "not billing") + " register type attribute";
        }

        public RegisterTypeAttribute(boolean isCumulative, Aggregate aggregate, MacroPeriod macroPeriod) {
            this.isCumulative = isCumulative;
            this.aggregate = aggregate;
            this.macroPeriod = macroPeriod;
        }
    }

    private Map<RegisterTypeAttribute, RegisterDataInfoFactory.RegisterType> registerTypeMap = new HashMap<>();

    private void putAllTypes() {
        putCumulativeEventBillingValues();
        putEventBillingValues();
        putEventValues();
        putNotCumulativeBillingValues();
        putCumulativeBillingValues();
        putCumulativeValues();
        putNotCumulativeValues();
    }

    private static final List<Aggregate> aggregatesWithEventDate = Arrays.asList(Aggregate.MAXIMUM, Aggregate.FIFTHMAXIMIMUM,
            Aggregate.FOURTHMAXIMUM, Aggregate.MINIMUM, Aggregate.SECONDMAXIMUM, Aggregate.SECONDMINIMUM, Aggregate.THIRDMAXIMUM);

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

    private RegisterReadingWithValidationStatus mockRegisterReadingWithValidationStatus(RegisterTypeAttribute registerTypeAttribute) {
        RegisterReadingWithValidationStatus registerReadingWithValidationStatus = mock(RegisterReadingWithValidationStatus.class);
        ReadingRecord readingRecord = mock(ReadingRecord.class);

        ReadingType readingType = mock(ReadingType.class);
        when(readingType.isCumulative()).thenReturn(registerTypeAttribute.isCumulative);
        when(readingType.getAggregate()).thenReturn(registerTypeAttribute.aggregate);
        when(readingType.getMacroPeriod()).thenReturn(registerTypeAttribute.macroPeriod);

        when(readingRecord.getReadingType()).thenReturn(readingType);
        when(registerReadingWithValidationStatus.getReadingRecord()).thenReturn(readingRecord);
        return registerReadingWithValidationStatus;
    }

}
