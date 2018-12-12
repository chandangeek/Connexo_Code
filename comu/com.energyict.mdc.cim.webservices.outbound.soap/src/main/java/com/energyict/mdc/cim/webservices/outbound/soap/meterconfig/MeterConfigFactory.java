package com.energyict.mdc.cim.webservices.outbound.soap.meterconfig;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import ch.iec.tc57._2011.meterconfig.EndDeviceInfo;
import ch.iec.tc57._2011.meterconfig.Manufacturer;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfig.MeterMultiplier;
import ch.iec.tc57._2011.meterconfig.Name;
import ch.iec.tc57._2011.meterconfig.ProductAssetModel;
import ch.iec.tc57._2011.meterconfig.SimpleEndDeviceFunction;
import ch.iec.tc57._2011.meterconfig.Status;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MeterConfigFactory {

    public MeterConfig asMeterConfig(List<Device> devices) {
        Set<String> deviceConfigRefs = new HashSet<>();
        MeterConfig meterConfig = new MeterConfig();
        devices.forEach(device -> {
            Meter meter = createMeter(device);
            meterConfig.getMeter().add(meter);

            DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
            String deviceConfigRef = "" + deviceConfiguration.getId();
            if (!deviceConfigRefs.contains(deviceConfigRef)) {
                meterConfig.getSimpleEndDeviceFunction().add(createSimpleEndDeviceFunction(deviceConfigRef, deviceConfiguration.getName()));
                deviceConfigRefs.add(deviceConfigRef);
            }
            Meter.SimpleEndDeviceFunction endDeviceFunctionRef = createEndDeviceFunctionRef(deviceConfigRef);
            meter.getComFunctionOrConnectDisconnectFunctionOrSimpleEndDeviceFunction().add(endDeviceFunctionRef);
        });
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
}