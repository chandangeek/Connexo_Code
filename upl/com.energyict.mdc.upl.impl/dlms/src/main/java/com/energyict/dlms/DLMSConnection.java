/*
 * DLMSConnection.java
 *
 * Created on 11 oktober 2007, 10:18
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dlms;

import com.energyict.dialer.connection.*;
import java.io.*;

/**
 *
 * @author kvds
 */
public interface DLMSConnection {
    
    public final int DLMS_CONNECTION_TCPIP=1;
    public final int DLMS_CONNECTION_HDLC=0;
    
    public byte[] sendRequest(byte[] byteRequestBuffer) throws IOException;
    public void setHHUSignOn(HHUSignOn hhuSignOn,String meterId);
    public HHUSignOn getHhuSignOn();
    public void connectMAC() throws IOException,DLMSConnectionException;
    public void disconnectMAC() throws IOException,DLMSConnectionException;
    public int getType();
	public void setSNRMType(int type);  
	public void setIskraWrapper(int type);
	public void setInvokeIdAndPriority(InvokeIdAndPriority iiap);
	public InvokeIdAndPriority getInvokeIdAndPriority();
}
