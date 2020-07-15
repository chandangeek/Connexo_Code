/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.meterconfig;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.cim.webservices.outbound.soap.MeterConfigFactory;
import com.energyict.mdc.cim.webservices.outbound.soap.impl.TranslationKeys;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Batch;
import com.energyict.mdc.common.device.data.Device;
import com.elster.jupiter.metering.DefaultState;
import com.energyict.mdc.common.pluggable.PluggableClassUsageProperty;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.upl.TypedProperties;

import ch.iec.tc57._2011.meterconfig.ConnectionAttributes;
import ch.iec.tc57._2011.meterconfig.EndDeviceInfo;
import ch.iec.tc57._2011.meterconfig.Manufacturer;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfig.MeterMultiplier;
import ch.iec.tc57._2011.meterconfig.Name;
import ch.iec.tc57._2011.meterconfig.ProductAssetModel;
import ch.iec.tc57._2011.meterconfig.SimpleEndDeviceFunction;
import ch.iec.tc57._2011.meterconfig.Status;
import ch.iec.tc57._2011.meterconfig.Zones;
import com.elster.connexo._2017.schema.customattributes.Attribute;
import com.elster.connexo._2017.schema.customattributes.CustomAttributeSet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component(name="com.energyict.mdc.cim.webservices.outbound.soap.meterconfig.MeterConfigFactory", service=MeterConfigFactory.class)
public class MeterConfigFactoryImpl implements MeterConfigFactory {

    private static final String COMPONENT_NAME = "SIM";

    private volatile CustomPropertySetService customPropertySetService;
    private volatile Clock clock;
    private volatile Thesaurus thesaurus;

