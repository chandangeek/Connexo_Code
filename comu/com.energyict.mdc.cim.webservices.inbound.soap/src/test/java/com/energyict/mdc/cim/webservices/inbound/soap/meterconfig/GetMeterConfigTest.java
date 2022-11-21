/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.OverlapCalculatorBuilder;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.AbstractMockMeterConfig;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.outbound.soap.PingResult;
import com.energyict.mdc.common.device.data.Device;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfig.MeterMultiplier;
import ch.iec.tc57._2011.meterconfig.ProductAssetModel;
import ch.iec.tc57._2011.meterconfig.SimpleEndDeviceFunction;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigFaultMessageType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigRequestMessageType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigResponseMessageType;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.google.common.collect.Range;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class GetMeterConfigTest extends AbstractMockMeterConfig {
    private static final String NOUN = "MeterConfig";
    private static final String METER_ITEM = NOUN + ".Meter";


    private com.energyict.mdc.device.data.DeviceBuilder deviceBuilder;
    @Mock
    private OverlapCalculatorBuilder overlapCalculatorBuilder;

    private ExecuteMeterConfigEndpoint executeMeterConfigEndpoint;

    @Before
    public void setUp() throws Exception {
        executeMeterConfigEndpoint = getInstance(ExecuteMeterConfigEndpoint.class);
        Field webServiceContextField = AbstractInboundEndPoint.class.getDeclaredField("webServiceContext");
        webServiceContextField.setAccessible(true);
        webServiceContextField.set(executeMeterConfigEndpoint, webServiceContext);
        when(messageContext.get(anyString())).thenReturn(1l);
        when(webServiceContext.getMessageContext()).thenReturn(messageContext);
        inject(AbstractInboundEndPoint.class, executeMeterConfigEndpoint, "threadPrincipalService", threadPrincipalService);
        inject(AbstractInboundEndPoint.class, executeMeterConfigEndpoint, "webServicesService", webServicesService);
        inject(AbstractInboundEndPoint.class, executeMeterConfigEndpoint, "transactionService", transactionService);
        when(transactionService.execute(any())).then(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return ((ExceptionThrowingSupplier) invocationOnMock.getArguments()[0]).get();
            }
        });
        when(webServicesService.getOngoingOccurrence(1l)).thenReturn(webServiceCallOccurrence);
        when(webServiceCallOccurrence.getApplicationName()).thenReturn(Optional.of("ApplicationName"));
        when(webServiceCallOccurrence.getRequest()).thenReturn(Optional.of("Request"));

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
    public void testNoMetersInMeterConfig() {
        // Prepare request
        MeterConfig meterConfig = new MeterConfig();
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        try {
            // Business method
            executeMeterConfigEndpoint.getMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.EMPTY_LIST.translate(thesaurus, METER_ITEM));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode()).isEqualTo(MessageSeeds.EMPTY_LIST.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.EMPTY_LIST.translate(thesaurus, METER_ITEM));
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testGetMeterConfigSuccessfully() throws Exception {
        mockMeterConfigFactoryWithCas();

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
        MeterConfigResponseMessageType response = executeMeterConfigEndpoint.getMeterConfig(meterConfigRequest);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
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

        List<SimpleEndDeviceFunction> responseSimpleEndDeviceFunction = responseMeterConfig
                .getSimpleEndDeviceFunction();
        assertThat(responseSimpleEndDeviceFunction).hasSize(1);
        assertThat(responseSimpleEndDeviceFunction.get(0).getMRID()).isEqualTo(DEVICE_CONFIG_ID);
        assertThat(responseSimpleEndDeviceFunction.get(0).getConfigID()).isEqualTo(DEVICE_CONFIGURATION_NAME);

        assertThat(responseMeter.getMeterCustomAttributeSet()).hasSize(3);
        assertThat(responseMeter.getMeterCustomAttributeSet().get(0).getAttribute().get(0).getName()).isEqualTo(GA_NAME_1);
        assertThat(responseMeter.getMeterCustomAttributeSet().get(0).getAttribute().get(0).getValue()).isEqualTo(GA_VALUE_1);
        assertThat(responseMeter.getMeterCustomAttributeSet().get(0).getAttribute().get(1).getName()).isEqualTo(GA_NAME_2);
        assertThat(responseMeter.getMeterCustomAttributeSet().get(0).getAttribute().get(1).getValue()).isEqualTo(GA_VALUE_2);
        assertThat(responseMeter.getMeterCustomAttributeSet().get(1).getId()).isEqualTo(NON_VERSIONED_CPS_ID);
        assertThat(responseMeter.getMeterCustomAttributeSet().get(1).getAttribute().get(0).getName()).isEqualTo(CPS_NAME_1);
        assertThat(responseMeter.getMeterCustomAttributeSet().get(1).getAttribute().get(0).getValue()).isEqualTo(CPS_VALUE_1);
        assertThat(responseMeter.getMeterCustomAttributeSet().get(1).getAttribute().get(1).getName()).isEqualTo(CPS_NAME_2);
        assertThat(responseMeter.getMeterCustomAttributeSet().get(1).getAttribute().get(1).getValue()).isEqualTo(CPS_VALUE_2);
        assertThat(responseMeter.getMeterCustomAttributeSet().get(2).getId()).isEqualTo(VERSIONED_CPS_ID);
    }

    @Test
    public void testGetMeterConfigSuccessfullyWithSystemMeterStatusSource() throws Exception {
        when(meterConfigFactory.asGetMeterConfig(any(Device.class), any(PingResult.class), eq(true))).thenReturn(meterConfig);
        when(meterConfig.getMeter()).thenReturn(Arrays.asList(createMeterWithMeterStatus()));

        // Prepare request
        MeterConfig meterConfigInRequest = new MeterConfig();
        meterConfigInRequest.setMeterStatusSource(MeterStatusSource.SYSTEM.toString());
        SimpleEndDeviceFunction simpleEndDeviceFunction = createDefaultEndDeviceFunction();
        meterConfigInRequest.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createDefaultMeter();
        meterConfigInRequest.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfigInRequest);

        // Business method
        MeterConfigResponseMessageType response = executeMeterConfigEndpoint.getMeterConfig(meterConfigRequest);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
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

        List<SimpleEndDeviceFunction> responseSimpleEndDeviceFunction = responseMeterConfig
                .getSimpleEndDeviceFunction();
        assertThat(responseSimpleEndDeviceFunction).hasSize(1);
        assertThat(responseSimpleEndDeviceFunction.get(0).getMRID()).isEqualTo(DEVICE_CONFIG_ID);
        assertThat(responseSimpleEndDeviceFunction.get(0).getConfigID()).isEqualTo(DEVICE_CONFIGURATION_NAME);

        assertThat(responseMeter.getMeterStatus().getStatusInfoComTaskExecutionStatus()).isEqualTo(COM_TASK_EXECUTION_STATUS);
        assertThat(responseMeter.getMeterStatus().getStatusInfoComTaskExecutionDate()).isEqualTo(COM_TASK_EXECUTION_DATE);
        assertThat(responseMeter.getMeterStatus().getFirmwareStatus().getMeterFirmware().getVersion()).isEqualTo(FIRMWARE_VERSION);
        assertThat(responseMeter.getMeterStatus().getFirmwareStatus().getMeterFirmware().getImageIdentifier()).isEqualTo(FIRMWARE_IMAGE_IDENTIFIER);
        assertThat(responseMeter.getMeterStatus().getFirmwareStatus().getMeterFirmware().getUploadDate()).isEqualTo(FIRMWARE_UPLOAD_DATE);
        assertThat(responseMeter.getMeterStatus().getFirmwareStatus().getMeterFirmware().getActivationDate()).isEqualTo(FIRMWARE_ACTIVATION_DATE);
        assertThat(responseMeter.getMeterStatus().getFirmwareStatus().getCommunicationFirmware()).isNull();
        assertThat(responseMeter.getMeterStatus().getFirmwareStatus().getAuxiliaryFirmware()).isNull();
        assertThat(responseMeter.getMeterStatus().getFirmwareStatus().getImage()).isNull();
        assertThat(responseMeter.getMeterStatus().getCalendarStatus().getCalendar()).isEqualTo(CALENDAR_NAME);
        assertThat(responseMeter.getMeterStatus().getCalendarStatus().getUploadDate()).isEqualTo(CALENDAR_UPLOAD_DATE);
        assertThat(responseMeter.getMeterStatus().getCalendarStatus().getActivationDate()).isEqualTo(CALENDAR_ACTIVATION_DATE);
        assertThat(responseMeter.getMeterStatus().getContactorStatus().getStatus()).isEqualTo(CONTACTOR_STATUS);
        assertThat(responseMeter.getMeterStatus().getContactorStatus().getSentDate()).isEqualTo(CONTACTOR_SENT_DATE);
        assertThat(responseMeter.getMeterStatus().getContactorStatus().getActivationDate()).isEqualTo(CONTACTOR_ACTIVATION_DATE);
    }

    @Test
    public void testGetMeterConfigNotSupportedMeterStatusSource() throws Exception {
        // Prepare request
        MeterConfig meterConfig = new MeterConfig();
        meterConfig.setMeterStatusSource(MeterStatusSource.METER.getSource());
        SimpleEndDeviceFunction simpleEndDeviceFunction = createDefaultEndDeviceFunction();
        meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createDefaultMeter();
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        try {
            // Business method
            executeMeterConfigEndpoint.getMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.METER_STATUS_NOT_SUPPORTED.translate(thesaurus, "MeterStatusSource", MeterStatusSource.METER.getSource(), "'" + MeterStatusSource.SYSTEM
                    .getSource() + "'"));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode()).isEqualTo(MessageSeeds.METER_STATUS_NOT_SUPPORTED.getErrorCode());
        }
    }

    @Test
    public void testGetMeterConfigAsyncNotSupportedMeterStatusSource() throws Exception {
        // Prepare request
        MeterConfig meterConfig = new MeterConfig();
        meterConfig.setMeterStatusSource("testStatus");
        SimpleEndDeviceFunction simpleEndDeviceFunction = createDefaultEndDeviceFunction();
        meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createDefaultMeter();
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);
        meterConfigRequest.getHeader().setAsyncReplyFlag(true);

        try {
            // Business method
            executeMeterConfigEndpoint.getMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.METER_STATUS_NOT_SUPPORTED.translate(thesaurus, "MeterStatusSource", "testStatus", "'" + MeterStatusSource.SYSTEM.getSource() + "', '" + MeterStatusSource.METER
                    .getSource() + "'"));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode()).isEqualTo(MessageSeeds.METER_STATUS_NOT_SUPPORTED.getErrorCode());
        }
    }

    @Test
    public void testWrongPingField() throws Exception {
        // Prepare request
        MeterConfig meterConfigInRequest = new MeterConfig();
        meterConfigInRequest.setPing("WrongPingField");
        SimpleEndDeviceFunction simpleEndDeviceFunction = createDefaultEndDeviceFunction();
        meterConfigInRequest.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createDefaultMeter();
        meterConfigInRequest.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfigInRequest);

        try {
            // Business method
            executeMeterConfigEndpoint.getMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo("Element 'Ping' contains unsupported value 'WrongPingField'. Must be one of: 'Yes' or 'No'.");
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode()).isEqualTo("SIM1048");
        }
    }

    @Test
    public void testSuccessfullyPing() throws Exception {
        when(meterConfigFactory.asGetMeterConfig(any(Device.class), any(PingResult.class), eq(true))).thenReturn(meterConfig);
        when(meterConfig.getMeter()).thenReturn(Arrays.asList(createMeterWithPing(PingResult.SUCCESSFUL)));
        when(meterConfigPingUtils.ping(any(Device.class))).thenReturn(Optional.empty());

        // Prepare request
        MeterConfig meterConfigInRequest = new MeterConfig();
        meterConfigInRequest.setPing("Yes");
        SimpleEndDeviceFunction simpleEndDeviceFunction = createDefaultEndDeviceFunction();
        meterConfigInRequest.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createDefaultMeter();
        meterConfigInRequest.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfigInRequest);

        // Business method
        MeterConfigResponseMessageType response = executeMeterConfigEndpoint.getMeterConfig(meterConfigRequest);

        // Assert response
        checkMeterConfigResponse(response);
        assertThat(response.getPayload().getMeterConfig().getMeter().get(0).getPingResult()).isEqualTo("Successful");
    }

    @Test
    public void testFailedPing() throws Exception {
        when(meterConfigFactory.asGetMeterConfig(any(Device.class), any(PingResult.class), eq(true))).thenReturn(meterConfig);
        when(meterConfig.getMeter()).thenReturn(Arrays.asList(createMeterWithPing(PingResult.FAILED)));

        // Prepare request
        MeterConfig meterConfigInRequest = new MeterConfig();
        meterConfigInRequest.setPing("Yes");
        SimpleEndDeviceFunction simpleEndDeviceFunction = createDefaultEndDeviceFunction();
        meterConfigInRequest.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createDefaultMeter();
        meterConfigInRequest.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfigInRequest);

        // Business method
        MeterConfigResponseMessageType response = executeMeterConfigEndpoint.getMeterConfig(meterConfigRequest);

        // Assert response
        checkMeterConfigResponse(response);
        assertThat(response.getPayload().getMeterConfig().getMeter().get(0).getPingResult()).isEqualTo("Failed");
        assertThat(response.getReply().getError()).hasSize(1);
        ErrorType error = response.getReply().getError().get(0);
        assertThat(error.getLevel()).isEqualTo(ErrorType.Level.WARNING);
        assertThat(error.getCode())
                .isEqualTo(MessageSeeds.MISSING_HOST_PORT.getErrorCode());
        assertThat(error.getDetails()).isEqualTo(MessageSeeds.MISSING_HOST_PORT
                .translate(thesaurus));
    }

    @Test
    public void testNoReplyAddress() throws Exception {
        MeterConfig meterConfig = new MeterConfig();
        meterConfig.getMeter().add(createDefaultMeter());
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);
        meterConfigRequest.getHeader().setAsyncReplyFlag(true);

        try {
            // Business method
            executeMeterConfigEndpoint.createMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
        }
    }

    @Test
    public void testOutboundNotConfigured() {
        MeterConfig meterConfig = new MeterConfig();
        meterConfig.getMeter().add(createDefaultMeter());
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);
        meterConfigRequest.getHeader().setAsyncReplyFlag(true);
        meterConfigRequest.getHeader().setReplyAddress(REPLY_ADDRESS);

        EndPointConfiguration endPointConfiguration = mockEndPointConfiguration("epc1");
        when(endPointConfiguration.getUrl()).thenReturn(REPLY_ADDRESS + "_1");
        Finder<EndPointConfiguration> finder = mockFinder(Collections.singletonList(endPointConfiguration));
        when(endPointConfigurationService.findEndPointConfigurations()).thenReturn(finder);

        try {
            // Business method
            executeMeterConfigEndpoint.getMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.NO_END_POINT_WITH_URL.translate(thesaurus, REPLY_ADDRESS));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode()).isEqualTo(MessageSeeds.NO_END_POINT_WITH_URL.getErrorCode());
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    private void checkMeterConfigResponse(MeterConfigResponseMessageType response) {
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.REPLY);
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

        List<SimpleEndDeviceFunction> responseSimpleEndDeviceFunction = responseMeterConfig
                .getSimpleEndDeviceFunction();
        assertThat(responseSimpleEndDeviceFunction).hasSize(1);
        assertThat(responseSimpleEndDeviceFunction.get(0).getMRID()).isEqualTo(DEVICE_CONFIG_ID);
        assertThat(responseSimpleEndDeviceFunction.get(0).getConfigID()).isEqualTo(DEVICE_CONFIGURATION_NAME);
    }
}
