package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import com.energyict.cbo.Quantity;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for the {@link DeviceRegister} implementations
 *
 * @author gna
 * @since 4/04/12 - 13:29
 */
public class DeviceRegisterTest {

    private static RegisterIdentifier getMockedRegisterIdentifier() {
        return mock(RegisterIdentifier.class);
    }

    @Test
    public void registerIdTest() {
        BillingDeviceRegisters billingDeviceRegisters = new BillingDeviceRegisters(getMockedRegisterIdentifier());
        assertThat(billingDeviceRegisters.getRegisterIdentifier()).isNotNull();

        DefaultDeviceRegister defaultDeviceRegister = new DefaultDeviceRegister(getMockedRegisterIdentifier());
        assertThat(defaultDeviceRegister.getRegisterIdentifier()).isNotNull();

        MaximumDemandDeviceRegister maximumDemandDeviceRegister = new MaximumDemandDeviceRegister(getMockedRegisterIdentifier());
        assertThat(maximumDemandDeviceRegister.getRegisterIdentifier()).isNotNull();
    }

    @Test
    public void defaultSupportedNonIssueTest() {
        BillingDeviceRegisters billingDeviceRegisters = new BillingDeviceRegisters(getMockedRegisterIdentifier());
        assertThat(billingDeviceRegisters.getResultType()).isEqualTo(ResultType.Supported);
        assertThat(billingDeviceRegisters.getIssues()).isEmpty();

        DefaultDeviceRegister defaultDeviceRegister = new DefaultDeviceRegister(getMockedRegisterIdentifier());
        assertThat(defaultDeviceRegister.getResultType()).isEqualTo(ResultType.Supported);
        assertThat(defaultDeviceRegister.getIssues()).isEmpty();

        MaximumDemandDeviceRegister maximumDemandDeviceRegister = new MaximumDemandDeviceRegister(getMockedRegisterIdentifier());
        assertThat(maximumDemandDeviceRegister.getResultType()).isEqualTo(ResultType.Supported);
        assertThat(maximumDemandDeviceRegister.getIssues()).isEmpty();
    }

    @Test
    public void setCollectedDataTest() {
        BillingDeviceRegisters billingDeviceRegisters = new BillingDeviceRegisters(getMockedRegisterIdentifier());
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
        BillingDeviceRegisters billingDeviceRegisters = new BillingDeviceRegisters(getMockedRegisterIdentifier());
        billingDeviceRegisters.setCollectedTimeStamps(Date.from(readTime), Date.from(fromTime), Date.from(toTime));

        assertThat(billingDeviceRegisters.getReadTime()).isEqualTo(Date.from(readTime));
        assertThat(billingDeviceRegisters.getFromTime()).isEqualTo(Date.from(fromTime));
        assertThat(billingDeviceRegisters.getToTime()).isEqualTo(Date.from(toTime));
        assertThat(billingDeviceRegisters.getEventTime()).isNull();
    }

    @Test
    public void maximumDemandRegisterTimeTest() {
        final Instant readTime = Instant.now().minus(Duration.ofSeconds(100));
        final Instant fromTime = Instant.now().minus(Duration.ofSeconds(700));
        final Instant toTime = Instant.now().minus(Duration.ofSeconds(550));
        final Instant eventTime = Instant.now().minus(Duration.ofMillis(643712));
        MaximumDemandDeviceRegister maximumDemandDeviceRegister = new MaximumDemandDeviceRegister(getMockedRegisterIdentifier());
        maximumDemandDeviceRegister.setCollectedTimeStamps(Date.from(readTime), Date.from(fromTime), Date.from(toTime), Date.from(eventTime));

        assertThat(maximumDemandDeviceRegister.getReadTime()).isEqualTo(Date.from(readTime));
        assertThat(maximumDemandDeviceRegister.getFromTime()).isEqualTo(Date.from(fromTime));
        assertThat(maximumDemandDeviceRegister.getToTime()).isEqualTo(Date.from(toTime));
        assertThat(maximumDemandDeviceRegister.getEventTime()).isEqualTo(Date.from(eventTime));
    }

    @Test
    public void defaultDeviceRegisterTimeTest() {
        final Instant readTime = Instant.now().minus(Duration.ofSeconds(100));
        DefaultDeviceRegister defaultDeviceRegister = new DefaultDeviceRegister(getMockedRegisterIdentifier());
        defaultDeviceRegister.setReadTime(Date.from(readTime));

        assertThat(defaultDeviceRegister.getReadTime()).isEqualTo(Date.from(readTime));
        assertThat(defaultDeviceRegister.getToTime()).isEqualTo(Date.from(readTime));
        assertThat(defaultDeviceRegister.getFromTime()).isNull();
        assertThat(defaultDeviceRegister.getEventTime()).isNull();
    }

}