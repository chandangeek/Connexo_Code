/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.edf.messages.objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FtpServerId  extends ComplexCosemObject{

	protected final static String ELEMENTNAME = "ftpServerId";
	protected final static String PORTATTRIBUTE = "port";
	protected final static String USERNAMEATTRIBUTE = "username";
	protected final static String PASSWORDATTRIBUTE = "password";
	protected final static String SERVERADDRESSATTRIBUTE = "serveraddress";
	protected final static String SENDERADDRESSATTRIBUTE = "senderaddress";

	private int portNumber;
	private OctetString username = new OctetString();
	private OctetString password = new OctetString();
	private OctetString serverAddress = new OctetString();
	private OctetString senderAddress = new OctetString();
	
	public FtpServerId() {
		super();
	}

	public FtpServerId(int portNumber, String username,
			String password, String serverAddress,
			String senderAddress) {
		super();
		this.portNumber = portNumber;
		this.username = new OctetString(username);
		this.password = new OctetString(password);
		this.serverAddress = new OctetString(serverAddress);
		this.senderAddress = new OctetString(senderAddress);
	}

	public FtpServerId(int portNumber, byte[] username,
			byte[] password, byte[] serverAddress,
			byte[] senderAddress) {
		super();
		this.portNumber = portNumber;
		this.username = new OctetString(username);
		this.password = new OctetString(password);
		this.serverAddress = new OctetString(serverAddress);
		this.senderAddress = new OctetString(senderAddress);
	}

	public FtpServerId(Element element) {
		super(element);
		this.portNumber = Integer.parseInt(element.getAttribute(PORTATTRIBUTE));
		this.username = new OctetString(element.getAttribute(USERNAMEATTRIBUTE));
		this.password = new OctetString(element.getAttribute(PASSWORDATTRIBUTE));
		this.serverAddress = new OctetString(element.getAttribute(SERVERADDRESSATTRIBUTE));
		this.senderAddress = new OctetString(element.getAttribute(SENDERADDRESSATTRIBUTE));
	}

	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portnumber) {
		this.portNumber = portnumber;
	}

	public String getUsername() {
		return username.convertOctetStringToString();
	}

	public void setUsername(String username) {
		this.username = new OctetString(username);
	}

	public String getPassword() {
		return password.convertOctetStringToString();
	}

	public void setPassword(String password) {
		this.password = new OctetString(password);
	}

	public String getServerAddress() {
		return serverAddress.convertOctetStringToString();
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = new OctetString(serverAddress);
	}

	public String getSenderAddress() {
		return senderAddress.convertOctetStringToString();
	}

	public void setSenderAddress(String senderAddress) {
		this.senderAddress = new OctetString(senderAddress);
	}

	public byte[] getUsernameOctets() {
		return username.getOctets();
	}

	public void setUsernameOctets(byte[] username) {
		this.username = new OctetString(username);
	}

	public byte[] getPasswordOctets() {
		return password.getOctets();
	}

	public void setPasswordOctets(byte[] password) {
		this.password = new OctetString(password);
	}

	public byte[] getServerAddressOctets() {
		return serverAddress.getOctets();
	}

	public void setServerAddressOctets(byte[] serverAddress) {
		this.serverAddress = new OctetString(serverAddress);
	}

	public byte[] getSenderAddressOctets() {
		return senderAddress.getOctets();
	}

	public void setSenderAddressOctets(byte[] senderAddress) {
		this.senderAddress = new OctetString(senderAddress);
	}

	public Element generateXMLElement(Document document) {
		Element root = document.createElement(ELEMENTNAME);
		root.setAttribute(PORTATTRIBUTE, ""+portNumber);
		root.setAttribute(USERNAMEATTRIBUTE, username.convertOctetStringToString());
		root.setAttribute(PASSWORDATTRIBUTE, password.convertOctetStringToString());
		root.setAttribute(SERVERADDRESSATTRIBUTE, serverAddress.convertOctetStringToString());
		root.setAttribute(SENDERADDRESSATTRIBUTE, senderAddress.convertOctetStringToString());
		return root;
	}

	
	
}
