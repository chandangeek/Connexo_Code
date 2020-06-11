/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.DefaultState;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Batch;
import com.energyict.mdc.common.device.data.CIMLifecycleDates;
import com.energyict.mdc.common.device.data.Device;

import ch.iec.tc57._2011.meterconfig.ConfigurationEvent;
import ch.iec.tc57._2011.meterconfig.EndDeviceInfo;
import ch.iec.tc57._2011.meterconfig.LifecycleDate;
import ch.iec.tc57._2011.meterconfig.Manufacturer;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfig.MeterMultiplier;
import ch.iec.tc57._2011.meterconfig.Name;
import ch.iec.tc57._2011.meterconfig.ProductAssetModel;
import ch.iec.tc57._2011.meterconfig.SimpleEndDeviceFunction;
import ch.iec.tc57._2011.meterconfig.Status;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigPayloadType;
import ch.iec.tc57._2011.meterconfigmessage.MeterConfigRequestMessageType;
import ch.iec.tc57._2011.meterconfigmessage.ObjectFactory;
import com.elster.connexo._2017.schema.customattributes.Attribute;
import com.elster.connexo._2017.schema.customattributes.CustomAttributeSet;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractMockMeterConfig extends AbstractMockActivator {
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
    protected static final String STATE_NAME = DefaultState.IN_STOCK.getKey();
    protected static final String REPLY_ADDRESS = "replyAddress";
    protected static final String NON_VERSIONED_CPS_ID = "my cps id";
    protected static final String VERSIONED_CPS_ID = "my versioned cps id";
    protected static final String GENERAL_ATTRIBUTES_ID = "General attributes id";
    protected static final String CPS_NAME_1 = "name 1";
    protected static final String CPS_VALUE_1 = "value 1";
    protected static final String CPS_NAME_2 = "name 2";
    protected static final String CPS_VALUE_2 = "value 2";
    protected static final String GA_NAME_1 = "GA name 1";
    protected static final String GA_VALUE_1 = "GA value 1";
    protected static final String GA_NAME_2 = "GA name 2";
    protected static final String GA_VALUE_2 = "GA value 2";

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
    @Mock
    protected RegisteredCustomPropertySet registeredCustomNonVersionedPropertySet;
    @Mock
    protected RegisteredCustomPropertySet registeredCustomVersionedPropertySet;
    @Mock
    protected CustomPropertySet customNonVersionedPropertySet;
    @Mock
    protected CustomPropertySet customVersionedPropertySet;
    @Mock
    private CIMLifecycleDates lifecycleDates;
    @Mock
    protected MeterConfig meterConfig;
    @Mock
    protected WebServiceContext webServiceContext;
    @Mock
    protected MessageContext messageContext;
    @Mock
    protected WebServiceCallOccurrence webServiceCallOccurrence;

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

    protected void mockCustomPropertySetService() {
        when(customPropertySetService.findActiveCustomPropertySet(NON_VERSIONED_CPS_ID)).thenReturn(Optional.of(registeredCustomNonVersionedPropertySet));
        when(registeredCustomNonVersionedPropertySet.getCustomPropertySet()).thenReturn(customNonVersionedPropertySet);
        when(customPropertySetService.findActiveCustomPropertySet(VERSIONED_CPS_ID)).thenReturn(Optional.of(registeredCustomVersionedPropertySet));
        when(registeredCustomVersionedPropertySet.getCustomPropertySet()).thenReturn(customVersionedPropertySet);
        when(customVersionedPropertySet.isVersioned()).thenReturn(true);
    }

    protected void mockCustomPropertySetSpecs(boolean mockConverters) {
        PropertySpec prop1 = mock(PropertySpec.class);
        when(prop1.getName()).thenReturn(CPS_NAME_1);
        PropertySpec prop2 = mock(PropertySpec.class);
        when(prop2.getName()).thenReturn(CPS_NAME_2);
        when(customNonVersionedPropertySet.getPropertySpecs()).thenReturn(Arrays.asList(prop1, prop2));
        when(customVersionedPropertySet.getPropertySpecs()).thenReturn(Arrays.asList(prop1, prop2));
        if (mockConverters) {
            ValueFactory valueFactory = mock(ValueFactory.class);
            when(prop1.getValueFactory()).thenReturn(valueFactory);
            when(prop2.getValueFactory()).thenReturn(valueFactory);
            when(valueFactory.fromStringValue(anyString())).thenReturn("some value");
        }
    }

    protected void mockDevice() {
        when(device.getCreateTime()).thenReturn(RECEIVED_DATE);
        when(device.getmRID()).thenReturn(DEVICE_MRID);
        when(device.getName()).thenReturn(DEVICE_NAME);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getSerialNumber()).thenReturn(SERIAL_NUMBER);
        when(device.getManufacturer()).thenReturn(MANUFACTURER);
        when(device.getModelNumber()).thenReturn(MODEL_NUMBER);
        when(device.getModelVersion()).thenReturn(MODEL_VERSION);
        when(device.getBatch()).thenReturn(Optional.of(batch));
        when(device.getUsagePoint()).thenReturn(Optional.empty());
        when(batch.getName()).thenReturn(BATCH);
        when(device.getMultiplier()).thenReturn(BigDecimal.valueOf(MULTIPLIER));
        when(device.getState()).thenReturn(state);
        when(state.getName()).thenReturn(DefaultState.IN_STOCK.getKey());
        mockDeviceConfiguration();
        mockLifeCycleDates();
        mockMeterConfigFactoryWithDefaultMeter();
    }

    private void mockMeterConfigFactoryWithDefaultMeter() {
        when(meterConfig.getMeter()).thenReturn(Arrays.asList(createDefaultMeter()));
        when(meterConfig.getSimpleEndDeviceFunction()).thenReturn(Arrays.asList(createDefaultEndDeviceFunction()));
        when(meterConfigFactory.asMeterConfig(any(Device.class))).thenReturn(meterConfig);
        when(meterConfigFactory.asGetMeterConfig(any(Device.class))).thenReturn(meterConfig);
    }

    protected void mockMeterConfigFactoryWithCas() {
        when(meterConfig.getMeter()).thenReturn(Arrays.asList(createMeterWithCas()));
        when(meterConfig.getSimpleEndDeviceFunction()).thenReturn(Arrays.asList(createDefaultEndDeviceFunction()));
        when(meterConfigFactory.asMeterConfig(any(Device.class))).thenReturn(meterConfig);
        when(meterConfigFactory.asGetMeterConfig(any(Device.class))).thenReturn(meterConfig);
    }

    private void mockLifeCycleDates() {
        when(lifecycleDates.getReceivedDate()).thenReturn(Optional.of(RECEIVED_DATE));
        when(device.getLifecycleDates()).thenReturn(lifecycleDates);
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
        status.setValue(STATE_NAME);
        return status;
    }

    protected Meter createMeterWithCas() {
        Meter meter = createMeter();
        meter.setMRID(DEVICE_MRID);
        meter.setLotNumber(BATCH);
        meter.setSerialNumber(SERIAL_NUMBER);
        meter.getMeterMultipliers().add(createMeterMultiplier(MULTIPLIER));
        EndDeviceInfo endDeviceInfo = createEndDeviceInfo(MODEL_NUMBER, MODEL_VERSION, MANUFACTURER);
        meter.setEndDeviceInfo(endDeviceInfo);
        meter.setStatus(createStatus());
        meter.getMeterCustomAttributeSet().addAll(Arrays.asList(createGeneralAttributes(), createNonVersionedCustomPropertySet(), createVersionedCustomPropertySet()));
        return meter;
    }

    protected Meter createDefaultMeter() {
        Meter meter = createMeter();
        meter.setMRID(DEVICE_MRID);
        meter.setLotNumber(BATCH);
        meter.setSerialNumber(SERIAL_NUMBER);
        meter.getMeterMultipliers().add(createMeterMultiplier(MULTIPLIER));
        EndDeviceInfo endDeviceInfo = createEndDeviceInfo(MODEL_NUMBER, MODEL_VERSION, MANUFACTURER);
        meter.setEndDeviceInfo(endDeviceInfo);
        meter.setStatus(createStatus());
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


    protected CustomAttributeSet createGeneralAttributes() {
        CustomAttributeSet cas = new CustomAttributeSet();
        cas.setId(GENERAL_ATTRIBUTES_ID);
        Attribute attrbute = new Attribute();
        attrbute.setName(GA_NAME_1);
        attrbute.setValue(GA_VALUE_1);
        cas.getAttribute().add(attrbute);
        attrbute = new Attribute();
        attrbute.setName(GA_NAME_2);
        attrbute.setValue(GA_VALUE_2);
        cas.getAttribute().add(attrbute);
        return cas;
    }

    protected CustomAttributeSet createNonVersionedCustomPropertySet() {
        return createCustomAttributeSet(NON_VERSIONED_CPS_ID);
    }

    private CustomAttributeSet createCustomAttributeSet(String cpsId) {
        CustomAttributeSet cas = new CustomAttributeSet();
        cas.setId(cpsId);
        Attribute attrbute = new Attribute();
        attrbute.setName(CPS_NAME_1);
        attrbute.setValue(CPS_VALUE_1);
        cas.getAttribute().add(attrbute);
        attrbute = new Attribute();
        attrbute.setName(CPS_NAME_2);
        attrbute.setValue(CPS_VALUE_2);
        cas.getAttribute().add(attrbute);
        return cas;
    }

    protected CustomAttributeSet createVersionedCustomPropertySet() {
        CustomAttributeSet customAttributeSet = createCustomAttributeSet(VERSIONED_CPS_ID);
        customAttributeSet.setFromDateTime(RECEIVED_DATE.plusSeconds(10));
        return customAttributeSet;
    }

}
