package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import java.util.List;
import java.util.Optional;

public class SecurityInfo {

	private List<SecurityKeyInfo> securityKeys;

	private Optional<List<String>> deviceStatuses;

	public List<SecurityKeyInfo> getSecurityKeys() {
		return securityKeys;
	}

	public void setSecurityKeys(List<SecurityKeyInfo> securityKeys) {
		this.securityKeys = securityKeys;
	}

	public Optional<List<String>> getDeviceStatuses() {
		return deviceStatuses;
	}

	public void setDeviceStatuses(Optional<List<String>> deviceStatuses) {
		this.deviceStatuses = deviceStatuses;
	}

}
