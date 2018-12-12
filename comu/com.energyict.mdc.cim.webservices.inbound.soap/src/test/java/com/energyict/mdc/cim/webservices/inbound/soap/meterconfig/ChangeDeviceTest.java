/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.AbstractMockMeterConfig;

import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfig.MeterMultiplier;
import ch.iec.tc57._2011.meterconfig.ProductAssetModel;
import ch.iec.tc57._2011.meterconfig.SimpleEndDeviceFunction;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigRequestMessageType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigResponseMessageType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ChangeDeviceTest extends AbstractMockMeterConfig {

    @Before
    public void setUp() throws Exception {
        when(deviceService.findDeviceByMrid(DEVICE_MRID)).thenReturn(Optional.of(device));
        when(deviceService.findDeviceByName(DEVICE_NAME)).thenReturn(Optional.of(device));
        when(deviceService.findAllDevices(any(Condition.class))).thenReturn(FakeBuilder.initBuilderStub(Collections.emptyList(), Finder.class));
        when(state.getName()).thenReturn(STATE_NAME);
        mockDevice();
    }

    @Test
    public void testChangeDeviceSuccessfully() throws Exception {
        // Prepare request
        MeterConfig meterConfig = new MeterConfig();
        SimpleEndDeviceFunction simpleEndDeviceFunction = createDefaultEndDeviceFunction();
        meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createDefaultMeter();
        meter.setLotNumber("");
        meter.setConfigurationEvents(createConfigurationEvent());
        meter.setStatus(createStatus());
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        // Business method
        MeterConfigResponseMessageType response = getInstance(ExecuteMeterConfigEndpoint.class).changeMeterConfig(meterConfigRequest);

        // Assert invocations
        verify(deviceService).findDeviceByMrid(DEVICE_MRID);
        verify(device).setSerialNumber(SERIAL_NUMBER);
        verify(device).setManufacturer(MANUFACTURER);
        verify(device).setModelNumber(MODEL_NUMBER);
        verify(device).setModelVersion(MODEL_VERSION);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CHANGED);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterConfig");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);

        MeterConfig responseMeterConfig = response.getPayload().getMeterConfig();
        assertThat(responseMeterConfig.getMeter()).hasSize(1);

        Meter responseMeter = responseMeterConfig.getMeter().get(0);
        assertThat(responseMeter.getMRID()).isEqualTo(DEVICE_MRID);
        assertThat(responseMeter.getNames().get(0).getName()).isEqualTo(DEVICE_NAME);
        assertThat(responseMeter.getSerialNumber()).isEqualTo(SERIAL_NUMBER);
        assertThat(responseMeter.getType()).isEqualTo(DEVICE_TYPE_NAME);
        assertThat(responseMeter.getLotNumber()).isEqualTo(BATCH);

        ProductAssetModel assetModel = responseMeter.getEndDeviceInfo().getAssetModel();
        assertThat(assetModel.getManufacturer().getNames().get(0).getName()).isEqualTo(MANUFACTURER);
        assertThat(assetModel.getModelNumber()).isEqualTo(MODEL_NUMBER);
        assertThat(assetModel.getModelVersion()).isEqualTo(MODEL_VERSION);

        List<MeterMultiplier> meterMultipliers = responseMeter.getMeterMultipliers();
        assertThat(meterMultipliers).hasSize(1);
        assertThat(meterMultipliers.get(0).getValue()).isEqualTo(MULTIPLIER);

        assertThat(responseMeter.getStatus()).isNotNull();
        assertThat(responseMeter.getStatus().getValue()).isEqualTo(STATE_NAME);

        List<Object> refs = responseMeter.getComFunctionOrConnectDisconnectFunctionOrSimpleEndDeviceFunction();
        assertThat(refs).hasSize(1);
        assertThat(refs.get(0)).isInstanceOf(Meter.SimpleEndDeviceFunction.class);
        assertThat(((Meter.SimpleEndDeviceFunction) refs.get(0)).getRef()).isEqualTo(DEVICE_CONFIG_ID);

        List<SimpleEndDeviceFunction> responseSimpleEndDeviceFunction = responseMeterConfig.getSimpleEndDeviceFunction();
        assertThat(responseSimpleEndDeviceFunction).hasSize(1);
        assertThat(responseSimpleEndDeviceFunction.get(0).getMRID()).isEqualTo(DEVICE_CONFIG_ID);
        assertThat(responseSimpleEndDeviceFunction.get(0).getConfigID()).isEqualTo(DEVICE_CONFIGURATION_NAME);
    }
}
