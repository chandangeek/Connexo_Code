/*
 * DGCOMConnection.java
 *
 * Created on 22 mei 2006, 14:27
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom;

import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.inbound.MeterType;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Koen
 */
public class DGCOMConnection extends Connection  implements ProtocolConnection {

    private static final int DEBUG=0;
    private static final long TIMEOUT=60000;

    int timeout;
    int maxRetries;
    long forcedDelay;
    ByteArrayOutputStream txOutputStream = new ByteArrayOutputStream();
    String nodeId;
    int securityLevel;

    /** Creates a new instance of DGCOMConnection */
    public DGCOMConnection(InputStream inputStream,
            OutputStream outputStream,
            int timeout,
            int maxRetries,
            long forcedDelay,
            int echoCancelling,
            HalfDuplexController halfDuplexController,
            String serialNumber,int securityLevel) {
        super(inputStream, outputStream, forcedDelay, echoCancelling,halfDuplexController);
        this.timeout = timeout;
        this.maxRetries=maxRetries;
        this.forcedDelay=forcedDelay;
        this.securityLevel=securityLevel;

    }

    public MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws java.io.IOException {
        this.nodeId=nodeId;
        return null;
    }

    public byte[] dataReadout(String strID, String nodeId) throws NestedIOException, ProtocolConnectionException {
        return null;
    }

    public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {
    }

    public HHUSignOn getHhuSignOn() {
        return null;
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn) {
    }


    private void sendFrame() throws ConnectionException {
        sendOut(txOutputStream.toByteArray());
    }

    public void signon() throws IOException {
        if ((nodeId==null) || ("".compareTo(nodeId)==0)) {
            return;
        }
        int retry=0;
        doSignon();
        while(true) {
            try {
                delayAndFlush(forcedDelay); // KV_DEBUG
                try {
                   handshake();
                }
                catch(ConnectionException e) {
                    if (e.getReason() != TIMEOUT_ERROR) {
                        throw e;
                    }
                }
                sendFrame();
                receiveFrame(true,0);
            }
            catch(ConnectionException e) {
                if (e.getReason() == CRC_ERROR) {
                    throw new ProtocolConnectionException("sendCommand() error maxRetries (" + maxRetries + "), " + e.getMessage());
                }
                else {
                    if (retry++>=maxRetries) {
                        throw new ProtocolConnectionException("sendCommand() error maxRetries ("+maxRetries+"), "+e.getMessage());
                    }
                }
            }
        } // while(true)
    }

    public void doSignon() throws IOException {
        String str=new String("/?"+(nodeId==null?"":nodeId)+"!");
        byte[] rawData = str.getBytes();
        txOutputStream.reset();
        int checksum=0;
        txOutputStream.write(rawData);
        for (int i=0;i<rawData.length;i++) {
            checksum += (((int) rawData[i]) & 0xFF);
        }
        txOutputStream.write(checksum&0xFF);
        txOutputStream.write((checksum>>8)&0xFF);
    }

     public ResponseData sendCommand(byte[] cmdData) throws IOException {
         return sendCommand(cmdData, true);
     }

     public ResponseData sendCommand(byte[] cmdData, boolean response) throws IOException {
         return sendCommand(cmdData, response, 0);
     }
     public ResponseData sendCommand(byte[] cmdData, boolean response, int size) throws IOException {
        int retry=0;
        doSendCommand(cmdData);
        while(true) {
            try {
                delayAndFlush(forcedDelay); // KV_DEBUG
                handshake();
                sendFrame();
                return receiveFrame(response,size);
            } catch(ConnectionException e) {
                if (e.getReason() == CRC_ERROR) {
                    throw new ProtocolConnectionException("sendCommand() error maxRetries (" + maxRetries + "), " + e.getMessage());
                }
                else {
                    if (retry++>=maxRetries) {
                        throw new ProtocolConnectionException("sendCommand() error maxRetries ("+maxRetries+"), "+e.getMessage());
                    }
                }
            }
        } // while(true)
    }

    private void doSendCommand(byte[] rawData) throws IOException {
        txOutputStream.reset();
        int checksum=0;
        txOutputStream.write(rawData);
        for (int i=0;i<rawData.length;i++) {
            checksum += (((int) rawData[i]) & 0xFF);
        }
        txOutputStream.write(checksum&0xFF);
        txOutputStream.write((checksum>>8)&0xFF);
    }


    public void handshake() throws IOException {
        handshake(0x55);
        handshake((byte)0xAA);
    }

    private final int MAX_HANDSHAKE_RETRIES=5;
    private final int HANDSHAKE_TIMEOUT=2000; // = 2 sec.

    private void handshake(int kar) throws IOException {
        int karRx;
        int retries=0;
        kar = kar & 0xFF; // no sign extension! bytes are signed in java! so be careful with values >= 0x80!
        while(true) {
            sendOut((byte)kar);
            long timeout = System.currentTimeMillis() + HANDSHAKE_TIMEOUT;
            copyEchoBuffer();
            while(true) {
                if ((karRx = readIn()) != -1) {
                    if (karRx == kar) {
                        return;
                    }
                }
                if (System.currentTimeMillis() - timeout > 0) {
                    if (retries++<MAX_HANDSHAKE_RETRIES) {
                        timeout = System.currentTimeMillis() + HANDSHAKE_TIMEOUT;
                        break;
                    }
                    else {
                        throw new ProtocolConnectionException("handshake() interframe timeout error", TIMEOUT_ERROR);
                    }
                }
            }
        }
    }

