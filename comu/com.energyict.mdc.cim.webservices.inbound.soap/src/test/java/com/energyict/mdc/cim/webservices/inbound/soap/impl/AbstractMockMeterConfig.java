/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.fsm.State;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.Device;

import ch.iec.tc57._2011.meterconfig.EndDeviceInfo;
import ch.iec.tc57._2011.meterconfig.LifecycleDate;
import ch.iec.tc57._2011.meterconfig.Manufacturer;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfig.MeterMultiplier;
import ch.iec.tc57._2011.meterconfig.ConfigurationEvent;
import ch.iec.tc57._2011.meterconfig.Status;
import ch.iec.tc57._2011.meterconfig.Name;
import ch.iec.tc57._2011.meterconfig.ProductAssetModel;
import ch.iec.tc57._2011.meterconfig.SimpleEndDeviceFunction;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigPayloadType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigRequestMessageType;
import ch.iec.tc57._2011.meterconfigmessage.ObjectFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class AbstractMockMeterConfig extends AbstractMockActivator {

    protected static final String DEVICE_MRID = UUID.randomUUID().toString();
    protected static final String DEVICE_NAME = "SPE0000001";
    protected static final String SERIAL_NUMBER = "00000001";
    protected static final Instant RECEIVED_DATE = Instant.now();
    protected static final String DEVICE_TYPE_NAME = "Actaris SL7000";
    protected static final String BATCH = "batch";
    protected static final String MANUFACTURER = "Honeywell";
    protected static final String MODEL_NUMBER = "001";
    protected static final String MODEL_VERSION = "1.0.0";
    protected static final String DEVICE_CONFIG_ID = "123";
    protected static final String DEVICE_CONFIGURATION_NAME = "Default";
    protected static final float MULTIPLIER = 1.23456789f;
    protected static final String STATE_NAME = "I'm okay. And you?";
    protected static final String REPLY_ADDRESS = "replyAddress";

    protected final ObjectFactory meterConfigMessageObjectFactory = new ObjectFactory();
    protected final ch.iec.tc57._2011.schema.message.ObjectFactory cimMessageObjectFactory
            = new ch.iec.tc57._2011.schema.message.ObjectFactory();

    @Mock
    protected DeviceType deviceType;
    @Mock
    protected DeviceConfiguration deviceConfiguration;
    @Mock
    protected Device device;
    @Mock
    protected Batch batch;
    @Mock
    protected State state;

    protected void mockDeviceType() {
        when(deviceType.getName()).thenReturn(DEVICE_TYPE_NAME);
        when(deviceType.getConfigurations()).thenReturn(Collections.singletonList(deviceConfiguration));
    }
    protected void mockDeviceConfiguration() {
        when(deviceConfigurationService.findDeviceTypeByName(any())).thenReturn(Optional.empty());
        when(deviceConfigurationService.findDeviceTypeByName(DEVICE_TYPE_NAME)).thenReturn(Optional.of(deviceType));
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(deviceConfiguration.getId()).thenReturn(Long.valueOf(DEVICE_CONFIG_ID));
        when(deviceConfiguration.getName()).thenReturn(DEVICE_CONFIGURATION_NAME);
        mockDeviceType();
    }

    protected void mockDevice() {
        when(device.getmRID()).thenReturn(DEVICE_MRID);
        when(device.getName()).thenReturn(DEVICE_NAME);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getSerialNumber()).thenReturn(SERIAL_NUMBER);
        when(device.getManufacturer()).thenReturn(MANUFACTURER);
        when(device.getModelNumber()).thenReturn(MODEL_NUMBER);
        when(device.getModelVersion()).thenReturn(MODEL_VERSION);
        when(device.getBatch()).thenReturn(Optional.of(batch));
        when(batch.getName()).thenReturn(BATCH);
        when(device.getMultiplier()).thenReturn(BigDecimal.valueOf(MULTIPLIER));
        when(device.getState()).thenReturn(state);
        mockDeviceConfiguration();
    }

    protected SimpleEndDeviceFunction createDefaultEndDeviceFunction() {
        return createSimpleEndDeviceFunction(DEVICE_CONFIG_ID, DEVICE_CONFIGURATION_NAME);
    }

    protected SimpleEndDeviceFunction createSimpleEndDeviceFunction(String deviceConfigId, String deviceConfigurationName) {
        SimpleEndDeviceFunction simpleEndDeviceFunction = new SimpleEndDeviceFunction();
        simpleEndDeviceFunction.setMRID(deviceConfigId);
        simpleEndDeviceFunction.setConfigID(deviceConfigurationName);
        return simpleEndDeviceFunction;
    }

    protected ConfigurationEvent createConfigurationEvent() {
        ConfigurationEvent configurationEvent = new ConfigurationEvent();
        configurationEvent.setReason("changeMultiplier");
        return configurationEvent;
    }

    protected Status createStatus() {
        Status status = new Status();
        status.setValue("Active");
        return status;
    }

    protected Meter createDefaultMeter() {
        Meter meter = createMeter();
        meter.setMRID(DEVICE_MRID);
        meter.setLotNumber(BATCH);
        meter.setSerialNumber(SERIAL_NUMBER);
        meter.getMeterMultipliers().add(createMeterMultiplier(MULTIPLIER));
        EndDeviceInfo endDeviceInfo = createEndDeviceInfo(MODEL_NUMBER, MODEL_VERSION, MANUFACTURER);
        meter.setEndDeviceInfo(endDeviceInfo);
        return meter;
    }

    protected Meter createMeter() {
        Meter meter = createMeter(DEVICE_NAME, RECEIVED_DATE, DEVICE_TYPE_NAME);
        Meter.SimpleEndDeviceFunction simpleEndDeviceFunctionRef = createSimpleEndDeviceFunctionRef(DEVICE_CONFIG_ID);
        meter.getComFunctionOrConnectDisconnectFunctionOrSimpleEndDeviceFunction().add(simpleEndDeviceFunctionRef);
        return meter;
    }

    protected Meter createMeter(String deviceName, Instant receivedDate, String deviceTypeName) {
        Meter meter = new Meter();
        meter.getNames().add(name(deviceName));
        LifecycleDate lifecycleDate = new LifecycleDate();
        meter.setLifecycle(lifecycleDate);
        lifecycleDate.setReceivedDate(receivedDate);
        meter.setType(deviceTypeName);
        return meter;
    }

    protected MeterMultiplier createMeterMultiplier(float multiplier) {
        MeterMultiplier meterMultiplier = new MeterMultiplier();
        meterMultiplier.setValue(multiplier);
        return meterMultiplier;
    }

    protected EndDeviceInfo createEndDeviceInfo(String modelNumber, String modelVersion, String manufacturerName) {
        EndDeviceInfo endDeviceInfo = new EndDeviceInfo();
        ProductAssetModel productAssetModel = new ProductAssetModel();
        endDeviceInfo.setAssetModel(productAssetModel);
        productAssetModel.setModelNumber(modelNumber);
        productAssetModel.setModelVersion(modelVersion);
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.getNames().add(name(manufacturerName));
        productAssetModel.setManufacturer(manufacturer);
        return endDeviceInfo;
    }

    protected Meter.SimpleEndDeviceFunction createSimpleEndDeviceFunctionRef(String deviceConfigId) {
        Meter.SimpleEndDeviceFunction simpleEndDeviceFunctionRef = new Meter.SimpleEndDeviceFunction();
        simpleEndDeviceFunctionRef.setRef(deviceConfigId);
        return simpleEndDeviceFunctionRef;
    }

    protected MeterConfigRequestMessageType createMeterConfigRequest(MeterConfig meterConfig) {
        MeterConfigPayloadType meterConfigPayload = meterConfigMessageObjectFactory.createMeterConfigPayloadType();
        meterConfigPayload.setMeterConfig(meterConfig);
        MeterConfigRequestMessageType meterConfigRequestMessage = meterConfigMessageObjectFactory.createMeterConfigRequestMessageType();
        meterConfigRequestMessage.setPayload(meterConfigPayload);
        meterConfigRequestMessage.setHeader(cimMessageObjectFactory.createHeaderType());
        return meterConfigRequestMessage;
    }

    protected Name name(String value) {
        Name name = new Name();
        name.setName(value);
        return name;
    }
}