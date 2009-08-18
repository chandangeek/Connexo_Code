package com.energyict.dlms;

/*
 * DLMSConnection.java
 *
 * Created on 11 oktober 2007, 10:18
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

import java.io.IOException;

import com.energyict.dialer.connection.HHUSignOn;

/**
 *
 * @author kvds
 */
public interface DLMSConnection {

	int DLMS_CONNECTION_TCPIP=1;
	int DLMS_CONNECTION_HDLC=0;

	byte[] sendRequest(byte[] byteRequestBuffer) throws IOException;

	void setHHUSignOn(HHUSignOn hhuSignOn,String meterId);

	HHUSignOn getHhuSignOn();

	void connectMAC() throws IOException,DLMSConnectionException;

	void disconnectMAC() throws IOException,DLMSConnectionException;

	int getType();

	void setSNRMType(int type);

	void setIskraWrapper(int type);

	void setInvokeIdAndPriority(InvokeIdAndPriority iiap);

	InvokeIdAndPriority getInvokeIdAndPriority();

}
