package com.energyict.mdc.cim.webservices.inbound.soap.impl;

public class SecurityKeyInfo {

	private String publicKeyLabel;

	private byte[] symmetricKey;

	private byte[] securityAccessorKey;

	private String securityAccessorName;

	public String getPublicKeyLabel() {
		return publicKeyLabel;
	}

	public void setPublicKeyLabel(String publicKeyLabel) {
		this.publicKeyLabel = publicKeyLabel;
	}

	public byte[] getSymmetricKey() {
		return symmetricKey;
	}

	public void setSymmetricKey(byte[] symmetricKey) {
		this.symmetricKey = symmetricKey;
	}

	public byte[] getSecurityAccessorKey() {
		return securityAccessorKey;
	}

	public void setSecurityAccessorKey(byte[] securityAccessorKey) {
		this.securityAccessorKey = securityAccessorKey;
	}

	public String getSecurityAccessorName() {
		return securityAccessorName;
	}

	public void setSecurityAccessorName(String securityAccessorName) {
		this.securityAccessorName = securityAccessorName;
	}

}
