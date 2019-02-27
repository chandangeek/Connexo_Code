package com.elster.jupiter.cim.webservices.inbound.soap.servicecall;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import ch.iec.tc57._2011.masterdatalinkageconfig.ConfigurationEvent;

public class ConfigEventInfo {

	private Instant createdDateTime;
	private Instant effectiveDateTime;

	public ConfigEventInfo() {
		super();
	}

	public ConfigEventInfo(ConfigurationEvent configurationEvent) {
		super();
		this.createdDateTime = configurationEvent.getCreatedDateTime();
		this.effectiveDateTime = configurationEvent.getEffectiveDateTime();
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
		return this.createdDateTime != null ? this.createdDateTime.toEpochMilli() : 0;
	}

	@JsonSetter
	public void setEpochCreatedDateTime(long epochCreatedDateTime) {
		this.createdDateTime = Instant.ofEpochMilli(epochCreatedDateTime);
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
		return this.effectiveDateTime != null ? this.effectiveDateTime.toEpochMilli() : 0;
	}

	@JsonSetter
	public void setEpochEffectiveDateTime(long epochEffectiveDateTime) {
		this.effectiveDateTime = Instant.ofEpochMilli(epochEffectiveDateTime);
	}
}