    public MeterConfigFactoryImpl() {
        // for OSGI purposes
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.SOAP);
    }

    @Override
    public MeterConfig asMeterConfig(Device device) {
        return asMeterConfig(Arrays.asList(device));
    }

    @Override
    public MeterConfig asGetMeterConfig(Device device) {
        return asGetMeterConfig(Arrays.asList(device));
    }

    @Override
    public MeterConfig asMeterConfig(Collection<Device> devices) {
        Set<String> deviceConfigRefs = new HashSet<>();
        MeterConfig meterConfig = new MeterConfig();
        devices.forEach(device -> {
            Meter meter = createMeter(device);
            meterConfig.getMeter().add(meter);

            DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
            String deviceConfigRef = Long.toString(deviceConfiguration.getId());
            if (!deviceConfigRefs.contains(deviceConfigRef)) {
                meterConfig.getSimpleEndDeviceFunction().add(createSimpleEndDeviceFunction(deviceConfigRef, deviceConfiguration.getName()));
                deviceConfigRefs.add(deviceConfigRef);
            }
            Meter.SimpleEndDeviceFunction endDeviceFunctionRef = createEndDeviceFunctionRef(deviceConfigRef);
            meter.getComFunctionOrConnectDisconnectFunctionOrSimpleEndDeviceFunction().add(endDeviceFunctionRef);
            device.getConnectionTasks().forEach(connTask -> meter.getConnectionAttributes().add(getConnectionAttribute(connTask)));
        });
        return meterConfig;
    }

    @Override
    public MeterConfig asGetMeterConfig(Collection<Device> devices) {
        MeterConfig meterConfig = new MeterConfig();
        for (Device device : devices) {
            Meter meter = getMeter(device);
            meterConfig.getMeter().add(meter);
            SimpleEndDeviceFunction simpleEndDeviceFunction = getSimpleEndDeviceFunction(device, meter);
            meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        }
        return meterConfig;
    }

    private Meter createMeter(Device device) {
        Meter meter = new Meter();
        meter.setMRID(device.getmRID());
        meter.setSerialNumber(device.getSerialNumber());
        meter.getNames().add(createName(device.getName()));
        device.getBatch().map(Batch::getName).ifPresent(meter::setLotNumber);
        meter.setEndDeviceInfo(createEndDeviceInfo(device));
        meter.setType(device.getDeviceConfiguration().getDeviceType().getName());
        meter.getMeterMultipliers().add(createMultiplier(device.getMultiplier()));
        meter.setStatus(createStatus(device));
        return meter;
    }

    private EndDeviceInfo createEndDeviceInfo(Device device) {
        EndDeviceInfo endDeviceInfo = new EndDeviceInfo();
        ProductAssetModel productAssetModel = createAssetModel(device);
        endDeviceInfo.setAssetModel(productAssetModel);
        return endDeviceInfo;
    }

    private ProductAssetModel createAssetModel(Device device) {
        ProductAssetModel productAssetModel = new ProductAssetModel();
        productAssetModel.setModelNumber(device.getModelNumber());
        productAssetModel.setModelVersion(device.getModelVersion());
        Manufacturer manufacturer = createManufacturer(device.getManufacturer());
        productAssetModel.setManufacturer(manufacturer);
        return productAssetModel;
    }

    private Manufacturer createManufacturer(String manufacturerName) {
        if (manufacturerName == null) {
            return null;
        }
        Manufacturer manufacturer = new Manufacturer();
        Name name = createName(manufacturerName);
        manufacturer.getNames().add(name);
        return manufacturer;
    }

    private SimpleEndDeviceFunction createSimpleEndDeviceFunction(String deviceConfigRef, String deviceConfigName) {
        SimpleEndDeviceFunction simpleEndDeviceFunction = new SimpleEndDeviceFunction();
        simpleEndDeviceFunction.setMRID(deviceConfigRef);
        simpleEndDeviceFunction.setConfigID(deviceConfigName);
        simpleEndDeviceFunction.setZones(new Zones());
        return simpleEndDeviceFunction;
    }

    private Meter.SimpleEndDeviceFunction createEndDeviceFunctionRef(String deviceConfigRef) {
        Meter.SimpleEndDeviceFunction simpleEndDeviceFunctionRef = new Meter.SimpleEndDeviceFunction();
        simpleEndDeviceFunctionRef.setRef(deviceConfigRef);
        return simpleEndDeviceFunctionRef;
    }

    private Name createName(String name) {
        Name nameBean = new Name();
        nameBean.setName(name);
        return nameBean;
    }

    private Status createStatus(Device device) {
        String stateKey = device.getState().getName();
        String stateName = DefaultState.fromKey(stateKey)
                .map(DefaultState::getDefaultFormat)
                .orElse(stateKey);
        Status status = new Status();
        status.setValue(stateName);
        return status;
    }

    private MeterMultiplier createMultiplier(BigDecimal multiplier) {
        MeterMultiplier meterMultiplier = new MeterMultiplier();
        meterMultiplier.setValue(multiplier.floatValue());
        return meterMultiplier;
    }

    private Meter getMeter(Device device) {
        Meter meter = new Meter();
        meter.setMRID(device.getmRID());
        meter.setSerialNumber(device.getSerialNumber());
        meter.getNames().add(createName(device.getName()));
        device.getBatch().map(Batch::getName).ifPresent(meter::setLotNumber);
        meter.setEndDeviceInfo(createEndDeviceInfo(device));
        meter.setType(device.getDeviceConfiguration().getDeviceType().getName());
        meter.getMeterMultipliers().add(createMultiplier(device.getMultiplier()));
        String stateKey = device.getState().getName();
        String stateName = DefaultState.fromKey(stateKey)
                .map(DefaultState::getDefaultFormat)
                .orElse(stateKey);
        meter.setStatus(createStatus(stateName));
        //general attributes
        List<CustomAttributeSet> generalList = new ArrayList<>();
        TypedProperties deviceProperties = device.getDeviceProtocolProperties();
        if (device.getDeviceType().getDeviceProtocolPluggableClass().isPresent()) {
            List<PropertySpec> propertySpecs = device.getDeviceType().getDeviceProtocolPluggableClass().get().getDeviceProtocol().getPropertySpecs();
            CustomAttributeSet attributeSet = new CustomAttributeSet();
            attributeSet.setId(thesaurus.getFormat(TranslationKeys.GENERAL_ATTRIBUTES).format());
            for (PropertySpec propertySpec : propertySpecs) {
                Attribute attr = new Attribute();
                attr.setName(propertySpec.getName());
                Object propertyValue = getPropertyValue(propertySpec, deviceProperties);
                attr.setValue(convertPropertyValue(propertySpec, propertyValue));
                attributeSet.getAttribute().add(attr);
            }
            generalList.add(attributeSet);
        }
        meter.getMeterCustomAttributeSet().addAll(generalList);
        //custom attributes
        List<CustomAttributeSet> customList = device.getDeviceType().getCustomPropertySets()
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .map(registeredCustomPropertySet -> convertToCustomAttributeSet(registeredCustomPropertySet, device))
                .collect(Collectors.toList());
        meter.getMeterCustomAttributeSet().addAll(customList);

        device.getConnectionTasks().forEach(connTask -> meter.getConnectionAttributes().add(getConnectionAttribute(connTask)));
        return meter;
    }

    private ConnectionAttributes getConnectionAttribute(ConnectionTask<?, ?> connTask) {
        TypedProperties typedProperties = connTask.getTypedProperties();
        ConnectionAttributes attr = new ConnectionAttributes();
        attr.setConnectionMethod(connTask.getName());
        for (PropertySpec propertySpec : connTask.getPluggableClass().getPropertySpecs()) {
            ch.iec.tc57._2011.meterconfig.Attribute attribute = new ch.iec.tc57._2011.meterconfig.Attribute();
            attribute.setName(propertySpec.getName());
            Object value = typedProperties.getLocalValue(propertySpec.getName());
            if (value == null) {
                value = typedProperties.getInheritedValue(propertySpec.getName());
            }
            attribute.setValue(convertPropertyValue(propertySpec, value));
            attr.getAttribute().add(attribute);
        }
        return attr;
    }

    private Object getPropertyValue(PropertySpec propertySpec, TypedProperties deviceProperties) {
        Object domainValue = deviceProperties.getLocalValue(propertySpec.getName());
        if (domainValue == null) {
            domainValue = deviceProperties == null ? null : deviceProperties.getInheritedValue(propertySpec.getName());
            if (domainValue == null) {
                return getDefaultPropertyValue(propertySpec);
            }
        }
        return domainValue;
    }

    private CustomAttributeSet convertToCustomAttributeSet(RegisteredCustomPropertySet registeredCustomPropertySet, Device device) {
        CustomAttributeSet customAttributeSet = new CustomAttributeSet();
        CustomPropertySetValues values = null;
        CustomPropertySet propertySet = registeredCustomPropertySet.getCustomPropertySet();
        List<PropertySpec> propertySpecs = propertySet.getPropertySpecs();
        customAttributeSet.setId(propertySet.getId());
        if (!propertySet.isVersioned()) {
            values = customPropertySetService.getUniqueValuesFor(propertySet, device);
        } else {
            values = customPropertySetService.getUniqueValuesFor(propertySet, device, clock.instant());
            if (values.isEmpty()) {
                return customAttributeSet; // for versioned CAS empty values means no version
            }
        }
        for (PropertySpec propertySpec : propertySpecs) {
            Attribute attr = new Attribute();
            attr.setName(propertySpec.getName());
            if (values == null) {
                Object propertyValue = getDefaultPropertyValue(propertySpec);
                attr.setValue(convertPropertyValue(propertySpec, propertyValue));
                customAttributeSet.getAttribute().add(attr);
            } else {
                attr.setValue(convertPropertyValue(propertySpec, values.getProperty(propertySpec.getName())));
                customAttributeSet.getAttribute().add(attr);
            }
            if (propertySet.isVersioned() && values != null) {
                if (values.getEffectiveRange().hasLowerBound()) {
                    customAttributeSet.setFromDateTime(values.getEffectiveRange().lowerEndpoint());
                    customAttributeSet.setVersionId(values.getEffectiveRange().lowerEndpoint());
                } else {
                    customAttributeSet.setVersionId(device.getCreateTime());
                }
                if (values.getEffectiveRange().hasUpperBound()) {
                    customAttributeSet.setToDateTime(values.getEffectiveRange().upperEndpoint());
                }
            }
        }
        return customAttributeSet;
    }

    private Object getDefaultPropertyValue(PropertySpec propertySpec) {
        return propertySpec.getPossibleValues() == null ? null : propertySpec.getPossibleValues().getDefault();
    }

    private String convertPropertyValue(PropertySpec spec, Object value) {
        return spec.getValueFactory().toStringValue(value);
    }

    private SimpleEndDeviceFunction getSimpleEndDeviceFunction(Device device, Meter meter) {
        String deviceConfigRef = Long.toString(device.getDeviceConfiguration().getId());
        Meter.SimpleEndDeviceFunction endDeviceFunctionRef = createEndDeviceFunctionRef(deviceConfigRef);
        meter.getComFunctionOrConnectDisconnectFunctionOrSimpleEndDeviceFunction().add(endDeviceFunctionRef);
        return createSimpleEndDeviceFunction(deviceConfigRef, device.getDeviceConfiguration().getName());
    }

    private Status createStatus(String state) {
        Status status = new Status();
        status.setValue(state);
        return status;
    }

}
