/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.AbstractMockMeterConfig;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.common.device.data.Device;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigFaultMessageType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigRequestMessageType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigResponseMessageType;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeleteDeviceTest extends AbstractMockMeterConfig {
    private static final String USAGE_POINT_NAME = "usage point name";

    private ExecuteMeterConfigEndpoint executeMeterConfigEndpoint;

    @Mock
    private Device gateway;
    @Mock
    private Device slave;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint metrologyConfigurationOnUsagePoint;
    @Mock
    private UsagePointMetrologyConfiguration metrologyConfiguration;

    @Before
    public void setUp() throws Exception {
        executeMeterConfigEndpoint = getInstance(ExecuteMeterConfigEndpoint.class);
        Field webServiceContextField = AbstractInboundEndPoint.class.getDeclaredField("webServiceContext");
        webServiceContextField.setAccessible(true);
        webServiceContextField.set(executeMeterConfigEndpoint, webServiceContext);
        when(messageContext.get(anyString())).thenReturn(1L);
        when(webServiceContext.getMessageContext()).thenReturn(messageContext);
        inject(AbstractInboundEndPoint.class, executeMeterConfigEndpoint, "threadPrincipalService", threadPrincipalService);
        inject(AbstractInboundEndPoint.class, executeMeterConfigEndpoint, "webServicesService", webServicesService);
        inject(AbstractInboundEndPoint.class, executeMeterConfigEndpoint, "webServiceCallOccurrenceService", webServiceCallOccurrenceService);
        inject(AbstractInboundEndPoint.class, executeMeterConfigEndpoint, "transactionService", transactionService);
        when(webServiceCallOccurrenceService.getOngoingOccurrence(1L)).thenReturn(webServiceCallOccurrence);
        when(webServiceCallOccurrence.getApplicationName()).thenReturn(Optional.of("ApplicationName"));
        when(webServiceCallOccurrence.getRequest()).thenReturn(Optional.of("Request"));
        when(deviceService.findDeviceByMrid(DEVICE_MRID)).thenReturn(Optional.of(device));
        when(deviceService.findDeviceByName(DEVICE_NAME)).thenReturn(Optional.of(device));
        mockDevice();
        when(topologyService.getPhysicalGateway(any(Device.class))).thenReturn(Optional.empty());
    }

    @Test
    public void testDeleteDeviceSuccessfully() throws Exception {
        // Prepare request
        MeterConfig meterConfig = new MeterConfig();
        Meter meter = createDefaultMeter();
        meter.setConfigurationEvents(createConfigurationEvent());
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        // Business method
        MeterConfigResponseMessageType response = executeMeterConfigEndpoint.deleteMeterConfig(meterConfigRequest);

        // Assert invocations
        verify(deviceService).findDeviceByMrid(DEVICE_MRID);
        SetMultimap<String, String> values = HashMultimap.create();
        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
        values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), DEVICE_MRID);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);
        verify(device).delete();

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.DELETED);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterConfig");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
    }

    @Test
    public void testDeleteDeviceFailsGapsNotAllowed() throws Exception {
        when(device.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getName()).thenReturn(USAGE_POINT_NAME);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(metrologyConfigurationOnUsagePoint));
        when(metrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(metrologyConfiguration.areGapsAllowed()).thenReturn(false);
        // Prepare request
        MeterConfig meterConfig = new MeterConfig();
        Meter meter = createDefaultMeter();
        meter.setConfigurationEvents(createConfigurationEvent());
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        try {
            // Business method
            executeMeterConfigEndpoint.deleteMeterConfig(meterConfigRequest);

            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage())
                    .isEqualTo(MessageSeeds.METROLOGY_CONFIG_NOT_ALLOW_GAPS.translate(thesaurus, DEVICE_NAME, SERIAL_NUMBER, USAGE_POINT_NAME));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode())
                    .isEqualTo(MessageSeeds.METROLOGY_CONFIG_NOT_ALLOW_GAPS.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.METROLOGY_CONFIG_NOT_ALLOW_GAPS
                    .translate(thesaurus, DEVICE_NAME, SERIAL_NUMBER, USAGE_POINT_NAME));
            SetMultimap<String, String> values = HashMultimap.create();
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), DEVICE_MRID);
            verify(webServiceCallOccurrence).saveRelatedAttributes(values);
        }
    }

    @Test
    public void testDeleteDeviceSuccessGapsAllowed() throws Exception {
        when(device.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        when(usagePoint.getName()).thenReturn(USAGE_POINT_NAME);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(metrologyConfigurationOnUsagePoint));
        when(metrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(metrologyConfiguration.areGapsAllowed()).thenReturn(true);
        // Prepare request
        MeterConfig meterConfig = new MeterConfig();
        Meter meter = createDefaultMeter();
        meter.setConfigurationEvents(createConfigurationEvent());
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        // Business method
        MeterConfigResponseMessageType response = executeMeterConfigEndpoint.deleteMeterConfig(meterConfigRequest);

        // Assert invocations
        verify(deviceService).findDeviceByMrid(DEVICE_MRID);
        SetMultimap<String, String> values = HashMultimap.create();
        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
        values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), DEVICE_MRID);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);
        verify(device).delete();

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.DELETED);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterConfig");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
    }

    @Test
    public void testDeleteDeviceFailsCantRemoveGateway() throws Exception {
        when(topologyService.getSlaveDevices(any(Device.class))).thenReturn(Arrays.asList(slave));
        // Prepare request
        MeterConfig meterConfig = new MeterConfig();
        Meter meter = createDefaultMeter();
        meter.setConfigurationEvents(createConfigurationEvent());
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        try {
            // Business method
            executeMeterConfigEndpoint.deleteMeterConfig(meterConfigRequest);

            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage())
                    .isEqualTo(MessageSeeds.CANT_REMOVE_GATEWAY.translate(thesaurus, DEVICE_NAME, SERIAL_NUMBER));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode())
                    .isEqualTo(MessageSeeds.CANT_REMOVE_GATEWAY.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.CANT_REMOVE_GATEWAY
                    .translate(thesaurus, DEVICE_NAME, SERIAL_NUMBER));
            SetMultimap<String, String> values = HashMultimap.create();
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), DEVICE_MRID);
            verify(webServiceCallOccurrence).saveRelatedAttributes(values);
        }
    }

    @Test
    public void testDeleteDeviceSuccessUnlinkSlave() throws Exception {
        when(topologyService.getPhysicalGateway(device)).thenReturn(Optional.of(gateway));
        // Prepare request
        MeterConfig meterConfig = new MeterConfig();
        Meter meter = createDefaultMeter();
        meter.setConfigurationEvents(createConfigurationEvent());
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        // Business method
        MeterConfigResponseMessageType response = executeMeterConfigEndpoint.deleteMeterConfig(meterConfigRequest);

        // Assert invocations
        verify(deviceService).findDeviceByMrid(DEVICE_MRID);
        SetMultimap<String, String> values = HashMultimap.create();
        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
        values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), DEVICE_MRID);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);
        verify(topologyService).clearPhysicalGateway(device);
        verify(device).delete();

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.DELETED);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterConfig");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
    }
}
