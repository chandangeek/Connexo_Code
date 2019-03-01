/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.getmeterconfig;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.AbstractMockMeterConfig;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

import ch.iec.tc57._2011.getmeterconfig.FaultMessage;
import ch.iec.tc57._2011.getmeterconfig.GetMeterConfig;
import ch.iec.tc57._2011.getmeterconfigmessage.GetMeterConfigPayloadType;
import ch.iec.tc57._2011.getmeterconfigmessage.GetMeterConfigRequestMessageType;
import ch.iec.tc57._2011.getmeterconfigmessage.ObjectFactory;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigFaultMessageType;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.ReplyType;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class GetMeterConfigTest extends AbstractMockMeterConfig {
    private com.energyict.mdc.device.data.DeviceBuilder deviceBuilder;
    protected final ObjectFactory getMeterConfigMessageObjectFactory = new ObjectFactory();

    @Before
    public void setUp() throws Exception {
        deviceBuilder = FakeBuilder.initBuilderStub(device, com.energyict.mdc.device.data.DeviceBuilder.class);
        when(deviceService.newDeviceBuilder(deviceConfiguration, DEVICE_NAME, RECEIVED_DATE)).thenReturn(deviceBuilder);
        when(deviceService.findAllDevices(any(Condition.class))).thenReturn(FakeBuilder.initBuilderStub(Collections.emptyList(), Finder.class));
        when(state.getName()).thenReturn(STATE_NAME);
        mockDevice();
    }

    @Test
    public void testNoMetersInGetMeterConfig() throws Exception {
        // Prepare request
        GetMeterConfig getMeterConfig = new GetMeterConfig();
        GetMeterConfigRequestMessageType getMeterConfigRequest = createGetMeterConfigRequest(getMeterConfig);

        try {
            // Business method
            getInstance(GetMeterConfigEndpoint.class).getMeterConfig(getMeterConfigRequest);
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
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.EMPTY_LIST.translate(thesaurus, "GetMeterConfig.Meter"));
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    protected GetMeterConfigRequestMessageType createGetMeterConfigRequest(GetMeterConfig getMeterConfig) {
        GetMeterConfigPayloadType getMeterConfigPayload = getMeterConfigMessageObjectFactory.createGetMeterConfigPayloadType();
        getMeterConfigPayload.setGetMeterConfig(getMeterConfig);
        GetMeterConfigRequestMessageType getMeterConfigRequestMessage = getMeterConfigMessageObjectFactory.createGetMeterConfigRequestMessageType();
        getMeterConfigRequestMessage.setPayload(getMeterConfigPayload);
        getMeterConfigRequestMessage.setHeader(cimMessageObjectFactory.createHeaderType());
        return getMeterConfigRequestMessage;
    }


}
