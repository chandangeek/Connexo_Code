package com.energyict.dlms;

/*
 * DLMSConnection.java
 *
 * Created on 11 oktober 2007, 10:18
 */

import com.energyict.dlms.protocolimplv2.connection.DlmsConnection;

/**
 * This extends from the V2 DlmsConnection and adds some getters/setters that are used by the V1 protocols
 * <p/>
 * The interface provides all functionality to start, use and stop a communication session.
 * It can be used by an application layer to handle communication with a device.
 *
 * @author kvds
 */
public interface DLMSConnection extends DlmsConnection {

    /**
     * Set the type of SNRM frame for the HDLC connection.
     * This changes the HDLC sign on frame.
     * Otherwise, a default frame is used that works in most cases.
     *
     * @param type: 1 = use specific parameter lengths, 0 = use defaults
     */
    void setSNRMType(int type);

    /**
     * Switch the client an server addresses in the TCP IP connection
     *
     * @param type: 1 = switch, 0 = don't switch
     */
    void setIskraWrapper(int type);

}