    private final int STATE_WAIT_FOR_CONFIRMATION=0;
    private final int STATE_WAIT_FOR_LENGTH=1;
    private final int STATE_WAIT_FOR_DATA=2;
    private final int STATE_WAIT_FOR_CHECKSUM=3;
    private final int TRANSMISSION_ACCEPTED=0x33;
    private final int CHECKSUM_ERROR=0x99;
    private final int READY_FOR_NEXT_PACKET=0x66;
    private final int COMMAND_NOT_ACCEPTED=0xCC;
    private final int SUCCESSFUL_UNLOCK=0xDD;

    private ResponseData receiveFrame(boolean response, int size) throws IOException {

//System.out.println("KV_DEBUG> receiveFrame "+size);

        long protocolTimeout,interFrameTimeout;
        int kar;
        int count=0;
        int retry=0;
        int len=0;
        int checksum=0;
        int checksumRx=0;
        int state = STATE_WAIT_FOR_CONFIRMATION;
        ByteArrayOutputStream frameArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();


        interFrameTimeout = System.currentTimeMillis() + timeout;
        protocolTimeout = System.currentTimeMillis() + TIMEOUT;

        frameArrayOutputStream.reset();
        resultArrayOutputStream.reset();

        copyEchoBuffer();
        while(true) {
            if ((kar = readIn()) != -1) {
                switch(state) {
                    case STATE_WAIT_FOR_CONFIRMATION:
                        interFrameTimeout = System.currentTimeMillis() + timeout;

                        if (kar == TRANSMISSION_ACCEPTED) { // Transmission accepted
                            if (!response) {
                                return null;
                            }
                            else {
                                state = STATE_WAIT_FOR_LENGTH;
                                checksum=0;
                            }
                        }

                        if (kar == READY_FOR_NEXT_PACKET) { // Ready for next packet
                            //
                        }

                        if (kar == CHECKSUM_ERROR) { // Checksum error, re-transmit packet
                            if (DEBUG>=1) System.out.println("KV_DEBUG> Checksum error, re-transmit packet");
                            throw new ProtocolConnectionException("receiveFrame() Checksum error, re-transmit packet",PROTOCOL_ERROR);
                        }

                        if (kar == COMMAND_NOT_ACCEPTED) { // Command not accepted
                            if (DEBUG>=1) System.out.println("KV_DEBUG> Command not accepted");
                            throw new ProtocolConnectionException("receiveFrame() Command not accepted",PROTOCOL_ERROR);
                        }

                        if (kar == SUCCESSFUL_UNLOCK) { // Confirmation of successfull unlock with L1 security key (RX only)
                            if (DEBUG>=1) System.out.println("KV_DEBUG> Confirmation of successfull unlock with L1 security key (RX only)");
                            return null;
                        }

                        break; // STATE_WAIT_FOR_CONFIRMATION

                    case STATE_WAIT_FOR_LENGTH:
                        checksum+=kar;
                        len = kar;
                        count=0;
                        checksumRx=0;
                        state = STATE_WAIT_FOR_DATA;
                        break; // STATE_WAIT_FOR_LENGTH

                    case STATE_WAIT_FOR_DATA:
                        checksum+=kar;
                        frameArrayOutputStream.write(kar);
                        if (count++ >= (len-1)) {
                            count=0;
                            checksumRx=0;
                            state = STATE_WAIT_FOR_CHECKSUM;
                        }
                        break; // STATE_WAIT_FOR_DATA

                    case STATE_WAIT_FOR_CHECKSUM:
                        checksumRx += (kar<<(count*8));

                        if (count++==1) {
                            if (checksum == checksumRx) {
                                resultArrayOutputStream.write(frameArrayOutputStream.toByteArray());
                                frameArrayOutputStream.reset();
                                if ((size > 0) && (len > 0)) {
//System.out.println("KV_DEBUG> (size > 0) "+size);
                                   size -= len;
                                   protocolTimeout = System.currentTimeMillis() + TIMEOUT;
                                }
                                if (size == 0) {
if (DEBUG>=1) System.out.println("KV_DEBUG> return (size == 0)");
                                    sendOut((byte)TRANSMISSION_ACCEPTED);
                                    return new ResponseData(resultArrayOutputStream.toByteArray());
                                }
                                else {
if (DEBUG>=1) System.out.println("KV_DEBUG> STATE_WAIT_FOR_LENGTH (size == "+size+")");
                                    state = STATE_WAIT_FOR_LENGTH;
                                    checksum=0;
                                    interFrameTimeout = System.currentTimeMillis() + timeout;
                                    sendOut((byte)READY_FOR_NEXT_PACKET);
                                }
                            }
                            else {
                                if (retry++>=maxRetries) {
                                    throw new ProtocolConnectionException("receiveFrame() response crc error",CRC_ERROR);
                                }
                                state = STATE_WAIT_FOR_LENGTH;
                                checksum=0;
                                interFrameTimeout = System.currentTimeMillis() + timeout;

                                frameArrayOutputStream.reset();
                                sendOut((byte)CHECKSUM_ERROR);
                            }
                        }
                        break; // STATE_WAIT_FOR_CHECKSUM

                }
            }
            if (System.currentTimeMillis() - protocolTimeout > 0) {
                throw new ProtocolConnectionException("receiveFrame() response timeout error",TIMEOUT_ERROR);
            }
            if (System.currentTimeMillis() - interFrameTimeout > 0) {
                throw new ProtocolConnectionException("receiveFrame() interframe timeout error",TIMEOUT_ERROR);
            }
        }
    }

}