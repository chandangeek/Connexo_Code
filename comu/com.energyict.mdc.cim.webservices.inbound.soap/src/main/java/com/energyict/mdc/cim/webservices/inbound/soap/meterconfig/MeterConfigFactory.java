/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

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
import ch.iec.tc57._2011.meterconfig.Zone;
import ch.iec.tc57._2011.meterconfig.Zones;

import java.math.BigDecimal;
import java.util.Map;

class MeterConfigFactory {

    MeterConfig asMeterConfig(Device device) {
        MeterConfig meterConfig = new MeterConfig();
        Meter meter = createMeter(device);
        meterConfig.getMeter().add(meter);
        SimpleEndDeviceFunction simpleEndDeviceFunction = createSimpleEndDeviceFunction(device, meter);
        meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
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
        String stateKey = device.getState().getName();
        String stateName = DefaultState.fromKey(stateKey)
                .map(DefaultState::getDefaultFormat)
                .orElse(stateKey);
        meter.setStatus(createStatus(stateName));
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

    private SimpleEndDeviceFunction createSimpleEndDeviceFunction(Device device, Meter meter) {
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
        if(device.getZones() != null) {
            simpleEndDeviceFunction.setZones(new Zones());
            for (Map.Entry<String, String> zone : device.getZones().entries()) {
                simpleEndDeviceFunction.getZones().getZone().add(createZone(zone.getKey(), zone.getValue()));
            }
        }
        return simpleEndDeviceFunction;
    }

    private Name createName(String name) {
        Name nameBean = new Name();
        nameBean.setName(name);
        return nameBean;
    }

    private Status createStatus(String state) {
        Status status = new Status();
        status.setValue(state);
        return status;
    }

    private MeterMultiplier createMultiplier(BigDecimal multiplier) {
        MeterMultiplier meterMultiplier = new MeterMultiplier();
        meterMultiplier.setValue(multiplier.floatValue());
        return meterMultiplier;
    }

    private Zone createZone(String zoneName, String zoneTypeName) {
        Zone zone = new Zone();
        zone.setZoneName(zoneName);
        zone.setZoneType(zoneTypeName);
        return zone;
    }
}