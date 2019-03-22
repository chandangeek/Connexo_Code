/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.OverlapCalculatorBuilder;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.AbstractMockMeterConfig;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfig.SimpleEndDeviceFunction;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigFaultMessageType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigRequestMessageType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigResponseMessageType;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.google.common.collect.Range;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class GetMeterConfigTest extends AbstractMockMeterConfig {

    private com.energyict.mdc.device.data.DeviceBuilder deviceBuilder;
    @Mock
    private OverlapCalculatorBuilder overlapCalculatorBuilder;

    @Before
    public void setUp() throws Exception {

        deviceBuilder = FakeBuilder.initBuilderStub(device, com.energyict.mdc.device.data.DeviceBuilder.class);
        when(deviceService.newDeviceBuilder(deviceConfiguration, DEVICE_NAME, RECEIVED_DATE)).thenReturn(deviceBuilder);
        when(deviceService.findAllDevices(any(Condition.class)))
                .thenReturn(FakeBuilder.initBuilderStub(Collections.emptyList(), Finder.class));
        when(state.getName()).thenReturn(STATE_NAME);
        mockDevice();
        when(customPropertySetService.calculateOverlapsFor(any(CustomPropertySet.class), any()))
                .thenReturn(overlapCalculatorBuilder);
        when(overlapCalculatorBuilder.whenCreating(any(Range.class))).thenReturn(Collections.emptyList());
        when(deviceService.findDeviceByMrid(DEVICE_MRID)).thenReturn(Optional.of(device));
        when(deviceService.findDeviceByName(DEVICE_NAME)).thenReturn(Optional.of(device));
    }

    @Test
    public void testNoMetersInMeterConfig() throws Exception {
        // Prepare request
        MeterConfig meterConfig = new MeterConfig();
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        try {
            // Business method
            getInstance(ExecuteMeterConfigEndpoint.class).getMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.EMPTY_LIST.translate(thesaurus));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode()).isEqualTo(MessageSeeds.EMPTY_LIST.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.EMPTY_LIST.translate(thesaurus, "MeterConfig.Meter"));
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testCreateDeviceWithCpsSuccessfully() throws Exception {

        mockCustomPropertySetService();
        mockCustomPropertySetSpecs(true);
        doTestCreateDeviceWithCps();
    }

    private void doTestCreateDeviceWithCps() throws FaultMessage {
        // Prepare request
        MeterConfig meterConfig = new MeterConfig();
        SimpleEndDeviceFunction simpleEndDeviceFunction = createDefaultEndDeviceFunction();
        meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createDefaultMeter();
        meter.getMeterCustomAttributeSet().add(createNonVersionedCustomPropertySet());
        meter.getMeterCustomAttributeSet().add(createVersionedCustomPropertySet());
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        // Business method
        MeterConfigResponseMessageType response = getInstance(ExecuteMeterConfigEndpoint.class)
                .getMeterConfig(meterConfigRequest);

        // Assert response
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
    }
}
