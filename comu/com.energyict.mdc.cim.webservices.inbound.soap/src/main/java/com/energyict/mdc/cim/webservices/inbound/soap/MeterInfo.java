/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.SecurityInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.customattributeset.CasInfo;

import ch.iec.tc57._2011.meterconfig.ConnectionAttributes;
import ch.iec.tc57._2011.meterconfig.ElectronicAddress;
import ch.iec.tc57._2011.meterconfig.SharedCommunicationSchedule;
import ch.iec.tc57._2011.meterconfig.Zone;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class MeterInfo {
    private String deviceName;
    private String mRID;
    private String name;
    private String serialNumber;
    private String batch;
    private String manufacturer;
    private String modelNumber;
    private String modelVersion;
    private BigDecimal multiplier;
    private Instant shipmentDate;
    private String deviceType;
    private String deviceConfigurationName;
    private String statusValue;
    private Instant statusEffectiveDate;
    private Instant multiplierEffectiveDate;
    private String configurationEventReason;
    private ElectronicAddressInfo electronicAddressInfo;
    private List<Zone> zones = Collections.emptyList();
    private List<CasInfo> customAttributeSets;
    private SecurityInfo securityInfo;
    private List<ConnectionAttributes> connectionAttributes;
    private List<SharedCommunicationSchedule> SharedCommunicationSchedules;

    public MeterInfo() {
    }

    public List<SharedCommunicationSchedule> getSharedCommunicationSchedules() {
        return SharedCommunicationSchedules;
    }

    public void setSharedCommunicationSchedules(List<SharedCommunicationSchedule> sharedCommunicationSchedules) {
        SharedCommunicationSchedules = sharedCommunicationSchedules;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getmRID() {
        return mRID;
    }

    public void setmRID(String mRID) {
        this.mRID = mRID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    @JsonIgnore
    public Instant getShipmentDate() {
        return this.shipmentDate;
    }

    public void setShipmentDate(Instant time) {
        this.shipmentDate = time;
    }

    @JsonGetter
    private long getEpochShipmentDate() {
        return this.shipmentDate != null ? this.shipmentDate.toEpochMilli() : 0;
    }

    @JsonSetter
    private void setEpochShipmentDate(long time) {
        this.shipmentDate = Instant.ofEpochMilli(time);
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceConfigurationName() {
        return deviceConfigurationName;
    }

    public void setDeviceConfigurationName(String deviceConfigurationName) {
        this.deviceConfigurationName = deviceConfigurationName;
    }

    @JsonIgnore
    public Instant getStatusEffectiveDate() {
        return this.statusEffectiveDate;
    }

    public void setStatusEffectiveDate(Instant time) {
        this.statusEffectiveDate = time;
    }

    @JsonGetter
    private long getEpochStatusEffectiveDate() {
        return this.statusEffectiveDate != null ? this.statusEffectiveDate.toEpochMilli() : 0;
    }

    @JsonSetter
    private void setEpochStatusEffectiveDate(long time) {
        this.statusEffectiveDate = Instant.ofEpochMilli(time);
    }

    @JsonIgnore
    public Instant getMultiplierEffectiveDate() {
        return this.multiplierEffectiveDate;
    }

    public void setMultiplierEffectiveDate(Instant time) {
        this.multiplierEffectiveDate = time;
    }

    @JsonGetter
    private long getEpochMultiplierEffectiveDate() {
        return this.multiplierEffectiveDate != null ? this.multiplierEffectiveDate.toEpochMilli() : 0;
    }

    @JsonSetter
    private void setEpochMultiplierEffectiveDate(long time) {
        this.multiplierEffectiveDate = Instant.ofEpochMilli(time);
    }

    public String getStatusValue() {
        return statusValue;
    }

    public void setStatusValue(String statusValue) {
        this.statusValue = statusValue;
    }

    public String getConfigurationEventReason() {
        return configurationEventReason;
    }

    public void setConfigurationEventReason(String configurationEventReason) {
        this.configurationEventReason = configurationEventReason;
    }

    public ElectronicAddressInfo getElectronicAddress() {
        return electronicAddressInfo;
    }

    public void setElectronicAddress(ElectronicAddress electronicAddress) {
        electronicAddressInfo = new ElectronicAddressInfo(electronicAddress);
    }

    public List<Zone> getZones() {
        return zones;
    }

    public void setZones(List<Zone> zones) {
        this.zones = zones;
    }

    public List<CasInfo> getCustomAttributeSets() {
        return customAttributeSets;
    }

    public void setCustomAttributeSets(List<CasInfo> customAttributeSets) {
        this.customAttributeSets = customAttributeSets;
    }

    public SecurityInfo getSecurityInfo() {
        return securityInfo;
    }

    public void setSecurityInfo(SecurityInfo securityInfo) {
        this.securityInfo = securityInfo;
    }

    public List<ConnectionAttributes> getConnectionAttributes() {
        return connectionAttributes;
    }

    public void setConnectionAttributes(List<ConnectionAttributes> connectionAttributes) {
        this.connectionAttributes = connectionAttributes;
    }
}
