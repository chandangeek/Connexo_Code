/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AuthenticateRequest.java
 *
 * Created on 15/02/2006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class AuthenticateRequest extends AbstractRequest {

	final int DEBUG=0;
	RequestData requestData=new RequestData();
	private byte[] password;
	private byte[] ticket;
	private int securityLevel;
	private byte[] doubleEncryptedTicket;

	/** Creates a new instance of AuthenticateRequest */
	public AuthenticateRequest(PSEMServiceFactory psemServiceFactory) {
		super(psemServiceFactory);
	}

	protected void parse(ResponseData responseData) throws IOException {
		response = new AuthenticateResponse(getPSEMServiceFactory());
		response.build(responseData);
		AuthenticateResponse authenticateResponse = (AuthenticateResponse)response;
		// Securitylevel the same?
		if (authenticateResponse.getSecurityLevel() == getSecurityLevel()) {
			// length of response OK?
			if (getDoubleEncryptedTicket().length == authenticateResponse.getDoubleEncryptedTicket().length) {
				// check response, but not for A1800
				for (int i=0;i<getDoubleEncryptedTicket().length;i++) {
					if (getDoubleEncryptedTicket()[i] != authenticateResponse.getDoubleEncryptedTicket()[i]) {
						throw new IOException("Authentication failed, double encryption mismatch!");
					}
				}
			}
			//this exception can be commentted out in order to temporarily read the
			//elster A1800 until a proper fix is made
//			else throw new IOException("Authentication failed, double encryption length mismatch!");
		}
		else throw new IOException("Authentication failed, security level mismatch!");
	}

	protected RequestData getRequestData() {
		return requestData;
	}

	public void authenticate(int securityLevel, byte[] password, byte[] ticket) throws IOException {

		this.setSecurityLevel(securityLevel);
		this.password=password;
		this.ticket=ticket;

		requestData.setCode(AUTHENTICATE);
		byte[] data = new byte[10]; // Length (1 byte) + key (1 byte) + 8 bytes encrypted ticket
		data[0] = 9;
		data[1] = (byte)securityLevel;
		System.arraycopy(getEncryptedTicket(), 0, data,2,8);
		requestData.setData(data);
	}

	private byte[] getEncryptedTicket() throws IOException {
		DESEncryptor de = DESEncryptor.getInstance(password);
		if (DEBUG>=1) System.out.println("KV_DEBUG> ticket data: " + ProtocolUtils.outputHexString(ticket));
		byte[] data = de.encrypt(ticket);
		data = ProtocolUtils.getSubArray2(data, 0, 8);
		if (DEBUG>=1) System.out.println("KV_DEBUG> encrypted ticket: " + ProtocolUtils.outputHexString(data));
		doubleEncryptedTicket = de.encrypt(data);
		doubleEncryptedTicket = ProtocolUtils.getSubArray2(doubleEncryptedTicket, 0, 8);
		if (DEBUG>=1) System.out.println("KV_DEBUG> 2x encrypted ticket: " + ProtocolUtils.outputHexString(doubleEncryptedTicket));
		return data;
	}

	public byte[] getPassword() {
		return password;
	}

	private void setPassword(byte[] password) {
		this.password = password;
	}

	public byte[] getTicket() {
		return ticket;
	}

	private void setTicket(byte[] ticket) {
		this.ticket = ticket;
	}

	public int getSecurityLevel() {
		return securityLevel;
	}

	private void setSecurityLevel(int securityLevel) {
		this.securityLevel = securityLevel;
	}

	public byte[] getDoubleEncryptedTicket() {
		return doubleEncryptedTicket;
	}

	private void setDoubleEncryptedTicket(byte[] doubleEncryptedTicket) {
		this.doubleEncryptedTicket = doubleEncryptedTicket;
	}

}