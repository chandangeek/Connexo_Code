/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.OverlapCalculatorBuilder;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.metering.DefaultState;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.AbstractMockMeterConfig;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.meterconfig.EndDeviceInfo;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfig.MeterMultiplier;
import ch.iec.tc57._2011.meterconfig.ProductAssetModel;
import ch.iec.tc57._2011.meterconfig.SimpleEndDeviceFunction;
import ch.iec.tc57._2011.meterconfig.Status;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigFaultMessageType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigRequestMessageType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigResponseMessageType;
import ch.iec.tc57._2011.schema.message.ErrorType;
import ch.iec.tc57._2011.schema.message.HeaderType;
import ch.iec.tc57._2011.schema.message.ReplyType;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Range;
import com.google.common.collect.SetMultimap;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreateDeviceTest extends AbstractMockMeterConfig {
    private static final String NOUN = "MeterConfig";
    private static final String METER_ITEM = NOUN + ".Meter";
    private static final String METER_LIFECYCLE = METER_ITEM + ".lifecycle";
    private static final String METER_LIFECYCLE_DATE = METER_LIFECYCLE + ".receivedDate";
    private static final String METER_SIMPLE_FUNCTION = METER_ITEM + ".SimpleEndDeviceFunction";
    private static final String METER_CONFIG_FUNCTION = NOUN + ".SimpleEndDeviceFunction";
    private static final String NON_VERSIONED_CPS_ID = "my cps id";
    private static final String NAME2 = "name 2";
    private static final String VALUE2 = "value 2";
    private static final String NO_SUCH_DEVICE_TYPE_TEXT = "no such device type";
    private static final String NO_SUCH_DEVICE_CONFIG_TEXT = "no such device config";

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
        when(messageContext.get(anyString())).thenReturn(1L);
        when(webServiceContext.getMessageContext()).thenReturn(messageContext);
        inject(AbstractInboundEndPoint.class, executeMeterConfigEndpoint, "threadPrincipalService", threadPrincipalService);
        inject(AbstractInboundEndPoint.class, executeMeterConfigEndpoint, "webServicesService", webServicesService);
        inject(AbstractInboundEndPoint.class, executeMeterConfigEndpoint, "webServiceCallOccurrenceService", webServiceCallOccurrenceService);
        inject(AbstractInboundEndPoint.class, executeMeterConfigEndpoint, "transactionService", transactionService);
        when(webServiceCallOccurrenceService.getOngoingOccurrence(1L)).thenReturn(webServiceCallOccurrence);
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
    }

    @Test
    public void testNoMetersInMeterConfig() throws Exception {
        // Prepare request
        MeterConfig meterConfig = new MeterConfig();
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        try {
            // Business method
            executeMeterConfigEndpoint.createMeterConfig(meterConfigRequest);
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
    public void testCreateDeviceSuccessfully() throws Exception {
        // Prepare request
        MeterConfig meterConfig = new MeterConfig();
        SimpleEndDeviceFunction simpleEndDeviceFunction = createDefaultEndDeviceFunction();
        meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createDefaultMeter();
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        // Business method
        MeterConfigResponseMessageType response = executeMeterConfigEndpoint.createMeterConfig(meterConfigRequest);

        // Assert invocations
        verify(deviceService).newDeviceBuilder(deviceConfiguration, DEVICE_NAME, RECEIVED_DATE);
        verify(deviceBuilder).withBatch(BATCH);
        verify(deviceBuilder).withSerialNumber(SERIAL_NUMBER);
        verify(deviceBuilder).withManufacturer(MANUFACTURER);
        verify(deviceBuilder).withModelNumber(MODEL_NUMBER);
        verify(deviceBuilder).withModelVersion(MODEL_VERSION);
        verify(deviceBuilder).withMultiplier(BigDecimal.valueOf(MULTIPLIER));
        verify(deviceBuilder).create();

        SetMultimap<String, String> values = HashMultimap.create();

        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
        values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), DEVICE_MRID);
        values.put(CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), SERIAL_NUMBER);

        verify(webServiceCallOccurrence).saveRelatedAttributes(values);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CREATED);
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

    @Test
    public void testCreateDeviceWithCpsShouldFailWhenCpsIsNotFound() throws Exception {
        try {
            when(customPropertySetService.findActiveCustomPropertySet(NON_VERSIONED_CPS_ID))
                    .thenReturn(Optional.empty());
            doTestCreateDeviceWithCps();
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            assertThat(faultMessage.getMessage())
                    .isEqualTo(MessageSeeds.CANT_FIND_CUSTOM_ATTRIBUTE_SET.translate(thesaurus, NON_VERSIONED_CPS_ID));
        }
    }

    @Test
    public void testCreateDeviceWithCpsShouldFailWhenAttributeIsNotFound() throws Exception {
        try {
            mockCustomPropertySetService();
            doTestCreateDeviceWithCps();
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            assertThat(faultMessage.getMessage())
                    .isEqualTo(MessageSeeds.CANT_FIND_CUSTOM_ATTRIBUTE.translate(thesaurus, NAME2, NON_VERSIONED_CPS_ID));
        }
    }

    @Test
    public void testCreateDeviceWithCpsShouldFailWhenAttributeValueCannotBeConverted() throws Exception {
        try {
            mockCustomPropertySetService();
            mockCustomPropertySetSpecs(false);
            doTestCreateDeviceWithCps();
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            assertThat(faultMessage.getMessage())
                    .isEqualTo(MessageSeeds.CANT_CONVERT_VALUE_OF_CUSTOM_ATTRIBUTE.translate(thesaurus, VALUE2, NAME2, NON_VERSIONED_CPS_ID));
        }
    }

    @Test
    public void testCreateDeviceWithCpsShouldFailWhenValuesCannotBeAssigned() throws Exception {
        try {
            mockCustomPropertySetService();
            mockCustomPropertySetSpecs(true);
            doThrow(RuntimeException.class).when(customPropertySetService)
                    .setValuesFor(eq(customNonVersionedPropertySet), eq(device), any(CustomPropertySetValues.class));
            doTestCreateDeviceWithCps();
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            assertThat(faultMessage.getMessage())
                    .isEqualTo(MessageSeeds.CANT_ASSIGN_VALUES_FOR_CUSTOM_ATTRIBUTE_SET.translate(thesaurus, NON_VERSIONED_CPS_ID));
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
        MeterConfigResponseMessageType response = executeMeterConfigEndpoint.createMeterConfig(meterConfigRequest);

        // Assert invocations
        verify(customPropertySetService).setValuesFor(eq(customNonVersionedPropertySet), eq(device),
                any(CustomPropertySetValues.class));
        verify(customPropertySetService).setValuesVersionFor(eq(customVersionedPropertySet), eq(device),
                any(CustomPropertySetValues.class), any(Range.class));
        SetMultimap<String, String> values = HashMultimap.create();

        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
        values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), DEVICE_MRID);
        values.put(CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), SERIAL_NUMBER);

        verify(webServiceCallOccurrence).saveRelatedAttributes(values);


        // Assert response
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);
    }

    @Test
    public void testCreateDeviceSuccessfullyWithoutOptionalParameters() throws Exception {
        // Prepare request
        MeterConfig meterCfg = new MeterConfig();
        SimpleEndDeviceFunction simpleEndDeviceFunction = createDefaultEndDeviceFunction();
        meterCfg.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createMeter();
        meter.setMRID(DEVICE_MRID);
        meter.setSerialNumber(null);
        meter.setLotNumber(null);
        EndDeviceInfo endDeviceInfo = new EndDeviceInfo();
        endDeviceInfo.setAssetModel(new ProductAssetModel());
        meter.setEndDeviceInfo(endDeviceInfo);
        meter.getMeterMultipliers().add(createMeterMultiplier(1));
        Status status = new Status();
        status.setValue(DefaultState.COMMISSIONING.getDefaultFormat());
        meter.setStatus(status);
        meterCfg.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterCfg);

        // Re-mock device
        when(device.getSerialNumber()).thenReturn(null);
        when(device.getMultiplier()).thenReturn(BigDecimal.ONE);
        when(device.getBatch()).thenReturn(Optional.empty());
        when(device.getManufacturer()).thenReturn(null);
        when(device.getModelNumber()).thenReturn(null);
        when(device.getModelVersion()).thenReturn(null);
        when(state.getName()).thenReturn(DefaultState.COMMISSIONING.getKey());
        when(meterConfig.getMeter()).thenReturn(Arrays.asList(meter));

        // Business method
        MeterConfigResponseMessageType response = executeMeterConfigEndpoint.createMeterConfig(meterConfigRequest);

        // Assert invocations
        verify(deviceService).newDeviceBuilder(deviceConfiguration, DEVICE_NAME, RECEIVED_DATE);
        verify(deviceBuilder).withBatch(null);
        verify(deviceBuilder).withSerialNumber(null);
        verify(deviceBuilder).withManufacturer(null);
        verify(deviceBuilder).withModelNumber(null);
        verify(deviceBuilder).withModelVersion(null);
        verify(deviceBuilder).create();
        SetMultimap<String, String> values = HashMultimap.create();

        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
        values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), DEVICE_MRID);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);

        // Assert response
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CREATED);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterConfig");
        assertThat(response.getReply().getResult()).isEqualTo(ReplyType.Result.OK);

        MeterConfig responseMeterConfig = response.getPayload().getMeterConfig();
        assertThat(responseMeterConfig.getMeter()).hasSize(1);

        Meter responseMeter = responseMeterConfig.getMeter().get(0);
        assertThat(responseMeter.getMRID()).isEqualTo(DEVICE_MRID);
        assertThat(responseMeter.getNames().get(0).getName()).isEqualTo(DEVICE_NAME);
        assertThat(responseMeter.getSerialNumber()).isNull();
        assertThat(responseMeter.getType()).isEqualTo(DEVICE_TYPE_NAME);
        assertThat(responseMeter.getLotNumber()).isNull();

        ProductAssetModel assetModel = responseMeter.getEndDeviceInfo().getAssetModel();
        assertThat(assetModel.getManufacturer()).isNull();
        assertThat(assetModel.getModelNumber()).isNull();
        assertThat(assetModel.getModelVersion()).isNull();

        List<MeterMultiplier> meterMultipliers = responseMeter.getMeterMultipliers();
        assertThat(meterMultipliers).hasSize(1);
        assertThat(meterMultipliers.get(0).getValue()).isEqualTo(1);

        assertThat(responseMeter.getStatus()).isNotNull();
        assertThat(responseMeter.getStatus().getValue()).isEqualTo(DefaultState.COMMISSIONING.getDefaultFormat());

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

    @Test
    public void testSyncModeNotSupported() throws Exception {
        // Prepare request
        MeterConfig meterConfig = new MeterConfig();
        SimpleEndDeviceFunction simpleEndDeviceFunction = createDefaultEndDeviceFunction();
        meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createDefaultMeter();
        meterConfig.getMeter().add(meter);
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);
        meterConfigRequest.getHeader().setAsyncReplyFlag(false);

        try {
            // Business method
            executeMeterConfigEndpoint.createMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.UNABLE_TO_CREATE_DEVICE.translate(thesaurus));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode()).isEqualTo(MessageSeeds.SYNC_MODE_NOT_SUPPORTED.getErrorCode());

            SetMultimap<String, String> values = HashMultimap.create();
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), DEVICE_MRID);
            values.put(CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), SERIAL_NUMBER);

            verify(webServiceCallOccurrence).saveRelatedAttributes(values);
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testCreateDeviceFailedWithLocalizedException() throws Exception {
        MeterConfig meterConfig = new MeterConfig();
        SimpleEndDeviceFunction simpleEndDeviceFunction = createDefaultEndDeviceFunction();
        meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createDefaultMeter();
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        LocalizedException localizedException = mock(LocalizedException.class);
        when(localizedException.getLocalizedMessage()).thenReturn("ErrorMessage");
        when(localizedException.getErrorCode()).thenReturn("ERRORCODE");
        when(deviceService.newDeviceBuilder(deviceConfiguration, DEVICE_NAME, RECEIVED_DATE))
                .thenThrow(localizedException);

        try {
            // Business method
            executeMeterConfigEndpoint.createMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.UNABLE_TO_CREATE_DEVICE.translate(thesaurus));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode()).isEqualTo("ERRORCODE");
            assertThat(error.getDetails()).isEqualTo("ErrorMessage");
            SetMultimap<String, String> values = HashMultimap.create();
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), DEVICE_MRID);
            values.put(CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), SERIAL_NUMBER);
            verify(webServiceCallOccurrence).saveRelatedAttributes(values);
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testCreateDeviceFailedWithVerboseConstraintViolationException() throws Exception {
        MeterConfig meterConfig = new MeterConfig();
        SimpleEndDeviceFunction simpleEndDeviceFunction = createDefaultEndDeviceFunction();
        meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createDefaultMeter();
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        VerboseConstraintViolationException exception = mock(VerboseConstraintViolationException.class);
        when(exception.getLocalizedMessage()).thenReturn("ErrorMessage");
        when(deviceService.newDeviceBuilder(deviceConfiguration, DEVICE_NAME, RECEIVED_DATE)).thenThrow(exception);

        try {
            // Business method
            executeMeterConfigEndpoint.createMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.UNABLE_TO_CREATE_DEVICE.translate(thesaurus));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getDetails()).isEqualTo("ErrorMessage");
            SetMultimap<String, String> values = HashMultimap.create();
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), DEVICE_MRID);
            values.put(CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), SERIAL_NUMBER);
            verify(webServiceCallOccurrence).saveRelatedAttributes(values);
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testDeviceTypeNotFound() throws Exception {
        MeterConfig meterConfig = new MeterConfig();
        SimpleEndDeviceFunction simpleEndDeviceFunction = createDefaultEndDeviceFunction();
        meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createDefaultMeter();
        meter.setType("no such device type");
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        try {
            // Business method
            executeMeterConfigEndpoint.createMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.NO_SUCH_DEVICE_TYPE.translate(thesaurus, NO_SUCH_DEVICE_TYPE_TEXT));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(MessageSeeds.NO_SUCH_DEVICE_TYPE.getErrorTypeLevel());
            assertThat(error.getCode()).isEqualTo(MessageSeeds.NO_SUCH_DEVICE_TYPE.getErrorCode());
            assertThat(error.getDetails())
                    .isEqualTo(MessageSeeds.NO_SUCH_DEVICE_TYPE.translate(thesaurus, NO_SUCH_DEVICE_TYPE_TEXT));
            SetMultimap<String, String> values = HashMultimap.create();
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), DEVICE_MRID);
            values.put(CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), SERIAL_NUMBER);
            verify(webServiceCallOccurrence).saveRelatedAttributes(values);
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testDeviceConfigurationNotFound() throws Exception {
        MeterConfig meterConfig = new MeterConfig();
        SimpleEndDeviceFunction simpleEndDeviceFunction = createSimpleEndDeviceFunction(DEVICE_CONFIG_ID,
                NO_SUCH_DEVICE_CONFIG_TEXT);
        meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createDefaultMeter();
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        try {
            // Business method
            executeMeterConfigEndpoint.createMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage())
                    .isEqualTo(MessageSeeds.NO_SUCH_DEVICE_CONFIGURATION.translate(thesaurus, NO_SUCH_DEVICE_CONFIG_TEXT));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(MessageSeeds.NO_SUCH_DEVICE_CONFIGURATION.getErrorTypeLevel());
            assertThat(error.getCode()).isEqualTo(MessageSeeds.NO_SUCH_DEVICE_CONFIGURATION.getErrorCode());
            assertThat(error.getDetails())
                    .isEqualTo(MessageSeeds.NO_SUCH_DEVICE_CONFIGURATION.translate(thesaurus, NO_SUCH_DEVICE_CONFIG_TEXT));
            SetMultimap<String, String> values = HashMultimap.create();
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), DEVICE_MRID);
            values.put(CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), SERIAL_NUMBER);
            verify(webServiceCallOccurrence).saveRelatedAttributes(values);
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testReceivedDateIsMissing() throws Exception {
        MeterConfig meterConfig = new MeterConfig();
        SimpleEndDeviceFunction simpleEndDeviceFunction = createDefaultEndDeviceFunction();
        meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createMeter(DEVICE_NAME, null, DEVICE_TYPE_NAME);
        Meter.SimpleEndDeviceFunction simpleEndDeviceFunctionRef = createSimpleEndDeviceFunctionRef(DEVICE_CONFIG_ID);
        meter.getComFunctionOrConnectDisconnectFunctionOrSimpleEndDeviceFunction().add(simpleEndDeviceFunctionRef);
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        try {
            // Business method
            executeMeterConfigEndpoint.createMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.MISSING_ELEMENT.translate(thesaurus, METER_LIFECYCLE_DATE));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(MessageSeeds.MISSING_ELEMENT.getErrorTypeLevel());
            assertThat(error.getCode()).isEqualTo(MessageSeeds.MISSING_ELEMENT.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(
                    MessageSeeds.MISSING_ELEMENT.translate(thesaurus, "MeterConfig.Meter.lifecycle.receivedDate"));
            SetMultimap<String, String> values = HashMultimap.create();
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
            verify(webServiceCallOccurrence).saveRelatedAttributes(values);

        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testDeviceConfigurationByReferenceNotFound() throws Exception {
        MeterConfig meterConfig = new MeterConfig();
        Meter meter = createDefaultMeter();
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        try {
            // Business method
            executeMeterConfigEndpoint.createMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage())
                    .isEqualTo(MessageSeeds.ELEMENT_BY_REFERENCE_NOT_FOUND.translate(thesaurus, METER_SIMPLE_FUNCTION, METER_CONFIG_FUNCTION));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(MessageSeeds.ELEMENT_BY_REFERENCE_NOT_FOUND.getErrorTypeLevel());
            assertThat(error.getCode()).isEqualTo(MessageSeeds.ELEMENT_BY_REFERENCE_NOT_FOUND.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.ELEMENT_BY_REFERENCE_NOT_FOUND.translate(thesaurus,
                    "MeterConfig.Meter.SimpleEndDeviceFunction", "MeterConfig.SimpleEndDeviceFunction"));
            SetMultimap<String, String> values = HashMultimap.create();
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), DEVICE_MRID);
            values.put(CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), SERIAL_NUMBER);
            verify(webServiceCallOccurrence).saveRelatedAttributes(values);
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testMeterSimpleEndDeviceFunctionReferenceNotFoundShouldTryToUseDefaultConfiguration() throws Exception {
        MeterConfig meterConfig = new MeterConfig();
        Meter meter = createMeter(DEVICE_NAME, RECEIVED_DATE, DEVICE_TYPE_NAME);
        meter.getComFunctionOrConnectDisconnectFunctionOrSimpleEndDeviceFunction()
                .add(new Meter.SimpleEndDeviceFunction());
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        try {
            // Business method
            executeMeterConfigEndpoint.createMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage())
                    .isEqualTo(MessageSeeds.NO_DEFAULT_DEVICE_CONFIGURATION.translate(thesaurus));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(MessageSeeds.NO_DEFAULT_DEVICE_CONFIGURATION.getErrorTypeLevel());
            assertThat(error.getCode()).isEqualTo(MessageSeeds.NO_DEFAULT_DEVICE_CONFIGURATION.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.NO_DEFAULT_DEVICE_CONFIGURATION.translate(thesaurus));
            SetMultimap<String, String> values = HashMultimap.create();
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
            verify(webServiceCallOccurrence).saveRelatedAttributes(values);
        }
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
            SetMultimap<String, String> values = HashMultimap.create();
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), DEVICE_MRID);
            values.put(CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), SERIAL_NUMBER);
            verify(webServiceCallOccurrence).saveRelatedAttributes(values);
        }
    }

    @Test
    public void testOutboundNotConfigured() throws Exception {
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
            executeMeterConfigEndpoint.createMeterConfig(meterConfigRequest);
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
            SetMultimap<String, String> values = HashMultimap.create();
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), DEVICE_MRID);
            values.put(CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), SERIAL_NUMBER);
            verify(webServiceCallOccurrence).saveRelatedAttributes(values);
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }
}
