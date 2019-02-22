/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.sendmeterconfig;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.upl.TypedProperties;

import ch.iec.tc57._2011.meterconfig.EndDeviceInfo;
import ch.iec.tc57._2011.meterconfig.Manufacturer;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfig.MeterMultiplier;
import ch.iec.tc57._2011.meterconfig.Name;
import ch.iec.tc57._2011.meterconfig.ProductAssetModel;
import ch.iec.tc57._2011.meterconfig.SimpleEndDeviceFunction;
import ch.iec.tc57._2011.meterconfig.Status;
import com.elster.connexo._2017.schema.customattributes.Attribute;
import com.elster.connexo._2017.schema.customattributes.CustomAttributeSet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(name="com.energyict.mdc.cim.webservices.outbound.soap.sendmeterconfig.GetMeterConfigFactory", service=GetMeterConfigFactory.class)
public class GetMeterConfigFactoryImpl implements GetMeterConfigFactory {
    private volatile CustomPropertySetService customPropertySetService;
    private volatile Clock clock;

    public GetMeterConfigFactoryImpl() {

    }

    public MeterConfig asMeterConfig(List<Device> devices) {
        MeterConfig meterConfig = new MeterConfig();
        for (Device device : devices) {
            Meter meter = getMeter(device);
            meterConfig.getMeter().add(meter);
            SimpleEndDeviceFunction simpleEndDeviceFunction = getSimpleEndDeviceFunction(device, meter);
            meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        }
        return meterConfig;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    private Meter getMeter(Device device) {
        Meter meter = new Meter();
        meter.setMRID(device.getmRID());
        meter.setSerialNumber(device.getSerialNumber());
        meter.getNames().add(getName(device.getName()));
        device.getBatch().map(Batch::getName).ifPresent(meter::setLotNumber);
        meter.setEndDeviceInfo(getEndDeviceInfo(device));
        meter.setType(device.getDeviceConfiguration().getDeviceType().getName());
        meter.getMeterMultipliers().add(getMultiplier(device.getMultiplier()));
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
            attributeSet.setId(MessageSeeds.GENERAL_ATTRIBUTES.getDefaultFormat());
            for (PropertySpec propertySpec : propertySpecs) {
                Attribute attr = new Attribute();
                attr.setName(propertySpec.getName());
                Object propertyValue = getPropertyValue(propertySpec, deviceProperties, deviceProperties.getLocalValue(propertySpec.getName()) != null ? deviceProperties::getLocalValue : null);
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
        return meter;
    }

    private Object getPropertyValue(PropertySpec propertySpec, TypedProperties deviceProperties, Function<String, Object> propertyValueProvider) {
        Object domainValue = null;
        if (propertyValueProvider != null) {
            domainValue = propertyValueProvider.apply(propertySpec.getName());
        }
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
        if (!propertySet.isVersioned()) {
            values = customPropertySetService.getUniqueValuesFor(propertySet, device);
        } else {
            values = customPropertySetService.getUniqueValuesFor(propertySet, device, clock.instant());
        }
        List<PropertySpec> propertySpecs = propertySet.getPropertySpecs();
        customAttributeSet.setId(propertySet.getId());
        for (PropertySpec propertySpec : propertySpecs) {
            Attribute attr = new Attribute();
            attr.setName(propertySpec.getName());
            Object value = (values == null)?null:values.getProperty(propertySpec.getName());
            if (value == null) {
                Object propertyValue = getDefaultPropertyValue(propertySpec);
                attr.setValue(convertPropertyValue(propertySpec, propertyValue));
                customAttributeSet.getAttribute().add(attr);
            } else {
                attr.setValue(convertPropertyValue(propertySpec, value));
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

    private EndDeviceInfo getEndDeviceInfo(Device device) {
        EndDeviceInfo endDeviceInfo = new EndDeviceInfo();
        ProductAssetModel productAssetModel = createAssetModel(device);
        endDeviceInfo.setAssetModel(productAssetModel);
        return endDeviceInfo;
    }

    private SimpleEndDeviceFunction getSimpleEndDeviceFunction(Device device, Meter meter) {
        String deviceConfigRef = "" + device.getDeviceConfiguration().getId();
        Meter.SimpleEndDeviceFunction endDeviceFunctionRef = createEndDeviceFunctionRef(deviceConfigRef);
        meter.getComFunctionOrConnectDisconnectFunctionOrSimpleEndDeviceFunction().add(endDeviceFunctionRef);
        return createEndDeviceFunction(deviceConfigRef, device);
    }

    private Meter.SimpleEndDeviceFunction createEndDeviceFunctionRef(String deviceConfigRef) {
        Meter.SimpleEndDeviceFunction simpleEndDeviceFunctionRef = new Meter.SimpleEndDeviceFunction();
        simpleEndDeviceFunctionRef.setRef(deviceConfigRef);
        return simpleEndDeviceFunctionRef;
    }

    private SimpleEndDeviceFunction createEndDeviceFunction(String deviceConfigRef, Device device) {
        SimpleEndDeviceFunction simpleEndDeviceFunction = new SimpleEndDeviceFunction();
        simpleEndDeviceFunction.setMRID(deviceConfigRef);
        simpleEndDeviceFunction.setConfigID(device.getDeviceConfiguration().getName());
        return simpleEndDeviceFunction;
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
        Name name = getName(manufacturerName);
        manufacturer.getNames().add(name);
        return manufacturer;
    }

    private Name getName(String name) {
        Name nameBean = new Name();
        nameBean.setName(name);
        return nameBean;
    }

    private Status createStatus(String state) {
        Status status = new Status();
        status.setValue(state);
        return status;
    }

    private MeterMultiplier getMultiplier(BigDecimal multiplier) {
        MeterMultiplier meterMultiplier = new MeterMultiplier();
        meterMultiplier.setValue(multiplier.floatValue());
        return meterMultiplier;
    }
}