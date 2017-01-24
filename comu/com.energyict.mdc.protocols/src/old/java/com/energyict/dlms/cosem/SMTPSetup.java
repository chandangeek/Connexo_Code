/*
 * Data.java
 *
 * Created on 30 augustus 2004, 13:52
 */

package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class SMTPSetup extends AbstractCosemObject {

    private Unsigned16 serverPort=null;
    private OctetString userName=null;
    private OctetString loginPassword=null;
    private OctetString serverAddress=null;
    private OctetString senderAddress=null;

    /** Creates a new instance of Data */
    public SMTPSetup(ProtocolLink protocolLink,ObjectReference objectReference) {
        super(protocolLink,objectReference);
    }

    protected int getClassId() {
        return DLMSClassId.SMTP_SETUP.getClassId();
    }

    public void writeServerPort(Unsigned16 serverPort) throws IOException {
        write(2, serverPort.getBEREncodedByteArray());
        this.serverPort=serverPort;
    }
    public Unsigned16 readServerPort() throws IOException {
        if (serverPort == null) {
            serverPort = (Unsigned16) AXDRDecoder.decode(getLNResponseData(2));
        }
        return serverPort;
    }

    public void writeUserName(OctetString userName) throws IOException {
        write(3, userName.getBEREncodedByteArray());
        this.userName=userName;
    }
    public OctetString readUserName() throws IOException {
        if (userName == null) {
            userName = (OctetString) AXDRDecoder.decode(getLNResponseData(3));
        }
        return userName;
    }

    public void writeLoginPassword(OctetString loginPassword) throws IOException {
        write(4, loginPassword.getBEREncodedByteArray());
        this.loginPassword=loginPassword;
    }
    public OctetString readLoginPassword() throws IOException {
        if (loginPassword==null) {
            loginPassword = (OctetString) AXDRDecoder.decode(getLNResponseData(4));
        }
        return loginPassword;
    }


    public void writeServerAddress(OctetString serverAddress) throws IOException {
        write(5, serverAddress.getBEREncodedByteArray());
        this.serverAddress=serverAddress;
    }
    public OctetString readServerAddress() throws IOException {
        if (serverAddress==null) {
            serverAddress = (OctetString) AXDRDecoder.decode(getLNResponseData(5));
        }
        return serverAddress;
    }

    public void writeSenderAddress(OctetString senderAddress) throws IOException {
        write(6, senderAddress.getBEREncodedByteArray());
        this.senderAddress=senderAddress;
    }
    public OctetString readSenderAddress() throws IOException {
        if (senderAddress==null) {
            senderAddress = (OctetString) AXDRDecoder.decode(getLNResponseData(6));
        }
        return senderAddress;
    }
}
