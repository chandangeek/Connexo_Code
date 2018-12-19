/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.sendmeterconfig;

import ch.iec.tc57._2011.meterconfig.*;
import com.elster.connexo._2017.schema.customattributes.Attribute;
import com.elster.connexo._2017.schema.customattributes.CustomAttributeSet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.upl.TypedProperties;
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
            for (PropertySpec propertySpec : propertySpecs) {
                CustomAttributeSet attributeSet = new CustomAttributeSet();
                attributeSet.setId(MessageSeeds.GENERAL_ATTRIBUTES.getDefaultFormat());
                Attribute attr = new Attribute();
                attr.setName(propertySpec.getName());
                Object propertyValue = getPropertyValue(propertySpec, deviceProperties, deviceProperties.getLocalValue(propertySpec.getName()) != null ? deviceProperties::getLocalValue : null);
                attr.setValue(String.valueOf(propertyValue));
                attributeSet.getAttribute().add(attr);
                generalList.add(attributeSet);
            }
        }
        meter.getMeterCustomAttributeSet().addAll(generalList);
        //custom attributes
        List<CustomAttributeSet> customList = device.getDeviceType().getCustomPropertySets()
                .stream()
                //.filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
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
                return propertySpec.getPossibleValues() == null ? null : propertySpec.getPossibleValues().getDefault();
            }
        }
        return domainValue;
    }

    private CustomAttributeSet convertToCustomAttributeSet(RegisteredCustomPropertySet registeredCustomPropertySet, Device device) {
        CustomAttributeSet customAttribute = new CustomAttributeSet();
        CustomPropertySetValues values = null;
        if (!registeredCustomPropertySet.getCustomPropertySet().isVersioned()) {
            values = customPropertySetService.getUniqueValuesFor(registeredCustomPropertySet.getCustomPropertySet(), device);
        } else {
            values = customPropertySetService.getUniqueValuesFor(registeredCustomPropertySet.getCustomPropertySet(),
                            device,
                            this.clock.instant());
        }
        if (values == null || values.isEmpty()) {
            List<PropertySpec> propertySpecs = registeredCustomPropertySet.getCustomPropertySet().getPropertySpecs();
            for (PropertySpec propertySpec : propertySpecs) {
                Attribute attr = new Attribute();
                attr.setName(propertySpec.getName());
                Object propertyValue = getPropertyValue(propertySpec, null, null);
                attr.setValue(String.valueOf(propertyValue));
                customAttribute.getAttribute().add(attr);
            }
        } else {
            setAttrToCustomAttribute(values, customAttribute);
        }
        return customAttribute;
    }

    private void setAttrToCustomAttribute(CustomPropertySetValues values, CustomAttributeSet customAttribute) {
        for (String property : values.propertyNames()) {
            Attribute attr = new Attribute();
            attr.setName(property);
            attr.setValue(String.valueOf(values.getProperty(property)));
            customAttribute.getAttribute().add(attr);
            if (values.getEffectiveRange().hasLowerBound()) {
                customAttribute.setFromDateTime(values.getEffectiveRange().lowerEndpoint());
            }
            if (values.getEffectiveRange().hasUpperBound()) {
                customAttribute.setToDateTime(values.getEffectiveRange().upperEndpoint());
            }
        }
    }

    private EndDeviceInfo getEndDeviceInfo(Device device) {
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