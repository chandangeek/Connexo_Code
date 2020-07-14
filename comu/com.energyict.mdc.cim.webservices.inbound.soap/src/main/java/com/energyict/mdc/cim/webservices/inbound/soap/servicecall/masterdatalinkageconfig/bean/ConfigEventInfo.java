/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean;

import ch.iec.tc57._2011.masterdatalinkageconfig.ConfigurationEvent;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.time.Instant;

public class ConfigEventInfo {

	private Instant createdDateTime;
	private Instant effectiveDateTime;

	public ConfigEventInfo() {
		super();
	}

	public ConfigEventInfo(ConfigurationEvent configurationEvent) {
		super();
		createdDateTime = configurationEvent.getCreatedDateTime();
		effectiveDateTime = configurationEvent.getEffectiveDateTime();
	}

	@JsonIgnore
	public Instant getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(Instant createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	@JsonGetter
	public long getEpochCreatedDateTime() {
		return createdDateTime != null ? createdDateTime.toEpochMilli() : 0;
	}

	@JsonSetter
	public void setEpochCreatedDateTime(long epochCreatedDateTime) {
		createdDateTime = Instant.ofEpochMilli(epochCreatedDateTime);
	}

	@JsonIgnore
	public Instant getEffectiveDateTime() {
		return effectiveDateTime;
	}

	public void setEffectiveDateTime(Instant effectiveDateTime) {
		this.effectiveDateTime = effectiveDateTime;
	}

	@JsonGetter
	public long getEpochEffectiveDateTime() {
		return effectiveDateTime != null ? effectiveDateTime.toEpochMilli() : 0;
	}

	@JsonSetter
	public void setEpochEffectiveDateTime(long epochEffectiveDateTime) {
		effectiveDateTime = Instant.ofEpochMilli(epochEffectiveDateTime);
	}
}
