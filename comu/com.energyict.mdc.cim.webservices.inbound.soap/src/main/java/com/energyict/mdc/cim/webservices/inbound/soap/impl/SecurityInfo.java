package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import java.util.List;
import java.util.Optional;

public class SecurityInfo {

	private List<SecurityKeyInfo> securityKeys;

	private List<String> deviceStatuses;

	private boolean deviceStatusesElementPresent;

	public List<SecurityKeyInfo> getSecurityKeys() {
		return securityKeys;
	}

	public void setSecurityKeys(List<SecurityKeyInfo> securityKeys) {
		this.securityKeys = securityKeys;
	}

	public List<String> getDeviceStatuses() {
		return deviceStatuses;
	}

	public void setDeviceStatuses(List<String> deviceStatuses) {
		this.deviceStatuses = deviceStatuses;
	}

	public boolean isDeviceStatusesElementPresent() {
		return deviceStatusesElementPresent;
	}

	public void setDeviceStatusesElementPresent(boolean deviceStatusesElementPresent) {
		this.deviceStatusesElementPresent = deviceStatusesElementPresent;
	}
}