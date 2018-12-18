/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap;

import com.energyict.mdc.cim.webservices.inbound.soap.impl.CustomPropertySetInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.SecurityInfo;

import ch.iec.tc57._2011.meterconfig.ElectronicAddress;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.math.BigDecimal;
import java.time.Instant;
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
	private List<CustomPropertySetInfo> customAttributeSets;
	private List<SecurityInfo> securityInfos;

	public MeterInfo() {
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
		return shipmentDate;
	}

	public void setShipmentDate(Instant time) {
		shipmentDate = time;
	}

	@JsonGetter
	private long getEpochShipmentDate() {
		return shipmentDate != null ? shipmentDate.toEpochMilli() : 0;
	}

	@JsonSetter
	private void setEpochShipmentDate(long time) {
		shipmentDate = Instant.ofEpochMilli(time);
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
		return statusEffectiveDate;
	}

	public void setStatusEffectiveDate(Instant time) {
		statusEffectiveDate = time;
	}

	@JsonGetter
	private long getEpochStatusEffectiveDate() {
		return statusEffectiveDate != null ? statusEffectiveDate.toEpochMilli() : 0;
	}

	@JsonSetter
	private void setEpochStatusEffectiveDate(long time) {
		statusEffectiveDate = Instant.ofEpochMilli(time);
	}

	@JsonIgnore
	public Instant getMultiplierEffectiveDate() {
		return multiplierEffectiveDate;
	}

	public void setMultiplierEffectiveDate(Instant time) {
		multiplierEffectiveDate = time;
	}

	@JsonGetter
	private long getEpochMultiplierEffectiveDate() {
		return multiplierEffectiveDate != null ? multiplierEffectiveDate.toEpochMilli() : 0;
	}

	@JsonSetter
	private void setEpochMultiplierEffectiveDate(long time) {
		multiplierEffectiveDate = Instant.ofEpochMilli(time);
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

	public List<CustomPropertySetInfo> getCustomAttributeSets() {
		return customAttributeSets;
	}

	public void setCustomAttributeSets(List<CustomPropertySetInfo> customAttributeSets) {
		this.customAttributeSets = customAttributeSets;
	}

	public List<SecurityInfo> getSecurityInfos() {
		return securityInfos;
	}

	public void setSecurityInfoList(List<SecurityInfo> securityInfos) {
		this.securityInfos = securityInfos;
	}
}
