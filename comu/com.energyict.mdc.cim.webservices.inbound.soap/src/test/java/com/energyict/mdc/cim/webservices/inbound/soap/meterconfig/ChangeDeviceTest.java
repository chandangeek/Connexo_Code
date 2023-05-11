/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.CimAttributeNames;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.AbstractMockMeterConfig;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;

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
import com.elster.connexo._2018.schema.securitykeys.AllowedDeviceStatuses;
import com.elster.connexo._2018.schema.securitykeys.SecurityKey;
import com.elster.connexo._2018.schema.securitykeys.SecurityKeys;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.w3._2001._04.xmlenc.CipherDataType;
import org.w3._2001._04.xmlenc.EncryptedDataType;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ChangeDeviceTest extends AbstractMockMeterConfig {
    private static final String METER = "SPE0000001";
    private static final String IN_STOCK_MSG = "dlc.default.inStock";
    private static final String SECURITY_ACCESSOR_NAME = "my security accessor name";

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
        when(deviceService.findDeviceByMrid(DEVICE_MRID)).thenReturn(Optional.of(device));
        when(deviceService.findDeviceByName(DEVICE_NAME)).thenReturn(Optional.of(device));
        when(deviceService.findAllDevices(any(Condition.class)))
                .thenReturn(FakeBuilder.initBuilderStub(Collections.emptyList(), Finder.class));
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
        MeterConfigResponseMessageType response = executeMeterConfigEndpoint.changeMeterConfig(meterConfigRequest);

        // Assert invocations
        verify(deviceService).findDeviceByMrid(DEVICE_MRID);
        verify(device).setSerialNumber(SERIAL_NUMBER);
        verify(device).setManufacturer(MANUFACTURER);
        verify(device).setModelNumber(MODEL_NUMBER);
        verify(device).setModelVersion(MODEL_VERSION);
        SetMultimap<String, String> values = HashMultimap.create();
        values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
        values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), DEVICE_MRID);
        values.put(CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), SERIAL_NUMBER);
        verify(webServiceCallOccurrence).saveRelatedAttributes(values);

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

        List<SimpleEndDeviceFunction> responseSimpleEndDeviceFunction = responseMeterConfig
                .getSimpleEndDeviceFunction();
        assertThat(responseSimpleEndDeviceFunction).hasSize(1);
        assertThat(responseSimpleEndDeviceFunction.get(0).getMRID()).isEqualTo(DEVICE_CONFIG_ID);
        assertThat(responseSimpleEndDeviceFunction.get(0).getConfigID()).isEqualTo(DEVICE_CONFIGURATION_NAME);
    }

    @Test
    public void testChangeDeviceFailsWhenStateIsNotAllowed() throws Exception {
        // Prepare request
        MeterConfig meterConfig = new MeterConfig();
        SimpleEndDeviceFunction simpleEndDeviceFunction = createDefaultEndDeviceFunction();
        meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createDefaultMeter();
        meter.setLotNumber("");
        meter.setConfigurationEvents(createConfigurationEvent());
        meter.setStatus(createStatus());
        AllowedDeviceStatuses allowedDeviceStatuses = new AllowedDeviceStatuses();
        allowedDeviceStatuses.getAllowedDeviceStatus().add("Some wierd status");
        SecurityKeys securityKeys = new SecurityKeys();
        SecurityKey securityKey = new SecurityKey();
        securityKey.setSecurityAccessorName(SECURITY_ACCESSOR_NAME);
        EncryptedDataType securityAccessorKey = new EncryptedDataType();
        CipherDataType cipherData = new CipherDataType();
        cipherData.setCipherValue("1234".getBytes());
        securityAccessorKey.setCipherData(cipherData);
        securityKey.setSecurityAccessorKey(securityAccessorKey);
        securityKeys.getSecurityKey().add(securityKey);
        securityKeys.setAllowedDeviceStatuses(allowedDeviceStatuses);
        meter.setSecurityKeys(securityKeys);
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        try {
            // Business method
            executeMeterConfigEndpoint.changeMeterConfig(meterConfigRequest);

            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage())
                    .isEqualTo(MessageSeeds.SECURITY_KEY_UPDATE_FORBIDDEN_FOR_DEVICE_STATUS.translate(thesaurus, METER, IN_STOCK_MSG));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode())
                    .isEqualTo(MessageSeeds.SECURITY_KEY_UPDATE_FORBIDDEN_FOR_DEVICE_STATUS.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.SECURITY_KEY_UPDATE_FORBIDDEN_FOR_DEVICE_STATUS
                    .translate(thesaurus, DEVICE_NAME, STATE_NAME));
            SetMultimap<String, String> values = HashMultimap.create();
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), DEVICE_MRID);
            values.put(CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), SERIAL_NUMBER);
            verify(webServiceCallOccurrence).saveRelatedAttributes(values);
        }
    }

    @Test
    public void testChangeDeviceShouldFailDuringKeyImportWhenStateIsAllowedButKeyImportIsNotSetupProperly()
            throws Exception {
        // Prepare request
        MeterConfig meterConfig = new MeterConfig();
        SimpleEndDeviceFunction simpleEndDeviceFunction = createDefaultEndDeviceFunction();
        meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        Meter meter = createDefaultMeter();
        meter.setLotNumber("");
        meter.setConfigurationEvents(createConfigurationEvent());
        meter.setStatus(createStatus());
        AllowedDeviceStatuses allowedDeviceStatuses = new AllowedDeviceStatuses();
        allowedDeviceStatuses.getAllowedDeviceStatus().add(STATE_NAME);
        SecurityKeys securityKeys = new SecurityKeys();
        SecurityKey securityKey = new SecurityKey();
        String securityAccessorName = SECURITY_ACCESSOR_NAME;
        securityKey.setSecurityAccessorName(securityAccessorName);
        EncryptedDataType securityAccessorKey = new EncryptedDataType();
        CipherDataType cipherData = new CipherDataType();
        cipherData.setCipherValue("1234".getBytes());
        securityAccessorKey.setCipherData(cipherData);
        securityKey.setSecurityAccessorKey(securityAccessorKey);
        securityKeys.getSecurityKey().add(securityKey);
        securityKeys.setAllowedDeviceStatuses(allowedDeviceStatuses);
        meter.setSecurityKeys(securityKeys);
        meterConfig.getMeter().add(meter);
        MeterConfigRequestMessageType meterConfigRequest = createMeterConfigRequest(meterConfig);

        try {
            // Business method
            executeMeterConfigEndpoint.changeMeterConfig(meterConfigRequest);
            fail("FaultMessage must be thrown");
        } catch (FaultMessage faultMessage) {
            // Asserts
            assertThat(faultMessage.getMessage())
                    .isEqualTo(MessageSeeds.EXCEPTION_OCCURRED_DURING_KEY_IMPORT.translate(thesaurus, METER, SECURITY_ACCESSOR_NAME));
            MeterConfigFaultMessageType faultInfo = faultMessage.getFaultInfo();
            assertThat(faultInfo.getReply().getResult()).isEqualTo(ReplyType.Result.FAILED);
            assertThat(faultInfo.getReply().getError()).hasSize(1);
            ErrorType error = faultInfo.getReply().getError().get(0);
            assertThat(error.getLevel()).isEqualTo(ErrorType.Level.FATAL);
            assertThat(error.getCode()).isEqualTo(MessageSeeds.EXCEPTION_OCCURRED_DURING_KEY_IMPORT.getErrorCode());
            assertThat(error.getDetails()).isEqualTo(MessageSeeds.EXCEPTION_OCCURRED_DURING_KEY_IMPORT
                    .translate(thesaurus, DEVICE_NAME, securityAccessorName));
            SetMultimap<String, String> values = HashMultimap.create();
            values.put(CimAttributeNames.CIM_DEVICE_NAME.getAttributeName(), DEVICE_NAME);
            values.put(CimAttributeNames.CIM_DEVICE_MR_ID.getAttributeName(), DEVICE_MRID);
            values.put(CimAttributeNames.CIM_DEVICE_SERIAL_NUMBER.getAttributeName(), SERIAL_NUMBER);
            verify(webServiceCallOccurrence).saveRelatedAttributes(values);
        }
    }

}
