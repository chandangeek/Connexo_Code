package com.energyict.mdc.engine.impl.meterdata;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import org.junit.*;

import java.util.Date;

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

    private static final int REGISTER_ID = 12;

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
        final Date readTime = new Date(System.currentTimeMillis() - 100000);
        final Date fromTime = new Date(System.currentTimeMillis() - 700000);
        final Date toTime = new Date(System.currentTimeMillis() - 550000);
        BillingDeviceRegisters billingDeviceRegisters = new BillingDeviceRegisters(getMockedRegisterIdentifier(), mock(ReadingType.class));
        billingDeviceRegisters.setCollectedTimeStamps(readTime, fromTime, toTime);

        assertThat(billingDeviceRegisters.getReadTime()).isEqualTo(readTime);
        assertThat(billingDeviceRegisters.getFromTime()).isEqualTo(fromTime);
        assertThat(billingDeviceRegisters.getToTime()).isEqualTo(toTime);
        assertThat(billingDeviceRegisters.getEventTime()).isNull();
    }

    @Test
    public void maximumDemandRegisterTimeTest() {
        final Date readTime = new Date(System.currentTimeMillis() - 100000);
        final Date fromTime = new Date(System.currentTimeMillis() - 700000);
        final Date toTime = new Date(System.currentTimeMillis() - 550000);
        final Date eventTime = new Date(System.currentTimeMillis() - 643712);
        MaximumDemandDeviceRegister maximumDemandDeviceRegister = new MaximumDemandDeviceRegister(getMockedRegisterIdentifier(), mock(ReadingType.class));
        maximumDemandDeviceRegister.setCollectedTimeStamps(readTime, fromTime, toTime, eventTime);

        assertThat(maximumDemandDeviceRegister.getReadTime()).isEqualTo(readTime);
        assertThat(maximumDemandDeviceRegister.getFromTime()).isEqualTo(fromTime);
        assertThat(maximumDemandDeviceRegister.getToTime()).isEqualTo(toTime);
        assertThat(maximumDemandDeviceRegister.getEventTime()).isEqualTo(eventTime);
    }

    @Test
    public void defaultDeviceRegisterTimeTest() {
        final Date readTime = new Date(System.currentTimeMillis() - 100000);
        DefaultDeviceRegister defaultDeviceRegister = new DefaultDeviceRegister(getMockedRegisterIdentifier(), mock(ReadingType.class));
        defaultDeviceRegister.setReadTime(readTime);

        assertThat(defaultDeviceRegister.getReadTime()).isEqualTo(readTime);
        assertThat(defaultDeviceRegister.getToTime()).isEqualTo(readTime);
        assertThat(defaultDeviceRegister.getFromTime()).isNull();
        assertThat(defaultDeviceRegister.getEventTime()).isNull();
    }

}