/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.AbstractMockMeterConfig;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.nls.LocalizedException;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class CreateDeviceTest extends AbstractMockMeterConfig {

    @Before
    public void setUp() throws Exception {
        when(deviceService.newDevice(deviceConfiguration, DEVICE_NAME, BATCH, RECEIVED_DATE)).thenReturn(device);
        when(deviceService.newDevice(deviceConfiguration, DEVICE_NAME, RECEIVED_DATE)).thenReturn(device);
        when(state.getName()).thenReturn(STATE_NAME);
        mockDevice();
    }

    @Test
    public void testNoMetersInMeterConfig() throws Exception {
        // Prepare request
        MeterConfig meterConfig = new MeterConfig();
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        try {
            // Business method
            getInstance(ExecuteMeterConfigEndpoint.class).createMeterConfig(meterConfigRequest);
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
    public void testCreateDeviceSuccessfully() throws Exception {
        // Prepare request
        MeterConfig meterConfig = new MeterConfig();
        SimpleEndDeviceFunction simpleEndDeviceFunction = createDefaultEndDeviceFunction();
        meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createDefaultMeter();
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        // Business method
        MeterConfigResponseMessageType response = getInstance(ExecuteMeterConfigEndpoint.class).createMeterConfig(meterConfigRequest);

        // Assert invocations
        verify(deviceService).newDevice(deviceConfiguration, DEVICE_NAME, BATCH, RECEIVED_DATE);
        verify(device).setSerialNumber(SERIAL_NUMBER);
        verify(device).setManufacturer(MANUFACTURER);
        verify(device).setModelNumber(MODEL_NUMBER);
        verify(device).setModelVersion(MODEL_VERSION);
        verify(device).setMultiplier(BigDecimal.valueOf(MULTIPLIER), RECEIVED_DATE);

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

        List<SimpleEndDeviceFunction> responseSimpleEndDeviceFunction = responseMeterConfig.getSimpleEndDeviceFunction();
        assertThat(responseSimpleEndDeviceFunction).hasSize(1);
        assertThat(responseSimpleEndDeviceFunction.get(0).getMRID()).isEqualTo(DEVICE_CONFIG_ID);
        assertThat(responseSimpleEndDeviceFunction.get(0).getConfigID()).isEqualTo(DEVICE_CONFIGURATION_NAME);
    }

    @Test
    public void testCreateDeviceSuccessfullyWithoutOptionalParameters() throws Exception {
        // Prepare request
        MeterConfig meterConfig = new MeterConfig();
        SimpleEndDeviceFunction simpleEndDeviceFunction = createDefaultEndDeviceFunction();
        meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createMeter();
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        // Re-mock device
        when(device.getSerialNumber()).thenReturn(null);
        when(device.getMultiplier()).thenReturn(BigDecimal.ONE);
        when(device.getBatch()).thenReturn(Optional.empty());
        when(device.getManufacturer()).thenReturn(null);
        when(device.getModelNumber()).thenReturn(null);
        when(device.getModelVersion()).thenReturn(null);
        when(state.getName()).thenReturn(DefaultState.COMMISSIONING.getKey());

        // Business method
        MeterConfigResponseMessageType response = getInstance(ExecuteMeterConfigEndpoint.class).createMeterConfig(meterConfigRequest);

        // Assert invocations
        verify(deviceService).newDevice(deviceConfiguration, DEVICE_NAME, RECEIVED_DATE);

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

        List<SimpleEndDeviceFunction> responseSimpleEndDeviceFunction = responseMeterConfig.getSimpleEndDeviceFunction();
        assertThat(responseSimpleEndDeviceFunction).hasSize(1);
        assertThat(responseSimpleEndDeviceFunction.get(0).getMRID()).isEqualTo(DEVICE_CONFIG_ID);
        assertThat(responseSimpleEndDeviceFunction.get(0).getConfigID()).isEqualTo(DEVICE_CONFIGURATION_NAME);
    }

    @Test
    public void testWarningIfMoreThanOneMeterSpecified() throws Exception {
        // Prepare request
        MeterConfig meterConfig = new MeterConfig();
        SimpleEndDeviceFunction simpleEndDeviceFunction = createDefaultEndDeviceFunction();
        meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createDefaultMeter();
        meterConfig.getMeter().add(meter);
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        // Business method
        MeterConfigResponseMessageType response = getInstance(ExecuteMeterConfigEndpoint.class).createMeterConfig(meterConfigRequest);

        // Asserts
        assertThat(response.getHeader().getVerb()).isEqualTo(HeaderType.Verb.CREATED);
        assertThat(response.getHeader().getNoun()).isEqualTo("MeterConfig");
        ReplyType reply = response.getReply();
        assertThat(reply.getResult()).isEqualTo(ReplyType.Result.PARTIAL);
        assertThat(reply.getError()).hasSize(1);
        assertThat(reply.getError().get(0).getCode()).isEqualTo(MessageSeeds.UNSUPPORTED_BULK_OPERATION.getErrorCode());
        assertThat(reply.getError().get(0).getLevel()).isEqualTo(MessageSeeds.UNSUPPORTED_BULK_OPERATION.getErrorTypeLevel());
        assertThat(reply.getError().get(0).getDetails()).isEqualTo(MessageSeeds.UNSUPPORTED_BULK_OPERATION.translate(thesaurus, "MeterConfig.Meter"));
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
        when(deviceService.newDevice(deviceConfiguration, DEVICE_NAME, BATCH, RECEIVED_DATE)).thenThrow(localizedException);

        try {
            // Business method
            getInstance(ExecuteMeterConfigEndpoint.class).createMeterConfig(meterConfigRequest);
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

            verify(transactionContext).close();
            verifyNoMoreInteractions(transactionContext);
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
        when(deviceService.newDevice(deviceConfiguration, DEVICE_NAME, BATCH, RECEIVED_DATE)).thenThrow(exception);

        try {
            // Business method
            getInstance(ExecuteMeterConfigEndpoint.class).createMeterConfig(meterConfigRequest);
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

            verify(transactionContext).close();
            verifyNoMoreInteractions(transactionContext);
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testDeviceTypeNotFound() throws Exception {
        MeterConfig meterConfig = new MeterConfig();
        Meter meter = createMeter(DEVICE_NAME, RECEIVED_DATE, "no such device type");
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        try {
            // Business method
            getInstance(ExecuteMeterConfigEndpoint.class).createMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.NO_SUCH_DEVICE_TYPE.translate(thesaurus));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(MessageSeeds.NO_SUCH_DEVICE_TYPE.getErrorTypeLevel());
            assertThat(error.getCode()).isEqualTo(MessageSeeds.NO_SUCH_DEVICE_TYPE.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.NO_SUCH_DEVICE_TYPE.translate(thesaurus, "no such device type"));

            verify(transactionContext).close();
            verifyNoMoreInteractions(transactionContext);
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testDeviceConfigurationNotFound() throws Exception {
        MeterConfig meterConfig = new MeterConfig();
        SimpleEndDeviceFunction simpleEndDeviceFunction = createSimpleEndDeviceFunction(DEVICE_CONFIG_ID, "no such device config");
        meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createDefaultMeter();
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        try {
            // Business method
            getInstance(ExecuteMeterConfigEndpoint.class).createMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.NO_SUCH_DEVICE_CONFIGURATION.translate(thesaurus));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(MessageSeeds.NO_SUCH_DEVICE_CONFIGURATION.getErrorTypeLevel());
            assertThat(error.getCode()).isEqualTo(MessageSeeds.NO_SUCH_DEVICE_CONFIGURATION.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.NO_SUCH_DEVICE_CONFIGURATION.translate(thesaurus, "no such device config"));

            verify(transactionContext).close();
            verifyNoMoreInteractions(transactionContext);
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
            getInstance(ExecuteMeterConfigEndpoint.class).createMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.MISSING_ELEMENT.translate(thesaurus));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(MessageSeeds.MISSING_ELEMENT.getErrorTypeLevel());
            assertThat(error.getCode()).isEqualTo(MessageSeeds.MISSING_ELEMENT.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.MISSING_ELEMENT.translate(thesaurus, "MeterConfig.Meter[0].lifecycle.receivedDate"));

            verify(transactionContext).close();
            verifyNoMoreInteractions(transactionContext);
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
            getInstance(ExecuteMeterConfigEndpoint.class).createMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.ELEMENT_BY_REFERENCE_NOT_FOUND.translate(thesaurus));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(MessageSeeds.ELEMENT_BY_REFERENCE_NOT_FOUND.getErrorTypeLevel());
            assertThat(error.getCode()).isEqualTo(MessageSeeds.ELEMENT_BY_REFERENCE_NOT_FOUND.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.ELEMENT_BY_REFERENCE_NOT_FOUND.translate(thesaurus,
                    "MeterConfig.Meter[0].SimpleEndDeviceFunction", "MeterConfig.SimpleEndDeviceFunction"));

            verify(transactionContext).close();
            verifyNoMoreInteractions(transactionContext);
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testMeterSimpleEndDeviceFunctionReferenceNotFound() throws Exception {
        MeterConfig meterConfig = new MeterConfig();
        Meter meter = createMeter(DEVICE_NAME, RECEIVED_DATE, DEVICE_TYPE_NAME);
        meter.getComFunctionOrConnectDisconnectFunctionOrSimpleEndDeviceFunction().add(new Meter.SimpleEndDeviceFunction());
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        try {
            // Business method
            getInstance(ExecuteMeterConfigEndpoint.class).createMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.MISSING_ELEMENT.translate(thesaurus));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(MessageSeeds.MISSING_ELEMENT.getErrorTypeLevel());
            assertThat(error.getCode()).isEqualTo(MessageSeeds.MISSING_ELEMENT.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.MISSING_ELEMENT.translate(thesaurus, "MeterConfig.Meter[0].SimpleEndDeviceFunction.ref"));

            verify(transactionContext).close();
            verifyNoMoreInteractions(transactionContext);
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }

    @Test
    public void testSimpleEndDeviceFunctionConfigIdNotFound() throws Exception {
        MeterConfig meterConfig = new MeterConfig();
        SimpleEndDeviceFunction simpleEndDeviceFunction = new SimpleEndDeviceFunction();
        simpleEndDeviceFunction.setMRID(DEVICE_CONFIG_ID);
        meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createDefaultMeter();
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        try {
            // Business method
            getInstance(ExecuteMeterConfigEndpoint.class).createMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage()).isEqualTo(MessageSeeds.MISSING_ELEMENT.translate(thesaurus));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(MessageSeeds.MISSING_ELEMENT.getErrorTypeLevel());
            assertThat(error.getCode()).isEqualTo(MessageSeeds.MISSING_ELEMENT.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.MISSING_ELEMENT.translate(thesaurus, "MeterConfig.SimpleEndDeviceFunction[0].configID"));

            verify(transactionContext).close();
            verifyNoMoreInteractions(transactionContext);
        } catch (Exception e) {
            fail("FaultMessage must be thrown");
        }
    }
}