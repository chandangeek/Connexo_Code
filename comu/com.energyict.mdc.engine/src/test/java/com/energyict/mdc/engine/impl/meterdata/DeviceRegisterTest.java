/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;

import java.time.Duration;
import java.time.Instant;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link DeviceRegister} implementations
 *
 * @author gna
 * @since 4/04/12 - 13:29
 */
public class DeviceRegisterTest {

    private static RegisterIdentifier getMockedRegisterIdentifier() {
        Register register = mock(Register.class);
        RegisterIdentifier registerIdentifier = mock(RegisterIdentifier.class);
        when(registerIdentifier.findRegister()).thenReturn(register);
        return registerIdentifier;
    }

    @Test
    public void registerIdTest() {
        BillingDeviceRegisters billingDeviceRegisters = new BillingDeviceRegisters(getMockedRegisterIdentifier(), mock(ReadingType.class));
        assertThat(billingDeviceRegisters.getRegisterIdentifier()).isNotNull();

        DefaultDeviceRegister defaultDeviceRegister = new DefaultDeviceRegister(getMockedRegisterIdentifier(), mock(ReadingType.class));
        assertThat(defaultDeviceRegister.getRegisterIdentifier()).isNotNull();

        MaximumDemandDeviceRegister maximumDemandDeviceRegister = new MaximumDemandDeviceRegister(getMockedRegisterIdentifier(), mock(ReadingType.class));
        assertThat(maximumDemandDeviceRegister.getRegisterIdentifier()).isNotNull();
    }

    @Test
    public void defaultSupportedNonIssueTest(){
        BillingDeviceRegisters billingDeviceRegisters = new BillingDeviceRegisters(getMockedRegisterIdentifier(), mock(ReadingType.class));
        assertThat(billingDeviceRegisters.getResultType()).isEqualTo(ResultType.Supported);
        assertThat(billingDeviceRegisters.getIssues()).isEmpty();

        DefaultDeviceRegister defaultDeviceRegister = new DefaultDeviceRegister(getMockedRegisterIdentifier(), mock(ReadingType.class));
        assertThat(defaultDeviceRegister.getResultType()).isEqualTo(ResultType.Supported);
        assertThat(defaultDeviceRegister.getIssues()).isEmpty();

        MaximumDemandDeviceRegister maximumDemandDeviceRegister = new MaximumDemandDeviceRegister(getMockedRegisterIdentifier(), mock(ReadingType.class));
        assertThat(maximumDemandDeviceRegister.getResultType()).isEqualTo(ResultType.Supported);
        assertThat(maximumDemandDeviceRegister.getIssues()).isEmpty();
    }

    @Test
    public void setCollectedDataTest() {
        BillingDeviceRegisters billingDeviceRegisters = new BillingDeviceRegisters(getMockedRegisterIdentifier(), mock(ReadingType.class));
        Quantity quantity = mock(Quantity.class);
        String text = "Collected String Test";
        billingDeviceRegisters.setCollectedData(quantity, text);
        assertThat(billingDeviceRegisters.getCollectedQuantity()).isEqualTo(quantity);
        assertThat(billingDeviceRegisters.getText()).isEqualTo(text);
    }

    @Test
    public void billingDeviceRegisterTimeTest() {
        final Instant readTime = Instant.now().minus(Duration.ofSeconds(100));
        final Instant fromTime = Instant.now().minus(Duration.ofSeconds(700));
        final Instant toTime = Instant.now().minus(Duration.ofSeconds(550));
        BillingDeviceRegisters billingDeviceRegisters = new BillingDeviceRegisters(getMockedRegisterIdentifier(), mock(ReadingType.class));
        billingDeviceRegisters.setCollectedTimeStamps(readTime, fromTime, toTime);

        assertThat(billingDeviceRegisters.getReadTime()).isEqualTo(readTime);
        assertThat(billingDeviceRegisters.getFromTime()).isEqualTo(fromTime);
        assertThat(billingDeviceRegisters.getToTime()).isEqualTo(toTime);
        assertThat(billingDeviceRegisters.getEventTime()).isNull();
    }

    @Test
    public void maximumDemandRegisterTimeTest() {
        final Instant readTime = Instant.now().minus(Duration.ofSeconds(100));
        final Instant fromTime = Instant.now().minus(Duration.ofSeconds(700));
        final Instant toTime = Instant.now().minus(Duration.ofSeconds(550));
        final Instant eventTime = Instant.now().minus(Duration.ofMillis(643712));
        MaximumDemandDeviceRegister maximumDemandDeviceRegister = new MaximumDemandDeviceRegister(getMockedRegisterIdentifier(), mock(ReadingType.class));
        maximumDemandDeviceRegister.setCollectedTimeStamps(readTime, fromTime, toTime, eventTime);

        assertThat(maximumDemandDeviceRegister.getReadTime()).isEqualTo(readTime);
        assertThat(maximumDemandDeviceRegister.getFromTime()).isEqualTo(fromTime);
        assertThat(maximumDemandDeviceRegister.getToTime()).isEqualTo(toTime);
        assertThat(maximumDemandDeviceRegister.getEventTime()).isEqualTo(eventTime);
    }

    @Test
    public void defaultDeviceRegisterTimeTest() {
        final Instant readTime = Instant.now().minus(Duration.ofSeconds(100));
        DefaultDeviceRegister defaultDeviceRegister = new DefaultDeviceRegister(getMockedRegisterIdentifier(), mock(ReadingType.class));
        defaultDeviceRegister.setReadTime(readTime);

        assertThat(defaultDeviceRegister.getReadTime()).isEqualTo(readTime);
        assertThat(defaultDeviceRegister.getToTime()).isEqualTo(readTime);
        assertThat(defaultDeviceRegister.getFromTime()).isNull();
        assertThat(defaultDeviceRegister.getEventTime()).isNull();
    }

}