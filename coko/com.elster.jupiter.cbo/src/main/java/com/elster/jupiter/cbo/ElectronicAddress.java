/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import javax.xml.bind.annotation.XmlTransient;

public final class ElectronicAddress implements Cloneable {
	private String email1;
	private String email2;
	private String lan;
	private String mac;
	private String password;
	private String radio;
	private String userID;
	private String web;
	
	public ElectronicAddress(String email1) {
		this.email1 = email1;
	}
	
	public ElectronicAddress() {
	}
	
	public String getEmail1() {
		return email1;
	}
	
	public void setEmail1(String email1) {
		this.email1 = email1;
	}
	
	public String getEmail2() {
		return email2;
	}
	
	public void setEmail2(String email2) {
		this.email2 = email2;
	}
	
	public String getLan() {
		return lan;
	}
	
	public void setLan(String lan) {
		this.lan = lan;
	}
	
	public String getMac() {
		return mac;
	}
	
	public void setMac(String mac) {
		this.mac = mac;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getRadio() {
		return radio;
	}
	
	public void setRadio(String radio) {
		this.radio = radio;
	}
	
	public String getUserId() {
		return userID;
	}
	
	public void setUserID(String userID) {
		this.userID = userID;
	}
	
	public String getWeb() {
		return web;
	}
	
	public void setWeb(String web) {
		this.web = web;
	}
	
	public ElectronicAddress copy() {
		try {
			return (ElectronicAddress) this.clone();
		} catch (CloneNotSupportedException ex) {
			throw new UnsupportedOperationException(ex);
		}
	}
	
	@XmlTransient
	public boolean isEmpty() {
		return 
			email1 == null &&
			email2 == null &&
			lan == null &&
			mac == null &&
			password == null &&
			radio == null &&
			userID == null &&
			web == null;
	}
}
